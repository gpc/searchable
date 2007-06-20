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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping

import org.codehaus.groovy.grails.plugins.searchable.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*

/**
 * @author Maurice Nicholson
 */
class DefaultSearchableCompassClassMappingXmlBuilderTests extends GroovyTestCase {
    def mappingXmlBuilder

    void setUp() {
        mappingXmlBuilder = new DefaultSearchableCompassClassMappingXmlBuilder()
    }

    void tearDown() {
        mappingXmlBuilder = null
    }

    void testConvertCamelCaseToLowerCaseDashed() {
        assert mappingXmlBuilder.convertCamelCaseToLowerCaseDashed("refAlias") == "ref-alias"
        assert mappingXmlBuilder.convertCamelCaseToLowerCaseDashed("refComponentAlias") == "ref-component-alias"
    }

    void testBuildClassMappingXml() {
        CompassMappingDescription description
        def is
        def mapping
        def properties
        def references

        // default mapping
        description = new CompassMappingDescription(
            mappedClass: Post,
            properties: [post: [property: true], title: [property: true], comments: [reference: [refAlias: 'Comment']], createdAt: [property: true]]
        )
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias='Post'>
    <id name='id' />
    <reference name='comments' ref-alias='Comment' />
    <property name='createdAt'>
      <meta-data>createdAt</meta-data>
    </property>
    <property name='title'>
      <meta-data>title</meta-data>
    </property>
    <property name='post'>
      <meta-data>post</meta-data>
    </property>
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == SearchableCompassUtils.getDefaultAlias(Post)
        properties = mapping.class.property
        assert properties.size() == 3
        properties.each {
            assert it.@name in ["title", "post", "createdAt"]
            assert it."meta-data" == it.@name
        }
        references = mapping.class.reference
        assert references.size() == 1
        assert references[0].@name == "comments"
        assert references[0].@"ref-alias" == SearchableCompassUtils.getDefaultAlias(Comment)

        // Date format
        description = new CompassMappingDescription(
            mappedClass: Comment,
            properties: [summary: [property: true], comment: [property: true], post: [reference: [refAlias: 'Post']], createdAt: [property: [format: "yyyy-MM-dd'T'HH:mm:ss"]]]
        )
        is = mappingXmlBuilder.buildClassMappingXml(description)
/*<?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Comment' alias='Comment'>
    <id name='id' />
    <property name='summary'>
      <meta-data>summary</meta-data>
    </property>
    <property name='createdAt'>
      <meta-data format='yyyy-MM-dd&apos;T&apos;HH:mm:ss'>createdAt</meta-data>
    </property>
    <property name='comment'>
      <meta-data>comment</meta-data>
    </property>
    <reference name='post' ref-alias='Post' />
  </class>
</compass-core-mapping>*/
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Comment.class.name
        assert mapping.class.@alias == SearchableCompassUtils.getDefaultAlias(Comment)
        properties = mapping.class.property
        assert properties.size() == 3
        properties.each {
            assert it.@name in ["summary", "comment", "createdAt"]
            def metaData = it."meta-data"
            if (it.@name == "createdAt") {
                assert metaData.@format == "yyyy-MM-dd'T'HH:mm:ss"
            }
            assert metaData == it.@name
        }
        references = mapping.class.reference
        assert references.size() == 1
        assert references[0].@name == "post"
        assert references[0].@"ref-alias" == SearchableCompassUtils.getDefaultAlias(Post)

        description = new CompassMappingDescription(
            mappedClass: Post,
            properties: [createdAt: [property: [format: 'yyyyMMdd', excludeFromAll: true]], title: [property: [boost: 2.0f, store: 'compress']], post: [property: [termVector: 'yes']], comments: [reference: [refAlias: 'Comment', propertyConverter: 'commentConverter'], component: [refAlias: 'Comment', cascade: 'create,delete']]]
        )
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias='Post'>
    <id name='id' />
    <property name='createdAt'>
      <meta-data format='yyyyMMdd' exclude-from-all='true'>createdAt</meta-data>
    </property>
    <property name='title'>
      <meta-data store='compress' boost='2.0'>title</meta-data>
    </property>
    <reference ref-alias='Comment' converter='commentConverter' name='comments' />
    <component ref-alias='Comment' cascade='create,delete' name='comments' />
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == SearchableCompassUtils.getDefaultAlias(Post)
        properties = mapping.class.property
        assert properties.size() == 3
        properties.each {
            assert it.@name in ["title", "createdAt", "post"]
            def metaData = it."meta-data"
            if (it.@name == "createdAt") {
                assert metaData.@'exclude-from-all' == "true"
                assert metaData.@format == "yyyyMMdd"
            }
            if (it.@name == "title") {
                assert metaData.@boost == "2.0"
                assert metaData.@store == "compress"
            }
            if (it.@name == "post") {
                assert metaData.@'term-vector' == "yes"
            }
            assert metaData == it.@name
        }
        references = mapping.class.reference
        assert references.size() == 1
        assert references[0].@name == "comments"
        assert references[0].@"ref-alias" == SearchableCompassUtils.getDefaultAlias(Comment)
        assert references[0].@converter == "commentConverter"

        def components = mapping.class.component
        assert components.size() == 1
        assert components[0].@name == "comments"
        assert components[0].@cascade == "create,delete"
        assert components[0].@"ref-alias" == SearchableCompassUtils.getDefaultAlias(Comment)

        shouldFail(IllegalArgumentException) {
            description = new CompassMappingDescription(
                mappedClass: Post,
                properties: [createdAt: [property: [noSuchPropertyOption: 'abc123']]]
            )
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }

        shouldFail(IllegalArgumentException) {
            description = new CompassMappingDescription(
                mappedClass: Post,
                properties: [author: [reference: [noSuchReferenceOption: 'abc123']]]
            )
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }

        shouldFail(IllegalArgumentException) {
            description = new CompassMappingDescription(
                mappedClass: Post,
                properties: [author: [component: [noSuchComponentOption: 'abc123']]]
            )
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }
    }

    private getXmlSlurper(is) {
        // TODO ignore DTD?
        /*
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false) */
        def xmlSlurper = new XmlSlurper(false, false)
        xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
//        try {
            return xmlSlurper.parse(is)
//        } catch (java.net.UnknownHostException uhe) {
  //          return xmlSlurper.parse(is)
   //     }
     //   throw new IllegalStateException("Failed to parse Xml!")
    }
}