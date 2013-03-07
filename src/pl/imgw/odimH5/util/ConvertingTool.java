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

    private RadarOptions[] radarOptions;
    private Map<String, List<FTPContainer>> ftpOptions;
    private FTPHandler ftp;
    private RainbowModel rb;
    private HashMap<String, FTPHandler> ftps = new HashMap<String, FTPHandler>();
    private boolean verbose;

    public ConvertingTool(RadarOptions[] radarOptions,
            Map<String, List<FTPContainer>> ftpOptions, RainbowModel rb,
            boolean verbose) {
        
        this.radarOptions = radarOptions;
        this.ftpOptions = ftpOptions;
        this.rb = rb;
        this.verbose = verbose;
        ftp = new FTPHandler(ftpOptions);

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

        Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL("", file_buf, verbose, rb,
                radarOptions);
        vol.makeH5();
        String radarID = vol.getRadarID();
        String toBeSentFileName = vol.getOutputFileName();
        File toBeSentFile = new File(toBeSentFileName);
        String radarName = vol.getRadarName();
        
        ftp.sendFile(toBeSentFile, radarID);

        return true;
    }

    public boolean convertHdf5ToVol(File file) {

        HDF2RainbowPVOL hdf = new HDF2RainbowPVOL("", file.getPath(), verbose,
                rb, radarOptions);
        
        String toBeSentFileName = hdf.getOutputFileName();
        File toBeSentFile = new File(toBeSentFileName);
        
        String radarName = hdf.getRadarName();
        
        ftp.sendFile(toBeSentFile, radarName);
        return true;
    }

}
