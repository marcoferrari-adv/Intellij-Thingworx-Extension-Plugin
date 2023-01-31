package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableDefinition;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableFieldDefinition;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import it.lutechcdm.thingworxextensionplugin.validation.ThingworxFieldValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.Comparator;
import java.util.Set;

public class AddConfigurationTableDialogWrapper extends DialogWrapper {

    final AddConfigurationTablePanel panel;

    private ConfigurationTableDefinition createdDefinition = null;

    private final Project project;
    private final Set<String> currentConfigTableNames;

    public AddConfigurationTableDialogWrapper(Project project, Set<String> currentConfigTableNames) {
        super(project, false);
        panel = new AddConfigurationTablePanel(project, currentConfigTableNames.size());
        setTitle("Add Configuration Table");
        init();
        this.project = project;
        this.currentConfigTableNames = currentConfigTableNames;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel p = JBUI.Panels.simplePanel();

        addNameValidator();
        addOrdinalValidator();

        //add event listener on fields
        panel.nameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });
        panel.ordinalField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {revalidateUI();
            }
        });
        p.addToCenter(panel.mainPanel);
        return p;
    }

    private void addNameValidator() {
        //Name validation
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        JTextField nameField = panel.nameField;
                        ThingworxFieldValidationHelper.validatePropertyName(nameField.getText(), nameField);

                        if(currentConfigTableNames.contains(nameField.getText()))
                            throw new ThingworxValidationException("Configuration table name " + nameField.getText() + " already used", nameField);
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
        //Ordinal validation
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

        JComponent[] components = new JComponent[]{panel.nameField, panel.ordinalField};

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
        JTable table = panel.fieldDefinitionsTable;
        if(table.getRowCount() < 1) {
            Messages.showErrorDialog(project, "At least one data shape definition must be present", "Error");
            return;
        }

        createdDefinition = new ConfigurationTableDefinition(panel.nameField.getText(), panel.descriptionField.getText(), panel.isMultiRowTable.isSelected(), Integer.parseInt(panel.ordinalField.getText()));
        panel.configurationTableParameters.sort(Comparator.comparing(ConfigurationTableFieldDefinition::getOrdinal));
        for(ConfigurationTableFieldDefinition parameter : panel.configurationTableParameters)
            createdDefinition.addFiledDefinition(parameter);

        super.doOKAction();
    }

    public ConfigurationTableDefinition getCreatedDefinition() {
        return createdDefinition;
    }
}
