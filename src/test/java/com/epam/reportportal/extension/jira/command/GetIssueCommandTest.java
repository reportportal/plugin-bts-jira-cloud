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

import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.PROJECT;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties.URL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.bts.Ticket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.Mock;


class GetIssueCommandTest extends BaseCommandTest {

  @Mock
  private IntegrationRepository integrationRepository;

  @Mock
  private TicketRepository ticketRepository;

  @Test
  @DisabledIf("disabled")
  void getIssueCommand() {
    Map<String, Object> params = new HashMap<>(JIRA_COMMAND_PARAMS);
    params.put(PROJECT.getName(), PROJECT.getParam(INTEGRATION.getParams()));
    params.put(URL.getName(), URL.getParam(INTEGRATION.getParams()));

    var ticket = new Ticket();
    ticket.setTicketId(String.valueOf((JIRA_COMMAND_PARAMS.get(TICKET_ID_FIELD))));
    when(ticketRepository.findByTicketId(anyString())).thenReturn(Optional.of(ticket));
    when(integrationRepository.findProjectBtsByUrlAndLinkedProject(anyString(), anyString(), anyLong()))
        .thenReturn(Optional.of(INTEGRATION));

    var command = new GetIssueCommand(ticketRepository, integrationRepository, cloudJiraClientProvider);
    Object response = command.executeCommand(params);

    assertNotNull(response);
  }
}
