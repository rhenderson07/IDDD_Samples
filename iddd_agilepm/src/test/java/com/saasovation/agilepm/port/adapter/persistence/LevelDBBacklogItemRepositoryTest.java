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

package com.saasovation.agilepm.port.adapter.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import com.saasovation.agilepm.domain.model.product.ProductId;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItem;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemId;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemRepository;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemStatus;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemType;
import com.saasovation.agilepm.domain.model.product.backlogitem.StoryPoints;
import com.saasovation.agilepm.domain.model.product.release.Release;
import com.saasovation.agilepm.domain.model.product.release.ReleaseId;
import com.saasovation.agilepm.domain.model.product.sprint.Sprint;
import com.saasovation.agilepm.domain.model.product.sprint.SprintId;
import com.saasovation.agilepm.domain.model.tenant.TenantId;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBUnitOfWork;

public class LevelDBBacklogItemRepositoryTest extends BaseLevelDBRepositoryTest {

	private BacklogItemRepository backlogItemRepository = new LevelDBBacklogItemRepository();

	@Test
	public void testSaveWithTypes() {
		BacklogItem backlogItem1 = new BacklogItem(new TenantId("12345"), new ProductId("67890"),
				new BacklogItemId("bli1"), "My backlog item 1.", "Domain Model", BacklogItemType.FEATURE,
				BacklogItemStatus.PLANNED, StoryPoints.EIGHT);

		LevelDBUnitOfWork.start(super.getDB());
		backlogItemRepository.save(backlogItem1);
		LevelDBUnitOfWork.current().commit();

		BacklogItem savedBacklogItem = backlogItemRepository.backlogItemOfId(backlogItem1.tenantId(),
				backlogItem1.backlogItemId());

		assertNotNull(savedBacklogItem);
		assertEquals(backlogItem1.tenantId(), savedBacklogItem.tenantId());
		assertEquals(backlogItem1.productId(), savedBacklogItem.productId());
		assertEquals(backlogItem1.summary(), savedBacklogItem.summary());
		assertEquals(backlogItem1.category(), savedBacklogItem.category());
		assertEquals(backlogItem1.type(), savedBacklogItem.type());
		assertEquals(backlogItem1.storyPoints(), savedBacklogItem.storyPoints());

		Collection<BacklogItem> savedBacklogItems = backlogItemRepository
				.allProductBacklogItems(backlogItem1.tenantId(), backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(1, savedBacklogItems.size());

		BacklogItem backlogItem2 = new BacklogItem(new TenantId("12345"), new ProductId("67890"),
				new BacklogItemId("bli2"), "My backlog item 1.", "Domain Model", BacklogItemType.FEATURE,
				BacklogItemStatus.DONE, StoryPoints.EIGHT);

		LevelDBUnitOfWork.start(super.getDB());
		backlogItemRepository.save(backlogItem2);
		LevelDBUnitOfWork.current().commit();

		savedBacklogItems = backlogItemRepository.allProductBacklogItems(backlogItem1.tenantId(),
				backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(2, savedBacklogItems.size());

		savedBacklogItems = backlogItemRepository.allOutstandingProductBacklogItems(backlogItem1.tenantId(),
				backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(1, savedBacklogItems.size());
	}

	@Test
	public void testScheduledAndCommittedBacklogItems() {
		BacklogItem backlogItem1 = new BacklogItem(new TenantId("12345"), new ProductId("p00000"),
				new BacklogItemId("bli1"), "My backlog item 1.", "Domain Model", BacklogItemType.FEATURE,
				BacklogItemStatus.PLANNED, StoryPoints.EIGHT);

		BacklogItem backlogItem2 = new BacklogItem(new TenantId("12345"), new ProductId("p00000"),
				new BacklogItemId("bli2"), "My backlog item 1.", "Domain Model", BacklogItemType.FEATURE,
				BacklogItemStatus.PLANNED, StoryPoints.EIGHT);

		BacklogItem backlogItem3 = new BacklogItem(new TenantId("12345"), new ProductId("p00000"),
				new BacklogItemId("bli3"), "My backlog item 1.", "Domain Model", BacklogItemType.FEATURE,
				BacklogItemStatus.PLANNED, StoryPoints.EIGHT);

		Release release = new Release(new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11111"),
				"release1", "My release 1.", new Date(), new Date());

		Sprint sprint = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11111"), "sprint1",
				"My sprint 1.", new Date(), new Date());

		backlogItem2.scheduleFor(release);
		backlogItem2.commitTo(sprint);

		backlogItem3.scheduleFor(release);

		LevelDBUnitOfWork.start(super.getDB());
		backlogItemRepository.save(backlogItem1);
		backlogItemRepository.save(backlogItem2);
		backlogItemRepository.save(backlogItem3);
		LevelDBUnitOfWork.current().commit();

		Collection<BacklogItem> savedBacklogItems = backlogItemRepository
				.allProductBacklogItems(backlogItem1.tenantId(), backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(3, savedBacklogItems.size());

		savedBacklogItems = backlogItemRepository.allOutstandingProductBacklogItems(backlogItem1.tenantId(),
				backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(3, savedBacklogItems.size());

		savedBacklogItems = backlogItemRepository.allBacklogItemsComittedTo(sprint.tenantId(), sprint.sprintId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(1, savedBacklogItems.size());

		savedBacklogItems = backlogItemRepository.allBacklogItemsScheduledFor(release.tenantId(), release.releaseId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(2, savedBacklogItems.size());

		LevelDBUnitOfWork.start(super.getDB());
		backlogItemRepository.remove(backlogItem2);
		LevelDBUnitOfWork.current().commit();

		savedBacklogItems = backlogItemRepository.allOutstandingProductBacklogItems(backlogItem1.tenantId(),
				backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(2, savedBacklogItems.size());

		savedBacklogItems = backlogItemRepository.allBacklogItemsComittedTo(sprint.tenantId(), sprint.sprintId());

		assertTrue(savedBacklogItems.isEmpty());

		savedBacklogItems = backlogItemRepository.allBacklogItemsScheduledFor(release.tenantId(), release.releaseId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(1, savedBacklogItems.size());

		LevelDBUnitOfWork.start(super.getDB());
		backlogItemRepository.remove(backlogItem3);
		LevelDBUnitOfWork.current().commit();

		savedBacklogItems = backlogItemRepository.allBacklogItemsScheduledFor(release.tenantId(), release.releaseId());

		assertTrue(savedBacklogItems.isEmpty());

		savedBacklogItems = backlogItemRepository.allOutstandingProductBacklogItems(backlogItem1.tenantId(),
				backlogItem1.productId());

		assertFalse(savedBacklogItems.isEmpty());
		assertEquals(1, savedBacklogItems.size());
	}
}
