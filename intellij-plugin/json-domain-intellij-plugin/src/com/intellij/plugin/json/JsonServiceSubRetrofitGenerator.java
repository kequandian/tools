package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonServiceSubRetrofitGenerator extends JsonBaseGenerator {
    public JsonServiceSubRetrofitGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
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
        imports("com.jfeat.plugin.rest.http.retrofit.TypedJsonString");
        imports("retrofit.Callback");
        imports("retrofit.http.Body");
        imports("retrofit.http.GET");
        imports("retrofit.http.Header");
        imports("retrofit.http.POST");
        imports("retrofit.http.Path");
        imports("retrofit.http.Query");
    }

    @Override
    protected void addConstructors() {

    }

    @Override
    protected void addFields() {

    }

    @Override
    protected void addMethods() {

        // get method
        String getMethodText = "@GET(\"/%s/{id}\")" +
                "    void get%s(@Header(\"Authorization\") String token," +
                "                    @Path(\"id\") long id," +
                "                    Callback<String> cb);";
        getMethodText = String.format(getMethodText, getBaseName(), StringUtil.toLowerCase(getBaseName()));
        method(getMethodText);

        // list method
        String listMethodText = "@GET(\"/%s\")\n" +
                "    void list%s(@Header(\"Authorization\") String token,\n" +
                "                       Callback<String> cb);";
        listMethodText = String.format(listMethodText, StringUtil.toLowerCase(getBaseName()), getBaseName());
        method(listMethodText);

        // list method
        listMethodText = "@GET(\"/%s\")\n" +
                "    void list%s(@Header(\"Authorization\") String token,\n" +
                "                    @Query(\"PageNumber\") int page,\n" +
                "                    @Query(\"PageSize\") int pageSize,\n" +
                "                    Callback<String> cb);";
        listMethodText = String.format(listMethodText, StringUtil.toLowerCase(getBaseName()), getBaseName());
        method(listMethodText);


        // add method
        String addMethodText = "@POST(\"/%s\")\n" +
                "    void add%s(@Header(\"Authorization\") String token,\n" +
                "               @Body TypedJsonString body,\n" +
                "               Callback<String> cb);";
        addMethodText = String.format(addMethodText, StringUtil.toLowerCase(getBaseName()), getBaseName());
        method(addMethodText);
    }

    @Override
    protected String getClassName() {
        return String.format("Retrofit%sService", getBaseName());
    }

    @Override
    protected String getTargetPackage() {
        return ".service.retrofit";
    }
}
