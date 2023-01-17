package it.lutechcdm.thingworxextensionplugin.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxIcons;
import it.lutechcdm.thingworxextensionplugin.utils.FileUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThingworxModuleBuilder extends ModuleBuilder {

    private ThingworxProjectWizardData myWizardData;

    @Override
    public @Nullable @NonNls String getBuilderId() {
        return "ThingworxModuleBuilder";
    }

    @Override
    public String getName() {
        return "Thingworx Project";
    }

    @Override
    public String getPresentableName() {
        return "Thingworx Project";
    }

    @Override
    public String getDescription() {
        return "Thingworx project";
    }

    @Override
    public Icon getNodeIcon() {
        return ThingworxIcons.TWX_ICON;
    }

    @Override
    public ModuleType<?> getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public String getParentGroup() {
        return "Java";
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(final WizardContext context, final Disposable parentDisposable) {
        final ThingworxModuleWizardStep step = new ThingworxModuleWizardStep(context);
        Disposer.register(parentDisposable, step);
        return step;
    }

    @Override
    public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        Sdk moduleSdK = getModuleJdk();
        if (moduleSdK != null){
            modifiableRootModel.setSdk(moduleSdK);
        }
        else {
            modifiableRootModel.inheritSdk();
        }

        final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);

        final VirtualFile baseDir = contentEntry == null ? null : contentEntry.getFile();
        if (baseDir != null) {
            setupProject(modifiableRootModel, baseDir, myWizardData, contentEntry);
        }
    }

    private void setupProject(ModifiableRootModel modifiableRootModel, VirtualFile baseDir, ThingworxProjectWizardData wizardData, ContentEntry contentEntry) {
        try {
            String[] foldersToCreate = {ThingworxConstants.SRC_FOLDER, ThingworxConstants.BIN_FOLDER, ThingworxConstants.CONFIG_FOLDER, ThingworxConstants.TWX_LIB, ThingworxConstants.LIB_FOLDER};
            for(String folderToCreate : foldersToCreate) {
                File dir = new File(baseDir.getPath(), folderToCreate);
                if(!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Messages.showErrorDialog(modifiableRootModel.getProject(), "Failed to build project: unable to create folder" + dir.getAbsolutePath(), "Failed to Build Project");
                        return;
                    }
                }
            }

            LocalFileSystem.getInstance().refresh(false);
            Project project = modifiableRootModel.getProject();
            setupSdk(project, modifiableRootModel, baseDir, wizardData);

            //set source folder
            VirtualFile srcFolder = baseDir.findFileByRelativePath(ThingworxConstants.SRC_FOLDER);
            if(srcFolder == null) {
                Messages.showErrorDialog(project, "Failed to build project: " + ThingworxConstants.SRC_FOLDER + " folder not found", "Failed to Build Project");
                return;
            }

            contentEntry.addSourceFolder(srcFolder, false);

            //set bin folder and compile path
            VirtualFile binFolder = baseDir.findFileByRelativePath(ThingworxConstants.BIN_FOLDER);
            if(binFolder == null) {
                Messages.showErrorDialog(project, "Failed to build project: " + ThingworxConstants.BIN_FOLDER + " folder not found", "Failed to Build Project");
                return;
            }
            contentEntry.addExcludeFolder(binFolder);
            CompilerModuleExtension compilerModuleExtension = modifiableRootModel.getModuleExtension(CompilerModuleExtension.class);
            compilerModuleExtension.setCompilerOutputPath(binFolder);
            compilerModuleExtension.setExcludeOutput(true);
            compilerModuleExtension.inheritCompilerOutputPath(false);

            VirtualFile configFilesFolder = baseDir.findFileByRelativePath(ThingworxConstants.CONFIG_FOLDER);
            if(configFilesFolder == null) {
                Messages.showErrorDialog(project, "Failed to build project: " + ThingworxConstants.CONFIG_FOLDER + " folder not found", "Failed to Build Project");
                return;
            }

            createMetadataInfo(project, configFilesFolder, wizardData);
            createBuildExtension(project, baseDir, wizardData);
            createExtensionPropertyFile(baseDir, wizardData);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createExtensionPropertyFile(VirtualFile baseDir, ThingworxProjectWizardData wizardData) throws IOException {
        Properties p = new Properties();
        try(PrintWriter pw = new PrintWriter(new FileOutputStream(new File(baseDir.getPath(), "extension.properties"), false), true, StandardCharsets.UTF_8)) {
            p.put("build_framework", "ANT");
            p.put("eclipse.preferences.version", "1");
            p.put("ha_compatible", "" + wizardData.haCompatible);
            p.put("min_thingworx_version", wizardData.minTwxVersion);
            p.put("package_version", wizardData.packageVersion);
            p.put("project_vendor", wizardData.vendor);
            p.put("sdk_location", wizardData.sdkLocation);
            p.put("plugin_version", "1.0.0");
            p.store(pw, null);
        }
    }

    private void createBuildExtension(Project project, VirtualFile baseDir, ThingworxProjectWizardData wizardData) throws IOException {
        try(InputStream is = this.getClass().getResourceAsStream("/templates/build-extension.xml")) {
            if(is == null)
                throw new IOException("Failed to get build-extension.xml template");

            String content = FileUtils.readStreamToString(is);
            content = content.replaceFirst("\\{name}", project.getName());
            content = content.replaceFirst("\\{jar_name}", project.getName().toLowerCase().replaceAll("\\s+", ""));
            content = content.replaceFirst("\\{package_version}", wizardData.packageVersion);
            content = content.replaceFirst("\\{project_vendor}", wizardData.vendor);
            try(FileOutputStream fos = new FileOutputStream(new File(baseDir.getPath(), "build-extension.xml"))) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void createMetadataInfo(Project project, VirtualFile configFilesFolder, ThingworxProjectWizardData wizardData) throws IOException {
        try(InputStream is = this.getClass().getResourceAsStream("/templates/metadata.xml")) {
            if(is == null)
                throw new IOException("Failed to get metadata.xml template");

            String content = FileUtils.readStreamToString(is);
            content = content.replaceFirst("\\{name}", project.getName());
            content = content.replaceFirst("\\{minimumThingWorxVersion}", wizardData.minTwxVersion);
            content = content.replaceFirst("\\{packageVersion}", wizardData.packageVersion);
            content = content.replaceFirst("\\{vendor}", wizardData.vendor);
            content = content.replaceFirst("\\{haCompatible}", "" + wizardData.haCompatible);

            try(FileOutputStream fos = new FileOutputStream(new File(configFilesFolder.getPath(), "metadata.xml"))) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void setupSdk(Project project, ModifiableRootModel modifiableRootModel, VirtualFile baseDir, ThingworxProjectWizardData wizardData) throws IOException {
        String sdkPath = wizardData.sdkLocation;
        VirtualFile twxLibFolder = baseDir.findFileByRelativePath(ThingworxConstants.TWX_LIB);
        if(twxLibFolder == null) {
            Messages.showErrorDialog(project, "Failed to build project: " + ThingworxConstants.TWX_LIB + " folder not found", "Failed to Build Project");
            return;
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(sdkPath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                if(fileName.toLowerCase().endsWith(".jar") && !entry.isDirectory()) {
                    File filePath = new File(twxLibFolder.getPath(), fileName);
                    FileUtils.copyStreamToOut(zipIn, filePath.getPath());
                }
                //to next entry
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        final Library twxSdkLibrary = modifiableRootModel.getModuleLibraryTable().createLibrary("twx-sdk");
        twxLibFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(twxLibFolder.getPath());
        if (twxLibFolder != null) {
            final Library.ModifiableModel libModel = twxSdkLibrary.getModifiableModel();
            libModel.addJarDirectory(twxLibFolder, true, OrderRootType.CLASSES);
            libModel.commit();
        }
    }



    void setWizardData(ThingworxProjectWizardData settings) {
        myWizardData = settings;
    }
}
