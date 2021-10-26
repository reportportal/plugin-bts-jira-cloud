package com.epam.reportportal.extension.jira.command.connection;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestConnectionCommand implements NamedPluginCommand<Boolean> {

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
		IntegrationParams integrationParams = ofNullable(integration.getParams()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration params are not specified."
		));
		String project = CloudJiraProperties.PROJECT.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project key is not specified."));

		try (JiraRestClient restClient = cloudJiraClientProvider.get(integrationParams)) {
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
