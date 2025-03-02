package com.epam.reportportal.extension.jira.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

class TestConnectionCommandTest extends BaseCommandTest {

  @Test
  @DisabledIf("disabled")
  void testConnection() {
    var command = new TestConnectionCommand(cloudJiraClientProvider);
    Boolean response = command.executeCommand(INTEGRATION, new HashMap<>());
    assertTrue(response);
  }
}
