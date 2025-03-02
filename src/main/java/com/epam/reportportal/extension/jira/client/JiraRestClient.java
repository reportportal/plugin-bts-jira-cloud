package com.epam.reportportal.extension.jira.client;


import com.epam.reportportal.extension.jira.api.IssueAttachmentsApi;
import com.epam.reportportal.extension.jira.api.IssueLinksApi;
import com.epam.reportportal.extension.jira.api.IssueSearchApi;
import com.epam.reportportal.extension.jira.api.IssuesApi;
import com.epam.reportportal.extension.jira.api.ProjectsApi;
import com.epam.reportportal.extension.jira.api.UserSearchApi;
import com.epam.reportportal.extension.jira.api.UsersApi;
import com.epam.reportportal.extension.jira.api.client.ApiClient;
import lombok.Getter;

@Getter
public class JiraRestClient {

  private final ApiClient apiClient;

  public JiraRestClient(String url, String username, String apikey) {

    this.apiClient = new ApiClient();
    this.apiClient.setBasePath(url);
    this.apiClient.setUsername(username);
    this.apiClient.setPassword(apikey);
    this.apiClient.setDebugging(Boolean.parseBoolean(System.getenv().getOrDefault("DEBUGGER_ENABLED", "false")));
  }

  public ProjectsApi projectsApi() {
    return new ProjectsApi(apiClient);
  }

  public UsersApi usersApi() {
    return new UsersApi(apiClient);
  }

  public UserSearchApi userSearchApi() {
    return new UserSearchApi(apiClient);
  }

  public IssuesApi issuesApi() {
    return new IssuesApi(apiClient);
  }

  public IssueSearchApi issueSearchApi() {
    return new IssueSearchApi(apiClient);
  }

  public IssueLinksApi issueLinksApi() {
    return new IssueLinksApi(apiClient);
  }

  public IssueAttachmentsApi issueAttachmentsApi() {
    return new IssueAttachmentsApi(apiClient);
  }

}
