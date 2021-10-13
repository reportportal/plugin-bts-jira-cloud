import { LABELS } from 'components/constans';

export const IntegrationSettings = (props) => {
  const { data, goToPreviousPage, onUpdate, isGlobal, ...extensionProps } = props;
  const {
    lib: { React, useDispatch },
    actions: { showModalAction, hideModalAction },
    components: { IntegrationSettings: IntegrationSettingsContainer, BtsAuthFieldsInfo },
  } = extensionProps;

  const dispatch = useDispatch();

  const fieldsConfig = [
    {
      value: data.integrationParameters.url,
      message: LABELS.URL,
    },
    {
      value: data.integrationParameters.project,
      message: LABELS.PROJECT,
    },
    {
      value: data.integrationParameters.email,
      message: LABELS.AUTH_BY,
    },
  ];

  const getConfirmationFunc = (testConnection) => (integrationData, integrationMetaData) => {
    onUpdate(
      integrationData,
      () => {
        dispatch(hideModalAction());
        testConnection();
      },
      integrationMetaData,
    );
  };

  const editAuthorizationClickHandler = (testConnection) => {
    const {
      data: { name, integrationParameters, integrationType },
    } = props;

    dispatch(
      showModalAction({
        id: 'addIntegrationModal',
        data: {
          onConfirm: getConfirmationFunc(testConnection),
          instanceType: integrationType.name,
          customProps: {
            initialData: {
              ...integrationParameters,
              integrationName: name,
            },
            editAuthMode: true,
          },
        },
      }),
    );
  };

  const getEditAuthConfig = () => ({
    content: <BtsAuthFieldsInfo fieldsConfig={fieldsConfig} />,
    onClick: editAuthorizationClickHandler,
  });

  return (
    <IntegrationSettingsContainer
      data={data}
      goToPreviousPage={goToPreviousPage}
      onUpdate={() => {}}
      editAuthConfig={getEditAuthConfig()}
      isGlobal={isGlobal}
      formFieldsComponent={() => <div>TBD</div>}
      formKey="BTS_FIELDS_FORM"
      isEmptyConfiguration={
        !data.integrationParameters.defectFormFields ||
        !data.integrationParameters.defectFormFields.length
      }
    />
  );
};
