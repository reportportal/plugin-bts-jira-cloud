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

  public static String BUG = """
                {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 344491,
        "fields": [
          {
            "id": "summary",
            "required": true,
            "fieldName": "Summary",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "New summary"
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
            "id": "description",
            "required": false,
            "fieldName": "Description",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "New description"
            ]
          },
          {
            "id": "customfield_10031",
            "required": false,
            "fieldName": "Custom field",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
            ]
          },
          {
            "id": "customfield_10032",
            "required": false,
            "fieldName": "New field",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
            ]
          },
          {
            "id": "customfield_10021",
            "required": false,
            "fieldName": "Flagged",
            "fieldType": "array",
            "definedValues": [
              {
                "valueId": "10019",
                "valueName": "Impediment"
              }
            ],
            "value": []
          },
          {
            "id": "customfield_10043",
            "required": false,
            "fieldName": "Design",
            "fieldType": "array",
            "definedValues": [],
            "value": [
              "new design"
            ]
          },
          {
            "id": "customfield_10000",
            "required": false,
            "fieldName": "Development",
            "fieldType": "any",
            "definedValues": [],
            "value": [
              "hello development team"
            ]
          },
          {
            "id": "customfield_10044",
            "required": false,
            "fieldName": "Vulnerability",
            "fieldType": "any",
            "definedValues": [],
            "value": [
              "Critical vulnerability "
            ]
          },
          {
            "id": "customfield_10045",
            "value": [
              "Critical"
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
              "25.1 25.2"
            ]
          },
          {
            "id": "labels",
            "required": false,
            "fieldName": "Labels",
            "fieldType": "array",
            "definedValues": [],
            "value": [
              "new label"
            ]
          },
          {
            "id": "customfield_10016",
            "required": false,
            "fieldName": "Story point estimate",
            "fieldType": "number",
            "definedValues": [],
            "value": [
              "8"
            ]
          },
          {
            "id": "customfield_10019",
            "required": false,
            "fieldName": "Rank",
            "fieldType": "any",
            "definedValues": [],
            "value": []
          },
          {
            "id": "issuelinks",
            "required": false,
            "fieldName": "Linked Issues",
            "fieldType": "array",
            "definedValues": [],
            "value": [
              "EPMRPP-500 EPMRPP-510 EPMRPP-511"
            ]
          },
          {
            "id": "assignee",
            "required": false,
            "fieldName": "Assignee",
            "fieldType": "user",
            "commandName": "searchUsers",
            "definedValues": [],
            "value": [
              "5e4430904d2a000c9113b8c3"
            ]
          }
        ],
        "backLinks": {
          "344491": "http://dev.epmrpp.reportportal.io/ui/#helen/launches/all/89493/344489/344490/344491/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.type%3DSTEP%26filter.in.status%3DFAILED%252CINTERRUPTED"
        }
      }
      """;

}
