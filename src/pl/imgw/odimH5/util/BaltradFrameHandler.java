/***************************************************************************************************
 *
 * Copyright (C) 2009-2010 Institute of Meteorology and Water Management, IMGW
 *
 * This file is part of the BaltradDex software.
 *
 * BaltradDex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BaltradDex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the BaltradDex software.  If not, see http://www.gnu.org/licenses.
 *
 ***************************************************************************************************/

package pl.imgw.odimH5.util;

//import eu.baltrad.dex.log.model.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class implementing Baltrad message handling functionality.
 * 
 * @author <a href="mailto:maciej.szewczykowski@imgw.pl>Maciej Szewczykowski</a>
 * @version 1.0
 * @since 1.0
 */
public class BaltradFrameHandler {
    // ----------------------------------------------------------------------------------------
    // Constants
    // XML element / document version
    private static final String XML_VERSION = "1.0";
    // XML element / document encoding
    private static final String XML_ENCODING = "UTF-8";
    // XML element / root node
    private static final String ROOT_ELEM = "baltrad_frame";
    // XML element / header node
    private static final String HDR = "header";
    // XML element / MIME type attribute
    private static final String MIME_TYPE = "mimetype";

    // XML elements / available MIME types
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_APPLICATION = "application/octet-stream";
    public static final String MIME_TEXT = "text/plain";
    public static final String MIME_TEXT_XML = "text/xml";

    // XML element / user name attribute
    private static final String USR_NAME = "user_name";
    // XML element / user's password attribute
    private static final String PASSWD = "passwd";
    // XML element / sender node address
    private static final String SNDR_ADDR = "sender_node_address";
    // XML element / sender node name
    private static final String SNDR_NAME = "sender_node_name";

    // XML element / content node
    private static final String CONTENT = "content";
    // XML element / content type
    private static final String CONTENT_TYPE = "type";

    // frame content identifiers
    // XML element / message content frame
    public static final String MSG = "message";
    // XML element / file content frame
    public static final String FILE = "file";
    // XML element / object content frame
    public static final String OBJECT = "object";

    // XML element / message type attribute
    private static final String MSG_CLASS = "class";
    // XML elements / message type attribute values
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String REQUEST = "REQUEST";

    // Baltrad communication signals

    // channel list request string
    public static final String CHNL_LIST_RQST = "_chnl_list_rqst_";
    // channel list object identifier
    public static final String CHNL_LIST = "_chnl_list_";
    // channel synchronization request
    public static final String CHNL_SYNC_RQST = "_chnl_sync_rqst_";
    // channel synchronization response
    public static final String CHNL_SYNC_RSPNS = "_chnl_sync_rspns_";
    // channel subscription request
    public static final String CHNL_SBN_RQST = "_chnl_sbn_rqst_";
    // channel subscription confirmation
    public static final String CHNL_SBN_CFN = "_chnl_sbn_cfn_";
    // channel subscription change request
    public static final String SBN_CHNG_RQST = "_sbn_chng_rqst_";
    // channel subscription change success message
    public static final String SBN_CHNG_OK = "_sbn_chng_ok_";
    // channel subscription change failure message
    public static final String SBN_CHNG_FAIL = "_sbn_chng_fail_";

    // XML element / message text attribute
    private static final String MSG_TEXT = "text";

    // XML element / relative file name attribute
    private static final String FILE_NAME = "name";
    // XML element / data channel attribute
    private static final String CHNL_NAME = "channel";
    // Character set
    private static final Charset CHARSET = Charset.forName(XML_ENCODING);
    // ----------------------------------------------------------------------------------------
    // Variables
    // Receiver's URL address
    private String url;
    // Reference to LoManager class object
    // private LogManager logManager = new LogManager();
    private MessageLogger msg;
    private boolean verbose;

    // ------------------------------------------------------------------------------------------
    // Methods
    /**
     * Default constructor.
     */
    public BaltradFrameHandler(MessageLogger msg, boolean verbose) {
        this.msg = msg;
    }

    /**
     * Constructor sets field values.
     * 
     * @param url
     *            Receiver's URL address
     */
    public BaltradFrameHandler(MessageLogger msg, String url, boolean verbose) {

        this.url = url;
        this.msg = msg;
        this.verbose = verbose;
    }

    /**
     * Method posts data on the receiver's server.
     */
    public void handleBF(BaltradFrame baltradFrame) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(
                    CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httpPost = new HttpPost(getUrl());
            httpPost.setEntity(baltradFrame);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                resEntity.consumeContent();
            }
            httpClient.getConnectionManager().shutdown();
        } catch (IOException e) {
            msg.showMessage("Frame handler error:" + e.getMessage(), verbose);
        }
    }

    /**
     * Creates XML header for file frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param userName
     *            User's name
     * @param passwd
     *            User's password
     * @param nodeName
     *            Sender node name
     * @param channel
     *            Channel of origin
     * @param absFilePath
     *            Absolute file path
     * @return XML header as string
     */
    public String createDataHdr(String mimeType, String userName,
            String passwd, String nodeName, String channel, String absFilePath) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set user name
            header.setAttribute(USR_NAME, userName);
            // set password
            header.setAttribute(PASSWD, passwd);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            root.appendChild(header);
            // File object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, FILE);
            content.setAttribute(FILE_NAME, absFilePath.substring(absFilePath
                    .lastIndexOf(File.separator) + 1, absFilePath.length()));
            content.setAttribute(CHNL_NAME, channel);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Creates XML header for file frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param nodeName
     *            Sender node name
     * @param channel
     *            Channel of origin
     * @param absFilePath
     *            Absolute file path
     * @return XML header as string
     */
    public String createDataHdr(String mimeType, String nodeName,
            String channel, String absFilePath) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            root.appendChild(header);
            // File object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, FILE);
            content.setAttribute(FILE_NAME, absFilePath.substring(absFilePath
                    .lastIndexOf(File.separator) + 1, absFilePath.length()));
            content.setAttribute(CHNL_NAME, channel);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Creates XML header for message frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param userName
     *            User's name
     * @param passwd
     *            User's password
     * @param nodeAddress
     *            Sender node address
     * @param nodeName
     *            Sender node name
     * @param msgClass
     *            Message class
     * @param msgText
     *            Message text
     * @return XML header as string
     */
    public String createMsgHdr(String mimeType, String userName, String passwd,
            String nodeAddress, String nodeName, String msgClass, String msgText) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set user name
            header.setAttribute(USR_NAME, userName);
            // set password
            header.setAttribute(PASSWD, passwd);
            // set sender node address
            header.setAttribute(SNDR_ADDR, nodeAddress);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            root.appendChild(header);
            // Message object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, MSG);
            content.setAttribute(MSG_CLASS, msgClass);
            content.setAttribute(MSG_TEXT, msgText);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Creates XML header for message frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param nodeAddress
     *            Sender node address
     * @param nodeName
     *            Sender node name
     * @param msgClass
     *            Message class
     * @param msgText
     *            Message text
     * @return XML header as string
     */
    public String createMsgHdr(String mimeType, String nodeAddress,
            String nodeName, String msgClass, String msgText) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set sender node address
            header.setAttribute(SNDR_ADDR, nodeAddress);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            root.appendChild(header);
            // Message object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, MSG);
            content.setAttribute(MSG_CLASS, msgClass);
            content.setAttribute(MSG_TEXT, msgText);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Creates XML header for object frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param nodeAddress
     *            Sender node address
     * @param nodeName
     *            Sender node name
     * @param msgText
     *            Message text
     * @param absFilePath
     *            Absolute path to the object-holding file
     * @return XML header as string
     */
    public String createObjectHdr(String mimeType, String nodeAddress,
            String nodeName, String msgText, String absFilePath) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set sender node addressheader
            header.setAttribute(SNDR_ADDR, nodeAddress);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            root.appendChild(header);
            // Message object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, OBJECT);
            content.setAttribute(MSG_TEXT, msgText);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Creates XML header for object frame.
     * 
     * @param mimeType
     *            MIME message type
     * @param nodeAddress
     *            Sender node address
     * @param nodeName
     *            Sender node name
     * @param localUserName
     *            User name on the local (receiving) server
     * @param msgText
     *            Message text
     * @param absFilePath
     *            Absolute path to the object-holding file
     * @return XML header as string
     */
    public String createObjectHdr(String mimeType, String nodeAddress,
            String nodeName, String localUserName, String msgText,
            String absFilePath) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // Create document root
            Element root = doc.createElement(ROOT_ELEM);
            doc.appendChild(root);
            // Header definition tag
            Element header = doc.createElement(HDR);
            // MIME content-type identifier
            header.setAttribute(MIME_TYPE, mimeType);
            // set sender node addressheader
            header.setAttribute(SNDR_ADDR, nodeAddress);
            // set sender node name
            header.setAttribute(SNDR_NAME, nodeName);
            // set local user name
            header.setAttribute(USR_NAME, localUserName);
            root.appendChild(header);
            // Message object definition tag
            Element content = doc.createElement(CONTENT);
            content.setAttribute(CONTENT_TYPE, OBJECT);
            content.setAttribute(MSG_TEXT, msgText);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage(
                    "XML parser error:" + e.getMessage(), verbose);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Method transforms XML document into string.
     * 
     * @param xmlVersion
     *            XML document version
     * @param xmlEncoding
     *            XML document encoding
     * @param doc
     *            XML document
     * @return XML document as string
     */
    public String xmlDocToString(String xmlVersion, String xmlEncoding,
            Document doc) {
        OutputFormat format = new OutputFormat(doc);
        format.setVersion(xmlVersion);
        format.setEncoding(xmlEncoding);
        format.setIndenting(true);
        StringWriter xmlStringWriter = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(xmlStringWriter, format);
        try {
            serializer.serialize(doc);
        } catch (IOException e) {
            msg.showMessage(
                    "XML document serialization" + "error: " + e.getMessage(), verbose);
        }
        return xmlStringWriter.toString();
    }

    /**
     * Method transforms string into XML document.
     * 
     * @param xmlString
     *            Input string
     * @return XML document
     */
    public Document stringToXMLDocument(String xmlString) {
        Document doc = null;
        try {
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new StringReader(xmlString)));
            doc = parser.getDocument();
        } catch (SAXException e) {
            msg.showMessage(
                    "String to XML document " + "transformation error: "
                            + e.getMessage(), verbose);
        } catch (IOException e) {
            msg.showMessage(
                    "String to XML document " + "transformation error: "
                            + e.getMessage(), verbose);
        }
        return doc;
    }

    /**
     * Method retrieves given element from XML document
     * 
     * @param doc
     *            XML document
     * @param tagName
     *            Target XML tag name
     * @param attributeName
     *            Target attribute name
     * @return XML element value as string
     */
    public String getXMLHeaderElement(Document doc, String tagName,
            String attributeName) {
        Node node = null;
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.item(0).hasAttributes()) {
            NamedNodeMap map = nodes.item(0).getAttributes();
            node = map.getNamedItem(attributeName);
        }
        return node.getNodeValue();
    }

    /**
     * Method gets message MIME type.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message MIME type
     */
    public String getMimeType(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), HDR,
                MIME_TYPE);
    }

    /**
     * Method gets frame content type
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Frame content type
     */
    public String getContentType(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), CONTENT,
                CONTENT_TYPE);
    }

    /**
     * Method gets user name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return User name
     */
    public String getUserName(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), HDR,
                USR_NAME);
    }

    /**
     * Method gets user's password.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return User's password
     */
    public String getPassword(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), HDR, PASSWD);
    }

    /**
     * Gets sender node address.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Sender node address
     */
    public String getSenderNodeAddress(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), HDR,
                SNDR_ADDR);
    }

    /**
     * Gets sender node name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Sender node name
     */
    public String getSenderNodeName(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), HDR,
                SNDR_NAME);
    }

    /**
     * Method gets data channel name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Data channel name
     */
    public String getChannel(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), CONTENT,
                CHNL_NAME);
    }

    /**
     * Method gets data file name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Data file name
     */
    public String getFileName(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), CONTENT,
                FILE_NAME);
    }

    /**
     * Method gets message type.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message type identifier
     */
    public String getMessageClass(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), CONTENT,
                MSG_CLASS);
    }

    /**
     * Method gets message text.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message text
     */
    public String getMessageText(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), CONTENT,
                MSG_TEXT);
    }

    /**
     * Method gets receiver's URL address.
     * 
     * @return Receiver's URL address
     */
    public String getUrl() {
        return url;
    }

    /**
     * Method sets receiver's URL address.
     * 
     * @param url
     *            Receiver's URL address
     */
    public void setUrl(String url) {
        this.url = url;
    }

   
}
// --------------------------------------------------------------------------------------------------