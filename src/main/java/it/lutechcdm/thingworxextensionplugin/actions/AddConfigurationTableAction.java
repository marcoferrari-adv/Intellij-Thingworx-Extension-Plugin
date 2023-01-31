package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.lang.jvm.JvmAnnotation;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.annotation.JvmNestedAnnotationValue;
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
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import it.lutechcdm.thingworxextensionplugin.utils.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxJavaObject;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableDefinition;
import it.lutechcdm.thingworxextensionplugin.ui.AddConfigurationTableDialogWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddConfigurationTableAction extends ThingworxAnAction {

    private static final List<ThingworxJavaObject> VALID_CLASS_TYPES = List.of(ThingworxJavaObject.AUTHENTICATOR, ThingworxJavaObject.DIRECOTRY_SERICE, ThingworxJavaObject.THING_TEMPLATE);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (!(file instanceof PsiJavaFile psiJavaFile))
            return;

        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if (project == null)
            return;

        Set<String> currentConfigurationTableName = accumulateConfigurationTableNames(psiJavaFile);

        AddConfigurationTableDialogWrapper dialog = new AddConfigurationTableDialogWrapper(project, currentConfigurationTableName);
        dialog.setModal(true);
        dialog.setSize(700, 600);
        if (dialog.showAndGet()) {
            ConfigurationTableDefinition configurationTableDefinitionDefinition = dialog.getCreatedDefinition();
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        addNewConfigurationTable(project, psiJavaFile, configurationTableDefinitionDefinition);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        fileEditorManager.openTextEditor(new OpenFileDescriptor(project, file.getVirtualFile(), 0), true);
                    })
            );
        }
    }

    private void addNewConfigurationTable(Project project, PsiJavaFile psiJavaFile, ConfigurationTableDefinition configurationTableDefinitionDefinition) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass == null)
            return;

        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_CONFIG_TABLE_DEFINITIONS_ANNOTATION);
        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_CONFIG_TABLE_DEFINITION_ANNOTATION);
        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_DATA_SHAPE_DEFINITION_ANNOTATION);
        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_FIELD_DEFINITION_ANNOTATION);

        PsiAnnotation propertyDefinitions = PSIJavaFileUtils.getOrCreateClassAnnotation(project, psiJavaFile, psiClass, ThingworxConstants.THINGWORX_CONFIG_TABLE_DEFINITIONS_ANNOTATION);
        if (propertyDefinitions == null)
            return;

        JvmAnnotationAttribute propertiesAttribute = propertyDefinitions.findAttribute("tables");
        if(propertiesAttribute == null) {
            //create dummy annotation and use property field to generate member
            PsiAnnotationMemberValue newMemberValue =
                    JavaPsiFacade.getInstance(project)
                            .getElementFactory()
                            .createAnnotationFromText("@A(tables = {\n" + configurationTableDefinitionDefinition + "\n})", null)
                            .findDeclaredAttributeValue("tables");

            if(newMemberValue == null)
                return;

            propertyDefinitions.setDeclaredAttributeValue("tables", newMemberValue);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMemberValue);
        }
        else {
            PsiAnnotationMemberValue propertiesPsiAnnotationMemberValue = propertyDefinitions.findAttributeValue("tables");
            if(propertiesPsiAnnotationMemberValue == null)
                return;

            String text = propertiesPsiAnnotationMemberValue.getText();

            if(text.startsWith("{") && text.endsWith("}"))
                text = text.substring(1, text.length() -1);
            text = StringUtils.stripEnd(text, "\r\n");
            PsiAnnotationMemberValue newMemberValue =
                    JavaPsiFacade.getInstance(project)
                            .getElementFactory()
                            .createAnnotationFromText("@A(tables = {" + text + ",\n" + configurationTableDefinitionDefinition + "\n})", null)
                            .findDeclaredAttributeValue("tables");

            if(newMemberValue == null)
                return;

            JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMemberValue);
            propertyDefinitions.setDeclaredAttributeValue("tables", newMemberValue);
        }

        JavaCodeStyleManager.getInstance(project).removeRedundantImports(psiJavaFile);

    }

    private Set<String> accumulateConfigurationTableNames(PsiJavaFile psiJavaFile) {
        Set<String> names = new HashSet<>();

        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if (psiClass == null)
            return names;


        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList == null)
            return names;

        PsiAnnotation[] annotations = modifierList.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if ("com.thingworx.metadata.annotations.ThingworxConfigurationTableDefinitions".equals(annotation.getQualifiedName())) {
                JvmAnnotationAttribute tablesAttribute = annotation.findAttribute("tables");
                if (tablesAttribute != null) {
                    JvmAnnotationAttributeValue tablesAttributeValue = tablesAttribute.getAttributeValue();
                    if (tablesAttributeValue instanceof JvmAnnotationArrayValue annotationArrayValue) {
                        for (JvmAnnotationAttributeValue annotationArrayValueEntry : annotationArrayValue.getValues()) {
                            if (annotationArrayValueEntry instanceof JvmNestedAnnotationValue nestedAnnotation) {
                                JvmAnnotation nestedAnnotationValue = nestedAnnotation.getValue();
                                JvmAnnotationAttribute nameAttribute = nestedAnnotationValue.findAttribute("name");
                                if (nameAttribute != null) {
                                    JvmAnnotationAttributeValue nameAttributeValue = nameAttribute.getAttributeValue();
                                    if(nameAttributeValue instanceof JvmAnnotationConstantValue nameConstValue && nameConstValue.getConstantValue()!= null) {
                                        names.add(nameConstValue.getConstantValue().toString());
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
        return names;
    }

    @Override
    protected boolean isAvailable(DataContext dataContext, @NotNull AnActionEvent event) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if (file instanceof PsiJavaFile psiJavaFile) {
            ThingworxJavaObject javaObjectType = PSIJavaFileUtils.getThingworxJavaObjectTpe(psiJavaFile);
            return project != null && ThingworxProjectUtils.isThingworxProject(project) && VALID_CLASS_TYPES.contains(javaObjectType);
        }
        return false;
    }


}
