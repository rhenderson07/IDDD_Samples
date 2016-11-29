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
import java.util.List;

import org.junit.Test;

import com.saasovation.agilepm.domain.model.team.Team;
import com.saasovation.agilepm.domain.model.team.TeamRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBUnitOfWork;

public class LevelDBTeamRepositoryTest extends BaseLevelDBRepositoryTest {

	private TeamRepository teamRepository = new LevelDBTeamRepository();

	@Test
	public void testSave() {
		Team team = new Team(new TenantId("12345"), "team1");

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.save(team);
		LevelDBUnitOfWork.current().commit();

		Team savedTeam = teamRepository.teamNamed(team.tenantId(), team.name());

		assertNotNull(savedTeam);
		assertEquals(team.tenantId(), savedTeam.tenantId());
		assertEquals(team.name(), savedTeam.name());

		Collection<Team> savedTeams = this.teamRepository.allTeamsOfTenant(team.tenantId());

		assertFalse(savedTeams.isEmpty());
		assertEquals(1, savedTeams.size());
	}

	@Test
	public void testRemove() {
		Team team1 = new Team(new TenantId("12345"), "team1");

		Team team2 = new Team(new TenantId("12345"), "team2");

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.save(team1);
		teamRepository.save(team2);
		LevelDBUnitOfWork.current().commit();

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.remove(team1);
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = team2.tenantId();

		Collection<Team> savedTeams = teamRepository.allTeamsOfTenant(tenantId);
		assertFalse(savedTeams.isEmpty());
		assertEquals(1, savedTeams.size());
		assertEquals(team2.name(), savedTeams.iterator().next().name());

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.remove(team2);
		LevelDBUnitOfWork.current().commit();

		savedTeams = teamRepository.allTeamsOfTenant(tenantId);
		assertTrue(savedTeams.isEmpty());
	}

	@Test
	public void testSaveAllRemoveAll() {
		Team team1 = new Team(new TenantId("12345"), "team1");

		Team team2 = new Team(new TenantId("12345"), "team2");

		Team team3 = new Team(new TenantId("12345"), "team3");

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.saveAll(Arrays.asList(new Team[] { team1, team2, team3 }));
		LevelDBUnitOfWork.current().commit();

		TenantId tenantId = team1.tenantId();

		Collection<Team> savedTeams = teamRepository.allTeamsOfTenant(tenantId);
		assertFalse(savedTeams.isEmpty());
		assertEquals(3, savedTeams.size());

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.removeAll(Arrays.asList(new Team[] { team1, team3 }));
		LevelDBUnitOfWork.current().commit();

		savedTeams = teamRepository.allTeamsOfTenant(tenantId);
		assertFalse(savedTeams.isEmpty());
		assertEquals(1, savedTeams.size());
		assertEquals(team2.name(), savedTeams.iterator().next().name());

		LevelDBUnitOfWork.start(super.getDB());
		teamRepository.removeAll(Arrays.asList(new Team[] { team2 }));
		LevelDBUnitOfWork.current().commit();

		savedTeams = teamRepository.allTeamsOfTenant(tenantId);
		assertTrue(savedTeams.isEmpty());
	}

	@Test
	public void testConcurrentTransactions() {
		final List<Integer> orderOfCommits = new ArrayList<Integer>();

		Team team1 = new Team(new TenantId("12345"), "team1");

		LevelDBUnitOfWork.start(getDB());
		teamRepository.save(team1);

		new Thread() {
			@Override
			public void run() {
				Team team2 = new Team(new TenantId("12345"), "team2");

				LevelDBUnitOfWork.start(getDB());
				teamRepository.save(team2);
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

		Collection<Team> savedTeams = teamRepository.allTeamsOfTenant(team1.tenantId());

		assertFalse(savedTeams.isEmpty());
		assertEquals(2, savedTeams.size());
	}

}
