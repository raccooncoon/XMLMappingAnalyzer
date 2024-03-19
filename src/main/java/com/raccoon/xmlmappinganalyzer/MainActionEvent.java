package com.raccoon.xmlmappinganalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.nio.file.Files;
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

            List<ReturnDTO> returnDTOS = xmlTagList.stream().map(xmlTag -> {
                String id = xmlTag.getAttributeValue("id");
                String subtag = xmlTag.getName();
                String namespace = ((XmlTagImpl) xmlTag.getParent()).getAttributeValue("namespace");
                String fileName = xmlTag.getContainingFile().getName();
                String filePath = xmlTag.getContainingFile().getVirtualFile().getPath();
                String context = xmlTag.getText().replaceAll("\"", "");
                String moduleName = ModuleUtilCore.findModuleForPsiElement(xmlTag).getName();

                System.out.println("id = " + id);

                // 저장 하기 (xml list table)
                // saveCsvFile(Arrays.asList(moduleName, id, subtag, namespace, fileName, context), "./xml_list.csv");

                ReturnDTO.ReturnDTOBuilder returnDTOBuilder = ReturnDTO.builder()
                        .moduleName(moduleName)
                        .id(id)
                        .subtag(subtag)
                        .namespace(namespace)
                        .fileName(fileName)
                        .filePath(filePath)
                        .context(context);

                PsiClass psiClass = JavaPsiFacade.getInstance(project)
                        .findClass(Objects.requireNonNull(namespace), GlobalSearchScope.allScope(project));

                if (psiClass != null) {
                    List<ReturnDTO.TopCaller> collect = Arrays.stream(psiClass.findMethodsByName(id, true))
                            .flatMap((PsiMethod method) -> findMethodCaller.findTopCallingMethods(method).stream())
                            .map(method -> {
                                String className = method.getContainingClass().getName();
                                String methodName = method.getName();
                                String url = findMethodCaller.extractUrl(method);
                                return ReturnDTO.TopCaller.builder()
                                        .filePath(method.getContainingFile().getVirtualFile().getPath())
                                        .methodName(className + "." + methodName)
                                        .url(url)
                                        .build();
                            }).collect(Collectors.toList());
                    returnDTOBuilder.topCallingMethods(collect);
                }

                return returnDTOBuilder.build();

            }).collect(Collectors.toList());

            saveJsonfile(returnDTOS);

        }
    }

    private void saveJsonfile(List<ReturnDTO> returnDTOS) {
        // ObjectMapper 초기화
        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .build();

        // JSON 파일 경로 설정
        String jsonFilePath = generateSavePath("xml_list.json");

        try {
            // returnDTOS를 JSON 문자열로 변환
            String jsonContent = objectMapper.writeValueAsString(returnDTOS);

            // JSON 파일로 저장
            Files.write(Paths.get(jsonFilePath), jsonContent.getBytes());

            System.out.println("ReturnDTO objects have been saved to " + jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
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
