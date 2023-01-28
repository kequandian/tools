package com.intellij.plugin.json.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by vincent on 2016/5/10.
 */

public class XMLPackageExtractor implements PackageExtractor {
    public XMLPackageExtractor() {
    }

    public String extractPackageFromManifestStream(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        Element manifest = (Element)doc.getElementsByTagName("manifest").item(0);
        return manifest.getAttribute("package");
    }
}