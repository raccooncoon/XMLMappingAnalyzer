package com.raccoon.xmlmappinganalyzer;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class FindMethodCaller {

    //메소드를 호출 하는 최상위 메소드 모두 검색
    public Set<PsiMethod> findTopCallingMethods(PsiMethod method) {
        Set<PsiMethod> topCallingMethods = new HashSet<>();
        findTopCallingMethodsRecursively(method, topCallingMethods, new HashSet<>());
        return topCallingMethods;
    }

    private void findTopCallingMethodsRecursively(PsiMethod method, Set<PsiMethod> topCallingMethods, Set<PsiMethod> calledMethods) {
        if (method == null || calledMethods.contains(method)) {
            return;
        }

        calledMethods.add(method);

        ReferencesSearch.search(method).findAll().forEach(reference -> {
            PsiMethod callerMethod = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class);
            if (callerMethod != null) {
                findTopCallingMethodsRecursively(callerMethod, topCallingMethods, calledMethods);
            }
        });

        // If no references were found, it means this is a top-level method
        if (ReferencesSearch.search(method).findAll().isEmpty()) {
            topCallingMethods.add(method);
        }
    }

    public String extractUrl( PsiMethod psiMethod) {
        String methodUrl = extractUrlFromAnnotation(psiMethod.getModifierList());
        String classUrl = extractUrlFromAnnotation(psiMethod.getContainingClass().getModifierList());
        // 클래스 URL 과 메서드 URL 을 결합
        return classUrl + methodUrl;
    }

    private String extractUrlFromAnnotation(PsiModifierList modifierList) {
        if (modifierList == null) {
            return "";
        }

        String[] annotationNames = {
                "Controller",
                "RestController",
                "RequestMapping",
                "GetMapping",
                "PostMapping",
                "PutMapping",
                "DeleteMapping",
                "PatchMapping",
                "org.springframework.web.bind.annotation.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.DeleteMapping",
                "org.springframework.web.bind.annotation.PatchMapping"
        };

        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (Arrays.asList(annotationNames).contains(qualifiedName)) {
                PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
                if (value != null) {
                    String text = value.getText();
                    return text.replaceAll("^\"|\"$", "");
                }
            }
        }
        return "";
    }
}
