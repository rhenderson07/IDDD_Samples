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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saasovation.common.domain.model.DomainEvent;
import com.saasovation.common.event.TestableDomainEvent;
import com.saasovation.common.event.TestableNavigableDomainEvent;

@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationReaderTest {

	@Test
	public void testReadBasicProperties() {
		DomainEvent domainEvent = new TestableDomainEvent(100, "testing");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		assertEquals(1L, reader.notificationId());
		assertEquals("1", reader.notificationIdAsString());
		assertEquals(domainEvent.occurredOn(), reader.occurredOn());
		assertEquals(notification.typeName(), reader.typeName());
		assertEquals(notification.version(), reader.version());
		assertEquals(domainEvent.eventVersion(), reader.version());
	}

	@Test
	public void testReadDomainEventProperties() {
		TestableDomainEvent domainEvent = new TestableDomainEvent(100, "testing");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		assertEquals("" + domainEvent.eventVersion(), reader.eventStringValue("eventVersion"));
		assertEquals("" + domainEvent.eventVersion(), reader.eventStringValue("/eventVersion"));
		assertEquals("" + domainEvent.id(), reader.eventStringValue("id"));
		assertEquals("" + domainEvent.id(), reader.eventStringValue("/id"));
		assertEquals("" + domainEvent.name(), reader.eventStringValue("name"));
		assertEquals("" + domainEvent.name(), reader.eventStringValue("/name"));
		assertEquals("" + domainEvent.occurredOn().getTime(), reader.eventStringValue("occurredOn"));
		assertEquals("" + domainEvent.occurredOn().getTime(), reader.eventStringValue("/occurredOn"));
	}

	@Test
	public void testReadNestedDomainEventProperties() {
		TestableNavigableDomainEvent domainEvent = new TestableNavigableDomainEvent(100, "testing");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		assertEquals("" + domainEvent.eventVersion(), reader.eventStringValue("eventVersion"));
		assertEquals("" + domainEvent.eventVersion(), reader.eventStringValue("/eventVersion"));
		assertEquals(domainEvent.eventVersion(), reader.eventIntegerValue("eventVersion").intValue());
		assertEquals(domainEvent.eventVersion(), reader.eventIntegerValue("/eventVersion").intValue());
		assertEquals("" + domainEvent.nestedEvent().eventVersion(),
				reader.eventStringValue("nestedEvent", "eventVersion"));
		assertEquals("" + domainEvent.nestedEvent().eventVersion(),
				reader.eventStringValue("/nestedEvent/eventVersion"));
		assertEquals(domainEvent.nestedEvent().eventVersion(),
				reader.eventIntegerValue("nestedEvent", "eventVersion").intValue());
		assertEquals(domainEvent.nestedEvent().eventVersion(),
				reader.eventIntegerValue("/nestedEvent/eventVersion").intValue());
		assertEquals("" + domainEvent.nestedEvent().id(), reader.eventStringValue("nestedEvent", "id"));
		assertEquals("" + domainEvent.nestedEvent().id(), reader.eventStringValue("/nestedEvent/id"));
		assertEquals(domainEvent.nestedEvent().id(), reader.eventLongValue("nestedEvent", "id").longValue());
		assertEquals(domainEvent.nestedEvent().id(), reader.eventLongValue("/nestedEvent/id").longValue());
		assertEquals("" + domainEvent.nestedEvent().name(), reader.eventStringValue("nestedEvent", "name"));
		assertEquals("" + domainEvent.nestedEvent().name(), reader.eventStringValue("/nestedEvent/name"));
		assertEquals("" + domainEvent.nestedEvent().occurredOn().getTime(),
				reader.eventStringValue("nestedEvent", "occurredOn"));
		assertEquals("" + domainEvent.nestedEvent().occurredOn().getTime(),
				reader.eventStringValue("/nestedEvent/occurredOn"));
		assertEquals(domainEvent.nestedEvent().occurredOn(), reader.eventDateValue("nestedEvent", "occurredOn"));
		assertEquals(domainEvent.nestedEvent().occurredOn(), reader.eventDateValue("/nestedEvent/occurredOn"));
		assertEquals("" + domainEvent.occurredOn().getTime(), reader.eventStringValue("occurredOn"));
		assertEquals("" + domainEvent.occurredOn().getTime(), reader.eventStringValue("/occurredOn"));
		assertEquals(domainEvent.occurredOn(), reader.eventDateValue("occurredOn"));
		assertEquals(domainEvent.occurredOn(), reader.eventDateValue("/occurredOn"));
	}

	@Test
	public void testDotNotation() {
		TestableNavigableDomainEvent domainEvent = new TestableNavigableDomainEvent(100, "testing");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		assertEquals("" + domainEvent.nestedEvent().eventVersion(),
				reader.eventStringValue("nestedEvent.eventVersion"));
		assertEquals(domainEvent.nestedEvent().eventVersion(),
				reader.eventIntegerValue("nestedEvent.eventVersion").intValue());
	}

	@Test
	public void testReadBogusProperties() {
		TestableNavigableDomainEvent domainEvent = new TestableNavigableDomainEvent(100L, "testing");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		boolean mustThrow = false;

		try {
			reader.eventStringValue("eventVersion.version");
		} catch (Exception e) {
			mustThrow = true;
		}

		assertTrue(mustThrow);
	}

	@Test
	public void testReadNullProperties() {
		TestableNullPropertyDomainEvent domainEvent = new TestableNullPropertyDomainEvent(100L, "testingNulls");

		Notification notification = new Notification(1, domainEvent);

		NotificationSerializer serializer = NotificationSerializer.instance();

		String serializedNotification = serializer.serialize(notification);

		NotificationReader reader = new NotificationReader(serializedNotification);

		assertNull(reader.eventStringValue("textMustBeNull"));

		assertNull(reader.eventStringValue("textMustBeNull2"));

		assertNull(reader.eventIntegerValue("numberMustBeNull"));

		assertNull(reader.eventStringValue("nested.nestedTextMustBeNull"));

		assertNull(reader.eventStringValue("nullNested.nestedTextMustBeNull"));

		assertNull(reader.eventStringValue("nested.nestedDeeply.nestedDeeplyTextMustBeNull"));

		assertNull(reader.eventStringValue("nested.nestedDeeply.nestedDeeplyTextMustBeNull2"));

		assertNull(reader.eventStringValue("nested.nullNestedDeeply.nestedDeeplyTextMustBeNull"));

		assertNull(reader.eventStringValue("nested.nullNestedDeeply.nestedDeeplyTextMustBeNull2"));
	}
}
