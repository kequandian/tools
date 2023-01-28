package com.intellij.plugin.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by vincent on 2016/5/13.
 */
public class EditorHelper {
    public String getContents(Project project, Editor editor, VirtualFile file) throws IOException {
        editor = getEditor(project, editor, file);

        if(editor!=null){
            return getText(editor);
        }

        InputStream in;
        //if (editor != null) {
        //    in = new ByteArrayInputStream(getText(editor).getBytes());
        //} else {
        //    in = file.getInputStream();
        //}
        in = file.getInputStream();

        int count = in.available();
        byte[] bytes = new byte[count];
        in.read(bytes);

        return new String(bytes);
    }

    private Editor getEditor(Project project, Editor editor, VirtualFile file) {
        if (editor == null) {
            TextEditor textEditor = getTextEditor(project, file);
            if (textEditor != null) {
                return textEditor.getEditor();
            }
        }
        return editor;
    }

    private TextEditor getTextEditor(Project project, VirtualFile file) {
        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
        if (fileEditor instanceof TextEditor) {
            return (TextEditor) fileEditor;
        }
        return null;
    }

    private String getText(Editor editor) {
        return editor.getDocument().getText();
    }
}
