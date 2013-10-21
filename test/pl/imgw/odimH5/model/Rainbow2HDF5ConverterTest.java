/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import java.io.File;

import ncsa.hdf.object.h5.H5File;

import org.junit.Test;
import org.w3c.dom.Document;

import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;
import static org.junit.Assert.*;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class Rainbow2HDF5ConverterTest {

    @Test
    public void convertRainbowVolumetest() {
        
        String fileName = "test-data/2012103117500800dBZ.vol";
        boolean verbose = true;
        
        String fileNameOut = "test-data/out/T_PAGZ42_C_SOWR_2012103117500800.h5";
        
        HDF5Model model = new HDF5Model();
        model.setMessageLogger(new MessageLogger());
        
        
        byte[] fileBuff = model.readDataFile(fileName, verbose);

        RainbowModel rainbow = new RainbowModel();
        rainbow.setMessageLogger(model.getMessageLogger());
        rainbow.setHDFModel(model);
        
        Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL(fileNameOut, fileBuff,
                    verbose, rainbow);
        
        vol.makeH5();
        
        H5File h5 = HDF5Model.openHDF5File(fileNameOut);
        
        assertTrue(h5.exists());
        
        new File(fileNameOut).delete();
        
        
    }
    
}
