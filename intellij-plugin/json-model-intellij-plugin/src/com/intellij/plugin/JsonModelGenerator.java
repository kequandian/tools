package com.intellij.plugin;

import com.google.common.io.Files;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugin.util.EditorHelper;
import com.intellij.plugin.util.PackageHelper;
import com.intellij.plugin.util.ProjectHelper;
import com.intellij.plugin.util.WriterUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;

/**
 * Created by vincent on 2016/4/4.
 */
public class JsonModelGenerator {
    private Project project;
    private Editor editor;
    private Module module;
    private VirtualFile selectedFile;

    private PsiClass psiJavaClass;

    private ProjectHelper projectHelper = new ProjectHelper();
    private PackageHelper packageHelper = new PackageHelper();
    private EditorHelper editorHelper = new EditorHelper();

    private String jsonText;

    public JsonModelGenerator(Project project, Editor editor, VirtualFile selectedFile, Module module) {
        this.project = project;
        this.module = module;
        this.editor = editor;
        this.selectedFile = selectedFile;

        try {
            if (editor != null) {
                PsiFile psiJsonFile = PsiUtilBase.getPsiFileInEditor(editor, project);

                String className = this.getModelClassName(psiJsonFile.getName());
                this.psiJavaClass = PsiElementFactory.SERVICE.getInstance(project).createClass(className);

                jsonText = psiJsonFile.getText();

            } else {
                String className = this.getModelClassName(selectedFile.getName());
                this.psiJavaClass = PsiElementFactory.SERVICE.getInstance(project).createClass(className);

                jsonText = editorHelper.getContents(project, editor, selectedFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * generate java class from json in editor
     *
     * @return
     */
    public PsiClass generateJavaClass() {
        new WriterUtil(this.jsonText, this.project, this.psiJavaClass).generate();

        return psiJavaClass;
    }

    /**
     * get the generated java class from json in editor
     *
     * @return
     */
    public PsiClass getGeneratedClass() {
        return this.psiJavaClass;
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

        if(packageName!=null && packageName.length()>0) {
            if (!packageName.endsWith(".model")) {
                packageName = packageName + ".model";
            }
        }

        return packageName;
    }


    /**
     * create eh java model file in project file system
     * and open the java file in editor
     *
     * @throws IOException
     */
    public VirtualFile generateJavaFile() throws IOException {
        generateJavaClass();

        // save code to file
        String fileName = psiJavaClass.getName() + ".java";

        String finalCode = psiJavaClass.getText();

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

    public void reformat(Editor editor, VirtualFile createdFile) {
        if (editor == null) {
            FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(createdFile);
            if (editors != null) {
                editor = ((TextEditor) editors[0]).getEditor();
            }
        }

        /// style
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
     * get the java model class name depends on json file
     *
     * @return
     */
    private String getModelClassName(String fileName) {
        String className = Files.getNameWithoutExtension(fileName);
        String[] arr = className.split("-|_");
        if (arr.length == 1) {
            className = StringUtil.capitalize(arr[0]);
        } else if (arr.length == 2) {
            className = StringUtil.capitalize(arr[0]) + StringUtil.capitalize(arr[1]);
        }
        return className;
    }
}
