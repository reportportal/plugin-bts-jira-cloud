package com.epam.reportportal.extension.jira.command.atlassian;

import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.internal.json.CimProjectJsonParser;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class CreateIssueMetadataJsonParserEx implements JsonObjectParser<Iterable<CimProject>> {

	private final GenericJsonArrayParser<CimProject> projectsParser = new GenericJsonArrayParser<CimProject>(new CimProjectJsonParser());

	@Override
	public Iterable<CimProject> parse(final JSONObject json) throws JSONException {
		JSONArray projects = json.getJSONArray("projects");
		String newJson = projects.toString().replaceAll("(,\"copy\")|(\"copy\")", "");
		JSONArray jsonArray = new JSONArray(newJson);
		return projectsParser.parse(jsonArray);
	}
}
