package com.intellij.plugin;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.util.ProjectHelper;
import com.intellij.psi.*;
import com.sun.istack.internal.NotNull;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonModelTest extends JsonBaseTest {
    ProjectHelper projectHelper = new ProjectHelper();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //projectHelper.tryCreateFolder(myFixture.getProject(), "project1/src/com/example/domain/model");

        //IdeaTestApplication.getInstance().setDataProvider(new TestDataProvider(getProject()));
    }

    public void _testAction() {
        JsonModelAction action = new JsonModelAction();
        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
                projectHelper.tryCreateFolder(myFixture.getProject(), "src/com/example/domain/model");
                myFixture.testAction(action);
            }
        }.execute();
    }

    public void testJsonModelGenerator() {

        new WriteCommandAction(myFixture.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {

                projectHelper.tryCreateFolder(myFixture.getProject(), "src/com/example/domain/model");

                Project project = myFixture.getProject();
                Module module = myFixture.getModule();
                Editor editor = myFixture.getEditor();
                VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

                JsonModelGenerator generator = new JsonModelGenerator(project,null,virtualFile,module);
                generator.generateJavaFile();

                if(true){
                    return;
                }

                PsiClass clss = generator.getGeneratedClass();
                PsiField[] psiFields = clss.getFields();
                for (PsiField field : psiFields) {
                    PsiIdentifier identifier = field.getNameIdentifier();
                    String name = field.getName();
                    PsiIdentifier id = field.getNameIdentifier();
                    PsiModifierList list = field.getModifierList();

                    ASTNode node = field.getNode();
                }

                PsiReferenceList referenceList = clss.getImplementsList();
                PsiElement element = clss.getContext();
                String text = element.getText();
                System.out.println(text);

            }
        }.execute();
    }
}
