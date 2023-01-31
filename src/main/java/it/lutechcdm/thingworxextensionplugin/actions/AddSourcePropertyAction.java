package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import it.lutechcdm.thingworxextensionplugin.utils.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxJavaObject;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.definitions.PropertyDefinition;
import it.lutechcdm.thingworxextensionplugin.ui.AddPropertyDialogWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AddSourcePropertyAction extends ThingworxAnAction {

    private static final List<ThingworxJavaObject> VALID_CLASS_TYPES = List.of(ThingworxJavaObject.THING_TEMPLATE, ThingworxJavaObject.THING_SHAPE);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if(!(file instanceof PsiJavaFile psiJavaFile))
            return;

        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if(project == null)
            return;

        AddPropertyDialogWrapper dialog = new AddPropertyDialogWrapper(project);
        dialog.setModal(true);
        dialog.setSize(400, 600);
        if(dialog.showAndGet()) {
            PropertyDefinition createdDefinition = dialog.getCreatedDefinition();
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        addNewProperty(project, psiJavaFile, createdDefinition);

                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        fileEditorManager.openTextEditor(new OpenFileDescriptor(project, file.getVirtualFile(), 0), true);
                    })
            );
        }
    }

    private void addNewProperty(Project project, PsiJavaFile psiJavaFile, @NotNull PropertyDefinition propertyDefinition) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass == null)
            return;

        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_PROPERTY_DEFINITION_ANNOTATION);
        PsiAnnotation propertyDefinitions = PSIJavaFileUtils.getOrCreateClassAnnotation(project, psiJavaFile, psiClass, ThingworxConstants.THINGWORX_PROPERTY_DEFINITIONS_ANNOTATION);
        if (propertyDefinitions == null)
            return;

        JvmAnnotationAttribute propertiesAttribute = propertyDefinitions.findAttribute("properties");
        if(propertiesAttribute == null) {
            //create dummy annotation and use property field to generate member
            PsiAnnotationMemberValue newMemberValue =
                    JavaPsiFacade.getInstance(project)
                            .getElementFactory()
                            .createAnnotationFromText("@A(properties = {\n" + propertyDefinition + "\n})", null)
                            .findDeclaredAttributeValue("properties");

            if(newMemberValue == null)
                return;

            propertyDefinitions.setDeclaredAttributeValue("properties", newMemberValue);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMemberValue);
        }
        else {
            PsiAnnotationMemberValue propertiesPsiAnnotationMemberValue = propertyDefinitions.findAttributeValue("properties");
            if(propertiesPsiAnnotationMemberValue == null)
                return;

            String text = propertiesPsiAnnotationMemberValue.getText();
            if(text.startsWith("{") && text.endsWith("}"))
                text = text.substring(1, text.length() -1);
            text = StringUtils.stripEnd(text, "\r\n");
            PsiAnnotationMemberValue newMemberValue =
                    JavaPsiFacade.getInstance(project)
                            .getElementFactory()
                            .createAnnotationFromText("@A(properties = {" + text + ",\n" + propertyDefinition + "\n})", null)
                            .findDeclaredAttributeValue("properties");

            if(newMemberValue == null)
                return;

            JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMemberValue);
            propertyDefinitions.setDeclaredAttributeValue("properties", newMemberValue);
        }

        JavaCodeStyleManager.getInstance(project).removeRedundantImports(psiJavaFile);
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
