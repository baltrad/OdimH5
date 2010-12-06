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

    private DataBufferContainer dataBuff = null;
    
    public HashMap<String, String> dsWhat = new HashMap<String, String>();
    public HashMap<String, String> dsWhere = new HashMap<String, String>();
    public HashMap<String, String> dsdWhat = new HashMap<String, String>();
    public HashMap<String, String> dsdData = new HashMap<String, String>();
   
    public DataBufferContainer getDataBuff() {
        return dataBuff;
    }
    public void setDataBuff(DataBufferContainer dataBuff) {
        this.dataBuff = dataBuff;
    }
       
    
}
