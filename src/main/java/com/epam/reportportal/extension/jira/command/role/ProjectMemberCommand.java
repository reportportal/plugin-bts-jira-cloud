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
package com.epam.reportportal.extension.jira.command.role;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.epam.reportportal.extension.jira.command.utils.CommandParamUtils.PROJECT_ID_PARAM;
import static com.epam.reportportal.extension.jira.command.utils.CommandParamUtils.retrieveLong;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class ProjectMemberCommand<T> extends AbstractRoleBasedCommand<T> {

	protected final ProjectRepository projectRepository;

	protected ProjectMemberCommand(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public void validateRole(Map<String, Object> params) {
		Long projectId = retrieveLong(params, PROJECT_ID_PARAM);
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

		ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

		validatePermissions(user, project);
	}

	protected void validatePermissions(ReportPortalUser user, Project project) {
		BusinessRule.expect(ofNullable(user.getProjectDetails()).flatMap(detailsMapping -> ofNullable(detailsMapping.get(project.getName()))),
				Optional::isPresent
		).verify(ErrorType.ACCESS_DENIED);
	}
}
