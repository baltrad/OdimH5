/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import java.io.File;

import ncsa.hdf.object.h5.H5File;

import org.junit.Test;
import org.w3c.dom.Document;

import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;
import static org.junit.Assert.*;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class HDF5ToRainbow5ConverterTest {

    @Test
    public void convertRainbowVolumetest() {

        String fileName = "test-data/BOR/be17d2fa-a285-4c0b-88db-9639e0be91b6.h5";
        boolean verbose = true;

        String fileNameOut = "";

        HDF5Model model = new HDF5Model();
        model.setMessageLogger(new MessageLogger());

        byte[] fileBuff = model.readDataFile(fileName, verbose);

        RainbowModel rainbow = new RainbowModel();
        rainbow.setMessageLogger(model.getMessageLogger());
        rainbow.setHDFModel(model);

        HDF2RainbowPVOL hdf = new HDF2RainbowPVOL(fileNameOut, fileName, true,
                rainbow);

        File h5 = new File(hdf.getOutputFileName());

        assertTrue(h5.exists());

    }

}
