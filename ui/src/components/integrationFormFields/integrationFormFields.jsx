export const IntegrationFormFields = (props) => {
  const { initialize, disabled, lineAlign, initialData, updateMetaData, ...extensionProps } = props;
  const {
    lib: { React },
    components: { IntegrationFormField, FieldErrorHint, Input, InputTextArea },
    validators: { requiredField, btsUrl, btsProject, btsIntegrationName },
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
        label="Integration Name"
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
        label="Link to BTS"
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
        label="Project name in BTS"
        required
        maxLength="80"
        validate={btsProject}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="email"
        disabled={disabled}
        label="Login or e-mail"
        required
        lineAlign={lineAlign}
        validate={requiredField}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="apiToken"
        label="API Token"
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
