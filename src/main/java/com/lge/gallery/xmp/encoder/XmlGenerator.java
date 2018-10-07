package com.lge.gallery.xmp.encoder;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.adobe.xmp.XMPConst;
import com.lge.gallery.xmp.encoder.model.Model;
import com.lge.gallery.xmp.encoder.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlGenerator {
    private static final boolean DEBUG = false;
    private static final char MARKER_SOI = '￘';
    private static final char MARKER_SOS = 'ￚ';
    private static final String TAG = "XmlGenerator";
    private static final String TAG_DES_START = "<rdf:Description";
    private static final String TAG_RDF_END = "</rdf:RDF>";
    private static final String TAG_XMP_END = "</x:xmpmeta>";
    private static final String TAG_XMP_START = "<x:xmpmeta";
    private XmpMetadata mMetadata;
    private String mUuid;
    private String mXmpDesElements;

    public XmlGenerator(XmpMetadata data) {
        this.mMetadata = data;
    }

    public ArrayList<XmpDataWrapper> generate() {
        ArrayList<XmpDataWrapper> result = new ArrayList();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            result.add(generateBasic(builder));
            if (this.mMetadata.hasExension()) {
                result.add(generateExtension(builder));
            }
        } catch (ParserConfigurationException e) {
        }
        return result;
    }

    private XmpDataWrapper generateBasic(DocumentBuilder builder) {
        Entry<String, Model> entry;
        Element elementNode;
        Document document = builder.newDocument();
        Element elementRdf = createDefaultHeader(document);
        ArrayList<Node> originXmpDesElementNodes = makeXmpDescriptionElementNodes(document);
        if (originXmpDesElementNodes != null) {
            Iterator it = originXmpDesElementNodes.iterator();
            while (it.hasNext()) {
                elementRdf.appendChild((Node) it.next());
            }
        }
        Element description = document.createElement("rdf:Description");
        description.setAttribute("rdf:about", "");
        XmpDto xmp = this.mMetadata.getData();
        description.setAttribute("xmlns:" + xmp.getPrefix(), xmp.getNamespace());
        elementRdf.appendChild(writeExtensionDescription(description, xmp));
        Iterator<Entry<String, Model>> iterator = xmp.childrenIterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            elementNode = document.createElement((String) entry.getKey());
            elementNode.appendChild(document.createTextNode(((Model) entry.getValue()).getData()));
            description.appendChild(elementNode);
        }
        if (xmp.hasExtension()) {
            Iterator<XmpDto> iter = xmp.extensionIterator();
            while (iter.hasNext()) {
                Iterator<Entry<String, Model>> subIterator = ((XmpDto) iter.next()).childrenIterator();
                while (subIterator.hasNext()) {
                    entry = (Entry) subIterator.next();
                    if (!((Model) entry.getValue()).isRaw()) {
                        elementNode = document.createElement((String) entry.getKey());
                        elementNode.appendChild(document.createTextNode(((Model) entry.getValue()).getData()));
                        description.appendChild(elementNode);
                    }
                }
            }
            Element uuid = document.createElement("xmpNote:HasExtendedXMP");
            this.mUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            uuid.appendChild(document.createTextNode(this.mUuid));
            description.appendChild(uuid);
        }
        return new XmpDataWrapper(dumpToByteArray(document), false);
    }

    private XmpDataWrapper generateExtension(DocumentBuilder builder) {
        XmpDto xmp = this.mMetadata.getData();
        Document document = builder.newDocument();
        Element elementRdf = createDefaultHeader(document);
        Element description = document.createElement("rdf:Description");
        description.setAttribute("rdf:about", "");
        elementRdf.appendChild(writeExtensionDescription(description, xmp));
        if (xmp.hasExtension()) {
            Iterator<XmpDto> iter = xmp.extensionIterator();
            while (iter.hasNext()) {
                Iterator<Entry<String, Model>> subIterator = ((XmpDto) iter.next()).childrenIterator();
                while (subIterator.hasNext()) {
                    Entry<String, Model> entry = (Entry) subIterator.next();
                    if (((Model) entry.getValue()).isRaw()) {
                        Element elementNode = document.createElement((String) entry.getKey());
                        elementNode.appendChild(document.createTextNode(((Model) entry.getValue()).getData()));
                        description.appendChild(elementNode);
                    }
                }
            }
        }
        return new XmpDataWrapper(dumpToByteArray(document), true);
    }

    public String getUuid() {
        return this.mUuid;
    }

    private Element createDefaultHeader(Document document) {
        Element root = document.createElement("x:xmpmeta");
        root.setAttribute("xmlns:x", XMPConst.NS_X);
        root.setAttribute("x:xmptk", "Soohyun XMP encoder 0.1");
        document.appendChild(root);
        Element elementRdf = document.createElement("rdf:RDF");
        elementRdf.setAttribute("xmlns:rdf", XMPConst.NS_RDF);
        root.appendChild(elementRdf);
        return elementRdf;
    }

    private Element writeExtensionDescription(Element element, XmpDto xmp) {
        if (xmp.hasExtension()) {
            element.setAttribute("xmlns:xmpNote", XMPConst.NS_XMP_NOTE);
            Iterator<XmpDto> iter = xmp.extensionIterator();
            while (iter.hasNext()) {
                XmpDto dto = (XmpDto) iter.next();
                element.setAttribute("xmlns:" + dto.getPrefix(), dto.getNamespace());
            }
        }
        return element;
    }

    private String extractXmpDescriptionElements(byte[] src) {
        String elements = null;
        try {
            StringBuilder builder = new StringBuilder(new String(src, "UTF-8"));
            int xmpStartIndex = builder.indexOf(TAG_XMP_START);
            int xmpEndIndex = builder.indexOf(TAG_XMP_END) + 12;
            if (xmpStartIndex < 0 || xmpEndIndex < 0) {
                Log.d(TAG, "No xmp tag.");
                return null;
            }
            String xmpData = builder.substring(xmpStartIndex, xmpEndIndex);
            int desStartIndex = xmpData.indexOf(TAG_DES_START);
            int desEndIndex = xmpData.indexOf(TAG_RDF_END);
            if (desStartIndex == -1) {
                return null;
            }
            elements = xmpData.substring(desStartIndex, desEndIndex);
            return elements;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    String makeXmpDescriptionElements(Context context, Uri from) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(from);
            String makeXmpDescriptionElements = makeXmpDescriptionElements(is);
            return makeXmpDescriptionElements;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.closeSilently(is);
        }
    }

    String makeXmpDescriptionElements(InputStream is) {
        try {
            DataInputStream dis = new DataInputStream(is);
            if (dis.readChar() != MARKER_SOI) {
                throw new IOException("No jpeg image.");
            }
            while (true) {
                char marker = dis.readChar();
                char length = dis.readChar();
                if (length > 2) {
                    byte[] buffer = new byte[length];
                    int read = dis.read(buffer, 0, length - 2);
                    if (XmpUtils.isXMP(buffer)) {
                        this.mXmpDesElements = extractXmpDescriptionElements(buffer);
                    }
                    if (marker == MARKER_SOS || read <= 0) {
                        break;
                    }
                }
            }
            return this.mXmpDesElements;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Node> makeXmpDescriptionElementNodes(Document document) {
        if (this.mXmpDesElements == null) {
            return null;
        }
        ArrayList<Node> nodes = new ArrayList();
        int startIndex = this.mXmpDesElements.indexOf(TAG_DES_START);
        int totalLength = this.mXmpDesElements.length();
        while (true) {
            int endIndex = this.mXmpDesElements.indexOf(TAG_DES_START, startIndex + 16);
            if (endIndex == -1) {
                endIndex = totalLength;
            }
            try {
                nodes.add(document.importNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(this.mXmpDesElements.substring(startIndex, endIndex).getBytes())).getDocumentElement(), true));
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            if (endIndex == totalLength) {
                return nodes;
            }
            startIndex = endIndex;
        }
    }

    private String dumpDocument(Document document) {
        StringWriter writer = new StringWriter();
        dump(document, new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    private byte[] dumpToByteArray(Document document) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dump(document, new StreamResult(baos));
        return baos.toByteArray();
    }

    private void dump(Document document, StreamResult result) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(new DOMSource(document), result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e2) {
            e2.printStackTrace();
        }
    }
}
