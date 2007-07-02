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

    void testBuildClassMappingXmlWithConstantMetatData() {
        CompassClassMapping description
        InputStream is
        def mapping
        def constants
        def properties

        // single meta data, single value, no attributes
        description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post")) // not essentially important here
        description.addConstantMetaData('myextradata', [:], ['thevalue'])
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
<?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias='postalias' root='true'>
    <id name='id' />
    <constant>
      <meta-data>myextradata</meta-data>
      <meta-data-value>thevalue</meta-data-value>
    </constant>
    <property name='post'>
      <meta-data>post</meta-data>
    </property>
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == "postalias"
        properties = mapping.class.property
        assert properties.size() == 1
        assert properties[0].@name == "post"
        assert properties[0]."meta-data" == "post"
        constants = mapping.class.constant
        assert constants.size() == 1
        assert constants[0].'meta-data' == 'myextradata'
        assert constants[0].'meta-data-value' == 'thevalue'
        // TODO Assert no attributes

        // multiple meta datas, single/many values, with attributes
        description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post")) // not essentially important here
        description.addConstantMetaData('singleval', [:], ['value'])
        description.addConstantMetaData('multivalue', [:], ['valueone', 'valuetwo'])
        description.addConstantMetaData('withattrs', [index: 'un_tokenized', termVector: true], ['value'])
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
<?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias='postalias' root='true'>
    <id name='id' />
    <constant>
      <meta-data>singleval</meta-data>
      <meta-data-value>value</meta-data-value>
    </constant>
    <constant>
      <meta-data>multivalue</meta-data>
      <meta-data-value>valueone</meta-data-value>
      <meta-data-value>valuetwo</meta-data-value>
    </constant>
    <constant>
      <meta-data index='un_tokenized' term-vector='true'>withattrs</meta-data>
      <meta-data-value>value</meta-data-value>
    </constant>
    <property name='post'>
      <meta-data>post</meta-data>
    </property>
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == "postalias"
        properties = mapping.class.property
        assert properties.size() == 1
        assert properties[0].@name == "post"
        assert properties[0]."meta-data" == "post"
        constants = mapping.class.constant
        assert constants.size() == 3
        assert constants[0].'meta-data' == 'singleval'
        assert constants[0].'meta-data-value' == 'value'
        // TODO Assert no attributes
        assert constants[1].'meta-data' == 'multivalue'
        assert constants[1].'meta-data-value'[0] == 'valueone'
        assert constants[1].'meta-data-value'[1] == 'valuetwo'
        assert constants[2].'meta-data'.@index == 'un_tokenized'
        assert constants[2].'meta-data'.@'term-vector' == 'true'
        assert constants[2].'meta-data' == 'withattrs'
        assert constants[2].'meta-data-value' == 'value'

        // bad attributes
        shouldFail(IllegalArgumentException) {
            description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
            description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post")) // not essentially important here
            description.addConstantMetaData('badattributes', [noSuchAttribute: 'whatever'], ['thevalue'])
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }
    }

    void testBuildClassMappingXml() {
        CompassClassMapping description
        def is
        def mapping
        def properties
        def references

        // BigDecimal option value mapping
        description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post", [boost: 2.0]))
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias="postalias">
    <id name='id' />
    <property name='post'>
      <meta-data boost='2.0'>post</meta-data>
    </property>
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == "postalias"
        properties = mapping.class.property
        assert properties.size() == 1
        assert properties[0].@name == "post"
        assert properties[0]."meta-data" == "post"
        assert properties[0]."meta-data".@boost == "2.0"

        // float option value mapping
        description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post", [boost: 2.0f]))
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias="postalias">
    <id name='id' />
    <property name='post'>
      <meta-data boost='2.0'>post</meta-data>
    </property>
  </class>
</compass-core-mapping> */
        mapping = getXmlSlurper(is)
        assert mapping.class.@name == Post.class.name
        assert mapping.class.@alias == "postalias"
        properties = mapping.class.property
        assert properties.size() == 1
        assert properties[0].@name == "post"
        assert properties[0]."meta-data" == "post"
        assert properties[0]."meta-data".@boost == "2.0"

        // default mapping
        description = new CompassClassMapping(mappedClass: Post, alias: "postalias")
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("post"))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("title"))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("createdAt"))
        description.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("comments", [refAlias: 'commentalias']))
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias="postalias">
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
        assert mapping.class.@alias == "postalias"
        properties = mapping.class.property
        assert properties.size() == 3
        properties.each {
            assert it.@name in ["title", "post", "createdAt"]
            assert it."meta-data" == it.@name
        }
        references = mapping.class.reference
        assert references.size() == 1
        assert references[0].@name == "comments"
        assert references[0].@"ref-alias" == 'commentalias'

        // Date format
        description = new CompassClassMapping(mappedClass: Comment, alias: 'commentalias')
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("summary"))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("comment"))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("createdAt", [format: "yyyy-MM-dd'T'HH:mm:ss"]))
        description.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("post", [refAlias: 'postalias']))
        is = mappingXmlBuilder.buildClassMappingXml(description)
/*<?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Comment' alias='commentalias'>
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
        assert mapping.class.@alias == 'commentalias'
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
        assert references[0].@"ref-alias" == 'postalias'

        description = new CompassClassMapping(mappedClass: Post, alias: 'postalias')
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance("createdAt", [format: 'yyyyMMdd', excludeFromAll: true]))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance('title', [boost: 2.0f, store: 'compress']))
        description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance('post', [termVector: 'yes']))
        description.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance('comments', [refAlias: 'commentalias', propertyConverter: 'commentConverter']))
        description.addPropertyMapping(CompassClassPropertyMapping.getComponentInstance('comments', [refAlias: 'commentalias', cascade: 'create,delete']))
        is = mappingXmlBuilder.buildClassMappingXml(description)
        /*
        <?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
<compass-core-mapping>
  <class name='org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post' alias='postalias'>
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
        assert mapping.class.@alias == 'postalias'
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
        assert references[0].@"ref-alias" == 'commentalias'
        assert references[0].@converter == "commentConverter"

        def components = mapping.class.component
        assert components.size() == 1
        assert components[0].@name == "comments"
        assert components[0].@cascade == "create,delete"
        assert components[0].@"ref-alias" == 'commentalias'

        shouldFail(IllegalArgumentException) {
            description = new CompassClassMapping(mappedClass: Post, alias: 'postalias')
            description.addPropertyMapping(CompassClassPropertyMapping.getPropertyInstance('createdAt', [noSuchPropertyOption: 'abc123']))
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }

        shouldFail(IllegalArgumentException) {
            description = new CompassClassMapping(mappedClass: Post, alias: 'postalias')
            description.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance('author', [noSuchReferenceOption: 'abc123']))
            is = mappingXmlBuilder.buildClassMappingXml(description)
        }

        shouldFail(IllegalArgumentException) {
            description = new CompassClassMapping(mappedClass: Post, alias: 'postalias')
            description.addPropertyMapping(CompassClassPropertyMapping.getComponentInstance('author', [noSuchComponentOption: 'abc123']))
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