/*
 * Copyright 2011 the original author or authors.
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
package grails.plugin.searchable.internal.compass.config

import org.springframework.beans.factory.*
import org.hibernate.SessionFactory

/**
 * Handles Grails 1.4's SessionFactoryProxy class returning the underlying proxied session factory if needed
 *
 * @author Graeme Rocher
 */
class SessionFactoryLookup implements InitializingBean, FactoryBean{

	SessionFactory sessionFactory
	SessionFactory resolvedSessionFactory
	
	def getObject() { resolvedSessionFactory }
	boolean isSingleton() { true }
	Class getObjectType() { SessionFactory }
	
	void afterPropertiesSet() {
		if("SessionFactoryProxy".equals(sessionFactory.getClass().simpleName)) {
			resolvedSessionFactory = sessionFactory.getCurrentSessionFactory()
		}
		else {
			resolvedSessionFactory = sessionFactory
		}
	}

}