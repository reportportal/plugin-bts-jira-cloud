/*
 * Copyright 2025 EPAM Systems
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

import static com.epam.reportportal.extension.jira.command.UserSearchCommand.SEARCH_TERM;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.URL;

import com.epam.reportportal.extension.jira.dto.UserDto;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

@Slf4j
class UserSearchCommandTest extends BaseCommandTest {

  @Test
  @DisabledIf("disabled")
  void searchUsersByQuery() {
    Map<String, Object> params = new HashMap<>();
    params.put(SEARCH_TERM, "t");
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()).get());

    var command = new UserSearchCommand(projectRepository, cloudJiraClientProvider, organizationRepositoryCustom);
    var users = command.invokeCommand(INTEGRATION, params);
    log.info("Found users: {}", users.stream().map(UserDto::name).toList());
    Assertions.assertFalse(users.isEmpty());
  }
}
