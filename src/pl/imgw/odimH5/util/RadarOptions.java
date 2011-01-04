/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

/**
 * 
 * Contains set of options for Baltrad Feeder.
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class RadarOptions {

    private String location = "";
    private String radarName = "";
    private String radarWMOName = "";
    private String fileName = "";
    private String dir = "";
    private String nrays = "";
    private String format = "";


    public String getRadarName() {
        return radarName;
    }

    public void setRadarName(String radarName) {
        this.radarName = radarName;
    }

    public String getRadarWMOName() {
        return radarWMOName;
    }

    public void setRadarWMOName(String radarWMOName) {
        this.radarWMOName = radarWMOName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String remoteDir) {
        this.dir = remoteDir;
    }

    public String getNrays() {
        return nrays;
    }

    public void setNrays(String nrays) {
        this.nrays = nrays;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * Returns true if any of obligated fields is empty
     * 
     * @return
     */
    public boolean isEmpty() {
        if (radarName == null || radarName.isEmpty())
            return true;
        if (radarWMOName == null || radarWMOName.isEmpty())
            return true;
        if (dir == null || dir.isEmpty())
            return true;
        return false;
    }
}
