package jd.ide.intellij;


import jd.commonide.IdeDecompiler;
import jd.commonide.preferences.IdePreferences;
import jd.core.CoreConstants;

import java.io.File;

/**
 * Java Decompiler tool, use native libs to achieve decompilation.
 * <p/>
 */
public class JavaDecompiler {
    /**
     * Actual call to the native lib.
     *
     * @param basePath          Path to the root of the classpath, either a path to a directory or a path to a jar file.
     * @param internalTypeName  internal name of the type.
     * @return Decompiled class text.
     */
    public String decompile(String basePath, String internalTypeName) {
        // Create preferences
        IdePreferences preferences = new IdePreferences(
            true, false, true,
            false, false, false, false);

        // Decompile
        return IdeDecompiler.decompile(preferences, basePath, internalTypeName);
    }

    /**
     * @return version of JD-Core
     * @since JD-Core 0.7.1
     */
    public String getVersion() { return CoreConstants.JD_CORE_VERSION; }
}
