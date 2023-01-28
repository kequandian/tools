package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonApiGenerator extends JsonBaseGenerator{
    public JsonApiGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module);
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "Api";
    }

    @Override
    protected String getTargetPackage() {
        return ".api";
    }

    @Override
    protected String extend() {
        return "com.jfeat.plugin.api.BaseApi";
    }

    @Override
    protected String implement() {
        return null;
    }

    @Override
    protected void addImports() {

    }

    @Override
    protected void addConstructors(){
        imports(".API");

        String text = String.format("public %s() { endpoint(API.URL); }", getClassName());
        method(text);
    }

    @Override
    protected void addFields(){
        String apiName = StringUtil.toLowerCase(getBaseName());
        String text = String.format("private static final String %s = \"%s\";", getApiPath(), apiName);
        field(text);
    }

    @Override
    protected void addMethods(){
        //imports("com.jfeat.plugin.lang.UrlBuilder");

        createListMethod();
        createListMethodWithPage();
        createItemMethod();
    }

    private void createListMethod(){
        String text = String.format("public String listApi(){\n" +
                "            return new com.jfeat.plugin.lang.UrlBuilder(getUrl())\n" +
                "                    .withEncodedPath(%s)\n" +
                "                    .toString();}", getApiPath());

        method(text);
        //shorten("listApi");
    }

    private void createListMethodWithPage(){
        String text = String.format("public String listApi(int page, int pageSize){\n" +
                "            return new com.jfeat.plugin.lang.UrlBuilder(getUrl())\n" +
                "                    .withEncodedPath(%s)\n" +
                "                    .withEncodedQuery(\"page=\"+page+\"&pageSize=\"+pageSize)" +
                "                    .toString();}", getApiPath());

        method(text);
        //shorten("listApi");
    }

    private void createItemMethod(){
        String text = String.format("public String itemApi(long id){\n" +
                "            return new com.jfeat.plugin.lang.UrlBuilder(getUrl())\n" +
                "                    .withEncodedPath(%s)\n" +
                "                    .withEncodedPath(String.valueOf(id))" +
                "                    .toString();}", getApiPath());

        method(text);
        //shorten("itemApi");
    }

    private String getApiPath(){
        return StringUtil.toUpperCase(getBaseName()) + "_PATH";
    }
}
