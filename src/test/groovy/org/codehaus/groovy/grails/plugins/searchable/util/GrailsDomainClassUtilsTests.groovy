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

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

/**
* @author Maurice Nicholson
*/
class GrailsDomainClassUtilsTests extends GroovyTestCase {
    def a, b, aa, aaa, gcl

    void setUp() {
        gcl = new GroovyClassLoader()
        a = gcl.parseClass("class A { Long id; Long version; String aStr; }")
        b = gcl.parseClass("class B { Long id; Long version; String bStr; }")
        aa = gcl.parseClass("class Aa extends A { String aaStr; }")
        aaa = gcl.parseClass("class Aaa extends Aa { String aaaStr; }")
    }

    void tearDown() {
        gcl = a = b = aa = aaa = null
    }

    void testGetSubClasses() {
        def ga = new DefaultGrailsApplication([a, aa, aaa, b] as Class[], gcl)
        def adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        def aadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aa")
        def aaadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aaa")
        def bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        assert GrailsDomainClassUtils.getSubClasses(adc, []) == [] as Set
        assert GrailsDomainClassUtils.getSubClasses(adc, [adc, aadc, aaadc, bdc]) == [aadc, aaadc] as Set
        assert GrailsDomainClassUtils.getSubClasses(aadc, [adc, aadc, aaadc, bdc]) == [aaadc] as Set
        assert GrailsDomainClassUtils.getSubClasses(aaadc, [adc, aadc, aaadc, bdc]) == [] as Set
        assert GrailsDomainClassUtils.getSubClasses(bdc, [adc, aadc, aaadc, bdc]) == [] as Set
    }

    void testGetSuperClass() {
        GrailsApplication ga

        ga = new DefaultGrailsApplication([a] as Class[], gcl)
        def adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")

        assert GrailsDomainClassUtils.getSuperClass(adc, []) == null
        assert GrailsDomainClassUtils.getSuperClass(adc, [adc]) == null

        ga = new DefaultGrailsApplication([a, b] as Class[], gcl)
        adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        def bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        assert GrailsDomainClassUtils.getSuperClass(bdc, [adc, bdc]) == null

        // 1 level of extension
        ga = new DefaultGrailsApplication([a, aa, b] as Class[], gcl)
        adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        def aadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aa")
        assert adc.getSubClasses().size() > 0
        assert GrailsDomainClassUtils.getSuperClass(adc, [adc, aadc, bdc]) == null
        assert GrailsDomainClassUtils.getSuperClass(aadc, [adc, aadc, bdc]) == adc

        // 2 levels of extension
        ga = new DefaultGrailsApplication([a, aa, aaa, b] as Class[], gcl)
        adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        aadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aa")
        def aaadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aaa")
        assert adc.getSubClasses().size() == 2
        assert aadc.getSubClasses().size() == 1
        assert GrailsDomainClassUtils.getSuperClass(adc, [adc, aadc, aaadc, bdc]) == null
        assert GrailsDomainClassUtils.getSuperClass(aadc, [adc, aadc, aaadc, bdc]) == adc
        assert GrailsDomainClassUtils.getSuperClass(aaadc, [adc, aadc, aaadc, bdc]) == aadc
        assert GrailsDomainClassUtils.getSuperClass(bdc, [adc, aadc, aaadc, bdc]) == null

        // Superclss not in given collection
    }

    void testGetSuperClasses() {
        // 2 levels of extension
        def ga = new DefaultGrailsApplication([a, aa, aaa, b] as Class[], gcl)
        def adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        def aadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aa")
        def aaadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aaa")
        def bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        assert adc.getSubClasses().size() == 2
        assert aadc.getSubClasses().size() == 1
        assert GrailsDomainClassUtils.getSuperClasses(bdc, [adc, aadc, aaadc, bdc]) == [] as Set
        assert GrailsDomainClassUtils.getSuperClasses(adc, [adc, aadc, aaadc, bdc]) == [] as Set
        assert GrailsDomainClassUtils.getSuperClasses(aadc, [adc, aadc, aaadc, bdc]) == [adc] as Set
        assert GrailsDomainClassUtils.getSuperClasses(aaadc, [adc, aadc, aaadc, bdc]) == [adc, aadc] as Set
    }

    void testGetClazzes() {
        assert GrailsDomainClassUtils.getClazzes(null) == [] as Set
        assert GrailsDomainClassUtils.getClazzes([]) == [] as Set
        assert GrailsDomainClassUtils.getClazzes([new DefaultGrailsDomainClass(a), new DefaultGrailsDomainClass(b), new DefaultGrailsDomainClass(aa)]) == [a, b, aa] as Set
    }

    void testGetGrailsDomainClass() {
        def adc = new DefaultGrailsDomainClass(a)
        def aadc = new DefaultGrailsDomainClass(aa)
        def aaadc = new DefaultGrailsDomainClass(aaa)
        def bdc = new DefaultGrailsDomainClass(b)
        assert GrailsDomainClassUtils.getGrailsDomainClass(a, []) == null
        assert GrailsDomainClassUtils.getGrailsDomainClass(a, [adc, aadc, aaadc, bdc]) == adc
        assert GrailsDomainClassUtils.getGrailsDomainClass(aaa, [adc, aadc, aaadc, bdc]) == aaadc
        assert GrailsDomainClassUtils.getGrailsDomainClass(b, [adc, aadc, aaadc, bdc]) == bdc
    }

    void testIsWithinInhertitanceHierarchy() {
        def ga = new DefaultGrailsApplication([a, aa, aaa, b] as Class[], gcl)
        def adc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "A")
        def aadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aa")
        def aaadc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "Aaa")
        def bdc = ga.getArtefact(DomainClassArtefactHandler.TYPE, "B")
        assert GrailsDomainClassUtils.isWithinInhertitanceHierarchy(adc, []) == false
        assert GrailsDomainClassUtils.isWithinInhertitanceHierarchy(adc, [adc, aadc, aaadc, bdc]) == true
        assert GrailsDomainClassUtils.isWithinInhertitanceHierarchy(aadc, [adc, aadc, aaadc, bdc]) == true
        assert GrailsDomainClassUtils.isWithinInhertitanceHierarchy(aaadc, [adc, aadc, aaadc, bdc]) == true
        assert GrailsDomainClassUtils.isWithinInhertitanceHierarchy(bdc, [adc, aadc, aaadc, bdc]) == false
    }
}