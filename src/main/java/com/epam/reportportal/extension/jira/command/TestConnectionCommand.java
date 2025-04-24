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

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.extension.jira.utils.IntegrationValidator;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestConnectionCommand implements PluginCommand<Boolean> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionCommand.class);

  private final CloudJiraClientProvider cloudJiraClientProvider;

  public TestConnectionCommand(CloudJiraClientProvider cloudJiraClientProvider) {
    this.cloudJiraClientProvider = cloudJiraClientProvider;
  }

  @Override
  public String getName() {
    return "testConnection";
  }

  @Override
  public Boolean executeCommand(Integration integration, Map<String, Object> params) {
    IntegrationParams integrationParams = ofNullable(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Integration params are not specified."));
    String projectKey = CloudJiraProperties.PROJECT.getParam(integrationParams)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project key is not specified."));
    IntegrationValidator.validateThirdPartyUrl(integration);

    try {
      var jn = cloudJiraClientProvider.getApiClient(integrationParams).projectsApi().getProject(projectKey, null, null);
      return jn != null;
    } catch (Exception e) {
      LOGGER.error("Unable to connect to Cloud Jira: ", e);
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to connect to Cloud Jira. Message: %s", e.getMessage()), e);
    }
  }
}
