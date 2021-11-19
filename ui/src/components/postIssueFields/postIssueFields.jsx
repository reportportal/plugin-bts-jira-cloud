import { POST_ISSUE } from 'constants/pluginCommands';

export const PostIssueFields = () => <div />;

export const postIssueAction = (
  extensionProps,
  formData,
  integrationId,
  activeProject,
  projectInfo,
) => {
  const {
    utils: { fetch, URLS },
  } = extensionProps;

  return fetch(URLS.projectIntegrationByIdCommand(activeProject, integrationId, POST_ISSUE), {
    method: 'PUT',
    data: {
      projectId: String(projectInfo.projectId),
      entity: formData,
    },
  });
};
