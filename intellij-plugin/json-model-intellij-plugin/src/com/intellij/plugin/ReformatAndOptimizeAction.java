package com.intellij.plugin;

/**
 * Created by vincent on 2016/5/15.
 */
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class ReformatAndOptimizeAction extends EditorAction {
    protected ReformatAndOptimizeAction() {
        super(new EditorWriteActionHandler() {
            public void executeWriteAction(Editor editor, Caret caret, final DataContext dataContext) {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        Project project = (Project)dataContext.getData(DataKeys.PROJECT.getName());
                        if(project != null) {
                            PsiFile file = (PsiFile)dataContext.getData(DataKeys.PSI_FILE.getName());
                            if(file != null && file.isWritable()) {
                                PsiDocumentManager.getInstance(project).commitAllDocuments();
                                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                                if(codeStyleManager != null) {
                                    codeStyleManager.reformat(file);
                                }

                                ImportOptimizer importOptimizer = LanguageImportStatements.INSTANCE.forFile(file)
                                        .iterator().next();
                                if(importOptimizer != null) {
                                    importOptimizer.processFile(file).run();
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}
