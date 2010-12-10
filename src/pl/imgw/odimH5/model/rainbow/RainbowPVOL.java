/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;
import java.util.HashMap;

import ncsa.hdf.hdf5lib.HDF5Constants;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.PVOL_H5;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.RadarOptions;

/**
 * 
 * Converts Rainbow volume files to XML descriptor or HDF format.
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class RainbowPVOL {

    private static final String VER52X = "5.2";
    private static final String VER53X = "5.3";
    private static final String PRODUCT = "SCAN";

    private boolean verbose;
    private RainbowModel rb;
    private RadarOptions[] options;

    private String version;
    // private String radarName = "";
    private String filePrefix = "";
    private String outputFileName = "";
    private String date = "";
    private String time = "";
    private String rangestep;
    private int shift = 0;
    private int size = 0;
    private String nray_new = "";
    private String nray_org = "";

    private HashMap<String, String> whatG;
    private HashMap<String, String> howG;
    private HashMap<String, String> whereG;
    private PVOLSlicesCont[] slices;

    private HashMap<Integer, DataBufferContainer> blobs;

    /**
     * 
     * @param outputFileName
     * @param fileBuff
     * @param verbose
     * @param rb
     * @param options
     */

    public RainbowPVOL(String outputFileName, byte[] fileBuff, boolean verbose,
            RainbowModel rb, RadarOptions[] options) {

        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.VOLUME, verbose);

        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);
        if (inputDoc == null)
            return;

        this.verbose = verbose;
        this.rb = rb;
        this.options = options;

        NodeList nodeList = null;
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
        version = rb.getRAINBOWMetadataElement(nodeList, "version", verbose);

        whatG = makeWhatGroup(inputDoc);
        if (whatG == null)
            return;

        // before making 'how', 'what' has to be done
        howG = makeHowGroup(inputDoc);

        whereG = makeWhereGroup(inputDoc);
        if (whereG == null)
            return;

        NodeList sliceList = inputDoc.getElementsByTagName("slice");
        size = sliceList.getLength();

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "antspeed", verbose);
        double antSpeed = 1;
        try {
            antSpeed = Double.parseDouble(rb.getRAINBOWMetadataElement(
                    nodeList, "", verbose));
        } catch (NumberFormatException e) {
            System.out.println("<antspeed> is missing or is not a number");
            // return;
        }

        shift = (int) (360.0 / antSpeed);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "rangestep", verbose);
        rangestep = (rb.getRAINBOWMetadataElement(nodeList, "", verbose));
        try {
            rangestep = String.valueOf(Double.parseDouble(rangestep) * 1000);
        } catch (NumberFormatException e) {
            System.out.println("<rangestep> is not a number");
            return;
        }

        blobs = rb.getAllRainbowDataBlobs(fileBuff, verbose);
        if (blobs.size() == 0) {
            return;
        }

        slices = makeSlices(sliceList);

        // ============ set output file name ==================
        if (outputFileName.isEmpty()) {
            this.outputFileName = filePrefix + whatG.get(PVOL_H5.DATE)
                    + whatG.get(PVOL_H5.TIME) + ".h5";
        } else {
            this.outputFileName = outputFileName;
        }

    }

    public void makeXML() {

        HDF5Model proc = rb.getHDFModel();

        // Create XML document object
        Document od = rb.hdf.createXMLDocumentObject(verbose);
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
        what
                .appendChild(rb.makeAttr(PVOL_H5.OBJECT, rb.PVOL, od,
                        rb.H5_STRING));
        what.appendChild(rb.makeAttr(PVOL_H5.VERSION, rb.VERSION, od,
                rb.H5_STRING));
        what.appendChild(rb.makeAttr(PVOL_H5.DATE, whatG.get(PVOL_H5.DATE), od,
                rb.H5_STRING));
        what.appendChild(rb.makeAttr(PVOL_H5.TIME, whatG.get(PVOL_H5.TIME), od,
                rb.H5_STRING));
        what.appendChild(rb.makeAttr(PVOL_H5.SOURCE, whatG.get(PVOL_H5.SOURCE),
                od, rb.H5_STRING));
        root.appendChild(what);

        // where group
        Element where = od.createElement(rb.H5_GROUP);
        where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);
        where.appendChild(rb.makeAttr(PVOL_H5.LON, whereG.get(PVOL_H5.LON), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr(PVOL_H5.LAT, whereG.get(PVOL_H5.LAT), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr(PVOL_H5.HEIGHT, whereG
                .get(PVOL_H5.HEIGHT), od, rb.H5_DOUBLE));
        root.appendChild(where);

        // how group
        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);
        how.appendChild(rb.makeAttr(PVOL_H5.STARTEPOCHS, howG
                .get(PVOL_H5.STARTEPOCHS), od, rb.H5_LONG));
        how.appendChild(rb.makeAttr(PVOL_H5.ENDEPOCHS, howG
                .get(PVOL_H5.ENDEPOCHS), od, rb.H5_LONG));
        how.appendChild(rb.makeAttr(PVOL_H5.SYSTEM, rb.RAINBOW_SYSTEM, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.SOFTWARE, rb.RAINBOW_SOFTWARE, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.SW_VERSION, howG
                .get(PVOL_H5.SW_VERSION), od, rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.BEAMWIDTH, howG
                .get(PVOL_H5.BEAMWIDTH), od, rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr(PVOL_H5.WAVELENGTH, howG
                .get(PVOL_H5.WAVELENGTH), od, rb.H5_DOUBLE));
        root.appendChild(how);

        for (int i = 0; i < size; i++) {

            PVOLSlicesCont s = slices[i];

            // dataset
            Element dataset = od.createElement(rb.H5_GROUP);
            dataset.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATASET + (i + 1));

            // what
            Element dataset_what = od.createElement(rb.H5_GROUP);
            dataset_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.PRODUCT,
                    rb.RAINBOW_SCAN, od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.STARTDATE, s.dsWhat
                    .get(PVOL_H5.STARTDATE), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.STARTTIME, s.dsWhat
                    .get(PVOL_H5.STARTTIME), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.ENDDATE, s.dsdWhat
                    .get(PVOL_H5.ENDDATE), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.ENDTIME, s.dsWhat
                    .get(PVOL_H5.ENDTIME), od, rb.H5_STRING));
            dataset.appendChild(dataset_what);

            // where
            Element dataset_where = od.createElement(rb.H5_GROUP);
            dataset_where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);

            dataset_where.appendChild(rb.makeAttr(PVOL_H5.ELANGLE, s.dsWhere
                    .get(PVOL_H5.ELANGLE), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.NBINS, s.dsWhere
                    .get(PVOL_H5.NBINS), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.RSTART, s.dsWhere
                    .get(PVOL_H5.RSTART), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.RSCALE, s.dsWhere
                    .get(PVOL_H5.RSCALE), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.NRAYS, s.dsWhere
                    .get(PVOL_H5.NRAYS), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.A1GATE, s.dsWhere
                    .get(PVOL_H5.A1GATE), od, rb.H5_LONG));
            dataset.appendChild(dataset_where);

            // data1 what
            Element data1 = od.createElement(rb.H5_GROUP);
            data1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA + "1");
            Element data_what = od.createElement(rb.H5_GROUP);
            data_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            data_what.appendChild(rb.makeAttr(PVOL_H5.QUANTITY, s.dsdWhat.get(
                    PVOL_H5.QUANTITY).toUpperCase(), od, rb.H5_STRING));
            data_what.appendChild(rb.makeAttr(PVOL_H5.GAIN, s.dsdWhat
                    .get(PVOL_H5.GAIN), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.OFFSET, s.dsdWhat
                    .get(PVOL_H5.OFFSET), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.NODATA, String
                    .valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.UNDETECT, String
                    .valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));
            data1.appendChild(data_what);

            // dataset

            int rays = Integer.parseInt(s.dsWhere.get(PVOL_H5.NRAYS));
            int bins = Integer.parseInt(s.dsWhere.get(PVOL_H5.NBINS));

            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(s
                    .getDataBuff().getDataBuffer(), bins, Integer
                    .parseInt(nray_org), verbose);

            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins, Integer
                    .parseInt(s.dsWhere.get(PVOL_H5.A1GATE)));

            infDataBuff = proc.transposeArray(infDataBuff, rays, bins);
            Element dataset1 = od.createElement(rb.H5_DATASET);
            dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA);

            dataset1.setAttribute(PVOL_H5.DATA_TYPE, rb.H5_INTEGER);
            dataset1.setAttribute(PVOL_H5.DATA_SIZE, s.dsdData
                    .get(PVOL_H5.DATA_SIZE));
            dataset1.setAttribute(PVOL_H5.CHUNK, rb.H5_DATA_CHUNK + "x"
                    + rb.H5_DATA_CHUNK);

            dataset1.setAttribute(PVOL_H5.DIMENSIONS, s.dsWhere
                    .get(PVOL_H5.NRAYS)
                    + "x" + s.dsWhere.get(PVOL_H5.NBINS));
            dataset1.setAttribute(PVOL_H5.GZIP_LEVEL, rb.H5_GZIP_LEVEL);

            dataset1.setAttribute(PVOL_H5.CLASS, rb.IMAGE);
            dataset1.setAttribute(PVOL_H5.IM_VER, rb.IMAGE_VER);

            String dataDir = rb.hdf.createDirectory("data", verbose);

            String dataFileName = dataDir + File.separator + rb.H5_DATA
                    + (i + 1) + ".dat";
            Text text = od.createTextNode(dataFileName);
            dataset1.appendChild(text);
            data1.appendChild(dataset1);
            dataset.appendChild(data1);
            root.appendChild(dataset);

            rb.writeRAINBOWData(infDataBuff, dataFileName, verbose);
        }

        // Save XML document in file
        rb.hdf.saveXMLFile(od, outputFileName, verbose);
    }

    public void makeH5() {

        HDF5Model proc = rb.getHDFModel();

        // HDF5 file identifier
        int file_id = -1;
        int child_group_id = -1;
        // HDF5 file operation status

        file_id = proc.H5Fcreate_wrap(outputFileName,
                HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT,
                HDF5Constants.H5P_DEFAULT, verbose);

        // ======================= what group ==============================
        child_group_id = proc.H5Gcreate_wrap(file_id, "/what", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.OBJECT, rb.H5_STRING,
                rb.PVOL, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.VERSION, rb.H5_STRING,
                rb.VERSION, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.DATE, rb.H5_STRING,
                whatG.get(PVOL_H5.DATE), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.TIME, rb.H5_STRING,
                whatG.get(PVOL_H5.TIME), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SOURCE, rb.H5_STRING,
                whatG.get(PVOL_H5.SOURCE), verbose);

        proc.H5Gclose_wrap(child_group_id, verbose);

        // ======================= where group =============================
        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.LON, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.LON), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.LAT, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.LAT), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.HEIGHT, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.HEIGHT), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        // ======================= how group ===============================
        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.STARTEPOCHS,
                rb.H5_LONG, howG.get(PVOL_H5.STARTEPOCHS), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ENDEPOCHS, rb.H5_LONG,
                howG.get(PVOL_H5.ENDEPOCHS), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYSTEM, rb.H5_STRING,
                rb.RAINBOW_SYSTEM, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SOFTWARE, rb.H5_STRING,
                rb.RAINBOW_SOFTWARE, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SW_VERSION,
                rb.H5_STRING, howG.get(PVOL_H5.SW_VERSION), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.BEAMWIDTH,
                rb.H5_DOUBLE, howG.get(PVOL_H5.BEAMWIDTH), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.WAVELENGTH,
                rb.H5_DOUBLE, howG.get(PVOL_H5.WAVELENGTH), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        for (int i = 0; i < size; i++) {

            PVOLSlicesCont s = slices[i];
            int grandchild_group_id = -1;

            child_group_id = proc.H5Gcreate_wrap(file_id, "/dataset" + (i + 1),
                    0, verbose);
            // ======================= ds what group ===========================
            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "what",
                    0, verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.PRODUCT,
                    rb.H5_STRING, rb.RAINBOW_SCAN, verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.STARTDATE,
                    rb.H5_STRING, s.dsWhat.get(PVOL_H5.STARTDATE), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.STARTTIME,
                    rb.H5_STRING, s.dsWhat.get(PVOL_H5.STARTTIME), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.ENDDATE,
                    rb.H5_STRING, s.dsWhat.get(PVOL_H5.ENDDATE), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.ENDTIME,
                    rb.H5_STRING, s.dsWhat.get(PVOL_H5.ENDTIME), verbose);

            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            // ===================== ds where group ===========================
            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "where",
                    0, verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.ELANGLE,
                    rb.H5_DOUBLE, s.dsWhere.get(PVOL_H5.ELANGLE), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.NBINS,
                    rb.H5_LONG, s.dsWhere.get(PVOL_H5.NBINS), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RSTART,
                    rb.H5_DOUBLE, s.dsWhere.get(PVOL_H5.RSTART), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RSCALE,
                    rb.H5_DOUBLE, s.dsWhere.get(PVOL_H5.RSCALE), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.NRAYS,
                    rb.H5_LONG, s.dsWhere.get(PVOL_H5.NRAYS), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.A1GATE,
                    rb.H5_LONG, s.dsWhere.get(PVOL_H5.A1GATE), verbose);
            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "data1",
                    0, verbose);

            int grandgrandchild_group_id = -1;
            // ====================== dsd what group ==========================
            grandgrandchild_group_id = proc.H5Gcreate_wrap(grandchild_group_id,
                    "what", 0, verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.QUANTITY,
                    rb.H5_STRING, s.dsdWhat.get(PVOL_H5.QUANTITY), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.GAIN,
                    rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.GAIN), verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.OFFSET,
                    rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.OFFSET), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.NODATA,
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_NO_DATA), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.UNDETECT,
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_UNDETECT), verbose);

            proc.H5Gclose_wrap(grandgrandchild_group_id, verbose);

            int rays = Integer.parseInt(s.dsWhere.get(PVOL_H5.NRAYS));
            int bins = Integer.parseInt(s.dsWhere.get(PVOL_H5.NBINS));

            int dataspace_id = proc.H5Screate_simple_wrap(2, rays, bins, null,
                    verbose);

            grandgrandchild_group_id = proc.H5Dcreate_wrap(grandchild_group_id,
                    "data", HDF5Constants.H5T_STD_U8BE, dataspace_id, Integer
                            .parseInt(rb.H5_DATA_CHUNK), Integer
                            .parseInt(rb.H5_GZIP_LEVEL), verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.CLASS,
                    rb.H5_STRING, rb.IMAGE, verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.IM_VER,
                    rb.H5_STRING, rb.IMAGE_VER, verbose);

            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(s
                    .getDataBuff().getDataBuffer(), bins, Integer
                    .parseInt(nray_org), verbose);

            // przesunac azymuty
            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins, Integer
                    .parseInt(s.dsWhere.get(PVOL_H5.A1GATE)));

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

    private HashMap<String, String> makeWhatGroup(Document inputDoc) {

        NodeList nodeList = null;
        HashMap<String, String> what = new HashMap<String, String>();

        String source = "";

        if (version.substring(0, 3).matches(VER52X)) {

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        } else if (version.substring(0, 3).matches(VER53X)) {

            nodeList = rb
                    .getRAINBOWNodesByName(inputDoc, "sensorinfo", verbose);
            source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        } else {
            System.out.println("version of the volume not supported");
            return null;
        }

        String radarName = "";

        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarWMOName();
                filePrefix = options[i].getFileName();
                if (options[i].getNrays() != null)
                    nray_new = options[i].getNrays();
                break;
            }
        }

        if (radarName.isEmpty()) {
            System.out.println("Add " + source + " to options.xml");
            return null;
        } else {
            source = "WMO:" + radarName;
        }

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        what.put(PVOL_H5.DATE, rb.parseRAINBOWDate(date, verbose));
        what.put(PVOL_H5.SOURCE, source);
        what.put(PVOL_H5.TIME, rb.parseRAINBOWTime(time, verbose));

        return what;
    }

    /**
     * 
     * @param inputDoc
     * @return
     */
    private HashMap<String, String> makeHowGroup(Document inputDoc) {

        HashMap<String, String> how = new HashMap<String, String>();

        NodeList nodeList = null;

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        String beamwidth = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        String wavelength = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        how.put(PVOL_H5.STARTEPOCHS, rb.convertRAINBOWDate2Epoch(date, time,
                verbose));
        how.put(PVOL_H5.ENDEPOCHS, rb.convertRAINBOWDate2Epoch(date, time,
                verbose));
        how.put(PVOL_H5.SYSTEM, rb.RAINBOW_SYSTEM);
        how.put(PVOL_H5.SOFTWARE, rb.RAINBOW_SOFTWARE);
        how.put(PVOL_H5.SW_VERSION, version);
        how.put(PVOL_H5.BEAMWIDTH, beamwidth);
        how.put(PVOL_H5.WAVELENGTH, wavelength);

        return how;

    }

    /**
     * @param inputDoc
     * @return
     */
    private HashMap<String, String> makeWhereGroup(Document inputDoc) {

        HashMap<String, String> where = new HashMap<String, String>();

        NodeList nodeList = null;
        String lon = "";
        String lat = "";
        String height = "";

        if (version.substring(0, 3).matches(VER52X)) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            lon = (rb.getRAINBOWMetadataElement(nodeList, "lon", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            lat = (rb.getRAINBOWMetadataElement(nodeList, "lat", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            height = (rb.getRAINBOWMetadataElement(nodeList, "alt", verbose));
        } else if (version.substring(0, 3).matches(VER53X)) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "lon", verbose);
            lon = (rb.getRAINBOWMetadataElement(nodeList, "", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "lat", verbose);
            lat = (rb.getRAINBOWMetadataElement(nodeList, "", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "alt", verbose);
            height = (rb.getRAINBOWMetadataElement(nodeList, "", verbose));
        } else {
            return null;
        }

        where.put(PVOL_H5.LON, lon);
        where.put(PVOL_H5.LAT, lat);
        where.put(PVOL_H5.HEIGHT, height);

        return where;
    }

    /**
     * @param sliceList
     * @return
     */
    @SuppressWarnings("static-access")
    private PVOLSlicesCont[] makeSlices(NodeList sliceList) {

        PVOLSlicesCont slices[] = new PVOLSlicesCont[size];

        for (int i = 0; i < size; i++) {

            slices[i] = new PVOLSlicesCont();

            int raysBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rawdata", "blobid"));

            // =============== what group =================================
            String date = (rb.parseRAINBOWDate(rb.getValueByName(sliceList
                    .item(i), "slicedata", "date"), verbose));
            String time = (rb.parseRAINBOWTime(rb.getValueByName(sliceList
                    .item(i), "slicedata", "time"), verbose));

            slices[i].dsWhat.put(PVOL_H5.PRODUCT, PRODUCT);
            slices[i].dsWhat.put(PVOL_H5.STARTDATE, date);
            slices[i].dsWhat.put(PVOL_H5.STARTTIME, time);
            slices[i].dsWhat.put(PVOL_H5.ENDDATE, date);
            slices[i].dsWhat.put(PVOL_H5.ENDTIME, rb.parseRAINBOWTime(time,
                    shift, verbose));

            // =============== where group ================================
            String posangle = rb.getValueByName(sliceList.item(i), "posangle",
                    null);
            String bins = rb.getValueByName(sliceList.item(i), "rawdata",
                    "bins");
            String srange = rb.getValueByName(sliceList.item(i), "start_range",
                    null);

            if (srange == null) // default value is "0"
                srange = "0";

            nray_org = rb.getValueByName(sliceList.item(i), "rawdata", "rays");

            DataBufferContainer raysBuff = blobs.get(raysBlobNumber);
            byte[] infRaysBuff = rb.inflate1DRAINBOWDataSection(raysBuff
                    .getDataBuffer(), raysBuff.getDataBufferLength(), verbose);
            String a1gate = String.valueOf(startingAzimuthNumber(infRaysBuff,
                    Integer.parseInt(nray_org)));

            String rays = "";
            if (!nray_new.isEmpty())
                rays = nray_new;
            else
                rays = nray_org;

            slices[i].dsWhere.put(PVOL_H5.ELANGLE, posangle);
            slices[i].dsWhere.put(PVOL_H5.NBINS, bins);
            slices[i].dsWhere.put(PVOL_H5.RSTART, srange);
            slices[i].dsWhere.put(PVOL_H5.RSCALE, rangestep);
            slices[i].dsWhere.put(PVOL_H5.NRAYS, rays);
            slices[i].dsWhere.put(PVOL_H5.A1GATE, a1gate);

            // =============== data what group ============================

            String dataType = rb.getValueByName(sliceList.item(i), "rawdata",
                    "type");

            if (dataType.matches(rb.DBZ))
                dataType = "DBZH";
            else if (dataType.matches(rb.UPHIDP)) {
                dataType = "PHIDP";
            }

            String dataDepth = rb.getValueByName(sliceList.item(i), "rawdata",
                    "depth");
            String min = rb.getValueByName(sliceList.item(i), "dynz", "min");
            String max = rb.getValueByName(sliceList.item(i), "dynz", "max");
            String gain = rb.getRAINBOWGain(min, max, Integer
                    .parseInt(dataDepth));

            slices[i].dsdWhat.put(PVOL_H5.QUANTITY, dataType);
            slices[i].dsdWhat.put(PVOL_H5.GAIN, gain);
            slices[i].dsdWhat.put(PVOL_H5.OFFSET, rb
                    .getRAINBOWOffset(min, gain));

            // =============== data dataset ================================
            DataBufferContainer dataBuff = blobs.get(dataBlobNumber);
            slices[i].dsdData.put(PVOL_H5.DATA_SIZE, dataDepth);
            slices[i].setDataBuff(dataBuff);

        }
        return slices;
    }

    /**
     * 
     * Method looks for index of the smallest azimuth number which represents 0
     * degree angle (North)
     * 
     * @param data
     * @param length
     * @return index of the smallest azimuth from the intArray
     */

    public int startingAzimuthNumber(byte[] data, int length) {

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
     * 
     * Helping method, converts 2-byte array to integer
     * 
     * @param high
     * @param low
     * @return
     */

    public int byte2int(byte high, byte low) {

        return (int) ((high & 0xff) << 8 | (low & 0xff));
    }

    public String getOutputFileName() {
        return outputFileName;
    }

}
