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

package com.saasovation.common.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.domain.model.DomainEventSubscriber;

@RunWith(SpringJUnit4ClassRunner.class)
public class DomainEventPublisherTest {

	private boolean anotherEventHandled;
	private boolean eventHandled;

	@Test
	public void testDomainEventPublisherPublish() {
		DomainEventPublisher.instance().reset();

		DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<TestableDomainEvent>() {
			@Override
			public void handleEvent(TestableDomainEvent aDomainEvent) {
				assertEquals(100L, aDomainEvent.id());
				assertEquals("test", aDomainEvent.name());
				eventHandled = true;
			}

			@Override
			public Class<TestableDomainEvent> subscribedToEventType() {
				return TestableDomainEvent.class;
			}
		});

		assertFalse(this.eventHandled);

		DomainEventPublisher.instance().publish(new TestableDomainEvent(100L, "test"));

		assertTrue(this.eventHandled);
	}

	@Test
	public void testDomainEventPublisherBlocked() throws Exception {
		DomainEventPublisher.instance().reset();

		DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<TestableDomainEvent>() {
			@Override
			public void handleEvent(TestableDomainEvent aDomainEvent) {
				assertEquals(100L, aDomainEvent.id());
				assertEquals("test", aDomainEvent.name());
				eventHandled = true;
				// attempt nested publish, which should not work
				DomainEventPublisher.instance().publish(new AnotherTestableDomainEvent(1000.0));
			}

			@Override
			public Class<TestableDomainEvent> subscribedToEventType() {
				return TestableDomainEvent.class;
			}
		});

		DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<AnotherTestableDomainEvent>() {
			@Override
			public void handleEvent(AnotherTestableDomainEvent aDomainEvent) {
				double ERROR_DELTA = 0.0001;
				// should never be reached due to blocked publisher
				assertEquals(1000.0, aDomainEvent.value(), ERROR_DELTA);
				anotherEventHandled = true;
			}

			@Override
			public Class<AnotherTestableDomainEvent> subscribedToEventType() {
				return AnotherTestableDomainEvent.class;
			}
		});

		assertFalse(this.eventHandled);
		assertFalse(this.anotherEventHandled);

		DomainEventPublisher.instance().publish(new TestableDomainEvent(100L, "test"));

		assertTrue(this.eventHandled);
		assertFalse(this.anotherEventHandled);
	}
}
