/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER51X;
import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER52X;
import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER53X;

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
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;

/**
 * 
 * Converts Rainbow volume files to XML descriptor or HDF format.
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class Rainbow2HDF {


    protected static final String PRODUCT = "SCAN";

    protected boolean verbose;
    protected RainbowModel rb;
    protected RadarOptions[] options;
    protected int rIndex = 0;
    protected double antSpeed = 1;
    protected String highprf = "";
    protected String lowprf = "";
    protected String csr = "";
    protected String log = "";
    protected String sqi = "";

    protected String version;
    // protected String radarName = "";
    protected String filePrefix = "";
    protected String originator = "";
    protected String product_id = "";
    protected String outputFileName = "";
    protected String date = "";
    protected String time = "";
    protected String rangestep;
    protected String radarId = "";
    protected String radarFullName = "";
    protected int shift = 0;
    protected int size = 0;
    protected String nray_new = "";
    protected String nray_org[];

    public boolean correct = false;

    protected Document inputDoc;
    
    protected HashMap<String, String> whatG;
    protected HashMap<String, String> howG;
    protected HashMap<String, String> whereG;
    protected HashMap<String, String> qiG;
    protected PVOLSlicesCont[] slices;

    protected HashMap<Integer, DataBufferContainer> blobs;

    protected static final String PFLAG = "T";
    protected static final String PID = "PA";
    protected static final String OFLAG = "C";
    
    /**
     * 
     * Collecting mandatory data from fileBuff for further processing.
     * 
     * @param outputFileName
     * @param fileBuff
     * @param verbose
     * @param rb
     * @param options
     */

    public Rainbow2HDF(byte[] fileBuff,
            boolean verbose, RainbowModel rb)
            {

        this.verbose = verbose;
        this.rb = rb;
        
        this.inputDoc = getDocument(fileBuff);
        
        if (inputDoc == null)
            return;
        
        options = OptionsHandler.getOpt().getRadarOptions();

    }

    /**
     * @param fileBuff
     * @param verbose
     * @param rb
     * @return
     */
    protected Document getDocument(byte[] fileBuff) {
        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.VOLUME, verbose);

        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);
        return inputDoc;
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
        root.appendChild(rb.makeAttr(PVOL_H5.CONVENTIONS, rb.H5_CONV, od,
                rb.H5_STRING));
        od.appendChild(root);

        // what group
        Element what = od.createElement(rb.H5_GROUP);
        what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
        what.appendChild(rb.makeAttr(PVOL_H5.OBJECT, rb.PVOL, od, rb.H5_STRING));
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
        where.appendChild(rb.makeAttr(PVOL_H5.HEIGHT,
                whereG.get(PVOL_H5.HEIGHT), od, rb.H5_DOUBLE));
        root.appendChild(where);

        // how group
        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);
        how.appendChild(rb.makeAttr(PVOL_H5.STARTEPOCHS,
                howG.get(PVOL_H5.STARTEPOCHS), od, rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr(PVOL_H5.ENDEPOCHS,
                howG.get(PVOL_H5.ENDEPOCHS), od, rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr(PVOL_H5.SYSTEM, rb.RAINBOW_SYSTEM, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.SOFTWARE, rb.RAINBOW_SOFTWARE, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.SW_VERSION,
                howG.get(PVOL_H5.SW_VERSION), od, rb.H5_STRING));
        how.appendChild(rb.makeAttr(PVOL_H5.BEAMWIDTH,
                howG.get(PVOL_H5.BEAMWIDTH), od, rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr(PVOL_H5.WAVELENGTH,
                howG.get(PVOL_H5.WAVELENGTH), od, rb.H5_DOUBLE));
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
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.STARTDATE,
                    s.dsWhat.get(PVOL_H5.STARTDATE), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.STARTTIME,
                    s.dsWhat.get(PVOL_H5.STARTTIME), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.ENDDATE,
                    s.dsWhat.get(PVOL_H5.ENDDATE), od, rb.H5_STRING));
            dataset_what.appendChild(rb.makeAttr(PVOL_H5.ENDTIME,
                    s.dsWhat.get(PVOL_H5.ENDTIME), od, rb.H5_STRING));
            dataset.appendChild(dataset_what);

            // where
            Element dataset_where = od.createElement(rb.H5_GROUP);
            dataset_where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);

            dataset_where.appendChild(rb.makeAttr(PVOL_H5.ELANGLE,
                    s.dsWhere.get(PVOL_H5.ELANGLE), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.NBINS,
                    s.dsWhere.get(PVOL_H5.NBINS), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.RSTART,
                    s.dsWhere.get(PVOL_H5.RSTART), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.RSCALE,
                    s.dsWhere.get(PVOL_H5.RSCALE), od, rb.H5_DOUBLE));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.NRAYS,
                    s.dsWhere.get(PVOL_H5.NRAYS), od, rb.H5_LONG));
            dataset_where.appendChild(rb.makeAttr(PVOL_H5.A1GATE,
                    s.dsWhere.get(PVOL_H5.A1GATE), od, rb.H5_LONG));
            dataset.appendChild(dataset_where);

            // data1 what
            Element data1 = od.createElement(rb.H5_GROUP);
            data1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA + "1");
            Element data_what = od.createElement(rb.H5_GROUP);
            data_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
            data_what.appendChild(rb.makeAttr(PVOL_H5.QUANTITY,
                    s.dsdWhat.get(PVOL_H5.QUANTITY).toUpperCase(), od,
                    rb.H5_STRING));
            data_what.appendChild(rb.makeAttr(PVOL_H5.GAIN,
                    s.dsdWhat.get(PVOL_H5.GAIN), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.OFFSET,
                    s.dsdWhat.get(PVOL_H5.OFFSET), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.NODATA,
                    String.valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));
            data_what.appendChild(rb.makeAttr(PVOL_H5.UNDETECT,
                    String.valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));
            data1.appendChild(data_what);

            // dataset

            int rays = Integer.parseInt(s.dsWhere.get(PVOL_H5.NRAYS));
            int bins = Integer.parseInt(s.dsWhere.get(PVOL_H5.NBINS));

            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(
                    s.getDataBuffContainer(), bins,
                    Integer.parseInt(nray_org[i]), verbose);

            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins,
                    Integer.parseInt(s.dsWhere.get(PVOL_H5.A1GATE)));

            infDataBuff = proc.transposeArray(infDataBuff, rays, bins);
            Element dataset1 = od.createElement(rb.H5_DATASET);
            dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA);

            dataset1.setAttribute(PVOL_H5.DATA_TYPE, rb.H5_INTEGER);
            dataset1.setAttribute(PVOL_H5.DATA_SIZE,
                    s.dsdData.get(PVOL_H5.DATA_SIZE));
            dataset1.setAttribute(PVOL_H5.CHUNK, s.dsWhere.get(PVOL_H5.NRAYS)
                    + "x" + s.dsWhere.get(PVOL_H5.NBINS));

            dataset1.setAttribute(
                    PVOL_H5.DIMENSIONS,
                    s.dsWhere.get(PVOL_H5.NRAYS) + "x"
                            + s.dsWhere.get(PVOL_H5.NBINS));
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

        // ========================== root =================================
        proc.H5Acreate_any_wrap(file_id, PVOL_H5.CONVENTIONS, rb.H5_STRING,
                rb.H5_CONV, verbose);

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

        if(proc.H5Gclose_wrap(child_group_id, verbose) < 0)
            correct = false;

        // ======================= where group =============================
        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.LON, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.LON), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.LAT, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.LAT), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.HEIGHT, rb.H5_DOUBLE,
                whereG.get(PVOL_H5.HEIGHT), verbose);
        if(proc.H5Gclose_wrap(child_group_id, verbose) < 0)
            correct = false;

        // ======================= how group ===============================
        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.STARTEPOCHS,
                rb.H5_DOUBLE, howG.get(PVOL_H5.STARTEPOCHS), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ENDEPOCHS,
                rb.H5_DOUBLE, howG.get(PVOL_H5.ENDEPOCHS), verbose);
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
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.RADOMELOSS,
                rb.H5_DOUBLE, howG.get(PVOL_H5.RADOMELOSS), verbose);
        proc.H5Acreate_any_wrap(child_group_id, PVOL_H5.ANTGAIN,
                rb.H5_DOUBLE, howG.get(PVOL_H5.ANTGAIN), verbose);
        
        // =============== quality index ================
        if (qiG != null)
            makeQIfields(proc, child_group_id);

        if(proc.H5Gclose_wrap(child_group_id, verbose)< 0)
            correct = false;

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

            if(proc.H5Gclose_wrap(grandchild_group_id, verbose)< 0)
                correct = false;

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
            if(proc.H5Gclose_wrap(grandchild_group_id, verbose)< 0)
                correct = false;

            // ===================== ds how group ===========================
            // --------------- new attributes ODIM H5 2.1
            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "how",
                    0, verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.TASK, rb.H5_STRING,
                    howG.get(PVOL_H5.TASK), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.SIMULATED, rb.H5_STRING,
                    s.dsHow.get(PVOL_H5.SIMULATED), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RPM,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RPM), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.PULSEWIDTH,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.PULSEWIDTH), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RXBANDWIDTH,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RXBANDWIDTH), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.LOWPRF,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.LOWPRF), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.HIGHPRF,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.HIGHPRF), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.TXLOSS,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.TXLOSS), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RXLOSS,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RXLOSS), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RADCONSTH,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RADCONSTH), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RADCONSTV,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RADCONSTH), verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.GASATTN,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.GASATTN), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.NOMTXPOWER,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.NOMTXPOWER), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.NI,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.NI), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.VSAMPLES,
                    rb.H5_LONG, s.dsHow.get(PVOL_H5.VSAMPLES), verbose);
            
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.VSAMPLES,
                    rb.H5_LONG, s.dsHow.get(PVOL_H5.VSAMPLES), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.AZMETHOD,
                    rb.H5_STRING, s.dsHow.get(PVOL_H5.AZMETHOD), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.BINMETHOD,
                    rb.H5_STRING, s.dsHow.get(PVOL_H5.BINMETHOD), verbose);
            
            proc.H5Acreate_double_array(grandchild_group_id, PVOL_H5.STARTAZA,
                    s.getAngles(), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.MALFUNC, rb.H5_STRING,
                    s.dsHow.get(PVOL_H5.MALFUNC), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.SQI,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.SQI), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.CSR,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.CSR), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.LOG,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.LOG), verbose);
            
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.RAC,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.RAC), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.PAC,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.PAC), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, PVOL_H5.S2N,
                    rb.H5_DOUBLE, s.dsHow.get(PVOL_H5.S2N), verbose);
            if(proc.H5Gclose_wrap(grandchild_group_id, verbose)< 0)
                correct = false;
            
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

            if(proc.H5Gclose_wrap(grandgrandchild_group_id, verbose)< 0)
                correct = false;

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

            long chunk[] = new long[2];
            chunk[0] = rays;
            chunk[1] = bins;

            grandgrandchild_group_id = proc.H5Dcreate_wrap(grandchild_group_id,
                    "data", HDF5Constants.H5T_STD_U8BE, dataspace_id, chunk,
                    Integer.parseInt(rb.H5_GZIP_LEVEL), verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.CLASS,
                    rb.H5_STRING, rb.IMAGE, verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, PVOL_H5.IM_VER,
                    rb.H5_STRING, rb.IMAGE_VER, verbose);

            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(
                    s.getDataBuffContainer(), bins,
                    Integer.parseInt(nray_org[i]), verbose);

            // przesunac azymuty
            // System.out.println(i + ": rays=" + rays + " bins=" + bins);
            infDataBuff = proc.shiftAzimuths(infDataBuff, rays, bins,
                    Integer.parseInt(s.dsWhere.get(PVOL_H5.A1GATE)));

            infDataBuff = proc.transposeArray(infDataBuff, rays, bins);

            if(proc.H5Dwrite_wrap(grandgrandchild_group_id,
                    HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                    HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                    infDataBuff, verbose)< 0)
                correct = false;

            // ====================== quality index group ===================

            int[][] infQiBuff = null;
            if (qiRays > 0 && qiBins > 0 && s.getQiBuff() != null) {

                // System.out.println("qbins: " + qiBins);
                // System.out.println("qrays: " + qiRays);

                int qi_group_id = -1;
                int qi_children_group_id = -1;
                qi_group_id = proc.H5Gcreate_wrap(child_group_id,
                        "quality1", 0, verbose);

                // quantity/what
                qi_children_group_id = proc.H5Gcreate_wrap(qi_group_id, "what",
                        0, verbose);

                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.GAIN,
                        rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.QI_GAIN), verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.OFFSET,
                        rb.H5_DOUBLE, s.dsdWhat.get(PVOL_H5.QI_OFFSET), verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.NODATA,
                        rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_NO_DATA),
                        verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.UNDETECT,
                        rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_UNDETECT),
                        verbose);

                if(proc.H5Gclose_wrap(qi_children_group_id, verbose)< 0)
                    correct = false;

                int qi_dataspace_id = proc.H5Screate_simple_wrap(2, qiRays,
                        qiBins, null, verbose);

                long qi_chunk[] = new long[2];
                qi_chunk[0] = qiRays;
                qi_chunk[1] = qiBins;

                qi_children_group_id = proc.H5Dcreate_wrap(qi_group_id, "data",
                        HDF5Constants.H5T_STD_U8BE, qi_dataspace_id, qi_chunk,
                        Integer.parseInt(rb.H5_GZIP_LEVEL), verbose);

                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.CLASS,
                        rb.H5_STRING, rb.IMAGE, verbose);
                proc.H5Acreate_any_wrap(qi_children_group_id, PVOL_H5.IM_VER,
                        rb.H5_STRING, rb.IMAGE_VER, verbose);

                infQiBuff = rb.inflate2DRAINBOWDataSection(s.getQiBuff(),
                        qiBins, qiRays, verbose);

                infQiBuff = proc.transposeArray(infQiBuff, qiRays, qiBins);

                if(proc.H5Dwrite_wrap(qi_children_group_id,
                        HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                        infQiBuff, verbose)< 0)
                    correct = false;
                if(proc.H5Dclose_wrap(qi_children_group_id, verbose)< 0)
                    correct = false;
                if(proc.H5Gclose_wrap(qi_group_id, verbose)< 0)
                    correct = false;

            }

            if( proc.H5Dclose_wrap(grandgrandchild_group_id, verbose)< 0)
                correct = false;
            if(proc.H5Gclose_wrap(grandchild_group_id, verbose)< 0)
                correct = false;
            if(proc.H5Gclose_wrap(child_group_id, verbose)< 0)
                correct = false;
            if(proc.H5Sclose_wrap(dataspace_id, verbose)< 0)
                correct = false;

        }

        // proc.H5Gclose_wrap(child_group_id, verbose);

        if (proc.H5Fclose_wrap(file_id, verbose) < 0)
            correct = false;
        
    }

    /**
     * @param proc
     * @param child_group_id
     */
    protected void makeQIfields(HDF5Model proc, int parent) {
        
        int child_group_id = proc.H5Gcreate_wrap(parent, "radvol-qc", 0, verbose);
        
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

        proc.H5Gclose_wrap(child_group_id, verbose);
        
    }

    protected HashMap<String, String> makeWhatGroup(Document inputDoc) {

        NodeList nodeList = null;
        HashMap<String, String> what = new HashMap<String, String>();

        
        if (version.substring(0, 3).matches(VER51X)
                || version.substring(0, 3).matches(VER52X)) {

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            radarId = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);
            radarFullName = rb.getRAINBOWMetadataElement(nodeList, "name",
                    verbose);

        } else if (version.substring(0, 3).matches(VER53X)) {

            nodeList = rb
                    .getRAINBOWNodesByName(inputDoc, "sensorinfo", verbose);
            radarId = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);
            radarFullName = rb.getRAINBOWMetadataElement(nodeList, "name",
                    verbose);

        } else {
            System.out.println("version of the volume not supported");
            return null;
        }

        String source = radarId;
        
        String radarName = "";

        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarId();
                filePrefix = options[i].getFileName();
                originator = options[i].getOriginator();
                product_id = options[i].getProductId();
                rIndex = i;
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
            source = radarName;
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
    protected HashMap<String, String> makeHowGroup(Document inputDoc) {

        HashMap<String, String> how = new HashMap<String, String>();

        NodeList nodeList = null;

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        String beamwidth = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        String wavelength = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        
        try {
            wavelength = Double.toString((Double.parseDouble(wavelength) * 100));
        } catch (NumberFormatException e) {
            
        }
        
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String task = rb.getRAINBOWMetadataElement(nodeList, "name", verbose);

        how.put(PVOL_H5.STARTEPOCHS,
                rb.convertRAINBOWDate2Epoch(date, time, verbose));
        how.put(PVOL_H5.ENDEPOCHS,
                rb.convertRAINBOWDate2Epoch(date, time, verbose));
        how.put(PVOL_H5.SYSTEM, rb.RAINBOW_SYSTEM);
        how.put(PVOL_H5.SOFTWARE, rb.RAINBOW_SOFTWARE);
        how.put(PVOL_H5.TASK, task);
        
        how.put(PVOL_H5.SW_VERSION, version);
        how.put(PVOL_H5.BEAMWIDTH, beamwidth);
        how.put(PVOL_H5.WAVELENGTH, wavelength);
        how.put(PVOL_H5.RADOMELOSS, options[rIndex].getRadomeloss());
        how.put(PVOL_H5.ANTGAIN, options[rIndex].getAntgain());
        
        return how;

    }

    /**
     * Quality index
     * 
     * @param inputDoc
     * @return
     */
    protected HashMap<String, String> makeQIGroup(Document inputDoc) {

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

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QCOn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPIKE_QCOn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPIKE_QIOn,
                verbose);
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

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QCOn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.RSPEC_QCOn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.RSPEC_QIOn,
                verbose);
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

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QCOn,
                verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SPEC_QCOn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.SPEC_QIOn,
                verbose);
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

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QCOn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QIOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QIOn, value);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_Max, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_Max, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.PBB_QIUn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.PBB_QIUn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.ATT_QCOn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.ATT_QIOn, verbose);
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

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QCOn, verbose);
        value = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        qi.put(PVOL_H5.SUM_QCOn, value);

        nodeList = rb
                .getRAINBOWNodesByName(inputDoc, PVOL_H5.SUM_QIOn, verbose);
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
    protected String getZeroOneValue(String value) {

        if (value.matches("On"))
            return "1";

        return "0";
    }

    /**
     * @param inputDoc
     * @return
     */
    protected HashMap<String, String> makeWhereGroup(Document inputDoc) {

        HashMap<String, String> where = new HashMap<String, String>();

        NodeList nodeList = null;
        String lon = "";
        String lat = "";
        String height = "";

        if (version.substring(0, 3).matches(rb.VER51X)
                || version.substring(0, 3).matches(rb.VER52X)) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            lon = (rb.getRAINBOWMetadataElement(nodeList, "lon", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            lat = (rb.getRAINBOWMetadataElement(nodeList, "lat", verbose));

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
            height = (rb.getRAINBOWMetadataElement(nodeList, "alt", verbose));
        } else if (version.substring(0, 3).matches(rb.VER53X)) {
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
    protected PVOLSlicesCont[] makeSlices(NodeList sliceList) {

        PVOLSlicesCont slices[] = new PVOLSlicesCont[size];

        nray_org = new String[size];
        for (int i = 0; i < size; i++) {

            slices[i] = new PVOLSlicesCont();

            int raysBlobNumber = Integer.parseInt(rb.getValueByName(
                    sliceList.item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(rb.getValueByName(
                    sliceList.item(i), "rawdata", "blobid"));
            String qi = rb
                    .getValueByName(sliceList.item(i), "QIdata", "blobid");
            int qiBlobNumber = -1;
            if (qi != null && !qi.isEmpty())
                qiBlobNumber = Integer.parseInt(qi);

            // =============== what group =================================
            
            
            try {
                antSpeed = Double.parseDouble(rb.getValueByName(sliceList.item(i), "antspeed",
                        null));
            } catch (NumberFormatException e) {
                System.out.println("<antspeed> is missing or is not a number");
                // return;
            } catch (NullPointerException e) {
                
            }
            
            shift = (int) (360.0 / antSpeed);
            String date = (rb.parseRAINBOWDate(
                    rb.getValueByName(sliceList.item(i), "slicedata", "date"),
                    verbose));
            String time = (rb.parseRAINBOWTime(
                    rb.getValueByName(sliceList.item(i), "slicedata", "time"),
                    verbose));

            slices[i].dsWhat.put(PVOL_H5.PRODUCT, PRODUCT);
            slices[i].dsWhat.put(PVOL_H5.STARTDATE, date);
            slices[i].dsWhat.put(PVOL_H5.STARTTIME, time);
            slices[i].dsWhat.put(PVOL_H5.ENDDATE, date);
            slices[i].dsWhat.put(PVOL_H5.ENDTIME,
                    rb.parseRAINBOWTime(time, shift, verbose));

            // =============== where group ================================
            String posangle = rb.getValueByName(sliceList.item(i), "posangle",
                    null);
            String bins = rb.getValueByName(sliceList.item(i), "rawdata",
                    "bins");
            String srange = rb.getValueByName(sliceList.item(i), "start_range",
                    null);

            // =============== how group ================================
            double rpm = antSpeed * 60 / 360;
            slices[i].dsHow.put(PVOL_H5.RPM, String.valueOf(rpm));
            
            // ======= ATTRIBUTES FOR ODIM H5 2.1 
            // ------- Radar Options
            slices[i].dsHow.put(PVOL_H5.SIMULATED, options[rIndex].getSimulated());
            slices[i].dsHow.put(PVOL_H5.PULSEWIDTH, options[rIndex].getPulsewidth());
            slices[i].dsHow.put(PVOL_H5.RXBANDWIDTH, options[rIndex].getRXbandwidth());
            slices[i].dsHow.put(PVOL_H5.TXLOSS, options[rIndex].getTXloss());
            slices[i].dsHow.put(PVOL_H5.RXLOSS, options[rIndex].getRXloss());
            slices[i].dsHow.put(PVOL_H5.GASATTN, options[rIndex].getGasattn());
            slices[i].dsHow.put(PVOL_H5.RADCONSTH, options[rIndex].getRadconstH());
            slices[i].dsHow.put(PVOL_H5.RADCONSTV, options[rIndex].getRadconstV());
            slices[i].dsHow.put(PVOL_H5.NOMTXPOWER, options[rIndex].getNomTXpower());
            slices[i].dsHow.put(PVOL_H5.TXPOWER, options[rIndex].getTXpower());
            slices[i].dsHow.put(PVOL_H5.VSAMPLES, options[rIndex].getVsamples());
            slices[i].dsHow.put(PVOL_H5.AZMETHOD, options[rIndex].getAzmethod());
            slices[i].dsHow.put(PVOL_H5.BINMETHOD, options[rIndex].getBinmethod());
            slices[i].dsHow.put(PVOL_H5.MALFUNC, options[rIndex].getMalfunc());
            slices[i].dsHow.put(PVOL_H5.NEZ, options[rIndex].getNEZ());
            slices[i].dsHow.put(PVOL_H5.RAC, options[rIndex].getRAC());
            slices[i].dsHow.put(PVOL_H5.PAC, options[rIndex].getPAC());
            slices[i].dsHow.put(PVOL_H5.S2N, options[rIndex].getS2N());
            double radconstH = 0, radconstV = 0;
            try {
                radconstH = Double.parseDouble(options[rIndex].getRadconstH());
            } catch (Exception e) {
                radconstH = 0;
            }
            try {
                radconstV = Double.parseDouble(options[rIndex].getRadconstH());
            } catch (Exception e) {
                radconstV = 0;
            }
            //-------- RAINBOW XML
            String value = rb.getValueByName(sliceList.item(i), "highprf", null);
            if (value != null)
                highprf = value;
            slices[i].dsHow.put(PVOL_H5.HIGHPRF, highprf);
            value = rb.getValueByName(sliceList.item(i), "lowprf", null);
            if (value != null)
                lowprf = value;
            if (lowprf.matches("0"))
                lowprf = highprf;
            slices[i].dsHow.put(PVOL_H5.LOWPRF, lowprf);
            String ni = rb.getValueByName(sliceList.item(i), "dynv", "max");
            slices[i].dsHow.put(PVOL_H5.NI, ni);
            
            String vsampl = rb.getValueByName(sliceList.item(i), "timesamp", null);
            slices[i].dsHow.put(PVOL_H5.VSAMPLES, vsampl);
            
            value = rb.getValueByName(sliceList.item(i), "csr", null);
            if (value != null)
                csr = value;
            slices[i].dsHow.put(PVOL_H5.CSR, csr);
            
            value = rb.getValueByName(sliceList.item(i), "sqi", null);
            if (value != null)
                sqi = value;
            slices[i].dsHow.put(PVOL_H5.SQI, sqi);
            
            value = rb.getValueByName(sliceList.item(i), "log", null);
            if (value != null)
                log = value;
            slices[i].dsHow.put(PVOL_H5.LOG, log);
            
            if (radconstH == 0) {
                try {
                    double wavelength = Double.parseDouble(howG
                            .get(PVOL_H5.WAVELENGTH));
                    double nompower = Double.parseDouble(options[rIndex]
                            .getNomTXpower());
                    double beamwidth = Double.parseDouble(howG
                            .get(PVOL_H5.BEAMWIDTH));
                    double pulselength = Double.parseDouble(options[rIndex]
                            .getPulsewidth());
                    double antgain = Double.parseDouble(options[rIndex]
                            .getAntgain());
                    double radomeloss = Double.parseDouble(options[rIndex]
                            .getRadomeloss());
                    double txloss = Double.parseDouble(options[rIndex]
                            .getTXloss());
                    double rxloss = Double.parseDouble(options[rIndex]
                            .getRXloss());
                    radconstH = radarConst(wavelength, nompower, beamwidth,
                            pulselength, antgain, radomeloss, txloss, rxloss);
                } catch (Exception e) {
                    
                }
            }
            if (radconstH != 0)
                slices[i].dsHow.put(PVOL_H5.RADCONSTH,
                        String.valueOf(radconstH));

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

            if (min != null && max != null) {

                gain = rb.getRAINBOWGain(min, max, Integer.parseInt(dataDepth));
                slices[i].dsdWhat.put(PVOL_H5.QI_GAIN, gain);
                slices[i].dsdWhat.put(PVOL_H5.QI_OFFSET,
                        rb.getRAINBOWOffset(min, gain));
                // System.out.println("min=" + min + " max=" + max + " depth=" +
                // dataDepth + " gain=" + gain);
            }

            if (srange == null) // default value is "0"
                srange = "0";

            nray_org[i] = rb.getValueByName(sliceList.item(i), "rawdata",
                    "rays");

            DataBufferContainer dbcRays = blobs.get(raysBlobNumber);
            byte[] infRaysBuff = null;
            if (dbcRays.getCompression() == 1) {
                infRaysBuff = rb.inflate1DRAINBOWDataSection(dbcRays, verbose);
            } else {
                infRaysBuff = dbcRays.getDataBuffer();
            }
            
            String rays = "";
            if (!nray_new.isEmpty())
                rays = nray_new;
            else
                rays = nray_org[i];
            
            int[] angles = new int[Integer.parseInt(nray_org[i])];
            angles = getAngles(infRaysBuff, Integer.parseInt(rays));
            int firstAngle = startingAzimuthNumber(angles);
            String a1gate = String.valueOf(firstAngle);
            double[] anglesD = sortAngles(angles, firstAngle);
            slices[i].setAngles(anglesD);
            

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
            } else if (dataType.matches(rb.V)) {
                dataType = "VRAD";
            }

            dataDepth = rb
                    .getValueByName(sliceList.item(i), "rawdata", "depth");
            min = rb.getValueByName(sliceList.item(i), "rawdata", "min");
            max = rb.getValueByName(sliceList.item(i), "rawdata", "max");
            gain = rb.getRAINBOWGain(min, max, Integer.parseInt(dataDepth));

            slices[i].dsdWhat.put(PVOL_H5.QUANTITY, dataType);
            slices[i].dsdWhat.put(PVOL_H5.GAIN, gain);
            slices[i].dsdWhat.put(PVOL_H5.OFFSET,
                    rb.getRAINBOWOffset(min, gain));

            // =============== data dataset ================================
            DataBufferContainer dbcData = blobs.get(dataBlobNumber);
            DataBufferContainer dbcQi = null;
            if (qiBlobNumber > -1)
                dbcQi = blobs.get(qiBlobNumber);
            slices[i].dsdData.put(PVOL_H5.DATA_SIZE, dataDepth);
            slices[i].setDataBuffContainer(dbcData);
            if (dbcQi != null)
                slices[i].setQiBuff(dbcQi);

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

    public int startingAzimuthNumber(int[] data) {

        int value = 0, minValue = 99999, counter = 0;
        int length = data.length;

        for (int i = 0; i < length; i++) {

            value = data[i];

            if (value < minValue) {
                minValue = value;
                counter = i;
            }
        }

        return counter;
    }
    
    public int[] getAngles(byte[] data, int length) {
        
        int angles[] = new int[length];
        for (int i = 0; i < length; i++) {
            angles[i] = byte2int(data[i * 2], data[i * 2 + 1]);
        }
        return angles;
    }
    
    public double[] sortAngles(int[] data, int first) {
        
        double gain = 0.00549; 

        double angles[] = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            angles[i] = data[(first + i)%data.length] * gain;
        }
        return angles;
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

    /**
     * Radar's ID
     * @return
     */
    public String getRadarID() {
        return radarId;
    }

    /**
     * Radar's name
     * @return
     */
    public String getRadarName() {
        return radarFullName;
    }
    
    protected String getFileName(String productId, String originator, String date) {
        return PFLAG + "_" + productId + "_" + OFLAG + "_" + originator + "_" + date + ".h5";
    
    }
    protected double radarConst(double wavelength, double nompower,
            double beamwidth, double pulselength, double antgain,
            double radomeloss, double txloss, double rxloss) {
        
        if (wavelength * nompower * beamwidth * pulselength * antgain
                * radomeloss * txloss * rxloss == 0)
            return 0;
        
        double c = 3.0E8;
        double K = 0.93;
        
        double numerator = 2.025 * Math.pow(2, 14) * Math.log(2) * 100
                * wavelength * wavelength;
        double nominative = Math.pow(Math.PI, 5) * 10.0E-23 * c * nompower
                * beamwidth * beamwidth * pulselength * K;

        double fraction = numerator/nominative;
        
        double log10 = 10*(Math.log(fraction)/Math.log(10));
        
        return log10 - 2 * antgain + radomeloss + txloss + rxloss;
    }
    
}
