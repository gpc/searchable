/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.plugins.searchable.test

import grails.spring.BeanBuilder
import grails.util.GrailsUtil
import java.lang.reflect.Constructor
import javax.servlet.ServletContext
import junit.framework.AssertionFailedError
import junit.framework.TestCase
import junit.framework.TestResult
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator
import org.codehaus.groovy.grails.plugins.DefaultGrailsPluginManager
import org.codehaus.groovy.grails.plugins.DefaultPluginMetaManager
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.plugins.searchable.test.DataSource
import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCaseClassLoader
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.beans.BeanWrapperImpl
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.io.Resource
import org.springframework.util.ClassUtils
import org.springframework.web.util.WebUtils
import grails.util.BuildSettings
import grails.util.BuildSettingsHolder
import grails.util.Metadata
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder
import org.springframework.beans.BeanWrapper
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.springframework.beans.BeanUtils
import java.beans.PropertyEditor
import java.beans.PropertyEditorSupport
import org.codehaus.groovy.runtime.GStringImpl
import java.util.zip.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils

/**
 * @author Maurice Nicholson
 */
abstract class SearchableFunctionalTestCase extends GroovyTestCase {
    private String grailsEnv
    private List injectedPropertiesNames = []
    private ApplicationContext applicationContext
    private GrailsApplication application
    private GrailsPluginManager pluginManager

    /**
     * Runs the test case and collects the results in TestResult.
     * This overrides the usual implementation to create a new instance of this test
     * with an isolated classloader, in order to avoid problems with stale meta classes
     * on classes required by the test
     */
    public void run(TestResult result) {
        try {
            ClassLoader cl = new SearchableFunctionalTestCaseClassLoader(this.getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(cl)

            Class newClass = cl.loadClass(this.getClass().getName())
            TestCase isolatedTest = newClass.newInstance()
            isolatedTest.setName(this.getName())

            result.startTest(this)
            try {
                try {
                    isolatedTest.beforeTest()
                    isolatedTest.runBare()
                } finally {
                    isolatedTest.afterTest()
                }
            } catch (ThreadDeath e) { // don't catch ThreadDeath by accident
                throw e
            } catch (AssertionFailedError e) {
                result.addFailure(this, e)
            } catch (Throwable e) {
                result.addError(this, e)
            }

            result.endTest(this)
        } catch (Throwable e) {
            throw new RuntimeException(e)
        } finally {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader())
        }
    }

    protected void beforeTest() {
        capturePreTestEnvironment()
        applyTestEnvironment()

        ClassLoader classClassLoader = this.getClass().getClassLoader()
        preloadResourcesClass(classClassLoader)

        GroovyClassLoader cl = new GroovyClassLoader(classClassLoader)
        def searchableConfigMap = getSearchableConfig(cl)
        def pluginClasses = getPluginAndExternalPluginClasses(cl)
        def serviceClasses = getServiceClasses(cl)
        def domainClasses = getDomainClasses()

        def builder = new BeanBuilder()
        builder.beans {
            if (searchableConfigMap) {
                searchableConfig(LinkedHashMap, searchableConfigMap)
            }

            grailsApplication(DefaultGrailsApplication, domainClasses + serviceClasses, cl)

            pluginManager(DefaultGrailsPluginManager, pluginClasses, grailsApplication)

            pluginMetaManager(DefaultPluginMetaManager)
        }
        def ctx = builder.createApplicationContext()

        application = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID)
        ApplicationHolder.setApplication(application)
        applyGrailsConfiguration(application, cl)

        pluginManager = ctx.getBean("pluginManager")
        PluginManagerHolder.setPluginManager(pluginManager)

        def configurator = new GrailsRuntimeConfigurator(application, ctx)
        configurator.setPluginManager(pluginManager)
        applicationContext = configurator.configure(createTestServletContext(cl))

        // restore the standard Groovy Map arg constructor that allows setting id property, etc
        for (domainClass in ctx.getBean("grailsApplication").domainClasses) {
            GrailsDomainClass dc = domainClass
            def mc = domainClass.metaClass

            mc.constructor = { Map map ->
                def instance = ctx.containsBean(dc.fullName) ? ctx.getBean(dc.fullName) : BeanUtils.instantiateClass(dc.clazz)
                BeanWrapper bean = new BeanWrapperImpl(instance)
//                bean.registerCustomEditor(GStringImpl.class, new GStringPropertyEditor())
                map.each { k, v ->
                    if (v instanceof GString) {
                        map[k] = v.toString()
                    }
                }
                bean.setPropertyValues(map)
                return instance
            }
        }

        injectSpringBeans(applicationContext)
    }

    protected void afterTest() {
        removeInjectedBeans()
        restorePreTestEnvironment()

        if (pluginManager) {
            pluginManager.shutdown()
            pluginManager = null
        }

        while (applicationContext) {
            def tmp = applicationContext.parent
            applicationContext.close()
            applicationContext = tmp
        }

        ApplicationHolder.setApplication(null)
        ServletContextHolder.setServletContext(null)
        PluginManagerHolder.setPluginManager(null)
        ConfigurationHolder.setConfig(null)
        ExpandoMetaClass.disableGlobally()

        applicationContext = null
        injectedPropertiesNames = null
        application = null
    }

    private void injectSpringBeans(ApplicationContext ctx) {
        def wrapper = new BeanWrapperImpl(this)
        def propertyDescriptors = wrapper.getPropertyDescriptors()
        for (propertyDescriptor in propertyDescriptors) {
            String name = propertyDescriptor.name
            if (ctx.containsBean(name)) {
                wrapper.setPropertyValue(name, ctx.getBean(name))
                injectedPropertiesNames << name
            } else if (name == "applicationContext") {
                wrapper.setPropertyValue(name, ctx)
                injectedPropertiesNames << name
            }
        }
    }

    private void removeInjectedBeans() {
        def wrapper = new BeanWrapperImpl(this)
        for (name in injectedPropertiesNames) {
            wrapper.setPropertyValue(name, null)
        }
    }

    private void capturePreTestEnvironment() {
        grailsEnv = System.properties[GrailsApplication.ENVIRONMENT]
    }

    private void applyTestEnvironment() {
        System.properties[GrailsApplication.ENVIRONMENT] = GrailsApplication.ENV_TEST

        def registry = GroovySystem.metaClassRegistry
        if(!(registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle)) {
            registry.setMetaClassCreationHandle(new ExpandoMetaClassCreationHandle());
        }

    }

    private void restorePreTestEnvironment() {
        if (grailsEnv != null) {
            System.properties[GrailsApplication.ENVIRONMENT] = grailsEnv
        } else {
            System.getProperties().remove(GrailsApplication.ENVIRONMENT)
        }

        if (!application) return

        GroovyClassLoader classLoader = application.getClassLoader();
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        Class[] loadedClasses = classLoader.getLoadedClasses();
        for (int i = 0; i < loadedClasses.length; i++) {
            Class loadedClass = loadedClasses[i];
            metaClassRegistry.removeMetaClass(loadedClass);
        }
    }

    protected void applyGrailsConfiguration(GrailsApplication grailsApplication, cl) {
        def dataSourceClass = getDataSourceClass(cl)
        ConfigSlurper configSlurper = new ConfigSlurper(GrailsUtil.getEnvironment())
        def config = grailsApplication.config
        config.merge(configSlurper.parse(dataSourceClass))
    }

    private Collection<Class> getServiceClasses(GroovyClassLoader cl) {
        def pluginHome = getPluginHome(cl)
        return new File(pluginHome, "grails-app/services").listFiles().findAll { f -> f.name.endsWith("Service.groovy") }.collect { cl.parseClass(it) }
    }

    /**
     * Provide a List of user domain classes
     */
    protected abstract Collection<Class<?>> getDomainClasses()

    private ServletContext createTestServletContext(ClassLoader cl) {
        def attributes = [:]
        def servletContext = [
            setAttribute: { String name, Object value ->
                attributes[name] = value
            },
            getAttribute: { String name ->
                assert attributes.containsKey(name), "ServletContext attribute [${name}] was not set"
                attributes[name]
            },
            getResource: { String name ->
                return null
            },
            getResourceAsStream: { String name ->
                return null
            },
            toString: { 'dummy context' }
        ] as ServletContext
        servletContext.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(System.properties['java.io.tmpdir']))
        servletContext
    }

    private Class getDataSourceClass(cl) {
        return DataSource.class
    }

    private Class[] getPluginAndExternalPluginClasses(GroovyClassLoader cl) {
        // Find the plugin class for this plugin
        def pluginHome = getPluginHome(cl)
        Properties props = new Properties()
        File metadataFile = new File(pluginHome, "application.properties")
        assert metadataFile.exists(), "Missing metadata file ${metadataFile.absolutePath}"
        props.load(new FileInputStream(metadataFile))
        String pluginFileName = props['app.name']
        pluginFileName = pluginFileName[0].toUpperCase() + pluginFileName.substring(1) + "GrailsPlugin.groovy"
        def pluginFile = new File(pluginHome, pluginFileName)
        assert pluginFile.exists(), "Plugin file not found: ${pluginFile.absolutePath}"
        def pluginClasses = [cl.parseClass(pluginFile)]

        // Locate and load dependent plugins
        def grailsVersion = props['app.grails.version']
        def plugins = props.findAll { name, value -> name.startsWith("plugins.") }.collect { name, value -> [name: name[8..-1], version: value] }
        plugins.each { plugin ->
            def pluginZip = [System.properties['user.home'], ".grails", grailsVersion, "plugins", "grails-${plugin.name}-${plugin.version}.zip"].join(File.separator)
            cl.addURL(new URL("file://" + pluginZip))
            def pluginClassName = plugin.name[0].toUpperCase() + plugin.name[1..-1] + "GrailsPlugin"
            pluginClasses << cl.loadClass(pluginClassName)
        }
        return pluginClasses as Class[]
    }

    protected File getPluginHome(cl) {
        String resourceBaseName = this.getClass().getName().replaceAll("\\.", "/")
        def url = cl.getResource(resourceBaseName + ".class")
        if (!url) {
            url = cl.getResource(resourceBaseName + ".groovy")
        }
        assert url != null, "Failed to locate this class as resource! ${this.getClass().getName()}"
        for (def dir = new File(URLDecoder.decode(url.getFile())); dir; dir = dir.getParentFile()) {
            if (new File(dir, "grails-app").isDirectory()) {
                return dir;
            }
        }
        assert false, "plugin home was not found!"
    }

    protected void preloadResourcesClass(ClassLoader cl) {
        try {
            def packageName = ClassUtils.getPackageName(this.getClass())
            def clazz = cl.loadClass(packageName + "." + "resources")
            if (clazz != null) {
                cl.addPreloadedClass(GrailsRuntimeConfigurator.SPRING_RESOURCES_CLASS, clazz)
            }
        } catch (ClassNotFoundException ex) {
            // ignore
        }
    }

    protected Map getSearchableConfig(ClassLoader cl) {
        def slurper = new ConfigSlurper()
        def packageName = ClassUtils.getPackageName(this.getClass())
        try {
            String resourceBaseName = packageName.replaceAll("\\.", "/")
            Class configClass = cl.loadClass(resourceBaseName + "/" + "SearchableConfig.class") // just for tests!
            return slurper.parse(configClass)
        } catch (ClassNotFoundException e) {
        }
        try {
            String resourceBaseName = packageName.replaceAll("\\.", "/")
            def is = cl.getResourceAsStream(resourceBaseName + "/" + "compass-settings.properties")
            if (is != null) {
                def props = new Properties()
                props.load(is)
//                System.out.println("${this.getClass().getName()}: compass-settings.properties is ${props}")

                def searchableConfig = getTestConfig()
                searchableConfig.searchable.compassSettings = slurper.parse(props)
                return searchableConfig
//            } else {
//                System.out.println("${this.getClass().getName()}: NO compass-settings.properties")
            }
        } catch (ClassNotFoundException e) {
        }
        return getTestConfig()
    }

    protected Map getTestConfig() {
        return [searchable: [compassConnection: "ram://functional-test-index", bulkIndexOnStartup: false, mirrorChanges: false]]
    }
}
