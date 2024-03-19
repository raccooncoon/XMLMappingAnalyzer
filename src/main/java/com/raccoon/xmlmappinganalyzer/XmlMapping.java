package com.raccoon.xmlmappinganalyzer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class XmlMapping extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        FindCaller findCaller = new FindCaller();

        Project project = e.getProject();
        if (project != null) {
            PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null) {
                int offset = Objects.requireNonNull(e.getData(CommonDataKeys.EDITOR)).getCaretModel().getOffset();
                PsiElement selectedElement = psiFile.findElementAt(offset);
                PsiMethod selectedMethod = PsiTreeUtil.getParentOfType(selectedElement, PsiMethod.class);
                Set<PsiMethod> topCallingMethods = findCaller.findTopCallingMethods(selectedMethod);

                for (PsiMethod method : topCallingMethods) {
                    System.out.println(method.getContainingFile().getVirtualFile().getPath());
                }
            }
        }
    }
}
