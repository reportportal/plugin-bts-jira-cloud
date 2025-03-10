/*
 * Copyright 2025 EPAM Systems
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

import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.log.Log;

public class SampleData {

  public static TestItem getTestItem() {
    var testItem = new TestItem();
    TestItemResults testItemResults = new TestItemResults();

    testItem.setItemResults(testItemResults);
    return testItem;
  }

  public static Log getLogsItem() {
    var log = new Log();
    TestItemResults testItemResults = new TestItemResults();
    var attachemt = new Attachment();
    attachemt.setFileName("");
    return log;
  }

  public static final String EPIC = """
            {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 4713343083,
        "fields": [
          {
            "id": "issuetype",
            "value": [
              "Epic"
            ],
            "required": true,
            "fieldName": "Issue Type",
            "fieldType": "issuetype",
            "definedValues": []
          },
          {
            "id": "summary",
            "required": true,
            "fieldName": "Summary",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "Test by Oleg"
            ]
          }
        ],
        "backLinks": {
          "4713343083": "https://reportportal.epam.com/ui/#epm-rpp/launches/all/8636133/4713341992/4713343083/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.issueType%3Dti001%252Cti_qdpzzqt1yulh%252Cti_qhqltb6q9y5y%252Cti_vb94w7l7xlpu%252Cti_skk1lqxyxfsi"
        }
      }
      """;

  public static String STORY = """
              {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 1678,
        "fields": [
          {
            "id": "summary",
            "value": [
              "Test"
            ],
            "required": true,
            "fieldName": "Summary",
            "fieldType": "string",
            "definedValues": []
          },
          {
            "id": "issuetype",
            "value": [
              "Bug"
            ],
            "required": true,
            "fieldName": "Issue Type",
            "fieldType": "issuetype",
            "definedValues": [],
            "disabled": true
          },
          {
            "id": "customfield_10047",
            "required": false,
            "fieldName": "Affected version from Helen",
            "fieldType": "array",
            "definedValues": [],
            "value": [
              "1234"
            ]
          },
          {
            "id": "labels",
            "required": false,
            "fieldName": "Labels",
            "fieldType": "array",
            "definedValues": [],
            "value": [
              " 56 78"
            ]
          }
        ],
         "backLinks":
           {
            "1678":"https://localhost:8080/ui/#superadmin_personal/launches/all/297/1677/1678/1679/log?item2Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.issueType%3Dab001"
           }
      
      }
      """;

  public static String BUG = """
           {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 336474,
        "fields": [
            {
                "id": "summary",
                "required": true,
                "fieldName": "Summary",
                "fieldType": "string",
                "definedValues": [],
                "value": [
                    "Test"
                ]
            },
            {
                "id": "issuetype",
                "value": [
                    "Bug"
                ],
                "required": true,
                "fieldName": "Issue Type",
                "fieldType": "issuetype",
                "definedValues": [],
                "disabled": true
            },
            {
                "id": "parent",
                "required": false,
                "fieldName": "Parent",
                "fieldType": "issuelink",
                "definedValues": [],
                "value": [
                    "EPMRPP-419"
                ]
            },
            {
                "id": "customfield_10045",
                "value": [
                    "Major"
                ],
                "required": false,
                "fieldName": "Severity",
                "fieldType": "option",
                "definedValues": [
                    {
                        "valueId": "None",
                        "valueName": "None"
                    },
                    {
                        "valueId": "10021",
                        "valueName": "Critical"
                    },
                    {
                        "valueId": "10022",
                        "valueName": "Major"
                    },
                    {
                        "valueId": "10023",
                        "valueName": "Minor"
                    },
                    {
                        "valueId": "10024",
                        "valueName": "Trivial"
                    },
                    {
                        "valueId": "10028",
                        "valueName": "New option"
                    },
                    {
                        "valueId": "10033",
                        "valueName": "New option 2"
                    },
                    {
                        "valueId": "10035",
                        "valueName": "New option 3"
                    }
                ]
            },
            {
                "id": "customfield_10046",
                "value": [
                    "Option 2"
                ],
                "required": false,
                "fieldName": "Priority",
                "fieldType": "option",
                "definedValues": [
                    {
                        "valueId": "None",
                        "valueName": "None"
                    },
                    {
                        "valueId": "10026",
                        "valueName": "Option 1"
                    },
                    {
                        "valueId": "10027",
                        "valueName": "Option 2"
                    }
                ]
            },
            {
                "id": "customfield_10047",
                "required": false,
                "fieldName": "Affected version from Helen",
                "fieldType": "array",
                "definedValues": [],
                "value": [
                    "test custom field"
                ]
            },
            {
                "id": "issuelinks",
                "required": false,
                "fieldName": "Linked Issues",
                "fieldType": "array",
                "definedValues": [],
                "value": [
                    "EPMRPP-449"
                ]
            },
            {
                "id": "assignee",
                "value": [
                    "557058:8eac799c-d542-4ae7-a0d5-45f3bcd9a97a"
                ],
                "required": false,
                "fieldName": "Assignee",
                "fieldType": "user",
                "commandName": "searchUsers",
                "definedValues": []
            }
        ],
        "backLinks": {
            "336474": "http://dev.epmrpp.reportportal.io/ui/?#helen/launches/all/88800/336472/336473/336474/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.type%3DSTEP%26filter.in.status%3DFAILED%252CINTERRUPTED"
        }
    }


      """;

}
