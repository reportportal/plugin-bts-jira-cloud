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

import static com.epam.reportportal.extension.jira.command.utils.IssueField.AFFECTS_VERSIONS_FIELD;
import static com.epam.reportportal.extension.jira.command.utils.IssueField.ASSIGNEE_FIELD;
import static com.epam.reportportal.extension.jira.command.utils.IssueField.COMPONENTS_FIELD;
import static com.epam.reportportal.extension.jira.command.utils.IssueField.FIX_VERSIONS_FIELD;
import static com.epam.reportportal.extension.jira.command.utils.IssueField.ISSUE_TYPE_FIELD;
import static com.epam.reportportal.extension.jira.command.utils.IssueField.PRIORITY_FIELD;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.extension.ProjectManagerCommand;
import com.epam.reportportal.extension.jira.api.model.IssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.jira.api.model.IssueTypeIssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.Project;
import com.epam.reportportal.extension.jira.api.model.ProjectComponent;
import com.epam.reportportal.extension.jira.api.model.ProjectIssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.Version;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketUtils;
import com.epam.reportportal.model.externalsystem.AllowedValue;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueFieldsCommand extends ProjectManagerCommand<List<PostFormField>> {

  public static final String ISSUE_TYPE = "issueType";
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIssueFieldsCommand.class);

  private final CloudJiraClientProvider cloudJiraClientProvider;

  public GetIssueFieldsCommand(ProjectRepository projectRepository, CloudJiraClientProvider cloudJiraClientProvider) {
    super(projectRepository);
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  public String getName() {
    return "getIssueFields";
  }

  @Override
  protected List<PostFormField> invokeCommand(Integration integration, Map<String, Object> params) {
    List<PostFormField> result = new ArrayList<>();

    final String issueTypeParam = Optional.ofNullable(params.get(ISSUE_TYPE))
        .map(it -> (String) it)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Issue type is not provided"));

    try {
      var client = cloudJiraClientProvider.getApiClient(integration.getParams());
      var projectKey = CloudJiraProperties.PROJECT.getParam(integration.getParams())
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
      Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

      IssueTypeDetails issueType = jiraProject.getIssueTypes().stream()
          .filter(input -> issueTypeParam.equalsIgnoreCase(input.getName()))
          .findFirst()
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Issue type '" + issueTypeParam + "' not found"));

      IssueCreateMetadata issueCreateMetadata = client.issuesApi().getCreateIssueMeta(
          Collections.singletonList(jiraProject.getId()),
          null,
          Collections.singletonList(issueType.getId()),
          null,
          "projects.issuetypes.fields"
      );

      ProjectIssueCreateMetadata project = issueCreateMetadata.getProjects().get(0);
      IssueTypeIssueCreateMetadata cimIssueType = project.getIssuetypes().get(0);
      cimIssueType.getFields().entrySet().stream().forEach(issueField -> {
        List<String> defValue = null;
        String fieldID = issueField.getKey();
        List<AllowedValue> allowed = new ArrayList<>();
        String fieldName = issueField.getValue().getName();
        if ("reporter".equalsIgnoreCase(fieldID)
            || "project".equalsIgnoreCase(fieldID)
            || "attachment".equalsIgnoreCase(fieldID)
            || "timetracking".equalsIgnoreCase(fieldID)
            || "Epic Link".equalsIgnoreCase(fieldName)
            || "Sprint".equalsIgnoreCase(fieldName)) {
          return;
        }

        String fieldType = issueField.getValue().getSchema().getType();
        boolean isRequired = issueField.getValue().getRequired();
        String commandName = null;

        // Provide values for custom fields with predefined options
        if (issueField.getValue().getAllowedValues() != null) {
          allowed = issueField.getValue().getAllowedValues().stream()
            .map(value -> (JsonNode) new ObjectMapper().valueToTree(value))
            .filter(JIRATicketUtils::isCustomField)
            .map(jn -> new AllowedValue(jn.get("id").asText(), jn.get("value").asText()))
            .collect(Collectors.toList());
        }

        if (fieldID.equalsIgnoreCase(COMPONENTS_FIELD.getValue())) {
          for (ProjectComponent component : jiraProject.getComponents()) {
            allowed.add(new AllowedValue(component.getId(), component.getName()));
          }
        }
        if (fieldID.equalsIgnoreCase(FIX_VERSIONS_FIELD.getValue())) {
          for (Version version : jiraProject.getVersions()) {
            allowed.add(new AllowedValue(version.getId(), version.getName()));
          }
        }
        if (fieldID.equalsIgnoreCase(AFFECTS_VERSIONS_FIELD.getValue())) {
          for (Version version : jiraProject.getVersions()) {
            allowed.add(new AllowedValue(version.getId(), version.getName()));
          }
        }
        if (fieldID.equalsIgnoreCase(PRIORITY_FIELD.getValue())) {
          allowed = issueField.getValue().getAllowedValues().stream()
              .map(value -> (JsonNode) new ObjectMapper().valueToTree(value))
              .map(jn -> new AllowedValue(jn.get("id").asText(), jn.get("name").asText()))
              .collect(Collectors.toList());
        }
        if (fieldID.equalsIgnoreCase(ISSUE_TYPE_FIELD.getValue())) {
          isRequired = true;
          defValue = Collections.singletonList(issueTypeParam);
        }
        if (fieldID.equalsIgnoreCase(ASSIGNEE_FIELD.getValue())) {
          commandName = "searchUsers";
        }

        PostFormField postForm = new PostFormField(fieldID, fieldName, fieldType, isRequired, defValue, allowed);
        if (StringUtils.isNotEmpty(commandName)) {
          postForm.setCommandName(commandName);
        }
        result.add(postForm);
      });

      return result;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return new ArrayList<>();
    }
  }

}
