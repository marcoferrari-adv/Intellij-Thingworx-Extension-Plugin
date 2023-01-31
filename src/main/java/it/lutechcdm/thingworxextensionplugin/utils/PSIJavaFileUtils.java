package it.lutechcdm.thingworxextensionplugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.ThingworxJavaObject;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class PSIJavaFileUtils {

    public static void ensureClassIsImported(@NotNull Project project, @NotNull PsiJavaFile psiJavaFile, @NotNull String fullyQualifiedName) {
        PsiImportList importList = psiJavaFile.getImportList();
        if(importList == null)
            return;

        PsiClass classToImport = JavaPsiFacade.getInstance(project).findClass(fullyQualifiedName, GlobalSearchScope.allScope(project));
        if(classToImport == null)
            return;
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiImportStatement importStatement = factory.createImportStatement(classToImport);
        importList.add(importStatement);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(importStatement);
    }

    public static PsiAnnotation getOrCreateClassAnnotation(@NotNull Project project, @NotNull PsiJavaFile psiJavaFile, @NotNull PsiClass psiClass, @NotNull String annotationQualifiedName) {

        PsiModifierList modifierList =  psiClass.getModifierList();
        if(modifierList == null)
            return null;

        PsiAnnotation theAnnotation = null;
        PsiAnnotation[] annotations = modifierList.getAnnotations();
        for(PsiAnnotation annotation : annotations) {
            if (annotationQualifiedName.equals(annotation.getQualifiedName())) {
                theAnnotation = annotation;
                break;
            }
        }

        if(theAnnotation == null) {
            ensureClassIsImported(project, psiJavaFile, annotationQualifiedName);
            theAnnotation = modifierList.addAnnotation(annotationQualifiedName);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(theAnnotation);
        }

        return theAnnotation;
    }

    public static PsiMethod createMethod(@NotNull Project project, @NotNull PsiJavaFile psiJavaFile, @NotNull PsiClass psiClass, String name, ThingworxBaseTypes returnType, PsiAnnotation[] methodAnnotations,
                                         Map<String, ThingworxBaseTypes> parameterDefinitions, Map<String, PsiAnnotation[]> parameterAnnotation) {

        boolean isReturnTypeArray = ThingworxBaseTypes.isJavaArrayType(returnType);
        PsiType returnClass = PsiType.VOID;
        if(ThingworxBaseTypes.isJavaClassType(returnType)) {
            ensureClassIsImported(project, psiJavaFile, returnType.getJavaClass());
            returnClass = PsiType.getTypeByName(returnType.getJavaClass(), project, GlobalSearchScope.allScope(project));
        }

        PsiMethod method = JavaPsiFacade.getInstance(project).getElementFactory().createMethod(name, isReturnTypeArray ? returnClass.createArrayType() : returnClass);
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);

        LinkedHashMap<String, PsiType> methodParametersMap = new LinkedHashMap<>();

        if(parameterDefinitions != null) {
            for (Map.Entry<String, ThingworxBaseTypes> e : parameterDefinitions.entrySet()) {
                ThingworxBaseTypes parameterType = e.getValue();
                if (parameterType != null && ThingworxBaseTypes.isJavaClassType(parameterType)) {
                    boolean isParameterArray = ThingworxBaseTypes.isJavaArrayType(parameterType);
                    ensureClassIsImported(project, psiJavaFile, parameterType.getJavaClass());
                    PsiType parameter = PsiType.getTypeByName(parameterType.getJavaClass(), project, GlobalSearchScope.allScope(project));
                    methodParametersMap.put(e.getKey(), isParameterArray ? parameter.createArrayType() : parameter);
                }
            }
        }

        PsiParameterList parameters = JavaPsiFacade.getInstance(project)
                .getElementFactory()
                .createParameterList(methodParametersMap.keySet().toArray(new String[0]), methodParametersMap.values().toArray(new PsiType[0]));
        method.getParameterList().replace(parameters);

        for(PsiParameter param : method.getParameterList().getParameters()) {
            if(param.getModifierList() != null && parameterAnnotation.containsKey(param.getName())) {
                for(PsiAnnotation paramAnnotation : parameterAnnotation.get(param.getName())) {
                    param.getModifierList().addAfter(paramAnnotation, null);
                }
            }
        }

        if(!PsiType.VOID.equals(returnClass)) {
            PsiCodeBlock body = JavaPsiFacade.getInstance(project).getElementFactory().createCodeBlockFromText("{\nreturn null;}", null);
            if(method.getBody() != null) {
                method.getBody().replace(body);
                method = (PsiMethod) CodeStyleManager.getInstance(project).reformat(method);
                JavaCodeStyleManager.getInstance(project).removeRedundantImports(psiJavaFile);
                method = (PsiMethod) JavaCodeStyleManager.getInstance(project).shortenClassReferences(method);
            }
        }


        if(methodAnnotations != null) {
            for (PsiAnnotation annotation : methodAnnotations)
                method.getModifierList().addAfter(annotation, null);
        }

        PsiMethod lastMethod = null;
        PsiMethod[] allMethods = psiClass.getMethods();
        if(allMethods.length > 0){
            lastMethod = allMethods[allMethods.length-1];
        }

        if(lastMethod != null) {
            psiClass.addAfter(method, lastMethod);
        }
        else {
            psiClass.add(method);
        }
        JavaCodeStyleManager.getInstance(project).removeRedundantImports(psiJavaFile);
        return method;
    }

    public static boolean isJavaThing(PsiJavaFile psiJavaFile) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass != null && psiClass.getModifierList() != null) {
            PsiAnnotation[] annotations = psiClass.getModifierList().getAnnotations();
            for(PsiAnnotation annotation : annotations) {
                if(ThingworxConstants.THINGWORX_THING_TEMPLATE_ANNOTATION.equals(annotation.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ThingworxJavaObject getThingworxJavaObjectTpe(PsiJavaFile psiJavaFile) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass != null) {
            MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(psiJavaFile.getProject(), false));
            String className = psiJavaFile.getPackageName().isEmpty() ? psiClass.getName() : psiJavaFile.getPackageName() + "." + psiClass.getName();
            if(className == null || className.isEmpty())
                return ThingworxJavaObject.UNKNOWN;

            if(metadata.isThingTemplate(className))
                return ThingworxJavaObject.THING_TEMPLATE;
            else if (metadata.isThingShape(className))
                return ThingworxJavaObject.THING_SHAPE;
            else if (metadata.isAuthenticator(className))
                return ThingworxJavaObject.AUTHENTICATOR;
            else if (metadata.isDirectoryService(className))
                return ThingworxJavaObject.DIRECOTRY_SERICE;
            else if (metadata.isScriptLibrary(className))
                return ThingworxJavaObject.SCRIPT_LIBRARY;
        }

        return ThingworxJavaObject.UNKNOWN;
    }

    public static boolean isClassMetadataManaged(PsiJavaFile psiJavaFile) {
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiJavaFile, PsiClass.class, true);
        if(psiClass != null) {

            MetadataConfigFile metadata = new MetadataConfigFile(ThingworxProjectUtils.getMetadataFile(psiJavaFile.getProject(), false));

            String className = psiJavaFile.getPackageName().isEmpty() ? psiClass.getName() : psiJavaFile.getPackageName() + "." + psiClass.getName();
            return className != null && !className.isEmpty() && (metadata.isThingTemplate(className) || metadata.isThingShape(className) || metadata.isAuthenticator(className)
                    || metadata.isDirectoryService(className) || metadata.isScriptLibrary(className));
        }
        return false;
    }

}
