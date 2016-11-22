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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class EventSerializerTest {

	@Test
	public void testDefaultFormat() {
		EventSerializer serializer = EventSerializer.instance();

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		assertTrue(serializedEvent.contains("\"id\""));
		assertTrue(serializedEvent.contains("\"occurredOn\""));
		assertFalse(serializedEvent.contains("\n"));
		assertTrue(serializedEvent.contains("null"));
	}

	@Test
	public void testCompact() {
		EventSerializer serializer = new EventSerializer(true);

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		assertTrue(serializedEvent.contains("\"id\""));
		assertTrue(serializedEvent.contains("\"occurredOn\""));
		assertFalse(serializedEvent.contains("\n"));
		assertFalse(serializedEvent.contains("null"));
	}

	@Test
	public void testPrettyAndCompact() {
		EventSerializer serializer = new EventSerializer(true, true);

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		assertTrue(serializedEvent.contains("\"id\""));
		assertTrue(serializedEvent.contains("\"occurredOn\""));
		assertTrue(serializedEvent.contains("\n"));
		assertFalse(serializedEvent.contains("null"));
	}

	@Test
	public void testDeserializeDefault() {
		EventSerializer serializer = EventSerializer.instance();

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		TestableDomainEvent event = serializer.deserialize(serializedEvent, TestableDomainEvent.class);

		assertTrue(serializedEvent.contains("null"));
		assertEquals(1, event.id());
		assertEquals(null, event.name());
		assertNotNull(event.occurredOn());
	}

	@Test
	public void testDeserializeCompactNotNull() {
		EventSerializer serializer = new EventSerializer(true);

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, "test"));

		TestableDomainEvent event = serializer.deserialize(serializedEvent, TestableDomainEvent.class);

		assertFalse(serializedEvent.contains("null"));
		assertTrue(serializedEvent.contains("\"test\""));
		assertEquals(1, event.id());
		assertEquals("test", event.name());
		assertNotNull(event.occurredOn());
	}

	@Test
	public void testDeserializeCompactNull() {
		EventSerializer serializer = new EventSerializer(true);

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		TestableDomainEvent event = serializer.deserialize(serializedEvent, TestableDomainEvent.class);

		assertFalse(serializedEvent.contains("null"));
		assertEquals(1, event.id());
		assertEquals(null, event.name());
		assertNotNull(event.occurredOn());
	}

	@Test
	public void testDeserializePrettyAndCompactNull() {
		EventSerializer serializer = new EventSerializer(true, true);

		String serializedEvent = serializer.serialize(new TestableDomainEvent(1, null));

		TestableDomainEvent event = serializer.deserialize(serializedEvent, TestableDomainEvent.class);

		assertFalse(serializedEvent.contains("null"));
		assertTrue(serializedEvent.contains("\n"));
		assertEquals(1, event.id());
		assertEquals(null, event.name());
		assertNotNull(event.occurredOn());
	}
}
