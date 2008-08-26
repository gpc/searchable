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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.mixed

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class MixedMappingTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [PluginSearchableProperty, CompassXml]
    }

    void testMixedMappings() {
        // these domain classes reference each other and have defined their own
        // aliases, so this test shows that aliases are binded late in mapping

        new PluginSearchableProperty(id: 1L, value: "mapped by plugin searchable property").index()
        assert searchableService.searchEvery('alias:searchableproperty').size() == 1

        new CompassXml(id: 1L, value: "mapped by Compass native XML").index()
        assert searchableService.searchEvery('alias:xml').size() == 1
    }
}
