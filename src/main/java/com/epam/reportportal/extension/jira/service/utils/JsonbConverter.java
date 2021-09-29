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
package com.epam.reportportal.extension.jira.service.utils;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class JsonbConverter {

	private final ObjectMapper objectMapper;

	public JsonbConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> T convert(JSONB jsonb, Class<T> clazz) {
		try {
			return objectMapper.readValue(jsonb.data(), clazz);
		} catch (JsonProcessingException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
		}
	}
}