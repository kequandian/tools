package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/8/2.
 */
public abstract class JsonBaseGenerator extends BaseGenerator{

    public JsonBaseGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        this(project, editor, selectedFile, module, true);
    }

    public JsonBaseGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module, boolean isCls) {
        super(project, editor, selectedFile, module, isCls);

        generate();
    }

    abstract protected String extend();
    abstract protected String implement();

    // create class
    abstract protected void addImports();
    abstract protected void addConstructors();
    abstract protected void addFields();
    abstract protected void addMethods();


    @Override
    protected void generate(){
        // append definition comment
        // /**
        //  * Created by intellij plugin JsonGenerator on 2016/4/4.
        //  */
        definition();

        // extends
        if(!StringUtil.isEmpty(extend()) && StringUtil.isEmpty(implement())){
            extendsClassWithQualifiedName(extend());
        }

        // implements
        else if(!StringUtil.isEmpty(implement()) && StringUtil.isEmpty(extend())){
            implementsClassWithQualifiedName(implement());
        }

        else if(!StringUtil.isEmpty(extend()) && !StringUtil.isEmpty(implement())){
            throw new RuntimeException("extends and implements are both provided, not allowed!");
        }

        addImports();
        addConstructors();
        addFields();
        addMethods();
    }

    protected void methodToString(){
        String text = "@Override" +
                "    public String toString(){" +
                "        return new com.google.gson.Gson().toJson(this);" +
                "    }";
        method(text);
    }

    protected String getModelQualifiedName(String modelName){
        return getPackageQualifiedName(".model."+modelName);
    }
}
