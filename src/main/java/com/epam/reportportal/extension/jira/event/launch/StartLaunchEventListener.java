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
package com.epam.reportportal.extension.jira.event.launch;

import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import org.springframework.context.ApplicationListener;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StartLaunchEventListener implements ApplicationListener<StartLaunchEvent> {

	private final LaunchRepository launchRepository;

	public StartLaunchEventListener(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Override
	public void onApplicationEvent(StartLaunchEvent event) {

	}
}
