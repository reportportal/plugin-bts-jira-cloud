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

package com.epam.reportportal.extension.jira.command.utils;

import java.util.Objects;

/**
 * IssueSeverity enumerator<br>
 * Describe default severities from JIRA (a while)
 *
 * @author Andrei_Ramanchuk
 */
public enum IssuePriority {

	//@formatter:off
	BLOCKER(1),
	CRITICAL(2),
	MAJOR(3), 
	MINOR(4), 
	TRIVIAL(5);
	//@formatter:on

	private long priority;

	IssuePriority(long value) {
		this.priority = value;
	}

	public static IssuePriority findByName(String name) {
		for (IssuePriority type : IssuePriority.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}

	public static IssuePriority findByPriority(String priority) {
		for (IssuePriority issuePriority : IssuePriority.values()) {
			if (Objects.equals(issuePriority.getValue(), Long.valueOf(priority))) {
				return issuePriority;
			}
		}
		return null;
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}

	public long getValue() {
		return priority;
	}
}