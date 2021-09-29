/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.jira.command.entity;

import com.epam.reportportal.extension.jira.command.role.ProjectManagerCommand;
import com.epam.reportportal.extension.jira.command.utils.CommandParamUtils;
import com.epam.reportportal.extension.jira.command.utils.RequestEntityConverter;
import com.epam.reportportal.extension.jira.command.utils.RequestEntityValidator;
import com.epam.reportportal.extension.jira.entity.model.CreateEntity;
import com.epam.reportportal.extension.jira.service.EntityService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.Map;

import static com.epam.reportportal.extension.jira.command.utils.CommandParamUtils.ENTITY_PARAM;

public class CreateEntityCommand extends ProjectManagerCommand<OperationCompletionRS> {

	private final RequestEntityConverter requestEntityConverter;
	private final EntityService entityService;

	public CreateEntityCommand(ProjectRepository projectRepository, RequestEntityConverter requestEntityConverter,
			EntityService entityService) {
		super(projectRepository);
		this.requestEntityConverter = requestEntityConverter;
		this.entityService = entityService;
	}

	@Override
	protected OperationCompletionRS invokeCommand(Integration integration, Map<String, Object> params) {
		Long projectId = CommandParamUtils.retrieveLong(params, CommandParamUtils.PROJECT_ID_PARAM);
		CreateEntity createRq = requestEntityConverter.getEntity(ENTITY_PARAM, params, CreateEntity.class);
		RequestEntityValidator.validate(createRq);
		return entityService.create(projectId, createRq);
	}
}
