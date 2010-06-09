/*
 * BaltradNode :: Radar data exchange and communication system
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2010
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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
 * @author szewczenko
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
    private static final String BF_ROOT_ELEM = "baltrad_frame";
    // XML element / header node
    private static final String BF_HEADER = "header";
    // XML element / MIME type attribute
    private static final String BF_MIME_TYPE = "mimetype";
    // XML elements / available MIME types
    public static final String BF_MIME_MULTIPART = "multipart/form-data";
    public static final String BF_MIME_APPLICATION = "application/octet-stream";
    public static final String BF_MIME_TEXT = "text/plain";
    public static final String BF_MIME_TEXT_XML = "text/xml";
    // XML element / frame sender attribute
    private static final String BF_SENDER = "sender";
    // XML element / content node
    private static final String BF_CONTENT = "content";
    // XML element / content type
    private static final String BF_CONTENT_TYPE = "type";
    // XML element / message content node
    public static final String BF_MSG_CONTENT = "message";
    // XML element / message type attribute
    private static final String BF_MSG_CLASS = "class";
    // XML elements / message type attribute values
    public static final String BF_MSG_INFO = "INFO";
    public static final String BF_MSG_WRN = "WARNING";
    public static final String BF_MSG_ERR = "ERROR";
    // XML element / message text attribute
    private static final String BF_MSG_TEXT = "text";
    // XML element / file content node
    public static final String BF_FILE_CONTENT = "file";
    // XML element / file id
    public static final String BF_FILE_ID = "file_id";
    // XML element / relative file name attribute
    private static final String BF_FILE_NAME = "name";
    // XML element / data channel attribute
    private static final String BF_CHANNEL_NAME = "channel";
    // Character set
//    private static final Charset BF_CHARSET = Charset.forName(XML_ENCODING);
    // ----------------------------------------------------------------------------------------
    // Variables
    // Receiver's URL address
    private String url;
    // Reference to LoManager class object
    private MessageLogger msg;

    // ------------------------------------------------------------------------------------------
    // Methods
    /**
     * Default constructor.
     */
    public BaltradFrameHandler(MessageLogger msg) {
        this.msg = msg;
    }

    /**
     * Constructor sets field values.
     * 
     * @param url
     *            Receiver's URL address
     */
    public BaltradFrameHandler(MessageLogger msg, String url) {

        this.url = url;
        this.msg = msg;
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
            msg.showMessage("Frame handler error:" + e.getMessage(), true);
        }
    }

    /**
     * Method creates Baltrad data frame XML header.
     * 
     * @param mimeType
     *            MIME message type
     * @param sender
     *            Sender's address
     * @param channel
     *            Channel of origin
     * @param absFilePath
     *            Absolute file path
     * @param id
     *            File object id
     */
    public String createBFDataHdr(String mimeType, String sender,
            String channel, String absFilePath) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();

            // Create document root
            Element root = doc.createElement(BF_ROOT_ELEM);
            doc.appendChild(root);

            // Header definition tag
            Element header = doc.createElement(BF_HEADER);

            // MIME content-type identifier
            header.setAttribute(BF_MIME_TYPE, mimeType);
            header.setAttribute(BF_SENDER, sender);
            root.appendChild(header);

            // File object definition tag
            Element content = doc.createElement(BF_CONTENT);
            content.setAttribute(BF_CONTENT_TYPE, BF_FILE_CONTENT);
            content.setAttribute(BF_FILE_NAME, absFilePath.substring(
                    absFilePath.lastIndexOf(File.separator) + 1, absFilePath
                            .length()));
            content.setAttribute(BF_CHANNEL_NAME, channel);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage("Frame handler error:" + e.getMessage(), true);
        }
        // Transform XML header to string
        return xmlDocToString(XML_VERSION, XML_ENCODING, doc);
    }

    /**
     * Method creates Baltrad message frame XML header.
     * 
     * @param mimeType
     *            MIME message type
     * @param sender
     *            Sender's address
     * @param msgType
     *            Message type
     * @param msgText
     *            Message text
     */
    public String createBFMsgHdr(String mimeType, String sender,
            String msgType, String msgText) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();

            // Create document root
            Element root = doc.createElement(BF_ROOT_ELEM);
            doc.appendChild(root);

            // Header definition tag
            Element header = doc.createElement(BF_HEADER);

            // MIME content-type identifier
            header.setAttribute(BF_MIME_TYPE, mimeType);
            header.setAttribute(BF_SENDER, sender);
            root.appendChild(header);

            // Message object definition tag
            Element content = doc.createElement(BF_CONTENT);
            content.setAttribute(BF_CONTENT_TYPE, BF_MSG_CONTENT);
            content.setAttribute(BF_MSG_CLASS, msgType);
            content.setAttribute(BF_MSG_TEXT, msgText);
            root.appendChild(content);
        } catch (ParserConfigurationException e) {
            msg.showMessage("XML parser error:" + e.getMessage(), true);
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
            msg.showMessage("XML document serialization error: "
                    + e.getMessage(), true);

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
            msg.showMessage("String to XML document transformation error: "
                    + e.getMessage(), true);
        } catch (IOException e) {
            msg.showMessage("String to XML document transformation error: "
                    + e.getMessage(), true);
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
    public String getBFMimeType(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_HEADER,
                BF_MIME_TYPE);
    }

    /**
     * Method gets message sender.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message sender name
     */
    public String getBFSender(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_HEADER,
                BF_SENDER);
    }

    /**
     * Method gets frame content type
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Frame content type
     */
    public String getBFContentType(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_CONTENT_TYPE);
    }

    /**
     * Method data channel name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Data channel name
     */
    public String getBFChannel(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_CHANNEL_NAME);
    }

    /**
     * Method gets data file id.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Data file id
     */
    public String getBFFileId(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_FILE_ID);
    }

    /**
     * Method gets data file name.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Data file name
     */
    public String getBFFileName(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_FILE_NAME);
    }

    /**
     * Method gets message type.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message type identifier
     */
    public String getBFMessageClass(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_MSG_CLASS);
    }

    /**
     * Method gets message text.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @return Message text
     */
    public String getBFMessageText(String xmlHdrStr) {
        return getXMLHeaderElement(stringToXMLDocument(xmlHdrStr), BF_CONTENT,
                BF_MSG_TEXT);
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

    /**
     * Method gets reference to LogManager class instance.
     * 
     * @return Reference to LogManager class instance
     */
    public MessageLogger getMessageLogger() {
        return msg;
    }
}
// --------------------------------------------------------------------------------------------------
