package com.epam.reportportal.extension.jira.command.utils;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.jasypt.util.text.BasicTextEncryptor;

import java.net.URI;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CloudJiraClientProvider {

	private final BasicTextEncryptor textEncryptor;

	public CloudJiraClientProvider(BasicTextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
	}

	public JiraRestClient get(IntegrationParams integrationParams) {
		String providedUsername = CloudJiraProperties.EMAIL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "User email is not specified."));
		String credentials = textEncryptor.decrypt(CloudJiraProperties.API_TOKEN.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Api token is not specified.")));
		String url = CloudJiraProperties.URL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Url to the Cloud Jira is not specified."
				));
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(URI.create(url), providedUsername, credentials);
	}

}
