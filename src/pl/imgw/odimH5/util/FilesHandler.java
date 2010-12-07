/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;

import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

/**
 * 
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FilesHandler {

   
    /**
     * Method used to verify whether current file is valid HDF5 file
     * 
     * @param fileName
     *            Input file name
     * @return True if current file is HDF5 file
     */
    public static boolean verifyHDF5File(File file) {
        boolean res;
        try {
            FileFormat fileFormat = FileFormat
                    .getFileFormat(FileFormat.FILE_TYPE_HDF5);
            H5File inputFile = (H5File) fileFormat.createInstance(file
                    .getName(), FileFormat.READ);
            inputFile.open();
            res = true;
        } catch (Exception e) {
            System.out.println(file.getName() + " is not valid hdf5 file.");
            res = false;
        }
        return res;
    }

}
