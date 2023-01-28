package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonServiceSubOkGenerator extends JsonBaseGenerator{

    private static final String API_FIELD = "api";

    public JsonServiceSubOkGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project,editor,selectedFile,module);
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
        imports("com.jfeat.plugin.api.OkTokenClient");
        imports("com.jfeat.plugin.rest.JsonHttpException");
        imports("com.jfeat.plugin.rest.http.OkJsonClient");
    }

    @Override
    protected void addConstructors() {
        constructor();
        addConstructor("String", "url", String.format("this.%s.endpoint(url);", API_FIELD));
    }

    @Override
    protected void addFields() {
        String apiClassName = getBaseName() + "Api";

        imports(".api." + apiClassName);

        field(apiClassName, API_FIELD, true);
    }

    @Override
    protected void addMethods() {
        // get method
        String getMethodText = "public String get%s(long id) throws JsonHttpException {" +
                "        String api = %s.itemApi(id);" +
                "        OkJsonClient result = new OkTokenClient().url(api).get();" +
                "        return result.execute();" +
                "  }";
        getMethodText = String.format(getMethodText, getBaseName(), API_FIELD);
        method(getMethodText);

        // list method
        String listMethodText = "public String list%s() throws JsonHttpException {" +
                "        String api = %s.listApi();" +
                "        OkJsonClient result = new OkTokenClient().url(api).get();" +
                "        return result.execute();" +
                "    }";
        listMethodText = String.format(listMethodText, getBaseName(), API_FIELD);
        method(listMethodText);

        // add method
        String qualifiedName = getPackageQualifiedName(".rest."+getBaseName()+"Data");
        imports(qualifiedName);

        String addMethodText = "public String add%s(%sRequest data) throws JsonHttpException {" +
                "        String api = %s.listApi();" +
                "        String body = data.toString();" +
                "        OkJsonClient result = new OkTokenClient().url(api).post(body);" +
                "        return result.execute();" +
                "    }";
        addMethodText = String.format(addMethodText, getBaseName(), getBaseName(), API_FIELD);
        method(addMethodText);
    }

    @Override
    protected String getClassName() {
        return getBaseName() + "JsonService";
    }

    @Override
    protected String getTargetPackage() {
        return ".service.ok";
    }
}
