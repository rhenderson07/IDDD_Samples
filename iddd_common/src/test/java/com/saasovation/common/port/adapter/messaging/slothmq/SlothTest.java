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

package com.saasovation.common.port.adapter.messaging.slothmq;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saasovation.common.domain.model.DomainEventPublisher;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlothTest{

	private ExchangePublisher publisher;
	private TestExchangeListener testExchangeListener;

	@Test
	public void testPublishSubscribe() {
		this.publisher.publish("my.test.type", "A tiny little message.");
		this.publisher.publish("my.test.type1", "A slightly bigger message.");
		this.publisher.publish("my.test.type2", "An even bigger message, still.");
		this.publisher.publish("my.test.type3", "An even bigger (bigger!) message, still.");

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
		}

		assertEquals("my.test.type", testExchangeListener.receivedType());
		assertEquals("A tiny little message.", testExchangeListener.receivedMessage());
		assertEquals(4, TestExchangeListenerAgain.uniqueMessages().size());
	}

	@Before
	public void setUp() {
        DomainEventPublisher.instance().reset();

		SlothServer.executeInProcessDetachedServer();

		this.testExchangeListener = new TestExchangeListener();

		SlothClient.instance().register(this.testExchangeListener);

		SlothClient.instance().register(new TestExchangeListenerAgain());
		SlothClient.instance().register(new TestExchangeListenerAgain());
		SlothClient.instance().register(new TestExchangeListenerAgain());

		this.publisher = new ExchangePublisher("TestExchange");
	}

	@After
	public void tearDown() {
		this.testExchangeListener.close();

		SlothClient.instance().closeAll();
	}

	private static class TestExchangeListener extends ExchangeListener {

		private String receivedMessage;
		private String receivedType;

		TestExchangeListener() {
			super();
		}

		public String receivedMessage() {
			return this.receivedMessage;
		}

		public String receivedType() {
			return this.receivedType;
		}

		@Override
		protected String exchangeName() {
			return "TestExchange";
		}

		@Override
		protected void filteredDispatch(String aType, String aTextMessage) {
			this.receivedType = aType;
			this.receivedMessage = aTextMessage;
		}

		@Override
		protected String[] listensTo() {
			return new String[] { "my.test.type" };
		}

		@Override
		protected String name() {
			return this.getClass().getName();
		}
	}

	private static class TestExchangeListenerAgain extends ExchangeListener {

		private static int idCount = 0;
		private static Set<String> uniqueMessages = new HashSet<String>();

		private int id;

		public static Set<String> uniqueMessages() {
			return uniqueMessages;
		}

		TestExchangeListenerAgain() {
			super();

			this.id = ++idCount;
		}

		@Override
		protected String exchangeName() {
			return "TestExchange";
		}

		@Override
		protected void filteredDispatch(String aType, String aTextMessage) {
			synchronized (uniqueMessages) {
				uniqueMessages.add(aType + ":" + aTextMessage);
			}
		}

		@Override
		protected String[] listensTo() {
			return null;	// all
		}

		@Override
		protected String name() {
			return this.getClass().getName() + "#" + this.id;
		}
	}
}
