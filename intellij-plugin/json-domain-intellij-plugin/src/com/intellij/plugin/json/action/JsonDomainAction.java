package com.intellij.plugin.json.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by vincent on 2016/4/4.
 * ReplaceFullyQualifiedNameWithImportIntention
 * https://github.com/JetBrains/intellij-community/blob/10a4c91dbfca935b4b5531003fd8c13b56e66202/plugins/IntentionPowerPak/src/com/siyeh/ipp/fqnames/ReplaceFullyQualifiedNameWithImportIntention.java
 */
public class JsonDomainAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if(project==null) {
            project = (Project) event.getData(PlatformDataKeys.PROJECT);
        }
        Editor editor = (Editor)event.getData(PlatformDataKeys.EDITOR);
        VirtualFile selectedFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(selectedFile==null) {
            selectedFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        }
        Module module = event.getData(LangDataKeys.MODULE);

        JsonDomainGeneratorGroup generator =
                new JsonDomainGeneratorGroup(project, editor, selectedFile, module);
        try {
            generator.generateGroup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
