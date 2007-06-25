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
package pages

/**
 * Abstract super class for zipatrip pages
 */
abstract class AbstractPage extends components.AbstractComponent {

    String xPath = "" // satisfy the contract of extending AbstractComponent

    // Verify this is actually the expected page
    def verifyPage(text) {
        ant.verifyText(text: getVerifyPageText(text))
    }

    // Verify the given messages are present
    def verifyMessages(Object[] args) {
        for (arg in args) {
            ant.verifyText(description: "Message exists", text: arg)
        }
    }

    // Verify that the given text exists anywhere on the page 
    def verifyText(text) {
        ant.verifyText(text: text)
    }

    // Verify the logged-in status is showing for the given username
/*    def verifyLoggedInStatus(name) {
        ant.verifyXPath(xpath: "//div[@id='topUtilityBelt']/span", text: "Welcome, ${name} Logout")
        ant.verifyXPath(xpath: "//div[@id='topUtilityBelt']//a[contains(@href, 'logout')]", text: "Logout")
    }

    // Verify the logged-in status is showing for the given username
    def verifyNotLoggedInStatus(name) {
        ant.verifyXPath(xpath: "//div[@id='topUtilityBelt']//a[contains(@href, 'login')]", text: "Login")
    }

    // Click the login link, assuming there is one, and return a
    // new LoginPage instance
    def clickLoginLink() {
        clickLink("Login", LoginPage)
    }
    
    // Click the logout link
    def clickLogoutLink() {
        clickLink("Logout", null)
    }*/
}
