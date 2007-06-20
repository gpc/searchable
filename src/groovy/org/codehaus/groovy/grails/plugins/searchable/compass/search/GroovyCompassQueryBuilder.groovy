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
package org.codehaus.groovy.grails.plugins.searchable.compass.search

import org.compass.core.*
import org.compass.core.util.ClassUtils

import org.codehaus.groovy.runtime.*

import org.apache.commons.logging.LogFactory

/**
 * A Groovy CompassQuery builder, taking nested closures and dynamic method
 * invocation in the Groovy-builder style
 *
 * Note: Instances of this class are NOT thread-safe: you should create one
 * for the duration of your CompassSession then discard it
 *
 * @author Maurice Nicholson
 */
class GroovyCompassQueryBuilder {
    CompassQueryBuilder queryBuilder

    /**
     * Constructor
     */
    GroovyCompassQueryBuilder(CompassQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder
    }

    /**
     * Build a CompassQuery for the given closure and return it
     */
    CompassQuery buildQuery(Closure closure) {
        def invoker = new CompassQueryBuildingClosureDelegate(queryBuilder)
        closure = closure.clone()
        closure.delegate = invoker
        def result
        if (closure.getMaximumNumberOfParameters() == 1) {
            result = closure(queryBuilder)
        } else {
            result = closure()
        }
        def query = invoker.query
        if (!query) return result
        return query
    }

    /**
     * Directly invoke a builder method
     */
    Object invokeMethod(String name, Object args) {
        def invoker = new CompassQueryBuildingClosureDelegate(queryBuilder)
        invoker."${name}"(*args)
        invoker.query
    }
}

/**
 * This class acts as the query-builder closure delegate, identifying
 * which method to call on which object and tying the result together at
 * the end
 *
 * Note: Instances of this class are NOT thread-safe
 */
class CompassQueryBuildingClosureDelegate {
    private static final LOG = LogFactory.getLog(CompassQueryBuildingClosureDelegate.class)
    private static final SHORT_METHOD_NAMES = [should: 'addShould', must: 'addMust', mustNot: 'addMustNot', sort: 'addSort']
    private static final BOOLEAN_ADDER_NAMES = ['addShould', 'addMust', 'addMustNot']

    def queryBuilder
    def stack = new Stack()
    int depth = 0
    def previous // previous method invocation result, if any

    CompassQueryBuildingClosureDelegate(queryBuilder) {
        this.queryBuilder = queryBuilder
    }

    Object invokeMethod(String name, Object args) {
        if (traceEnabled) {
            trace("invokeMethod(${name}, ${args})")
        }
        depth++

        // Remove Closure and options Map
        def invokeArgs = args.toList()
        def closure = remove(invokeArgs, Closure)
        def options = remove(invokeArgs, Map)

        // Transform method name?
        if (SHORT_METHOD_NAMES[name]) {
            name = SHORT_METHOD_NAMES[name]
        }

        // Method to call is *on* CompassQuery but we have builder?
        def queryMethod = hasMethod(CompassQuery, name) && !hasMethod(queryBuilder, name)
        if (queryMethod && !hasMethod(peek(), name) && hasToQuery(peek())) {
            maybeAddPreviousShould() // Lazy boolean?
            def temp = pop()
            if (traceEnabled) {
                trace("converting ${getShortClassName(temp)} to query for ${name}")
            }
            temp = toQuery(temp)
            push(temp)
        } else if (queryMethod && !hasMethod(previous, name) && hasToQuery(previous)) {
            if (traceEnabled) {
                trace("converting ${getShortClassName(previous)} to query for ${name}")
            }
            previous = toQuery(previous)
        }

        // Implicit boolean?
        if (name in BOOLEAN_ADDER_NAMES && !withinBool) {
            trace("Implicit boolean -- ${name} called when the stack is ${stack}, args ${invokeArgs}")
            assert previous, "Expected previous result for implicit boolean!? May be due to closure + implicit boolean combo"
            def bool = queryBuilder.bool()
            push(bool)
        }

        // Method to call takes CompassQuery but we have Builder?
        if (name in BOOLEAN_ADDER_NAMES && !isQuery(invokeArgs[0])) {
            if (!invokeArgs) {
                assert closure, "Attempt to call ${name} without a query or closure argument"
                maybeAddPreviousShould()
                trace("executing nested boolean closure")
                def temp = previous
                previous = null
                invokeArgs = [this.bool(closure)]
                closure = null
                previous = temp
                trace("done nested boolean closure")
            }
            invokeArgs = [toQuery(invokeArgs[0])]
        }

        // Convert any BigDecimals to floats: simple but does the job for now
        invokeArgs = invokeArgs.collect { it instanceof BigDecimal ? it as float : it }

        def result
        if (hasMethod(peek(), name)) {
            if (traceEnabled) {
                trace("invoking ${peek().getClass().name}.${name}(${invokeArgs})")
            }
            result = pop()."${name}"(*invokeArgs)
            if (traceEnabled) {
                trace("result is ${result.getClass().name} ${result}")
            }
            push(result)
        } else if (hasMethod(queryBuilder, name)) {
            // Eager implicit boolean check for previous should clause
            if (previous && !withinBool) {
                trace("implicit boolean spotted")
                def bool = queryBuilder.bool()
                bool.addShould(toQuery(previous))
                push(bool)
                previous = null // not necessary?
            } else {
                // Lazy boolean?
                maybeAddPreviousShould()
            }
            if (traceEnabled) {
                trace("invoking queryBuilder.${name}(${invokeArgs})")
            }
            result = queryBuilder."${name}"(*invokeArgs)
            if (traceEnabled) {
                trace("result is ${result.getClass().name} ${result}")
            }
        } else if (hasMethod(previous, name)) {
            if (traceEnabled) {
                trace("invoking ${getShortClassName(previous)}.${name}(${invokeArgs})")
            }
            result = previous."${name}"(*invokeArgs)
            if (traceEnabled) {
                trace("result is ${result.getClass().name} ${result}")
            }
        } else {
            throw new UnsupportedOperationException(
                "No such method CompassQueryBuilder#${name}" +
                (!peek() ? '' : " or ${getShortClassName(peek())}#${name}") +
                (!previous ? '' : " or ${getShortClassName(previous)}#${name}") +
                ". (Arguments were ${invokeArgs}, stack is ${stack}, result is ${result})" + //, this.result is ${this.result}) " +
                ". Refer to the Compass API docs at http://www.opensymphony.com/compass/versions/1.1/api/org/compass/core/CompassQueryBuilder.html to see what methods are available"
            )
        }

        // Recurse into nested closure?
        if (closure) {
            trace("invoking the closure arg")
            push(result)
            def temp = previous
            previous = null
            closure = closure.clone()
            closure.delegate = this
            depth++
            def x = closure()
            depth--

            // Complete semi-implicit boolean?
            maybeAddPreviousShould()

            previous = temp
            result = pop()
        }

        // Apply builder/query options?
        result = applyOptions(result, options)
        previous = result
        depth--

        if (traceEnabled) {
            trace("after methods and closure, depth ${depth}, stack ${stack}, previous ${previous}")
            trace("returning ${result}")
        }
        return result
    }

    void maybeAddPreviousShould() {
        if (previous && withinBool && previous != peek()) {
            trace("previous lazy boolean should clause spotted")
            peek().addShould(toQuery(previous))
            previous = null // not necessary?
        }
    }

    Object applyOptions(result, options) {
        if (!options) {
            return result
        }

        // Convert any BigDecimals to floats: simple but does the job for now
        options.each { k, v ->
            if (v instanceof BigDecimal) {
                options[k] = v as float 
            }
        }

        if (traceEnabled) {
            trace("applying options ${options} to ${result}")
        }
        def queryOptions = [:]

        def hasToQuery = !isQuery(result) && hasToQuery(result)
        for (option in options) {
            // special case for this single no-parameter, non-setter string query builder method
            if (option.key == 'useAndDefaultOperator') {
                if (option.value) {
                    assert result instanceof CompassQueryBuilder.CompassMultiPropertyQueryStringBuilder || result instanceof  CompassQueryBuilder.CompassQueryStringBuilder, "'useAndDefaultOperator' option provided when current query/builder is a ${getShortClassName(result)}, but should be a Compass*QueryStringBuilder"
                    result.useAndDefaultOperator()
                }
                continue
            }
            def methodName = "set" + option.key[0].toUpperCase() + option.key[1..-1]
            trace "method is -- ${methodName}(${option.value})"
            if (hasMethod(result, methodName)) {
                if (traceEnabled) {
                    trace("invoking ${getShortClassName(result)}#${methodName}(${getShortClassName(option.value)} ${option.value})")
                }
                result = InvokerHelper.invokeMethod(result, methodName, option.value)
            } else if (hasToQuery && hasMethod(CompassQuery, methodName)) {
                trace("added option to queryOptions, ${queryOptions}")
                queryOptions[option.key] = option.value
            } else {
                throw new UnsupportedOperationException(
                    "No such method ${getShortClassName(result)}#${methodName}" + (!hasToQuery ? '' : " or CompassQuery#${methodName}") +
                    ". (Arguments were ${option.value}) " +
                    ". Refer to the Compass API docs at http://www.opensymphony.com/compass/versions/1.1/api/org/compass/core/CompassQueryBuilder.html to see what methods are available"
                )
            }
        }

        if (queryOptions) {
            trace "setting query options"
            result = toQuery(result)
            for (option in queryOptions) {
                def methodName = "set" + option.key[0].toUpperCase() + option.key[1..-1]
                if (traceEnabled) {
                    trace("method is -- ${methodName}(${option.value})")
                }
                assert hasMethod(result, methodName)
                if (traceEnabled) {
                    trace("invoking ${getShortClassName(result)}#${methodName}(${getShortClassName(option.value)} ${option.value})")
                }
                result = InvokerHelper.invokeMethod(result, methodName, option.value)
            }
        }

        result
    }

    CompassQuery getQuery() {
        if (previous) {
            boolean completeBoolean = false
            if (withinBool && peek() != previous) {
                if (traceEnabled) {
                    trace("Within bool ${peek()}, and previous is ${previous}")
                }
                completeBoolean = true
            }
            if (!isQuery(previous)) {
                trace("converting result to query for caller")
                previous = toQuery(previous)
            }
            if (completeBoolean) {
                trace("completing boolean")
                peek().addShould(previous)
                previous = pop().toQuery()
            }
        }
        previous
    }

    boolean isWithinBool() {
        isBoolBuilder(peek())
    }

    void push(object) {
        stack.push(object)
        if (traceEnabled) {
            trace("pushed ${getShortClassName(object)} ${object} to stack")
        }
    }

    Object pop() {
        if (traceEnabled) {
            trace("popping ${peek() == null ? 'null' : getShortClassName(peek())} ${peek()} from the stack")
        }
        stack.empty() ? null : stack.pop()
    }

    Object peek() {
        stack.empty() ? null : stack.peek()
    }

    boolean hasMethod(thing, name) {
        def clazz = thing instanceof Class ? thing : thing?.getClass()
        clazz?.methods?.find { it.name == name }
    }

    boolean hasToQuery(object) {
        hasMethod(object, "toQuery")
    }

    CompassQuery toQuery(object) {
        if (isQuery(object)) return object
        InvokerHelper.invokeMethod(object, "toQuery", null)
    }

    boolean isQuery(object) {
        object instanceof CompassQuery
    }

    boolean isBoolBuilder(object) {
        object instanceof CompassQueryBuilder.CompassBooleanQueryBuilder
    }

    String getShortClassName(thing) {
        def clazz = thing instanceof Class ? thing : thing?.getClass()
        ClassUtils.getShortName(clazz)
    }

    Object remove(args, clazz) {
        def instance
        if (args && clazz.isAssignableFrom(args[args.size() - 1].getClass())) {
            instance = args[args.size() - 1]
            args.remove(instance)
        }
        instance
    }

    boolean isTraceEnabled() {
        LOG.traceEnabled
    }

    void trace(message) {
        def buf = new StringBuffer(message.size() + depth * 2)
        depth.times { buf.append("  ") }
        buf.append(message)
        LOG.trace(buf)
    }
}
