package com.raccoon.xmlmappinganalyzer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.*;
import java.util.stream.Collectors;

public class XmlMapping {

    public List<XmlTag> getAllXmlTagList(Project project) {
        return findAllXmlFiles(project).stream()
                .filter(xmlFile -> Objects.requireNonNull(xmlFile.getRootTag()).getName().equals("mapper"))
                .flatMap(xmlFile -> Objects.requireNonNull(Arrays.stream(xmlFile.getRootTag().getSubTags())))
                .filter(xmlTag -> List.of("insert", "update", "delete","select").contains(xmlTag.getName()))
                .collect(Collectors.toList());

            /*PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null) {
                int offset = Objects.requireNonNull(e.getData(CommonDataKeys.EDITOR)).getCaretModel().getOffset();
                PsiElement selectedElement = psiFile.findElementAt(offset);
                PsiMethod selectedMethod = PsiTreeUtil.getParentOfType(selectedElement, PsiMethod.class);
                Set<PsiMethod> topCallingMethods = findCaller.findTopCallingMethods(selectedMethod);

                for (PsiMethod method : topCallingMethods) {
                    System.out.println(method.getContainingFile().getVirtualFile().getPath());
                }
            }*/
    }

    private List<XmlFile> findAllXmlFiles(Project project) {
        List<XmlFile> xmlFiles = new ArrayList<>();

        // 모든 XML 파일을 찾기 위해 프로젝트 내의 모든 XML 파일 이름을 검색
        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "xml", GlobalSearchScope.allScope(project));

        PsiManager psiManager = PsiManager.getInstance(project);

        // 각 XML 파일에 대한 PsiFile 객체 얻기
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile instanceof XmlFile) {
                xmlFiles.add((XmlFile) psiFile);
            }
        }
        return xmlFiles;
    }
}
