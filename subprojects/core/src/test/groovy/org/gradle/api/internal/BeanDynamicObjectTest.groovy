/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal

import spock.lang.Specification

class BeanDynamicObjectTest extends Specification {
    def "can get value of property of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("prop")
        dynamicObject.getProperty("prop") == "value"
    }

    def "can get value of read only property of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("readOnly")
        dynamicObject.getProperty("readOnly") == "read-only"
    }

    def "can get metaClass of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("metaClass")
        dynamicObject.getProperty("metaClass") == bean.metaClass
    }

    def "can get property of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.getProperty("prop") == "value"
        dynamicObject.getProperty("dyno") == "ok"
    }

    def "can only check for static properties of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("prop")
        !dynamicObject.hasProperty("dyno")
    }

    def "can get property of closure delegate via closure instance"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def cl = {}
        cl.delegate = bean
        def dynamicObject = new BeanDynamicObject(cl)

        expect:
        dynamicObject.getProperty("prop") == "value"
        dynamicObject.getProperty("dyno") == "ok"
    }

    def "fails when get value of unknown property of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.getProperty("unknown")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "unknown"
        e.type == Bean
        e.message == "Could not get unknown property 'unknown' for object of type ${Bean.name}."
    }

    def "fails when get value of unknown property of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.getProperty("unknown")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "unknown"
        e.type == BeanWithDynamicProperties
        e.message == "Could not get unknown property 'unknown' for object of type ${BeanWithDynamicProperties.name}."
    }

    def "fails when get value of write only property of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.getProperty("writeOnly")

        then:
        def e = thrown(GroovyRuntimeException)
        e.message == "Cannot get the value of write-only property 'writeOnly' for object of type ${Bean.name}."
    }

    def "fails when get value of property of dynamic groovy object and no dynamic requested"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean).withNotImplementsMissing()

        expect:
        dynamicObject.getProperty("prop") == "value"

        when:
        dynamicObject.getProperty("dyno")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "dyno"
        e.type == BeanWithDynamicProperties
        e.message == "Could not get unknown property 'dyno' for object of type ${BeanWithDynamicProperties.name}."
    }

    def "includes toString() of bean in property get error message when has custom implementation"() {
        def bean = new Bean() {
            @Override
            String toString() {
                return "<bean>"
            }
        }
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.getProperty("unknown")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "unknown"
        e.type == bean.getClass()
        e.message == "Could not get unknown property 'unknown' for <bean> of type ${bean.getClass().name}."
    }

    def "can set value of property of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.setProperty("prop", "value")

        then:
        bean.prop == "value"
    }

    def "can set value of write only property of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("writeOnly")

        when:
        dynamicObject.setProperty("writeOnly", "value")

        then:
        bean.prop == "value"
    }

    def "can set property of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.setProperty("dyno", "value")

        then:
        noExceptionThrown()
    }

    def "can set property of closure delegate via closure instance"() {
        def bean = new BeanWithDynamicProperties()
        def cl = {}
        cl.delegate = bean
        def dynamicObject = new BeanDynamicObject(cl)

        when:
        dynamicObject.setProperty("prop", "value")

        then:
        bean.prop == "value"
    }

    def "fails when set value of unknown property of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.setProperty("unknown", "value")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "unknown"
        e.type == Bean
        e.message == "Could not set unknown property 'unknown' for object of type ${Bean.name}."
    }

    def "fails when set value of read only property of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasProperty("readOnly")

        when:
        dynamicObject.setProperty("readOnly", "value")

        then:
        def e = thrown(GroovyRuntimeException)
        e.message == "Cannot set the value of read-only property 'readOnly' for object of type ${Bean.name}."
    }

    def "fails when set value of unknown property of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties()
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.setProperty("unknown", 12)

        then:
        def e = thrown(MissingPropertyException)
        e.property == "unknown"
        e.type == BeanWithDynamicProperties
        e.message == "Could not set unknown property 'unknown' for object of type ${BeanWithDynamicProperties.name}."
    }

    def "fails when set value of property of dynamic groovy object and no dynamic requested"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean).withNotImplementsMissing()

        expect:
        dynamicObject.setProperty("prop", "value")

        when:
        dynamicObject.setProperty("dyno", "value")

        then:
        def e = thrown(MissingPropertyException)
        e.property == "dyno"
        e.type == BeanWithDynamicProperties
        e.message == "Could not set unknown property 'dyno' for object of type ${BeanWithDynamicProperties.name}."
    }

    def "includes toString() of bean in property set error message when has custom implementation"() {
        def bean = new Bean() {
            @Override
            String toString() {
                return "<bean>"
            }
        }
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.setProperty("unknown", "value")

        then:
        def e = thrown(MissingPropertyException)
        e.message == "Could not set unknown property 'unknown' for ${bean} of type ${bean.getClass().name}."
    }

    def "can invoke method of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.invokeMethod("m", [12] as Object[]) == "[13]"
    }

    def "can check for methods of groovy object"() {
        def bean = new Bean()
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasMethod("m", [12] as Object[])
        !dynamicObject.hasMethod("m", [] as Object[])
        !dynamicObject.hasMethod("other", [12] as Object[])
    }

    def "can invoke method of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.invokeMethod("dyno", [12, "a"] as Object[]) == "[12, a]"
    }

    def "can check for static methods of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        expect:
        dynamicObject.hasMethod("thing", [12] as Object[])
        !dynamicObject.hasMethod("dyno", [12, "a"] as Object[])
    }

    def "can invoke method of closure delegate via closure instance"() {
        def bean = new BeanWithDynamicProperties()
        def cl = {}
        cl.delegate = bean
        def dynamicObject = new BeanDynamicObject(cl)

        expect:
        dynamicObject.invokeMethod("dyno", [12, "a"] as Object[]) == "[12, a]"
    }

    def "fails when invoke unknown method of groovy object"() {
        def bean = new Bean(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.invokeMethod("unknown", [12] as Object[])

        then:
        def e = thrown(MissingMethodException)
        e.method == "unknown"
        e.type == Bean
        e.message == "Could not find method unknown() for arguments [12] on object of type ${Bean.name}."
    }

    def "fails when invoke unknown method of dynamic groovy object"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.invokeMethod("unknown", [12] as Object[])

        then:
        def e = thrown(MissingMethodException)
        e.method == "unknown"
        e.type == BeanWithDynamicProperties
        e.message == "Could not find method unknown() for arguments [12] on object of type ${BeanWithDynamicProperties.name}."
    }

    def "fails when invoke method of dynamic groovy object and no dynamic requested"() {
        def bean = new BeanWithDynamicProperties(prop: "value")
        def dynamicObject = new BeanDynamicObject(bean).withNotImplementsMissing()

        expect:
        dynamicObject.invokeMethod("thing", [12] as Object[]) == "12"

        when:
        dynamicObject.invokeMethod("dyno", [] as Object[])

        then:
        def e = thrown(MissingMethodException)
        e.method == "dyno"
        e.type == BeanWithDynamicProperties
        e.message == "Could not find method dyno() for arguments [] on object of type ${BeanWithDynamicProperties.name}."
    }

    def "includes toString() of bean in missing method error message when has custom implementation"() {
        def bean = new Bean() {
            @Override
            String toString() {
                return "<bean>"
            }
        }
        def dynamicObject = new BeanDynamicObject(bean)

        when:
        dynamicObject.invokeMethod("unknown", [] as Object[])

        then:
        def e = thrown(MissingMethodException)
        e.method == "unknown"
        e.type == bean.getClass()
        e.message == "Could not find method unknown() for arguments [] on <bean> of type ${bean.getClass().name}."
    }

    static class Bean {
        String prop

        String m(int l) {
            return "[${l+1}]"
        }

        String getReadOnly() {
            return "read-only"
        }

        void setWriteOnly(String s) {
            prop = s
        }
    }
}
