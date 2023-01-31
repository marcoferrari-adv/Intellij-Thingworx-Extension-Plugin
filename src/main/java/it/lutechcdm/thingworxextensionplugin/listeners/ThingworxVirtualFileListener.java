package it.lutechcdm.thingworxextensionplugin.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import it.lutechcdm.thingworxextensionplugin.utils.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThingworxVirtualFileListener implements BulkFileListener {

    private final Project project;

    public ThingworxVirtualFileListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for(VFileEvent event : events) {
            VirtualFile eventFile = event.getFile();
            if(eventFile != null && event instanceof VFileDeleteEvent)
                afterFileDeletion(eventFile);
        }
    }

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        for(VFileEvent event : events) {
            VirtualFile eventFile = event.getFile();
            if(eventFile != null && event instanceof VFileDeleteEvent)
                beforeFileDeletion(eventFile);
        }
    }

    private void beforeFileDeletion(@NotNull VirtualFile eventFile) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(eventFile);
        if(psiFile instanceof PsiJavaFile psiJavaFile) {

            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
            if(psiClass == null)
                return;

            if(ThingworxProjectUtils.isThingworxProject(project) && PSIJavaFileUtils.isClassMetadataManaged(psiJavaFile)) {
                ApplicationManager.getApplication().invokeLater(() ->
                        WriteCommandAction.runWriteCommandAction(project, () -> removeMetadataReference(project, psiJavaFile, psiClass))
                );
            }
        }
    }

    private void afterFileDeletion(@NotNull VirtualFile eventFile) {
        VirtualFile deletedFileParent = eventFile.getParent();
        if(deletedFileParent == null)
            return;

        if(deletedFileParent.isDirectory() && deletedFileParent.getName().equals(ThingworxConstants.LIB_FOLDER) && eventFile.getName().toLowerCase().endsWith("jar")) {
            if(ThingworxProjectUtils.isThingworxProject(project)) {
                ApplicationManager.getApplication().invokeLater(() ->
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                                removeJarMetadataReference(project, eventFile.getName());
                                ThingworxProjectUtils.removeJarToModuleBuildDependencies(project, eventFile.getPath());
                            }
                        )
                );
            }
        }

        if(deletedFileParent.isDirectory() && deletedFileParent.getName().equals(ThingworxConstants.UI_FOLDER) && eventFile.isDirectory()) {
            if(ThingworxProjectUtils.isThingworxProject(project)) {
                ApplicationManager.getApplication().invokeLater(() ->
                        WriteCommandAction.runWriteCommandAction(project, () -> removeWidgetMetadataReference(project, eventFile.getName()))
                );
            }
        }
    }

    private void removeJarMetadataReference(Project project, String jarName) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        XmlTag rootTag = metadata.getMetadataRoot();
        if (rootTag == null)
            return;

        metadata.removeJarResource(jarName);
    }

    private void removeWidgetMetadataReference(Project project, String widgetName) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        metadata.removeWidget(widgetName);
    }

    private void removeMetadataReference(Project project, PsiJavaFile psiJavaFile, PsiClass psiClass) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        String className = psiJavaFile.getPackageName().isEmpty() ? psiClass.getName() : psiJavaFile.getPackageName() + "." + psiClass.getName();

        if(metadata.removeThingTemplate(className))
            return;
        if(metadata.removeAuthenticator(className))
            return;
        if(metadata.removeThingShape(className))
            return;
        if(metadata.removeDirectoryService(className))
            return;
        if(metadata.removeExtensionManager(className))
            return;

        metadata.removeScriptFunctionLibrary(className);
    }
}
