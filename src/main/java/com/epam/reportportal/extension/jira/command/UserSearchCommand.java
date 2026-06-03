/*
 * Copyright 2024 EPAM Systems
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

import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.dto.UserDto;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class UserSearchCommand extends AbstractExtensionCommand<List<UserDto>> {

  private final ProjectRole minProjectRole = ProjectRole.EDITOR;
  private final OrganizationRole minOrgRole = OrganizationRole.MANAGER;
  private final UserRole minUserRole = UserRole.ADMINISTRATOR;

  public static final String SEARCH_TERM = "term";
  private final CloudJiraClientProvider cloudJiraClientProvider;

  public UserSearchCommand(ProjectRepository projectRepository, CloudJiraClientProvider cloudJiraClientProvider,
      OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  protected List<UserDto> invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    var params = pluginCommandRq.getArguments();
    JiraRestClient userClient = cloudJiraClientProvider.getApiClient(integration.getParams());
    String username = (String) ofNullable(params.get(SEARCH_TERM)).orElse("");

    return userClient.userSearchApi().findUsers(username, null, null, null, null, null)
        .stream()
        .map(user -> new UserDto(user.getAccountId(), user.getDisplayName()))
        .collect(Collectors.toList());
  }

  @Override
  public String getName() {
    return "searchUsers";
  }
}
