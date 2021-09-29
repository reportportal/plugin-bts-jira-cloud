/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.jira.command.role;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public abstract class AdminCommand<T> extends AbstractRoleBasedCommand<T> {

	@Override
	public void validateRole(Map<String, Object> params) {
		ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		BusinessRule.expect(UserRole.ADMINISTRATOR.equals(user.getUserRole()), Predicate.isEqual(true))
				.verify(ErrorType.ACCESS_DENIED, "Only user with role 'ADMINISTRATOR' is allowed to execute command.");
	}

}
