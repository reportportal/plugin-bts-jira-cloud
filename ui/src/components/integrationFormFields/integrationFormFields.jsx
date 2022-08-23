import { LABELS } from 'components/constans';

export const IntegrationFormFields = (props) => {
  const { initialize, disabled, initialData, updateMetaData, ...extensionProps } = props;
  const {
    lib: { React },
    components: { FieldErrorHint, FieldElement, FieldText, FieldTextFlex },
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
      <FieldElement
        name="integrationName"
        label={LABELS.INTEGRATION_NAME}
        validate={btsIntegrationName}
        disabled={disabled}
      >
        <FieldErrorHint provideHint={false}>
          <FieldText
            maxLength={55}
            defaultWidth={false}
            isRequired
            placeholder={LABELS.INTEGRATION_NAME}
          />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name="url" label={LABELS.URL} validate={btsUrl} disabled={disabled}>
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} isRequired placeholder={LABELS.URL} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name="project"
        label={LABELS.PROJECT}
        validate={btsProjectKey}
        disabled={disabled}
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={55} defaultWidth={false} isRequired placeholder={LABELS.PROJECT} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name="email" label={LABELS.EMAIl} validate={email} disabled={disabled}>
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} isRequired placeholder={LABELS.EMAIl} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name="apiToken"
        label={LABELS.TOKEN}
        disabled={disabled}
        validate={requiredField}
      >
        <FieldErrorHint provideHint={false}>
          <FieldTextFlex placeholder={LABELS.TOKEN} isRequired />
        </FieldErrorHint>
      </FieldElement>
    </>
  );
};
