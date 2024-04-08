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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import java.net.URI;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CloudJiraClientProvider {

  protected final BasicTextEncryptor textEncryptor;

  public CloudJiraClientProvider(BasicTextEncryptor textEncryptor) {
    this.textEncryptor = textEncryptor;
  }

  public JiraRestClient get(IntegrationParams integrationParams) {
    String providedUsername = CloudJiraProperties.EMAIL.getParam(integrationParams).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "User email is not specified."
        ));
    String credentials = textEncryptor.decrypt(
        CloudJiraProperties.API_TOKEN.getParam(integrationParams).orElseThrow(
            () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                "Api token is not specified."
            )));
    String url = CloudJiraProperties.URL.getParam(integrationParams).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Url to the Cloud Jira is not specified."
        ));
    return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
        URI.create(url), providedUsername, credentials);
  }

}
