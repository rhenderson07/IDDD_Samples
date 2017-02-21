/**
   Copyright 2012,2013 Vaughn Vernon

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.*/

package com.saasovation.agilepm.application.team;

import com.saasovation.agilepm.application.ApplicationServiceLifeCycle;
import com.saasovation.agilepm.domain.model.team.ProductOwner;
import com.saasovation.agilepm.domain.model.team.ProductOwnerRepository;
import com.saasovation.agilepm.domain.model.team.TeamMember;
import com.saasovation.agilepm.domain.model.team.TeamMemberRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;

public class TeamApplicationService {

	private ProductOwnerRepository productOwnerRepository;
	private TeamMemberRepository teamMemberRepository;

	public TeamApplicationService(TeamMemberRepository aTeamMemberRepository,
			ProductOwnerRepository aProductOwnerRepository) {

		super();

		this.productOwnerRepository = aProductOwnerRepository;
		this.teamMemberRepository = aTeamMemberRepository;
	}

	public void enableProductOwner(EnableProductOwnerCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			ProductOwner productOwner = productOwnerRepository.productOwnerOfIdentity(tenantId, aCommand.getUsername());

			if (productOwner == null) {
				productOwner = new ProductOwner(tenantId, aCommand.getUsername(), aCommand.getFirstName(),
						aCommand.getLastName(), aCommand.getEmailAddress(), aCommand.getOccurredOn());
			}

			productOwner.enable(aCommand.getOccurredOn());
			productOwnerRepository.save(productOwner);

			ApplicationServiceLifeCycle.success();
		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}

	public void enableTeamMember(EnableTeamMemberCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			TeamMember teamMember = teamMemberRepository.teamMemberOfIdentity(tenantId, aCommand.getUsername());

			if (teamMember == null) {
				teamMember = new TeamMember(tenantId, aCommand.getUsername(), aCommand.getFirstName(),
						aCommand.getLastName(), aCommand.getEmailAddress(), aCommand.getOccurredOn());
			}

			teamMember.enable(aCommand.getOccurredOn());
			teamMemberRepository.save(teamMember);

			ApplicationServiceLifeCycle.success();
		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}

	public void changeTeamMemberEmailAddress(ChangeTeamMemberEmailAddressCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			ProductOwner productOwner = productOwnerRepository.productOwnerOfIdentity(tenantId, aCommand.getUsername());

			if (productOwner != null) {
				productOwner.changeEmailAddress(aCommand.getEmailAddress(), aCommand.getOccurredOn());

				productOwnerRepository.save(productOwner);
			}

			TeamMember teamMember = this.teamMemberRepository.teamMemberOfIdentity(tenantId, aCommand.getUsername());

			if (teamMember != null) {
				teamMember.changeEmailAddress(aCommand.getEmailAddress(), aCommand.getOccurredOn());

				teamMemberRepository.save(teamMember);
			}

			ApplicationServiceLifeCycle.success();

		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}

	public void changeTeamMemberName(ChangeTeamMemberNameCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			ProductOwner productOwner = productOwnerRepository.productOwnerOfIdentity(tenantId, aCommand.getUsername());

			if (productOwner != null) {
				productOwner.changeName(aCommand.getFirstName(), aCommand.getLastName(), aCommand.getOccurredOn());

				productOwnerRepository.save(productOwner);
			}

			TeamMember teamMember = teamMemberRepository.teamMemberOfIdentity(tenantId, aCommand.getUsername());

			if (teamMember != null) {
				teamMember.changeName(aCommand.getFirstName(), aCommand.getLastName(), aCommand.getOccurredOn());

				teamMemberRepository.save(teamMember);
			}

			ApplicationServiceLifeCycle.success();

		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}

	public void disableProductOwner(DisableProductOwnerCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			ProductOwner productOwner = this.productOwnerRepository.productOwnerOfIdentity(tenantId,
					aCommand.getUsername());

			if (productOwner != null) {
				productOwner.disable(aCommand.getOccurredOn());

				productOwnerRepository.save(productOwner);
			}

			ApplicationServiceLifeCycle.success();

		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}

	public void disableTeamMember(DisableTeamMemberCommand aCommand) {
		TenantId tenantId = new TenantId(aCommand.getTenantId());

		ApplicationServiceLifeCycle.begin();

		try {
			TeamMember teamMember = this.teamMemberRepository.teamMemberOfIdentity(tenantId, aCommand.getUsername());

			if (teamMember != null) {
				teamMember.disable(aCommand.getOccurredOn());

				teamMemberRepository.save(teamMember);
			}

			ApplicationServiceLifeCycle.success();

		} catch (RuntimeException e) {
			ApplicationServiceLifeCycle.fail(e);
		}
	}
}
