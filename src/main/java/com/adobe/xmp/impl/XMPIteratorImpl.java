package com.adobe.xmp.impl;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.impl.xpath.XMPPath;
import com.adobe.xmp.impl.xpath.XMPPathParser;
import com.adobe.xmp.options.IteratorOptions;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class XMPIteratorImpl implements XMPIterator {
    private String baseNS = null;
    private Iterator nodeIterator = null;
    private IteratorOptions options;
    protected boolean skipSiblings = false;
    protected boolean skipSubtree = false;

    private class NodeIterator implements Iterator {
        protected static final int ITERATE_CHILDREN = 1;
        protected static final int ITERATE_NODE = 0;
        protected static final int ITERATE_QUALIFIER = 2;
        private Iterator childrenIterator = null;
        private int index = 0;
        private String path;
        private XMPPropertyInfo returnProperty = null;
        private int state = 0;
        private Iterator subIterator = Collections.EMPTY_LIST.iterator();
        private XMPNode visitedNode;

        public NodeIterator(XMPNode visitedNode, String parentPath, int index) {
            this.visitedNode = visitedNode;
            this.state = 0;
            if (visitedNode.getOptions().isSchemaNode()) {
                XMPIteratorImpl.this.setBaseNS(visitedNode.getName());
            }
            this.path = accumulatePath(visitedNode, parentPath, index);
        }

        public boolean hasNext() {
            if (this.returnProperty != null) {
                return true;
            }
            if (this.state == 0) {
                return reportNode();
            }
            if (this.state == 1) {
                if (this.childrenIterator == null) {
                    this.childrenIterator = this.visitedNode.iterateChildren();
                }
                boolean hasNext = iterateChildren(this.childrenIterator);
                if (hasNext || !this.visitedNode.hasQualifier() || XMPIteratorImpl.this.getOptions().isOmitQualifiers()) {
                    return hasNext;
                }
                this.state = 2;
                this.childrenIterator = null;
                return hasNext();
            }
            if (this.childrenIterator == null) {
                this.childrenIterator = this.visitedNode.iterateQualifier();
            }
            return iterateChildren(this.childrenIterator);
        }

        protected boolean reportNode() {
            this.state = 1;
            if (this.visitedNode.getParent() == null || (XMPIteratorImpl.this.getOptions().isJustLeafnodes() && this.visitedNode.hasChildren())) {
                return hasNext();
            }
            this.returnProperty = createPropertyInfo(this.visitedNode, XMPIteratorImpl.this.getBaseNS(), this.path);
            return true;
        }

        private boolean iterateChildren(Iterator iterator) {
            if (XMPIteratorImpl.this.skipSiblings) {
                XMPIteratorImpl.this.skipSiblings = false;
                this.subIterator = Collections.EMPTY_LIST.iterator();
            }
            if (!this.subIterator.hasNext() && iterator.hasNext()) {
                XMPNode child = (XMPNode) iterator.next();
                this.index++;
                this.subIterator = new NodeIterator(child, this.path, this.index);
            }
            if (!this.subIterator.hasNext()) {
                return false;
            }
            this.returnProperty = (XMPPropertyInfo) this.subIterator.next();
            return true;
        }

        public Object next() {
            if (hasNext()) {
                XMPPropertyInfo result = this.returnProperty;
                this.returnProperty = null;
                return result;
            }
            throw new NoSuchElementException("There are no more nodes to return");
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected String accumulatePath(XMPNode currNode, String parentPath, int currentIndex) {
            if (currNode.getParent() == null || currNode.getOptions().isSchemaNode()) {
                return null;
            }
            String separator;
            String segmentName;
            if (currNode.getParent().getOptions().isArray()) {
                separator = "";
                segmentName = "[" + String.valueOf(currentIndex) + "]";
            } else {
                separator = "/";
                segmentName = currNode.getName();
            }
            if (parentPath == null || parentPath.length() == 0) {
                return segmentName;
            }
            if (!XMPIteratorImpl.this.getOptions().isJustLeafname()) {
                return parentPath + separator + segmentName;
            }
            if (segmentName.startsWith("?")) {
                return segmentName.substring(1);
            }
            return segmentName;
        }

        protected XMPPropertyInfo createPropertyInfo(XMPNode node, String baseNS, String path) {
            final String value = node.getOptions().isSchemaNode() ? null : node.getValue();
            final XMPNode xMPNode = node;
            final String str = baseNS;
            final String str2 = path;
            return new XMPPropertyInfo() {
                public String getNamespace() {
                    if (xMPNode.getOptions().isSchemaNode()) {
                        return str;
                    }
                    return XMPMetaFactory.getSchemaRegistry().getNamespaceURI(new QName(xMPNode.getName()).getPrefix());
                }

                public String getPath() {
                    return str2;
                }

                public String getValue() {
                    return value;
                }

                public PropertyOptions getOptions() {
                    return xMPNode.getOptions();
                }

                public String getLanguage() {
                    return null;
                }
            };
        }

        protected Iterator getChildrenIterator() {
            return this.childrenIterator;
        }

        protected void setChildrenIterator(Iterator childrenIterator) {
            this.childrenIterator = childrenIterator;
        }

        protected XMPPropertyInfo getReturnProperty() {
            return this.returnProperty;
        }

        protected void setReturnProperty(XMPPropertyInfo returnProperty) {
            this.returnProperty = returnProperty;
        }
    }

    private class NodeIteratorChildren extends NodeIterator {
        private Iterator childrenIterator;
        private int index = 0;
        private String parentPath;

        public NodeIteratorChildren(XMPNode parentNode, String parentPath) {
            super();
            if (parentNode.getOptions().isSchemaNode()) {
                XMPIteratorImpl.this.setBaseNS(parentNode.getName());
            }
            this.parentPath = accumulatePath(parentNode, parentPath, 1);
            this.childrenIterator = parentNode.iterateChildren();
        }

        public boolean hasNext() {
            if (getReturnProperty() != null) {
                return true;
            }
            if (XMPIteratorImpl.this.skipSiblings) {
                return false;
            }
            if (!this.childrenIterator.hasNext()) {
                return false;
            }
            XMPNode child = (XMPNode) this.childrenIterator.next();
            this.index++;
            String path = null;
            if (child.getOptions().isSchemaNode()) {
                XMPIteratorImpl.this.setBaseNS(child.getName());
            } else if (child.getParent() != null) {
                path = accumulatePath(child, this.parentPath, this.index);
            }
            if (XMPIteratorImpl.this.getOptions().isJustLeafnodes() && child.hasChildren()) {
                return hasNext();
            }
            setReturnProperty(createPropertyInfo(child, XMPIteratorImpl.this.getBaseNS(), path));
            return true;
        }
    }

    public XMPIteratorImpl(XMPMetaImpl xmp, String schemaNS, String propPath, IteratorOptions options) throws XMPException {
        boolean baseSchema;
        XMPNode startNode;
        if (options == null) {
            options = new IteratorOptions();
        }
        this.options = options;
        String initialPath = null;
        if (schemaNS == null || schemaNS.length() <= 0) {
            baseSchema = false;
        } else {
            baseSchema = true;
        }
        boolean baseProperty;
        if (propPath == null || propPath.length() <= 0) {
            baseProperty = false;
        } else {
            baseProperty = true;
        }
        if (!baseSchema && !baseProperty) {
            startNode = xmp.getRoot();
        } else if (baseSchema && baseProperty) {
            XMPPath path = XMPPathParser.expandXPath(schemaNS, propPath);
            XMPPath basePath = new XMPPath();
            for (int i = 0; i < path.size() - 1; i++) {
                basePath.add(path.getSegment(i));
            }
            startNode = XMPNodeUtils.findNode(xmp.getRoot(), path, false, null);
            this.baseNS = schemaNS;
            initialPath = basePath.toString();
        } else if (!baseSchema || baseProperty) {
            throw new XMPException("Schema namespace URI is required", 101);
        } else {
            startNode = XMPNodeUtils.findSchemaNode(xmp.getRoot(), schemaNS, false);
        }
        if (startNode == null) {
            this.nodeIterator = Collections.EMPTY_LIST.iterator();
        } else if (this.options.isJustChildren()) {
            this.nodeIterator = new NodeIteratorChildren(startNode, initialPath);
        } else {
            this.nodeIterator = new NodeIterator(startNode, initialPath, 1);
        }
    }

    public void skipSubtree() {
        this.skipSubtree = true;
    }

    public void skipSiblings() {
        skipSubtree();
        this.skipSiblings = true;
    }

    public boolean hasNext() {
        return this.nodeIterator.hasNext();
    }

    public Object next() {
        return this.nodeIterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("The XMPIterator does not support remove().");
    }

    protected IteratorOptions getOptions() {
        return this.options;
    }

    protected String getBaseNS() {
        return this.baseNS;
    }

    protected void setBaseNS(String baseNS) {
        this.baseNS = baseNS;
    }
}
