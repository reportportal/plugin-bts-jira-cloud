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

package com.epam.reportportal.extension.jira.command.utils;

import static com.epam.reportportal.extension.jira.command.utils.IssueField.ASSIGNEE_FIELD;

import com.epam.reportportal.extension.jira.api.model.EntityProperty;
import com.epam.reportportal.extension.jira.api.model.IssueBean;
import com.epam.reportportal.extension.jira.api.model.IssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.jira.api.model.IssueTypeIssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.IssueUpdateDetails;
import com.epam.reportportal.extension.jira.api.model.Project;
import com.epam.reportportal.extension.jira.api.model.ProjectIssueCreateMetadata;
import com.epam.reportportal.extension.jira.api.model.User;
import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide toolscom.epam.reportportal.extension.bugtracking.jira for working with JIRA tickets(conversion).
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public class JIRATicketUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketUtils.class);

  // Field format from UI calendar control
  public static final String JIRA_FORMAT = "yyyy-MM-dd";

  public static final String PARENT_FIELD_ID = "parent";

  private JIRATicketUtils() {
  }

  public static Ticket toTicket(IssueBean jiraIssue, String jiraUrl) {
    Ticket ticket = new Ticket();
    JsonNode jn = new ObjectMapper().valueToTree(jiraIssue);

    ticket.setId(jiraIssue.getKey());
    ticket.setSummary(jn.get("fields").get("summary").asText());
    ticket.setStatus(jn.get("fields").get("status").get("statusCategory").get("name").asText());
    ticket.setTicketUrl(stripEnd(jiraUrl, "/") + "/browse/" + jn.get("key").asText());
    return ticket;
  }

  public static IssueUpdateDetails toIssueInput(JiraRestClient client, Project jiraProject, IssueTypeDetails issueType, PostTicketRQ ticketRQ,
      JIRATicketDescriptionService descriptionService) {
    String userDefinedDescription = "";
    IssueUpdateDetails issueUpdateDetails = new IssueUpdateDetails();

    IssueCreateMetadata issueCreateMetadata = client.issuesApi().getCreateIssueMeta(
        Collections.singletonList(jiraProject.getId()),
        null,
        Collections.singletonList(issueType.getId()),
        null,
        "projects.issuetypes.fields"
    );
    issueUpdateDetails.putFieldsItem("project", Map.entry("id", jiraProject.getId()));
    issueUpdateDetails.putFieldsItem("issuetype", Map.entry("id", issueType.getId()));

    ProjectIssueCreateMetadata project = issueCreateMetadata.getProjects().get(0);
    BusinessRule.expect(project, Predicates.notNull())
        .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, String.format("Project %s not found", jiraProject.getKey()));

    List<IssueTypeIssueCreateMetadata> cimIssueType = project.getIssuetypes();

    for (PostFormField one : ticketRQ.getFields()) {

      if (one.getIsRequired() && CollectionUtils.isEmpty(one.getValue())) {
        BusinessRule.fail()
            .withError(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                Suppliers.formattedSupplier("Required parameter '{}' is empty", one.getFieldName()));
      }

      if (!checkField(one)) {
        continue;
      }

      // Skip issuetype and project fields cause got them in
      // issueInputBuilder already
      if (one.getId().equalsIgnoreCase(IssueField.ISSUE_TYPE_FIELD.value) || one.getId().equalsIgnoreCase(IssueField.PROJECT_FIELD.value)) {
        continue;
      }

      if (one.getId().equalsIgnoreCase(IssueField.DESCRIPTION_FIELD.value)) {
        userDefinedDescription = one.getValue().get(0);
      }
      if (one.getId().equalsIgnoreCase(IssueField.SUMMARY_FIELD.value)) {
        issueUpdateDetails.putFieldsItem(IssueField.SUMMARY_FIELD.value, one.getValue().get(0));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.PRIORITY_FIELD.value)) {
        if (IssuePriority.findByName(one.getValue().get(0)) != null) {
          issueUpdateDetails.putFieldsItem(IssueField.PRIORITY_FIELD.value, one.getValue().get(0));
        }
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.COMPONENTS_FIELD.value)) {
        issueUpdateDetails.putFieldsItem(IssueField.COMPONENTS_FIELD.value, one.getValue());
        continue;
      }
      if (one.getId().equalsIgnoreCase(ASSIGNEE_FIELD.getValue())) {
        issueUpdateDetails.putFieldsItem(ASSIGNEE_FIELD.getValue(), Map.entry("id", one.getValue().get(0)));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.REPORTER_FIELD.value)) {
        issueUpdateDetails.putFieldsItem(IssueField.REPORTER_FIELD.value, Map.entry("id", one.getValue().get(0)));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.AFFECTS_VERSIONS_FIELD.value)) {
        var versions = one.getValue().stream().map(version -> Map.entry("id", version)).toList();
        issueUpdateDetails.putFieldsItem(IssueField.AFFECTS_VERSIONS_FIELD.value, versions);
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.FIX_VERSIONS_FIELD.value)) {
        var versions = one.getValue().stream().map(version -> Map.entry("id", version)).toList();
        issueUpdateDetails.putFieldsItem(IssueField.FIX_VERSIONS_FIELD.value, versions);
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.LINKS_FIELD.value)) {
        continue;
      }
      if (one.getId().equalsIgnoreCase(PARENT_FIELD_ID) && !one.getValue().isEmpty()) {
        issueUpdateDetails.putFieldsItem(PARENT_FIELD_ID, Map.entry("key", one.getValue()));
        continue;
      }

      var cimFieldInfo = cimIssueType.getFirst().getFields().get(one.getId());
      // Arrays and fields with 'allowedValues' handler
      if (cimFieldInfo != null) {
        try {
          //  List<ComplexIssueInputFieldValue> arrayOfValues = Lists.newArrayList();
          for (Object o : cimFieldInfo.getAllowedValues()) {
/*            if (o instanceof CustomFieldOption) {
              CustomFieldOption cfo = (CustomFieldOption) o;
              if (one.getValue().contains(cfo.getValue())) {
                arrayOfValues.add(
                    ComplexIssueInputFieldValue.with("id", String.valueOf(cfo.getId())));
              }
            }*/
          }
          if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
            //     issueInputBuilder.setFieldValue(one.getId(), arrayOfValues);
          } else {
            //       issueInputBuilder.setFieldValue(one.getId(), arrayOfValues.get(0));
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          //      issueInputBuilder.setFieldValue(one.getId(), "ReportPortal autofield");
        }
      } else {
        if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
          if (one.getId().equalsIgnoreCase(IssueField.LABELS_FIELD.value)) {
            //          issueInputBuilder.setFieldValue(one.getId(), processLabels(one.getValue().get(0)));
          } else {
            //           issueInputBuilder.setFieldValue(one.getId(), one.getValue());
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.NUMBER.name)) {
          //     issueInputBuilder.setFieldValue(one.getId(), Long.valueOf(one.getValue().get(0)));
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.USER.name)) {
          if (!one.getValue().get(0).equals("")) {
            // TODO create user cache (like for projects) for JIRA
            // 'user' type fields
            User jiraUser = client.usersApi().getUser(null, one.getValue().get(0), null, null);
            // FIXME change validator as common validate method for
            // fields
            BusinessRule.expect(jiraUser, Predicates.notNull())
                .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
                    "Value for '{}' field with 'user' type wasn't found in JIRA",
                    one.getValue().get(0)
                ));
            issueUpdateDetails.putFieldsItem(ASSIGNEE_FIELD.getValue(), Map.entry("id", jiraUser.getAccountId()));
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.DATE.name)) {
          try {
            SimpleDateFormat format = new SimpleDateFormat(JIRA_FORMAT);
            Date fieldValue = format.parse(one.getValue().get(0));
            issueUpdateDetails.putFieldsItem(one.getId(), fieldValue.toInstant());
          } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
          }
        } else {
          issueUpdateDetails.putFieldsItem(one.getId(), one.getValue().get(0));
        }
      }
    }
    issueUpdateDetails.getProperties()
        .add(new EntityProperty("description", userDefinedDescription.concat("\n").concat(descriptionService.getDescription(ticketRQ))));
    return issueUpdateDetails;
  }

  /**
   * Processing labels for JIRA through spaces split
   *
   * @param values
   * @return
   */
  private static List<String> processLabels(String values) {
    return Stream.of(values.split(" ")).collect(Collectors.toList());
  }

  /**
   * Just JIRA field types enumerator
   *
   * @author Andrei_Ramanchuk
   */
  @Getter
  public enum IssueFieldType {
    //@formatter:off
		ARRAY("array"), 
		DATE("date"), 
		NUMBER("number"), 
		USER("user"),
		OPTION("option"),
		STRING("string");
		//@formatter:on

    private final String name;

    IssueFieldType(String value) {
      this.name = value;
    }

  }

  private static boolean checkField(PostFormField field) {
    return (CollectionUtils.isNotEmpty(field.getValue()) && (!"".equals(field.getValue().get(0))));
  }

  static String stripEnd(String str, String suffix) {
    String trimmed = str;
    if (str.endsWith(suffix)) {
      trimmed = str.substring(0, str.length() - suffix.length());
    }
    return trimmed;
  }
}
