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

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.ScriptTestAdapter;

import java.io.File;

/**
 * Based on the GDK version
 *
 * @author Maurice Nicholson
 */
public class GroovyTestSuite extends TestSuite {

    protected static String file = null;

    protected static GroovyClassLoader LOADER = new GroovyClassLoader(GroovyTestSuite.class.getClassLoader());

    public static void main(String[] args) {
        Thread.currentThread().setContextClassLoader(LOADER);
        if (args.length > 0) {
            file = args[0];
        }
        TestRunner.run(suite());
    }

    public static Test suite() {
        GroovyTestSuite suite = new GroovyTestSuite();
        try {
            suite.loadTestSuite();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the test suite: " + e, e);
        }
        return suite;
    }

    public void loadTestSuite() throws Exception {
        String fileName = System.getProperty("test", file);
        if (fileName == null) {
            throw new RuntimeException("No filename given in the 'test' system property so cannot run a Groovy unit test");
        }
        System.out.println("Compiling: " + fileName);
        Class type = compile(fileName);
        String[] args = {};
        if (!Test.class.isAssignableFrom(type) && Script.class.isAssignableFrom(type)) {
            // lets treat the script as a Test
            addTest(new ScriptTestAdapter(type, args));
        } else {
            addTestSuite(type);
        }
    }

    public Class compile(String fileName) throws Exception {
        return LOADER.parseClass(new File(fileName));
    }
}

