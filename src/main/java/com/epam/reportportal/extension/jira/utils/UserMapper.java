/*
 * Copyright 2024 EPAM Systems
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
package com.epam.reportportal.extension.jira.utils;

import com.atlassian.jira.rest.client.api.domain.User;
import com.epam.reportportal.extension.jira.dto.UserDto;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class UserMapper {

  public static List<UserDto> toUserDtoList(Iterable<User> users) {
    return StreamSupport.stream(users.spliterator(), false)
        .map(user -> {
          UserDto dto = new UserDto();
          dto.setId(user.getAccountId());
          dto.setName(user.getDisplayName());
          return dto;
        })
        .collect(Collectors.toList());
  }
}
