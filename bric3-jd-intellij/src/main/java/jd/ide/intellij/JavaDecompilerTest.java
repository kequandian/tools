package jd.ide.intellij;

import java.io.File;

/**
 * Created by new on 2016-03-11.
 */
public class JavaDecompilerTest {
    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage:");
            System.out.println(" JavaDecompilerTest <base-path.jar> <com/className.class>");
            return;
        }
        String basePath = args[0];
        String className = args[1];

        JavaDecompiler javaDecompiler = new JavaDecompiler();
        File jarFile = new File(basePath);
        if(jarFile.isFile()) {
            String decompiled = javaDecompiler.decompile(jarFile.getPath(), className);
            System.out.println(decompiled);
        }else if(jarFile.isDirectory()){
            String decompiled = javaDecompiler.decompile(jarFile.getPath(), className);
            System.out.println(decompiled);
        }else {
            System.out.println(String.format("\"%s\" not exists", jarFile));
        }
    }
}
