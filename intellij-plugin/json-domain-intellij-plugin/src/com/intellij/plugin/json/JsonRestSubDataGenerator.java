package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonRestSubDataGenerator extends JsonBaseGenerator{

    public JsonRestSubDataGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module);
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "Data";
    }

    @Override
    protected String getTargetPackage() {
        return ".rest";
    }

    @Override
    protected String extend() {
        return null;
    }

    @Override
    protected String implement() {
        return null;
    }

    @Override
    protected void addImports() {

    }

    @Override
    protected void addConstructors() {
        constructor();
    }

    @Override
    protected void addFields() {
    }

    @Override
    protected void addMethods() {
        methodToString();
    }

}
