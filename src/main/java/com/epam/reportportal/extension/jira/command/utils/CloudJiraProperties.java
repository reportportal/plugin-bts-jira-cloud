package com.epam.reportportal.extension.jira.command.utils;

import com.epam.ta.reportportal.entity.integration.IntegrationParams;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public enum CloudJiraProperties {

	EMAIL("email"),
	PROJECT("project"),
	API_TOKEN("apiToken"),
	URL("url");

	private final String name;

	CloudJiraProperties(String name) {
		this.name = name;
	}

	public Optional<String> getParam(IntegrationParams params) {
		return Optional.ofNullable(params.getParams().get(this.name)).map(o -> (String) o);
	}

	public void setParam(IntegrationParams params, String value) {
		if (null == params.getParams()) {
			params.setParams(new HashMap<>());
		}
		params.getParams().put(this.name, value);
	}
}
