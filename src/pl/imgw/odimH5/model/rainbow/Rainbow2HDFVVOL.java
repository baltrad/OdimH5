/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER51X;
import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER52X;
import static pl.imgw.odimH5.model.rainbow.RainbowModel.VER53X;

import java.io.File;
import java.util.HashMap;

import ncsa.hdf.hdf5lib.HDF5Constants;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.AplicationConstans;
import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.PVOL_H5;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class Rainbow2HDFVVOL  extends Rainbow2HDF{

    private static final String PRODUCT_ID = "PAHZ";
    
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

    public Rainbow2HDFVVOL(String outputFileName, byte[] fileBuff,
            boolean verbose, RainbowModel rb)
            {

        super(fileBuff, verbose, rb);

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
            setOutputFileName();
        } else
            this.outputFileName = outputFileName;

        correct = true;

    }    
    
    public Rainbow2HDFVVOL(String outputFileName, byte[] fileBuff,
            boolean verbose, RainbowModel rb, boolean tmp) {
        this(outputFileName, fileBuff, verbose, rb);
        if (tmp) {

            this.outputFileName = new File(AplicationConstans.TMP,
                    this.outputFileName).getPath();

        }
    }
    
    /**
     * @param outputFileName
     */
    protected void setOutputFileName() {

        String date = whatG.get(PVOL_H5.DATE) + whatG.get(PVOL_H5.TIME);
        if (originator != null && !originator.isEmpty()) {
            this.outputFileName = getFileName(PRODUCT_ID + product_id,
                    originator, date);
        } else if (filePrefix != null && !filePrefix.isEmpty())
            this.outputFileName = filePrefix + date + ".h5";

    }
    
}
