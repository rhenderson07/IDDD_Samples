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

package com.saasovation.common.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.saasovation.common.CommonTestCase;
import com.saasovation.common.event.EventStore;
import com.saasovation.common.event.MockEventStore;
import com.saasovation.common.persistence.PersistenceManagerProvider;

public class NotificationLogTest extends CommonTestCase {

	private EventStore mockEventStore;

	@Before
	public void setUp() {
		mockEventStore = new MockEventStore(new PersistenceManagerProvider() {
		});
	}

	@Test
	public void testCurrentNotificationLogFromFactory() {

		NotificationLogFactory factory = new NotificationLogFactory(mockEventStore);
		NotificationLog log = factory.createCurrentNotificationLog();

		assertTrue(NotificationLogFactory.notificationsPerLog() >= log.totalNotifications());
		assertTrue(mockEventStore.countStoredEvents() >= log.totalNotifications());
		assertFalse(log.hasNextNotificationLog());
		assertTrue(log.hasPreviousNotificationLog());
		assertFalse(log.isArchived());
	}

	@Test
	public void testFirstNotificationLogFromFactory() {

		NotificationLogId id = NotificationLogId.first(NotificationLogFactory.notificationsPerLog());
		NotificationLogFactory factory = new NotificationLogFactory(mockEventStore);
		NotificationLog log = factory.createNotificationLog(id);

		assertEquals(NotificationLogFactory.notificationsPerLog(), log.totalNotifications());
		assertTrue(mockEventStore.countStoredEvents() >= log.totalNotifications());
		assertTrue(log.hasNextNotificationLog());
		assertFalse(log.hasPreviousNotificationLog());
		assertTrue(log.isArchived());
	}

	@Test
	public void testPreviousOfCurrentNotificationLogFromFactory() {

		long totalEvents = mockEventStore.countStoredEvents();
		boolean shouldBePrevious = totalEvents > (NotificationLogFactory.notificationsPerLog() * 2);
		NotificationLogFactory factory = new NotificationLogFactory(mockEventStore);
		NotificationLog log = factory.createCurrentNotificationLog();

		NotificationLogId previousId = log.decodedPreviousNotificationLogId();
		log = factory.createNotificationLog(previousId);

		assertEquals(NotificationLogFactory.notificationsPerLog(), log.totalNotifications());
		assertTrue(totalEvents >= log.totalNotifications());
		assertTrue(log.hasNextNotificationLog());
		assertEquals(shouldBePrevious, log.hasPreviousNotificationLog());
		assertTrue(log.isArchived());
	}

	@Test
	public void testEncodedWithDecodedNavigationIds() {

		NotificationLogFactory factory = new NotificationLogFactory(mockEventStore);
		NotificationLog log = factory.createCurrentNotificationLog();

		String currentId = log.notificationLogId();
		NotificationLogId decodedCurrentLogId = log.decodedNotificationLogId();
		assertEquals(log.decodedNotificationLogId(), new NotificationLogId(currentId));

		String previousId = log.previousNotificationLogId();
		NotificationLogId decodedPreviousLogId = log.decodedPreviousNotificationLogId();
		assertEquals(decodedPreviousLogId, new NotificationLogId(previousId));
		log = factory.createNotificationLog(log.decodedPreviousNotificationLogId());

		String nextId = log.nextNotificationLogId();
		NotificationLogId decodedNextLogId = log.decodedNextNotificationLogId();
		assertEquals(decodedNextLogId, new NotificationLogId(nextId));
		assertEquals(decodedCurrentLogId, decodedNextLogId);
	}
}
