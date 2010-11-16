/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;
import java.sql.Time;
import java.util.HashMap;

import ncsa.hdf.hdf5lib.HDF5Constants;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.ParametersContainer;
import pl.imgw.odimH5.model.SliceContainer;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.RadarOptions;

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
     * @param fileNameOut
     *            Output name of XML file
     * @param fileBuff
     *            Input file data in a byte array
     * @param verbose
     *            Verbose mode
     * @param rb
     *            Rainbow class model
     * 
     * @return Radar name in format XXX
     */

    @SuppressWarnings("static-access")
    public static boolean createDescriptor(String fileNameOut, byte[] fileBuff,
            boolean verbose, Model rb, RadarOptions[] options) {

        boolean isDirect = false;
        if (fileNameOut.isEmpty() || fileNameOut.endsWith(".h5")
                || fileNameOut.endsWith(".hdf"))
            isDirect = true;

        ParametersContainer cont = new ParametersContainer();
        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.VOLUME, verbose);

        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);
        if (inputDoc == null)
            return false;

        NodeList nodeList = null;

        // =========== what group Element ========================
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        String radarName = "";
        String filePrefix = "";

        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarWMOName();
                filePrefix = options[i].getFileName();
                break;
            }
        }

        if (radarName.isEmpty()) {
            System.out.println("Add " + source + " to options.xml");
            System.exit(0);
        } else {
            source = "WMO:" + radarName;
        }

        cont.setSource(source);

        // ============== where group ============================

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        cont.setLon(rb.getRAINBOWMetadataElement(nodeList, "lon", verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        cont.setLat(rb.getRAINBOWMetadataElement(nodeList, "lat", verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        cont.setAlt(rb.getRAINBOWMetadataElement(nodeList, "alt", verbose));

        // ============= how group ============================

        // nodeList = rb.getRAINBOWNodesByName(inputDoc, "task", verbose);
        // String task = rb.getRAINBOWMetadataElement(nodeList, "name",
        // verbose);
        // how.appendChild(rb.makeAttr("task", task, od, rb.H5_STRING));
        cont.setStartepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setEndepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setDate(rb.parseRAINBOWDate(date, verbose));
        cont.setTime(rb.parseRAINBOWTime(time, verbose));

        if (fileNameOut.isEmpty()) {
            fileNameOut = filePrefix + cont.getDate()
                    + cont.getTime().substring(0, 4) + ".h5";
        }
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
        cont.setSwVersion(rb.getRAINBOWMetadataElement(nodeList, "version",
                verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        cont.setBeamwidth(rb.getRAINBOWMetadataElement(nodeList, "", verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        cont.setWavelength(rb.getRAINBOWMetadataElement(nodeList, "", verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "antspeed", verbose);
        double antSpeed = Double.parseDouble(rb.getRAINBOWMetadataElement(nodeList,
                "", verbose));
        int shift = (int) (360.0 / antSpeed);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "rangestep", verbose);
        String rangestep = (rb.getRAINBOWMetadataElement(nodeList,
                "", verbose));
        rangestep = String.valueOf(Double.parseDouble(rangestep) * 1000);

        // ===================== datasetn group =============================

        NodeList sliceList = inputDoc.getElementsByTagName("slice");
        int datasetSize = sliceList.getLength();

        SliceContainer slices[] = new SliceContainer[datasetSize];

        HashMap<Integer, DataBufferContainer> blobs = rb
                .getAllRainbowDataBlobs(fileBuff, verbose);

        // String sliceTime[] = new String[datasetSize];
        // String sliceDate[] = new String[datasetSize];
        // String pangle[] = new String[datasetSize];
        // String bins[] = new String[datasetSize];
        // String srange[] = new String[datasetSize];
        // String rstep[] = new String[datasetSize];
        // String rays[] = new String[datasetSize];
        // String a1gate[] = new String[datasetSize];
        // String datatype[] = new String[datasetSize];
        // String min[] = new String[datasetSize];
        // String gain[] = new String[datasetSize];
        int firstBlob = -1;

        for (int i = 0; i < datasetSize; i++) {

            DataBufferContainer dataBuff = null;
            int dataDepth = 0;
            SliceContainer slice = new SliceContainer();

            // ========== datasetn specific what group ===============
            slice.setStartDate(rb.parseRAINBOWDate(rb.getValueByName(sliceList
                    .item(i), "slicedata", "date"), verbose));
            slice.setStartTime(rb.parseRAINBOWTime(rb.getValueByName(sliceList
                    .item(i), "slicedata", "time"), verbose));
            slice.setEndDate(slice.getStartDate());

            slice.setEndTime(rb.parseRAINBOWTime(slice.getStartTime(), shift,
                    verbose));
            // ============= datasetn specific where group ============

            slice.setPangle(rb.getValueByName(sliceList.item(i), "posangle",
                    null));
            slice.setBins(rb.getValueByName(sliceList.item(i), "rawdata",
                    "bins"));
            slice.setSrange(rb.getValueByName(sliceList.item(i), "start_range",
                    null));
            if (slice.getSrange() == null) // default value is "0"
                slice.setSrange("0");

            String rangestepslice = (rb.getValueByName(sliceList.item(i),
                    "rangestep", null));

            if (rangestepslice == null)
                rangestepslice = rangestep;
            slice.setRstep(rangestepslice);

            slice.setRays(rb.getValueByName(sliceList.item(i), "rawdata",
                    "rays"));

            // ===============================================================

            int raysBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rawdata", "blobid"));

            // if (firstBlob == -1) {
            // firstBlob = rb.getMin(raysBlobNumber, dataBlobNumber);
            // }

            int raysDepth = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "depth"));
            dataDepth = Integer.parseInt(rb.getValueByName(sliceList.item(i),
                    "rawdata", "depth"));

            // dataBuff = rb.getRainbowDataSection(fileBuff, dataBlobNumber,
            // firstBlob, verbose);

            dataBuff = blobs.get(dataBlobNumber);

            // DataBufferContainer raysBuff = rb.getRainbowDataSection(fileBuff,
            // raysBlobNumber, firstBlob, verbose);

            DataBufferContainer raysBuff = blobs.get(raysBlobNumber);

            byte[] infRaysBuff = rb.inflate1DRAINBOWDataSection(raysBuff
                    .getDataBuffer(), raysBuff.getDataBufferLength(), verbose);
            // =================================================================

            slice.setA1gate(String.valueOf(startingAzimuthNumber(infRaysBuff,
                    Integer.parseInt(slice.getRays()))));

            // ============= datasetn specific data group ==================

            // data specific where group

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
            slice.setDatatype(rb.getValueByName(sliceList.item(i), "rawdata",
                    "type"));

            if (slice.getDatatype().matches(rb.DBZ))
                slice.setDatatype("DBZH");
            else if (slice.getDatatype().matches(rb.UPHIDP))
                slice.setDatatype("PHIDP");

            slice.setMin(rb.getValueByName(sliceList.item(i), "dynz", "min"));
            String max = rb.getValueByName(sliceList.item(i), "dynz", "max");

            slice.setGain(rb.getRAINBOWGain(slice.getMin(), max, dataDepth));
            slice.setDataDepth(dataDepth);
            slice.setDataBuff(dataBuff);
            slices[i] = slice;

        }

        cont.setSlices(slices);

        if (isDirect) {
            makeH5(rb, verbose, cont, fileNameOut);
        } else {
            makeXML(rb, verbose, cont, fileNameOut);
        }

        return true;

    }

    private static void makeXML(Model rb, boolean verbose,
            ParametersContainer cnt, String fileName) {

        int datasetSize = cnt.getSlices().length;

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
        what.appendChild(rb.makeAttr("object", rb.PVOL, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("version", rb.VERSION, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("date", cnt.getDate(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("time", cnt.getTime(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("source", cnt.getSource(), od,
                rb.H5_STRING));
        root.appendChild(what);

        Element where = od.createElement(rb.H5_GROUP);
        where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);
        where.appendChild(rb.makeAttr("lon", cnt.getLon(), od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("lat", cnt.getLat(), od, rb.H5_DOUBLE));
        where
                .appendChild(rb.makeAttr("height", cnt.getAlt(), od,
                        rb.H5_DOUBLE));
        root.appendChild(where);

        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);
        how.appendChild(rb.makeAttr("startepochs", cnt.getStartepochs(), od,
                rb.H5_LONG));
        how.appendChild(rb.makeAttr("endepochs", cnt.getEndepochs(), od,
                rb.H5_LONG));
        how.appendChild(rb.makeAttr("system", rb.RAINBOW_SYSTEM, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("software", rb.RAINBOW_SOFTWARE, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("sw_version", cnt.getSwVersion(), od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("beamwidth", cnt.getBeamwidth(), od,
                rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr("wavelength", cnt.getBeamwidth(), od,
                rb.H5_DOUBLE));
        root.appendChild(how);

        for (int i = 0; i < datasetSize; i++) {
            Element dataset = od.createElement(rb.H5_GROUP);
            dataset.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATASET + (i + 1));

            Element dataset_what = od.createElement(rb.H5_GROUP);
            dataset_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            dataset_what.appendChild(rb.makeAttr("product", "SCAN", od,
                    rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("startdate",
                    cnt.getSlices()[i].getStartDate(), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("starttime",
                    cnt.getSlices()[i].getStartTime(), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("enddate", cnt.getSlices()[i]
                    .getEndDate(), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr("endtime", cnt.getSlices()[i]
                    .getEndTime(), od, rb.H5_STRING));
            dataset.appendChild(dataset_what);

            Element dataset_where = od.createElement(rb.H5_GROUP);
            dataset_where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);

            dataset_where.appendChild(rb.makeAttr("elangle", cnt.getSlices()[i]
                    .getPangle(), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("nbins", cnt.getSlices()[i]
                    .getBins(), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr("rstart", cnt.getSlices()[i]
                    .getSrange(), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("rscale", cnt.getSlices()[i]
                    .getRstep(), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr("nrays", cnt.getSlices()[i]
                    .getRays(), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr("a1gate", cnt.getSlices()[i]
                    .getA1gate(), od, rb.H5_LONG));
            dataset.appendChild(dataset_where);

            Element data1 = od.createElement(rb.H5_GROUP);
            data1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA + "1");
            Element data_what = od.createElement(rb.H5_GROUP);
            data_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            data_what.appendChild(rb.makeAttr("quantity", cnt.getSlices()[i]
                    .getDatatype().toUpperCase(), od, rb.H5_STRING));
            data_what.appendChild(rb.makeAttr("gain", cnt.getSlices()[i]
                    .getGain(), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr("offset", cnt.getSlices()[i]
                    .getMin(), od, rb.H5_DOUBLE));

            data_what.appendChild(rb.makeAttr("nodata", String
                    .valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr("undetect", String
                    .valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));
            data1.appendChild(data_what);

            Element dataset1 = od.createElement(rb.H5_DATASET);
            dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA);

            dataset1.setAttribute("data_type", rb.H5_INTEGER);
            dataset1.setAttribute("data_size", String
                    .valueOf(cnt.getSlices()[i].getDataDepth()));
            dataset1.setAttribute("chunk", rb.H5_DATA_CHUNK + "x"
                    + rb.H5_DATA_CHUNK);

            dataset1.setAttribute("dimensions", cnt.getSlices()[i].getRays()
                    + "x" + cnt.getSlices()[i].getBins());
            dataset1.setAttribute("gzip_level", rb.H5_GZIP_LEVEL);

            String dataDir = rb.proc.createDirectory("data", verbose);

            String dataFileName = dataDir + File.separator + rb.H5_DATA
                    + (i + 1) + ".dat";
            Text text = od.createTextNode(dataFileName);
            dataset1.appendChild(text);
            data1.appendChild(dataset1);
            dataset.appendChild(data1);
            root.appendChild(dataset);

            int width = Integer.parseInt(cnt.getSlices()[i].getRays());
            int height = Integer.parseInt(cnt.getSlices()[i].getBins());
            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(cnt
                    .getSlices()[i].getDataBuff().getDataBuffer(), width,
                    height, verbose);
            rb.writeRAINBOWData(infDataBuff, dataFileName, verbose);
        }

        // Save XML document in file
        rb.proc.saveXMLFile(od, fileName, verbose);
    }

    private static void makeH5(Model rb, boolean verbose,
            ParametersContainer cnt, String fileName) {

        DataProcessorModel proc = rb.getDataProcessorModel();

        int datasetSize = cnt.getSlices().length;

        // HDF5 file identifier
        int file_id = -1;
        int child_group_id = -1;
        // HDF5 file operation status

        file_id = proc.H5Fcreate_wrap(fileName, HDF5Constants.H5F_ACC_TRUNC,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/what", 0, verbose);

        proc.H5Acreate_any_wrap(child_group_id, "object", rb.H5_STRING,
                rb.PVOL, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "version", rb.H5_STRING,
                rb.VERSION, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "date", rb.H5_STRING, cnt
                .getDate(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "time", rb.H5_STRING, cnt
                .getTime(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "source", rb.H5_STRING, cnt
                .getSource(), verbose);

        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lon", rb.H5_DOUBLE, cnt
                .getLon(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lat", rb.H5_DOUBLE, cnt
                .getLat(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "height", rb.H5_DOUBLE, cnt
                .getAlt(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "startepochs", rb.H5_LONG, cnt
                .getStartepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "endepochs", rb.H5_LONG, cnt
                .getEndepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "system", rb.H5_STRING,
                rb.RAINBOW_SYSTEM, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "software", rb.H5_STRING,
                rb.RAINBOW_SOFTWARE, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "sw_version", rb.H5_STRING, cnt
                .getSwVersion(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "beamwidth", rb.H5_DOUBLE, cnt
                .getBeamwidth(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "wavelength", rb.H5_DOUBLE, cnt
                .getWavelength(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        for (int i = 0; i < datasetSize; i++) {

            int grandchild_group_id = -1;

            child_group_id = proc.H5Gcreate_wrap(file_id, "/dataset" + (i + 1),
                    0, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "what",
                    0, verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, "product",
                    rb.H5_STRING, "SCAN", verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "startdate",
                    rb.H5_STRING, cnt.getSlices()[i].getStartDate(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "starttime",
                    rb.H5_STRING, cnt.getSlices()[i].getStartTime(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "enddate",
                    rb.H5_STRING, cnt.getSlices()[i].getEndDate(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "endtime",
                    rb.H5_STRING, cnt.getSlices()[i].getEndTime(), verbose);

            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "where",
                    0, verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, "elangle",
                    rb.H5_DOUBLE, cnt.getSlices()[i].getPangle(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "nbins", rb.H5_LONG,
                    cnt.getSlices()[i].getBins(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "rstart",
                    rb.H5_DOUBLE, cnt.getSlices()[i].getSrange(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "rscale",
                    rb.H5_DOUBLE, cnt.getSlices()[i].getRstep(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "nrays", rb.H5_LONG,
                    cnt.getSlices()[i].getRays(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "a1gate", rb.H5_LONG,
                    cnt.getSlices()[i].getA1gate(), verbose);
            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "data1",
                    0, verbose);

            int grandgrandchild_group_id = -1;

            grandgrandchild_group_id = proc.H5Gcreate_wrap(grandchild_group_id,
                    "what", 0, verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "quantity",
                    rb.H5_STRING, cnt.getSlices()[i].getDatatype(), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "gain",
                    rb.H5_DOUBLE, cnt.getSlices()[i].getGain(), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "offset",
                    rb.H5_DOUBLE, cnt.getSlices()[i].getMin(), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "nodata",
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_NO_DATA), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "undetect",
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_UNDETECT), verbose);

            proc.H5Gclose_wrap(grandgrandchild_group_id, verbose);

            int rays = Integer.parseInt(cnt.getSlices()[i].getRays());
            int bins = Integer.parseInt(cnt.getSlices()[i].getBins());

            int dataspace_id = proc.H5Screate_simple_wrap(2, rays, bins, null,
                    verbose);

            grandgrandchild_group_id = proc.H5Dcreate_wrap(grandchild_group_id,
                    "data", HDF5Constants.H5T_STD_U8BE, dataspace_id, Integer
                            .parseInt(rb.H5_DATA_CHUNK), Integer
                            .parseInt(rb.H5_GZIP_LEVEL), verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "CLASS",
                    rb.H5_STRING, "IMAGE", verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "IMAGE_VERSION",
                    rb.H5_STRING, rb.IMAGE_VER, verbose);

            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(cnt
                    .getSlices()[i].getDataBuff().getDataBuffer(), bins, rays,
                    verbose);

            // przesunac azymuty
            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins, Integer
                    .parseInt(cnt.getSlices()[i].getA1gate()));

            infDataBuff = proc.transposeArray(infDataBuff, rays, bins);

            proc.H5Dwrite_wrap(grandgrandchild_group_id,
                    HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                    HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                    infDataBuff, verbose);

            proc.H5Dclose_wrap(grandgrandchild_group_id, verbose);
            proc.H5Gclose_wrap(grandchild_group_id, verbose);
            proc.H5Gclose_wrap(child_group_id, verbose);
            proc.H5Sclose_wrap(dataspace_id, verbose);

        }

        // proc.H5Gclose_wrap(child_group_id, verbose);

        proc.H5Fclose_wrap(file_id, verbose);

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

        return counter;
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
