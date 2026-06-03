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

package com.epam.reportportal.extension.jira;

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TicketRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.command.ExtensionCommand;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.jira.command.GetIssueCommand;
import com.epam.reportportal.extension.jira.command.GetIssueFieldsCommand;
import com.epam.reportportal.extension.jira.command.GetIssueTypesCommand;
import com.epam.reportportal.extension.jira.command.PostTicketCommand;
import com.epam.reportportal.extension.jira.command.RetrieveCreationParamsCommand;
import com.epam.reportportal.extension.jira.command.RetrieveUpdateParamsCommand;
import com.epam.reportportal.extension.jira.command.TestConnectionCommand;
import com.epam.reportportal.extension.jira.command.UserSearchCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.JIRATicketDescriptionService;
import com.epam.reportportal.extension.jira.event.plugin.PluginLoadedEventListener;
import com.epam.reportportal.extension.jira.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.jira.utils.MemoizingSupplier;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Extension
public class CloudJiraExtension implements ReportPortalExtensionPoint, DisposableBean {

  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String DOCUMENTATION_LINK = "https://reportportal.io/docs/plugins/AtlassianJiraCloud";
  public static final String BINARY_DATA_PROPERTIES_FILE_ID = "binary-data.properties";

  private static final String PLUGIN_ID = "JIRA Cloud";

  private static final String NAME_FIELD = "name";

  private static final String PLUGIN_NAME = "Jira Cloud";

  private final String resourcesDir;

  private final Supplier<Map<String, ExtensionCommand<?>>> pluginCommandMapping =
      new MemoizingSupplier<>(this::getIntegrationExtensionCommands);
  private final Supplier<Map<String, ExtensionCommand<?>>> commonPluginCommandMapping =
      new MemoizingSupplier<>(this::getCommonExtensionCommands);

  private final Supplier<RequestEntityConverter> requestEntityConverterSupplier;

  private final Supplier<ApplicationListener<PluginUploadedEvent>> pluginLoadedListenerSupplier;

  private final Supplier<CloudJiraClientProvider> cloudJiraClientProviderSupplier;

  private final Supplier<JIRATicketDescriptionService> jiraTicketDescriptionServiceSupplier;

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationRepositoryCustom organizationRepository;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private TestItemRepository testItemRepository;

  @Autowired
  private BasicTextEncryptor textEncryptor;

  @Autowired
  @Qualifier("attachmentDataStoreService")
  private DataStoreService dataStoreService;

  public CloudJiraExtension(Map<String, Object> initParams) {
    resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf)
        .orElse("");

    pluginLoadedListenerSupplier = new MemoizingSupplier<>(() -> new PluginLoadedEventListener(
        PLUGIN_ID, integrationTypeRepository, integrationRepository,
        new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
    ));

    requestEntityConverterSupplier = new MemoizingSupplier<>(() -> new RequestEntityConverter(objectMapper));

    cloudJiraClientProviderSupplier = new MemoizingSupplier<>(() -> new CloudJiraClientProvider(textEncryptor));

    jiraTicketDescriptionServiceSupplier = new MemoizingSupplier<>(
        () -> new JIRATicketDescriptionService(logRepository, testItemRepository));
  }

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(NAME_FIELD, PLUGIN_NAME);
    params.put(COMMON_COMMANDS, new ArrayList<>(commonPluginCommandMapping.get().keySet()));
    return params;
  }

  @Override
  public PluginCommand<?> getIntegrationCommand(String commandName) {
    return null; // pluginCommandMapping.get().get(commandName);
  }

  @Override
  public CommonPluginCommand<?> getCommonCommand(String commandName) {
    return null; // commonPluginCommandMapping.get().get(commandName);
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.BTS;
  }

  @PostConstruct
  public void createIntegration() {
    initListeners();
  }

  private void initListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.addApplicationListener(pluginLoadedListenerSupplier.get());
  }

  @Override
  public void destroy() {
    removeListeners();
  }

  private void removeListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
  }

  @Override
  public Map<String, ExtensionCommand<?>> getCommonExtensionCommands() {
    List<ExtensionCommand<?>> commands = new ArrayList<>();
    commands.add(new RetrieveCreationParamsCommand(textEncryptor, projectRepository, organizationRepository));
    commands.add(new RetrieveUpdateParamsCommand(textEncryptor, projectRepository, organizationRepository));
    commands.add(new GetIssueCommand(ticketRepository, integrationRepository, cloudJiraClientProviderSupplier.get(), projectRepository, organizationRepository));
    return commands.stream()
        .collect(Collectors.toMap(ExtensionCommand::getName, it -> it));
  }


  @Override
  public Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    List<ExtensionCommand<?>> commands = new ArrayList<>();
    commands.add(
        new UserSearchCommand(projectRepository, cloudJiraClientProviderSupplier.get(), organizationRepository));
    commands.add(
        new TestConnectionCommand(cloudJiraClientProviderSupplier.get(), projectRepository, organizationRepository));
    commands.add(
        new GetIssueFieldsCommand(projectRepository, organizationRepository, cloudJiraClientProviderSupplier.get()));
    commands.add(
        new GetIssueTypesCommand(projectRepository, cloudJiraClientProviderSupplier.get(), organizationRepository));
    commands.add(new PostTicketCommand(projectRepository,
        requestEntityConverterSupplier.get(),
        cloudJiraClientProviderSupplier.get(),
        jiraTicketDescriptionServiceSupplier.get(),
        dataStoreService,
        organizationRepository
    ));
    return commands.stream().collect(Collectors.toMap(ExtensionCommand::getName, it -> it));
  }
}
