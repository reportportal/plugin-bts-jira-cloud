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

import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static java.util.stream.Collectors.toSet;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.reportportal.extension.jira.command.connection.TestConnectionCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketDescriptionService;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketUtils;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.reportportal.extension.util.RequestEntityValidator;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.reportportal.rules.exception.ReportPortalException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class PostTicketCommand extends ProjectMemberCommand<Ticket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostTicketCommand.class);

  private final RequestEntityConverter requestEntityConverter;

  private final CloudJiraClientProvider cloudJiraClientProvider;

  private final JIRATicketDescriptionService descriptionService;

  private final DataStoreService dataStoreService;

  public PostTicketCommand(ProjectRepository projectRepository,
      RequestEntityConverter requestEntityConverter,
      CloudJiraClientProvider cloudJiraClientProvider,
      JIRATicketDescriptionService descriptionService, DataStoreService dataStoreService) {
    super(projectRepository);
    this.requestEntityConverter = requestEntityConverter;
    this.cloudJiraClientProvider = cloudJiraClientProvider;
    this.descriptionService = descriptionService;
    this.dataStoreService = dataStoreService;
  }

  @Override
  protected Ticket invokeCommand(Integration integration, Map<String, Object> params) {
    PostTicketRQ ticketRQ =
        requestEntityConverter.getEntity(ENTITY_PARAM, params, PostTicketRQ.class);
    RequestEntityValidator.validate(ticketRQ);
    expect(ticketRQ.getFields(), not(isNull())).verify(
        UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");
    List<PostFormField> fields = ticketRQ.getFields();

    // TODO add validation of any field with allowedValues() array
    // Additional validation required for unsupported
    // ticket type and/or components in JIRA.
    PostFormField issueType = new PostFormField();
    PostFormField components = new PostFormField();
    PostFormField linkedIssue = null;
    for (PostFormField object : fields) {
      if ("issuetype".equalsIgnoreCase(object.getId())) {
        issueType = object;
      }
      if ("components".equalsIgnoreCase(object.getId())) {
        components = object;
      }
      if ("issuelinks".equalsIgnoreCase(object.getId())) {
        linkedIssue = object;
      }
    }

    expect(issueType.getValue().size(), equalTo(1)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
        formattedSupplier("[IssueType] field has multiple values '{}' but should be only one",
            issueType.getValue()
        )
    );
    final String issueTypeStr = issueType.getValue().get(0);

    try (JiraRestClient client = cloudJiraClientProvider.get(integration.getParams())) {
      Project jiraProject = client.getProjectClient().getProject(
          CloudJiraProperties.PROJECT.getParam(integration.getParams()).orElseThrow(
              () -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                  "Project is not specified."
              ))).claim();

      if (null != components.getValue()) {
        Set<String> validComponents =
            StreamSupport.stream(jiraProject.getComponents().spliterator(), false)
                .map(BasicComponent::getName).collect(toSet());
        validComponents.forEach(component -> expect(component, in(validComponents)).verify(
            UNABLE_INTERACT_WITH_INTEGRATION,
            formattedSupplier("Component '{}' not exists in the external system", component)
        ));
      }

      // TODO consider to modify code below - project cached
      Optional<IssueType> issueTypeOptional =
          StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
              .filter(input -> issueTypeStr.equalsIgnoreCase(input.getName())).findFirst();

      expect(issueTypeOptional, Preconditions.IS_PRESENT).verify(UNABLE_INTERACT_WITH_INTEGRATION,
          formattedSupplier("Unable post issue with type '{}' for project '{}'.",
              issueType.getValue().get(0), integration.getProject()
          )
      );
      IssueInput issueInput =
          JIRATicketUtils.toIssueInput(client, jiraProject, issueTypeOptional, ticketRQ,
              descriptionService
          );

      Map<String, String> binaryData = findBinaryData(issueInput);

      /*
       * Claim because we wanna be sure everything is OK
       */
      BasicIssue createdIssue = client.getIssueClient().createIssue(issueInput).claim();

      // post binary data
      Issue issue = client.getIssueClient().getIssue(createdIssue.getKey()).claim();

      AttachmentInput[] attachmentInputs = new AttachmentInput[binaryData.size()];
      int counter = 0;
      for (Map.Entry<String, String> binaryDataEntry : binaryData.entrySet()) {

        Optional<InputStream> data = dataStoreService.load(binaryDataEntry.getKey());
        if (data.isPresent()) {
          attachmentInputs[counter] = new AttachmentInput(binaryDataEntry.getValue(), data.get());
          counter++;
        }
      }
      if (counter != 0) {
        client.getIssueClient()
            .addAttachments(issue.getAttachmentsUri(), Arrays.copyOf(attachmentInputs, counter))
            .claim();
      }
      if (linkedIssue != null) {
        linkIssues(client, issue, linkedIssue);
      }

      return getTicket(createdIssue.getKey(), integration.getParams(), client).orElse(null);

    } catch (ReportPortalException e) {
      throw e;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, e.getMessage());
    }
  }

  @Override
  public String getName() {
    return "postTicket";
  }

  /**
   * Parse ticket description and find binary data
   *
   * @param issueInput Jira issue
   * @return Parsed parameters
   */
  private Map<String, String> findBinaryData(IssueInput issueInput) {
    Map<String, String> binary = new HashMap<>();
    String description =
        issueInput.getField(IssueFieldId.DESCRIPTION_FIELD.id).getValue().toString();
    if (null != description) {
      // !54086a2c3c0c7d4446beb3e6.jpg| or [^54086a2c3c0c7d4446beb3e6.xml]
      String regex = "(!|\\[\\^)\\w+\\.\\w{0,10}(\\||\\])";
      Matcher matcher = Pattern.compile(regex).matcher(description);
      while (matcher.find()) {
        String rawValue = description.subSequence(matcher.start(), matcher.end()).toString();
        String binaryDataName =
            rawValue.replace("!", "").replace("[", "").replace("]", "").replace("^", "")
                .replace("|", "");
        String binaryDataId = binaryDataName.split("\\.")[0];
        binary.put(binaryDataId, binaryDataName);
      }
    }
    return binary;
  }

  private Optional<Ticket> getTicket(String id, IntegrationParams details,
      JiraRestClient jiraRestClient) {
    SearchResult issues = findIssue(id, jiraRestClient);
    if (issues.getTotal() > 0) {
      Issue issue = jiraRestClient.getIssueClient().getIssue(id).claim();
      return Optional.of(JIRATicketUtils.toTicket(issue, CloudJiraProperties.URL.getParam(details)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
              "Url is not specified."
          ))));
    } else {
      return Optional.empty();
    }
  }

  private SearchResult findIssue(String id, JiraRestClient jiraRestClient) {
    return jiraRestClient.getSearchClient().searchJql("issue = " + id).claim();
  }

  private void linkIssues(JiraRestClient jiraRestClient, Issue issue, PostFormField field) {
    LOGGER.error("linkIssues: " + issue);
    LOGGER.error("FieldInput: " + field);
    String value = field.getValue().get(0);
    String[] s = value.split(" ");
    for (String v: s) {
      LOGGER.error("Field value: " + v);
      LinkIssuesInput linkIssuesInput = new LinkIssuesInput(issue.getKey(), v, "Relates");
      jiraRestClient.getIssueClient().linkIssue(linkIssuesInput).claim();
    }
  }
}
