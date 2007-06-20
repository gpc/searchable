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
package org.codehaus.groovy.grails.plugins.searchable.test.domain.property

/**
 * A Grails domain class exhibiting all searchable property types (simple types)
 *
 * @author Maurice Nicholson
 */
class SearchablePropertyTypes {
    static searchable = true
    static transients = ['transientProperty']
    static constraints = { // any old constraints
        locale(nullable: false)
        string(blank: false, size: 1..1000)
        anInteger(max: 9999)
        date(nullable: false, unique: true)
    }
    Long id
    Long version

    long aLong
    boolean aBoolean
    Boolean aBooleanObj
    byte aByte
    Byte aByteObj
    char aChar
    Character aCharacter
    short aShort
    Short aShortObj
    int anInt
    Integer anInteger
    float aFloat
    Float aFloatObj
    double aDouble
    Double aDoubleObj

    String string
    BigDecimal bigDecimal
    BigInteger bigInteger
    Locale locale
    URL url
    Date date
    Calendar calendar
//    File file
    java.sql.Date sqlDate
    java.sql.Time sqlTime
    java.sql.Timestamp sqlTimestamp
    StringBuffer buf

    long[] longArray
    Long[] longObjArray
    boolean[] booleanArray
    Boolean[] booleanObjArray
    byte[] byteArray
    Byte[] byteObjArray
    char[] charArray
    Character[] characterArray
    short[] shortArray
    Short[] shortObjArray
    int[] intArray
    Integer[] integerArray
    float[] floatArray
    Float[] floatObjArray
    double[] doubleArray
    Double[] doubleObjArray

    String[] stringArray
    BigDecimal[] bigDecimalArray
    BigInteger[] bigIntegerArray
    Locale[] localeArray
    URL[] urlArray
    Date[] dateArray
    Calendar[] calendarArray
    Class[] clazzArray
//    File[] fileArray
    java.sql.Date[] sqlDateArray
    java.sql.Time[] sqlTimeArray
    java.sql.Timestamp[] sqlTimestampArray
//    StringBuffer[] bufArray

    // Persistent Map: String key and value
    Map stringMap

    // I'm not going overboard on derived property tests because they appear to
    // be identical to "real" properties with the APIs I use, so these are just for santity checks:
    // one from each main category

    // Primitive
    int getDerivedInt() {
        6
    }
    int[] getDerivedIntArray() {
        [900, 300] as int[]
    }

    // Wrapper
    Integer getDerivedInteger() {
        60
    }
    Integer[] getDerivedIntegerArray() {
        [9000, 3000] as Integer[]
    }

    // Value type
    BigDecimal getDerivedBigDecimal() {
        7230.12
    }
    BigDecimal[] getDerivedBigDecimalArray() {
        [1.1, 99.8999999999] as BigDecimal[]
    }

    // Transient property
    String getTransientProperty() {
        "here one minute, gone the next"
    }
}
