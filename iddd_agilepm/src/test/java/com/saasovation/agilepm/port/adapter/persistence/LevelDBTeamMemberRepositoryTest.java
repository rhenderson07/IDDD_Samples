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

import com.saasovation.agilepm.domain.model.team.TeamMember;
import com.saasovation.agilepm.domain.model.team.TeamMemberRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBUnitOfWork;

public class LevelDBTeamMemberRepositoryTest extends BaseLevelDBRepositoryTest {

	private TeamMemberRepository teamMemberRepository = new LevelDBTeamMemberRepository();

	@Test
	public void testSave() {
		TeamMember teamMember = new TeamMember(new TenantId("12345"), "jdoe", "John", "Doe", "jdoe@saasovation.com",
				new Date());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.save(teamMember);
		LevelDBUnitOfWork.current().commit();

		TeamMember savedTeamMember = teamMemberRepository.teamMemberOfIdentity(teamMember.tenantId(),
				teamMember.username());

		assertNotNull(savedTeamMember);
		assertEquals(teamMember.tenantId(), savedTeamMember.tenantId());
		assertEquals(teamMember.username(), savedTeamMember.username());
		assertEquals(teamMember.firstName(), savedTeamMember.firstName());
		assertEquals(teamMember.lastName(), savedTeamMember.lastName());
		assertEquals(teamMember.emailAddress(), savedTeamMember.emailAddress());

		Collection<TeamMember> savedTeamMembers = this.teamMemberRepository
				.allTeamMembersOfTenant(teamMember.tenantId());

		assertFalse(savedTeamMembers.isEmpty());
		assertEquals(1, savedTeamMembers.size());
	}

	@Test
	public void testRemove() {
		TeamMember teamMember1 = new TeamMember(new TenantId("12345"), "jdoe", "John", "Doe", "jdoe@saasovation.com",
				new Date());

		TeamMember teamMember2 = new TeamMember(new TenantId("12345"), "zdoe", "Zoe", "Doe", "zoe@saasovation.com",
				new Date());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.save(teamMember1);
		teamMemberRepository.save(teamMember2);
		LevelDBUnitOfWork.current().commit();

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.remove(teamMember1);
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = teamMember2.tenantId();

		Collection<TeamMember> savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(tenantId);
		assertFalse(savedTeamMembers.isEmpty());
		assertEquals(1, savedTeamMembers.size());
		assertEquals(teamMember2.username(), savedTeamMembers.iterator().next().username());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.remove(teamMember2);
		LevelDBUnitOfWork.current().commit();

		savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(tenantId);
		assertTrue(savedTeamMembers.isEmpty());
	}

	@Test
	public void testSaveAllRemoveAll() {
		TeamMember teamMember1 = new TeamMember(new TenantId("12345"), "jdoe", "John", "Doe", "jdoe@saasovation.com",
				new Date());

		TeamMember teamMember2 = new TeamMember(new TenantId("12345"), "zdoe", "Zoe", "Doe", "zoe@saasovation.com",
				new Date());

		TeamMember teamMember3 = new TeamMember(new TenantId("12345"), "jsmith", "John", "Smith",
				"jsmith@saasovation.com", new Date());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.saveAll(Arrays.asList(new TeamMember[] { teamMember1, teamMember2, teamMember3 }));
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = teamMember1.tenantId();

		Collection<TeamMember> savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(tenantId);
		assertFalse(savedTeamMembers.isEmpty());
		assertEquals(3, savedTeamMembers.size());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.removeAll(Arrays.asList(new TeamMember[] { teamMember1, teamMember3 }));
		LevelDBUnitOfWork.current().commit();

		savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(tenantId);
		assertFalse(savedTeamMembers.isEmpty());
		assertEquals(1, savedTeamMembers.size());
		assertEquals(teamMember2.username(), savedTeamMembers.iterator().next().username());

		LevelDBUnitOfWork.start(super.getDB());
		teamMemberRepository.removeAll(Arrays.asList(new TeamMember[] { teamMember2 }));
		LevelDBUnitOfWork.current().commit();

		savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(tenantId);
		assertTrue(savedTeamMembers.isEmpty());
	}

	@Test
	public void testConcurrentTransactions() {
		final List<Integer> orderOfCommits = new ArrayList<Integer>();

		TeamMember teamMember1 = new TeamMember(new TenantId("12345"), "jdoe", "John", "Doe", "jdoe@saasovation.com",
				new Date());

		LevelDBUnitOfWork.start(getDB());
		teamMemberRepository.save(teamMember1);

		new Thread() {
			@Override
			public void run() {
				TeamMember teamMember2 = new TeamMember(new TenantId("12345"), "zdoe", "Zoe", "Doe",
						"zoe@saasovation.com", new Date());

				LevelDBUnitOfWork.start(getDB());
				teamMemberRepository.save(teamMember2);
				LevelDBUnitOfWork.current().commit();
				orderOfCommits.add(2);
			}
		}.start();

		try {
			Thread.sleep(250L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LevelDBUnitOfWork.current().commit();
		orderOfCommits.add(1);

		for (int idx = 0; idx < orderOfCommits.size(); ++idx) {
			assertEquals(idx + 1, orderOfCommits.get(idx).intValue());
		}

		try {
			Thread.sleep(250L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Collection<TeamMember> savedTeamMembers = teamMemberRepository.allTeamMembersOfTenant(teamMember1.tenantId());

		assertFalse(savedTeamMembers.isEmpty());
		assertEquals(2, savedTeamMembers.size());
	}
}
