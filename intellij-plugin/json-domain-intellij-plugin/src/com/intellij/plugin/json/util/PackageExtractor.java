package com.intellij.plugin.json.util;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by vincent on 2016/5/10.
 */
public interface PackageExtractor {
    String extractPackageFromManifestStream(InputStream var1) throws ParserConfigurationException, SAXException, IOException;
}

