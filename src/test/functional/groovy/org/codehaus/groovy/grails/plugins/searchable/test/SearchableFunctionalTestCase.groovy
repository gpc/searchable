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
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.factory.config.MapFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.io.Resource
import org.springframework.web.util.WebUtils

/**
 * @author Maurice Nicholson
 */
abstract class SearchableFunctionalTestCase extends GroovyTestCase {
    private String grailsEnv
    private List injectedPropertiesNames = []
    private ApplicationContext applicationContext
    private GrailsApplication application

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
                isolatedTest.runBare()
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

    void setUp() {
        capturePreTestEnvironment()
        applyTestEnvironment()

        ClassLoader classClassLoader = this.getClass().getClassLoader()
        def gclClass = classClassLoader.loadClass(GroovyClassLoader.getName())
        def constructor = gclClass.getConstructor(ClassLoader)
        def cl = constructor.newInstance(classClassLoader)

        def registry = GroovySystem.metaClassRegistry
        if(!(registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle)) {
            registry.setMetaClassCreationHandle(new ExpandoMetaClassCreationHandle());
        }

        def searchableConfigMap = getSearchableConfig(cl)
        def pluginClasses = getPluginClasses(cl)
        def pluginXmlResources = getPluginResources(cl)
        def appClasses = getAppClasses(cl)

        def builder = new BeanBuilder()
        builder.beans {
            if (searchableConfigMap) {
                searchableConfig(LinkedHashMap, searchableConfigMap)
            }

            grailsApplication(DefaultGrailsApplication, appClasses, cl)

            pluginManager(DefaultGrailsPluginManager, pluginClasses, grailsApplication)

            pluginMetaManager(DefaultPluginMetaManager, pluginXmlResources)
        }
        def ctx = builder.createApplicationContext()

        def pluginManager = ctx.getBean("pluginManager")
//        PluginManagerHolder.setPluginManager(pluginManager)

//        pluginManager.applicationContext = webAppCtx

        def servletContext = getTestServletContext()
//        webAppCtx.servletContext = servletContext

        application = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class)
        applyGrailsConfiguration(application, cl)

        def configurator = new GrailsRuntimeConfigurator(application, ctx)
        PluginManagerHolder.setPluginManager(pluginManager)
        configurator.pluginManager = pluginManager

        applicationContext = configurator.configure(servletContext)

        injectBeanReferences(applicationContext)
    }

    void tearDown() {
        removeInjectedBeanReferences()

        GroovyClassLoader classLoader = application.getClassLoader();
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        Class[] loadedClasses = classLoader.getLoadedClasses();
        for (int i = 0; i < loadedClasses.length; i++) {
            Class loadedClass = loadedClasses[i];
            metaClassRegistry.removeMetaClass(loadedClass);
        }

        GrailsPluginManager pluginManager = PluginManagerHolder.currentPluginManager();
        pluginManager.shutdown();

        while (applicationContext) {
            def tmp = applicationContext.parent
            applicationContext.close()
            applicationContext = tmp
        }

//        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        ApplicationHolder.setApplication(null);
        ServletContextHolder.setServletContext(null);
        PluginManagerHolder.setPluginManager(null);
        ConfigurationHolder.setConfig(null);
        ExpandoMetaClass.disableGlobally();

        applicationContext = null
        injectedPropertiesNames = null
        application = null

        restorePreTestEnvironment()
    }

    private void injectBeanReferences(ApplicationContext ctx) {
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

    private void removeInjectedBeanReferences() {
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
    }

    private void restorePreTestEnvironment() {
        if (grailsEnv != null) {
            System.properties[GrailsApplication.ENVIRONMENT] = grailsEnv
        } else {
            System.getProperties().remove(GrailsApplication.ENVIRONMENT)
        }
    }

    protected void applyGrailsConfiguration(GrailsApplication grailsApplication, cl) {
        def dataSourceClass = getDataSourceClass(cl)
        ConfigSlurper configSlurper = new ConfigSlurper(GrailsUtil.getEnvironment())
        def config = grailsApplication.config
        config.merge(configSlurper.parse(dataSourceClass));
    }

    private Resource[] getPluginResources(cl) {
        def resourceLoader = new GenericApplicationContext()
        def standardResources = resourceLoader.getResources("classpath*:**/plugins/*/plugin.xml") as List

        return (standardResources + resourceLoader.getResource(getPluginHomeFileResourcePrefix(cl) + "/plugin.xml")) as Resource[]
    }

    private String getPluginHomeFileResourcePrefix(cl) {
        def pluginHome = getPluginHome(cl)
        return "file://" + pluginHome.absolutePath.replaceAll("\\\\", "/")
    }

    private Class[] getAppClasses(GroovyClassLoader cl) {
        def pluginHome = getPluginHome(cl)
        def serviceClass = cl.parseClass(new File(pluginHome, "grails-app/services/SearchableService.groovy"))
        def domainClasses = getDomainClasses()
        return [serviceClass] + domainClasses
    }

    /**
     * Provide a List of user domain classes
     */
    // todo rename to getDomainClazzes to be in keeping with Grails lingo
    // todo make return type Collection since part of API
    // todo make protected since part of API
    abstract getDomainClasses();

    private ServletContext getTestServletContext() {
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
            }
        ] as ServletContext
        servletContext.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(System.properties['java.io.tmpdir']))
        servletContext
    }

    private Class getDataSourceClass(cl) {
        return DataSource.class
    }

    private Class[] getPluginClasses(cl) {
        def pluginHome = getPluginHome(cl)
        Properties props = new Properties()
        File metadataFile = new File(pluginHome, "application.properties")
        assert metadataFile.exists(), "Missing metadata file ${metadataFile.absolutePath}"
        props.load(new FileInputStream(metadataFile))
        String pluginFileName = props['app.name']
        pluginFileName = pluginFileName.substring(0, 1).toUpperCase() + pluginFileName.substring(1) + "GrailsPlugin.groovy"
        def pluginFile = new File(pluginHome, pluginFileName)
        assert pluginFile.exists(), "Plugin file not found: ${pluginFile.absolutePath}"
        return [cl.parseClass(pluginFile)] as Class[]
    }

    protected File getPluginHome(cl) {
        String resourceBaseName = this.getClass().getName().replaceAll("\\.", "/")
        def url = cl.getResource(resourceBaseName + ".class")
        if (!url) {
            url = cl.getResource(this.getClass().getName().replaceAll("\\.", "/") + ".groovy")
        }
        assert url != null, "Failed to locate this class as resource! ${this.getClass().getName()}"
        for (def dir = new File(URLDecoder.decode(url.getFile())); dir; dir = dir.getParentFile()) {
            if (new File(dir, "grails-app").isDirectory()) {
                return dir;
            }
        }
        assert false, "plugin home was not found!"
    }

    protected Map getSearchableConfig(ClassLoader cl) {
        def slurper = new ConfigSlurper()
        try {
            String resourceBaseName = this.getClass().getPackage().getName().replaceAll("\\.", "/")
            Class configClass = cl.loadClass(resourceBaseName + "/" + "SearchableConfig.class")

            return slurper.parse(configClass)
        } catch (ClassNotFoundException e) {
        }
        try {
            String resourceBaseName = this.getClass().getPackage().getName().replaceAll("\\.", "/")
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
