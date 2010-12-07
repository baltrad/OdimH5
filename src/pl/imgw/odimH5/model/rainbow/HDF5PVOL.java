/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;
import pl.imgw.odimH5.model.HDF5Model;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class HDF5PVOL {
    
    HDF5Model hdf;
    
    public HDF5PVOL(String outputFileName, String inputFileName, boolean verbose,
            RainbowModel rb) {
        
        hdf = rb.getHDFModel();
        
        H5File inputFile = hdf.openHDF5File(inputFileName);
        
        Group root = hdf.getHDF5RootGroup(inputFile, verbose);
        
        
        
        
    }

}
