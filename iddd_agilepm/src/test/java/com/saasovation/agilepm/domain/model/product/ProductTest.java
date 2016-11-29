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

package com.saasovation.agilepm.domain.model.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saasovation.agilepm.domain.model.discussion.DiscussionAvailability;
import com.saasovation.agilepm.domain.model.discussion.DiscussionDescriptor;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItem;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemId;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemType;
import com.saasovation.agilepm.domain.model.product.backlogitem.StoryPoints;
import com.saasovation.agilepm.domain.model.product.release.Release;
import com.saasovation.agilepm.domain.model.product.release.ReleaseId;
import com.saasovation.agilepm.domain.model.product.sprint.Sprint;
import com.saasovation.agilepm.domain.model.product.sprint.SprintId;
import com.saasovation.common.domain.model.process.ProcessId;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProductTest {

	private Product newProduct;

	private Product productWithBacklogItems;

	private ProductMockDataUtil mockDataUtil;

	@Before
	public void setUp() {
		mockDataUtil = new ProductMockDataUtil();
		newProduct = mockDataUtil.productForTest();
		productWithBacklogItems = buildProductWithBacklogItems();
	}

	private Product buildProductWithBacklogItems() {
		Product product = mockDataUtil.productForTest();

		BacklogItem backlogItem1 = mockDataUtil.backlogItem1ForTest(newProduct);
		BacklogItem backlogItem2 = mockDataUtil.backlogItem2ForTest(newProduct);
		BacklogItem backlogItem3 = mockDataUtil.backlogItem3ForTest(newProduct);

		product.plannedProductBacklogItem(backlogItem1);
		product.plannedProductBacklogItem(backlogItem2);
		product.plannedProductBacklogItem(backlogItem3);

		return product;
	}

	@Test
	public void constructor_PopulatesFields() {
		assertNotNull(newProduct);
		assertNotNull(newProduct.name());
		assertNotNull(newProduct.description());
		assertNotNull(newProduct.discussion().availability());
		assertNull(newProduct.discussionInitiationId());
	}

	@Test
	public void planBacklogItem_ValidValues_CreatesNonNullBacklogItem() {
		BacklogItemId plannedItemId = new BacklogItemId("B12345");

		BacklogItem backlogItem = newProduct.planBacklogItem(plannedItemId,
				"Support threaded discussions for Scrum products and backlog items.", "Domain Model",
				BacklogItemType.FEATURE, StoryPoints.EIGHT);

		assertNotNull(backlogItem);
	}

	@Test
	public void testPlannedBacklogItem() {
		for (ProductBacklogItem productBacklogItem : productWithBacklogItems.allBacklogItems()) {
			if (productBacklogItem.ordering() == 1) {
				assertTrue(productBacklogItem.backlogItemId().id().endsWith("-1"));
			}
			if (productBacklogItem.ordering() == 2) {
				assertTrue(productBacklogItem.backlogItemId().id().endsWith("-2"));
			}
			if (productBacklogItem.ordering() == 3) {
				assertTrue(productBacklogItem.backlogItemId().id().endsWith("-3"));
			}
		}
	}

	@Test
	public void testReorderFrom() {
		Product product = mockDataUtil.productForTest();

		BacklogItem backlogItem1 = mockDataUtil.backlogItem1ForTest(product);
		BacklogItem backlogItem2 = mockDataUtil.backlogItem2ForTest(product);
		BacklogItem backlogItem3 = mockDataUtil.backlogItem3ForTest(product);

		product.plannedProductBacklogItem(backlogItem1);
		product.plannedProductBacklogItem(backlogItem2);
		product.plannedProductBacklogItem(backlogItem3);

		ProductBacklogItem productBacklogItem1 = null;
		ProductBacklogItem productBacklogItem2 = null;
		ProductBacklogItem productBacklogItem3 = null;

		for (ProductBacklogItem productBacklogItem : product.allBacklogItems()) {
			if (productBacklogItem.ordering() == 1) {
				productBacklogItem1 = productBacklogItem;
			}
			if (productBacklogItem.ordering() == 2) {
				productBacklogItem2 = productBacklogItem;
			}
			if (productBacklogItem.ordering() == 3) {
				productBacklogItem3 = productBacklogItem;
			}
		}

		product.reorderFrom(backlogItem3.backlogItemId(), 1);

		assertEquals(1, productBacklogItem3.ordering());
		assertEquals(2, productBacklogItem1.ordering());
		assertEquals(3, productBacklogItem2.ordering());
	}

	@Test
	public void testRequestAndInitiateDiscussion() {
		Product product = mockDataUtil.productForTest();

		product.requestDiscussion(DiscussionAvailability.REQUESTED);

		assertTrue(product.discussion().descriptor().isUndefined());
		assertEquals(DiscussionAvailability.REQUESTED, product.discussion().availability());

		// eventually...
		ProcessId processId = ProcessId.newProcessId();
		product.startDiscussionInitiation(processId.id());

		// eventually...
		product.initiateDiscussion(new DiscussionDescriptor("CollabDiscussion12345"));

		assertEquals(processId.id(), product.discussionInitiationId());
		assertFalse(product.discussion().descriptor().isUndefined());
		assertEquals(DiscussionAvailability.READY, product.discussion().availability());
	}

	@Test
	public void testRequestAndFailedDiscussion() {
		Product product = mockDataUtil.productForTest();

		product.requestDiscussion(DiscussionAvailability.REQUESTED);

		assertTrue(product.discussion().descriptor().isUndefined());
		assertEquals(DiscussionAvailability.REQUESTED, product.discussion().availability());

		// eventually...
		ProcessId processId = ProcessId.newProcessId();
		product.startDiscussionInitiation(processId.id());

		// eventually...
		product.failDiscussionInitiation();

		assertNull(product.discussionInitiationId());
		assertTrue(product.discussion().descriptor().isUndefined());
		assertEquals(DiscussionAvailability.FAILED, product.discussion().availability());
	}

	@Test
	public void testScheduleRelease() {
		Product product = mockDataUtil.productForTest();

		Date begins = new Date();
		Date ends = new Date(begins.getTime() + (86400000L * 30L));

		Release release = product.scheduleRelease(new ReleaseId("R-12345"), "Release 1.3",
				"Enterprise interactive release.", begins, ends);

		assertNotNull(release);
		assertEquals("Release 1.3", release.name());
		assertEquals("Enterprise interactive release.", release.description());
	}

	@Test
	public void testScheduleSprint() {
		Product product = mockDataUtil.productForTest();

		Date begins = new Date();
		Date ends = new Date(begins.getTime() + (86400000L * 30L));

		Sprint sprint = product.scheduleSprint(new SprintId("S12345"), "Collaboration Integration Sprint",
				"Make Scrum project collaboration possible.", begins, ends);

		assertNotNull(sprint);
		assertEquals("Collaboration Integration Sprint", sprint.name());
		assertEquals("Make Scrum project collaboration possible.", sprint.goals());
	}
}
