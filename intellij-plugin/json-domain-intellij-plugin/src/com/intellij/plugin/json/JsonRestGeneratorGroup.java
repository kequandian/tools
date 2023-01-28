package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonRestGeneratorGroup implements JsonGeneratorGroup{
    JsonRestSubReqGenerator reqGenerator;
    JsonRestSubRespGenerator respGenerator;
    JsonRestSubListRespGenerator listRespGenerator;
    JsonRestSubDataGenerator dataGenerator;

    public JsonRestGeneratorGroup(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        reqGenerator = new JsonRestSubReqGenerator(project, editor, selectedFile, module);
        respGenerator = new JsonRestSubRespGenerator(project, editor, selectedFile, module);
        listRespGenerator = new JsonRestSubListRespGenerator(project, editor, selectedFile, module);
        dataGenerator = new JsonRestSubDataGenerator(project, editor, selectedFile, module);
    }

    @Override
    public void generateJavaFile() throws IOException {
        reqGenerator.generateJavaFile();
        respGenerator.generateJavaFile();
        listRespGenerator.generateJavaFile();
        dataGenerator.generateJavaFile();
    }
}
