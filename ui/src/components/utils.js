const regex = (regexStr) => (value) => RegExp(regexStr).test(value);
const trimValue = (value) => (typeof value === 'string' ? value.trim() : value);
const isEmpty = (value) => {
  const trimmedValue = trimValue(value);
  return trimmedValue === '' || trimmedValue === undefined || trimmedValue === null;
};
const isNotEmpty = (value) => !isEmpty(value);
const composeValidators = (validators) => (value) =>
  validators.every((validator) => validator(value));

const jiraCloudUrl = composeValidators([
  isNotEmpty,
  regex(/^https:\/\/[^?]*\.(atlassian\.(com|net)|jira\.com)$/),
]);

const bindMessageToValidator = (validator, errorMessage) => (value) =>
  !validator(value) ? errorMessage : undefined;

export const btsJiraCloudUrl = bindMessageToValidator(jiraCloudUrl, 'btsUrlHint');
