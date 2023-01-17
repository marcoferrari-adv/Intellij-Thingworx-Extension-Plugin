package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import it.lutechcdm.thingworxextensionplugin.ThingworxProjectUtils;

import java.util.Objects;

public interface CreateThingworxJavaTemplateAction {
    void addThingMetadataReference(Project project, PsiJavaFile psiJavaFile, PsiClass psiClass);

    default boolean isThingworxAvailable(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        boolean isValidTwxProject = project != null && ThingworxProjectUtils.isThingworxProject(project);
        boolean isFileNotFile = event.getData(CommonDataKeys.PSI_FILE) == null;
        return isValidTwxProject && isFileNotFile && Objects.equals(event.getPlace(), ActionPlaces.PROJECT_VIEW_POPUP);
    }
}
