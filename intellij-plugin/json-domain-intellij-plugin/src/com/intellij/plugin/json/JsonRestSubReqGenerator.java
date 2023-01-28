package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonRestSubReqGenerator extends JsonBaseGenerator{
    public JsonRestSubReqGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module);
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
        //constructor
        constructor();
        addConstructor(getBaseName()+"Data", "data");
    }

    @Override
    protected void addFields() {
        // getter and setter
        fieldGetterSetter(getBaseName()+"Data", "data");
    }

    @Override
    protected void addMethods() {
        // toString() method
        methodToString();
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "Request";
    }

    @Override
    protected String getTargetPackage() {
        return ".rest";
    }
}
