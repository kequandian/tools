package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonRestSubRespGenerator extends JsonBaseGenerator{

    public JsonRestSubRespGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module);
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "Response";
    }

    @Override
    protected String getTargetPackage() {
        return ".rest";
    }

    @Override
    protected String extend() {
        return "com.jfeat.plugin.api.StatusResponse";
    }

    @Override
    protected String implement() {
        return null;
    }

    @Override
    protected void addImports() {
        imports(getModelQualifiedName(getBaseName()));
    }

    @Override
    protected void addConstructors() {
    }

    @Override
    protected void addFields() {
        fieldGetterSetter(getBaseName(), "data");
    }

    @Override
    protected void addMethods() {

    }

}
