/*
 * Copyright 2021 EPAM Systems
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

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.jira.api.model.SearchResults;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketUtils;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueCommand implements CommonPluginCommand<Ticket> {

  private final String TICKET_ID = "ticketId";
  private final String PROJECT_ID = "projectId";

  private final TicketRepository ticketRepository;
  private final IntegrationRepository integrationRepository;
  private final CloudJiraClientProvider cloudJiraClientProvider;

  public GetIssueCommand(TicketRepository ticketRepository,
      IntegrationRepository integrationRepository,
      CloudJiraClientProvider cloudJiraClientProvider) {
    this.ticketRepository = ticketRepository;
    this.integrationRepository = integrationRepository;
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  public Ticket executeCommand(Map<String, Object> params) {
    var ticketId = Optional.ofNullable(params.get(TICKET_ID))
        .map(String::valueOf)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, TICKET_ID + "  must be provided"));
    final com.epam.ta.reportportal.entity.bts.Ticket ticket = ticketRepository.findByTicketId(ticketId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Ticket not found with id " + TICKET_ID));
    final Long projectId = (Long) Optional.ofNullable(params.get(PROJECT_ID))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, PROJECT_ID + " must be provided"));

    final String btsUrl = CloudJiraProperties.URL.getParam(params)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    final String btsProject = CloudJiraProperties.PROJECT.getParam(params)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));

    final Integration integration =
        integrationRepository.findProjectBtsByUrlAndLinkedProject(btsUrl, btsProject, projectId)
            .orElseGet(() -> integrationRepository.findGlobalBtsByUrlAndLinkedProject(btsUrl, btsProject)
                .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Integration with provided url and project isn't found")));
    return getTicket(ticketId, integration.getParams());
  }

  private Ticket getTicket(String ticketId, IntegrationParams details) {
    var client = cloudJiraClientProvider.getApiClient(details);
    SearchResults issues;
    try {
      var jql = String.format("project=%s and key=%s", CloudJiraProperties.PROJECT.getParam(details.getParams()).get(), ticketId);
      issues = client.issueSearchApi().searchForIssuesUsingJql(jql, null, 50, "", null, null, null, null, null);

    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
    if (CollectionUtils.isNotEmpty(issues.getIssues())) {
      return JIRATicketUtils.toTicket(issues.getIssues().getFirst(), CloudJiraProperties.URL.getParam(details)
          .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified.")));
    } else {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Ticket with id {} is not found", ticketId);
    }

  }

  @Override
  public String getName() {
    return "getIssue";
  }
}
