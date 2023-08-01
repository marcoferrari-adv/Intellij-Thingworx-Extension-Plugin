package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import org.jetbrains.annotations.NotNull;

public abstract class ThingworxAnAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabledAndVisible(isAvailable(event.getDataContext(), event));
    }

    protected boolean isAvailable(DataContext dataContext, @NotNull AnActionEvent event) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        return project != null && ThingworxProjectUtils.isThingworxProject(project);
    }
}
