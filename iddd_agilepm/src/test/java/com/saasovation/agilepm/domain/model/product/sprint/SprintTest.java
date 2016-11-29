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

package com.saasovation.agilepm.domain.model.product.sprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saasovation.agilepm.domain.model.product.Product;
import com.saasovation.agilepm.domain.model.product.ProductMockDataUtil;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItem;

@RunWith(SpringJUnit4ClassRunner.class)
public class SprintTest {

	private Product product;
	private Sprint sprint;

	// private Product productWithBacklogItems;

	private ProductMockDataUtil mockDataUtil;

	@Before
	public void setUp() {
		mockDataUtil = new ProductMockDataUtil();
		product = mockDataUtil.productForTest();
		sprint = mockDataUtil.sprintForTest(product);
	}

	@Test
	public void scheduleSprint_CreatesSprintWithPopulatedFields() {
		SprintId sprintId = new SprintId("S-12345");
		String sprintName = "Collaboration Integration Sprint";
		String sprintGoal = "Make Scrum project collaboration possible.";
		Date begins = new Date();
		Date ends = new Date(begins.getTime() + (86400000L * 30L));

		Sprint sprint = product.scheduleSprint(sprintId, sprintName, sprintGoal, begins, ends);

		assertEquals(sprintId.id(), sprint.sprintId().id());
		assertEquals(sprintName, sprint.name());
		assertEquals(sprintGoal, sprint.goals());
		assertEquals(begins, sprint.begins());
		assertEquals(ends, sprint.ends());
	}

	@Test
	public void captureRetrospectiveMeetingResults_OverwritesExistingRetrospective() {
		String retrospective = "We learned these five things: ...";
		assertNotEquals(retrospective, sprint.retrospective());

		sprint.captureRetrospectiveMeetingResults(retrospective);

		assertEquals(retrospective, sprint.retrospective());
	}

	@Test
	public void adjustGoals_OverwritesExistingGoals() {
		String adjustedGoals = "Make Scrum product and backlog item collaboration possible.";
		assertNotEquals(adjustedGoals, sprint.goals());

		sprint.adjustGoals(adjustedGoals);

		assertEquals(adjustedGoals, sprint.goals());
	}

	@Test
	public void nowBeginsOn_OverwritesExistingBeginDate() {
		Date date = new Date(new Date().getTime() + (86400000L * 2L));
		assertNotEquals(date, sprint.begins());

		sprint.nowBeginsOn(date);

		assertEquals(date, sprint.begins());
	}

	@Test
	public void nowEndsOn_OverwritesExistingEndDate() {
		Date date = new Date(new Date().getTime() + (86400000L * 10L));
		assertNotEquals(date, sprint.ends());

		sprint.nowEndsOn(date);

		assertEquals(date, sprint.ends());
	}

	@Test
	public void commit_AddsToBacklogItems() {
		BacklogItem itemToCommit = mockDataUtil.backlogItem1ForTest(product);
		Collection<CommittedBacklogItem> originalCommittedItems = sprint.allCommittedBacklogItems();
		assertFalse(containsBacklogItem(originalCommittedItems, itemToCommit));

		sprint.commit(itemToCommit);

		Collection<CommittedBacklogItem> updatedCommittedItems = sprint.allCommittedBacklogItems();
		assertTrue(containsBacklogItem(updatedCommittedItems, itemToCommit));
	}

	private boolean containsBacklogItem(Collection<CommittedBacklogItem> committedItems, BacklogItem item) {
		boolean containsBacklogItem = committedItems.stream()
				.anyMatch(x -> x.backlogItemId().equals(item.backlogItemId()));
		return containsBacklogItem;
	}

	@Test
	public void rename_OverwritesExistingName() {
		String changedName = "New Sprint Name";
		assertNotEquals(changedName, sprint.name());

		sprint.rename(changedName);

		assertEquals(changedName, sprint.name());
	}

	@Test
	public void reorderFrom_ReorderBacklogItems() {
		BacklogItem backlogItem1 = mockDataUtil.backlogItem1ForTest(product);
		BacklogItem backlogItem2 = mockDataUtil.backlogItem2ForTest(product);
		BacklogItem backlogItem3 = mockDataUtil.backlogItem3ForTest(product);

		sprint.commit(backlogItem1);
		sprint.commit(backlogItem2);
		sprint.commit(backlogItem3);

		CommittedBacklogItem scheduledBacklogItem1 = null;
		CommittedBacklogItem scheduledBacklogItem2 = null;
		CommittedBacklogItem scheduledBacklogItem3 = null;

		for (CommittedBacklogItem scheduledBacklogItem : sprint.allCommittedBacklogItems()) {
			if (scheduledBacklogItem.ordering() == 1) {
				scheduledBacklogItem1 = scheduledBacklogItem;
			}
			if (scheduledBacklogItem.ordering() == 2) {
				scheduledBacklogItem2 = scheduledBacklogItem;
			}
			if (scheduledBacklogItem.ordering() == 3) {
				scheduledBacklogItem3 = scheduledBacklogItem;
			}
		}

		sprint.reorderFrom(backlogItem3.backlogItemId(), 1);

		assertEquals(1, scheduledBacklogItem3.ordering());
		assertEquals(2, scheduledBacklogItem1.ordering());
		assertEquals(3, scheduledBacklogItem2.ordering());
	}

	@Test
	public void testSchedule() {
		Product product = mockDataUtil.productForTest();
		Sprint sprint = mockDataUtil.sprintForTest(product);

		BacklogItem backlogItem1 = mockDataUtil.backlogItem1ForTest(product);
		BacklogItem backlogItem2 = mockDataUtil.backlogItem2ForTest(product);
		BacklogItem backlogItem3 = mockDataUtil.backlogItem3ForTest(product);

		sprint.commit(backlogItem1);
		sprint.commit(backlogItem2);
		sprint.commit(backlogItem3);

		for (CommittedBacklogItem scheduledBacklogItem : sprint.allCommittedBacklogItems()) {
			if (scheduledBacklogItem.ordering() == 1) {
				assertTrue(scheduledBacklogItem.backlogItemId().id().endsWith("-1"));
			}
			if (scheduledBacklogItem.ordering() == 2) {
				assertTrue(scheduledBacklogItem.backlogItemId().id().endsWith("-2"));
			}
			if (scheduledBacklogItem.ordering() == 3) {
				assertTrue(scheduledBacklogItem.backlogItemId().id().endsWith("-3"));
			}
		}
	}

	@Test
	public void testUnschedule() {
		Product product = mockDataUtil.productForTest();
		Sprint sprint = mockDataUtil.sprintForTest(product);

		BacklogItem backlogItem1 = mockDataUtil.backlogItem1ForTest(product);
		BacklogItem backlogItem2 = mockDataUtil.backlogItem2ForTest(product);
		BacklogItem backlogItem3 = mockDataUtil.backlogItem3ForTest(product);

		sprint.commit(backlogItem1);
		sprint.commit(backlogItem2);
		sprint.commit(backlogItem3);

		assertEquals(3, sprint.allCommittedBacklogItems().size());

		sprint.uncommit(backlogItem2);

		assertEquals(2, sprint.allCommittedBacklogItems().size());

		for (CommittedBacklogItem scheduledBacklogItem : sprint.allCommittedBacklogItems()) {
			assertTrue(scheduledBacklogItem.backlogItemId().equals(backlogItem1.backlogItemId())
					|| scheduledBacklogItem.backlogItemId().equals(backlogItem3.backlogItemId()));
		}
	}
}