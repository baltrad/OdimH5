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
public class Rainbow2HDFPVOL {

    private static final String VER51X = "5.1";
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
    private String source = "";
    private String radarFullName = "";
    private int shift = 0;
    private int size = 0;
    private String nray_new = "";
    private String nray_org[];

    public boolean correct = false;

    private HashMap<String, String> whatG;
    private HashMap<String, String> howG;
    private HashMap<String, String> whereG;
    private HashMap<String, String> qiG;
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

    public Rainbow2HDFPVOL(String outputFileName, byte[] fileBuff, boolean verbose,
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

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "QI", verbose);
        String isQI = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        if (isQI.matches("1"))
            qiG = makeQIGroup(inputDoc);
        else
            qiG = null;

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
            this.outputFileName = whatG.get(PVOL_H5.DATE)
                    + whatG.get(PVOL_H5.TIME) + ".h5";
            if (filePrefix != null && !filePrefix.isEmpty())
                this.outputFileName = filePrefix + this.outputFileName;
        } else {
            this.outputFileName = outputFileName;
        }

        correct = true;

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
                    .parseInt(nray_org[i]), verbose);

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
        // =============== quality index ================
        if (qiG != null)
            makeQIfields(proc, child_group_id);

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

            int qiRays = 0;
            int qiBins = 0;
            if (s.dsWhere.get(PVOL_H5.QI_NRAYS) != null
                    && !s.dsWhere.get(PVOL_H5.QI_NRAYS).isEmpty()) {
                qiRays = Integer.parseInt(s.dsWhere.get(PVOL_H5.QI_NRAYS));
            }
            if (s.dsWhere.get(PVOL_H5.QI_NBINS) != null
                    && !s.dsWhere.get(PVOL_H5.QI_NBINS).isEmpty()) {
                qiBins = Integer.parseInt(s.dsWhere.get(PVOL_H5.QI_NBINS));
            }

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
                    .parseInt(nray_org[i]), verbose);

            // przesunac azymuty
            // System.out.println(i + ": rays=" + rays + " bins=" + bins);
            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins, Integer
                    .parseInt(s.dsWhere.get(PVOL_H5.A1GATE)));

            infDataBuff = proc.transposeArray(infDataBuff, rays, bins);

            proc.H5Dwrite_wrap(grandgrandchild_group_id,
                    HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                    HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                    infDataBuff, verbose);

            // ====================== quality index group ===================

            int[][] infQiBuff = null;
            if (qiRays > 0 && qiBins > 0 && s.getQiBuff() != null) {

                // System.out.println("qbins: " + qiBins);
                // System.out.println("qrays: " + qiRays);

                int qi_group_id = -1;
                int qi_children_group_id = -1;
                qi_group_id = proc.H5Gcreate_wrap(grandchild_group_id,
                        "quality1", 0, verbose);
               
                //quantity/what
                qi_children_group_id = proc.H5Gcreate_wrap(qi_group_id, "what",
                        0, verbose);
                
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.GAIN,
                        rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.QI_GAIN), verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.OFFSET,
                        rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.QI_OFFSET), verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.NODATA,
                        rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_NO_DATA), verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.UNDETECT,
                        rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_UNDETECT), verbose);
                
                proc.H5Gclose_wrap(qi_children_group_id, verbose);

                int qi_dataspace_id = proc.H5Screate_simple_wrap(2, qiRays,
                        qiBins, null, verbose);
                qi_children_group_id = proc.H5Dcreate_wrap(qi_group_id, "data",
                        HDF5Constants.H5T_STD_U8BE, qi_dataspace_id, Integer
                                .parseInt(rb.H5_DATA_CHUNK), Integer
                                .parseInt(rb.H5_GZIP_LEVEL), verbose);

                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.CLASS,
                        rb.H5_STRING, rb.IMAGE, verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.IM_VER,
                        rb.H5_STRING, rb.IMAGE_VER, verbose);
                
                infQiBuff = rb.inflate2DRAINBOWDataSection(s.getQiBuff()
                        .getDataBuffer(), qiBins, qiRays, verbose);

                infQiBuff = proc.transposeArray(infQiBuff, qiRays, qiBins);

                proc.H5Dwrite_wrap(qi_children_group_id,
                        HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                        infQiBuff, verbose);
                proc.H5Dclose_wrap(qi_children_group_id, verbose);
                proc.H5Gclose_wrap(qi_group_id, verbose);

            }

            proc.H5Dclose_wrap(grandgrandchild_group_id, verbose);
            proc.H5Gclose_wrap(grandchild_group_id, verbose);
            proc.H5Gclose_wrap(child_group_id, verbose);
            proc.H5Sclose_wrap(dataspace_id, verbose);

        }

        // proc.H5Gclose_wrap(child_group_id, verbose);

        proc.H5Fclose_wrap(file_id, verbose);

    }

    /**
     * @param proc
     * @param child_group_id
     */
    private void makeQIfields(HDF5Model proc, int child_group_id) {
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GEN_a, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.GEN_a), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GEN_b, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.GEN_b), verbose);

        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SYS_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SYS_QIOn), verbose);
        
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Freq, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SYS_Freq), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIFreq,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIFreq), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Beam, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SYS_Beam), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIBeam,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIBeam), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Elev, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SYS_Elev), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIElev,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIElev), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Azim, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SYS_Azim), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIAzim,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIAzim), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Filter,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_Filter), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIFilter,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIFilter), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Detect,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_Detect), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIDetect,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIDetect), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Speed,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_Speed), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QISpeed,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QISpeed), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Radome,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_Radome), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIRadome,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIRadome), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_Calibr,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_Calibr), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QICalibr,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QICalibr), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_TSamp,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_TSamp), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QITSamp,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QITSamp), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_RSamp,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_RSamp), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SYS_QIRSamp,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SYS_QIRSamp), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AH_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.AH_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AH_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.AH_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AH_QI0, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.AH_QI0), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AH_QI1, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.AH_QI1), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AV_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.AV_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AV_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.AV_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AV_QI0, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.AV_QI0), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.AV_QI1, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.AV_QI1), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GC_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.GC_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GC_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.GC_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GC_QI, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.GC_QI), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GC_QIUn, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.GC_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.GC_MinPbb,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.GC_MinPbb), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SPIKE_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SPIKE_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_QI, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SPIKE_QI), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_QIUn,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPIKE_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_Diff,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPIKE_Diff), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_Azim,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPIKE_Azim), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_Refl,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPIKE_Refl), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPIKE_Perc,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPIKE_Perc), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.RSPEC_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.RSPEC_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_QI, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.RSPEC_QI), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_QIUn,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.RSPEC_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_Grid,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.RSPEC_Grid), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_Num,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.RSPEC_Num), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RSPEC_Step,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.RSPEC_Step), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SPEC_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SPEC_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_QI, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SPEC_QI), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_QIUn,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPEC_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_Grid,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPEC_Grid), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_Num, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SPEC_Num), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SPEC_Step,
                rb.H5_DOUBLE, qiG.get(PVOL_H5.SPEC_Step), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.PBB_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.PBB_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.PBB_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.ATT_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.PBB_QIUn, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.PBB_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.PBB_Max, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.PBB_Max), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.ATT_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.PBB_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_a, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_a), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_b, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_b), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_QI0, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_QI0), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_QI1, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_QI1), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_QIUn, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_QIUn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_Refl, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_Refl), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_Last, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_Last), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ATT_Sum, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.ATT_Sum), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SUM_QCOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SUM_QCOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SUM_QIOn, rb.H5_LONG,
                qiG.get(PVOL_H5.SUM_QIOn), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SUM_QI0, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SUM_QI0), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.SUM_QI1, rb.H5_DOUBLE,
                qiG.get(PVOL_H5.SUM_QI1), verbose);

    }

    private HashMap<String, String> makeWhatGroup(Document inputDoc) {

        NodeList nodeList = null;
        HashMap<String, String> what = new HashMap<String, String>();

        String source = "";

        if (version.substring(0, 3).matches(VER51X)
                || version.substring(0, 3).matches(VER52X)) {

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);
            radarFullName = rb.getRAINBOWMetadataElement(nodeList, "name",
                    verbose);

        } else if (version.substring(0, 3).matches(VER53X)) {

            nodeList = rb
                    .getRAINBOWNodesByName(inputDoc, "sensorinfo", verbose);
            source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);
            radarFullName = rb.getRAINBOWMetadataElement(nodeList, "name",
                    verbose);

        } else {
            System.out.println("version of the volume not supported");
            return null;
        }

        this.source = source;
        String radarName = "";

        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarWMOName();
                filePrefix = options[i].getFileName();
                if (options[i].getNrays() != null)
                    nray_new = options[i].getNrays();
                if (options[i].getLocation() != null)
                    radarFullName = options[i].getLocation();
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
     * Quality index
     * 
     * @param inputDoc
     * @return
     */
    private HashMap<String, String> makeQIGroup(Document inputDoc) {

        HashMap<String, String> qi = new HashMap<String, String>();

        NodeList nodeList = null;
        String value = "";

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GEN_a, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GEN_a, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GEN_b, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GEN_b, value);

        // brakuje QCOn oraz QIOn
        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QCOn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIOn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Freq, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Freq, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIFreq,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIFreq, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Beam, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Beam, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIBeam,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIBeam, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Elev, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Elev, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIElev,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIElev, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Azim, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Azim, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIAzim,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIAzim, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Filter,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Filter, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIFilter,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIFilter, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Detect,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Detect, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIDetect,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIDetect, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Speed,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Speed, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QISpeed,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QISpeed, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Radome,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Radome, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIRadome,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIRadome, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_Calibr,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_Calibr, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QICalibr,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QICalibr, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_TSamp,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_TSamp, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QITSamp,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QITSamp, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_RSamp,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_RSamp, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SYS_QIRSamp,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SYS_QIRSamp, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AH_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AH_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AH_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AH_QIOn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AH_QI0, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AH_QI0, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AH_QI1, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AH_QI1, value);


        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AV_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AV_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AV_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AV_QIOn, value);

        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AV_QI0, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AV_QI0, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.AV_QI1, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AV_QI1, value);


        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GC_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.AH_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GC_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GC_QIOn, value);

        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GC_QI, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GC_QI, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GC_QIUn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GC_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.GC_MinPbb,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.GC_MinPbb, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_QIOn, value);
        
        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QI, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_QI, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QIUn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_Diff,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_Diff, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_Azim,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_Azim, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_Refl,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_Refl, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_Perc,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_Perc, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_QIOn, value);
        
        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QI, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_QI, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QIUn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_Grid,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_Grid, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_Num,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_Num, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_Step,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_Step, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_QIOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QI, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_QI, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QIUn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_Grid,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_Grid, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_Num, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_Num, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_Step,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_Step, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QIOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_Max, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_Max, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QIUn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QIOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_a, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_a, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_b, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_b, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QIUn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QIUn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QI1, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QI1, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QI0, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QI0, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_Refl, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_Refl, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_Last, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_Last, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_Sum, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_Sum, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SUM_QCOn, value);
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SUM_QIOn, value);        

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QI0, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SUM_QI0, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QI1, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SUM_QI1, value);
        
        return qi;

    }

    /**
     * QI helping method
     * 
     * @param value
     * @return
     */
    private String getZeroOneValue(String value) {

        if (value.matches("On"))
            return "1";

        return "0";
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

        if (version.substring(0, 3).matches(VER51X)
                || version.substring(0, 3).matches(VER52X)) {
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

        nray_org = new String[size];
        for (int i = 0; i < size; i++) {

            slices[i] = new PVOLSlicesCont();

            int raysBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(rb.getValueByName(sliceList
                    .item(i), "rawdata", "blobid"));
            String qi = rb
                    .getValueByName(sliceList.item(i), "QIdata", "blobid");
            int qiBlobNumber = -1;
            if (qi != null && !qi.isEmpty())
                qiBlobNumber = Integer.parseInt(qi);

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

            // =============== quality index prefs =========================
            String qiBins = rb.getValueByName(sliceList.item(i), "QIdata",
                    "bins");
            
            String qiRays = rb.getValueByName(sliceList.item(i), "QIdata",
                    "rays");
            
            String dataDepth = rb.getValueByName(sliceList.item(i), "QIdata",
            "depth");
            String min = rb.getValueByName(sliceList.item(i), "QIdata", "min");
            String max = rb.getValueByName(sliceList.item(i), "QIdata", "max");
            String gain = null;
            
            if(min != null && max != null) {
            
            gain = rb.getRAINBOWGain(min, max, Integer
                    .parseInt(dataDepth));
            slices[i].dsdWhat.put(PVOL_H5.QI_GAIN, gain);
            slices[i].dsdWhat.put(PVOL_H5.QI_OFFSET, rb
                    .getRAINBOWOffset(min, gain));
//            System.out.println("min=" + min + " max=" + max + " depth=" + dataDepth + " gain=" + gain);
            }

            if (srange == null) // default value is "0"
                srange = "0";

            nray_org[i] = rb.getValueByName(sliceList.item(i), "rawdata",
                    "rays");

            DataBufferContainer raysBuff = blobs.get(raysBlobNumber);
            byte[] infRaysBuff = rb.inflate1DRAINBOWDataSection(raysBuff
                    .getDataBuffer(), raysBuff.getDataBufferLength(), verbose);
            String a1gate = String.valueOf(startingAzimuthNumber(infRaysBuff,
                    Integer.parseInt(nray_org[i])));

            String rays = "";
            if (!nray_new.isEmpty())
                rays = nray_new;
            else
                rays = nray_org[i];

            slices[i].dsWhere.put(PVOL_H5.ELANGLE, posangle);
            slices[i].dsWhere.put(PVOL_H5.NBINS, bins);
            slices[i].dsWhere.put(PVOL_H5.RSTART, srange);
            slices[i].dsWhere.put(PVOL_H5.RSCALE, rangestep);
            slices[i].dsWhere.put(PVOL_H5.NRAYS, rays);
            slices[i].dsWhere.put(PVOL_H5.A1GATE, a1gate);
            if (qiBlobNumber > -1) {
                slices[i].dsWhere.put(PVOL_H5.QI_NBINS, qiBins);
                slices[i].dsWhere.put(PVOL_H5.QI_NRAYS, qiRays);
            }
            // System.out.println(i + ": rays=" + rays + " elangle=" +
            // posangle);

            // =============== data what group ============================

            String dataType = rb.getValueByName(sliceList.item(i), "rawdata",
                    "type");

            if (dataType.matches(rb.DBZ))
                dataType = "DBZH";
            else if (dataType.matches(rb.UPHIDP)) {
                dataType = "PHIDP";
            }

            dataDepth = rb.getValueByName(sliceList.item(i), "rawdata",
                    "depth");
            min = rb.getValueByName(sliceList.item(i), "rawdata", "min");
            max = rb.getValueByName(sliceList.item(i), "rawdata", "max");
            gain = rb.getRAINBOWGain(min, max, Integer
                    .parseInt(dataDepth));

            slices[i].dsdWhat.put(PVOL_H5.QUANTITY, dataType);
            slices[i].dsdWhat.put(PVOL_H5.GAIN, gain);
            slices[i].dsdWhat.put(PVOL_H5.OFFSET, rb
                    .getRAINBOWOffset(min, gain));

            // =============== data dataset ================================
            DataBufferContainer dataBuff = blobs.get(dataBlobNumber);
            DataBufferContainer qiBuff = null;
            if (qiBlobNumber > -1)
                qiBuff = blobs.get(qiBlobNumber);
            slices[i].dsdData.put(PVOL_H5.DATA_SIZE, dataDepth);
            slices[i].setDataBuff(dataBuff);
            if (qiBuff != null)
                slices[i].setQiBuff(qiBuff);

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

    public String getRadarName() {
        return source;
    }

    public String getRadarFullName() {
        return radarFullName;
    }

}
