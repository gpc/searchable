/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.converter

import org.compass.core.Resource
import org.compass.core.mapping.Mapping
import org.compass.core.marshall.MarshallingContext
import org.compass.core.converter.mapping.osem.ClassMappingConverter
import org.compass.core.lucene.LuceneProperty
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index

/**
 * @author Maurice Nicholson
 */
class AConverter extends ClassMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) {
        boolean store = super.marshall(resource, root, mapping, context);

        resource.addProperty(
            new LuceneProperty(new Field("converted_by", "a_converter", Store.YES, Index.UN_TOKENIZED))
        )
        return store;
    }
}