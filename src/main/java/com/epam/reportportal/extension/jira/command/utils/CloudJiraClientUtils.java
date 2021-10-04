package com.epam.reportportal.extension.jira.command.utils;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CloudJiraClientUtils {

	public static JiraRestClient getClient(String uri, String providedUsername, String credentials) {
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(URI.create(uri), providedUsername, credentials);
	}

}
