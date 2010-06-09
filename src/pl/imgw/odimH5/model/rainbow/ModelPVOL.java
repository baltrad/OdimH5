/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import pl.imgw.odimH5.util.DataBufferContainer;

/**
 * 
 * It contains static methods creating XML file of PVOL objects descriptor
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelPVOL {

    /**
     * 
     * Method creates descriptor file for RAINBOW software platform. This
     * descriptor is created for PVOL objects defined by ODIM_H5 specification.
     * 
     * @param fileName
     *            Output name of XML file
     * @param fileBuff
     *            Input file data in a byte array
     * @param verbose
     *            Verbose mode
     * @param rb
     *            Rainbow class model
     */
    @SuppressWarnings("static-access")
    public static void createDescriptor(String fileName, byte[] fileBuff,
            boolean verbose, Model rb) {

        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.VOLUME, verbose);

        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        String dataDir = rb.proc.createDirectory("data", verbose);

        NodeList nodeList = null;

        // =========== what group Element ========================
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        if (source.matches("BRZ")) {
            source = "PLC:" + Model.BRZ;
        } else if (source.matches("GDA")) {
            source = "PLC:" + Model.GDA;
        } else if (source.matches("LEG")) {
            source = "PLC:" + Model.LEG;
        } else if (source.matches("PAS")) {
            source = "PLC:" + Model.PAS;
        } else if (source.matches("POZ")) {
            source = "PLC:" + Model.POZ;
        } else if (source.matches("RAM")) {
            source = "PLC:" + Model.RAM;
        } else if (source.matches("RZE")) {
            source = "PLC:" + Model.RZE;
        } else if (source.matches("SWI")) {
            source = "PLC:" + Model.SWI;
        }

        // ============== where group ============================

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String lon = rb.getRAINBOWMetadataElement(nodeList, "lon", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String lat = rb.getRAINBOWMetadataElement(nodeList, "lat", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String alt = rb.getRAINBOWMetadataElement(nodeList, "alt", verbose);

        // ============= how group ============================

        // nodeList = rb.getRAINBOWNodesByName(inputDoc, "task", verbose);
        // String task = rb.getRAINBOWMetadataElement(nodeList, "name",
        // verbose);
        // how.appendChild(rb.makeAttr("task", task, od, rb.H5_STRING));
        String startepochs = rb.convertRAINBOWDate2Epoch(date, time, verbose);
        String endepochs = rb.convertRAINBOWDate2Epoch(date, time, verbose);
        date = rb.parseRAINBOWDate(date, verbose);
        time = rb.parseRAINBOWTime(time, verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
        String version = rb.getRAINBOWMetadataElement(nodeList, "version",
                verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        String beamwidth = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        String wavelength = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        // ===================== datasetn group =============================

        NodeList sliceList = inputDoc.getElementsByTagName("slice");
        int datasetSize = sliceList.getLength();
        String sliceTime[] = new String[datasetSize];
        String sliceDate[] = new String[datasetSize];
        String pangle[] = new String[datasetSize];
        String bins[] = new String[datasetSize];
        String srange[] = new String[datasetSize];
        String rstep[] = new String[datasetSize];
        String rays[] = new String[datasetSize];
        String a1gate[] = new String[datasetSize];
        String datatype[] = new String[datasetSize];
        String min[] = new String[datasetSize];
        String gain[] = new String[datasetSize];

        DataBufferContainer dataBuff[] = new DataBufferContainer[datasetSize];

        int dataDepth[] = new int[datasetSize];
        for (int i = 0; i < datasetSize; i++) {

            // ========== datasetn specific what group ===============
            sliceDate[i] = rb.parseRAINBOWDate(rb.getValueByName(sliceList
                    .item(i), "slicedata", "date"), verbose);
            sliceTime[i] = rb.parseRAINBOWTime(rb.getValueByName(sliceList
                    .item(i), "slicedata", "time"), verbose);

            // ============= datasetn specific where group ============

            pangle[i] = rb.getValueByName(sliceList.item(i), "posangle", null);
            bins[i] = rb.getValueByName(sliceList.item(i), "rawdata", "bins");
            srange[i] = rb.getValueByName(sliceList.item(i), "start_range",
                    null);
            if (srange[i] == null) // default value is "0"
                srange[i] = "0";

            rstep[i] = rb.getValueByName(sliceList.item(i), "rangestep", null);

            rays[i] = rb.getValueByName(sliceList.item(i), "rawdata", "rays");

            // ===============================================================

            int raysBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rawdata", "blobid"));
            int raysDepth = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "depth"));
            dataDepth[i] = Integer.parseInt(rb.getValueByName(
                    sliceList.item(i), "rawdata", "depth"));
            dataBuff[i] = rb.getRainbowDataSection(fileBuff, dataBlobNumber,
                    dataDepth[i], verbose);
            DataBufferContainer raysBuff = rb.getRainbowDataSection(fileBuff,
                    raysBlobNumber, raysDepth, verbose);
            byte[] infRaysBuff = rb.inflate1DRAINBOWDataSection(raysBuff
                    .getDataBuffer(), raysBuff.getDataBufferLength(), verbose);

            // =================================================================

            a1gate[i] = String.valueOf(startingAzimuthNumber(infRaysBuff,
                    Integer.parseInt(rays[i])));

            // ============= datasetn specific data group ==================

            // data specific where group

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
            datatype[i] = rb.getValueByName(sliceList.item(i), "rawdata",
                    "type");

            if (datatype[i].matches(rb.DBZ))
                datatype[i] = "TH";
            else if (datatype[i].matches(rb.UPHIDP))
                datatype[i] = "PHIDP";

            min[i] = rb.getValueByName(sliceList.item(i), "dynz", "min");
            String max = rb.getValueByName(sliceList.item(i), "dynz", "max");
            gain[i] = rb.getRAINBOWGain(min[i], max, dataDepth[i]);

        }

        // Create XML document object
        Document od = rb.proc.createXMLDocumentObject(verbose);
        Comment comment = od
                .createComment("ODIM_H5 descriptor file, platform: RAINBOW,"
                        + " file object: " + rb.PVOL);
        od.appendChild(comment);
        Element root = od.createElement(rb.H5_GROUP);
        root.setAttribute(rb.H5_OBJECT_NAME, rb.H5_ROOT);
        od.appendChild(root);

        // what group
        Element what = od.createElement(rb.H5_GROUP);
        what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
        what.appendChild(rb.makeAttr("object", "PVOL", od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("version", "H5rad 2.0", od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("date", date, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("time", time, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("source", source, od, rb.H5_STRING));
        root.appendChild(what);

        Element where = od.createElement(rb.H5_GROUP);
        where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);
        where.appendChild(rb.makeAttr("lon", lon, od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("lat", lat, od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("height", alt, od, rb.H5_DOUBLE));
        root.appendChild(where);

        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);
        how
                .appendChild(rb.makeAttr("startepochs", startepochs, od,
                        rb.H5_LONG));
        how.appendChild(rb.makeAttr("endepochs", endepochs, od, rb.H5_LONG));
        how.appendChild(rb.makeAttr("system", rb.RAINBOW_SYSTEM, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("software", rb.RAINBOW_SOFTWARE, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("sw_version", version, od, rb.H5_STRING));
        how.appendChild(rb.makeAttr("beamwidth", beamwidth, od, rb.H5_DOUBLE));
        how
                .appendChild(rb.makeAttr("wavelength", wavelength, od,
                        rb.H5_DOUBLE));
        root.appendChild(how);

        for (int i = 0; i < datasetSize; i++) {
            Element dataset = od.createElement(rb.H5_GROUP);
            dataset.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATASET + (i + 1));

            Element dataset_what = od.createElement(rb.H5_GROUP);
            dataset_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            dataset_what.appendChild(rb.makeAttr("product", "SCAN", od,
                    rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("startdate", sliceDate[i], od,
                    rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("starttime", sliceTime[i], od,
                    rb.H5_STRING));
            dataset.appendChild(dataset_what);

            Element dataset_where = od.createElement(rb.H5_GROUP);
            dataset_where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);

            dataset_where.appendChild(rb.makeAttr("elangle", pangle[i], od,
                    rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("nbins", bins[i], od,
                    rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr("rstart", srange[i], od,
                    rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("rscale", rstep[i], od,
                    rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("nrays", rays[i], od,
                    rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr("a1gate", a1gate[i], od,
                    rb.H5_LONG));
            dataset.appendChild(dataset_where);

            Element data1 = od.createElement(rb.H5_GROUP);
            data1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA + "1");
            Element data_what = od.createElement(rb.H5_GROUP);
            data_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            data_what.appendChild(rb.makeAttr("quantity", datatype[i]
                    .toUpperCase(), od, rb.H5_STRING));
            data_what.appendChild(rb
                    .makeAttr("gain", gain[i], od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr("offset", min[i], od,
                    rb.H5_DOUBLE));

            data_what.appendChild(rb.makeAttr("nodata", String
                    .valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr("undetect", String
                    .valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));
            data1.appendChild(data_what);

            Element dataset1 = od.createElement(rb.H5_DATASET);
            dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA);

            dataset1.setAttribute("data_type", rb.H5_INTEGER);
            dataset1.setAttribute("data_size", String.valueOf(dataDepth));
            dataset1.setAttribute("chunk", rb.H5_DATA_CHUNK + "x"
                    + rb.H5_DATA_CHUNK);

            dataset1.setAttribute("dimensions", rays[i] + "x" + bins[i]);
            dataset1.setAttribute("gzip_level", rb.H5_GZIP_LEVEL);

            String dataFileName = dataDir + File.separator + rb.H5_DATA
                    + (i + 1) + ".dat";
            Text text = od.createTextNode(dataFileName);
            dataset1.appendChild(text);
            data1.appendChild(dataset1);
            dataset.appendChild(data1);
            root.appendChild(dataset);

            int width = Integer.parseInt(rays[i]);
            int height = Integer.parseInt(bins[i]);
            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(dataBuff[i]
                    .getDataBuffer(), width, height, verbose);
            rb.writeRAINBOWData(infDataBuff, dataFileName, verbose);
        }

        // Save XML document in file
        rb.proc.saveXMLFile(od, fileName, verbose);

    }

    /**
     * Method looks for index of the smallest azimuth number which represents 0
     * degree angle (North)
     * 
     * @return index of the smallest azimuth from the intArray
     */
    public static int startingAzimuthNumber(byte[] data, int length) {

        int value = 0, minValue = 99999, counter = 0;

        for (int i = 0; i < length; i++) {

            value = byte2int(data[i * 2], data[i * 2 + 1]);

            if (value < minValue) {
                minValue = value;
                counter = i;
            }
        }
        return length - counter;
    }

    /**
     * Method ret
     * 
     * @param arr
     * @param start
     * @return
     */
    public static int byte2int(byte high, byte low) {

        return (int) ((high & 0xff) << 8 | (low & 0xff));
    }

}
