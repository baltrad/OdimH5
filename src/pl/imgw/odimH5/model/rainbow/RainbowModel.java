/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.model.rainbow;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.LogsHandler;
import pl.imgw.odimH5.util.MessageLogger;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZStream;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * Class encapsulating data processing methods for RAINBOW platform.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */

public class RainbowModel {

    public final int PRODUCT = 0;
    public final int VOLUME = 1;

    // Constants

    protected final String H5_ROOT = "/";
    protected final String H5_CONV = "ODIM_H5/V2_1";
    protected final String H5_GROUP = "group";
    protected final String H5_OBJECT_NAME = "name";
    protected final String H5_OBJECT_CLASS = "class";
    protected final String H5_WHAT = "what";
    protected final String H5_WHERE = "where";
    protected final String H5_HOW = "how";
    protected final String H5_ATTRIBUTE = "attribute";
    protected final String H5_DATASET = "dataset";
    protected final String H5_DATASET_N = "dataset1";
    protected final String H5_DATA = "data";
    protected final String H5_DATA_1 = "data1";
    protected final String H5_STRING = "string";
    protected final String H5_INTEGER = "integer";
    protected final String H5_DATA_SIZE = "32";
    protected final String H5_LONG = "long";
    protected final String H5_DOUBLE = "double";
    protected final String H5_SEQUENCE = "sequence";

    protected final String IMAGE_VER = "1.2";
    protected final String H5_DATA_CHUNK = "64";
    protected final String H5_GZIP_LEVEL = "6";

    protected final String EARTH_RAD = "6371000";

    protected final String TYPE_DBZ = "dBZ";
    protected final String TYPE_DBR = "dBR";
    protected final String TYPE_DBA = "dBA";
    protected final String TYPE_V = "V";
    protected final String TYPE_H = "Height";

    protected final String UPHIDP = "uPhiDP";
    protected final String DBZ = "dBZ";

    protected final String QNT_TH = "TH";
    protected final String QNT_RATE = "RATE";
    protected final String QNT_ACRR = "ACRR";
    protected final String QNT_VRAD = "VRAD";
    protected final String QNT_H = "HGHT";

    public final String VERSION = "H5rad 2.0";
    protected final String RAINBOW_SYSTEM = "GEMA";
    protected final String RAINBOW_SOFTWARE = "RAINBOW";
    protected final String RAINBOW_SCAN = "SCAN";
    public final String IMAGE = "IMAGE";
    public final String PVOL = "PVOL";
    public final String VP = "VP";
    public final String RHI = "RHI";

    private final String blobid = "blobid";
    private final String size = "size";
    private final String compression = "compression";

    protected final double FIRST_VALUE = 1.0;
    protected final double RAINBOW_NO_DATA = 255.0;
    protected final double RAINBOW_UNDETECT = 0.0;
    
    public static final String VER51X = "5.1";
    public static final String VER52X = "5.2";
    public static final String VER53X = "5.3";

    // public static final String BRZ = "WMO:12568";
    // public static final String GDA = "WMO:12151";
    // public static final String LEG = "WMO:12374";
    // public static final String PAS = "WMO:12544";
    // public static final String POZ = "WMO:12331";
    // public static final String RAM = "WMO:12514";
    // public static final String RZE = "WMO:12579";
    // public static final String SWI = "WMO:12220";

    // Reference to MessageLogger object
    private MessageLogger msgl;
    // Reference to DataProcessorModel object
    HDF5Model hdf;

    /**
     * Method returns reference to MessageLogger object.
     * 
     * @return Reference to MessageLogger object
     */
    public MessageLogger getMessageLogger() {
        return msgl;
    }

    /**
     * Method sets reference to MessageLogger object
     * 
     * @param msgl
     *            Reference to MessageLogger object
     */
    public void setMessageLogger(MessageLogger msgl) {
        this.msgl = msgl;
    }

    /**
     * Method returns reference to DataProcessorModel object.
     * 
     * @return Reference to DataProcessorModel object
     */
    public HDF5Model getHDFModel() {
        return hdf;
    }

    /**
     * Method sets reference to DataProcessorModel object
     * 
     * @param proc
     *            Reference to DataProcessorModel object
     */
    public void setHDFModel(HDF5Model proc) {
        this.hdf = proc;
    }

    /**
     * Method reads metadata header from Rainbow file. Header is stored in a
     * memory buffer.
     * 
     * @param file_buf
     *            File buffer
     * @param format
     *            PRODUCT or VOLUME
     * @param verbose
     *            Verbose mode toggle
     * @return Byte array containing XML header
     */
    public byte[] getRAINBOWMetadata(byte[] file_buf, int format,
            boolean verbose) {

        // Header end tag
        String END_XML = "";
        if (format == VOLUME) {
            END_XML = "</volume>";
        } else if (format == PRODUCT) {
            END_XML = "</product>";
        }

        int len = END_XML.length();

        // Tag buffer
        byte[] end_xml_buf = new byte[len + 1];
        // Header buffer
        byte[] hdr_buf = null;
        // Tag string
        String end_xml_seq = "";
        // Current offset
        int offset = 0;
        // Header end marker
        int end_xml = 0;
        // Seek for header end
        try {
            while (offset < file_buf.length) {
                end_xml_buf[len] = file_buf[offset];
                for (int i = 1; i <= len; i++) {
                    end_xml_buf[i - 1] = end_xml_buf[i];
                }
                for (int i = 0; i < len; i++) {
                    end_xml_seq += (char) end_xml_buf[i];
                }
                if (end_xml_seq.matches(END_XML)) {
                    end_xml = offset;
                }
                end_xml_seq = "";
                offset++;
            }
            // Read data into header array
            end_xml += 1;
            hdr_buf = new byte[end_xml];
            for (int i = 0; i < end_xml; i++) {
                hdr_buf[i] = file_buf[i];
            }
            msgl.showMessage("Reading RAINBOW metadata header", verbose);
        } catch (Exception e) {
            msgl.showMessage("Error while reading RAINBOW metadata header",
                    verbose);
        }
        return hdr_buf;
    }

    /**
     * Method reads data section from Rainbow file. Data section is stored in a
     * memory buffer.
     * 
     * @param file_buf
     *            File buffer
     * @param verbose
     *            Verbose mode toggle
     * @return Byte array containing data section
     */
    public byte[] getRAINBOWData_nieuzywane(byte[] file_buf, boolean verbose) {
        // Data section tags
        final String START_BIN = "<BLOB";
        final String END_BIN = "</BLOB>";
        // Tag buffers
        byte[] start_bin_buf = new byte[6];
        byte[] end_bin_buf = new byte[8];
        // Data buffer
        byte[] data_buf = null;
        // Tag strings
        String start_bin_seq = "";
        String end_bin_seq = "";
        // Current offset
        int offset = 0;
        // Data section markers
        int start_bin = 0;
        int end_bin = 0;
        // Seek for data section
        try {
            while (offset < file_buf.length) {
                // Data section start
                start_bin_buf[5] = file_buf[offset];
                for (int i = 1; i <= 5; i++) {
                    start_bin_buf[i - 1] = start_bin_buf[i];
                }
                for (int i = 0; i < 5; i++) {
                    start_bin_seq += (char) start_bin_buf[i];
                }
                if (start_bin_seq.matches(START_BIN)) {
                    while ((char) file_buf[offset] != '>') {
                        offset++;
                    }
                    start_bin = offset;
                }
                // Data section end
                end_bin_buf[7] = file_buf[offset];
                for (int i = 1; i <= 7; i++) {
                    end_bin_buf[i - 1] = end_bin_buf[i];
                }
                for (int i = 0; i < 7; i++) {
                    end_bin_seq += (char) end_bin_buf[i];
                }
                if (end_bin_seq.matches(END_BIN)) {
                    end_bin = offset;
                    break;
                }
                start_bin_seq = "";
                end_bin_seq = "";
                offset++;
            }
            // Read data into data array
            start_bin += 6;
            end_bin -= 6;
            int bin_count = 0;
            data_buf = new byte[end_bin - start_bin];
            for (int i = start_bin; i < end_bin; i++) {
                data_buf[bin_count] = file_buf[i];
                bin_count++;
            }
            msgl.showMessage("Reading RAINBOW data section", verbose);
        } catch (Exception e) {
            msgl.showMessage("Error while reading RAINBOW data section",
                    verbose);
        }
        return data_buf;
    }

    /**
     * Method reads Rainbow mask section from Rainbow file. Mask section is
     * stored in a memory buffer.
     * 
     * @param file_buf
     *            File buffer
     * @param verbose
     *            Verbose mode toggle
     * @return Byte array containing mask section
     */
    public DataBufferContainer getRAINBOWMaskSection_nieuzywana(
            byte[] file_buf, boolean verbose) {
        // Create an instance of MaskSection class
        DataBufferContainer dataBuff = new DataBufferContainer();
        // Mask section length
        int buff_len = 0;
        // Data section tags
        final String START_BIN = "<BLOB";
        final String END_BIN = "</BLOB>";
        // Tag buffers
        byte[] sec_start = new byte[6];
        byte[] sec_end = new byte[8];
        // Data buffer
        byte[] sec_buf = null;
        // Tag strings
        String start_seq = "";
        String end_seq = "";
        // Current offset
        int offset = 0;
        // Data section markers
        int start_pos = 0;
        int end_pos = 0;
        // Blob tag counters
        int start_blob_count = 0;
        int end_blob_count = 0;
        // Seek for mask section
        try {
            while (offset < file_buf.length) {
                // Mask section start
                sec_start[5] = file_buf[offset];
                for (int i = 1; i <= 5; i++) {
                    sec_start[i - 1] = sec_start[i];
                }
                for (int i = 0; i < 5; i++) {
                    start_seq += (char) sec_start[i];
                }
                if (start_seq.matches(START_BIN)) {
                    start_blob_count++;
                    if (start_blob_count > 1) {
                        while ((char) file_buf[offset] != '>') {
                            offset++;
                        }
                        start_pos = offset;
                    }
                }
                // Mask section end
                sec_end[7] = file_buf[offset];
                for (int i = 1; i <= 7; i++) {
                    sec_end[i - 1] = sec_end[i];
                }
                for (int i = 0; i < 7; i++) {
                    end_seq += (char) sec_end[i];
                }
                if (end_seq.matches(END_BIN)) {
                    end_blob_count++;
                    if (end_blob_count > 1) {
                        end_pos = offset;
                        break;
                    }
                }
                start_seq = "";
                end_seq = "";
                offset++;
            }
            // Read 4 bytes representing mask length
            byte[] mask_byte = new byte[4];
            for (int i = 0; i < 4; i++) {
                mask_byte[i] = file_buf[start_pos + i + 2];
            }
            // Read data into mask array
            start_pos += 6;
            end_pos -= 6;
            int mask_count = 0;
            sec_buf = new byte[end_pos - start_pos];
            for (int i = start_pos; i < end_pos; i++) {
                sec_buf[mask_count] = file_buf[i];
                mask_count++;
            }
            buff_len = byteArray2Int(mask_byte);
            dataBuff.setDataBufferLength(buff_len);
            dataBuff.setDataBuffer(sec_buf);
            msgl.showMessage("Reading RAINBOW mask section", verbose);
        } catch (Exception e) {
            msgl.showMessage("Error while reading RAINBOW mask section",
                    verbose);
        }
        return dataBuff;
    }

    /**
     * Method converts byte array value into integer value.
     * 
     * @param b
     *            Byte array
     * @return Integer
     */
    public int byteArray2Int(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * Method parses RAINBOW metadata buffer
     * 
     * @param hdrBuff
     *            RAINBOW metadata buffer
     * @param verbose
     *            Verbose mode toggle
     * @return XML document object
     */
    public Document parseRAINBOWMetadataBuffer(byte[] hdrBuff, boolean verbose) {

        Document doc = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(hdrBuff);
            DOMParser parser = new DOMParser();
            InputSource is = new InputSource(bis);
            parser.parse(is);
            doc = parser.getDocument();
            msgl.showMessage("Parsing RAINBOW metadata buffer", verbose);
        } catch (Exception e) {
            msgl.showMessage("Error while parsing RAINBOW metadata buffer: "
                    + e.getMessage(), verbose);
        }
        return doc;
    }

    /**
     * Method creates XML Element.
     * 
     * @param attributeName
     *            Attribute's name
     * @param attributeValue
     *            Attribute's value
     * @param outputDoc
     *            XML document object
     * @param type
     *            Type of the attribute
     * @return XML Element
     */
    public Element makeAttr(String attributeName, String attributeValue,
            Document outputDoc, String type) {

        Element attribute = outputDoc.createElement(H5_ATTRIBUTE);
        attribute.setAttribute(H5_OBJECT_NAME, attributeName);
        if (!type.isEmpty())
            attribute.setAttribute(H5_OBJECT_CLASS, type);
        Text text = outputDoc.createTextNode(attributeValue);
        attribute.appendChild(text);

        return attribute;
    }

    /**
     * Method creates XML tag element with given name and value.
     * 
     * @param tagName
     * @param value
     * @param od
     * @return
     */
    public Element makeTag(String tagName, String value, Document od) {
        Element tag = od.createElement(tagName);
        Text text = od.createTextNode(value);
        tag.appendChild(text);
        return tag;

    }

    /**
     * Method retrieves a list of nodes from XML document. Nodes are identified
     * by tag name.
     * 
     * @param doc
     *            XML document object
     * @param nodeName
     *            Node tag name
     * @param verbose
     *            Verbose mode toggle
     * @return List of XML nodes
     */
    public NodeList getRAINBOWNodesByName(Document doc, String nodeTagName,
            boolean verbose) {
        NodeList nodeList = null;
        nodeList = doc.getElementsByTagName(nodeTagName);
        if (nodeList.getLength() == 0) {
            msgl.showMessage("Metadata header element not found: "
                    + nodeTagName, verbose);
        }
        return nodeList;
    }

    /**
     * Method retrieves an element of RAINBOW metadata header. Element is
     * identified by its name and return its value.
     * 
     * @param nodeList
     *            List of top level nodes in XML document
     * @param argName
     *            Argument name
     * @param verbose
     *            Verbose mode toggle
     * @return Element value
     */
    public String getRAINBOWMetadataElement(NodeList nodeList, String argName,
            boolean verbose) {

        String value = "";
        Node node = null;
        Node attrNode = null;
        NamedNodeMap nodeMap = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (!argName.trim().isEmpty()) {
                nodeMap = node.getAttributes();
                for (int j = 0; j < nodeMap.getLength(); j++) {
                    attrNode = nodeMap.item(j);
                    if (attrNode.getNodeName().equals(argName)) {
                        value = attrNode.getNodeValue();
                    }
                }
            } else {
                value = node.getFirstChild().getNodeValue();
            }
        }
        if (value.trim().isEmpty()) {
            msgl.showMessage("Invalid metadata header element", verbose);
        }
        return value;
    }

    /**
     * Method retrieves attribute's value of RAINBOW metadata header. Attribute
     * is identified by its parent Element and its name.
     * 
     * @param node
     *            List of top level nodes in XML document
     * @param elemName
     *            name of the parent Element
     * @param atrName
     *            name of the attribute
     * @return attribute value
     */
    public static String getValueByName(Node node, String elemName,
            String atrName) {

        String value = null;
        int type = node.getNodeType();
        if (type == Node.DOCUMENT_NODE) {
            value = getValueByName(((Document) node).getDocumentElement(),
                    elemName, atrName);
        }
        if (type == Node.ELEMENT_NODE) {

            if (atrName != null && node.getNodeName().equals(elemName)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    value = getValueByName(attrs.item(i), elemName, atrName);
                    if (value != null) {
                        return value;
                    }
                }
            } else if (atrName == null && node.getNodeName().equals(elemName)
                    && node.hasChildNodes()) {
                return node.getFirstChild().getNodeValue();

            }
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    value = getValueByName(children.item(i), elemName, atrName);
                    if (value != null) {
                        return value;
                    }
                }
            }
        } else if (type == Node.ATTRIBUTE_NODE) {

            if (node.getNodeName().equals(atrName)) {
                return value = node.getNodeValue();
            }
        }

        return value;
    }

    /**
     * Method parses RAINBOW date and converts it to ODIM_H5 standard date.
     * 
     * @param rainbowDate
     *            Input date string
     * @param verbose
     *            Verbose mode toggle
     * @return Date string in ODIM_H5 format
     */
    public String parseRAINBOWDate(String rainbowDate, boolean verbose) {

        String h5Date = null;
        try {
            Format in = new SimpleDateFormat("yyyy-MM-dd");
            Format out = new SimpleDateFormat("yyyyMMdd");
            Date date = (Date) in.parseObject(rainbowDate);
            h5Date = out.format(date);
        } catch (ParseException e) {
            msgl.showMessage("Error while parsing date: " + e.getMessage(),
                    verbose);
        }
        return h5Date;
    }

    /**
     * Method add shift number of second to rainbow time.
     * 
     * @param rainbowTime
     *            Input time string
     * @param shift
     *            Number of seconds to shift
     * @param verbose
     *            Verbose mode toggle
     * @return Time string in ODIM_H5 format
     */
    public String parseRAINBOWTime(String rainbowTime, int shift,
            boolean verbose) {
        String h5Time = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            Format in_out = new SimpleDateFormat("HHmmss");
            Date date = (Date) in_out.parseObject(rainbowTime);
            cal.setTime(date);
            cal.add(Calendar.SECOND, shift);
            date = cal.getTime();
            h5Time = in_out.format(date);
        } catch (ParseException e) {
            msgl.showMessage("Error while parsing date: " + e.getMessage(),
                    verbose);
        }
        return h5Time;
    }

    /**
     * Method parses RAINBOW time and converts is to ODIM_H5 standard time.
     * 
     * @param rainbowTime
     *            Input time string
     * @param verbose
     *            Verbose mode toggle
     * @return Time string in ODIM_H5 format
     */
    public String parseRAINBOWTime(String rainbowTime, boolean verbose) {
        String h5Time = null;
        try {
            Format in = new SimpleDateFormat("HH:mm:ss");
            Format out = new SimpleDateFormat("HHmmss");
            Date date = (Date) in.parseObject(rainbowTime);
            h5Time = out.format(date);
        } catch (ParseException e) {
            msgl.showMessage("Error while parsing date: " + e.getMessage(),
                    verbose);
        }
        return h5Time;
    }

    /**
     * Method converts RAINBOW date to epoch date
     * 
     * @param rainbowDate
     *            RAINBOW date string
     * @param rainbowTime
     *            RAINBOW time string
     * @param verbose
     *            Verbose mode toggle
     * @return Epoch time string
     */
    public String convertRAINBOWDate2Epoch(String rainbowDate,
            String rainbowTime, boolean verbose) {
        String epoch = null;
        Calendar cal = Calendar.getInstance();
        try {
            Format in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = rainbowDate + " " + rainbowTime;
            Date date = (Date) in.parseObject(dateTime);
            cal.setTime(date);
            long time = cal.getTimeInMillis();
            epoch = String.valueOf(time);
        } catch (ParseException e) {
            msgl.showMessage("Error while parsing date: " + e.getMessage(),
                    verbose);
        }
        return epoch;
    }

    /**
     * Method converts RAINBOW metadata parameter
     * 
     * @param param
     *            Input parameter
     * @return Converted parameter string
     */
    public String convertRAINBOWParam(String param) {
        String outputParam = param.replaceAll("@", "");
        return outputParam.replace(" to ", ",");
    }

    /**
     * Method converts kilometers to meters in RAINBOW parameter
     * 
     * @param param
     *            Input parameter
     * @return Converted parameter string
     */
    public String convertKMtoM(String param) {

        String temp = "";
        double a = 0;
        int pos = 0;
        if (param.contains(",")) {
            pos = param.indexOf(",");
            temp = param.substring(0, pos++);

            a = Double.parseDouble(temp);
            temp = String.valueOf(a * 1000) + ",";
        }
        a = Double.parseDouble(param.substring(pos));
        temp += String.valueOf(a * 1000);
        System.out.println(temp);
        return temp;
    }

    /**
     * Method computes length of time interval in minutes
     * 
     * @param interval
     *            Input parameter
     * @return length in minutes
     */
    public String convertTimeInterval(String interval) {
        int i = 0;
        i = interval.indexOf("@", i) + 1;
        int minutes = Integer.parseInt(interval.substring(i++,
                interval.indexOf(" d"))) * 24 * 60;
        i = interval.indexOf("@", i) + 1;
        minutes += Integer.parseInt(interval.substring(i++,
                interval.indexOf(" h"))) * 60;
        i = interval.indexOf("@", i) + 1;
        minutes += Integer.parseInt(interval.substring(i,
                interval.indexOf(" m")));

        return String.valueOf(minutes);
    }

    /**
     * Method determines RAINBOW quantity value
     * 
     * @param type
     *            Data type paramater
     * @return Quantity string
     */
    public String getRAINBOWQuantity(String type) {
        String quantity = null;
        if (type.equals("dBZ")) {
            quantity = "DBZH";
        }

        // add further parameters here...

        return quantity;
    }

    /**
     * Method calculates gain parameter
     * 
     * @param min
     *            Data range minimum
     * @param max
     *            Data range maximum
     * @param res
     *            Data resolution
     * @return Gain parameter string
     */
    public String getRAINBOWGain(String min, String max, int res) {
        String gain = null;

        double minVal = 0;
        double maxVal = 0;
        try {
            minVal = Double.parseDouble(min);
            maxVal = Double.parseDouble(max);
        } catch (NumberFormatException e) {
            LogsHandler.saveProgramLogs("RainbowModel", e.getMessage());
        }
        double gainVal = (Math.abs(minVal) + Math.abs(maxVal))
                / (Math.pow(2, res) - 2);
        gain = String.valueOf(round(gainVal, 6));
        return gain;
    }

    /**
     * Method calculate offset parameter
     * 
     * @param min
     *            Minimal value of data
     * 
     * @param step
     * 
     * @return
     */
    public String getRAINBOWOffset(String min, String step) {

        double offset = 0;
        double gain = 0;
        try {
            offset = Double.parseDouble(min);

            gain = Double.parseDouble(step);

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block

            System.out.println(e.getLocalizedMessage());
            return null;
        }

        offset -= (gain * FIRST_VALUE);

        return String.valueOf(offset);
    }

    /**
     * Method parses RAINBOW projection string
     * 
     * @param lon0
     *            Projection's origin longitude
     * @param lat0
     *            Projection's origin latitude
     * @param type
     *            Projection type
     * @param ellps
     *            Ellipsoid type
     * @param radius
     *            Earth's radius
     * @return ODIM_H5 projection string
     */
    public String parseRAINBOWProjection(String lon0, String lat0, String type,
            String ellps, String radius) {
        String projection = null;
        projection = "+proj=" + type + " +lon_0=" + lon0 + " +lat_0=" + lat0
                + " " + ellps + " +a=" + radius;
        return projection;
    }

    /**
     * Method initializes geographic projection.
     * 
     * @param projectionString
     *            Projection definition string
     * @param verbose
     *            Verbose mode toggle
     * @return Reference to initialized Projection object
     */
    public Projection initializeProjection(String projectionString,
            boolean verbose) {

        Projection proj = null;
        String[] projectionParms = projectionString.split(" ");
        try {
            proj = ProjectionFactory.fromPROJ4Specification(projectionParms);
            proj.initialize();
        } catch (ProjectionException e) {
            msgl.showMessage(
                    "Error while initializing projection: " + e.getMessage(),
                    verbose);
        }
        return proj;
    }

    /**
     * Method calculates vertical and horizontal pixel size based on current
     * projection parameters
     * 
     * @param proj
     *            Projection object
     * @param lon_UL
     *            Upper left corner longitude
     * @param lat_UL
     *            Upper left corner latitude
     * @param lon_LR
     *            Lower right corner longitude
     * @param lat_LR
     *            Lower right corner latitude
     * @param xSize
     *            Image horizontal resolution
     * @param ySize
     *            Image vertical resolution
     * @return Pixel size
     */
    public Point2D.Double getRAINBOWXYSize(Projection proj, String lon_UL,
            String lat_UL, String lon_LR, String lat_LR, String xSize,
            String ySize) {
        Point2D.Double in = new Point2D.Double();
        Point2D.Double out = new Point2D.Double();
        Point2D.Double size = new Point2D.Double();

        double lon_ul = Double.parseDouble(lon_UL);
        double lat_ul = Double.parseDouble(lat_UL);
        double lon_lr = Double.parseDouble(lon_LR);
        double lat_lr = Double.parseDouble(lat_LR);
        int xsize = Integer.parseInt(xSize);
        int ysize = Integer.parseInt(ySize);

        in.setLocation(lon_ul, lat_ul);
        proj.transform(in, out);
        size.setLocation(out.getX(), out.getY());
        in.setLocation(lon_lr, lat_lr);
        proj.transform(in, out);
        size.setLocation((-size.getX() + out.getX()) / xsize,
                (size.getY() - out.getY()) / ysize);
        return size;
    }

    /**
     * Method inflates compressed RAINBOW data section with ZLib algorithm
     * 
     * @param input_buf
     *            Compressed buffer
     * @param rays
     *            Output array width
     * @param bins
     *            Output array height
     * @param verbose
     *            Verbose mode toggle
     * @return Inflated data section as an array of integers
     */
    public int[][] inflate2DRAINBOWDataSection(DataBufferContainer dbc,
            int rays, int bins, boolean verbose) {
        byte[] input_buf = dbc.getDataBuffer();
        msgl.showMessage("Inflating RAINBOW 2D data section", verbose);
        int[][] output_buf = new int[rays][bins];
        int len = rays * bins;
        byte[] byte_buf = new byte[len];

        if (dbc.getCompression() == 0) {
            if (rays * bins != dbc.getDataBufferLength()) {
                msgl.showMessage("Incorrect data size!!", true);
                return null;
            }

            byte_buf = dbc.getDataBuffer();
        } else {

            // Inflate input stream
            ZStream defStream = new ZStream();
            defStream.next_in = input_buf;
            defStream.next_in_index = 0;
            defStream.next_out = byte_buf;
            defStream.next_out_index = 0;
            int err = defStream.inflateInit();
            checkErr(defStream, err, "Inflation initialization error", verbose);
            int a = 0;
            while (defStream.total_out < len
                    && defStream.total_in < input_buf.length) {
                defStream.avail_in = defStream.avail_out = 1;
                err = defStream.inflate(JZlib.Z_NO_FLUSH);
                if (err == JZlib.Z_STREAM_END)
                    break;
                a++;
                checkErr(defStream, err, "Inflation error", verbose);
            }
            // System.out.println(a);
            err = defStream.inflateEnd();
            checkErr(defStream, err, "Inflation end error", verbose);
        }

        // Convert byte array into integer array
        int count = 0;
        for (int x = 0; x < rays; x++) {
            for (int y = 0; y < bins; y++) {
                count = y * rays + x;
                output_buf[x][y] = (int) byte_buf[count] & 0xFF;
            }
        }
        return output_buf;
    }

    /**
     * Method inflates compressed Rainbow data section with ZLib algorithm
     * 
     * @param maskSection
     *            Instance of Mask Section class containing compressed data
     * @param verbose
     *            Verbose mode toggle
     * @return Inflated data section as byte array
     */
    public byte[] inflate1DRAINBOWDataSection(DataBufferContainer dbc,
            boolean verbose) {

        int length = dbc.getDataBufferLength();
        byte[] dataBuff = dbc.getDataBuffer();

        msgl.showMessage("Inflating RAINBOW 1D data section", verbose);
        byte[] output_buf = new byte[length];
        ZStream defStream = new ZStream();
        defStream.next_in = dataBuff;
        defStream.next_in_index = 0;
        defStream.next_out = output_buf;
        defStream.next_out_index = 0;
        int err = defStream.inflateInit();
        checkErr(defStream, err, "Inflation initialization error", verbose);
        while (defStream.total_out < length
                && defStream.total_in < dataBuff.length) {
            defStream.avail_in = defStream.avail_out = 1;
            err = defStream.inflate(JZlib.Z_NO_FLUSH);
            if (err == JZlib.Z_STREAM_END)
                break;
            checkErr(defStream, err, "Inflation error", verbose);
        }
        err = defStream.inflateEnd();
        checkErr(defStream, err, "Inflation end error", verbose);
        return output_buf;
    }

    /**
     * Method checks deflation errors
     * 
     * @param z
     *            Input ZStream
     * @param err
     *            Error code
     * @param msg
     *            Message
     * @param verbose
     *            Verbose mode toggle
     */
    public void checkErr(ZStream z, int err, String msg, boolean verbose) {
        if (err != JZlib.Z_OK) {
            if (z.msg != null) {
                msg += ": " + z.msg;
            }
            msgl.showMessage(err + " " + msg, verbose);
            System.exit(0);
        }
    }

    /**
     * 
     * Function reads Rainbow data section from Rainbow file and puts it into an
     * DataBufferContainer
     * 
     * @param fileBuff
     * @param verbose
     * @return hash map containing blob number and data buffer container
     */
    public HashMap<Integer, DataBufferContainer> getAllRainbowDataBlobs(
            byte[] fileBuff, boolean verbose) {

        HashMap<Integer, DataBufferContainer> blobs = new HashMap<Integer, DataBufferContainer>();

        // Data section tags
        final String START_BIN = "<BLOB";
        final String END_BIN = "</BLOB>";
        // Tag buffers
        byte[] start_bin_buf = new byte[6];
        byte[] end_bin_buf = new byte[8];
        // Data buffer
        byte[] data_buf = null;
        // Tag strings
        String start_bin_seq = "";
        String end_bin_seq = "";
        // Current offset
        int offset = 0;
        // Data section markers
        int start_bin = 0;
        int end_bin = 0;
        // Seek for data section

        int currentBlob = 0;
        HashMap<String, Integer> header = null;

        int current = -1;
        while (offset < fileBuff.length) {
            String xmlHeader = "";
            try {
                while (offset < fileBuff.length) {
                    // Data section start

                    start_bin_buf[5] = fileBuff[offset];
                    for (int i = 1; i <= 5; i++) {
                        start_bin_buf[i - 1] = start_bin_buf[i];
                    }
                    for (int i = 0; i < 5; i++) {
                        start_bin_seq += (char) start_bin_buf[i];
                    }
                    if (start_bin_seq.matches(START_BIN)) {
                        while ((char) fileBuff[offset] != '>') {
                            xmlHeader += (char) fileBuff[offset];
                            offset++;
                        }
                        start_bin = offset;
                        current++;
                    }

                    // Data section end
                    end_bin_buf[7] = fileBuff[offset];
                    for (int i = 1; i <= 7; i++) {
                        end_bin_buf[i - 1] = end_bin_buf[i];
                    }
                    for (int i = 0; i < 7; i++) {
                        end_bin_seq += (char) end_bin_buf[i];
                    }
                    if (end_bin_seq.matches(END_BIN) && current == currentBlob) {
                        end_bin = offset;
                        break;
                    }
                    start_bin_seq = "";
                    end_bin_seq = "";
                    offset++;
                }
                
                if(xmlHeader.isEmpty())
                    continue;
                
                header = getHeader(xmlHeader);
                xmlHeader = "";
                int buffLen = 0;
                if (header.get(compression) == 1) {

                    // Read 4 bytes representing data length
                    // only when compression is set to "qt"
                    byte[] data_byte = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        data_byte[i] = fileBuff[start_bin + i + 2];
                    }
                    buffLen = byteArray2Int(data_byte);
                    start_bin += 4;

                } else {
                    buffLen = header.get(size);
                }
                start_bin += 2;
                end_bin -= 6;
                // Read data into data array
                int bin_count = 0;
                data_buf = new byte[end_bin - start_bin];
                for (int i = start_bin; i < end_bin; i++) {
                    data_buf[bin_count] = fileBuff[i];
                    bin_count++;
                }

                DataBufferContainer dbc = new DataBufferContainer();
                dbc.setDataBuffer(data_buf);
                dbc.setDataBufferLength(buffLen);
                dbc.setCompression(header.get(compression));

                blobs.put(header.get(blobid), dbc);
                msgl.showMessage("Reading RAINBOW data section from BLOB "
                        + header.get(blobid), verbose);
                currentBlob++;

            } catch (Exception e) {
                msgl.showMessage(
                        "Error while reading RAINBOW data section from BLOB "
                                + header.get(blobid), verbose);
            }
        }
        return blobs;
    }

    /**
     * 
     * Helper method. Gets blob parameter from xml header.
     * 
     * @param xmlHeader
     * @return
     */
    private HashMap<String, Integer> getHeader(String xmlHeader) {
        HashMap<String, Integer> header = new HashMap<String, Integer>();

        header.put(blobid, getAttribute(blobid, xmlHeader));
        header.put(size, getAttribute(size, xmlHeader));
        header.put(compression, getAttribute(compression, xmlHeader));

        return header;
    }

    private int getAttribute(String word, String sentance) {

        int start = 0, stop = 0;

        start = sentance.indexOf(word) + word.length() + 2;
        stop = sentance.indexOf("\"", start);
        int value = 0;
        try {
            value = Integer.valueOf(sentance.substring(start, stop));
        } catch (NumberFormatException e) {
            if (sentance.substring(start, stop).equals("qt"))
                return 1;
            else
                return 0;
        }
        return value;

    }

    /**
     * Function reads Rainbow data section from Rainbow file and puts it into an
     * array
     * 
     * @param fileBuff
     *            File buffer
     * @param blobNumber
     *            Number of the blob in the volume file (starting with 1)
     * @param depth
     *            Number of bits used to describe one pixel.
     * @param firstBlob
     *            Starting blob number
     * 
     * @return Byte array containing data section
     */
    public DataBufferContainer getRainbowDataSection(byte[] fileBuff,
            int blobNumber, int firstBlob, boolean verbose) {

        DataBufferContainer dbc = new DataBufferContainer();

        // Data section tags
        final String START_BIN = "<BLOB";
        final String END_BIN = "</BLOB>";
        // Tag buffers
        byte[] start_bin_buf = new byte[6];
        byte[] end_bin_buf = new byte[8];
        // Data buffer
        byte[] data_buf = null;
        // Tag strings
        String start_bin_seq = "";
        String end_bin_seq = "";
        // Current offset
        int offset = 0;
        // Data section markers
        int start_bin = 0;
        int end_bin = 0;
        // Seek for data section

        int current = firstBlob - 1;
        HashMap<String, Integer> header = null;
        String xmlHeader = "";
        try {
            while (offset < fileBuff.length) {
                // Data section start

                start_bin_buf[5] = fileBuff[offset];
                for (int i = 1; i <= 5; i++) {
                    start_bin_buf[i - 1] = start_bin_buf[i];
                }
                for (int i = 0; i < 5; i++) {
                    start_bin_seq += (char) start_bin_buf[i];
                }
                if (start_bin_seq.matches(START_BIN)) {
                    while ((char) fileBuff[offset] != '>') {
                        xmlHeader += (char) fileBuff[offset];
                        offset++;
                    }
                    start_bin = offset;
                    current++;
                }

                // Data section end
                end_bin_buf[7] = fileBuff[offset];
                for (int i = 1; i <= 7; i++) {
                    end_bin_buf[i - 1] = end_bin_buf[i];
                }
                for (int i = 0; i < 7; i++) {
                    end_bin_seq += (char) end_bin_buf[i];
                }
                if (end_bin_seq.matches(END_BIN) && current == blobNumber) {
                    end_bin = offset;
                    break;
                }
                start_bin_seq = "";
                end_bin_seq = "";
                offset++;
            }
            header = getHeader(xmlHeader);
            xmlHeader = "";
            int buffLen = 0;
            if (header.get(compression) == 1) {

                // Read 4 bytes representing data length
                // only when compression is set to "qt"
                byte[] data_byte = new byte[4];
                for (int i = 0; i < 4; i++) {
                    data_byte[i] = fileBuff[start_bin + i + 2];
                }
                buffLen = byteArray2Int(data_byte);
                start_bin += 4;

            } else {
                buffLen = header.get(size);
            }
            start_bin += 2;
            end_bin -= 6;
            // Read data into data array
            int bin_count = 0;
            data_buf = new byte[end_bin - start_bin];
            for (int i = start_bin; i < end_bin; i++) {
                data_buf[bin_count] = fileBuff[i];
                bin_count++;
            }

            dbc.setDataBuffer(data_buf);
            dbc.setDataBufferLength(buffLen);
            dbc.setCompression(header.get(compression));



        } catch (Exception e) {
            msgl.showMessage(
                    "Error while reading RAINBOW data section from BLOB "
                            + header.get(blobid), verbose);
        }

        return dbc;
    }

    /**
     * Method creates radar range mask for a given set of data
     * 
     * @param data_buf
     *            Input data buffer for which mask is created
     * @param width
     *            Input data buffer width
     * @param height
     *            Input data buffer height
     * @param mask_buf
     *            Uncompressed mask buffer
     * @param depth
     *            Mask depth
     * @param verbose
     *            Verbose mode toggle
     * @return Data buffer with mask
     */
    public int[][] createRAINBOWMask(int[][] data_buf, int width, int height,
            byte[] mask_buf, int depth, boolean verbose) {
        int count = 0, value = 0, _byte = 0, bit = 0;
        byte thisbyte = 0;
        try {
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    value = 0;
                    _byte = count * depth / 8;
                    bit = 7 - (count * depth % 8);
                    thisbyte = mask_buf[_byte];
                    value = (int) (thisbyte >> bit & ~(~0 << 1));
                    if (value != 0) {
                        data_buf[y][x] = 255;
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            msgl.showMessage("Failed to create range mask", verbose);
        }
        return data_buf;
    }

    /**
     * Method saves radar data buffer into file.
     * 
     * @param dataBuff
     *            Radar data buffer
     * @param fileName
     *            Output file name
     * @param verbose
     *            Verbose mode toggle
     */
    public void writeRAINBOWData(int[][] dataBuff, String fileName,
            boolean verbose) {
        msgl.showMessage("Writing data to file: " + fileName, verbose);
        try {
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataBuff);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            msgl.showMessage("Failed to write data file: " + e.getMessage(),
                    verbose);
        }
    }

    public static double round(double d, int ic) {
        double dex = Math.pow(10, ic);
        d = d * dex;
        d = Math.round(d);
        d = d / dex;
        return d;
    }

    public static int getMin(int a, int b) {
        if (a > b)
            return b;
        else
            return a;
    }
}
