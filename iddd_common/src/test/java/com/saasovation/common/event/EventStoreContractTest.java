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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.saasovation.common.CommonTestCase;
import com.saasovation.common.persistence.PersistenceManagerProvider;

public class EventStoreContractTest extends CommonTestCase {

	private EventStore mockEventStore;

	@Before
	public void setUp() {
		mockEventStore = new MockEventStore(new PersistenceManagerProvider() {
		});
	}

	@Test
	public void testAllStoredEventsBetween() {

		long totalEvents = mockEventStore.countStoredEvents();

		assertEquals(totalEvents, mockEventStore.allStoredEventsBetween(1, totalEvents).size());

		assertEquals(10, mockEventStore.allStoredEventsBetween(totalEvents - 9, totalEvents).size());
	}

	@Test
	public void testAllStoredEventsSince() {

		long totalEvents = mockEventStore.countStoredEvents();

		assertEquals(totalEvents, mockEventStore.allStoredEventsSince(0).size());

		assertEquals(0, mockEventStore.allStoredEventsSince(totalEvents).size());

		assertEquals(10, mockEventStore.allStoredEventsSince(totalEvents - 10).size());
	}

	@Test
	public void testAppend() {

		long numberOfEvents = mockEventStore.countStoredEvents();

		TestableDomainEvent domainEvent = new TestableDomainEvent(10001, "testDomainEvent");

		StoredEvent storedEvent = mockEventStore.append(domainEvent);

		assertTrue(mockEventStore.countStoredEvents() > numberOfEvents);
		assertEquals(mockEventStore.countStoredEvents(), numberOfEvents + 1);

		assertNotNull(storedEvent);

		TestableDomainEvent reconstitutedDomainEvent = storedEvent.toDomainEvent();

		assertNotNull(reconstitutedDomainEvent);
		assertEquals(domainEvent.id(), reconstitutedDomainEvent.id());
		assertEquals(domainEvent.name(), reconstitutedDomainEvent.name());
		assertEquals(domainEvent.occurredOn(), reconstitutedDomainEvent.occurredOn());
	}

	@Test
	public void testCountStoredEvents() {

		long numberOfEvents = mockEventStore.countStoredEvents();

		TestableDomainEvent lastDomainEvent = null;

		for (int idx = 0; idx < 10; ++idx) {
			TestableDomainEvent domainEvent = new TestableDomainEvent(10001 + idx, "testDomainEvent" + idx);

			lastDomainEvent = domainEvent;

			mockEventStore.append(domainEvent);
		}

		assertEquals(numberOfEvents + 10, mockEventStore.countStoredEvents());

		numberOfEvents = mockEventStore.countStoredEvents();

		assertEquals(1, mockEventStore.allStoredEventsBetween(numberOfEvents, numberOfEvents + 1000).size());

		StoredEvent storedEvent = mockEventStore.allStoredEventsBetween(numberOfEvents, numberOfEvents).get(0);

		assertNotNull(storedEvent);

		TestableDomainEvent reconstitutedDomainEvent = storedEvent.toDomainEvent();

		assertNotNull(reconstitutedDomainEvent);
		assertEquals(lastDomainEvent.id(), reconstitutedDomainEvent.id());
		assertEquals(lastDomainEvent.name(), reconstitutedDomainEvent.name());
		assertEquals(lastDomainEvent.occurredOn(), reconstitutedDomainEvent.occurredOn());
	}

	@Test
	public void testStoredEvent() {

		TestableDomainEvent domainEvent = new TestableDomainEvent(10001, "testDomainEvent");

		StoredEvent storedEvent = mockEventStore.append(domainEvent);

		assertNotNull(storedEvent);

		TestableDomainEvent reconstitutedDomainEvent = storedEvent.toDomainEvent();

		assertNotNull(reconstitutedDomainEvent);
		assertEquals(domainEvent.id(), reconstitutedDomainEvent.id());
		assertEquals(domainEvent.name(), reconstitutedDomainEvent.name());
		assertEquals(domainEvent.occurredOn(), reconstitutedDomainEvent.occurredOn());
	}
}
