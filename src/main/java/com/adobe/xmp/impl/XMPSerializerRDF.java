package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class XMPSerializerRDF {
    private static final int DEFAULT_PAD = 2048;
    private static final String PACKET_HEADER = "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>";
    private static final String PACKET_TRAILER = "<?xpacket end=\"";
    private static final String PACKET_TRAILER2 = "\"?>";
    static final Set RDF_ATTR_QUALIFIER = new HashSet(Arrays.asList(new String[]{XMPConst.XML_LANG, "rdf:resource", "rdf:ID", "rdf:bagID", "rdf:nodeID"}));
    private static final String RDF_EMPTY_STRUCT = "<rdf:Description/>";
    private static final String RDF_RDF_END = "</rdf:RDF>";
    private static final String RDF_RDF_START = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">";
    private static final String RDF_SCHEMA_END = "</rdf:Description>";
    private static final String RDF_SCHEMA_START = "<rdf:Description rdf:about=";
    private static final String RDF_STRUCT_END = "</rdf:Description>";
    private static final String RDF_STRUCT_START = "<rdf:Description";
    private static final String RDF_XMPMETA_END = "</x:xmpmeta>";
    private static final String RDF_XMPMETA_START = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"";
    private SerializeOptions options;
    private CountOutputStream outputStream;
    private int padding;
    private int unicodeSize = 1;
    private OutputStreamWriter writer;
    private XMPMetaImpl xmp;

    public void serialize(XMPMeta xmp, OutputStream out, SerializeOptions options) throws XMPException {
        try {
            this.outputStream = new CountOutputStream(out);
            this.writer = new OutputStreamWriter(this.outputStream, options.getEncoding());
            this.xmp = (XMPMetaImpl) xmp;
            this.options = options;
            this.padding = options.getPadding();
            this.writer = new OutputStreamWriter(this.outputStream, options.getEncoding());
            checkOptionsConsistence();
            String tailStr = serializeAsRDF();
            this.writer.flush();
            addPadding(tailStr.length());
            write(tailStr);
            this.writer.flush();
            this.outputStream.close();
        } catch (IOException e) {
            throw new XMPException("Error writing to the OutputStream", 0);
        }
    }

    private void addPadding(int tailLength) throws XMPException, IOException {
        if (this.options.getExactPacketLength()) {
            int minSize = this.outputStream.getBytesWritten() + (this.unicodeSize * tailLength);
            if (minSize > this.padding) {
                throw new XMPException("Can't fit into specified packet size", 107);
            }
            this.padding -= minSize;
        }
        this.padding /= this.unicodeSize;
        int newlineLen = this.options.getNewline().length();
        if (this.padding >= newlineLen) {
            this.padding -= newlineLen;
            while (this.padding >= newlineLen + 100) {
                writeChars(100, ' ');
                writeNewline();
                this.padding -= newlineLen + 100;
            }
            writeChars(this.padding, ' ');
            writeNewline();
            return;
        }
        writeChars(this.padding, ' ');
    }

    protected void checkOptionsConsistence() throws XMPException {
        if ((this.options.getEncodeUTF16BE() | this.options.getEncodeUTF16LE()) != 0) {
            this.unicodeSize = 2;
        }
        if (this.options.getExactPacketLength()) {
            if ((this.options.getOmitPacketWrapper() | this.options.getIncludeThumbnailPad()) != 0) {
                throw new XMPException("Inconsistent options for exact size serialize", 103);
            } else if ((this.options.getPadding() & (this.unicodeSize - 1)) != 0) {
                throw new XMPException("Exact size must be a multiple of the Unicode element", 103);
            }
        } else if (this.options.getReadOnlyPacket()) {
            if ((this.options.getOmitPacketWrapper() | this.options.getIncludeThumbnailPad()) != 0) {
                throw new XMPException("Inconsistent options for read-only packet", 103);
            }
            this.padding = 0;
        } else if (!this.options.getOmitPacketWrapper()) {
            if (this.padding == 0) {
                this.padding = this.unicodeSize * 2048;
            }
            if (this.options.getIncludeThumbnailPad() && !this.xmp.doesPropertyExist(XMPConst.NS_XMP, "Thumbnails")) {
                this.padding += this.unicodeSize * 10000;
            }
        } else if (this.options.getIncludeThumbnailPad()) {
            throw new XMPException("Inconsistent options for non-packet serialize", 103);
        } else {
            this.padding = 0;
        }
    }

    private String serializeAsRDF() throws IOException, XMPException {
        int level = 0;
        if (!this.options.getOmitPacketWrapper()) {
            writeIndent(0);
            write(PACKET_HEADER);
            writeNewline();
        }
        if (!this.options.getOmitXmpMetaElement()) {
            writeIndent(0);
            write(RDF_XMPMETA_START);
            if (!this.options.getOmitVersionAttribute()) {
                write(XMPMetaFactory.getVersionInfo().getMessage());
            }
            write("\">");
            writeNewline();
            level = 0 + 1;
        }
        writeIndent(level);
        write(RDF_RDF_START);
        writeNewline();
        if (this.options.getUseCanonicalFormat()) {
            serializeCanonicalRDFSchemas(level);
        } else {
            serializeCompactRDFSchemas(level);
        }
        writeIndent(level);
        write(RDF_RDF_END);
        writeNewline();
        if (!this.options.getOmitXmpMetaElement()) {
            writeIndent(level - 1);
            write(RDF_XMPMETA_END);
            writeNewline();
        }
        String tailStr = "";
        if (this.options.getOmitPacketWrapper()) {
            return tailStr;
        }
        for (level = this.options.getBaseIndent(); level > 0; level--) {
            tailStr = tailStr + this.options.getIndent();
        }
        return ((tailStr + PACKET_TRAILER) + (this.options.getReadOnlyPacket() ? 'r' : 'w')) + PACKET_TRAILER2;
    }

    private void serializeCanonicalRDFSchemas(int level) throws IOException, XMPException {
        if (this.xmp.getRoot().getChildrenLength() > 0) {
            startOuterRDFDescription(this.xmp.getRoot(), level);
            Iterator it = this.xmp.getRoot().iterateChildren();
            while (it.hasNext()) {
                serializeCanonicalRDFSchema((XMPNode) it.next(), level);
            }
            endOuterRDFDescription(level);
            return;
        }
        writeIndent(level + 1);
        write(RDF_SCHEMA_START);
        writeTreeName();
        write("/>");
        writeNewline();
    }

    private void writeTreeName() throws IOException {
        write(34);
        String name = this.xmp.getRoot().getName();
        if (name != null) {
            appendNodeValue(name, true);
        }
        write(34);
    }

    private void serializeCompactRDFSchemas(int level) throws IOException, XMPException {
        writeIndent(level + 1);
        write(RDF_SCHEMA_START);
        writeTreeName();
        Set usedPrefixes = new HashSet();
        usedPrefixes.add("xml");
        usedPrefixes.add("rdf");
        Iterator it = this.xmp.getRoot().iterateChildren();
        while (it.hasNext()) {
            declareUsedNamespaces((XMPNode) it.next(), usedPrefixes, level + 3);
        }
        boolean allAreAttrs = true;
        it = this.xmp.getRoot().iterateChildren();
        while (it.hasNext()) {
            allAreAttrs &= serializeCompactRDFAttrProps((XMPNode) it.next(), level + 2);
        }
        if (allAreAttrs) {
            write("/>");
            writeNewline();
            return;
        }
        write(62);
        writeNewline();
        it = this.xmp.getRoot().iterateChildren();
        while (it.hasNext()) {
            serializeCompactRDFElementProps((XMPNode) it.next(), level + 2);
        }
        writeIndent(level + 1);
        write("</rdf:Description>");
        writeNewline();
    }

    private boolean serializeCompactRDFAttrProps(XMPNode parentNode, int indent) throws IOException {
        boolean allAreAttrs = true;
        Iterator it = parentNode.iterateChildren();
        while (it.hasNext()) {
            XMPNode prop = (XMPNode) it.next();
            if (canBeRDFAttrProp(prop)) {
                writeNewline();
                writeIndent(indent);
                write(prop.getName());
                write("=\"");
                appendNodeValue(prop.getValue(), true);
                write(34);
            } else {
                allAreAttrs = false;
            }
        }
        return allAreAttrs;
    }

    private void serializeCompactRDFElementProps(XMPNode parentNode, int indent) throws IOException, XMPException {
        Iterator it = parentNode.iterateChildren();
        while (it.hasNext()) {
            XMPNode node = (XMPNode) it.next();
            if (!canBeRDFAttrProp(node)) {
                boolean emitEndTag = true;
                boolean indentEndTag = true;
                String elemName = node.getName();
                if (XMPConst.ARRAY_ITEM_NAME.equals(elemName)) {
                    elemName = "rdf:li";
                }
                writeIndent(indent);
                write(60);
                write(elemName);
                boolean hasGeneralQualifiers = false;
                boolean hasRDFResourceQual = false;
                Iterator iq = node.iterateQualifier();
                while (iq.hasNext()) {
                    XMPNode qualifier = (XMPNode) iq.next();
                    if (RDF_ATTR_QUALIFIER.contains(qualifier.getName())) {
                        hasRDFResourceQual = "rdf:resource".equals(qualifier.getName());
                        write(32);
                        write(qualifier.getName());
                        write("=\"");
                        appendNodeValue(qualifier.getValue(), true);
                        write(34);
                    } else {
                        hasGeneralQualifiers = true;
                    }
                }
                if (hasGeneralQualifiers) {
                    serializeCompactRDFGeneralQualifier(indent, node);
                } else if (!node.getOptions().isCompositeProperty()) {
                    Object[] result = serializeCompactRDFSimpleProp(node);
                    emitEndTag = ((Boolean) result[0]).booleanValue();
                    indentEndTag = ((Boolean) result[1]).booleanValue();
                } else if (node.getOptions().isArray()) {
                    serializeCompactRDFArrayProp(node, indent);
                } else {
                    emitEndTag = serializeCompactRDFStructProp(node, indent, hasRDFResourceQual);
                }
                if (emitEndTag) {
                    if (indentEndTag) {
                        writeIndent(indent);
                    }
                    write("</");
                    write(elemName);
                    write(62);
                    writeNewline();
                }
            }
        }
    }

    private Object[] serializeCompactRDFSimpleProp(XMPNode node) throws IOException {
        Boolean emitEndTag = Boolean.TRUE;
        Boolean indentEndTag = Boolean.TRUE;
        if (node.getOptions().isURI()) {
            write(" rdf:resource=\"");
            appendNodeValue(node.getValue(), true);
            write("\"/>");
            writeNewline();
            emitEndTag = Boolean.FALSE;
        } else if (node.getValue() == null || node.getValue().length() == 0) {
            write("/>");
            writeNewline();
            emitEndTag = Boolean.FALSE;
        } else {
            write(62);
            appendNodeValue(node.getValue(), false);
            indentEndTag = Boolean.FALSE;
        }
        return new Object[]{emitEndTag, indentEndTag};
    }

    private void serializeCompactRDFArrayProp(XMPNode node, int indent) throws IOException, XMPException {
        write(62);
        writeNewline();
        emitRDFArrayTag(node, true, indent + 1);
        if (node.getOptions().isArrayAltText()) {
            XMPNodeUtils.normalizeLangArray(node);
        }
        serializeCompactRDFElementProps(node, indent + 2);
        emitRDFArrayTag(node, false, indent + 1);
    }

    private boolean serializeCompactRDFStructProp(XMPNode node, int indent, boolean hasRDFResourceQual) throws XMPException, IOException {
        boolean hasAttrFields = false;
        boolean hasElemFields = false;
        Iterator ic = node.iterateChildren();
        while (ic.hasNext()) {
            if (canBeRDFAttrProp((XMPNode) ic.next())) {
                hasAttrFields = true;
            } else {
                hasElemFields = true;
            }
            if (hasAttrFields && hasElemFields) {
                break;
            }
        }
        if (hasRDFResourceQual && hasElemFields) {
            throw new XMPException("Can't mix rdf:resource qualifier and element fields", 202);
        } else if (!node.hasChildren()) {
            write(" rdf:parseType=\"Resource\"/>");
            writeNewline();
            return false;
        } else if (!hasElemFields) {
            serializeCompactRDFAttrProps(node, indent + 1);
            write("/>");
            writeNewline();
            return false;
        } else if (hasAttrFields) {
            write(62);
            writeNewline();
            writeIndent(indent + 1);
            write(RDF_STRUCT_START);
            serializeCompactRDFAttrProps(node, indent + 2);
            write(">");
            writeNewline();
            serializeCompactRDFElementProps(node, indent + 1);
            writeIndent(indent + 1);
            write("</rdf:Description>");
            writeNewline();
            return true;
        } else {
            write(" rdf:parseType=\"Resource\">");
            writeNewline();
            serializeCompactRDFElementProps(node, indent + 1);
            return true;
        }
    }

    private void serializeCompactRDFGeneralQualifier(int indent, XMPNode node) throws IOException, XMPException {
        write(" rdf:parseType=\"Resource\">");
        writeNewline();
        serializeCanonicalRDFProperty(node, false, true, indent + 1);
        Iterator iq = node.iterateQualifier();
        while (iq.hasNext()) {
            serializeCanonicalRDFProperty((XMPNode) iq.next(), false, false, indent + 1);
        }
    }

    private void serializeCanonicalRDFSchema(XMPNode schemaNode, int level) throws IOException, XMPException {
        Iterator it = schemaNode.iterateChildren();
        while (it.hasNext()) {
            serializeCanonicalRDFProperty((XMPNode) it.next(), this.options.getUseCanonicalFormat(), false, level + 2);
        }
    }

    private void declareUsedNamespaces(XMPNode node, Set usedPrefixes, int indent) throws IOException {
        Iterator it;
        if (node.getOptions().isSchemaNode()) {
            declareNamespace(node.getValue().substring(0, node.getValue().length() - 1), node.getName(), usedPrefixes, indent);
        } else if (node.getOptions().isStruct()) {
            it = node.iterateChildren();
            while (it.hasNext()) {
                declareNamespace(((XMPNode) it.next()).getName(), null, usedPrefixes, indent);
            }
        }
        it = node.iterateChildren();
        while (it.hasNext()) {
            declareUsedNamespaces((XMPNode) it.next(), usedPrefixes, indent);
        }
        it = node.iterateQualifier();
        while (it.hasNext()) {
            XMPNode qualifier = (XMPNode) it.next();
            declareNamespace(qualifier.getName(), null, usedPrefixes, indent);
            declareUsedNamespaces(qualifier, usedPrefixes, indent);
        }
    }

    private void declareNamespace(String prefix, String namespace, Set usedPrefixes, int indent) throws IOException {
        if (namespace == null) {
            QName qname = new QName(prefix);
            if (qname.hasPrefix()) {
                prefix = qname.getPrefix();
                namespace = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(prefix + ":");
                declareNamespace(prefix, namespace, usedPrefixes, indent);
            } else {
                return;
            }
        }
        if (!usedPrefixes.contains(prefix)) {
            writeNewline();
            writeIndent(indent);
            write("xmlns:");
            write(prefix);
            write("=\"");
            write(namespace);
            write(34);
            usedPrefixes.add(prefix);
        }
    }

    private void startOuterRDFDescription(XMPNode schemaNode, int level) throws IOException {
        writeIndent(level + 1);
        write(RDF_SCHEMA_START);
        writeTreeName();
        Set usedPrefixes = new HashSet();
        usedPrefixes.add("xml");
        usedPrefixes.add("rdf");
        declareUsedNamespaces(schemaNode, usedPrefixes, level + 3);
        write(62);
        writeNewline();
    }

    private void endOuterRDFDescription(int level) throws IOException {
        writeIndent(level + 1);
        write("</rdf:Description>");
        writeNewline();
    }

    private void serializeCanonicalRDFProperty(XMPNode node, boolean useCanonicalRDF, boolean emitAsRDFValue, int indent) throws IOException, XMPException {
        XMPNode qualifier;
        boolean emitEndTag = true;
        boolean indentEndTag = true;
        String elemName = node.getName();
        if (emitAsRDFValue) {
            elemName = "rdf:value";
        } else if (XMPConst.ARRAY_ITEM_NAME.equals(elemName)) {
            elemName = "rdf:li";
        }
        writeIndent(indent);
        write(60);
        write(elemName);
        boolean hasGeneralQualifiers = false;
        boolean hasRDFResourceQual = false;
        Iterator it = node.iterateQualifier();
        while (it.hasNext()) {
            qualifier = (XMPNode) it.next();
            if (RDF_ATTR_QUALIFIER.contains(qualifier.getName())) {
                hasRDFResourceQual = "rdf:resource".equals(qualifier.getName());
                if (!emitAsRDFValue) {
                    write(32);
                    write(qualifier.getName());
                    write("=\"");
                    appendNodeValue(qualifier.getValue(), true);
                    write(34);
                }
            } else {
                hasGeneralQualifiers = true;
            }
        }
        if (!hasGeneralQualifiers || emitAsRDFValue) {
            if (node.getOptions().isCompositeProperty()) {
                if (node.getOptions().isArray()) {
                    write(62);
                    writeNewline();
                    emitRDFArrayTag(node, true, indent + 1);
                    if (node.getOptions().isArrayAltText()) {
                        XMPNodeUtils.normalizeLangArray(node);
                    }
                    it = node.iterateChildren();
                    while (it.hasNext()) {
                        serializeCanonicalRDFProperty((XMPNode) it.next(), useCanonicalRDF, false, indent + 2);
                    }
                    emitRDFArrayTag(node, false, indent + 1);
                } else if (hasRDFResourceQual) {
                    it = node.iterateChildren();
                    while (it.hasNext()) {
                        XMPNode child = (XMPNode) it.next();
                        if (canBeRDFAttrProp(child)) {
                            writeNewline();
                            writeIndent(indent + 1);
                            write(32);
                            write(child.getName());
                            write("=\"");
                            appendNodeValue(child.getValue(), true);
                            write(34);
                        } else {
                            throw new XMPException("Can't mix rdf:resource and complex fields", 202);
                        }
                    }
                    write("/>");
                    writeNewline();
                    emitEndTag = false;
                } else if (node.hasChildren()) {
                    if (useCanonicalRDF) {
                        write(">");
                        writeNewline();
                        indent++;
                        writeIndent(indent);
                        write(RDF_STRUCT_START);
                        write(">");
                    } else {
                        write(" rdf:parseType=\"Resource\">");
                    }
                    writeNewline();
                    it = node.iterateChildren();
                    while (it.hasNext()) {
                        serializeCanonicalRDFProperty((XMPNode) it.next(), useCanonicalRDF, false, indent + 1);
                    }
                    if (useCanonicalRDF) {
                        writeIndent(indent);
                        write("</rdf:Description>");
                        writeNewline();
                        indent--;
                    }
                } else {
                    if (useCanonicalRDF) {
                        write(">");
                        writeNewline();
                        writeIndent(indent + 1);
                        write(RDF_EMPTY_STRUCT);
                    } else {
                        write(" rdf:parseType=\"Resource\"/>");
                        emitEndTag = false;
                    }
                    writeNewline();
                }
            } else if (node.getOptions().isURI()) {
                write(" rdf:resource=\"");
                appendNodeValue(node.getValue(), true);
                write("\"/>");
                writeNewline();
                emitEndTag = false;
            } else if (node.getValue() == null || "".equals(node.getValue())) {
                write("/>");
                writeNewline();
                emitEndTag = false;
            } else {
                write(62);
                appendNodeValue(node.getValue(), false);
                indentEndTag = false;
            }
        } else if (hasRDFResourceQual) {
            throw new XMPException("Can't mix rdf:resource and general qualifiers", 202);
        } else {
            if (useCanonicalRDF) {
                write(">");
                writeNewline();
                indent++;
                writeIndent(indent);
                write(RDF_STRUCT_START);
                write(">");
            } else {
                write(" rdf:parseType=\"Resource\">");
            }
            writeNewline();
            serializeCanonicalRDFProperty(node, useCanonicalRDF, true, indent + 1);
            it = node.iterateQualifier();
            while (it.hasNext()) {
                qualifier = (XMPNode) it.next();
                if (!RDF_ATTR_QUALIFIER.contains(qualifier.getName())) {
                    serializeCanonicalRDFProperty(qualifier, useCanonicalRDF, false, indent + 1);
                }
            }
            if (useCanonicalRDF) {
                writeIndent(indent);
                write("</rdf:Description>");
                writeNewline();
                indent--;
            }
        }
        if (emitEndTag) {
            if (indentEndTag) {
                writeIndent(indent);
            }
            write("</");
            write(elemName);
            write(62);
            writeNewline();
        }
    }

    private void emitRDFArrayTag(XMPNode arrayNode, boolean isStartTag, int indent) throws IOException {
        if (isStartTag || arrayNode.hasChildren()) {
            writeIndent(indent);
            write(isStartTag ? "<rdf:" : "</rdf:");
            if (arrayNode.getOptions().isArrayAlternate()) {
                write("Alt");
            } else if (arrayNode.getOptions().isArrayOrdered()) {
                write("Seq");
            } else {
                write("Bag");
            }
            if (!isStartTag || arrayNode.hasChildren()) {
                write(">");
            } else {
                write("/>");
            }
            writeNewline();
        }
    }

    private void appendNodeValue(String value, boolean forAttribute) throws IOException {
        if (value == null) {
            value = "";
        }
        write(Utils.escapeXML(value, forAttribute, true));
    }

    private boolean canBeRDFAttrProp(XMPNode node) {
        return (node.hasQualifier() || node.getOptions().isURI() || node.getOptions().isCompositeProperty() || XMPConst.ARRAY_ITEM_NAME.equals(node.getName())) ? false : true;
    }

    private void writeIndent(int times) throws IOException {
        for (int i = this.options.getBaseIndent() + times; i > 0; i--) {
            this.writer.write(this.options.getIndent());
        }
    }

    private void write(int c) throws IOException {
        this.writer.write(c);
    }

    private void write(String str) throws IOException {
        this.writer.write(str);
    }

    private void writeChars(int number, char c) throws IOException {
        while (number > 0) {
            this.writer.write(c);
            number--;
        }
    }

    private void writeNewline() throws IOException {
        this.writer.write(this.options.getNewline());
    }
}
