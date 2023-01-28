package com.intellij.plugin.json.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.json.*;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonDomainGeneratorGroup {
    private Project project;
    private Editor editor;
    private Module module;
    private VirtualFile selectedFile;

    public JsonDomainGeneratorGroup(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        this.project = project;
        this.module = module;
        this.editor = editor;
        this.selectedFile = selectedFile;
    }

    public void generateGroup(){
        try {
            // api
            BaseGenerator generator = new JsonApiGenerator(project, editor, selectedFile, module);
            generator.generateJavaFile();

            JsonGeneratorGroup generatorGroup;

            // model
            generator = new JsonModelGenerator(project, editor, selectedFile, module);
            generator.generateJavaFile();

            // reset group
            generatorGroup = new JsonRestGeneratorGroup(project, editor, selectedFile, module);
            generatorGroup.generateJavaFile();

            // service group
            generatorGroup = new JsonServiceGeneratorGroup(project, editor, selectedFile, module);
            generatorGroup.generateJavaFile();


            generator.reformat(project);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
