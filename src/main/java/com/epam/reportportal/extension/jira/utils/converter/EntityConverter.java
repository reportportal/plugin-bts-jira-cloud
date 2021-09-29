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
package com.epam.reportportal.extension.jira.utils.converter;

import com.epam.reportportal.extension.jira.entity.model.EntityResource;
import com.epam.reportportal.extension.jira.jooq.tables.pojos.JEntity;

import java.util.function.Function;

public class EntityConverter {

	public static final Function<JEntity, EntityResource> TO_RESOURCE = e -> {
		final EntityResource resource = new EntityResource();
		resource.setId(e.getId());
		resource.setName(e.getName());
		resource.setDescription(e.getDescription());
		return resource;
	};
}