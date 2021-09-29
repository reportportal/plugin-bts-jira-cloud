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
package com.epam.reportportal.extension.jira.service;

import com.epam.reportportal.extension.jira.dao.EntityRepository;
import com.epam.reportportal.extension.jira.entity.model.CreateEntity;
import com.epam.reportportal.extension.jira.entity.model.EntityResource;
import com.epam.reportportal.extension.jira.jooq.tables.pojos.JEntity;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.reportportal.extension.jira.utils.converter.EntityConverter.TO_RESOURCE;

public class EntityService {

	private final EntityRepository entityRepository;

	public EntityService(EntityRepository entityRepository) {
		this.entityRepository = entityRepository;
	}

	public OperationCompletionRS create(Long projectId, CreateEntity createRq) {
		final JEntity entity = new JEntity();
		entity.setName(createRq.getName());
		entity.setDescription(createRq.getDescription());
		entity.setProjectId(projectId);
		entityRepository.save(entity);
		return new OperationCompletionRS("Entity created");
	}

	public OperationCompletionRS delete(Long id) {
		entityRepository.delete(id);
		return new OperationCompletionRS("Entity removed");
	}
}
