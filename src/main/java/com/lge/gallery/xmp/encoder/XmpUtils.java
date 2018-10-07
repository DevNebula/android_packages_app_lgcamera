package com.lge.gallery.xmp.encoder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmpUtils {
    private static final byte[] IMAGE_XMP_NS_BYTE = "http://ns.adobe.com/xap/1.0/\u0000".getBytes();
    private static final int IMAGE_XMP_NS_LENGTH = IMAGE_XMP_NS_BYTE.length;

    public static boolean isXMP(byte[] data) {
        if (data == null || data.length < IMAGE_XMP_NS_LENGTH) {
            return false;
        }
        byte[] buffer = new byte[IMAGE_XMP_NS_LENGTH];
        System.arraycopy(data, 0, buffer, 0, IMAGE_XMP_NS_LENGTH);
        return Arrays.equals(IMAGE_XMP_NS_BYTE, buffer);
    }

    public static final Document parseToXML(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        InputSource source = new InputSource(new StringReader(s));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = null;
        try {
            return factory.newDocumentBuilder().parse(source);
        } catch (SAXException e) {
            e.printStackTrace();
            return doc;
        } catch (IOException e2) {
            e2.printStackTrace();
            return doc;
        } catch (ParserConfigurationException e3) {
            e3.printStackTrace();
            return doc;
        }
    }
}
