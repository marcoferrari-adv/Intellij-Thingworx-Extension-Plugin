package it.lutechcdm.thingworxextensionplugin.groups;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import it.lutechcdm.thingworxextensionplugin.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.ThingworxProjectUtils;
import org.jetbrains.annotations.NotNull;

public class ThingworxSourceActionGroup extends DefaultActionGroup {

    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        if(!event.getPresentation().isVisible())
            return;

        boolean isVisible = false;
        Project project = CommonDataKeys.PROJECT.getData(event.getDataContext());
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if(file instanceof PsiJavaFile) {
            isVisible = project != null && ThingworxProjectUtils.isThingworxProject(project) && PSIJavaFileUtils.isClassMetadataManaged((PsiJavaFile) file);
        }

        event.getPresentation().setEnabledAndVisible(isVisible);
    }

}
