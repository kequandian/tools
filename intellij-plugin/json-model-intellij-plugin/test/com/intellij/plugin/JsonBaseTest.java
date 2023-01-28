package com.intellij.plugin;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by vincent on 2016/4/4.
 *
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
}
