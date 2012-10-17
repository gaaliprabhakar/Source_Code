package org.jboss.soa.esb.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigTree implements Serializable, Cloneable {

    private static Logger logger = Logger.getLogger(ConfigTree.class);

    private static final long serialVersionUID = 1L;
    
	private boolean _pureText = true;
	
	private ConfigTree _dad;
	
	private String _name;
	
	private Map<String, String> _attributes;
	
	private List<Child> _childs;

	private static transient Logger _logger = Logger.getLogger(ConfigTree.class);
	
    public ConfigTree getParent() {
        return _dad;
    }

    private void setParent(ConfigTree dad) {
    	
        if (null != _dad && null != _dad._childs){
        	_dad._childs.remove(this);
        }
        
        if (null != dad){
        	dad.addChild(this);
        }
    }
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		if (null == name){
			throw new IllegalArgumentException();
		}
		_name = name;
	}

    public ConfigTree(String name) {
        this(name, null);
    }

    public ConfigTree(String name, ConfigTree dad) {
        setName(name);
        setParent(dad);
    }

    protected ConfigTree(ConfigTree other) {
        copyFrom(other);
    } 
    
    
    /**
     * assign a value to a named attribute
     *
     * @param name  String - the name (key) for the new attribute
     * @param value String - the value assigned to the key (if null - old value will be deleted)
     * @return String - old value assigned to the name (null if there was none)
     */
    public String setAttribute(String name, String value) {
        if (null == name)
            throw new IllegalArgumentException("Attribute name must be non null");
        if (null == _attributes)
            _attributes = new HashMap<String, String>();
        String oldVal = _attributes.remove(name);
        if (null != value)
            _attributes.put(name, value);
        return oldVal;
    } 

    /**
     * @return int - the number of non null attributes that this node has been assigned
     */
    public int attributeCount() {
        return (null == _attributes) ? 0 : _attributes.size();
    } 

    /**
     * Retrieve the value assigned to an attribute key.
     *
     * @param name String - the search key.
     * @return String - the value assigned to the specified key, or null if the
     *         attribute is not defined.
     */
    public String getAttribute(String name) {
        return (null == _attributes) ? null : _attributes.get(name);
    } 

    /**
     * Retrieve the value assigned to an attribute key, returning the supplied default
     * if the attribute is not defined.
     *
     * @param name         String - the search key.
     * @param defaultValue String - the default value to return if attribute is not set.
     * @return String - the value assigned to the specified key, or the default if
     *         the value is not defined.
     */
    public String getAttribute(String name, String defaultValue) {
        String ret = (null == _attributes) ? null : _attributes.get(name);
        return (ret != null ? ret : defaultValue);
    } 

    public long getLongAttribute(String name, long defaultValue) {
        String value = getAttribute(name);

        if(value != null) {
            try {
                return Long.parseLong(value.trim());
            } catch(NumberFormatException e) {
                logger.error("Invalid value '" + value + "' for property '" + name + "'.  Must be an long/integer value.  Returning default value '" + defaultValue + "'.");
            }
        }

        return defaultValue;
    }

    public float getFloatAttribute(String name, float defaultValue) {
        String value = getAttribute(name);

        if(value != null) {
            try {
                return Float.parseFloat(value.trim());
            } catch(NumberFormatException e) {
                logger.error("Invalid value '" + value + "' for property '" + name + "'.  Must be an float value.  Returning default value '" + defaultValue + "'.");
            }
        }

        return defaultValue;
    }

    public boolean getBooleanAttribute(String name, boolean defaultValue) {
        String value = getAttribute(name);

        if(value != null) {
            try {
                return Boolean.parseBoolean(value.trim());
            } catch(NumberFormatException e) {
                logger.error("Invalid value '" + value + "' for property '" + name + "'.  Must be an boolean value.  Returning default value '" + defaultValue + "'.");
            }
        }

        return defaultValue;
    }

    /**
     * Get the value of a requred property, throwing a {@link ConfigurationException}
     * if the property is not defined.
     *
     * @param name String - the search key.
     * @return The value assigned to the specified property.
     * @throws ConfigurationException The propery is not defined.
     */
    public String getRequiredAttribute(String name) throws ConfigurationException {
        String ret = getAttribute(name);

        if (ret == null) {
            throw new ConfigurationException("Required configuration property '" + name + "' not defined on configuration '" + _name + "'.");
        }

        return ret;
    }

    /**
     * obtain the list of all attribute names
     *
     * @return Set<String>  - the set of keys that have been assigned a non null value
     */
    public Set<String> getAttributeNames() {
        return (null == _attributes)
                ? new HashSet<String>()
                : _attributes.keySet();
    } 

    /**
     * obtain the list of all attribute as a List<KeyValuePair>
     *
     * @return List<KeyValuePair> - containing all attributes
     */
    public List<KeyValuePair> attributesAsList() {
        List<KeyValuePair> oRet = new ArrayList<KeyValuePair>();
        if (null != _attributes)
            for (Map.Entry<String, String> oCurr : _attributes.entrySet())
                oRet.add(new KeyValuePair(oCurr.getKey(), oCurr.getValue()));
        return oRet;
    } 

    /**
     * obtain the list of all child "property" elements as a List<KeyValuePair>
     *
     * @return List<KeyValuePair> - containing all child elements with tag name "property"
     */
    public List<KeyValuePair> childPropertyList() {
        List<KeyValuePair> oRet = new ArrayList<KeyValuePair>();
        for (ConfigTree current : getChildren("property")) {
            String name = current.getAttribute("name");
            if (null != name)
                oRet.add(new KeyValuePair(name, current.getAttribute("value")));
        }
        return oRet;
    } 

    /**
     * concatenated values of all child String values that have been added to 'this'
     * <br/>"" (zero length String) if no String child nodes
     *
     * @return String - concatenation of all String segments (equivalent to xml text nodes)
     */
    public String getWholeText() {
        if (null == _childs)
            return "";
        StringBuilder sb = null;
        for (Child child : _childs) {
            if (!(child._obj instanceof String))
                continue;
            if (null == sb)
                sb = new StringBuilder((String) child._obj);
            else
                sb.append((String) child._obj);
        }
        return sb.toString();

    } 

    /**
     * <b>first</b> child containing only text, with name=arg0
     *
     * @param name String - the name to filter
     * @return full text content of first 'pure text' child element under that name - &lt;null&gt; if none
     */
    public String getFirstTextChild(String name) {
        if (null == name)
            throw new IllegalArgumentException();
        if (null != _childs)
            for (Child oCurr : _childs) {
                ConfigTree tree = oCurr.getTree();
                if (null != tree && tree.isPureText() && name.equals(tree.getName()))
                    return tree.getWholeText();
            }
        return null;
    } 

    /**
     * Obtain all String values with the same name
     *
     * @param name String - filter for child String nodes
     * @return String[]
     */
    public String[] getTextChildren(String name) {
        if (null == name)
            throw new IllegalArgumentException();
        if (null == _childs)
            return new String[0];
        List<String> oRet = new ArrayList<String>();
        for (Child oCurr : _childs) {
            ConfigTree tree = oCurr.getTree();
            if (null != tree && tree.isPureText() && name.equals(tree.getName()))
                oRet.add(tree.getWholeText());
        }
        return oRet.toArray(new String[oRet.size()]);
    } 

    /**
     * add a child element that consists only of the text in arg0
     *
     * @param value String - the text to assign to the added child node
     */
    public void addTextChild(String value) {
        new Child(value);
    }

    private void addChild(ConfigTree child) {
        child._dad = this;
        new Child(child);
    }

    /**
     * retrieve list of child elements of 'this' that are instances of ConfigTree
     *
     * @return ConfigTree[] - Array containing all child elements of class ConfigTree
     */
    public ConfigTree[] getAllChildren() {
        if (null == _childs)
            return new ConfigTree[]{};
        List<ConfigTree> oRet = new ArrayList<ConfigTree>();
        for (Child oCurr : _childs)
            if (null != oCurr.getTree())
                oRet.add(oCurr.getTree());
        return oRet.toArray(new ConfigTree[oRet.size()]);
    } 

    /**
     * list of child elements of 'this' that are instances of ConfigTree, with name = arg0
     *
     * @param name String - the name of child nodes to filter
     * @return ConfigTree[] - child elements of class ConfigTree with name provided
     */
    public ConfigTree[] getChildren(String name) {
        if (null == name)
            throw new IllegalArgumentException();
        if (null == _childs)
            return new ConfigTree[0];
        List<ConfigTree> oRet = new ArrayList<ConfigTree>();
        for (Child oCurr : _childs)
            if (name.equals(oCurr.getName()))
                oRet.add(oCurr.getTree());
        return oRet.toArray(new ConfigTree[oRet.size()]);
    } 

    /**
     * <b>first</b> child of class ConfigTree with name=arg0
     *
     * @param name String - the name to filter
     * @return first child element under that name - &lt;null&gt; if none
     */
    public ConfigTree getFirstChild(String name) {
        if (null == name)
            throw new IllegalArgumentException();
        if (null != _childs)
            for (Child oCurr : _childs)
                if (name.equals(oCurr.getName()))
                    return oCurr.getTree();
        return null;
    } 

    /**
     * purge the list of children
     */
    public void removeAllChildren() {
        _childs = null;
    } 

    /**
     * remove children by name
     *
     * @param name String - only children by that name will be removed
     */
    public void removeChildrenByName(String name) {
        if (null == name)
            throw new IllegalArgumentException();
        if (null != _childs)
            for (ListIterator<Child> II = _childs.listIterator(); II.hasNext();)
                if (name.equals(II.next().getName()))
                    II.remove();
    } 

    /**
     * @return the number of child nodes (of any type)
     */
    public int childCount() {
        return (null == _childs) ? 0 : _childs.size();
    } 

    @Override
    public Object clone() {
        return cloneObj();
    }

    /**
     * instantiate a new ConfigTree with the same topology and contents of 'this'
     * <br/>Contained ConfigTree child elements will also be cloned
     * <br/>Transient objects are NOT copied nor cloned
     *
     * @return ConfigTree - Deep copy of 'this'
     */
    public ConfigTree cloneObj() {
        return cloneSubtree(null);
    }

    /**
     * @return ConfigTree - Deep copy of 'this'
     */
    private ConfigTree cloneSubtree(ConfigTree dad) {
        ConfigTree oRet = new ConfigTree(_name, dad);
        if (null != _attributes)
            for (Map.Entry<String, String> oAtt : _attributes.entrySet())
                oRet.setAttribute(oAtt.getKey(), oAtt.getValue());
        if (null != _childs)
            for (Child oChild : _childs) {
                ConfigTree tree = oChild.getTree();
                if (null != tree)
                    tree.cloneSubtree(oRet);
                else
                    oRet.addTextChild(oChild._obj.toString());
            }
        return oRet;
    } 

    protected void copyFrom(ConfigTree other) {
        this.setName(other.getName());
        this._pureText = other._pureText;

        if (null != other._attributes)
            for (Map.Entry<String, String> oneAtt : other._attributes.entrySet())
                setAttribute(oneAtt.getKey(), oneAtt.getValue());
        if (null != other._childs)
            for (Child child : other._childs)
                new Child(child);

    } 

    /**
     * obtain an instance of this class, from a 'normalized' xml format, with the default encoding
     * <p/> the 'normalized' xml format is the output of the toXml() instance method
     *
     * @param xml String - what to parse
     * @return ConfigTree - an object of this class
     * @throws SAXException - if xml format is invalid
     */
    public static ConfigTree fromXml(String xml) throws SAXException {
        try {
            return fromXml(xml, java.nio.charset.Charset.defaultCharset().toString());
        }
        catch (UnsupportedEncodingException e) {
            //  This can't happen
            _logger.fatal("Received unexpected exception: ", e);
            return null;
        }
    } 

    /**
     * obtain an instance of this class, from a 'normalized' xml format, with the encoding defined in arg1
     * <p/> the 'normalized' xml format is the output of the toXml() instance method
     *
     * @param xml      String - what to parse
     * @param encoding String - The encoding of arg 0
     * @return ConfigTree - an object of this class
     * @throws SAXException - if xml format is invalid
     */
    public static ConfigTree fromXml(String xml, String encoding)
            throws UnsupportedEncodingException, SAXException {
        if (null == xml)
            throw new IllegalArgumentException("Xml source String is null");
        try {
            return fromInputStream(new ByteArrayInputStream(xml.getBytes(encoding)));
        }
        catch (IOException e) {
            _logger.fatal("Received unexpected IOException: ", e);
            return null;
        }
    } 

    /**
     * obtain an instance of this class, from a 'normalized' xml format contained in an input stream
     * <p/> the 'normalized' xml format is the output of the toXml() instance method
     *
     * @param input InputStream - where to parse from
     * @return ConfigTree - an object of this class
     * @throws SAXException - if xml format is invalid
     * @throws IOException  - if an input/output error occurs
     */
    public static ConfigTree fromInputStream(InputStream input)
            throws SAXException, IOException {
        if (null == input)
            throw new IllegalArgumentException();
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e2) {
            _logger.error("Problems with default Parser Configuration", e2);
            return null;
        }
        Document oDoc = builder.parse(input);
        oDoc.normalize();

        return fromElement(oDoc.getDocumentElement());

    } 

    public static ConfigTree fromElement(Element elem) {
        ConfigTree tree = new ConfigTree(elem.getNodeName());
        NamedNodeMap NM = elem.getAttributes();
        if (null != NM)
            for (int i1 = 0; i1 < NM.getLength(); i1++) {
                Node node = NM.item(i1);
                tree.setAttribute(node.getNodeName(), node.getNodeValue());
            }
        NodeList NL = elem.getChildNodes();
        if (null != NL)
            for (int i1 = 0; i1 < NL.getLength(); i1++) {
                Node node = NL.item(i1);
                switch (node.getNodeType()) {
                    case Node.ELEMENT_NODE:
                        tree.addChild(ConfigTree.fromElement((Element) node));
                        break;
                    case Node.TEXT_NODE:
                        tree.addTextChild(node.getNodeValue());
                        break;
                }
            }

        return tree;
    } 

    private Element toElement(Document doc) {
        Element elem = doc.createElement(_name);
        if (null != _attributes)
            for (Map.Entry<String, String> oAtt : _attributes.entrySet())
                elem.setAttribute(oAtt.getKey(), oAtt.getValue());
        if (null != _childs)
            for (Child child : _childs) {
                ConfigTree tree = child.getTree();
                if (null != tree)
                    elem.appendChild(tree.toElement(doc));
                else
                    elem.appendChild(doc.createTextNode(child._obj.toString()));
            }
        return elem;
    } 

    /**
     * Equivalent to a call to toXml()
     *
     * @return String - a String with the 'standard' xml representation of 'this',
     *         using the default encoding
     */
    public String toString() {
        return toXml();
    }

    /**
     * Equivalent to a call to toXml(encoding)
     *
     * @param encoding String -
     * @return String - a String with the 'standard' xml representation of 'this',
     *         using the encoding specified in arg 0
     */

    public String toString(String encoding) {
        return toXml(encoding);
    }

    /**
     * @return String - a String with the 'standard' xml representation of 'this',
     *         using the default encoding
     */
    public String toXml() {
        return toXml(java.nio.charset.Charset.defaultCharset().toString());
    } 

    /**
     * Serialize this object - Transient objects are
     *
     * @param encoding String - String
     * @return String - a String with the 'standard' xml representation of 'this',
     *         using encoding specified in arg0
     */
    public String toXml(String encoding) {
        Transformer transf = null;
        try {
            transf = TransformerFactory.newInstance().newTransformer();
        }
        catch (TransformerConfigurationException e1) {
            _logger.error("Cannot obtain transformer to render XML output", e1);
            return null;
        }
        transf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transf.setOutputProperty(OutputKeys.INDENT, "no");
        transf.setOutputProperty(OutputKeys.ENCODING, encoding);

        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e2) {
            _logger.error("Problems with default Parser Configuration", e2);
            return null;
        }
        Document oDoc = builder.newDocument();
        oDoc.appendChild(toElement(oDoc));
        oDoc.normalize();
        DOMSource src = new DOMSource(oDoc);

        ByteArrayOutputStream oStrm = new ByteArrayOutputStream(5000);
        StreamResult res = new StreamResult(oStrm);

        try {
            transf.transform(src, res);
        }
        catch (TransformerException e3) {
            _logger.error("Problems with XML transformation", e3);
            return null;
        }

        return oStrm.toString();
    } 

    /**
     * @return boolean - indicating if 'this' element has ONLY text children (and consequently no ConfigTree children)
     */
    public boolean isPureText() {
        return _pureText;
    }

    private class Child {
        Object _obj;

        ConfigTree getTree() {
            return (_obj instanceof ConfigTree) ? (ConfigTree) _obj : null;
        }

        String getName() {
            return (_obj instanceof ConfigTree) ? ((ConfigTree) _obj)._name : null;
        }

        private Child(ConfigTree obj) {
            addToDad(obj);
            _pureText = false;
        }

        private Child(String obj) {
            addToDad(obj);
        }

        private Child(Child other) {
            if (other._obj instanceof ConfigTree) {
                addToDad(((ConfigTree) other._obj).cloneObj());
                _pureText = false;
                return;
            }
            if (other._obj instanceof String) {
                addToDad((String) other._obj);
                return;
            }

        }

        private void addToDad(Object obj) {
            if (null == _childs)
                _childs = new ArrayList<Child>();
            _obj = obj;
            _childs.add(this);
        }

    }
    
} 
