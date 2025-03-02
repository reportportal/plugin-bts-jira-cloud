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

import com.epam.reportportal.extension.jira.client.JiraRestClient;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CloudJiraClientProvider {

  protected final BasicTextEncryptor textEncryptor;

  private static final String USER_EMAIL_NOT_SPECIFIED = "User email is not specified.";
  private static final String API_TOKEN_NOT_SPECIFIED = "Api token is not specified.";
  private static final String URL_NOT_SPECIFIED = "Url to the Cloud Jira is not specified.";

  public CloudJiraClientProvider(BasicTextEncryptor textEncryptor) {
    this.textEncryptor = textEncryptor;
  }

  public JiraRestClient getApiClient(IntegrationParams integrationParams) {
    CloudJiraDetails details = extractAndDecryptDetails(integrationParams);
    return new JiraRestClient(details.url(), details.username(), details.credentials());
  }


  private CloudJiraDetails extractAndDecryptDetails(IntegrationParams integrationParams) {
    String providedUsername = getTextParamOrThrow(CloudJiraProperties.EMAIL, integrationParams, USER_EMAIL_NOT_SPECIFIED);
    String credentials = textEncryptor.decrypt(getTextParamOrThrow(CloudJiraProperties.API_TOKEN, integrationParams, API_TOKEN_NOT_SPECIFIED));
    String url = getTextParamOrThrow(CloudJiraProperties.URL, integrationParams, URL_NOT_SPECIFIED);
    return new CloudJiraDetails(providedUsername, credentials, url);
  }

  private String getTextParamOrThrow(CloudJiraProperties param, IntegrationParams params,
      String errorMessage) {
    return param.getParam(params).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, errorMessage));
  }


}
