package com.epam.reportportal.extension.jira.command.utils;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.net.URI;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CloudJiraClientUtils {

	public static Project getProject(JiraRestClient jiraRestClient, IntegrationParams params) {
		return jiraRestClient.getProjectClient()
				.getProject(CloudJiraProperties.PROJECT.getParam(params)
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
								"Project is not specified."
						)))
				.claim();
	}

	public static JiraRestClient getClient(String uri, String providedUsername, String credentials) {
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(URI.create(uri), providedUsername, credentials);
	}

	public static JiraRestClient getClient(IntegrationParams integrationParams) {
		String providedUsername = CloudJiraProperties.EMAIL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "User email is not specified."));
		String credentials = CloudJiraProperties.API_TOKEN.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Api token is not specified."));
		String url = CloudJiraProperties.URL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Url to the Cloud Jira is not specified."
				));
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(URI.create(url), providedUsername, credentials);
	}

}
