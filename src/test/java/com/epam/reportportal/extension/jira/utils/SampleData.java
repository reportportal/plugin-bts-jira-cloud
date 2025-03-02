package com.epam.reportportal.extension.jira.utils;

public class SampleData {

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

}
