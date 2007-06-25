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
 * Models a form
 *
 * @author Maurice Nicholson
 */
class FormComponent extends AbstractComponent {
    /** The form name */
    String formName

    /**
     * Verify input fields contain values as indicated
     * by the given map
     *
     * @param args a map of input-field-name/value pairs
     */
    def verifyInputFields(args) {
        args.each { key, value ->
            ant.verifyInputField(name: key, value: value)
        }
    }

    /**
     * Submit the form
     *
     * @param fields a map of input field name/values
     */
    def submit(fields) {
        return submit(fields, null)
    }

    /**
     * Submit the form
     *
     * @param fields a map of input field name/values
     * @param pageClass the type of page returned for this form submission
     * @return a new instance of the page class
     */
    def submit(fields, pageClass) {
        ant.selectForm(name: formName)
        fields.each { key, value ->
            ant.setInputField(name: key, value: value)
        }
        ant.clickButton(xpath: "//input[@type='submit']")

        if (pageClass) {
            def page = pageClass.newInstance()
            page.ant = ant
            return page
        }
    }
}