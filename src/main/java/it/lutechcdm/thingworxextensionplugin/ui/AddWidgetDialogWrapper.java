package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import it.lutechcdm.thingworxextensionplugin.validation.ThingworxFieldValidationHelper;
import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AddWidgetDialogWrapper extends DialogWrapper {

    private AddWidgetPanel panel;

    public AddWidgetDialogWrapper(Project project) {
        super(project, false);
        setTitle("Add Widget");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        panel = new AddWidgetPanel();

        addNameValidator();

        panel.name.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                revalidateUI();
            }
        });

        return panel.mainPanel;
    }

    private void addNameValidator() {
        //Name validation
        new ComponentValidator(getDisposable())
                .withValidator(() -> {
                    ValidationInfo v = null;
                    try {
                        ThingworxFieldValidationHelper.validateWidgetName(panel.name.getText(), panel.name);
                    }
                    catch (ThingworxValidationException e) {
                        v = new ValidationInfo(e.getMessage(), e.getSource());
                    }
                    return v;
                })
                .andStartOnFocusLost()
                .installOn(panel.name);
    }

    private void revalidateUI() {
        ComponentValidator.getInstance(panel.name).ifPresent(ComponentValidator::revalidate);
        setOKActionEnabled(true);
    }

    protected @Nullable ValidationInfo doValidate() {
        ValidationInfo validationInfo = super.doValidate();
        if (validationInfo != null)
            return validationInfo;

        JComponent[] components = new JComponent[]{panel.name,};

        for (JComponent component : components) {
            if (ComponentValidator.getInstance(component).isPresent()) {
                if (ComponentValidator.getInstance(component).get().getValidationInfo() != null) {
                    return ComponentValidator.getInstance(component).get().getValidationInfo();
                }
            }
        }
        return null;
    }

    public String getName() {
        if(panel != null)
            return panel.name.getText();

        return null;
    }

    public String getDescription() {
        if(panel != null)
            return panel.description.getText();

        return "";
    }
}
