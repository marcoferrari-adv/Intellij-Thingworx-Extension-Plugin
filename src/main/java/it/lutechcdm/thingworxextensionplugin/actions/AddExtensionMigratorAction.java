package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiRecordHeader;
import com.intellij.util.IncorrectOperationException;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import it.lutechcdm.thingworxextensionplugin.ui.ThingworxIcons;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AddExtensionMigratorAction extends JavaCreateTemplateInPackageAction<PsiClass> implements DumbAware, CreateThingworxJavaTemplateAction {

    public AddExtensionMigratorAction() {
        super("New Extension Migrator", "New Extension Migrator", ThingworxIcons.ADD_EXTENSION_MIGRATOR_ICON, true);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.@NotNull Builder builder) {
        builder.setTitle("New Extension Migrator")
                .addKind("Extension migrator", ThingworxIcons.ADD_EXTENSION_MIGRATOR_ICON, "Extension Migrator");
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory directory, @NonNls @NotNull String newName, @NonNls String templateName) {
        return "Create Extension Migrator " + newName;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        boolean isAvailable = super.isAvailable(event.getDataContext()) && isThingworxAvailable(event);
        event.getPresentation().setEnabledAndVisible(isAvailable);
    }

    @Override
    protected @Nullable PsiElement getNavigationElement(@NotNull PsiClass createdElement) {
        if (createdElement.isRecord()) {
            PsiRecordHeader header = createdElement.getRecordHeader();
            if (header != null) {
                return header.getLastChild();
            }
        }
        return createdElement.getLBrace();
    }

    @Override
    protected @Nullable PsiClass doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
        return JavaDirectoryService.getInstance().createClass(dir, className, templateName, false);
    }

    @Override
    protected void postProcess(@NotNull PsiClass createdElement, String templateName, Map<String, String> customProperties) {
        super.postProcess(createdElement, templateName, customProperties);

        Project project = createdElement.getProject();
        PsiJavaFile psiJavaFile = (PsiJavaFile) createdElement.getContainingFile();

        if(ThingworxProjectUtils.isThingworxProject(project)) {
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project, () -> addThingMetadataReference(project, psiJavaFile, createdElement))
            );
        }
    }

    @Override
    public void addThingMetadataReference(Project project, PsiJavaFile psiJavaFile, PsiClass psiClass) {
        MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(project));
        String className = psiJavaFile.getPackageName().isEmpty() ? psiClass.getName() : psiJavaFile.getPackageName() + "." + psiClass.getName();

        if(metadata.existsExtensionMigrator()) {
            //ask confirm if already defined
            int selectedOption = Messages.showDialog("An extension migrator is already defined, creating a new one will override the existing one", "Override Extension Migrator",
                    new String[]{Messages.getOkButton(), Messages.getCancelButton()}, 0, 0, Messages.getWarningIcon(), null);
            if (selectedOption == 0)
                metadata.addExtensionMigrator(className);
        }
        else
            metadata.addExtensionMigrator(className);
    }
}
