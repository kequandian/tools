package com.intellij.plugin;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.json.JsonApiGenerator;
import com.intellij.plugin.json.JsonModelGenerator;
import com.intellij.plugin.json.JsonRestGeneratorGroup;
import com.intellij.plugin.json.JsonServiceGeneratorGroup;
import com.intellij.plugin.json.action.JsonDomainAction;
import com.intellij.plugin.json.util.ProjectHelper;
import com.sun.istack.internal.NotNull;

/**
 * Created by vincent on 2016/4/4.
 * Replace qualified name with imports
 * ReplaceFullyQualifiedNameWithImportIntention
 * https://github.com/JetBrains/intellij-community/blob/10a4c91dbfca935b4b5531003fd8c13b56e66202/plugins/IntentionPowerPak/src/com/siyeh/ipp/fqnames/ReplaceFullyQualifiedNameWithImportIntention.java
 * ImportUtils
 * https://github.com/JetBrains/intellij-community/blob/10a4c91dbfca935b4b5531003fd8c13b56e66202/plugins/InspectionGadgets/InspectionGadgetsAnalysis/src/com/siyeh/ig/psiutils/ImportUtils.java
 */
public class JsonModelTest extends JsonBaseTest {
    ProjectHelper projectHelper = new ProjectHelper();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //if (!FileModificationService.getInstance().prepareFileForWrite(myFile)) return;

        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Project project = myFixture.getProject();

                VirtualFile srcDir = null;
                try {
                    srcDir = project.getBaseDir().findFileByRelativePath("src");
                    srcDir.delete(project);
                }catch (Exception e){
                }

                srcDir = projectHelper.findOrCreateSourceDir(project);
                projectHelper.cleanDirectory(myFixture.getProject(), srcDir);
                projectHelper.findOrCreateDirectory(project, srcDir, "com/domain");
            }
        }.execute();
    }

    public void _testAction() {
        JsonDomainAction action = new JsonDomainAction();
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
                myFixture.testAction(action);
            }
        }.execute();
    }

    public void _testJsonApiGenerator(){
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Project project = myFixture.getProject();
                Module module = myFixture.getModule();
                Editor editor = myFixture.getEditor();
                VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

                JsonApiGenerator api = new JsonApiGenerator(project, editor, virtualFile, module);
                VirtualFile createdFile = api.generateJavaFile();
                api.reformat(null, createdFile);

                String content = api.getContent();
                System.out.print(content);

                // release the editor
                releaseEditor(editor);
            }
        }.execute();
    }

    public void _testJsonModelGenerator(){
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Project project = myFixture.getProject();
                Module module = myFixture.getModule();
                Editor editor = myFixture.getEditor();
                VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

                JsonModelGenerator generator = new JsonModelGenerator(project,editor,virtualFile,module);
                generator.generateJavaFile();

                String content = generator.getContent();
                System.out.print(content);

            }
        }.execute();
    }
    public void testJsonRestGeneratorGroup(){
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Project project = myFixture.getProject();
                Module module = myFixture.getModule();
                Editor editor = myFixture.getEditor();
                VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

                JsonRestGeneratorGroup group = new JsonRestGeneratorGroup(project, editor, virtualFile, module);
                group.generateJavaFile();
            }
        }.execute();
    }
    public void _testJsonServiceSubImplGenerator(){
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Project project = myFixture.getProject();
                Module module = myFixture.getModule();
                Editor editor = myFixture.getEditor();
                VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

                JsonServiceGeneratorGroup api = new JsonServiceGeneratorGroup(project, editor, virtualFile, module);
                api.generateJavaFile();
            }

        }.execute();
    }
}
