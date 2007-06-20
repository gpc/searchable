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
package grails.plugins.searchable.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.IFileNameFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.ScriptTestAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.lang.reflect.Modifier;

/**
 * Based on the GDK version
 *
 * @author Maurice Nicholson
 */
public class AllTestSuite extends TestSuite {
    /** The System Property to set as base directory for collection of Test Cases.
     * The pattern will be used as an Ant fileset include basedir.
     * Key is "groovy.test.dir".
     * Default value is "./test/".
     */
    public static final String SYSPROP_TEST_DIR = "groovy.test.dir";

    /** The System Property to set as the filename pattern for collection of Test Cases.
     * The pattern will be used as Regualar Expression pattern applied with the find
     * operator agains each candidate file.path.
     * Key is "groovy.test.pattern".
     * Default value is "Test.groovy".
     */
    public static final String SYSPROP_TEST_PATTERN = "groovy.test.pattern";

    private static Logger LOG = Logger.getLogger(AllTestSuite.class.getName());
    private static ClassLoader JAVA_LOADER = AllTestSuite.class.getClassLoader();
    private static GroovyClassLoader GROOVY_LOADER = new GroovyClassLoader(JAVA_LOADER);

    private static final String[] EMPTY_ARGS = new String[]{};
    private static IFileNameFinder FINDER = null;

    private static String basedir;
    static { // this is only needed since the Groovy Build compiles *.groovy files after *.java files
        try {
            Class finderClass = Class.forName("groovy.util.FileNameFinder");
            FINDER = (IFileNameFinder) finderClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find and instantiate class FileNameFinder", e);
        }
    }

    public static Test suite() {
        Thread.currentThread().setContextClassLoader(GROOVY_LOADER);
        basedir = System.getProperty(SYSPROP_TEST_DIR, "./test/");
        String pattern = System.getProperty(SYSPROP_TEST_PATTERN, "**/*Test.groovy");
        return suite(basedir, pattern);
    }

    public static Test suite(String basedir, String pattern) {
        AllTestSuite suite = new AllTestSuite();
        String fileName = "";
        try {
            Collection filenames = FINDER.getFileNames(basedir, pattern);
            for (Iterator iter = filenames.iterator(); iter.hasNext();) {
                fileName = (String) iter.next();
                LOG.finest("trying to load "+ fileName);
                suite.loadTest(fileName);
            }
        } catch (CompilationFailedException e1) {
            e1.printStackTrace();
            throw new RuntimeException("CompilationFailedException when loading "+fileName, e1);
        } catch (IOException e2) {
            throw new RuntimeException("IOException when loading "+fileName, e2);
        }
        return suite;
    }

    protected void loadTest(String fileName) throws CompilationFailedException, IOException {
        Class type = compile(fileName);
        if (!Test.class.isAssignableFrom(type) && Script.class.isAssignableFrom(type)) {
            addTest(new ScriptTestAdapter(type, EMPTY_ARGS));
        } else if (!Modifier.isAbstract(type.getModifiers())) {
            addTestSuite(type);
        }
    }

    protected Class compile(String fileName) throws CompilationFailedException, IOException {
        if (fileName.endsWith(".class")) {
            try {
                String className = fileName;
                String path = new File(basedir).getAbsolutePath();
                className = className.substring(className.indexOf(path) + path.length());
                className = className.replaceAll("\\\\", "/").replaceAll("/", ".").substring(0, className.length() - ".class".length());
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return GROOVY_LOADER.parseClass(new File(fileName));
    }
}
