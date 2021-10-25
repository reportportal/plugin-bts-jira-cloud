package com.epam.reportportal.extension.jira.command;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.epam.reportportal.extension.ProjectManagerCommand;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.reportportal.extension.jira.command.utils.CloudJiraClientUtils.getClient;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraClientUtils.getProject;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueTypesCommand extends ProjectManagerCommand<List<String>> {

	public GetIssueTypesCommand(ProjectRepository projectRepository) {
		super(projectRepository);
	}

	@Override
	public String getName() {
		return "getIssueTypes";
	}

	@Override
	protected List<String> invokeCommand(Integration integration, Map<String, Object> params) {
		try (JiraRestClient client = getClient(integration.getParams())) {
			Project jiraProject = getProject(client, integration.getParams());
			return StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
					.map(IssueType::getName)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Check connection settings.");
		}
	}
}
