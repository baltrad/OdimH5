/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ConvertingTool {


    private FTPHandler ftp;
    private RainbowModel rb;
    private HashMap<String, FTPHandler> ftps = new HashMap<String, FTPHandler>();
    private boolean verbose;

    public ConvertingTool(RainbowModel rb, boolean verbose) {
        
        
        this.rb = rb;
        this.verbose = verbose;
        ftp = new FTPHandler();

    }

    public boolean convertRb5ToHdf5(File file) {

        int file_len = (int) file.length();
        byte[] file_buf = new byte[file_len];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(file_buf, 0, file_len);
            fis.close();

        } catch (IOException e) {
            return false;
        }

        Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL("", file_buf, verbose, rb, true);
        vol.makeH5();
        String radarID = vol.getRadarID();
        String toBeSentFileName = vol.getOutputFileName();
        File toBeSentFile = new File(toBeSentFileName);
        String radarName = vol.getRadarName();
        
        ftp.sendFile(toBeSentFile, radarID);

        toBeSentFile.delete();
        
        return true;
    }

    public boolean convertHdf5ToVol(File file) {

        HDF2RainbowPVOL hdf = new HDF2RainbowPVOL("", file.getPath(), verbose,
                rb, true);
        
        String toBeSentFileName = hdf.getOutputFileName();
        File toBeSentFile = new File(toBeSentFileName);
        
        String radarName = hdf.getRadarName();
        
        ftp.sendFile(toBeSentFile, radarName);
        
        toBeSentFile.delete();
        
        return true;
    }

    private void feedBaltrad(File toBeSentFile, File originalFile) {
        
        BaltradOptions baltradOptions = OptionsHandler.getOpt().getBaltradOptions();
        
        if (toBeSentFile != null
                && (toBeSentFile.getName().endsWith("h5") || toBeSentFile
                        .getName().endsWith("hdf"))
                && !baltradOptions.isEmpty()) {

//            msgl.showMessage("Sending file " + toBeSentFileName + " to "
//                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, toBeSentFile);
            baltradFeeder.feedToBaltrad();
//            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }

        if (!baltradOptions.isEmpty()
                && (originalFile.getName().endsWith("h5") || originalFile
                        .getName().endsWith("hdf"))) {

//            msgl.showMessage("Sending file " + originalFile + " to "
//                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, originalFile);
            baltradFeeder.feedToBaltrad();
//            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }

        if (originalFile != null
                && (originalFile.getName().endsWith("h5") || originalFile
                        .getName().endsWith("hdf"))
                && !baltradOptions.isEmpty()) {

//            msgl.showMessage("Sending file " + originalFile + " to "
//                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, originalFile);
            baltradFeeder.feedToBaltrad();
//            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }
    }
    
}
