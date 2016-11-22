//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.common;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.spring.SpringHibernateSessionProvider;

//

import org.junit.After;
import org.junit.Before;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
//

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = CommonTestConfig.class)
public abstract class CommonTestCase {
	@Rule
	public TestName name = new TestName();

	protected ApplicationContext applicationContext;
	protected SpringHibernateSessionProvider sessionProvider;
	private Transaction transaction;

	public CommonTestCase() {
		super();
	}

	protected Session session() {
		Session session = this.sessionProvider.session();

		return session;
	}

	protected Transaction transaction() {
		return this.transaction;
	}

	@Before
	public void setUp() {

		DomainEventPublisher.instance().reset();

//		this.applicationContext = new ClassPathXmlApplicationContext("applicationContext-common.xml");
//
//		this.sessionProvider = (SpringHibernateSessionProvider) this.applicationContext.getBean("sessionProvider");
//
//		this.setTransaction(this.session().beginTransaction());

		System.out.println(">>>>>>>>>>>>>>>>>>>> (start)" + name.getMethodName());
	}

	@After
	public void tearDown() {

//		this.transaction().rollback();
//
//		this.setTransaction(null);
//
//		this.session().clear();

		System.out.println("<<<<<<<<<<<<<<<<<<<< (end)");
	}

	private void setTransaction(Transaction aTransaction) {
		this.transaction = aTransaction;
	}
}
