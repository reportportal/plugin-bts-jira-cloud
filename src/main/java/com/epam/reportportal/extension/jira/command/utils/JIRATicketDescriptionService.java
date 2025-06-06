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

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide functionality for building jira's ticket description
 *
 * @author Aliaksei_Makayed
 * @author Dzmitry_Kavalets
 */
public class JIRATicketDescriptionService {

  public static final String JIRA_MARKUP_LINE_BREAK = "\\\\ ";
  public static final String BACK_LINK_HEADER = "h3.*Back link to Report Portal:*";
  public static final String BACK_LINK_PATTERN = "[Link to defect|%s]%n";
  public static final String COMMENTS_HEADER = "h3.*Test Item comments:*";
  public static final String CODE = "{code}";
  private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketDescriptionService.class);
  private static final String IMAGE_CONTENT = "image";
  private static final String IMAGE_HEIGHT_TEMPLATE = "|height=366!";

  private final LogRepository logRepository;
  private final TestItemRepository itemRepository;
  private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private final MimeTypes mimeRepository = TikaConfig.getDefaultConfig().getMimeRepository();

  public JIRATicketDescriptionService(LogRepository logRepository, TestItemRepository itemRepository) {
    this.logRepository = logRepository;
    this.itemRepository = itemRepository;
  }

  /**
   * Generate ticket description using logs of specified test item.
   *
   * @param ticketRQ
   * @return
   */
  public String getDescription(PostTicketRQ ticketRQ) {
    if (MapUtils.isEmpty(ticketRQ.getBackLinks())) {
      return "";
    }
    StringBuilder descriptionBuilder = new StringBuilder();

    TestItem item = itemRepository.findById(ticketRQ.getTestItemId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, ticketRQ.getTestItemId()));

    ticketRQ.getBackLinks().keySet()
        .forEach(backLinkId -> updateDescriptionBuilder(descriptionBuilder, ticketRQ, backLinkId, item));

    return descriptionBuilder.toString();
  }

  private void updateDescriptionBuilder(StringBuilder descriptionBuilder, PostTicketRQ ticketRQ,
      Long backLinkId, TestItem item) {
    if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(backLinkId))) {
      descriptionBuilder.append(BACK_LINK_HEADER).append("\n").append(" - ")
          .append(String.format(BACK_LINK_PATTERN, ticketRQ.getBackLinks().get(backLinkId)))
          .append("\n");
    }
    // For single test-item only
    // TODO add multiple test-items backlinks
    if (ticketRQ.getIsIncludeComments()) {
      if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(backLinkId))) {
        // If test-item contains any comments, then add it for JIRA
        // comments section
        ofNullable(item.getItemResults())
            .flatMap(result -> ofNullable(result.getIssue()))
            .ifPresent(issue -> {
              if (StringUtils.isNotBlank(issue.getIssueDescription())) {
                descriptionBuilder.append(COMMENTS_HEADER).append("\n")
                    .append(issue.getIssueDescription()).append("\n");
              }
            });
      }
    }
    updateWithLogsInfo(descriptionBuilder, backLinkId, ticketRQ);
  }

  private void updateWithLogsInfo(StringBuilder descriptionBuilder, Long backLinkId,
      PostTicketRQ ticketRQ) {
    itemRepository.findById(backLinkId).ifPresent(item -> ofNullable(item.getLaunchId())
        .ifPresent(launchId -> {
          List<Log> logs = logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(launchId,
              Collections.singletonList(item.getItemId()), ticketRQ.getNumberOfLogs());
          if (CollectionUtils.isNotEmpty(logs) && (ticketRQ.getIsIncludeLogs() || ticketRQ.getIsIncludeScreenshots())) {
            descriptionBuilder.append("h3.*Test execution log:*\n")
                .append("{panel:title=Test execution log|borderStyle=solid|borderColor=#ccc|titleColor=#34302D|titleBGColor=#6DB33F}");
            logs.forEach(log -> updateWithLog(descriptionBuilder, log, ticketRQ.getIsIncludeLogs(), ticketRQ.getIsIncludeScreenshots()));
            descriptionBuilder.append("{panel}\n");
          }
        }));
  }

  private void updateWithLog(StringBuilder descriptionBuilder, Log log, boolean includeLog,
      boolean includeScreenshot) {
    if (includeLog) {
      descriptionBuilder.append(CODE).append(getFormattedMessage(log)).append(CODE);
    }

    if (includeScreenshot) {
      ofNullable(log.getAttachment()).ifPresent(attachment -> addAttachment(descriptionBuilder, attachment));
    }
  }

  private String getFormattedMessage(Log log) {
    StringBuilder messageBuilder = new StringBuilder();
    ofNullable(log.getLogTime()).ifPresent(logTime -> messageBuilder.append(" Time: ")
        .append(dateFormat.format(TO_DATE.apply(logTime.atOffset(ZoneOffset.UTC).toLocalDateTime()))).append(", "));
    ofNullable(log.getLogLevel())
        .ifPresent(logLevel -> messageBuilder.append("Level: ").append(logLevel).append(", "));
    messageBuilder.append("Log: ").append(log.getLogMessage()).append("\n");
    return messageBuilder.toString();
  }

  private void addAttachment(StringBuilder descriptionBuilder, Attachment attachment) {
    if (StringUtils.isNotBlank(attachment.getContentType()) && StringUtils.isNotBlank(attachment.getFileId())) {
      try {
        MimeType mimeType = mimeRepository.forName(attachment.getContentType());
        if (attachment.getContentType().contains(IMAGE_CONTENT)) {
          descriptionBuilder.append("!").append(attachment.getFileId())
              .append(mimeType.getExtension()).append(IMAGE_HEIGHT_TEMPLATE);
        } else {
          descriptionBuilder.append("[^").append(attachment.getFileId())
              .append(mimeType.getExtension()).append("]");
        }
        descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
      } catch (MimeTypeException e) {
        descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
        LOGGER.error("JIRATicketDescriptionService error: {}", e.getMessage(), e);
      }

    }
  }
}
