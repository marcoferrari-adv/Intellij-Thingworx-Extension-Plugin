package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import it.lutechcdm.thingworxextensionplugin.ThingworxFieldValidationHelper;
import it.lutechcdm.thingworxextensionplugin.definitions.EventDefinition;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AddEventDialogWrapper extends DialogWrapper {

    final AddEventPanel panel = new AddEventPanel();

    private EventDefinition createdDefinition = null;

    public AddEventDialogWrapper(Project project) {
        super(project, false);
        setTitle("Add Event");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        BorderLayoutPanel p = JBUI.Panels.simplePanel();

        addNameValidator();
        addDataShapeValidator();

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

        p.addToCenter(panel.mainPanel);
        return p;
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

        JComponent[] components = new JComponent[]{panel.nameField, panel.dataShape};

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
        createdDefinition = new EventDefinition(panel.nameField.getText(), panel.dataShape.getText(), panel.descriptionField.getText(), panel.categoryField.getText());
        super.doOKAction();
    }

    public EventDefinition getCreatedDefinition() {
        return createdDefinition;
    }
}
