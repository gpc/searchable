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
package org.codehaus.groovy.grails.plugins.searchable.compass.domain

import org.apache.commons.logging.LogFactory
import org.compass.core.Compass
import org.codehaus.groovy.grails.plugins.searchable.compass.*

/**
 * @author Maurice Nicholson
 */
class DynamicDomainMethodUtils {
    static LOG = LogFactory.getLog("org.codehaus.groovy.grails.plugins.searchable.compass.domain.DynamicDomainMethodUtils")

    static attachDynamicMethods(searchableMethodFactory, domainClasses, Compass compass) {
        for (grailsDomainClass in domainClasses) {
            if (!SearchableCompassUtils.isRootMappedClass(grailsDomainClass, compass)) {
                continue
            }
            LOG.debug("Adding searchable methods to [${grailsDomainClass.clazz.name}]")

            // ------------------------------------------------------------
            // class methods

            /**
             * search: Returns a subset of the instances of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.search << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "search").invoke(*args)
            }

            /**
             * moreLikeThis: Returns more hits of this class like this given instance
             */
            grailsDomainClass.metaClass.'static'.moreLikeThis << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "moreLikeThis").invoke(*args)
            }

            /**
             * searchTop: Returns the top (most relevant) instance of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.searchTop << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "searchTop").invoke(*args)
            }

            /**
             * searchEvery: Returns all instance of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.searchEvery << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "searchEvery").invoke(*args)
            }

            /**
             * Returns the number of hits for the given query matching instances of this class
             */
            grailsDomainClass.metaClass.'static'.countHits << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "countHits").invoke(*args)
            }

            /**
             * Get term frequencies for the given args
             */
            grailsDomainClass.metaClass.'static'.termFreqs << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "termFreqs").invoke(*args)
            }

            /**
             * Suggest an alternative query (correcting possible spelling errors)
             */
            grailsDomainClass.metaClass.'static'.suggestQuery << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "suggestQuery").invoke(*args)
            }
            
            /**
             * index: Adds class instances to the search index
             */
            grailsDomainClass.metaClass.'static'.index << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "index").invoke(*args)
            }

            /**
             * indexAll: Adds class instances to the search index
             */
            grailsDomainClass.metaClass.'static'.indexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "indexAll").invoke(*args)
            }

            /**
             * unindex: Removes class instances from the search index
             */
            grailsDomainClass.metaClass.'static'.unindex << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "unindex").invoke(*args)
            }

            /**
             * unindexAll: Removes class instances from the search index
             */
            grailsDomainClass.metaClass.'static'.unindexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "unindexAll").invoke(*args)
            }

            /**
             * reindexAll: Updates the search index
             */
            grailsDomainClass.metaClass.'static'.reindexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "reindexAll").invoke(*args)
            }

            /**
             * reindex: Updates the search index
             */
            grailsDomainClass.metaClass.'static'.reindex << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "reindex").invoke(*args)
            }

            // ------------------------------------------------------------
            // instance methods

            /**
             * moreLikeThis: Returns more objects like this instance
             */
            grailsDomainClass.metaClass.moreLikeThis << { Object[] args ->
                searchableMethodFactory.getMethod(delegate.class, "moreLikeThis").invoke(([delegate] + args[0].toList()) as Object[])
            }

            /**
             * Adds the instance to the search index
             */
            grailsDomainClass.metaClass.index << { Object[] args ->
                searchableMethodFactory.getMethod("index").invoke(delegate)
            }

            /**
             * unindex instance method: removes the instance from the search index
             */
            grailsDomainClass.metaClass.unindex << { Object[] args ->
                searchableMethodFactory.getMethod("unindex").invoke(delegate)
            }

            /**
             * reindex instance method: updates the search index to reflect the current instance data
             */
            grailsDomainClass.metaClass.reindex << { Object[] args ->
                searchableMethodFactory.getMethod("reindex").invoke(delegate)
            }
        }
    }
}