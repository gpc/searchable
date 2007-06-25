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
package components

/**
 * Abstract superclass for specific page components
 *
 * @author Maurice Nicholson
 */
abstract class AbstractComponent {
    int index = -1 // if in a list
    Object parent // if in a context
    Object ant

    // Gets the accumlated Xpath for this component
    def getXPath() {
        def tmp = ""
        if (parent != null) { // use "!= null" because even empty list is "true"
            tmp += parent.getXPath()
        }
        tmp += xPath
        if (index > -1 && index != null) { // "0" is false, so check for not null
            tmp += "[${index + 1}]"
        }
        return tmp
    }
    
    // Click the first link with the given text content
    def clickLink(text, pageClass) {
        return clickLinkForXPath(getXPath() + "//a[text()='${text}']", pageClass)
    }

    // Click the link matching the xpath
    def clickLinkForXPath(xpath, pageClass) {
        ant.clickLink(xpath: xpath)
        if (pageClass) {
            def page = pageClass.newInstance()
            page.ant = ant
            return page
        }
    }

    // Verify it exists
    def verifyPresent() {
        ant.verifyXPath(xpath: getXPath())
    }
}
