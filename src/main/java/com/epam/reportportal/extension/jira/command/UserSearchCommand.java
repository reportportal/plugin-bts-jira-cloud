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

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.dto.UserDto;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class UserSearchCommand extends ProjectMemberCommand<List<UserDto>> {

  public static final String SEARCH_TERM = "term";
  private final CloudJiraClientProvider cloudJiraClientProvider;

  public UserSearchCommand(ProjectRepository projectRepository, CloudJiraClientProvider cloudJiraClientProvider) {
    super(projectRepository);
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  protected List<UserDto> invokeCommand(Integration integration, Map<String, Object> params) {
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
