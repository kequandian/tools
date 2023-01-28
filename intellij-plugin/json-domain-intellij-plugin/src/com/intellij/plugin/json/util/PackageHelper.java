package com.intellij.plugin.json.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincent on 2016/5/10.
 */
public class PackageHelper {

    private final PackageExtractor packageExtractor = new XMLPackageExtractor();

    private final ProjectHelper projectHelper = new ProjectHelper();

    public boolean checkGradle(Project project) {
        VirtualFile folder = project.getBaseDir().findFileByRelativePath("");
        if (folder != null) {
            VirtualFile gradleFile = folder.findChild("build.gradle");
            if (gradleFile != null) {
                return true;
            }
        }

        return false;
    }

    public boolean checkManifest(Project project, Module module) {
        for (String path : possibleManifestPaths()) {
            VirtualFile file = getManifestFileFromPath(project, path);
            if (file != null && file.exists()) {
                return true;
            }
        }
        for (String path : sourceRootPaths(project, module)) {
            VirtualFile file = getManifestFileFromPath(project, path);
            if (file != null && file.exists()) {
                return true;
            }
        }

        return false;
    }

    public String getPackageQualifiedName(Project project, VirtualFile virtualFile){
        VirtualFile projectDir = projectHelper.findSubProject(project, virtualFile);
        VirtualFile sourceDir = projectHelper.findSourceDir(project, projectDir);

        String packagePath = projectHelper.getRelativePath(sourceDir, virtualFile);
        return projectHelper.convertPackageFromPath(packagePath);
    }

    public String findPackageName(Project project, String subProject){
        VirtualFile subProjectDir = projectHelper.getVirtualFile(project,subProject);

        VirtualFile lastRoot = projectHelper.findLastRoot(project, subProjectDir);
        if(lastRoot!=null) {
            VirtualFile sourceDir = projectHelper.getSourceRootDir(subProjectDir);

            String packagePath = projectHelper.getRelativePath(sourceDir, lastRoot);

            packagePath = packagePath.replace("\\", "/");
            packagePath = packagePath.replace("/", ".");
            return packagePath;
        }

        return "";
    }

    public String getPackageNameFromManifest(Project project, Module module) {
        try {
            for (String path : possibleManifestPaths()) {
                VirtualFile file = getManifestFileFromPath(project, path);
                if (file != null && file.exists()) {
                    return packageExtractor.extractPackageFromManifestStream(file.getInputStream());
                }
            }
            for (String path : sourceRootPaths(project, module)) {
                VirtualFile file = getManifestFileFromPath(project, path);
                if (file != null && file.exists()) {
                    return packageExtractor.extractPackageFromManifestStream(file.getInputStream());
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private ArrayList<String> possibleManifestPaths() {
        return Lists.newArrayList("", "app/", "app/src/main/", "src/main/", "res/");
    }

    private List<String> sourceRootPaths(Project project, Module module) {
        return projectHelper.getSourceRootPathList(project, module);
    }

    private VirtualFile getManifestFileFromPath(Project project, String path) {
        VirtualFile folder = project.getBaseDir().findFileByRelativePath(path);
        if (folder != null) {
            return folder.findChild("AndroidManifest.xml");
        }

        return null;
    }


}
