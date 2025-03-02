package com.epam.reportportal.extension.jira.command;

import static com.epam.reportportal.extension.jira.command.UserSearchCommand.SEARCH_TERM;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.EMAIL;
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
    params.put(SEARCH_TERM, EMAIL.getParam(INTEGRATION.getParams()).get());
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()).get());

    var command = new UserSearchCommand(projectRepository, cloudJiraClientProvider);
    var users = command.invokeCommand(INTEGRATION, params);
    log.info("Found users: {}", users.stream().map(UserDto::username).toList());
    Assertions.assertFalse(users.isEmpty());
  }
}
