package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonServiceSubServiceGenerator extends JsonBaseGenerator{

    public JsonServiceSubServiceGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module, false);
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
        imports("retrofit.Callback");
    }

    @Override
    protected void addConstructors() {

    }

    @Override
    protected void addFields() {

    }

    @Override
    protected void addMethods() {
        method(String.format("void get%s(long id, Callback<String> cb);",getBaseName()));
        method(String.format("void list%s(Callback<String> cb);",getBaseName()));
        method(String.format("void list%s(int page, int pageSize, Callback<String> cb);", getBaseName()));

        String qualifiedName = getPackageQualifiedName(String.format(".rest.%sData",getBaseName()));
        imports(qualifiedName);
        method(String.format("void add%s(%sData data, Callback<String> cb);",getBaseName()));
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "Service";
    }

    @Override
    protected String getTargetPackage() {
        return ".service";
    }

}
