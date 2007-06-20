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
package org.codehaus.groovy.grails.plugins.searchable.compass.converter

import org.compass.core.config.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
class DefaultCompassConverterLookupHelperTests extends GroovyTestCase {

    void testHasConverter() {
        def helper = new DefaultCompassConverterLookupHelper(
            converterLookup: new CompassConfiguration().setConnection("ram://dummy").buildCompass().converterLookup
        )

        def types = [
            // Primitives
            Long.TYPE,
            Boolean.TYPE,
            Byte.TYPE,
            Character.TYPE,
            Short.TYPE,
            Integer.TYPE,
            Float.TYPE,
            Double.TYPE,

            // Wrappers
            Long,
            Boolean,
            Boolean,
            Character,
            Short,
            Integer,
            Float,
            Double,

            // Value types
            String,
            BigDecimal,
            BigInteger,
            Locale,
            URL,
            File,
            Date,
            Calendar,
            java.sql.Date,
            java.sql.Time,
            java.sql.Timestamp,
            StringBuffer
            // Java 5:
            // StringBuilder
            // Enum
        ]
        for (type in types) {
            assert helper.hasConverter(type), "expected to have converter for ${type}"
        }

        // arrays
        for (type in types) {
            type = java.lang.reflect.Array.newInstance(type, 0).getClass()
            assert helper.hasConverter(type), "expected to have converter for ${type} (array of ${type.componentType}"
        }

        // Others not supported
        types = [
            this.getClass(),
            java.lang.reflect.Field,
            java.text.Format
        ]
        for (type in types) {
            assert helper.hasConverter(type) == false
        }
    }
}