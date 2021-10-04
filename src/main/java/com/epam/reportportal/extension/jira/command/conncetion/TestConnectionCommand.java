package com.epam.reportportal.extension.jira.command.conncetion;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Map;

import static com.epam.reportportal.extension.jira.command.utils.CloudJiraClientUtils.getClient;
import static java.util.Optional.ofNullable;
import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestConnectionCommand implements PluginCommand<Boolean> {

	@Override
	public Boolean executeCommand(Integration integration, Map<String, Object> params) {
		IntegrationParams integrationParams = ofNullable(integration.getParams()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration params are not specified."
		));

		String url = CloudJiraProperties.URL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Url to the Cloud Jira is not specified."
				));
		String username = CloudJiraProperties.EMAIL.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "User email is not specified."));
		String apiToken = CloudJiraProperties.API_TOKEN.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Api token is not specified."));
		String project = CloudJiraProperties.PROJECT.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));

		try (JiraRestClient restClient = getClient(url, username, apiToken)) {
			return restClient.getProjectClient().getProject(project).claim() != null;
		} catch (Exception e) {
			LOGGER.error("Unable to connect to Cloud Jira: " + e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					String.format("Unable to connect to Cloud Jira. Message: %s", e.getMessage()),
					e
			);
		}
	}
}
