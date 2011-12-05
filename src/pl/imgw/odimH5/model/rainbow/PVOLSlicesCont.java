/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.util.HashMap;

import pl.imgw.odimH5.util.DataBufferContainer;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class PVOLSlicesCont {

    private DataBufferContainer dbc = null;
    private DataBufferContainer qiBuff = null;
    private double[] angles = null; 
    private int[][] datasetFromHdf = null;

    
    //HDF
    public HashMap<String, String> dsWhat = new HashMap<String, String>();
    public HashMap<String, String> dsWhere = new HashMap<String, String>();
    public HashMap<String, String> dsHow = new HashMap<String, String>();
    public HashMap<String, String> dsdWhat = new HashMap<String, String>();
    public HashMap<String, String> dsdData = new HashMap<String, String>();
    
    //Rainbow
    public HashMap<String, String> slice = new HashMap<String, String>();
    public HashMap<String, String> rayinfo = new HashMap<String, String>();
    public HashMap<String, String> rawdata = new HashMap<String, String>();
   
    public DataBufferContainer getDataBuffContainer() {
        return dbc;
    }
    
    public void setDataBuffContainer(DataBufferContainer dataBuff) {
        this.dbc = dataBuff;
    }
    
    public DataBufferContainer getQiBuff() {
        return qiBuff;
    }

    public void setQiBuff(DataBufferContainer qiBuff) {
        this.qiBuff = qiBuff;
    }

    public int[][] getDatasetFromHdf() {
        return datasetFromHdf;
    }
    
    public void setDatasetFromHdf(int[][] datasetFromHdf) {
        this.datasetFromHdf = datasetFromHdf;
    }

    /**
     * @return the angles
     */
    public double[] getAngles() {
        return angles;
    }

    /**
     * @param angles the angles to set
     */
    public void setAngles(double[] angles) {
        this.angles = angles;
    }
    
    
    
}
