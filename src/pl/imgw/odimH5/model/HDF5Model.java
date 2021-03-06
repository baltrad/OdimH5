/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.util.MessageLogger;

/**
 * Class encapsulating HDF5 processing methods.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class HDF5Model {

    // Constants
    private static final String XML_ATTR = "attribute";

    private static final String H5_ROOT = "/";
    private static final String H5_GROUP = "group";
    private static final String H5_DATASET = "dataset";
    private static final String H5_OBJECT_NAME = "name";
    private static final String H5_OBJECT_CLASS = "class";
    private static final String H5_GZIP_LEVEL = "gzip_level";
    private static final String H5_DATA_CHUNK = "chunk";
    private static final String H5_DIMENSIONS = "dimensions";

    // XML settings
    private static final String XML_VERSION = "1.0";
    private static final String XML_ENCODING = "UTF-8";

    private static final String H5_CLASS = "CLASS";
    private static final String H5_IM_VER = "IMAGE_VERSION";

    // Reference to MessageLogger object
    private MessageLogger msgl;

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
     * Method parses XML document
     * 
     * @param descriptorFile
     *            XML input file
     * @param verbose
     *            Verbose mode toggle
     * @return List of uppermost nodes within XML document structure
     */
    public NodeList parseDescriptor(String descriptorFile, boolean verbose) {

        NodeList nodeList = null;
        try {
            DOMParser parser = new DOMParser();
            parser.parse(descriptorFile);
            Document doc = parser.getDocument();
            nodeList = doc.getChildNodes();
            msgl.showMessage("Parsing descriptor file: " + descriptorFile,
                    verbose);
        } catch (Exception e) {
            msgl.showMessage("Failed to parse descriptor: " + e.getMessage(),
                    verbose);
        }
        return nodeList;
    }

    /**
     * Recursive method for creating HDF5 file based on XML descriptor.
     * 
     * @param nodeList
     *            List of XML nodes
     * @param file_id
     *            File identifier
     * @param cur_group_id
     *            Current HDF5 group identifier
     * @param verbose
     *            Verbose mode toggle
     */
    public void H5FcreateFromXML(NodeList nodeList, int cur_group_id,
            boolean verbose) {

        // Current XML node
        Node currentNode = null;
        // Attribute node map
        NamedNodeMap attributes = null;
        // HDF5 attribute name
        String attr_name = null;
        // HDF5 attribute class
        String attr_class = null;
        // HDF5 attribute value
        String attr_value = null;
        // Child group identifier
        int child_group_id = -1;
        // Dataspace identifier
        int dataspace_id = -1;
        // Dataset identifier
        int dataset_id = -1;
        // HDF5 operation status
        @SuppressWarnings("unused")
        int status = -1;

        for (int i = 0; i < nodeList.getLength(); i++) {

            currentNode = nodeList.item(i);
            // If current node has attributes and represents HDF5 attribute
            if (currentNode.hasAttributes()
                    && currentNode.getNodeName().equals(XML_ATTR)) {
                try {
                    attributes = currentNode.getAttributes();
                    attr_name = attributes.getNamedItem(H5_OBJECT_NAME)
                            .getNodeValue();
                    if (attr_name.startsWith("/"))
                        attr_name = attr_name.substring(1);
                    attr_class = attributes.getNamedItem(H5_OBJECT_CLASS)
                            .getNodeValue();

                    attr_value = currentNode.getFirstChild().getNodeValue();
                    H5Acreate_any_wrap(cur_group_id, attr_name, attr_class,
                            attr_value, verbose);
                } catch (NullPointerException e) {

                }

                // Method creating HDF5 attributes of different types

            }
            // If curent node has children and represents HDF5 dataset
            if (currentNode.hasAttributes()
                    && currentNode.getNodeName().equals(H5_DATASET)) {
                attributes = currentNode.getAttributes();
                attr_value = currentNode.getFirstChild().getNodeValue();
                attr_name = attributes.getNamedItem(H5_DIMENSIONS)
                        .getNodeValue();
                int dim_x = Integer.parseInt(attr_name.substring(0,
                        attr_name.lastIndexOf("x")));
                int dim_y = Integer.parseInt(attr_name.substring(
                        attr_name.lastIndexOf("x") + 1, attr_name.length()));
                dataspace_id = H5Screate_simple_wrap(2, dim_x, dim_y, null,
                        verbose);
                attr_name = attributes.getNamedItem(H5_DATA_CHUNK)
                        .getNodeValue();
                long chunk[] = new long[2];
                chunk[0] = Long.parseLong(attr_name.substring(0,
                        attr_name.lastIndexOf("x")));
                chunk[1] = Long.parseLong(attr_name.substring(attr_name
                        .lastIndexOf("x") + 1));
                attr_name = attributes.getNamedItem(H5_GZIP_LEVEL)
                        .getNodeValue();
                int gZipLevel = Integer.parseInt(attr_name);
                attr_name = attributes.getNamedItem(H5_OBJECT_NAME)
                        .getNodeValue();
                dataset_id = H5Dcreate_wrap(cur_group_id, attr_name,
                        HDF5Constants.H5T_STD_U8BE, dataspace_id, chunk,
                        gZipLevel, verbose);

                attr_name = attributes.getNamedItem(H5_CLASS).getNodeValue();

                H5Acreate_any_wrap(dataset_id, "CLASS", "string", attr_name,
                        verbose);

                attr_name = attributes.getNamedItem(H5_IM_VER).getNodeValue();

                H5Acreate_any_wrap(dataset_id, "IMAGE_VERSION", "string",
                        attr_name, verbose);

                int[][] dataBuff = readData(attr_value, verbose);

                status = H5Dwrite_wrap(dataset_id,
                        HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                        dataBuff, verbose);
                status = H5Dclose_wrap(dataset_id, verbose);
                status = H5Sclose_wrap(dataspace_id, verbose);
            }

            // If current node has children and represents HDF5 group
            if (currentNode.hasChildNodes()
                    && currentNode.getNodeName().equals(H5_GROUP)) {
                String groupName = getXMLAttributeValue(currentNode,
                        H5_OBJECT_NAME);

                child_group_id = H5Gcreate_wrap(cur_group_id, groupName, 0,
                        verbose);
                NodeList childNodes = currentNode.getChildNodes();
                H5FcreateFromXML(childNodes, child_group_id, verbose);
                status = H5Gclose_wrap(child_group_id, verbose);
            }
        }
    }

    /**
     * Helper method creating new HDF5 file.
     * 
     * @param filename
     *            File name
     * @param access_mode
     *            File create mode
     * @param create_id
     *            Create identifier
     * @param access_id
     *            Access identifier
     * @param verbose
     *            Verbose mode toggle
     * @return File identifier
     */
    public int H5Fcreate_wrap(String filename, int access_mode, int create_id,
            int access_id, boolean verbose) {
        int file_id = -1;
        try {
            int propId = H5.H5Pcreate(HDF5Constants.H5P_FILE_CREATE);
            
            H5.H5Pset_userblock(propId, 0);
            H5.H5Pset_sizes(propId, 4, 4);
            H5.H5Pset_sym_k(propId, 1, 1);
            H5.H5Pset_istore_k(propId, 1);
            
            file_id = H5.H5Fcreate(filename, access_mode, propId, access_id);
            msgl.showMessage("Created new HDF5 file: " + filename + ", ID="
                    + file_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 error] Failed to create HDF5 file: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 file: " + e.getMessage(),
                    verbose);
        }
        return file_id;
    }

    /**
     * Helper method terminating access to HDF5 file.
     * 
     * @param file_id
     *            File identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Close operation status
     */
    public int H5Fclose_wrap(int file_id, boolean verbose) {

        int status = -1;
        try {
            status = H5.H5Fclose(file_id);
            msgl.showMessage("Closing HDF5 file: ID=" + file_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage(
                    "[HDF5 error] Failed to close HDF5 file: "
                            + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error]Failed to close HDF5 file: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method creating new HDF5 group.
     * 
     * @param file_id
     *            File identifier
     * @param name
     *            Group name
     * @param size_hint
     *            Size hint
     * @param verbose
     *            Verbose mode toggle
     * @return Group identifier
     */
    public int H5Gcreate_wrap(int file_id, String name, int size_hint,
            boolean verbose) {

        int group_id = -1;
        try {
            group_id = H5.H5Gcreate(file_id, name, size_hint);
            msgl.showMessage(
                    "Created HDF5 group: " + name + ", ID=" + group_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 error] Failed to create HDF5 group: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 group: " + e.getMessage(),
                    verbose);
        }
        return group_id;
    }

    /**
     * Helper method terminating access to HDF5 file.
     * 
     * @param group_id
     *            Group identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Close operation status
     */
    public int H5Gclose_wrap(int group_id, boolean verbose) {

        int status = -1;
        try {
            status = H5.H5Gclose(group_id);
            msgl.showMessage("Closing HDF5 group: ID=" + group_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 error] Failed to close HDF5 group: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to close HDF5 group: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method for creating and writing HDF5 attribute of given type.
     * 
     * @param group_id
     *            Group identifier
     * @param attr_name
     *            HDF5 attribute name
     * @param attr_class
     *            HDF5 attribute data type
     * @param attr_value
     *            HDF5 attribute value
     * @param verbose
     *            Verbose mode toggle
     */
    public void H5Acreate_any_wrap(int group_id, String attr_name,
            String attr_class, String attr_value, boolean verbose) {

        if (attr_value == null || attr_value.isEmpty()) {
            return;
        }

        int size = -1;
        int attribute_id = -1;

        int dataspace_id = H5Screate_wrap(HDF5Constants.H5S_SCALAR, verbose);

        if (attr_class.equals("long")) {
            attribute_id = H5Acreate_numeric_wrap(group_id, attr_name,
                    HDF5Constants.H5T_NATIVE_LONG, dataspace_id,
                    HDF5Constants.H5P_DEFAULT, verbose);
            long[] value = new long[1];
            value[0] = (long) Long.parseLong(attr_value);
            H5Awrite_wrap(attribute_id, HDF5Constants.H5T_NATIVE_LONG, value,
                    verbose);
        }
        if (attr_class.equals("double")) {
            attribute_id = H5Acreate_numeric_wrap(group_id, attr_name,
                    HDF5Constants.H5T_NATIVE_DOUBLE, dataspace_id,
                    HDF5Constants.H5P_DEFAULT, verbose);
            double[] value = new double[1];
            value[0] = (double) Double.parseDouble(attr_value);
            H5Awrite_wrap(attribute_id, HDF5Constants.H5T_NATIVE_DOUBLE, value,
                    verbose);
        }
        if (attr_class.equals("string") || attr_class.equals("sequence")) {

            byte[] t_attr_buf = attr_value.getBytes();
            size = t_attr_buf.length + 1;
            byte[] attr_buf = new byte[size];
            for (int i = 0; i < t_attr_buf.length; i++) {
                attr_buf[i] = t_attr_buf[i];
            }
            attr_buf[size - 1] = 0;
            int dataset_id = H5Tcopy_wrap(HDF5Constants.H5T_C_S1, verbose);
            H5Tset_size_wrap(dataset_id, size, verbose);
            attribute_id = H5Acreate_string_wrap(group_id, attr_name,
                    dataset_id, dataspace_id, HDF5Constants.H5P_DEFAULT,
                    verbose);
            H5Awrite_wrap(attribute_id, dataset_id, attr_buf, verbose);
        }
        H5Aclose_wrap(attribute_id, verbose);
        H5Sclose_wrap(dataspace_id, verbose);
    }
    /**
     * Helper method for creating and writing HDF5 attribute of given type.
     * 
     * @param group_id
     *            Group identifier
     * @param attr_name
     *            HDF5 attribute name
     * @param attr_class
     *            HDF5 attribute data type
     * @param attr_value
     *            HDF5 attribute value
     * @param verbose
     *            Verbose mode toggle
     */
    public void H5Acreate_double_array(int group_id, String attr_name,
            double[] attr_value, boolean verbose) {

        if (attr_value == null) {
            return;
        }
        int rank = 1;
        
        long dims[] = { attr_value.length };
        long maxdims[] = { attr_value.length };

        int attribute_id = -1;

        int dataspace_id = H5Screate_wrap(HDF5Constants.H5S_SIMPLE, verbose);
        try {
            H5.H5Sset_extent_simple(dataspace_id, rank, dims, maxdims);
            attribute_id = H5.H5Acreate(group_id, attr_name,
                    HDF5Constants.H5T_NATIVE_DOUBLE, dataspace_id,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            H5.H5Awrite(attribute_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                    attr_value);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (HDF5Exception e) {
            // TODO Auto-generated catch block
//            msgl.showMessage(e.getMessage(), verbose);
            e.printStackTrace();
            System.exit(0);
        }

        H5Aclose_wrap(attribute_id, verbose);
        H5Sclose_wrap(dataspace_id, verbose);
    }

    /**
     * Helper method creating HDF5 dataspace.
     * 
     * @param datatype_id
     *            Dataspace type
     * @param verbose
     *            Verbose mode toggle
     * @return Dataspace identifier
     */
    public int H5Screate_wrap(int datatype_id, boolean verbose) {

        int dataspace_id = -1;
        try {
            dataspace_id = H5.H5Screate(datatype_id);
            msgl.showMessage("Created HDF5 dataspace: ID=" + dataspace_id
                    + ", datatype ID=" + datatype_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 error] Failed to create HDF5 dataspace: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 dataspace: "
                            + e.getMessage(), verbose);
        }
        return dataspace_id;
    }


    /**
     * Helper method for creating HDF5 simple dataspace
     * 
     * @param rank
     *            Rank
     * @param dims
     *            Dataspace dimensions
     * @param maxdims
     *            Maximum dataspace dimensions
     * @param dim_x
     *            Dataspace x dimension
     * @param dim_y
     *            Dataspace y dimension
     * @param verbose
     *            Verbose mode toggle
     * @return Dataspace identifier
     */
    public int H5Screate_simple_wrap(int rank, int dim_x, int dim_y,
            long maxdims[], boolean verbose) {
        int dataspace_id = -1;
        long[] dims = new long[2];
        dims[0] = (long) dim_x;
        dims[1] = (long) dim_y;
        try {
            dataspace_id = H5.H5Screate_simple(rank, dims, maxdims);
            msgl.showMessage("Created HDF5 dataspace: ID=" + dataspace_id,
                    verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 error] Failed to create HDF5 dataspace: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 dataspace: "
                            + e.getMessage(), verbose);
        }
        return dataspace_id;
    }

    /**
     * Helper method for closing HDF5 dataspace.
     * 
     * @param dataspace_id
     *            Dataspace identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Close operation status
     */
    public int H5Sclose_wrap(int dataspace_id, boolean verbose) {

        int status = -1;
        try {
            status = H5.H5Sclose(dataspace_id);
            msgl.showMessage("Closing HDF5 dataspace: ID=" + dataspace_id,
                    verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to close HDF5 dataspace: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to close HDF5 dataspace: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method for creating HDF5 numeric attribute.
     * 
     * @param group_id
     *            Container group identifier
     * @param attr_name
     *            Attribute name
     * @param datatype_id
     *            Attribute type identifier
     * @param dataspace_id
     *            Dataspace identifier
     * @param create_plist
     * @param verbose
     *            Verbose mode toggle
     * @return Attribute identifier
     */
    public int H5Acreate_numeric_wrap(int group_id, String attr_name,
            int datatype_id, int dataspace_id, int create_plist, boolean verbose) {
        int attribute_id = -1;
        try {
            attribute_id = H5.H5Acreate(group_id, attr_name, datatype_id,
                    dataspace_id, create_plist);
            msgl.showMessage("Created HDF5 attribute: " + attr_name + ", ID="
                    + attribute_id + ", group ID=" + group_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage(
                    "[HDF5 Error] Failed to create HDF5 numeric attribute: "
                            + attr_name, verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 numeric attribute: "
                            + attr_name, verbose);
        }
        return attribute_id;
    }

    /**
     * Helper method for creating HDF5 string attribute.
     * 
     * @param group_id
     *            Container group identifier
     * @param attr_name
     *            Attribute name
     * @param dataset_id
     *            Dataset identifier
     * @param dataspace_id
     *            Dataspace identifier
     * @param create_plist
     * @param verbose
     *            Verbose mode toggle
     * @return Attribute identifier
     */
    public int H5Acreate_string_wrap(int group_id, String attr_name,
            int dataset_id, int dataspace_id, int create_plist, boolean verbose) {

        int attribute_id = -1;
        try {

            attribute_id = H5.H5Acreate(group_id, attr_name, dataset_id,
                    dataspace_id, create_plist);
            msgl.showMessage("Created HDF5 attribute: " + attr_name + ", ID="
                    + attribute_id + ", group ID=" + group_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage(
                    "[HDF5 Error] Failed to create HDF5 string attribute: "
                            + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage("[Error] Failed to create HDF5 string attribute: "
                    + e.getMessage(), verbose);
        }
        return attribute_id;
    }

    /**
     * Helper method for writing HDF5 attribute
     * 
     * @param attr_id
     *            Attribute identifier
     * @param datatype_id
     *            Datatype identifier
     * @param buf
     *            Data buffer
     * @param verbose
     *            Verbose mode toggle
     * @return Write operation status
     */
    public int H5Awrite_wrap(int attr_id, int datatype_id, Object buf,
            boolean verbose) {

        int status = -1;
        try {
            status = H5.H5Awrite(attr_id, datatype_id, buf);
            msgl.showMessage("Writing HDF5 attribute: ID=" + attr_id
                    + ", datatype ID=" + datatype_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to write HDF5 attribute: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to write HDF5 attribute: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method for closing HDF5 attribute
     * 
     * @param attribute_id
     *            Attribute identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Close operation status
     */
    public int H5Aclose_wrap(int attribute_id, boolean verbose) {

        int status = -1;
        try {
            status = H5.H5Aclose(attribute_id);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to close HDF5 attribute: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to close HDF5 attribute: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method for setting HDF5 dataset size
     * 
     * @param dataset_id
     *            Dataset identifier
     * @param size
     *            Dataset size
     * @param verbose
     *            Verbose mode toggle
     * @return Operation status
     */
    public int H5Tset_size_wrap(int dataset_id, int size, boolean verbose) {
        int status = -1;
        try {
            H5.H5Tset_size(dataset_id, size);
            msgl.showMessage("Setting dataset size: ID=" + dataset_id
                    + ", size=" + size, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to set HDF5 dataspace size: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to set HDF5 dataspace size: "
                            + e.getMessage(), verbose);
        }
        return status;
    }

    /**
     * Helper method for copying HDF5 datatype.
     * 
     * @param datatype_id
     *            Datatype identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Dataset identifier
     */
    public int H5Tcopy_wrap(int datatype_id, boolean verbose) {
        int dataset_id = -1;
        try {
            dataset_id = H5.H5Tcopy(datatype_id);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to copy HDF5 datatype: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to copy HDF5 datatype: " + e.getMessage(),
                    verbose);
        }
        return dataset_id;
    }

    /**
     * Helper method for creating HDF5 dataset.
     * 
     * @param file_id
     *            File identifier
     * @param group_name
     *            Group name
     * @param datatype_id
     *            Datatype identifier
     * @param dataspace_id
     *            Dataspace identifier
     * @param chunk
     *            A 2-D array containing the size of each chunk
     * @param gZipLevel
     *            Gzip compression level
     * @param verbose
     *            Verbose mode toggle
     * @return Dataset identifier
     */
    public int H5Dcreate_wrap(int file_id, String group_name, int datatype_id,
            int dataspace_id, long[] chunk, int gZipLevel, boolean verbose) {
        @SuppressWarnings("unused")
        int status = -1;
        int dataset_id = -1;

        // chunk[0] = 128;
        // chunk[1] = 128;

        try {
            int plist_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);

            // status = H5.H5Pset_meta_block_size(plist_id, 0);

            status = H5.H5Pset_deflate(plist_id, gZipLevel);
            status = H5.H5Pset_layout(plist_id, HDF5Constants.H5D_CHUNKED);
            status = H5.H5Pset_chunk(plist_id, 2, chunk);
//            status = H5.H5Pset_sym_k(plist_id, 1, 1);
            // Create the dataset
            dataset_id = H5.H5Dcreate(file_id, group_name, datatype_id,
                    dataspace_id, plist_id);
            msgl.showMessage("Creating HDF5 dataset: ID=" + dataset_id
                    + ", datatype ID=" + datatype_id, verbose);

            // status = H5.H5Pset_userblock(plist, 0);
            // status = H5.H5Pset_sizes(plist, 4, 4);
            // status = H5.H5Pset_sym_k(plist, 1, 1);
            // status = H5.H5Pset_istore_k(plist, 1);

        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to create HDF5 dataset: "
                    + hdf5e.getMessage(), true);

        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to create HDF5 dataset: " + e.getMessage(),
                    true);
        }

        return dataset_id;
    }

    /**
     * Helper method for writing HDF5 dataset.
     * 
     * @param dataset_id
     *            Dataset identifier
     * @param mem_type_id
     *            Memory type identifier
     * @param mem_space_id
     *            Memory space identifier
     * @param file_space_id
     *            File space identifier
     * @param xfer_plist_id
     * @param buf
     *            Data buffer
     * @param verbose
     *            Verbose mode toggle
     * @return Operation status
     */
    public int H5Dwrite_wrap(int dataset_id, int mem_type_id, int mem_space_id,
            int file_space_id, int xfer_plist_id, Object buf, boolean verbose) {
        int status = -1;
        try {

            status = H5.H5Dwrite(dataset_id, mem_type_id, mem_space_id,
                    file_space_id, xfer_plist_id, buf);
            msgl.showMessage("Writing HDF5 dataset: ID=" + dataset_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to write HDF5 dataset: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[Error] Failed to write HDF5 dataset: " + e.getMessage(),
                    verbose);
        }
        return status;
    }

    /**
     * Helper method for closing HDF5 dataset.
     * 
     * @param dataset_id
     *            Dataset identifier
     * @param verbose
     *            Verbose mode toggle
     * @return Operation status
     */
    public int H5Dclose_wrap(int dataset_id, boolean verbose) {
        int status = -1;
        try {
            status = H5.H5Dclose(dataset_id);
            msgl.showMessage("Closing HDF5 dataset: ID=" + dataset_id, verbose);
        } catch (HDF5Exception hdf5e) {
            msgl.showMessage("[HDF5 Error] Failed to close HDF5 dataset: "
                    + hdf5e.getMessage(), verbose);
        } catch (Exception e) {
            msgl.showMessage(
                    "[HDF5 Error] Failed to close HDF5 dataset: "
                            + e.getMessage(), verbose);
        }
        return status;
    }

    /**
     * Method retrieves attribute value of XML document node.
     * 
     * @param node
     *            XML node
     * @param xmlAttributeName
     *            Attribute name
     * @return Attribute value
     */
    public String getXMLAttributeValue(Node node, String xmlAttributeName) {

        NamedNodeMap attributes = node.getAttributes();
        return attributes.getNamedItem(xmlAttributeName).getNodeValue();
    }

    /**
     * Method retrieves list of top-level nodes of XML document. These nodes
     * represent top level groups of HDF5 file.
     * 
     * @param rootNodes
     *            XML document root nodes
     * @return List of top-level nodes of XML document
     */
    public NodeList getTopLevelNodes(NodeList rootNodes) {

        NodeList topLevelNodes = null;
        Node curNode = null;
        NamedNodeMap attributes = null;
        String nodeValue = null;
        for (int i = 0; i < rootNodes.getLength(); i++) {
            curNode = rootNodes.item(i);
            if (curNode.hasAttributes()) {
                attributes = curNode.getAttributes();
                nodeValue = attributes.getNamedItem(H5_OBJECT_NAME)
                        .getNodeValue();
                if (nodeValue.equals(H5_ROOT)) {
                    topLevelNodes = curNode.getChildNodes();
                }
            }
        }
        return topLevelNodes;
    }

    /**
     * Method modifies top-level nodes of XML document by appending HDF5 root
     * path ('/')
     * 
     * @param topLevelNodes
     *            List of top-level nodes of XML document
     * @return List of modified top-level nodes of XML document
     */
    public void appendRootPath(NodeList topLevelNodes) {

        Node curNode = null;
        NamedNodeMap attributes = null;
        String nodeValue = null;
        String nodeValueWithRoot = null;
        for (int i = 0; i < topLevelNodes.getLength(); i++) {
            curNode = topLevelNodes.item(i);
            if (curNode.hasAttributes()) {
                attributes = curNode.getAttributes();
                nodeValue = attributes.getNamedItem(H5_OBJECT_NAME)
                        .getNodeValue();
                nodeValueWithRoot = H5_ROOT + nodeValue;
                attributes.getNamedItem(H5_OBJECT_NAME).setNodeValue(
                        nodeValueWithRoot);
            }
        }
    }

    /**
     * Method reads radar data file. File is stored in a memory buffer.
     * 
     * @param fileName
     *            Input file name
     * @param verbose
     *            Verbose mode toggle
     * @return Byte array containing input radar data file
     */
    public byte[] readDataFile(String fileName, boolean verbose) {
        File fin = new File(fileName);
        int file_len = (int) fin.length();
        byte[] file_buf = new byte[file_len];
        try {
            FileInputStream fis = new FileInputStream(fileName);
            fis.read(file_buf, 0, file_len);
            fis.close();
            msgl.showMessage("Reading input data file: " + fileName, verbose);
        } catch (IOException e) {
            msgl.showMessage(
                    "Error while reading input data file: " + fileName, verbose);
        }
        return file_buf;
    }

    /**
     * Method creates XML document object.
     * 
     * @param verbose
     *            Verbose mode toggle
     * @return Reference to XML document object
     */
    public Document createXMLDocumentObject(boolean verbose) {

        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException e) {
            msgl.showMessage(
                    "Error while creating XML document object: "
                            + e.getMessage(), verbose);
        }
        return doc;
    }

    /**
     * Method saves XML document to file.
     * 
     * @param doc
     *            XML document
     * @param fileName
     *            Output file name
     */
    public void saveXMLFile(Document doc, String fileName, boolean verbose) {

        try {
            OutputFormat format = new OutputFormat(doc);
            format.setVersion(XML_VERSION);
            format.setEncoding(XML_ENCODING);
            format.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(format);
            FileWriter fw = new java.io.FileWriter(fileName);
            serializer.setOutputCharStream(fw);
            serializer.serialize(doc);
            fw.close();
        } catch (Exception e) {
            msgl.showMessage("Error while saving XML file: " + e.getMessage(),
                    verbose);
        }

    }

    /**
     * Method creates new directory
     * 
     * @param dirName
     *            Directory name
     * @param verbose
     *            Verbose mode toggle
     * @return Directory path
     */
    public String createDirectory(String dirName, boolean verbose) {

        String path = null;
        File f = new File(dirName);
        path = f.getPath();
        if (f.exists()) {
            msgl.showMessage("Directory exists: " + dirName, verbose);
        } else {
            boolean success = f.mkdir();
            if (!success) {
                msgl.showMessage("Failed to create directory: " + dirName,
                        verbose);
            }
        }
        return path;
    }

    /**
     * Method converts integer value to byte array.
     * 
     * @param value
     *            Input integer value
     * @return Byte array
     */
    public byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    /**
     * Method reads actual radar data from file.
     * 
     * @param fileName
     *            Input file name
     * @param verbose
     *            Verbose mode toggle
     * @return Radar data buffer
     */
    public int[][] readData(String fileName, boolean verbose) {
        msgl.showMessage("Reading data file: " + fileName, verbose);
        int[][] dataBuff = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            dataBuff = (int[][]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            msgl.showMessage("Failed to read data file: " + e.getMessage(),
                    verbose);
        }
        return dataBuff;
    }

    /**
     * Method transposes a integer array.
     * 
     * @param intArray
     *            Input arrary
     * @param rays
     *            orignal size
     * @param bins
     *            original size
     * @return transposed array
     */
    public int[][] transposeArray(int[][] intArray, int rays, int bins) {

        int[][] newArray = new int[rays][bins];

        for (int i = 0; i < bins; i++) {
            for (int j = 0; j < rays; j++) {

                newArray[j][i] = intArray[i][j];
            }
        }

        return newArray;
    }

    /**
     * Function puts in order azimuths starting from 0 degree
     */
    public int[][] shiftAzimuths(int[][] intArray, int rays, int bins,
            int startingAzimuth) {

        startingAzimuth = rays - startingAzimuth;
        int[][] newArray = new int[bins][rays];
        int newJ = 0;
        for (int i = 0; i < bins; i++) {
            for (int j = 0; j < rays; j++) {

                newJ = (j + startingAzimuth) % rays;

                newArray[i][newJ] = intArray[i][j];
            }
        }

        return newArray;
    }

    /**
     * Method used to verify whether current file is HDF5 file
     * 
     * @param fileName
     *            Input file name
     * @return True if current file is HDF5 file
     */
    public boolean verifyHDF5File(String fileName) {
        boolean res;
        try {
            FileFormat fileFormat = FileFormat
                    .getFileFormat(FileFormat.FILE_TYPE_HDF5);
            H5File inputFile = (H5File) fileFormat.open(fileName,
                    FileFormat.READ);
            inputFile.open();
            res = true;
        } catch (Exception e) {
            res = false;
        }
        return res;
    }

    /**
     * Method opens HDF5 file
     * 
     * @param fileName
     *            Input file name
     * @return Reference to open HDF5 file
     */
    public static H5File openHDF5File(String fileName) {
        FileFormat fileFormat = FileFormat
                .getFileFormat(FileFormat.FILE_TYPE_HDF5);
        H5File inputFile = null;
        try {
            inputFile = (H5File) fileFormat.open(fileName, FileFormat.READ);
            inputFile.open();
        } catch (Exception e) {
            System.out.println("HDF5: Cannot open file: " + fileName);
        }
        return inputFile;
    }

    /**
     * Method closes HDF5 file
     * 
     * @param inputfile
     *            Input file
     */
    public void closeHDF5File(H5File inputfile) {

        try {
            inputfile.close();
        } catch (HDF5Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Method opens HDF5 file and gets its root group
     * 
     * @param inputFile
     *            Reference to open HDF5 file
     * @return Root group of HDF5 file
     */
    public Group getHDF5RootGroup(H5File inputFile, boolean verbose) {
        Group root = null;
        try {
            root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) inputFile
                    .getRootNode()).getUserObject();
        } catch (Exception e) {
            msgl.showMessage("I/O error: Error while opening HDF5 file",
                    verbose);
        }
        return root;
    }

    /**
     * Method retrieves HDF5 string attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param attrName
     *            Attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public String getHDF5StringValue(Group grp, String grpName,
            String attrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        String[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given name, retrieve its attributes
                if (obj.toString().matches(grpName)) {
                    List attrsList = obj.getMetadata();
                    // if attribute name matches given name, retrieve its value
                    for (int j = 0; j < attrsList.size(); j++) {
                        Attribute attr = (Attribute) attrsList.get(j);
                        if (attr.getName().matches(attrName)) {
                            ret = (String[]) attr.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 string attribute value",
                    verbose);
        }
        if (ret == null)
            return "";
        return (ret[0]);
    }

    /**
     * Method retrieves HDF5 integer attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param attrName
     *            Attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public int getHDF5IntegerValue(Group grp, String grpName, String attrName,
            boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        int[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given name, retrieve its attributes
                if (obj.toString().matches(grpName)) {
                    List attrsList = obj.getMetadata();
                    // if attribute name matches given name, retrieve its value
                    for (int j = 0; j < attrsList.size(); j++) {
                        Attribute attr = (Attribute) attrsList.get(j);
                        if (attr.getName().matches(attrName)) {
                            ret = (int[]) attr.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 integer attribute value",
                    verbose);
        }
        if (ret == null)
            return 0;
        return (ret[0]);
    }

    /**
     * Method retrieves HDF5 float attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param attrName
     *            Attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public double getHDF5DoubleValue(Group grp, String grpName,
            String attrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        double[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given name, retrieve its attributes
                if (obj.toString().matches(grpName)) {
                    List attrsList = obj.getMetadata();
                    // if attribute name matches given name, retrieve its value
                    for (int j = 0; j < attrsList.size(); j++) {
                        Attribute attr = (Attribute) attrsList.get(j);
                        if (attr.getName().matches(attrName)) {
                            ret = (double[]) attr.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 double attribute value",
                    verbose);
        }

        if (ret == null)
            return getHDF5FloatValue(grp, grpName, attrName, verbose);

        return (ret[0]);
    }

    /**
     * Method retrieves HDF5 float attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param attrName
     *            Attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public float getHDF5FloatValue(Group grp, String grpName, String attrName,
            boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        float[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given name, retrieve its attributes
                if (obj.toString().matches(grpName)) {
                    List attrsList = obj.getMetadata();
                    // if attribute name matches given name, retrieve its value
                    for (int j = 0; j < attrsList.size(); j++) {
                        Attribute attr = (Attribute) attrsList.get(j);
                        if (attr.getName().matches(attrName)) {
                            ret = (float[]) attr.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 float attribute value",
                    verbose);
        }

        if (ret == null)
            return 0;

        return (ret[0]);
    }

    /**
     * Method retrieves HDF5 leaf string attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param leafGrpName
     *            Leaf group name of HDF5 file
     * @param leafAttrName
     *            Leaf attribute name which value is retrieved
     * @param verbose
     * @return Given leaf attribute value
     */
    public String getHDF5StringLeafValue(Group grp, String grpName,
            String leafGrpName, String leafAttrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        String[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given group name, retrieve its
                // subgroups
                if (obj.toString().matches(grpName)) {
                    Group g = (Group) grpsList.get(i);
                    List leafGrpsList = g.getMemberList();
                    for (int j = 0; j < leafGrpsList.size(); j++) {
                        obj = (HObject) leafGrpsList.get(j);
                        // if subgroup name matches given subgroup, retrieve its
                        // attributes
                        if (obj.toString().matches(leafGrpName)) {
                            List leafAttrsList = obj.getMetadata();
                            for (int k = 0; k < leafAttrsList.size(); k++) {
                                Attribute leafAttr = (Attribute) leafAttrsList
                                        .get(k);
                                // if leaf attribute name matches given leaf
                                // attribute name,
                                // retrieve its value
                                if (leafAttr.getName().matches(leafAttrName)) {
                                    ret = (String[]) leafAttr.getValue();

                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 string attribute value",
                    verbose);
        }
        if (ret == null)
            return null;
        return (ret[0]);
    }

    /**
     * Method retrieves of HDF5 leaf float attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param leafGrpName
     *            Leaf group name of HDF5 file
     * @param leafAttrName
     *            Leaf attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public float getHDF5FloatLeafValue(Group grp, String grpName,
            String leafGrpName, String leafAttrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        float[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given group name, retrieve its
                // subgroups
                if (obj.toString().matches(grpName)) {
                    Group g = (Group) grpsList.get(i);
                    List leafGrpsList = g.getMemberList();
                    for (int j = 0; j < leafGrpsList.size(); j++) {
                        obj = (HObject) leafGrpsList.get(j);
                        // if subgroup name matches given subgroup, retrieve its
                        // attributes
                        if (obj.toString().matches(leafGrpName)) {
                            List leafAttrsList = obj.getMetadata();
                            for (int k = 0; k < leafAttrsList.size(); k++) {
                                Attribute leafAttr = (Attribute) leafAttrsList
                                        .get(k);
                                // if leaf attribute name matches given leaf
                                // attribute name,
                                // retrieve its value
                                if (leafAttr.getName().matches(leafAttrName)) {
                                    ret = (float[]) leafAttr.getValue();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 float attribute value",
                    verbose);
        }
        if (ret == null)
            return 0;
        return (ret[0]);
    }

    /**
     * Method retrieves of HDF5 leaf float attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param leafGrpName
     *            Leaf group name of HDF5 file
     * @param leafAttrName
     *            Leaf attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public int getHDF5IntLeafValue(Group grp, String grpName,
            String leafGrpName, String leafAttrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        long[] ret = null;
        int[] retint = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given group name, retrieve its
                // subgroups
                if (obj.toString().matches(grpName)) {
                    Group g = (Group) grpsList.get(i);
                    List leafGrpsList = g.getMemberList();
                    for (int j = 0; j < leafGrpsList.size(); j++) {
                        obj = (HObject) leafGrpsList.get(j);
                        // if subgroup name matches given subgroup, retrieve its
                        // attributes
                        if (obj.toString().matches(leafGrpName)) {
                            List leafAttrsList = obj.getMetadata();
                            for (int k = 0; k < leafAttrsList.size(); k++) {
                                Attribute leafAttr = (Attribute) leafAttrsList
                                        .get(k);
                                // if leaf attribute name matches given leaf
                                // attribute name,
                                // retrieve its value
                                if (leafAttr.getName().matches(leafAttrName)) {
                                    if (leafAttr.getValue() instanceof long[]) {
                                        ret = (long[]) leafAttr.getValue();
                                    } else if (leafAttr.getValue() instanceof int[]) {
                                        ret = new long[] { ((int[]) leafAttr
                                                .getValue())[0] };
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 integer attribute value",
                    verbose);
        }
        if (ret == null)
            return 0;
        return (int) (ret[0]);
    }

    /**
     * Method retrieves of HDF5 leaf float attribute value
     * 
     * @param grp
     *            Root group of HDF5 file
     * @param grpName
     *            Root group name of HDF5 file
     * @param leafGrpName
     *            Leaf group name of HDF5 file
     * @param leafAttrName
     *            Leaf attribute name which value is retrieved
     * @param verbose
     * @return Given attribute value
     */
    public double getHDF5DoubleLeafValue(Group grp, String grpName,
            String leafGrpName, String leafAttrName, boolean verbose) {
        List grpsList = grp.getMemberList();
        HObject obj = null;
        double[] ret = null;
        try {
            for (int i = 0; i < grpsList.size(); i++) {
                obj = (HObject) grpsList.get(i);
                // if group name matches given group name, retrieve its
                // subgroups
                if (obj.toString().matches(grpName)) {
                    Group g = (Group) grpsList.get(i);
                    List leafGrpsList = g.getMemberList();
                    for (int j = 0; j < leafGrpsList.size(); j++) {
                        obj = (HObject) leafGrpsList.get(j);
                        // if subgroup name matches given subgroup, retrieve its
                        // attributes
                        if (obj.toString().matches(leafGrpName)) {
                            List leafAttrsList = obj.getMetadata();
                            for (int k = 0; k < leafAttrsList.size(); k++) {
                                Attribute leafAttr = (Attribute) leafAttrsList
                                        .get(k);
                                // if leaf attribute name matches given leaf
                                // attribute name,
                                // retrieve its value
                                if (leafAttr.getName().matches(leafAttrName)) {
                                    ret = (double[]) leafAttr.getValue();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            msgl.showMessage(
                    "I/O error: Error while retrieving HDF5 double attribute value",
                    verbose);
        }
        if (ret == null)
            return getHDF5FloatLeafValue(grp, grpName, leafGrpName,
                    leafAttrName, verbose);
        return (ret[0]);
    }

    /**
     * Method retrieves HDF5 dataset. Dataset is converted from byte array to
     * integer array.
     * 
     * @param inputFile
     *            Reference to open HDF5 file
     * @param datasetPath
     *            Absolute HDF5 path to dataset
     * @param width
     *            Dataset width
     * @param height
     *            Dataset height
     * @param verbose
     * @return Dataset as integer array
     */
    public int[][] getHDF5Dataset(H5File inputFile, String datasetPath,
            int width, int height, boolean verbose) {
        int[][] dataArray2D = new int[width][height];
        try {
            Dataset dataset = (Dataset) inputFile.get(datasetPath);
            byte[] dataArray = (byte[]) dataset.read();
            int count = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    count = y * width + x;
                    dataArray2D[x][y] = (int) dataArray[count] & 0xFF;
                    if(dataArray2D[x][y] == 255) {
                        dataArray2D[x][y] = 0;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            msgl.showMessage("I/O error: Error while retrieving HDF5 dataset",
                    verbose);
        }
        return (dataArray2D);
    }

    /**
     * Method retrieves HDF5 dataset. Dataset is converted from byte array to
     * integer array.
     * 
     * @param inputFile
     *            Reference to open HDF5 file
     * @param datasetPath
     *            Absolute HDF5 path to dataset
     * @param width
     *            Dataset width
     * @param height
     *            Dataset height
     * @param verbose
     * @return Dataset as integer array
     */
    public int[][] getHDF5DoubleDataset(H5File inputFile, String datasetPath,
            int width, int height, boolean verbose) {
        int[][] dataArray2D = new int[width][height];
        try {
            Dataset dataset = (Dataset) inputFile.get(datasetPath);
            double[] dataArray = (double[]) dataset.read();
            int count = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    count = y * width + x;
                    dataArray2D[x][y] = (int) dataArray[count];
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            msgl.showMessage("I/O error: Error while retrieving HDF5 dataset",
                    verbose);
        }
        return (dataArray2D);
    }
    
    /**
     * Method converts HDF5 date format into desired format.
     * 
     * @param dateStr
     *            Input date string
     * @param delimiter
     *            Date delimiter character
     * @return Formatted date string
     */
    public String parseHDF5Date(String dateStr, String delimiter) {
        String date = new String("");
        String year = dateStr.substring(0, 4);
        date = year;
        date += delimiter;
        String month = dateStr.substring(4, 6);
        date += month;
        date += delimiter;
        String day = dateStr.substring(6, 8);
        date += day;
        return date;
    }

    /**
     * Method converts HDF5 time format into desired format.
     * 
     * @param timeStr
     *            Input time string
     * @param delimiter
     *            Time delimiter character
     * @return Formatted time string
     */
    public String parseHDF5Time(String timeStr, String delimiter) {
        String time = new String("");
        String hour = timeStr.substring(0, 2);
        time = hour;
        time += delimiter;
        String minute = timeStr.substring(2, 4);
        time += minute;
        time += delimiter;
        String second = timeStr.substring(4, 6);
        time += second;
        return time;
    }

}
