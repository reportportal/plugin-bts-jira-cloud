import { LABELS } from 'components/constans';

export const IntegrationFormFields = (props) => {
  const { initialize, disabled, lineAlign, initialData, updateMetaData, ...extensionProps } = props;
  const {
    lib: { React },
    components: { IntegrationFormField, FieldErrorHint, Input, InputTextArea },
    validators: { requiredField, btsUrl, btsProjectKey, btsIntegrationName, email },
    constants: { SECRET_FIELDS_KEY },
  } = extensionProps;

  React.useEffect(() => {
    initialize(initialData);
    updateMetaData({
      [SECRET_FIELDS_KEY]: ['apiToken'],
    });
  }, []);

  return (
    <>
      <IntegrationFormField
        name="integrationName"
        disabled={disabled}
        label={LABELS.INTEGRATION_NAME}
        required
        maxLength="55"
        validate={btsIntegrationName}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="url"
        disabled={disabled}
        label={LABELS.URL}
        required
        validate={btsUrl}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="project"
        disabled={disabled}
        label={LABELS.PROJECT}
        required
        maxLength="55"
        validate={btsProjectKey}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="email"
        disabled={disabled}
        label={LABELS.EMAIl}
        required
        lineAlign={lineAlign}
        validate={email}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="apiToken"
        label={LABELS.TOKEN}
        required
        disabled={disabled}
        lineAlign={lineAlign}
        validate={requiredField}
      >
        <FieldErrorHint>
          <InputTextArea type="text" mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
    </>
  );
};
