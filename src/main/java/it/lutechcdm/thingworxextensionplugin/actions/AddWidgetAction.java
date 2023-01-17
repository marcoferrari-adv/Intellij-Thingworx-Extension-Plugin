package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import it.lutechcdm.thingworxextensionplugin.ui.AddWidgetDialogWrapper;
import it.lutechcdm.thingworxextensionplugin.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AddWidgetAction extends ThingworxAnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if(project == null)
            return;

        AddWidgetDialogWrapper wrapper = new AddWidgetDialogWrapper(project);
        wrapper.setModal(true);
        wrapper.setSize(400, 150);
        if(wrapper.showAndGet()) {
            String name = wrapper.getName();
            String description = wrapper.getDescription();
            if(name != null && !name.isEmpty()) {
                String normalizedPluginName = name.replaceAll("\\s+", "_").toLowerCase().trim();
                ApplicationManager.getApplication().invokeLater( () ->
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            VirtualFile pluginFolder = createWidgetFolderStructure(project, normalizedPluginName);
                            if(pluginFolder != null) {
                                VirtualFile vf1 = copyTemplateFile(project, normalizedPluginName, "/templates/plugin_name.ide.css", new File(pluginFolder.getPath(), normalizedPluginName + ".ide.css"));
                                VirtualFile vf2 = copyTemplateFile(project, normalizedPluginName, "/templates/plugin_name.runtime.css", new File(pluginFolder.getPath(), normalizedPluginName + ".runtime.css"));
                                VirtualFile vf3 = copyTemplateFile(project, normalizedPluginName, "/templates/plugin_name.ide.js", new File(pluginFolder.getPath(), normalizedPluginName + ".ide.js"));
                                VirtualFile vf4 = copyTemplateFile(project, normalizedPluginName, "/templates/plugin_name.runtime.js", new File(pluginFolder.getPath(), normalizedPluginName + ".runtime.js"));

                                updateMetadataReference(project, normalizedPluginName, description, vf1, vf2, vf3, vf4);
                            }
                        }
                        catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                );
            }
        }
    }

    private VirtualFile copyTemplateFile(Project project, String normalizedPluginName, String templateName, File file) throws IOException {

        try(InputStream is = this.getClass().getResourceAsStream(templateName)) {
            if(is == null)
                throw new IOException("Failed to get file " + templateName + " template");

            String content = FileUtils.readStreamToString(is);
            content = content.replaceAll("plugin_name", normalizedPluginName);
            content = content.replaceAll("project_name", project.getName());
            try(FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        }

        LocalFileSystem.getInstance().refresh(false);
        return LocalFileSystem.getInstance().findFileByPath(file.getPath());
    }

    private static void updateMetadataReference(Project project, String name, String description, VirtualFile...fileResources) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        metadata.addWidget(name, description, fileResources);
    }

    private VirtualFile createWidgetFolderStructure(Project project, String name) {
        String projectBasePath = ThingworxProjectUtils.getThingworxProjectBasePath(project);
        File uiFolder = new File(projectBasePath, ThingworxConstants.UI_FOLDER);
        if(!uiFolder.exists() && !uiFolder.mkdirs()) {
            Messages.showErrorDialog(project, "Unable to create Widget: error while creating folder " + uiFolder.getPath(), "Add Widget Error");
            return null;
        }

        File pluginFolder = new File(uiFolder, name);
        if(!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            Messages.showErrorDialog(project, "Unable to create Widget: error while creating folder " + pluginFolder.getPath(), "Add Widget Error");
            return null;
        }

        LocalFileSystem.getInstance().refresh(false);
        return LocalFileSystem.getInstance().findFileByPath(pluginFolder.getPath());
    }


}
