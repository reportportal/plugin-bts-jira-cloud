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
import com.epam.reportportal.extension.jira.service.EntityService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.Map;

public class DeleteEntityCommand extends ProjectManagerCommand<OperationCompletionRS> {

	private final EntityService entityService;

	public DeleteEntityCommand(ProjectRepository projectRepository, EntityService entityService) {
		super(projectRepository);
		this.entityService = entityService;
	}

	@Override
	protected OperationCompletionRS invokeCommand(Integration integration, Map<String, Object> params) {
		final Long id = CommandParamUtils.retrieveLong(params, CommandParamUtils.ID_PARAM);
		return entityService.delete(id);
	}
}
