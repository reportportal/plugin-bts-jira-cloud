package com.epam.reportportal.extension.jira.command;

import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.google.common.collect.Maps;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class RetrieveUpdateParamsCommand implements NamedPluginCommand<Map<String, Object>> {

	private final BasicTextEncryptor textEncryptor;

	public RetrieveUpdateParamsCommand(BasicTextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String getName() {
		return "retrieveUpdate";
	}

	@Override
	//@param integration is always null because it can be not saved yet
	public Map<String, Object> executeCommand(Integration integration, Map<String, Object> integrationParams) {
		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(integrationParams.size());
		CloudJiraProperties.URL.getParam(integrationParams).ifPresent(url -> resultParams.put(CloudJiraProperties.URL.getName(), url));
		CloudJiraProperties.PROJECT.getParam(integrationParams)
				.ifPresent(url -> resultParams.put(CloudJiraProperties.PROJECT.getName(), url));
		CloudJiraProperties.EMAIL.getParam(integrationParams).ifPresent(url -> resultParams.put(CloudJiraProperties.EMAIL.getName(), url));
		CloudJiraProperties.API_TOKEN.getParam(integrationParams)
				.ifPresent(token -> resultParams.put(CloudJiraProperties.API_TOKEN.getName(), textEncryptor.encrypt(token)));
		Optional.ofNullable(integrationParams.get("defectFormFields"))
				.ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));
		return resultParams;
	}
}
