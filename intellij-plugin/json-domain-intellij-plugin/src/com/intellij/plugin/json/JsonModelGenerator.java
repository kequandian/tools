package com.intellij.plugin.json;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.json.util.WriterUtil;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonModelGenerator extends BaseGenerator {

    String jsonText;

    public JsonModelGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        super(project, editor, selectedFile, module);

        try {
            jsonText = getEditorContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        generate();
    }

    @Override
    protected String getClassName() {
        return getBaseName();
    }

    @Override
    protected String getTargetPackage() {
        return ".model";
    }

    @Override
    protected void generate() {
        new WriterUtil(this.jsonText, this.project, this.mClass).generate();
    }
}
