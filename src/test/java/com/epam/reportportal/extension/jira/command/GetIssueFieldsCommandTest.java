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

import static com.epam.reportportal.extension.jira.command.GetIssueFieldsCommand.ISSUE_TYPE;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.PROJECT;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.URL;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.epam.reportportal.model.externalsystem.PostFormField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GetIssueFieldsCommandTest extends BaseCommandTest {

  @ParameterizedTest
  @CsvSource(value = {
      "Epic"
  })
  void getIssueFields(String issueType) {
    if (disabled()) {
      return;
    }

    Map<String, Object> params = new HashMap<>(JIRA_COMMAND_PARAMS);
    params.put(PROJECT.getName(), PROJECT.getParam(INTEGRATION.getParams()));
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()));
    params.put(ISSUE_TYPE, issueType);

    var command = new GetIssueFieldsCommand(projectRepository, organizationRepositoryCustom, cloudJiraClientProvider);
    List<PostFormField> response = command.invokeCommand(INTEGRATION, params);

    assertFalse(response.isEmpty());
  }
}
