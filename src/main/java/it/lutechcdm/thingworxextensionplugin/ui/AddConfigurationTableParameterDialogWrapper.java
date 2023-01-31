package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableFieldDefinition;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import it.lutechcdm.thingworxextensionplugin.validation.ThingworxFieldValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AddConfigurationTableParameterDialogWrapper extends DialogWrapper {

    final AddConfigurationTableParameterPanel panel;

    ConfigurationTableFieldDefinition createdDefinition = null;

    public AddConfigurationTableParameterDialogWrapper(Project project, int starOrdinal, boolean isPrimaryKeyVisible) {
        super(project, true);
        panel = new AddConfigurationTableParameterPanel(starOrdinal, isPrimaryKeyVisible);
        setTitle("Add Service Parameter");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel p = JBUI.Panels.simplePanel();

        addNameValidator();
        addDataShapeValidator();
        addNumericFiledValidator();
        addOrdinalValidator();

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

        panel.ordinalField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        panel.baseTypeField.addItemListener(event -> revalidateUI());

        p.addToCenter(panel.mainPanel);
        return p;
    }

    private void addNumericFiledValidator() {
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.minValue.isVisible()) {
                            ThingworxFieldValidationHelper.validateNumericValue(panel.minValue.getText(), false, panel.minValue);
                        }
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.minValue);

        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.maxValue.isVisible()) {
                            ThingworxFieldValidationHelper.validateNumericValue(panel.maxValue.getText(), false, panel.maxValue);
                        }
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.maxValue);
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

    private void addOrdinalValidator() {
        //Name validation
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        JTextField ordinalField = panel.ordinalField;
                        ThingworxFieldValidationHelper.validateIntegerValue(ordinalField.getText(), true, 0, ordinalField);
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.ordinalField);
    }

    private void revalidateUI() {
        ComponentValidator.getInstance(panel.nameField).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.ordinalField).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.defaultValue).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.dataShape).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.minValue).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.maxValue).ifPresent(ComponentValidator::revalidate);
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

        JComponent[] components = new JComponent[]{panel.nameField, panel.ordinalField, panel.defaultValue, panel.dataShape, panel.minValue, panel.maxValue};

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
        ThingworxBaseTypes baseTypes = ThingworxBaseTypes.valueOf((String) panel.baseTypeField.getSelectedItem());
        int ordinal = Integer.parseInt(panel.ordinalField.getText());
        createdDefinition = new ConfigurationTableFieldDefinition(panel.nameField.getText(), panel.descriptionField.getText(), baseTypes, ordinal);

        if(panel.required.isSelected())
            createdDefinition.addAspect("isRequired:true");

        if(panel.defaultValue.isVisible())
            createdDefinition.addAspect("defaultValue:" + panel.defaultValue.getText());

        if(!panel.friendlyName.getText().isBlank())
            createdDefinition.addAspect("friendlyName:" + panel.friendlyName.getText());

        if(!panel.selectedOption.getText().isBlank())
            createdDefinition.addAspect("selectOptions:" + panel.selectedOption.getText());

        if(!panel.isPrimaryKey.isVisible())
            createdDefinition.addAspect("isPrimaryKey:" + panel.isPrimaryKey.isSelected());

        if(baseTypes == ThingworxBaseTypes.INFOTABLE) {
            String dataShape = panel.dataShape.getText();
            if(panel.dataShape.isVisible() && dataShape != null && !dataShape.isEmpty())
                createdDefinition.addAspect("dataShape:" + dataShape);

            String infoTableType = (String) panel.infoTableType.getSelectedItem();
            if(infoTableType != null) {
                switch (infoTableType) {
                    case ThingworxConstants.DATA_TABLE_INFOTABLE -> createdDefinition.addAspect("isDataTableEntry:true");
                    case ThingworxConstants.STREAM_ENTRY_INFOTABLE -> createdDefinition.addAspect("isStreamEntry:true");
                    case ThingworxConstants.CONTENT_CRAWLER_INFOTABLE -> createdDefinition.addAspect("isContentCrawlerEntry:true");
                    default -> createdDefinition.addAspect("isEntityDataShape:true");
                }
            }
        }
        else if(ThingworxBaseTypes.isNumericType(baseTypes)) {
            String minValue = panel.minValue.getText();
            if(panel.minValue.isVisible() && minValue != null && !minValue.isEmpty())
                createdDefinition.addAspect("minimumValue:" + minValue);

            String maxValue = panel.maxValue.getText();
            if(panel.maxValue.isVisible() && maxValue != null && !maxValue.isEmpty())
                createdDefinition.addAspect("maximumValue:" + maxValue);

            String units = panel.unit.getText();
            if(panel.unit.isVisible() && units != null && !units.isEmpty())
                createdDefinition.addAspect("units:" + minValue);
        }

        super.doOKAction();
    }

}
