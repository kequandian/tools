package com.intellij.plugin.json;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.json.util.EditorHelper;
import com.intellij.plugin.json.util.PackageHelper;
import com.intellij.plugin.json.util.ProjectHelper;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by intellij plugin JsonGenerator on 2016/4/4.
 */
abstract public class BaseGenerator {
    Project project;
    Editor editor;
    Module module;
    VirtualFile selectedFile;

    private ProjectHelper projectHelper = new ProjectHelper();
    private EditorHelper editorHelper = new EditorHelper();
    private PackageHelper packageHelper = new PackageHelper();

    private PsiElementFactory mFactory;

    private String mBaseName;

    PsiFile mFile;
    PsiClass mClass;

    public BaseGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        this.project = project;
        this.module = module;
        this.editor = editor;
        this.selectedFile = selectedFile;

        this.mFactory = JavaPsiFacade.getElementFactory(project);

        initDefault();

        // generate the class
        //generate();
    }

    public BaseGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module, boolean isCls) {
        this.project = project;
        this.module = module;
        this.editor = editor;
        this.selectedFile = selectedFile;

        this.mFactory = JavaPsiFacade.getElementFactory(project);

        if (isCls) {
            initDefault();
        } else {
            initInterface();
        }

        // generate the class
        //generate();
    }

    /**
     * init PsiFile and PsiClass for class
     */
    protected void initDefault() {
        this.mFile = createClassWithName(getClassName());
        this.mClass = ((PsiJavaFile) mFile).getClasses()[0];
    }

    /**
     * init PsiFile and PsiClass for interface
     */
    protected void initInterface() {
        this.mFile = createInterfaceWithName(getClassName());
        this.mClass = ((PsiJavaFile) mFile).getClasses()[0];
    }

    abstract protected String getClassName();

    abstract protected String getTargetPackage();   // support relative package

    abstract protected void generate();

    /**
     * create java model file in project file system
     * and open the java file in editor
     *
     * @throws IOException
     */
    public VirtualFile generateJavaFile() throws IOException {

        // save code to file
        String fileName = mClass.getName() + ".java";

        String finalCode = getContent();

        // add package
        String packageName = getPackageName();
        if (!StringUtil.isEmpty(packageName)) {
            StringBuilder builder = new StringBuilder();
            builder.append("package ");
            builder.append(packageName);
            builder.append(";\n\n");
            builder.append(finalCode);
            finalCode = builder.toString();
        }

        VirtualFile projectDir = projectHelper.findSubProject(project, selectedFile);
        VirtualFile sourceDir = projectHelper.getSourceRootDir(projectDir);
        String sourcePath = projectHelper.getRelativePath(project.getBaseDir(), sourceDir);

        String normalizePath = StringUtils.normalizePath(sourcePath + "/" + StringUtils.getPackageAsPath(packageName));

        VirtualFile createdFile = projectHelper.createOrFindFile(project, fileName, normalizePath);
        projectHelper.setFileContent(project, createdFile, finalCode);
        return createdFile;
    }

    protected void definition() {
        PsiElement root = mClass.getOriginalElement().getFirstChild();
        this.mClass.addBefore(addDefinitionComment(), root);
    }

    protected void imports(String qualifiedName) {
        addImportStatementWithQualifiedName(qualifiedName);
    }

    protected void constructor() {
        addConstructor();
    }

    protected void method(String text) {
        addMethodFromText(text);
    }

    protected void field(String text) {
        addFieldFromText(text);
    }
    protected void field(String type, String identifier){
        String text = String.format("private %s %s;", type, identifier);
        addFieldFromText(text);
    }
    protected void field(String type, String identifier, boolean created){
        if(created){
            String text = String.format("private %s %s = new %s();", type, identifier, type);
            addFieldFromText(text);
        }else{
            field(type, identifier);
        }
    }

    protected void comment(String comment) {
        addCommentFromText(comment);
    }

    protected void fieldGetterSetter(String typeString, String identifier) {
        String text = String.format(String.format("private %s %s;", typeString, identifier));
        PsiField field = addFieldFromText(text);
        getter(field);
        setter(field);
    }

    /**
     * PsiField field = field(String.format("private %s data;", getBaseName()));
     *
     * @param field
     */
    protected void getter(PsiField field) {
        String type = field.getType().getCanonicalText();
        String name = field.getName();

        // create getter
        String getterText = String.format("public %s get%s(){return this.%s;}", type, StringUtil.capitalize(name), name);
        PsiMethod getter = mFactory.createMethodFromText(getterText, null);

        mClass.add(getter);
    }

    /**
     * PsiField field = field(String.format("private %s data;", getBaseName()));
     *
     * @param field
     */
    protected void setter(PsiField field) {
        String type = field.getType().getCanonicalText();
        String name = field.getName();

        // create setter
        String setterText = String.format("public void set%s(%s %s){this.%s=%s;}",
                StringUtil.capitalize(name), type, name, name, name);
        PsiMethod setter = mFactory.createMethodFromText(setterText, null);

        mClass.add(setter);
    }

    protected PsiField addFieldFromText(String text) {
        PsiField field = mFactory.createFieldFromText(text, null);
        mClass.add(field);
        return field;
    }

    protected PsiElement addMethodFromText(String text) {
        return mClass.add(mFactory.createMethodFromText(text, null));
    }

    protected PsiComment addCommentFromText(String comment) {
        PsiComment psiComment = mFactory.createCommentFromText(comment, null);
        mClass.add(psiComment);
        mClass.add(mFactory.createCommentFromText("",null));
        return psiComment;
    }

    protected PsiComment addDefinitionComment() {
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String comment = "/* *\n" +
                " * Created by intellij plugin JsonGenerator on " + dateString + ".\n" +
                " */";
        return mFactory.createCommentFromText(comment, null);
    }

    protected void addConstructor() {
        String text = String.format("public %s(){}", mClass.getName());
        addMethodFromText(text);
    }

    protected void addConstructor(String body) {
        String text = String.format("public %s(){%s}", mClass.getName(), body);
        addMethodFromText(text);
    }

    protected void addConstructor(HashMap<String, String> params, String body) {
        StringBuilder paramString = new StringBuilder();
        for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext(); iter.next()) {
            paramString.append(iter);
            paramString.append(" ");
            paramString.append(params.get(iter));
            paramString.append(",");
        }

        paramString = paramString.deleteCharAt(paramString.length() - 1);

        String text = String.format("public %s(%s){%s}", mClass.getName(), paramString, body);
        addMethodFromText(text);
    }

    protected void addConstructor(String type, String identifier, String body) {
        String params = String.format("%s %s", type, identifier);
        String text = String.format("public %s(%s){%s}", mClass.getName(), params, body);
        addMethodFromText(text);
    }

    protected void addConstructor(String type, String identifier) {
        String params = String.format("%s %s", type, identifier);
        String body = String.format("this.%s=%s;", identifier, identifier);

        String text = String.format("public %s(%s){%s}", mClass.getName(), params, body);
        addMethodFromText(text);
    }

    protected PsiFile createFileWithQualifiedName(String qualifiedName) {
        String name, packageName;

        String text;

        if (!qualifiedName.contains(".")) {
            name = qualifiedName;
            packageName = "";
        } else {
            int ch = qualifiedName.lastIndexOf('.');
            name = qualifiedName.substring(ch + 1);
            packageName = qualifiedName.substring(0, ch);
        }

        PsiFile psiFile = createClassWithName(name);

        if (!StringUtil.isEmpty(packageName)) {
            PsiPackageStatement basePackageStatement = mFactory.createPackageStatement(packageName);
            psiFile.addAfter(basePackageStatement, null);
            //psiFile.add(basePackageStatement);
            text = psiFile.getText();
            if(text!=null){
                text = text.toString();
            }
        }

        return psiFile;
    }

    protected PsiFile createClassWithName(String className) {
        String fileName = className + ".java";
        String originalText = String.format("public class %s{}", className);

        return PsiFileFactory.getInstance(project).createFileFromText(fileName, JavaFileType.INSTANCE, originalText);
    }

    protected PsiFile createInterfaceWithName(String className) {
        String fileName = className + ".java";
        String originalText = String.format("public interface %s{}", className);

        return PsiFileFactory.getInstance(project).createFileFromText(fileName, JavaFileType.INSTANCE, originalText);
    }

    protected PsiFile extendsClass(PsiClass referenceClass) {
        if (mFile instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) mFile;
            PsiImportList importList = javaFile.getImportList();

            PsiImportStatement importStatement = mFactory.createImportStatement(referenceClass);
            importList.add(importStatement);

            PsiClass psiClass = javaFile.getClasses()[0];

            psiClass.getExtendsList().add(mFactory.createClassReferenceElement(referenceClass));

            return javaFile;
        }

        throw new RuntimeException("PsiFile must be an instance of PsiJavaFile.");
    }

    protected void addImportStatementWithQualifiedName(String qualifiedName) {
        if(!(mFile instanceof PsiJavaFile)){
            throw new RuntimeException("mFile is not instance of PsiJavaFile!");
        }

        String qualifiedPackageName = qualifiedName;

        if(qualifiedName.startsWith(".")){
            // means relative package
            qualifiedPackageName = getPackageQualifiedName(qualifiedName);
        }

        PsiFile importFile = createFileWithQualifiedName(qualifiedName);
        PsiImportStatement importStatement = mFactory.createImportStatement(((PsiJavaFile) importFile).getClasses()[0]);

        PsiImportList importList = ((PsiJavaFile) mFile).getImportList();
        importList.add(importStatement);
    }

    protected PsiFile extendsClassWithQualifiedName(String qualifiedName) {
        PsiFile baseFile = createFileWithQualifiedName(qualifiedName);
        return extendsClass(((PsiJavaFile) baseFile).getClasses()[0]);
    }

    protected PsiFile implementsClass(PsiClass referenceClass) {
        if (mFile instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) mFile;
            PsiImportList importList = javaFile.getImportList();

            PsiImportStatement importStatement = mFactory.createImportStatement(referenceClass);
            importList.add(importStatement);

            PsiClass psiClass = javaFile.getClasses()[0];
            psiClass.getImplementsList().add(mFactory.createClassReferenceElement(referenceClass));

            return javaFile;
        }

        throw new RuntimeException("PsiFile must be an instance of PsiJavaFile.");
    }

    protected PsiFile implementsClassWithQualifiedName(String qualifiedName) {
        PsiFile baseFile = createFileWithQualifiedName(qualifiedName);
        return implementsClass(((PsiJavaFile) baseFile).getClasses()[0]);
    }

    protected VirtualFile findOrCreatePackageDirectory(String childPackage) throws IOException {
        if (childPackage.indexOf('.') > 0) {
            throw new RuntimeException("TODO: to support long relative package");
        }

        VirtualFile projectDir = projectHelper.findSubProject(project, selectedFile);

        VirtualFile packageDir = projectHelper.findLastRoot(project, projectDir);

        if (!StringUtil.isEmpty(childPackage)) {
            if (!packageDir.getPath().endsWith("/" + childPackage)) {
                if (packageDir.findChild(childPackage) == null) {
                    packageDir = packageDir.createChildDirectory(project, childPackage);
                } else {
                    packageDir = packageDir.findChild(childPackage);
                }
            }
        }

        return packageDir;
    }

    public VirtualFile writeFile(String childPackage, boolean overrideIfNotExists) throws IOException {
        if (!FileModificationService.getInstance().prepareFileForWrite(mFile)) {
            return null;
        }

        if (false) {
            VirtualFile packageDir = findOrCreatePackageDirectory(childPackage);
            VirtualFile createdFile = packageDir.findOrCreateChildData(project, mFile.getName());

            String finalCode = mFile.getText();

            createdFile.setBinaryContent(finalCode.getBytes());
            FileEditorManager.getInstance(project).openFile(createdFile, true);

            return createdFile;

        } else {

            VirtualFile packageDir = findOrCreatePackageDirectory(childPackage);
            final PsiDirectory directory = PsiManager.getInstance(project).findDirectory(packageDir);
            if (directory == null) return null;

            /// add package
            String qualifiedName = "";
            PsiPackage myTargetPackage = JavaDirectoryService.getInstance().getPackage(directory);
            if (myTargetPackage == null) {
                qualifiedName = packageHelper.getPackageQualifiedName(project, packageDir);
            }

            PsiPackageStatement myStatement = ((PsiJavaFile) mFile).getPackageStatement();

            if (myTargetPackage.getQualifiedName().length() == 0) {
                if (myStatement != null) {
                    myStatement.delete();
                }
            } else {
                qualifiedName = myTargetPackage.getQualifiedName();

                final PsiPackageStatement packageStatement = mFactory.createPackageStatement(qualifiedName);
                if (myStatement != null) {
                    myStatement.getPackageReference().replace(packageStatement.getPackageReference());
                } else {
                    mFile.addAfter(packageStatement, null);
                }
            }

            PsiFile existsFile = directory.findFile(mFile.getName());
            if (existsFile == null) {
                directory.add(mFile);

            } else if (overrideIfNotExists) {
                if (existsFile != null) {
                    existsFile.delete();
                }
                directory.add(mFile);
            }

            PsiFile resultFile = directory.findFile(mFile.getName());
            return resultFile.getVirtualFile();
        }
    }


    public String getPackageName() {
        String packageName = "";

        if (packageHelper.checkManifest(project, module)) {
            packageName = packageHelper.getPackageNameFromManifest(project, module);
        }

        if (StringUtil.isEmpty(packageName)) {
            VirtualFile projectDir = projectHelper.findSubProject(project, selectedFile);
            String subProject = projectHelper.getRelativePath(project.getBaseDir(), projectDir);

            packageName = packageHelper.findPackageName(project, subProject);
        }

        if (!StringUtil.isEmpty(getTargetPackage())) {
            if(getTargetPackage()==null || !getTargetPackage().startsWith(".")){
                throw new RuntimeException("Fatal: target package must start with '.'");
            }
            packageName = packageName + getTargetPackage();
        }

        return packageName;
    }

    protected String getPackageQualifiedName(String relativePackage) {
        if(!(relativePackage.charAt(0)=='.')){
            relativePackage = "." + relativePackage;
        }

        VirtualFile projectDir = projectHelper.findSubProject(project, selectedFile);
        VirtualFile packageDir = projectHelper.findLastRoot(project, projectDir);

        VirtualFile srcDir = projectHelper.findSourceDir(project, projectDir);
        String packagePath = projectHelper.getRelativePath(srcDir, packageDir);

        String qualifiedName = projectHelper.convertPackageFromPath(packagePath);
        if (!StringUtil.isEmpty(relativePackage)) {
            qualifiedName = qualifiedName + relativePackage.replace("/", ".");
        }

        return qualifiedName;
    }

    public String getBaseName(String fileName) {
        int ch = fileName.lastIndexOf('.');
        String className = fileName.substring(0, ch);

        String[] arr = className.split("-|_");
        if (arr.length == 1) {
            className = StringUtil.capitalize(arr[0]);
        } else if (arr.length == 2) {
            className = StringUtil.capitalize(arr[0]) + StringUtil.capitalize(arr[1]);
        }
        return className;
    }

    public String getBaseName() {
        if (StringUtil.isEmpty(mBaseName)) {
            mBaseName = getBaseName(selectedFile.getName());
        }
        return mBaseName;
    }

    public String getLowercaseBaseName() {
        String baseName = getBaseName();
        return StringUtil.toLowerCase(baseName);
    }

    protected void shorten(String methodName) {
        shortenMethod(methodName);
    }

    protected void shortenMethod(String methodName) {
        final PsiMethod[] methods = mClass.findMethodsByName(methodName, false);
        if (methods != null && methods.length < 1) {
            throw new IncorrectOperationException(String.format("Method <%s> was not found", methodName));
        }

        for (int i = 0; i < methods.length; i++) {
            PsiMethod method = methods[i];

            method = (PsiMethod) JavaCodeStyleManager.getInstance(project).shortenClassReferences(method);
            CodeStyleManager.getInstance(project).reformat(method);
        }
    }

    public void reformat(Project project){
        ReformatCodeProcessor processor = new ReformatCodeProcessor(project, false);
        processor.run();
    }

    public void reformat(Editor editor, VirtualFile createdFile) {
        if (editor == null) {
            FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(createdFile);
            if (editors != null) {
                editor = ((TextEditor) editors[0]).getEditor();
            }
        }

        PsiFile javaFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        //PsiDocumentManager.getInstance(project).commitAllDocuments();
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        if (codeStyleManager != null) {
            codeStyleManager.reformat(javaFile);
        }

        ImportOptimizer importOptimizer = LanguageImportStatements.INSTANCE.forFile(javaFile)
                .iterator().next();
        if (importOptimizer != null) {
            importOptimizer.processFile(javaFile).run();
        }
    }

    /**
     * file file action
     *
     * @param childPackage
     * @param overrideIfNotExists
     */
    public void writeFileAction(String childPackage, boolean overrideIfNotExists) {
        new WriteCommandAction(project) {
            @Override
            protected void run(com.intellij.openapi.application.Result result) throws Throwable {
                writeFile(childPackage, overrideIfNotExists);
            }
        }.execute();
    }

    /**
     * get PsiFile from virtualFile
     *
     * @param virtualFile
     * @return
     */
    protected PsiFile findPsiFile(VirtualFile virtualFile) {
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    protected String getOriginalClassText() {
        return "public class " + mClass.getName() + "{}";
    }

    /**
     * get content from editor
     *
     * @return
     * @throws IOException
     */
    protected String getEditorContent() throws IOException {
        if (editor != null) {
            PsiFile psiJsonFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            return psiJsonFile.getText();
        }

        return editorHelper.getContents(project, editor, selectedFile);
    }

    /**
     * dump file content
     *
     * @return
     */
    public String getContent() {
        return mFile.getText();
    }

    public String dumpClass() {
        PsiClass clss = mClass;
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
        return text;
    }
}
