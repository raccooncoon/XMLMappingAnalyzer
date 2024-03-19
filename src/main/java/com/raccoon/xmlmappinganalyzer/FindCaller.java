package com.raccoon.xmlmappinganalyzer;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.HashSet;
import java.util.Set;


public class FindCaller  {

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
}
