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
import static com.epam.reportportal.extension.jira.utils.SampleData.EPIC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


@Slf4j
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
  void postTicketCommand(String issueType) throws JsonProcessingException, FileNotFoundException {
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

    lenient().when(dataStoreService.load(anyString()))
        .thenReturn(Optional.of(getClass().getClassLoader().getResourceAsStream("attachment.txt")));

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
    log.info(ticket.getTicketUrl());
    // TODO: make required checks with jira ticket
  }

}

