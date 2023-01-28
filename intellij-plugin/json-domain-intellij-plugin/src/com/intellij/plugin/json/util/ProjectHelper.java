package com.intellij.plugin.json.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 */
public class ProjectHelper {

    public VirtualFile findOrCreateDirectory(Project project, VirtualFile baseDir, String folder) throws IOException {
        if(baseDir==null){
            baseDir = project.getBaseDir();
        }

        // find first
        if(baseDir.findFileByRelativePath(folder)!=null){
            return baseDir.findFileByRelativePath(folder);
        }

        // not found, create it
        VirtualFile directory = baseDir;
        if (folder.startsWith("/")) {
            folder = folder.substring(1);
        }

        String[] folders = folder.split("/");
        for (String childFolder : folders) {
            VirtualFile childDirectory = directory.findChild(childFolder);
            if (childDirectory != null && childDirectory.isDirectory()) {
                directory = childDirectory;
            } else {
                directory = directory.createChildDirectory(baseDir, childFolder);
            }
        }
        return directory;
    }

    public VirtualFile findOrCreateDirectory(Project project, String folder) throws IOException {
        return findOrCreateDirectory(project, null, folder);
    }

    /**
     * source path
     *
     * @param project
     * @return
     */
    public VirtualFile findOrCreateSourceDir(Project project) throws IOException{
        return findOrCreateSourceDir(project, project.getBaseDir(), true);
    }


    /**
     * @return
     */
    public VirtualFile findSourceDir(Project project, VirtualFile projectDir){
        try {
            return findOrCreateSourceDir(project, projectDir, false);
        } catch (IOException e) {
        }

        throw new RuntimeException("Fatal: Should not be here");
    }

    /**
     * @return
     */
    private VirtualFile findOrCreateSourceDir(Project project, VirtualFile projectDir, boolean createIfNotExists) throws IOException {
        if(projectDir==null){
            projectDir = project.getBaseDir();
        }

        String[] commonSrcDirs = new String[]{"src/main/java", "src"};

        VirtualFile srcDirectory = null;
        for(String dir : commonSrcDirs){
            srcDirectory = projectDir.findFileByRelativePath(dir);
            //srcDirectory = findDirectory(project, projectDir, dir);

            if(srcDirectory!=null){
                return srcDirectory;
            }
        }

        if(createIfNotExists) {
            if (srcDirectory == null) {
                srcDirectory = projectDir.createChildDirectory(project, commonSrcDirs[1]);
            }
        }

        return srcDirectory;
    }

    /**
     * find the project where the virtual file reside
     *
     * @param project
     * @param virtualFile
     * @return
     */
    public VirtualFile findSubProject(Project project, VirtualFile virtualFile) {
        VirtualFile targetDir = project.getBaseDir();


        String relativePath = virtualFile.getPath().replace(project.getBasePath(), "");
        String[] dirs = relativePath.split("/");

        VirtualFile nextDir = targetDir;
        for (String dir : dirs) {
            if (StringUtil.isEmpty(dir)) {
                continue;
            }
            nextDir = nextDir.findFileByRelativePath(dir);
            if(nextDir==null){
                return targetDir;
            }

            if (nextDir != null) {
                if (nextDir.findChild("build.gradle") != null ||
                        nextDir.findChild("pom.xml") != null) {
                    targetDir = nextDir;
                    continue;
                }
                break;
            }
        }

        return targetDir;
    }

    public VirtualFile findLastRoot(Project project){
        return findLastRoot(project, project.getBaseDir());
    }


    /**
     * get to the child path if it is the only child
     * return if the folder has no child or many childen
     *
     * @return
     */
    public VirtualFile findLastRoot(Project project, VirtualFile projectDir){

        try {
            VirtualFile sourceDir = findOrCreateSourceDir(project, projectDir, true);
            if(sourceDir==null){
                throw new RuntimeException("Fatal: fail to create src dir.");
            }

            int count = 1;
            while (count == 1) {
                VirtualFile[] children = sourceDir.getChildren();

                int dirCount = 0;
                for (int i = 0; i < children.length; i++) {
                    if (sourceDir.getChildren()[i].isDirectory()) {
                        dirCount++;
                    }
                }
                if (dirCount == 1) {
                    sourceDir = children[0];
                    continue;
                }

                return sourceDir;
            }

            return sourceDir;

        } catch (IOException e) {
        }

        return null;
    }

    /**
     * get relative path
     * @param baseDir reference dir
     * @param virtualFile target dir
     * @return
     */
    public String getRelativePath(VirtualFile baseDir, VirtualFile virtualFile) {
        String projectPath = StringUtils.normalizePath(baseDir.getPath());
        String sourcePath = StringUtils.normalizePath(virtualFile.getPath()).replace(projectPath, "");
        if (sourcePath.startsWith("/")) {
            sourcePath = sourcePath.substring(1);
        }
        return sourcePath;
    }

    public String convertPackageFromPath(String path){
        if(path.startsWith(".")){
            path = path.substring(1);
        }
        if(path.contains("\\")) {
            path = path.replace("\\", "/");
        }

        String packageName = path.replace("/", ".");
        return packageName;
    }


    public VirtualFile setFileContent(Project project, VirtualFile createdFile, String code) throws IOException {
        createdFile.setBinaryContent(code.getBytes());
        openFileInEditor(project, createdFile);
        return createdFile;
    }

    public void openFileInEditor(Project project, VirtualFile fileWithGeneratedCode) {
        FileEditorManager.getInstance(project).openFile(fileWithGeneratedCode, true);
    }

    /**
     * source path
     *
     * @param project
     * @return
     */
    public VirtualFile getSourceRootDir(Project project) {
        return getSourceRootDir(project.getBaseDir());
    }

    /**
     * @return
     */
    public VirtualFile getSourceRootDir(VirtualFile projectDir) {

        VirtualFile sourceDir = projectDir.findFileByRelativePath("src");
        if (sourceDir == null) {
            throw new RuntimeException("No src path found.");
        }

        if (sourceDir.findFileByRelativePath("main") != null) {
            VirtualFile file = sourceDir.findFileByRelativePath("main");
            if (file != null) {
                file = file.findFileByRelativePath("java");
            }
            if (file != null) {
                sourceDir = file;
            }
        }

        return sourceDir;
    }

    public VirtualFile createOrFindFile(Project project, String fileName, String folderPath) throws IOException {
        VirtualFile folder = createFolderIfNotExist(project, folderPath);
        return folder.findOrCreateChildData(project, fileName);
    }

    private VirtualFile createFolderIfNotExist(Project project, String folder) throws IOException {
        VirtualFile directory = project.getBaseDir();
        if (folder.startsWith("/")) {
            folder = folder.substring(1);
        }

        String[] folders = folder.split("/");
        for (String childFolder : folders) {
            VirtualFile childDirectory = directory.findChild(childFolder);
            if (childDirectory != null && childDirectory.isDirectory() && childDirectory.exists()) {
                directory = childDirectory;
            } else {
                directory = directory.createChildDirectory(project, childFolder);
            }
        }
        return directory;
    }

    public List<String> getSourceRootPathList(Project project, Module module) {
        List<String> sourceRoots = Lists.newArrayList();
        String projectPath = StringUtils.normalizePath(project.getBasePath());

        if (module != null) {
            for (VirtualFile virtualFile : ModuleRootManager.getInstance(module).getSourceRoots(false)) {
                sourceRoots.add(StringUtils.normalizePath(virtualFile.getPath()).replace(projectPath, ""));
            }
        }

        return sourceRoots;
    }

    public VirtualFile getVirtualFile(Project project, String path){
        if(StringUtil.isEmpty(path)){
            return project.getBaseDir();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if(!path.contains("/")){
            return project.getBaseDir().findFileByRelativePath(path);
        }

        VirtualFile nextDir = project.getBaseDir();
        String[] dirs = path.split("/");
        for (String dir : dirs) {
            if (nextDir.findFileByRelativePath(dir) != null) {
                nextDir = nextDir.findFileByRelativePath(dir);
                continue;
            }
            return nextDir;
        }

        return nextDir;
    }

    @SuppressWarnings("debug only")
    public VirtualFile tryCreateDirectory(Project project, String folder) {
        try {
            return findOrCreateDirectory(project, folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("Debug only")
    public void cleanDirectory(Project project, VirtualFile baseDir){
        try {
            VirtualFile[] dirs = baseDir.getChildren();
            if(dirs!=null) {
                for (VirtualFile dir : dirs){
                    if(dir.getChildren().length>0){
                        cleanDirectory(project, dir);
                    }else {
                        dir.delete(project);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}