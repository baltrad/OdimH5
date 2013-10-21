/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import java.io.File;
import java.util.Iterator;
import java.util.List;


import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;

import org.junit.Test;
import org.w3c.dom.Document;

import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
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
public class ReadingOdimH5Test {

    @Test
    public void convertHDF2Rb5() {
        String fileName = "test-data/T_PAGZ51_C_LZIB_20130313075500.hdf";
        boolean verbose = true;
        
        String fileNameOut = "test-data/out/2013031307550000dBZ.vol";
        
        HDF5Model model = new HDF5Model();
        model.setMessageLogger(new MessageLogger());
        
        
        RainbowModel rainbow = new RainbowModel();
        rainbow.setMessageLogger(model.getMessageLogger());
        rainbow.setHDFModel(model);
        
        HDF2RainbowPVOL hdf = new HDF2RainbowPVOL(fileNameOut,
                fileName, verbose, rainbow);
        
        File output = new File(hdf.getOutputFileName());
        
        System.out.println(output);
        
        assertTrue(output.exists());
//        output.delete();
    }
    
}
