package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import it.lutechcdm.thingworxextensionplugin.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxFieldValidationHelper;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceDefinition;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceParameter;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceResult;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AddServiceDialogWrapper extends DialogWrapper {

    final AddServicePanel panel;

    ServiceDefinition createdDefinition = null;

    public AddServiceDialogWrapper(Project project) {
        super(project, false);
        panel = new AddServicePanel(project);
        setTitle("Add Service");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel p = JBUI.Panels.simplePanel();

        addNameValidator();
        addDataShapeValidator();
        addResultValidator();

        //add event listener on fields
        panel.nameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        panel.dataShape.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        panel.outputNameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        p.addToCenter(panel.mainPanel);
        return p;
    }

    private void addResultValidator() {
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.outputNameField.isVisible()) {
                            ThingworxFieldValidationHelper.validatePropertyName(panel.outputNameField.getText(), panel.outputNameField);
                        }
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.outputNameField);
    }

    private void addDataShapeValidator() {

        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.dataShape.isVisible()) {
                            ThingworxFieldValidationHelper.validateRequired(panel.dataShape.getText(), panel.dataShape);
                        }
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.dataShape);

    }

    private void addNameValidator() {
        //Name validation
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        JTextField nameField = panel.nameField;
                        ThingworxFieldValidationHelper.validatePropertyName(nameField.getText(), nameField);
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.nameField);
    }

    private void revalidateUI() {
        ComponentValidator.getInstance(panel.nameField).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.dataShape).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.outputNameField).ifPresent(ComponentValidator::revalidate);
        setOKActionEnabled(true);
    }

    @Override
    protected boolean shouldAddErrorNearButtons() {
        return true;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        ValidationInfo validationInfo = super.doValidate();
        if (validationInfo != null)
            return validationInfo;

        JComponent[] components = new JComponent[]{panel.nameField, panel.dataShape, panel.outputNameField};

        for (JComponent component : components) {
            if (ComponentValidator.getInstance(component).isPresent()) {
                if (ComponentValidator.getInstance(component).get().getValidationInfo() != null) {
                    return ComponentValidator.getInstance(component).get().getValidationInfo();
                }
            }
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        createdDefinition = new ServiceDefinition(panel.nameField.getText(), panel.descriptionField.getText(), panel.categoryField.getText(), panel.allowOverride.isSelected());
        createdDefinition.addAspect("isAsync:" + panel.async.isSelected());

        ThingworxBaseTypes baseTypes = ThingworxBaseTypes.valueOf((String) panel.outputBaseType.getSelectedItem());
        ServiceResult serviceResult = new ServiceResult(panel.outputNameField.getText(), panel.outputDescriptionField.getText(), baseTypes);
        if(baseTypes == ThingworxBaseTypes.INFOTABLE) {
            String dataShape = panel.dataShape.getText();
            if(panel.dataShape.isVisible() && dataShape != null && !dataShape.isEmpty())
                createdDefinition.addAspect("dataShape:" + dataShape);

            String infoTableType = (String) panel.infoTableType.getSelectedItem();
            if(infoTableType != null) {
                switch (infoTableType) {
                    case ThingworxConstants.DATA_TABLE_INFOTABLE:
                        createdDefinition.addAspect("isDataTableEntry:true");
                        break;
                    case ThingworxConstants.STREAM_ENTRY_INFOTABLE:
                        createdDefinition.addAspect("isStreamEntry:true");
                        break;
                    case ThingworxConstants.CONTENT_CRAWLER_INFOTABLE:
                        createdDefinition.addAspect("isContentCrawlerEntry:true");
                        break;
                    default:
                        createdDefinition.addAspect("isEntityDataShape:true");
                }
            }
        }
        createdDefinition.setServiceResult(serviceResult);
        for(ServiceParameter parameter : panel.serviceParameters)
            createdDefinition.addServiceParameter(parameter);

        super.doOKAction();
    }

    public ServiceDefinition getCreatedDefinition() {
        return createdDefinition;
    }
}
