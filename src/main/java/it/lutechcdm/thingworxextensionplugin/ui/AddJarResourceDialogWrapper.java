package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

public class AddJarResourceDialogWrapper extends DialogWrapper {

    private String jarPath;

    public AddJarResourceDialogWrapper(Project project) {
        super(project, false);
        setTitle("Add Jar Resource");
        init();
    }

    @SuppressWarnings({"deprecation", "rawtypes"})
    @Override
    protected @Nullable JComponent createCenterPanel() {
        ComboboxWithBrowseButton mySdkPathComboWithBrowse = new ComboboxWithBrowseButton(new JComboBox<>());
        mySdkPathComboWithBrowse.getComboBox().setEditable(true);


        TextComponentAccessor<JComboBox> textComponentAccessor = new TextComponentAccessor<>() {

            @Override
            public String getText(final JComboBox component) {
                return component.getEditor().getItem().toString().trim();
            }

            @Override
            public void setText(@NotNull final JComboBox component, @NotNull final String text) {
                if (!text.isEmpty()) {
                    component.getEditor().setItem(text);
                }
            }
        };

        ComponentWithBrowseButton.BrowseFolderActionListener<JComboBox> browseSDKListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Select JAR",
                        null, mySdkPathComboWithBrowse, null,
                        FileChooserDescriptorFactory.createSingleLocalFileDescriptor()
                                .withFileFilter(f ->  f != null && f.getExtension() != null && f.getExtension().equalsIgnoreCase("jar")),
                        textComponentAccessor);
        mySdkPathComboWithBrowse.addActionListener(browseSDKListener);

        final JTextComponent editorComponent = (JTextComponent) mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
        editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {

            @Override
            protected void textChanged(@NotNull final DocumentEvent e) {
                jarPath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
            }

        });

        return JBUI.Panels.simplePanel().addToCenter(mySdkPathComboWithBrowse);
    }

    public String getJarPath() {
        return jarPath;
    }
}
