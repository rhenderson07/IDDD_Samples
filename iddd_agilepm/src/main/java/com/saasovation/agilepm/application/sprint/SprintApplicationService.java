package com.saasovation.agilepm.application.sprint;

import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItem;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemId;
import com.saasovation.agilepm.domain.model.product.backlogitem.BacklogItemRepository;
import com.saasovation.agilepm.domain.model.product.sprint.Sprint;
import com.saasovation.agilepm.domain.model.product.sprint.SprintId;
import com.saasovation.agilepm.domain.model.product.sprint.SprintRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;

public class SprintApplicationService {

	private BacklogItemRepository backlogItemRepository;
	private SprintRepository sprintRepository;

	public SprintApplicationService(SprintRepository aSprintRepository, BacklogItemRepository aBacklogItemRepository) {
		this.backlogItemRepository = aBacklogItemRepository;
		this.sprintRepository = aSprintRepository;
	}

	public void commitBacklogItemToSprint(CommitBacklogItemToSprintCommand aCommand) {

		TenantId tenantId = new TenantId(aCommand.getTenantId());
		SprintId sprintId = new SprintId(aCommand.getSprintId());
		BacklogItemId backlogItemId = new BacklogItemId(aCommand.getBacklogItemId());

		Sprint sprint = sprintRepository.sprintOfId(tenantId, sprintId);
		BacklogItem backlogItem = backlogItemRepository.backlogItemOfId(tenantId, backlogItemId);

		sprint.commit(backlogItem);

		sprintRepository.save(sprint);
	}
}
