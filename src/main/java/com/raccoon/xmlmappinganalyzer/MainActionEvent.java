package com.raccoon.xmlmappinganalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import groovy.util.logging.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class MainActionEvent extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        XmlMapping xmlMapping = new XmlMapping();
        FindMethodCaller findMethodCaller = new FindMethodCaller();

        // 실행 전 기존 파일 삭제
        // deleteFilesInDirectory();

        Project project = anActionEvent.getProject();

        if (project != null) {

            List<XmlTag> xmlTagList = xmlMapping.getAllXmlTagList(project);
            int noDialog = Messages.showYesNoDialog("XML 파일 내 검색 된 Tag 수 : " + xmlTagList.size(), "저장 하시 겠습니까?", Messages.getQuestionIcon());

            if (noDialog == Messages.NO) {
                return;
            }

            String projectName = Objects.requireNonNull(new File(Objects.requireNonNull(project.getBasePath())).getName());

            List<ReturnDTO> returnDTOS = xmlTagList.stream().map(xmlTag -> {
                String id = xmlTag.getAttributeValue("id");
                String subtag = xmlTag.getName();
                String namespace = ((XmlTagImpl) xmlTag.getParent()).getAttributeValue("namespace");
                String fileName = xmlTag.getContainingFile().getName();
                String filePath = xmlTag.getContainingFile().getVirtualFile().getPath();
                String context = xmlTag.getText().replaceAll("\"", "");
                String moduleName = ModuleUtilCore.findModuleForPsiElement(xmlTag).getName();

                ReturnDTO.ReturnDTOBuilder returnDTOBuilder = ReturnDTO.builder()
                        .projectName(projectName)
                        .moduleName(moduleName)
                        .xmlid(id)
                        .subtag(subtag)
                        .namespace(namespace)
                        .fileName(fileName)
                        .context(context);

                PsiClass psiClass = JavaPsiFacade.getInstance(project)
                        .findClass(Objects.requireNonNull(namespace), GlobalSearchScope.allScope(project));

                if (psiClass != null) {
                    List<ReturnDTO.MethodModels> collect = Arrays.stream(psiClass.findMethodsByName(id, true))
                            .flatMap((PsiMethod method) -> findMethodCaller.findTopCallingMethods(method).stream())
                            .map(method -> {
                                String className = method.getContainingClass().getName();
                                String methodName = method.getName();
                                String url = findMethodCaller.extractUrl(method);
                                return ReturnDTO.MethodModels.builder()
                                        .className(className)
                                        .methodName(methodName)
                                        .url(url)
                                        .build();
                            }).collect(Collectors.toList());
                    returnDTOBuilder
                            .urlCount(collect.size())
                            .methodModels(collect);
                }

                return returnDTOBuilder.build();

            }).collect(Collectors.toList());

//            Log.info("returnDTOS : " + returnDTOS.size());

            String jsonFilePath = saveJsonfile(returnDTOS, projectName);

            Messages.showInfoMessage("ReturnDTO objects have been saved to " + jsonFilePath, "Save Success");

        } else {
            Messages.showInfoMessage("프로젝트가 없습니다.", "Error");
        }
    }

    private String saveJsonfile(List<ReturnDTO> returnDTOS, String projectName) {
        // ObjectMapper 초기화
        ObjectMapper objectMapper = JsonMapper.builder()
                //.configure(SerializationFeature.INDENT_OUTPUT, true) // 보기 좋게 포멧팅
                .build();

        // JSON 파일 경로 설정
        String jsonFilePath = generateSavePath( projectName + ".json" );

        try {

            StringBuffer sb = new StringBuffer();
            for (ReturnDTO returnDTO : returnDTOS) {
                sb.append(objectMapper.writeValueAsString(returnDTO));
                sb.append("\n");
          }
            Files.write(Paths.get(jsonFilePath), sb.toString().getBytes());

//          // returnDTOS를 JSON 문자열로 변환
//            String jsonContent = objectMapper.writeValueAsString(returnDTOS);
//
//            // JSON 파일로 저장
//            Files.write(Paths.get(jsonFilePath), jsonContent.getBytes());

            System.out.println("ReturnDTO objects have been saved to " + jsonFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonFilePath;
    }

    public String generateSavePath(String saveName) {
        Path directoryPath = getXmlSavePath();
        java.io.File directory = directoryPath.toFile();

        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directoryPath.resolve(saveName).toString();
    }
    public void deleteFilesInDirectory() {
        File directory = new File(getXmlSavePath().toString());
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(); // 디렉토리 내의 모든 파일을 가져옵니다.
            if (files != null) {
                for (File file : files) {
                    file.delete(); // 각 파일을 삭제합니다.
                }
            }
        }
    }

    @NotNull
    public Path getXmlSavePath() {
        return Paths.get(System.getProperty("user.home"), "csv-table-save");
    }
}
