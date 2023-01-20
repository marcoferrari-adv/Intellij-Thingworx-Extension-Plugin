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
import it.lutechcdm.thingworxextensionplugin.definitions.PropertyDefinition;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AddPropertyDialogWrapper extends DialogWrapper {

    final AddPropertyPanel panel = new AddPropertyPanel();

    PropertyDefinition createdDefinition = null;

    public AddPropertyDialogWrapper(Project project) {
        super(project, false);
        setTitle("Add Property");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel p = JBUI.Panels.simplePanel();

        addNameValidator();
        addDefaultValueValidator();
        addInfoTableDataShapeValidator();
        addNumericFiledValidator();

        //add event listener on fields
        panel.nameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        panel.defaultValue.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        panel.baseTypeField.addItemListener(e -> revalidateUI());

        panel.isDefault.addItemListener(e -> revalidateUI());

        panel.dataShape.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        p.addToCenter(panel.mainPanel);
        return p;
    }

    private void addNumericFiledValidator() {
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.minValue.isVisible()) {
                            ThingworxFieldValidationHelper.validateNumericValue(panel.minValue.getText(), false, panel.dataShape);
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
                            ThingworxFieldValidationHelper.validateNumericValue(panel.maxValue.getText(), false, panel.dataShape);
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

    private void addInfoTableDataShapeValidator() {

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
        ComponentValidator.getInstance(panel.defaultValue).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.dataShape).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.minValue).ifPresent(ComponentValidator::revalidate);
        ComponentValidator.getInstance(panel.maxValue).ifPresent(ComponentValidator::revalidate);
        setOKActionEnabled(true);
    }

    private void addDefaultValueValidator() {
        //default value validation
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        if(panel.isDefault.isSelected() && panel.defaultValue.isVisible()) {
                            JTextField defaultValueField = panel.defaultValue;
                            JComboBox<String> baseTypeField = panel.baseTypeField;
                            ThingworxBaseTypes baseTypes = ThingworxBaseTypes.valueOf((String) baseTypeField.getSelectedItem());
                            ThingworxFieldValidationHelper.validateDefaultValueForType(defaultValueField.getText(), baseTypes, defaultValueField);
                        }
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.defaultValue);
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

        JComponent[] components = new JComponent[]{panel.nameField, panel.defaultValue, panel.dataShape, panel.minValue, panel.maxValue};

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
        createdDefinition = new PropertyDefinition(panel.nameField.getText(), baseTypes, panel.descriptionField.getText(), panel.categoryField.getText(), false);
        createdDefinition.addAspect("dataChangeType:" + panel.dataChangeType.getSelectedItem());
        if(panel.defaultValue.isVisible())
            createdDefinition.addAspect("defaultValue:" + panel.defaultValue.getText());

        createdDefinition.addAspect("isPersistent:" + panel.persistent.isSelected());
        createdDefinition.addAspect("isLogged:" + panel.logged.isSelected());
        createdDefinition.addAspect("isReadOnly:" + panel.readonly.isSelected());
        if(baseTypes == ThingworxBaseTypes.INFOTABLE) {
            String dataShape = panel.dataShape.getText();
            if(panel.dataShape.isVisible() && dataShape != null && !dataShape.isEmpty())
                createdDefinition.addAspect("dataShape:" + dataShape);

            String infoTableType = (String) panel.infotableType.getSelectedItem();
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

    public PropertyDefinition getCreatedDefinition() {
        return createdDefinition;
    }
}
