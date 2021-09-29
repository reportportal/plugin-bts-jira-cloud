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
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class ProjectManagerCommand<T> extends ProjectMemberCommand<T> {

	protected ProjectManagerCommand(ProjectRepository projectRepository) {
		super(projectRepository);
	}

	@Override
	protected void validatePermissions(ReportPortalUser user, Project project) {
		ProjectRole projectRole = ofNullable(user.getProjectDetails()).flatMap(detailsMapping -> ofNullable(detailsMapping.get(project.getName())))
				.map(ReportPortalUser.ProjectDetails::getProjectRole)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

		BusinessRule.expect(projectRole, ProjectRole.PROJECT_MANAGER::sameOrLowerThan).verify(ErrorType.ACCESS_DENIED);
	}
}
