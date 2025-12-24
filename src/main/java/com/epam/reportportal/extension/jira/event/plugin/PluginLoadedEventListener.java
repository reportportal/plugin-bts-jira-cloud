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
package com.epam.reportportal.extension.jira.event.plugin;

import com.epam.reportportal.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.extension.jira.info.PluginInfoProvider;
import com.epam.reportportal.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.infrastructure.persistence.entity.integration.IntegrationType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.springframework.context.ApplicationListener;

/**
 * Event listener for plugin loaded events.
 * Updates integration type information when the Jira Cloud plugin is uploaded.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginLoadedEventListener implements ApplicationListener<PluginUploadedEvent> {

  private final String pluginId;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final IntegrationRepository integrationRepository;
  private final PluginInfoProvider pluginInfoProvider;

  public PluginLoadedEventListener(String pluginId,
      IntegrationTypeRepository integrationTypeRepository,
      IntegrationRepository integrationRepository,
      PluginInfoProvider pluginInfoProvider) {
    this.pluginId = pluginId;
    this.integrationTypeRepository = integrationTypeRepository;
    this.integrationRepository = integrationRepository;
    this.pluginInfoProvider = pluginInfoProvider;
  }

  @Override
  public void onApplicationEvent(PluginUploadedEvent event) {
    if (!supports(event)) {
      return;
    }

    String eventPluginId = event.getPluginActivityResource().getName();
    integrationTypeRepository.findByName(eventPluginId).ifPresent(integrationType -> {
      createIntegration(eventPluginId, integrationType);
      integrationTypeRepository.save(pluginInfoProvider.provide(integrationType));
    });
  }

  private boolean supports(PluginUploadedEvent event) {
    return pluginId.equals(event.getPluginActivityResource().getName());
  }

  private void createIntegration(String name, IntegrationType integrationType) {
    List<Integration> integrations = integrationRepository.findAllGlobalByType(integrationType);
    if (integrations.isEmpty()) {
      Integration integration = new Integration();
      integration.setName(name);
      integration.setType(integrationType);
      integration.setCreationDate(Instant.now());
      integration.setEnabled(true);
      integration.setCreator("SYSTEM");
      integration.setParams(new IntegrationParams(new HashMap<>()));
      integrationRepository.save(integration);
    }
  }
}
