package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import it.lutechcdm.thingworxextensionplugin.ui.AddJarResourceDialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AddJarResourceAction extends ThingworxAnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        AddJarResourceDialogWrapper wrapper = new AddJarResourceDialogWrapper(e.getProject());
        wrapper.setModal(true);
        wrapper.setSize(400, 100);
        if(wrapper.showAndGet()) {
            String jarPath = wrapper.getJarPath();
            if(wrapper.getJarPath() != null && !wrapper.getJarPath().isEmpty()) {
                Project project = e.getProject();
                if (project == null)
                    return;

                String projectBasePath = ThingworxProjectUtils.getThingworxProjectBasePath(project);
                if (projectBasePath == null)
                    return;

                String jarName = new File(jarPath).getName();
                File libDir = new File(projectBasePath, ThingworxConstants.LIB_FOLDER);
                if (!libDir.exists() && !libDir.isDirectory() && !libDir.mkdirs()) {
                    Messages.showErrorDialog(project, "Unable to create lib folder", "Add Jar Resource Error");
                    return;
                }

                File newJarFile = new File(libDir, jarName);
                try {
                    if(newJarFile.exists())
                        //jar already added
                        return;
                    Files.copy(Path.of(jarPath), Path.of(newJarFile.getPath()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
                catch (IOException ex) {
                    Messages.showErrorDialog(ex.getLocalizedMessage(), "Add JAR ERROR");
                }

                LocalFileSystem.getInstance().refresh(false);

                ApplicationManager.getApplication().invokeLater( () -> WriteCommandAction.runWriteCommandAction(project, () -> {
                    VirtualFile jarFile = LocalFileSystem.getInstance().findFileByPath(newJarFile.getPath());
                    updateMetadataReference(project, jarPath);
                    ThingworxProjectUtils.addJarToModuleBuildDependencies(project, jarFile);
                }));

            }
        }
    }



    private static void updateMetadataReference(Project project, String jarPath) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        metadata.addJarResource(jarPath);
    }
}
