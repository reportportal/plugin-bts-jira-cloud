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

package com.epam.reportportal.extension.jira.command;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.epam.reportportal.extension.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.jira.api.model.Project;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
public class GetIssueTypesCommand extends AbstractExtensionCommand<List<String>> {

  // Override AbstractExtensionCommand permission levels
  private final ProjectRole minProjectRole = ProjectRole.EDITOR;
  private final OrganizationRole minOrgRole = OrganizationRole.MANAGER;
  private final UserRole minUserRole = UserRole.ADMINISTRATOR;
  //

  private final CloudJiraClientProvider cloudJiraClientProvider;

  public GetIssueTypesCommand(ProjectRepository projectRepository,
      CloudJiraClientProvider cloudJiraClientProvider,
      OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  public String getName() {
    return "getIssueTypes";
  }

  @Override
  public List<String> invokeCommand(Integration integration, PluginCommandRQ pluginCommandRQ) {
    try {
      JiraRestClient client = cloudJiraClientProvider.getApiClient(integration.getParams());
      var projectKey = CloudJiraProperties.PROJECT.getParam(integration.getParams()).get();
      Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

      return jiraProject.getIssueTypes().stream()
          .map(IssueTypeDetails::getName)
          .toList();
    } catch (RestClientException e) {
      log.error("Error while fetching issue types from Jira API", e);
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project not found.");
    } catch (Exception e) {
      log.error("Unexpected error occurred while trying to get issue types", e);
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Check connection settings.");
    }
  }
}
