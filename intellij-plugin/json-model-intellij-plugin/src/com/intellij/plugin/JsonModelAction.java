package com.intellij.plugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 * ReplaceFullyQualifiedNameWithImportIntention
 * https://github.com/JetBrains/intellij-community/blob/10a4c91dbfca935b4b5531003fd8c13b56e66202/plugins/IntentionPowerPak/src/com/siyeh/ipp/fqnames/ReplaceFullyQualifiedNameWithImportIntention.java
 */
public class JsonModelAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if(project==null) {
            project = event.getData(PlatformDataKeys.PROJECT);
        }
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        VirtualFile selectedFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(selectedFile==null) {
            selectedFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        }
        Module module = event.getData(LangDataKeys.MODULE);


        JsonModelGenerator generator = new JsonModelGenerator(project, editor, selectedFile, module);
        try {
            VirtualFile javaFile = generator.generateJavaFile();

            if(false) {

                FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(javaFile);
                if (editors != null && editors.length > 0) {

                    Editor javaEditor = ((TextEditor) editors[0]).getEditor();
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            generator.reformat(editor, javaFile);
                        }
                    });

                    if (false) {
                        new EditorWriteActionHandler() {
                            public void executeWriteAction(Editor editor, Caret caret, final DataContext dataContext) {
                                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                                    public void run() {
                                        generator.reformat(editor, javaFile);
                                    }
                                });
                            }
                        }.executeWriteAction(javaEditor, null, event.getDataContext());
                    }
                }
            } // ignore

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
