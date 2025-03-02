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

    var command = new GetIssueFieldsCommand(projectRepository, cloudJiraClientProvider);
    List<PostFormField> response = command.invokeCommand(INTEGRATION, params);

    assertFalse(response.isEmpty());
  }
}
