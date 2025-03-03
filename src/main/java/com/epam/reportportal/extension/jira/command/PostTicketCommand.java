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

import static com.epam.reportportal.extension.jira.command.utils.JIRATicketUtils.getAuthorizationHeader;
import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.reportportal.extension.jira.api.model.CreatedIssue;
import com.epam.reportportal.extension.jira.api.model.IssueBean;
import com.epam.reportportal.extension.jira.api.model.IssueLinkType;
import com.epam.reportportal.extension.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.jira.api.model.IssueUpdateDetails;
import com.epam.reportportal.extension.jira.api.model.LinkIssueRequestJsonBean;
import com.epam.reportportal.extension.jira.api.model.ProjectComponent;
import com.epam.reportportal.extension.jira.api.model.SearchResults;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.extension.jira.command.utils.IssueField;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketDescriptionService;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketUtils;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.reportportal.extension.util.RequestEntityValidator;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
public class PostTicketCommand extends ProjectMemberCommand<Ticket> {

  private final RequestEntityConverter requestEntityConverter;

  private final CloudJiraClientProvider cloudJiraClientProvider;

  private final JIRATicketDescriptionService descriptionService;

  private final DataStoreService dataStoreService;

  private static final String LINKED_ISSUE_TYPE = "Relates";

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
    PostTicketRQ ticketRQ = requestEntityConverter.getEntity(ENTITY_PARAM, params, PostTicketRQ.class);
    RequestEntityValidator.validate(ticketRQ);
    expect(ticketRQ.getFields(), not(isNull()))
        .verify(UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");
    List<PostFormField> fields = ticketRQ.getFields();

    // TODO add validation of any field with allowedValues() array
    // Additional validation required for unsupported
    // ticket type and/or components in JIRA.
    PostFormField issueType = new PostFormField();
    PostFormField components = new PostFormField();
    PostFormField linkedIssue = null;
    for (PostFormField field : fields) {
      if ("issuetype".equalsIgnoreCase(field.getId())) {
        issueType = field;
      }
      if ("components".equalsIgnoreCase(field.getId())) {
        components = field;
      }
      if ("issuelinks".equalsIgnoreCase(field.getId())) {
        linkedIssue = field;
      }
    }
    var issType = issueType;

    expect(issueType.getValue().size(), equalTo(1)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
        formattedSupplier("[IssueType] field has multiple values '{}' but should be only one",
            issueType.getValue()
        )
    );
    final String issueTypeStr = issueType.getValue().getFirst();

    try {
      JiraRestClient client = cloudJiraClientProvider.getApiClient(integration.getParams());

      String projectKey = CloudJiraProperties.PROJECT.getParam(integration.getParams())
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
      var jiraProject = client.projectsApi().getProject(projectKey, null, null);

      if (components.getValue() != null) {
        Set<String> validComponents = jiraProject.getComponents().stream()
            .map(ProjectComponent::getName)
            .collect(toSet());

        // FIXME : compares with itself by mistake
        validComponents.forEach(component -> expect(component, in(validComponents))
            .verify(UNABLE_INTERACT_WITH_INTEGRATION, formattedSupplier("Component '{}' not exists in the external system", component)));
      }

      // TODO consider to modify code below - project cached
      IssueTypeDetails projectIssueType = jiraProject.getIssueTypes().stream()
          .filter(input -> issueTypeStr.equalsIgnoreCase(input.getName()))
          .findFirst()
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
              formattedSupplier("Unable post issue with type '{}' for project '{}'.", issType.getValue().getFirst(), integration.getProject())));

      IssueUpdateDetails issueRequest = JIRATicketUtils.toIssueInput(client, jiraProject, projectIssueType, ticketRQ, descriptionService);

      Map<String, String> binaryData = findBinaryData(issueRequest);

      /*
       * Claim because we want to be sure everything is OK
       */
      CreatedIssue createdIssue = client.issuesApi().createIssue(issueRequest, false);
      String issueKey = createdIssue.getKey();

      // post binary data
      IssueBean issue = client.issuesApi().getIssue(issueKey, null, false, null, null, false, false);
      for (Map.Entry<String, String> binaryDataEntry : binaryData.entrySet()) {
        Optional<InputStream> data = dataStoreService.load(binaryDataEntry.getKey());
        if (data.isPresent()) {
          binaryDataEntry.getValue();
          addAttachment(issueKey, integration, data.get());
        }
      }

      if (linkedIssue != null) {
        linkIssues(client, issue, linkedIssue);
      }

      return getTicket(issueKey, integration.getParams(), client)
          .orElse(null);

    } catch (ReportPortalException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage());
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
  private Map<String, String> findBinaryData(IssueUpdateDetails issueInput) {
    Map<String, String> binary = new HashMap<>();
    if (issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()) != null) {
      String description = issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()).toString();
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

  private Optional<Ticket> getTicket(String id, IntegrationParams details, JiraRestClient jiraRestClient) {
    SearchResults issues = findIssue(id, jiraRestClient);
    if (issues.getTotal() > 0) {
      IssueBean issue = jiraRestClient.issuesApi().getIssue(id, null, false, null, null, false, false);
      return Optional.of(JIRATicketUtils.toTicket(issue, CloudJiraProperties.URL.getParam(details)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."))));
    } else {
      return Optional.empty();
    }
  }

  private SearchResults findIssue(String id, JiraRestClient jiraRestClient) {
    return jiraRestClient.issueSearchApi().searchForIssuesUsingJql("issue = " + id, null, 50, "", null, null, null, null, null);
  }

  private void linkIssues(JiraRestClient jiraRestClient, IssueBean issue, PostFormField field) {
    String value = CollectionUtils.isNotEmpty(field.getValue()) ? field.getValue().get(0) : "";
    if (StringUtils.isNotEmpty(value)) {
      String[] issues = value.split(" ");
      for (String issueKey : issues) {
        var issueToLink = new IssueLinkType()
            .inward(issueKey)
            .outward(issue.getKey())
            .name(LINKED_ISSUE_TYPE);

        LinkIssueRequestJsonBean linkIssuesInput = new LinkIssueRequestJsonBean(null, null, null, issueToLink);
        jiraRestClient.issueLinksApi().linkIssues(linkIssuesInput);
      }
    }
  }

  @SneakyThrows
  public void addAttachment(String issueKey, Integration integration, InputStream data) throws RestClientException {
    var details = cloudJiraClientProvider.extractAndDecryptDetails(integration.getParams());

    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .setLaxMode()
        .setCharset(StandardCharsets.UTF_8)
        .addBinaryBody("file", data.readAllBytes(), ContentType.DEFAULT_BINARY, "attachment.txt");

    HttpPost request = new HttpPost(details.url() + String.format("/rest/api/3/issue/%s/attachments", issueKey));
    request.setEntity(entityBuilder.build());
    request.setHeader("X-Atlassian-Token", "no-check");
    request.setHeader("Authorization", "Basic " + getAuthorizationHeader(details.username(), details.credentials()));
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(request);
    Assert.isTrue(response.getCode() == 200, "Failed to upload attachment");
  }
}
