package it.lutechcdm.thingworxextensionplugin.project;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("ALL")
public class ThingworxGeneratorPeer implements ProjectGeneratorPeer<ThingworxProjectWizardData> {

    private static final String TWX_SDK_JAR_PREFIX = "thingworx-ext-sdk-";

    private JPanel myMainPanel;
    private ComboboxWithBrowseButton mySdkPathComboWithBrowse;

    private EditorTextField vendor;

    private EditorTextField packageVersion;

    private EditorTextField minThingworxVersion;

    private JBLabel error;
    private JCheckBox haCompatible;

    private String sdkPath;

    public ThingworxGeneratorPeer() {
        mySdkPathComboWithBrowse.getComboBox().setEditable(true);

        TextComponentAccessor<JComboBox> textComponentAccessor = new TextComponentAccessor<>() {

            @Override
            public String getText(final JComboBox component) {
                return getItemFromCombo(component);
            }

            @Override
            public void setText(@NotNull final JComboBox component, @NotNull final String text) {
                if (!text.isEmpty()) {
                    component.getEditor().setItem(text);
                }
            }
        };

        ComponentWithBrowseButton.BrowseFolderActionListener<JComboBox> browseSDKListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Select Thingworx SDK",
                        null, mySdkPathComboWithBrowse, null,
                        FileChooserDescriptorFactory.createSingleLocalFileDescriptor()
                                .withFileFilter(f ->  f != null && f.getExtension() != null && f.getExtension().equalsIgnoreCase("zip")),
                        textComponentAccessor);
        mySdkPathComboWithBrowse.addActionListener(browseSDKListener);


        final JTextComponent editorComponent = (JTextComponent) mySdkPathComboWithBrowse.getComboBox().getEditor().getEditorComponent();
        editorComponent.getDocument().addDocumentListener(new DocumentAdapter() {

            @Override
            protected void textChanged(@NotNull final DocumentEvent e) {
                sdkPath = mySdkPathComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
                if (!sdkPath.isEmpty()) {
                    error.setVisible(false);
                    onSdkPathChanged(sdkPath);
                }
            }

        });
    }

    private void onSdkPathChanged(String sdkPath) {
        try (ZipFile zipFile = new ZipFile(sdkPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            boolean sdkFound = false;
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();

                if(fileName.contains(TWX_SDK_JAR_PREFIX) &&
                        fileName.lastIndexOf("-") > fileName.indexOf(TWX_SDK_JAR_PREFIX) + TWX_SDK_JAR_PREFIX.length()) {

                    String sdkVersion = fileName.substring(fileName.indexOf(TWX_SDK_JAR_PREFIX) + TWX_SDK_JAR_PREFIX.length(), fileName.lastIndexOf("-"));
                    minThingworxVersion.setText(sdkVersion);
                    sdkFound = true;
                    break;
                }
            }

            if(!sdkFound) {
                error.setVisible(true);
                error.setText("Invalid sdk file " + sdkPath);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getItemFromCombo(@NotNull final JComboBox combo) {
        return combo.getEditor().getItem().toString().trim();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return myMainPanel;
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsField("Thingworx SDK", mySdkPathComboWithBrowse);
        settingsStep.addSettingsField("Vendor", vendor);
        settingsStep.addSettingsField("Package version", packageVersion);
        settingsStep.addSettingsField("Min. thingworx version", minThingworxVersion);
    }

    @NotNull
    @Override
    public ThingworxProjectWizardData getSettings() {
        return new ThingworxProjectWizardData(sdkPath, vendor.getText(), packageVersion.getText(), minThingworxVersion.getText(), haCompatible.isSelected());
    }

    @Override
    public @Nullable ValidationInfo validate() {

        if(sdkPath == null || sdkPath.isEmpty()) {
            error.setVisible(true);
            error.setText("SDK Path is required");
        }

        if(error.isVisible()) {
            return new ValidationInfo(error.getText());
        }

        return null;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    public boolean validateInIntelliJ() {
        ValidationInfo validationInfo = validate();
        return validationInfo == null;
    }

    private void createUIComponents() {
        mySdkPathComboWithBrowse = new ComboboxWithBrowseButton(new ComboBox<>());
    }
}
