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
package org.codehaus.groovy.grails.plugins.searchable.util

/**
 *
 *
 * @author Maurice Nicholson
 */
class MapUtilsTests extends GroovyTestCase {

    void testNullSafeAddMaps() {
        assert MapUtils.nullSafeAddMaps(null, null) == null

        def lhs = [a: 1, b: 2]
        def result = MapUtils.nullSafeAddMaps(lhs, null)
        assert result == lhs
        assert !result.is(lhs)

        def rhs = [x: 24, y: 25, z: 26]
        result = MapUtils.nullSafeAddMaps(null, rhs)
        assert result == rhs
        assert !result.is(rhs)

        result = MapUtils.nullSafeAddMaps(lhs, rhs)
        assert result == [a: 1, b: 2, x: 24, y: 25, z: 26]
        assert !result.is(lhs)
        assert !result.is(rhs)

        rhs = [a: 100, z: 26]
        result = MapUtils.nullSafeAddMaps(lhs, rhs)
        assert result == [a: 100, b: 2, z: 26]
        assert !result.is(lhs)
        assert !result.is(rhs)
    }
}