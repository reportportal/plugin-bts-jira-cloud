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

import com.epam.reportportal.extension.ProjectManagerCommand;
import com.epam.reportportal.extension.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.jira.api.model.Project;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClientException;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueTypesCommand extends ProjectManagerCommand<List<String>> {

  private final CloudJiraClientProvider cloudJiraClientProvider;

  public GetIssueTypesCommand(ProjectRepository projectRepository,
      CloudJiraClientProvider cloudJiraClientProvider) {
    super(projectRepository);
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  public String getName() {
    return "getIssueTypes";
  }

  @Override
  protected List<String> invokeCommand(Integration integration, Map<String, Object> params) {
    try {
      JiraRestClient client = cloudJiraClientProvider.getApiClient(integration.getParams());
      var projectKey = CloudJiraProperties.PROJECT.getParam(integration.getParams()).get();
      Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

      return jiraProject.getIssueTypes().stream()
          .map(IssueTypeDetails::getName)
          .toList();
    } catch (RestClientException e) {
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project not found.");
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Check connection settings.");
    }
  }
}
