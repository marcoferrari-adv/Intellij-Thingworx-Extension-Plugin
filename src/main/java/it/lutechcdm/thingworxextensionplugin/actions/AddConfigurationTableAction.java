package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import it.lutechcdm.thingworxextensionplugin.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.ThingworxJavaObject;
import it.lutechcdm.thingworxextensionplugin.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableDefinition;
import it.lutechcdm.thingworxextensionplugin.definitions.EventDefinition;
import it.lutechcdm.thingworxextensionplugin.ui.AddConfigurationTableDialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddConfigurationTableAction extends ThingworxAnAction {

    private static final List<ThingworxJavaObject> VALID_CLASS_TYPES = List.of(ThingworxJavaObject.AUTHENTICATOR, ThingworxJavaObject.DIRECOTRY_SERICE, ThingworxJavaObject.THING_TEMPLATE);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if(!(file instanceof PsiJavaFile psiJavaFile))
            return;

        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if(project == null)
            return;

        AddConfigurationTableDialogWrapper dialog = new AddConfigurationTableDialogWrapper(project);
        dialog.setModal(true);
        dialog.setSize(700, 600);
        if(dialog.showAndGet()) {
            ConfigurationTableDefinition configurationTableDefinitionDefinition = dialog.getCreatedDefinition();
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        //addNewConfigurationTable(project, psiJavaFile, configurationTableDefinitionDefinition);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        fileEditorManager.openTextEditor(new OpenFileDescriptor(project, file.getVirtualFile(), 0), true);
                    })
            );
        }

    }

    @Override
    protected boolean isAvailable(DataContext dataContext, @NotNull AnActionEvent event) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if(file instanceof PsiJavaFile psiJavaFile) {
            ThingworxJavaObject javaObjectType = PSIJavaFileUtils.getThingworxJavaObjectTpe(psiJavaFile);
            return project != null && ThingworxProjectUtils.isThingworxProject(project) && VALID_CLASS_TYPES.contains(javaObjectType);
        }
        return false;
    }


}
