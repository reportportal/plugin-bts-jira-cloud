package com.epam.reportportal.extension.jira.command;

import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class RetrieveValidParams implements NamedPluginCommand<Map<String, Object>> {

	private final BasicTextEncryptor textEncryptor;

	public RetrieveValidParams(BasicTextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String getName() {
		return "retrieveValid";
	}

	@Override
	//@param integration is always null because it can be not saved yet
	public Map<String, Object> executeCommand(Integration integration, Map<String, Object> params) {
		BusinessRule.expect(params, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		expect(CloudJiraProperties.URL.getParam(params), Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Url is not specified."
		);
		expect(CloudJiraProperties.PROJECT.getParam(params), Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"BTS project is not specified."
		);

		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(CloudJiraProperties.values().length);

		resultParams.put(CloudJiraProperties.EMAIL.getName(),
				CloudJiraProperties.EMAIL.getParam(params)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Email value cannot be NULL"))
		);

		String encryptedPassword = textEncryptor.encrypt(CloudJiraProperties.API_TOKEN.getParam(params)
				.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "API token value cannot be NULL")));

		resultParams.put(CloudJiraProperties.API_TOKEN.getName(), encryptedPassword);

		CloudJiraProperties.PROJECT.getParam(params)
				.ifPresent(btsProject -> resultParams.put(CloudJiraProperties.PROJECT.getName(), btsProject));
		CloudJiraProperties.URL.getParam(params).ifPresent(url -> resultParams.put(CloudJiraProperties.URL.getName(), url));

		Optional.ofNullable(params.get("defectFormFields"))
				.ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));

		return resultParams;
	}
}
