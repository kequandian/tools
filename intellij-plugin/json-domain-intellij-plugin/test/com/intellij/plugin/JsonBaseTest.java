package com.intellij.plugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by vincent on 2016/4/4.
 *
 *  *PsiPackage myTargetPackage = JavaDirectoryService.getInstance().getPackage(directory);
 * https://github.com/JetBrains/intellij-community/blob/10a4c91dbfca935b4b5531003fd8c13b56e66202/java/java-analysis-impl/src/com/intellij/codeInspection/wrongPackageStatement/AdjustPackageNameFix.java

 PsiJavaFile myFile = (PsiJavaFile) mClass.getContainingFile();
 PsiPackageStatement packageStatement = elementFactory.createPackageStatement("com.example");
 PsiPackageStatement myStatement = ((PsiJavaFile)myFile).getPackageStatement();
 if(myStatement==null) {
 myFile.addAfter(packageStatement, null);
 }
 PsiImportList importList = myFile.getImportList();
 String packageName = myFile.getPackageName();


 //VirtualFile virtualFile= null;
 //PsiFile file = myFixture.getFile();
 //if(file!=null) {
 //    virtualFile = file.getVirtualFile();
 //}
 //FileType fileType = virtualFile.getFileType();
 //String fileName = virtualFile.getName();
 //File e = new File(myFixture.getTestDataPath() + "//" + filePath);
 //String fileText = FileUtilRt.loadFile(e, "UTF-8", true);
 //psiJsonFile = PsiFileFactory.getInstance(myFixture.getProject())
 //.createFileFromText(JsonFileType.DEFAULT_EXTENSION, JsonFileType.INSTANCE, fileText);
 //psiJsonObject = (JsonObject) psiJsonFile.getFirstChild();
 */
public class JsonBaseTest extends LightCodeInsightFixtureTestCase {
    final String dataPath =  System.getProperty("user.dir") + "\\testData";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myFixture.setTestDataPath(dataPath);

        String filePath = "assets/json/device.json";
        myFixture.configureByFiles(filePath);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    protected void releaseEditor(Editor editor){
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
