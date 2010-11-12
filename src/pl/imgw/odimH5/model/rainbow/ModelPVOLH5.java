/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import ncsa.hdf.hdf5lib.HDF5Constants;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.OptionContainer;

/**
 * 
 * It contains helper methods for creating HDF files for PVOL objects
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelPVOLH5 {

    /**
     * 
     * Helper method for creating HDF5 file, it collects data from RAINBOW
     * volume file. It is created for PVOL objects defined by ODIM_H5
     * specification.
     * 
     * @param fileName
     *            Output name of HDF5 file
     * @param fileBuff
     *            Input file data in a byte array
     * @param verbose
     *            verbose mode
     * @param rb
     *            Rainbow model class instance
     * @param proc
     *            Data processor model class instance
     * @return name of new HDF5 file
     */
    public static String createDescriptor(String fileName, byte[] fileBuff,
            boolean verbose, Model rb, DataProcessorModel proc, OptionContainer[] options) {

        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.VOLUME, verbose);

        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        String fileNameH5 = null;

        NodeList nodeList = null;

        // =========== what group Element ========================
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "scan", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        String radarName = "";
        for(int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())){
                radarName = options[i].getRadarWMOName();
                break;
            }
        }

        if(radarName.isEmpty()) {
            System.out.println("Add " + source + " to options.xml");
            System.exit(0);
        } else {
            source = "WMO:" + radarName;
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
            sliceDate[i] = rb.parseRAINBOWDate(Model.getValueByName(sliceList
                    .item(i), "slicedata", "date"), verbose);
            sliceTime[i] = rb.parseRAINBOWTime(Model.getValueByName(sliceList
                    .item(i), "slicedata", "time"), verbose);

            // ============= datasetn specific where group ============

            pangle[i] = Model.getValueByName(sliceList.item(i), "posangle",
                    null);
            bins[i] = Model
                    .getValueByName(sliceList.item(i), "rawdata", "bins");
            srange[i] = Model.getValueByName(sliceList.item(i), "start_range",
                    null);
            if (srange[i] == null) // default value is "0"
                srange[i] = "0";

            rstep[i] = Model.getValueByName(sliceList.item(i), "rangestep",
                    null);

            rays[i] = Model
                    .getValueByName(sliceList.item(i), "rawdata", "rays");

            // ===============================================================

            int raysBlobNumber = Integer.parseInt(Model.getValueByName(
                    sliceList.item(i), "rayinfo", "blobid"));
            int dataBlobNumber = Integer.parseInt(Model.getValueByName(
                    sliceList.item(i), "rawdata", "blobid"));
            int raysDepth = Integer.parseInt(Model.getValueByName(sliceList
                    .item(i), "rayinfo", "depth"));
            int firstBlob = rb.getMin(raysBlobNumber, dataBlobNumber);
            dataDepth[i] = Integer.parseInt(Model.getValueByName(sliceList
                    .item(i), "rawdata", "depth"));
            dataBuff[i] = rb.getRainbowDataSection(fileBuff, dataBlobNumber,
                    firstBlob, verbose);
            DataBufferContainer raysBuff = rb.getRainbowDataSection(fileBuff,
                    raysBlobNumber, firstBlob, verbose);
            byte[] infRaysBuff = rb.inflate1DRAINBOWDataSection(raysBuff
                    .getDataBuffer(), raysBuff.getDataBufferLength(), verbose);

            // =================================================================

            a1gate[i] = String.valueOf(ModelPVOL.startingAzimuthNumber(
                    infRaysBuff, Integer.parseInt(rays[i])));

            // ============= datasetn specific data group ==================

            // data specific where group

            nodeList = rb.getRAINBOWNodesByName(inputDoc, "volume", verbose);
            datatype[i] = Model.getValueByName(sliceList.item(i), "rawdata",
                    "type");

            if (datatype[i].matches(rb.DBZ))
                datatype[i] = "TH";
            else if (datatype[i].matches(rb.UPHIDP))
                datatype[i] = "PHIDP";

            min[i] = Model.getValueByName(sliceList.item(i), "dynz", "min");
            String max = Model.getValueByName(sliceList.item(i), "dynz", "max");
            gain[i] = rb.getRAINBOWGain(min[i], max, dataDepth[i]);

        }
        // HDF5 file identifier
        int file_id = -1;
        int child_group_id = -1;
        // HDF5 file operation status

        fileNameH5 = fileName.substring(0, fileName.indexOf(".")) + ".h5";

        file_id = proc.H5Fcreate_wrap(fileNameH5, HDF5Constants.H5F_ACC_TRUNC,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/what", 0, verbose);

        proc.H5Acreate_any_wrap(child_group_id, "object", rb.H5_STRING, "PVOL",
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "version", rb.H5_STRING,
                "H5rad 2.0", verbose);
        proc.H5Acreate_any_wrap(child_group_id, "date", rb.H5_STRING, date,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "time", rb.H5_STRING, time,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "source", rb.H5_STRING, source,
                verbose);

        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lon", rb.H5_DOUBLE, lon,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lat", rb.H5_DOUBLE, lat,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "height", rb.H5_DOUBLE, alt,
                verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "startepochs", rb.H5_LONG,
                startepochs, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "endepochs", rb.H5_LONG,
                endepochs, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "system", rb.H5_STRING,
                rb.RAINBOW_SYSTEM, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "software", rb.H5_STRING,
                rb.RAINBOW_SOFTWARE, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "sw_version", rb.H5_STRING,
                version, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "beamwidth", rb.H5_DOUBLE,
                beamwidth, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "wavelength", rb.H5_DOUBLE,
                wavelength, verbose);
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
                    rb.H5_STRING, sliceDate[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "starttime",
                    rb.H5_STRING, sliceTime[i], verbose);

            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "where",
                    0, verbose);

            proc.H5Acreate_any_wrap(grandchild_group_id, "elangle",
                    rb.H5_DOUBLE, pangle[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "nbins", rb.H5_LONG,
                    bins[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "rstart",
                    rb.H5_DOUBLE, srange[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "rscale",
                    rb.H5_DOUBLE, rstep[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "nrays", rb.H5_LONG,
                    rays[i], verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "a1gate", rb.H5_LONG,
                    a1gate[i], verbose);
            proc.H5Gclose_wrap(grandchild_group_id, verbose);

            grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "data1",
                    0, verbose);

            int grandgrandchild_group_id = -1;

            grandgrandchild_group_id = proc.H5Gcreate_wrap(grandchild_group_id,
                    "what", 0, verbose);

            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "quantity",
                    rb.H5_STRING, datatype[i], verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "gain",
                    rb.H5_DOUBLE, gain[i], verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "offset",
                    rb.H5_DOUBLE, min[i], verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "nodata",
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_NO_DATA), verbose);
            proc.H5Acreate_any_wrap(grandgrandchild_group_id, "undetect",
                    rb.H5_DOUBLE, String.valueOf(rb.RAINBOW_UNDETECT), verbose);

            proc.H5Gclose_wrap(grandgrandchild_group_id, verbose);

            int dim_x = Integer.parseInt(rays[i]);
            int dim_y = Integer.parseInt(bins[i]);

            int dataspace_id = proc.H5Screate_simple_wrap(2, dim_x, dim_y,
                    null, verbose);

            grandgrandchild_group_id = proc.H5Dcreate_wrap(grandchild_group_id,
                    "data", HDF5Constants.H5T_STD_U16BE, dataspace_id, Integer
                            .parseInt(rb.H5_DATA_CHUNK), Integer
                            .parseInt(rb.H5_GZIP_LEVEL), verbose);

            int width = Integer.parseInt(rays[i]);
            int height = Integer.parseInt(bins[i]);
            int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(dataBuff[i]
                    .getDataBuffer(), width, height, verbose);

            infDataBuff = proc.transposeArray(infDataBuff, dim_x, dim_y);

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

        return fileNameH5;

    }

}
