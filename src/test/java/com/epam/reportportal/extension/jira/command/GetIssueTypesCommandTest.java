package com.epam.reportportal.extension.jira.command;

import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.PROJECT;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

class GetIssueTypesCommandTest extends BaseCommandTest {

  @Test
  @DisabledIf("disabled")
  void getIssueTypes() {
    Map<String, Object> params = new HashMap<>(JIRA_COMMAND_PARAMS);
    params.put(PROJECT.getName(), PROJECT.getParam(INTEGRATION.getParams()));
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()));

    var command = new GetIssueTypesCommand(projectRepository, cloudJiraClientProvider);
    List<String> response = command.invokeCommand(INTEGRATION, params);
    Assertions.assertFalse(response.isEmpty());
  }
}
