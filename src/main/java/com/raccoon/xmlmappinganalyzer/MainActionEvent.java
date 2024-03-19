package com.raccoon.xmlmappinganalyzer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActionEvent extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        XmlMapping xmlMapping = new XmlMapping();
        FindMethodCaller findMethodCaller = new FindMethodCaller();

        // 실행 전 기존 파일 삭제
        deleteFilesInDirectory(getXmlSavePath());

        Project project = anActionEvent.getProject();

        if (project != null) {

            List<XmlTag> xmlTagList = xmlMapping.getAllXmlTagList(project);
            System.out.println("xmlTagList.size() = " + xmlTagList.size());

            xmlTagList.forEach(xmlTag -> {
                String id = xmlTag.getAttributeValue("id");
                String subtag = xmlTag.getName();
                String namespace = ((XmlTagImpl) xmlTag.getParent()).getAttributeValue("namespace");
                String fileName = xmlTag.getContainingFile().getName();
                //String filePath = xmlTag.getContainingFile().getVirtualFile().getPath();
                String context = xmlTag.getText().replaceAll("\"", "");
                String moduleName = ModuleUtilCore.findModuleForPsiElement(xmlTag).getName();

                // 저장 하기 (xml list table)
                saveCsvFile(Arrays.asList(moduleName, id, subtag, namespace, fileName, context), "./xml_list.csv");

                PsiClass psiClass = JavaPsiFacade.getInstance(project)
                        .findClass(Objects.requireNonNull(namespace), GlobalSearchScope.allScope(project));

                if (psiClass != null) {
                    Arrays.stream(psiClass.findMethodsByName(id, true))
                            .flatMap((PsiMethod method) -> findMethodCaller.findTopCallingMethods(method).stream())
                            .forEach(method -> {
                                String className = method.getContainingClass().getName();
                                String methodName = method.getName();
                                String url = findMethodCaller.extractUrl(method);

//                                System.out.println("moduleName = " + moduleName);
//                                System.out.println("id = " + id);
//                                System.out.println("namespace = " + namespace);
//                                System.out.println("fileName = " + fileName);
//                                System.out.println("url = " + url);
//                                System.out.println("className = " + className);
//                                System.out.println("methodName = " + methodName);

                                // 저장 하기 (method list table)
                                // id, namespace, fileName
                                saveCsvFile(Arrays.asList(moduleName, id, namespace, className, methodName, url), "./method_list.csv");
                            });
                }
            });
        }
    }

    public void saveCsvFile(List<String> items, String saveFileName) {
        try (FileWriter writer = new FileWriter(generateSavePath(saveFileName), true)) {

            // Using String.format with a list
            String result = String.format(items.stream()
                    .map(item -> "\"%s\"")
                    .collect(Collectors.joining(",")), items.toArray());

            writer.append(result);
            writer.append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateSavePath(String saveName) {
        Path directoryPath = getXmlSavePath();
        java.io.File directory = directoryPath.toFile();

        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directoryPath.resolve(saveName).toString();
    }

    @NotNull
    public Path getXmlSavePath() {
        return Paths.get(System.getProperty("user.home"), "csv-table-save");
    }

    public void deleteFilesInDirectory(Path directoryPath) {
        File directory = new File(directoryPath.toString());
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(); // 디렉토리 내의 모든 파일을 가져옵니다.
            if (files != null) {
                for (File file : files) {
                    file.delete(); // 각 파일을 삭제합니다.
                }
            }
        }
    }
}
