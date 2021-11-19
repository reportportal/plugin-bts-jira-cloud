import {
  IntegrationFormFields,
  IntegrationSettings,
  PostIssueFields,
  postIssueAction,
} from 'components';

window.RP.registerPlugin({
  name: 'JIRA Cloud',
  extensions: [
    {
      name: 'integrationFormFields',
      title: 'JIRA Cloud plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: IntegrationFormFields,
    },
    {
      name: 'integrationSettings',
      title: 'JIRA Cloud plugin settings',
      type: 'uiExtension:integrationSettings',
      component: IntegrationSettings,
    },
    {
      type: 'uiExtension:postIssueForm',
      component: PostIssueFields,
      action: postIssueAction,
    },
  ],
});
