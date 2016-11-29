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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.saasovation.agilepm.domain.model.product.ProductId;
import com.saasovation.agilepm.domain.model.product.sprint.Sprint;
import com.saasovation.agilepm.domain.model.product.sprint.SprintId;
import com.saasovation.agilepm.domain.model.product.sprint.SprintRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBUnitOfWork;

public class LevelDBSprintRepositoryTest extends BaseLevelDBRepositoryTest {

	private SprintRepository sprintRepository = new LevelDBSprintRepository();

	@Test
	public void testSave() {
		Sprint sprint = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11111"), "sprint1",
				"My sprint 1.", new Date(), new Date());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.save(sprint);
		LevelDBUnitOfWork.current().commit();

		Sprint savedSprint = sprintRepository.sprintOfId(sprint.tenantId(), sprint.sprintId());

		assertNotNull(savedSprint);
		assertEquals(sprint.tenantId(), savedSprint.tenantId());
		assertEquals(sprint.name(), savedSprint.name());

		Collection<Sprint> savedSprints = this.sprintRepository.allProductSprints(sprint.tenantId(),
				sprint.productId());

		assertFalse(savedSprints.isEmpty());
		assertEquals(1, savedSprints.size());
	}

	@Test
	public void testRemove() {
		Sprint sprint1 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11111"), "sprint1",
				"My sprint 1.", new Date(), new Date());

		Sprint sprint2 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11112"), "sprint2",
				"My sprint 2.", new Date(), new Date());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.save(sprint1);
		sprintRepository.save(sprint2);
		LevelDBUnitOfWork.current().commit();

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.remove(sprint1);
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = sprint2.tenantId();
		ProductId productId = sprint2.productId();

		Collection<Sprint> savedSprints = sprintRepository.allProductSprints(tenantId, productId);
		assertFalse(savedSprints.isEmpty());
		assertEquals(1, savedSprints.size());
		assertEquals(sprint2.name(), savedSprints.iterator().next().name());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.remove(sprint2);
		LevelDBUnitOfWork.current().commit();

		savedSprints = sprintRepository.allProductSprints(tenantId, productId);
		assertTrue(savedSprints.isEmpty());
	}

	@Test
	public void testSaveAllRemoveAll() {
		Sprint sprint1 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11111"), "sprint1",
				"My sprint 1.", new Date(), new Date());

		Sprint sprint2 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11112"), "sprint2",
				"My sprint 2.", new Date(), new Date());

		Sprint sprint3 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11113"), "sprint3",
				"My sprint 3.", new Date(), new Date());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.saveAll(Arrays.asList(new Sprint[] { sprint1, sprint2, sprint3 }));
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = sprint1.tenantId();
		ProductId productId = sprint1.productId();

		Collection<Sprint> savedSprints = sprintRepository.allProductSprints(tenantId, productId);
		assertFalse(savedSprints.isEmpty());
		assertEquals(3, savedSprints.size());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.removeAll(Arrays.asList(new Sprint[] { sprint1, sprint3 }));
		LevelDBUnitOfWork.current().commit();

		savedSprints = sprintRepository.allProductSprints(tenantId, productId);
		assertFalse(savedSprints.isEmpty());
		assertEquals(1, savedSprints.size());
		assertEquals(sprint2.name(), savedSprints.iterator().next().name());

		LevelDBUnitOfWork.start(super.getDB());
		sprintRepository.removeAll(Arrays.asList(new Sprint[] { sprint2 }));
		LevelDBUnitOfWork.current().commit();

		savedSprints = sprintRepository.allProductSprints(tenantId, productId);
		assertTrue(savedSprints.isEmpty());
	}

	@Test
	public void testConcurrentTransactions() {
		final List<Integer> orderOfCommits = new ArrayList<Integer>();

		Sprint sprint1 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11111"), "sprint1",
				"My sprint 1.", new Date(), new Date());

		LevelDBUnitOfWork.start(getDB());
		sprintRepository.save(sprint1);

		new Thread() {
			@Override
			public void run() {
				Sprint sprint2 = new Sprint(new TenantId("12345"), new ProductId("p00000"), new SprintId("s11112"),
						"sprint2", "My sprint 2.", new Date(), new Date());

				LevelDBUnitOfWork.start(getDB());
				sprintRepository.save(sprint2);
				LevelDBUnitOfWork.current().commit();
				orderOfCommits.add(2);
			}
		}.start();

		try {
			Thread.sleep(250L);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		LevelDBUnitOfWork.current().commit();
		orderOfCommits.add(1);

		for (int idx = 0; idx < orderOfCommits.size(); ++idx) {
			assertEquals(idx + 1, orderOfCommits.get(idx).intValue());
		}

		try {
			Thread.sleep(250L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Collection<Sprint> savedSprints = sprintRepository.allProductSprints(sprint1.tenantId(), sprint1.productId());

		assertFalse(savedSprints.isEmpty());
		assertEquals(2, savedSprints.size());
	}

}
