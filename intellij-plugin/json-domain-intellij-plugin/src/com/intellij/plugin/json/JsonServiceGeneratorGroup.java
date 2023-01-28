package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonServiceGeneratorGroup implements JsonGeneratorGroup{
    JsonServiceSubImplGenerator implGen;
    JsonServiceSubOkGenerator okGen;
    JsonServiceSubRetrofitGenerator retrofitGen;
    JsonServiceSubServiceGenerator serviceGen;

    public JsonServiceGeneratorGroup(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        implGen = new JsonServiceSubImplGenerator(project, editor, selectedFile, module);
        okGen = new JsonServiceSubOkGenerator(project, editor, selectedFile, module);
        retrofitGen = new JsonServiceSubRetrofitGenerator(project, editor, selectedFile, module);
        serviceGen = new JsonServiceSubServiceGenerator(project, editor, selectedFile, module);
    }

    @Override
    public void generateJavaFile() throws IOException {
        implGen.generateJavaFile();
        okGen.generateJavaFile();
        retrofitGen.generateJavaFile();
        serviceGen.generateJavaFile();
    }
}
