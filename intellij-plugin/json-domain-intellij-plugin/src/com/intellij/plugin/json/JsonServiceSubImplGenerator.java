package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonServiceSubImplGenerator extends JsonBaseGenerator{

    public JsonServiceSubImplGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project,editor,selectedFile,module);
    }

    @Override
    protected String extend() {
        return "com.jfeat.plugin.api.factory.AbstractService";
    }

    @Override
    protected String implement() {
        String packageQualifiedName = getPackageQualifiedName(String.format(".service.%sService", getBaseName()));
        return packageQualifiedName;
    }

    @Override
    protected void addImports() {
        imports("import com.jfeat.plugin.api.LoginSession");
        imports("import com.jfeat.plugin.api.factory.AbstractService");
        imports("com.jfeat.plugin.rest.http.retrofit.TypedJsonString");
        imports("retrofit.Callback");
    }

    @Override
    protected void addConstructors() {
        constructor();
    }

    @Override
    protected void addFields() {
        imports(String.format(".service.retrofit.Retrofit%sService", getBaseName()));

        String type = String.format("Retrofit%sService", getBaseName());
        field(type, "retrofit");
    }

    @Override
    protected void addMethods() {
        // init
        String initText = String.format("@Override\n" +
                "    public void init() {\n" +
                "        this.retrofit = this.createRetrofitService(Retrofit%sService.class);\n" +
                "    }", getBaseName());
        addMethodFromText(initText);

        // getMethod
        String getMethod = String.format("@Override\n" +
                "    public void get%s(long id, Callback<String> cb) {\n" +
                "        retrofit.get%s(LoginSession.me().getAccessToken(), id, cb);\n" +
                "    }", getBaseName());
        addMethodFromText(getMethod);

        // list
        String listMethod = String.format("@Override\n" +
                "    public void list%s(Callback<String> cb) {\n" +
                "        retrofit.list%s(LoginSession.me().getAccessToken(), cb);\n" +
                "    }", getBaseName());
        addMethodFromText(listMethod);

        // page
        listMethod = String.format("@Override\n" +
                "    public void list%s(int page, int pageSize, Callback<String> cb) {\n" +
                "        retrofit.list%s(LoginSession.me().getAccessToken(), page, pageSize, cb);\n" +
                "    }", getBaseName());
        addMethodFromText(listMethod);

        // addMethod
        String addMethod = String.format("@Override\n" +
                "    public void add%s(%sData data, Callback<String> cb) {\n" +
                "        String request = data.toString();\n" +
                "        TypedJsonString body = new TypedJsonString(request);\n" +
                "        retrofit.add%s(LoginSession.me().getAccessToken(), body, cb);\n" +
                "    }",getBaseName());
        addMethodFromText(addMethod);
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "ServiceImpl";
    }

    @Override
    protected String getTargetPackage() {
        return ".service.impl";
    }
}
