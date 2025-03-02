package com.epam.reportportal.extension.jira.command;

import static com.epam.reportportal.extension.jira.command.GetIssueFieldsCommand.ISSUE_TYPE;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.PROJECT;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.URL;
import static com.epam.reportportal.extension.jira.utils.SampleData.EPIC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.jira.command.utils.JIRATicketDescriptionService;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


class PostTicketCommandTest extends BaseCommandTest {

  ObjectMapper objectMapper = new ObjectMapper();
  private final RequestEntityConverter requestEntityConverter = new RequestEntityConverter(objectMapper);

  @Mock
  DataStoreService dataStoreService;
  @Mock
  TestItemRepository itemRepository;
  @Mock
  LogRepository logRepository;

  @ParameterizedTest
  @CsvSource(value = {
      "Epic"
  })
  void postTicketCommand(String issueType) throws JsonProcessingException {
    if (disabled()) {
      return;
    }
    TestItem testItem = new TestItem();
    when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

    PostTicketRQ entity = objectMapper.readValue(EPIC, PostTicketRQ.class);

    Map<String, Object> params = new HashMap<>(JIRA_COMMAND_PARAMS);
    params.put(PROJECT.getName(), PROJECT.getParam(INTEGRATION.getParams()));
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()));
    params.put(ISSUE_TYPE, issueType);
    params.put("entity", entity);

    var command = new PostTicketCommand(projectRepository, requestEntityConverter, cloudJiraClientProvider,
        new JIRATicketDescriptionService(logRepository, itemRepository), dataStoreService);
    Ticket ticket = command.invokeCommand(INTEGRATION, params);

    assertNotNull(ticket);
    verifyJiraTicket(ticket);

  }

  private void verifyJiraTicket(Ticket ticket) {
    String username = (String) INTEGRATION.getParams().getParams().get("email");
    String credentials = basicTextEncryptor.decrypt((String) INTEGRATION.getParams().getParams().get("apiToken"));
    //String url = (String) INTEGRATION.getParams().getParams().get("url");

    RestTemplate restTemplate = new RestTemplateBuilder()
        .basicAuthentication(username, credentials)
        .build();

    var jiraTicket = restTemplate.getForObject(ticket.getTicketUrl(), String.class);

    // TODO: make required checks with jira ticket
  }
}
