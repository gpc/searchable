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
package org.codehaus.groovy.grails.plugins.searchable.test.domain.reference

/**
 * A Grails domain class exhibiting all searchable reference types (other domain classes)
 *
 * @author Maurice Nicholson
 */
class SearchableReferenceTypes {
    static searchable = true
    Long id
    Long version

    Referee ref
    static hasMany = [refSet: Referee, refList: Referee, refMap: Referee]
    Set refSet
    List refList
    Map refMap
}