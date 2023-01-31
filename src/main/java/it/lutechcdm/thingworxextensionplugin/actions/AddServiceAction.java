package it.lutechcdm.thingworxextensionplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import it.lutechcdm.thingworxextensionplugin.utils.PSIJavaFileUtils;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxJavaObject;
import it.lutechcdm.thingworxextensionplugin.utils.ThingworxProjectUtils;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceDefinition;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceParameter;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceResult;
import it.lutechcdm.thingworxextensionplugin.ui.AddServiceDialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

public class AddServiceAction extends ThingworxAnAction {

    private static final List<ThingworxJavaObject> VALID_CLASS_TYPES = List.of(ThingworxJavaObject.THING_TEMPLATE,
            ThingworxJavaObject.THING_SHAPE, ThingworxJavaObject.AUTHENTICATOR);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if(!(file instanceof PsiJavaFile psiJavaFile))
            return;

        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if(project == null)
            return;

        AddServiceDialogWrapper dialog = new AddServiceDialogWrapper(project);
        dialog.setModal(true);
        dialog.setSize(700, 600);
        if(dialog.showAndGet()) {
            ServiceDefinition serviceDefinition = dialog.getCreatedDefinition();
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project, () -> addNewServiceMethod(project, psiJavaFile, serviceDefinition))
            );
        }
    }

    private void addNewServiceMethod(Project project, PsiJavaFile psiJavaFile, ServiceDefinition serviceDefinition) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass == null)
            return;

        ServiceResult serviceResult = serviceDefinition.getServiceResult();
        if(serviceResult == null) {
            Messages.showErrorDialog(project, "Unable to generate service result", "Service Creation Error");
            return;
        }

        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_SERVICE_RESULT_ANNOTATION);
        PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_SERVICE_DEFINITION_ANNOTATION);

        if(!serviceDefinition.getServiceParameters().isEmpty())
            PSIJavaFileUtils.ensureClassIsImported(project, psiJavaFile, ThingworxConstants.THINGWORX_SERVICE_PARAMETER_ANNOTATION);

        PsiAnnotation definitionAnnotation = JavaPsiFacade.getInstance(project)
                .getElementFactory()
                .createAnnotationFromText(serviceDefinition.toString(), null);

        PsiAnnotation resultAnnotation = JavaPsiFacade.getInstance(project)
                .getElementFactory()
                .createAnnotationFromText(serviceResult.toString(), null);

        definitionAnnotation = (PsiAnnotation) JavaCodeStyleManager.getInstance(project).shortenClassReferences(definitionAnnotation);
        resultAnnotation = (PsiAnnotation) JavaCodeStyleManager.getInstance(project).shortenClassReferences(resultAnnotation);

        LinkedHashMap<String, ThingworxBaseTypes> parameterDefinitions = new LinkedHashMap<>();
        LinkedHashMap<String, PsiAnnotation[]> parameterAnnotations = new LinkedHashMap<>();
        for(ServiceParameter parameter : serviceDefinition.getServiceParameters()) {
            parameterDefinitions.put(parameter.getName(), parameter.getBaseType());

            PsiAnnotation parameterAnnotation = JavaPsiFacade.getInstance(project)
                    .getElementFactory()
                    .createAnnotationFromText(parameter.toString(), null);
            parameterAnnotations.put(parameter.getName(), new PsiAnnotation[] {parameterAnnotation});
        }

        PsiAnnotation[] methodAnnotations = new PsiAnnotation[] {definitionAnnotation, resultAnnotation};
        PsiMethod newMethod = PSIJavaFileUtils.createMethod(project, psiJavaFile, psiClass, serviceDefinition.getName(), serviceResult.getBaseType(),
                methodAnnotations, parameterDefinitions, parameterAnnotations);

        if(newMethod == null)
            Messages.showErrorDialog(project, "Failed to create method", "Method Creation Error");
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
