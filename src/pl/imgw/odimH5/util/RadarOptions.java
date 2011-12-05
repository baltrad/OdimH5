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
    
    private String simulated = "False";
    private String pulsewidth = "";
    private String RXbandwidth = "";
    private String TXloss = "";
    private String RXloss = "";
    private String radomeloss = "";
    private String antgain = "";
    private String gasattn = "";
    private String radconstH = "";
    private String radconstV = "";
    private String nomTXpower = "";
    private String TXpower = "";
    private String Vsamples = "";
    private String azmethod = "";
    private String binmethod = "";
    private String malfunc = "False";
    private String NEZ = "";
    private String RAC = "";
    private String PAC = "";
    private String S2N = "";
    


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
     * @return the simulated
     */
    public String getSimulated() {
        return simulated;
    }

    /**
     * @param simulated the simulated to set
     */
    public void setSimulated(String simulated) {
        if (simulated != null)
            this.simulated = simulated;
    }

    /**
     * @return the pulsewidth
     */
    public String getPulsewidth() {
        return pulsewidth;
    }

    /**
     * @param pulsewidth the pulsewidth to set
     */
    public void setPulsewidth(String pulsewidth) {
        this.pulsewidth = pulsewidth;
    }

    /**
     * @return the rXbandwidth
     */
    public String getRXbandwidth() {
        return RXbandwidth;
    }

    /**
     * @param rXbandwidth the rXbandwidth to set
     */
    public void setRXbandwidth(String rXbandwidth) {
        RXbandwidth = rXbandwidth;
    }

    /**
     * @return the tXloss
     */
    public String getTXloss() {
        return TXloss;
    }

    /**
     * @param tXloss the tXloss to set
     */
    public void setTXloss(String tXloss) {
        TXloss = tXloss;
    }

    /**
     * @return the rXloss
     */
    public String getRXloss() {
        return RXloss;
    }

    /**
     * @param rXloss the rXloss to set
     */
    public void setRXloss(String rXloss) {
        RXloss = rXloss;
    }

    /**
     * @return the radomeloss
     */
    public String getRadomeloss() {
        return radomeloss;
    }

    /**
     * @param radomeloss the radomeloss to set
     */
    public void setRadomeloss(String radomeloss) {
        this.radomeloss = radomeloss;
    }

    /**
     * @return the antgain
     */
    public String getAntgain() {
        return antgain;
    }

    /**
     * @param antgain the antgain to set
     */
    public void setAntgain(String antgain) {
        this.antgain = antgain;
    }

    /**
     * @return the gasattn
     */
    public String getGasattn() {
        return gasattn;
    }

    /**
     * @param gasattn the gasattn to set
     */
    public void setGasattn(String gasattn) {
        this.gasattn = gasattn;
    }

    /**
     * @return the radconstH
     */
    public String getRadconstH() {
        return radconstH;
    }

    /**
     * @param radconstH the radconstH to set
     */
    public void setRadconstH(String radconstH) {
        this.radconstH = radconstH;
    }

    /**
     * @return the radconstV
     */
    public String getRadconstV() {
        return radconstV;
    }

    /**
     * @param radconstV the radconstV to set
     */
    public void setRadconstV(String radconstV) {
        this.radconstV = radconstV;
    }

    /**
     * @return the nomTXpower
     */
    public String getNomTXpower() {
        return nomTXpower;
    }

    /**
     * @param nomTXpower the nomTXpower to set
     */
    public void setNomTXpower(String nomTXpower) {
        this.nomTXpower = nomTXpower;
    }

    /**
     * @return the tXpower
     */
    public String getTXpower() {
        return TXpower;
    }

    /**
     * @param tXpower the tXpower to set
     */
    public void setTXpower(String tXpower) {
        TXpower = tXpower;
    }

    /**
     * @return the vsamples
     */
    public String getVsamples() {
        return Vsamples;
    }

    /**
     * @param vsamples the vsamples to set
     */
    public void setVsamples(String vsamples) {
        Vsamples = vsamples;
    }

    /**
     * @return the azmethod
     */
    public String getAzmethod() {
        return azmethod;
    }

    /**
     * @param azmethod the azmethod to set
     */
    public void setAzmethod(String azmethod) {
        this.azmethod = azmethod;
    }

    /**
     * @return the binmethod
     */
    public String getBinmethod() {
        return binmethod;
    }

    /**
     * @param binmethod the binmethod to set
     */
    public void setBinmethod(String binmethod) {
        this.binmethod = binmethod;
    }

    /**
     * @return the malfunc
     */
    public String getMalfunc() {
        return malfunc;
    }

    /**
     * @param malfunc the malfunc to set
     */
    public void setMalfunc(String malfunc) {
        if (malfunc != null)
            this.malfunc = malfunc;
    }

    /**
     * @return the nEZ
     */
    public String getNEZ() {
        return NEZ;
    }

    /**
     * @param nEZ the nEZ to set
     */
    public void setNEZ(String nEZ) {
        NEZ = nEZ;
    }

    /**
     * @return the rAC
     */
    public String getRAC() {
        return RAC;
    }

    /**
     * @param rAC the rAC to set
     */
    public void setRAC(String rAC) {
        RAC = rAC;
    }

    /**
     * @return the pAC
     */
    public String getPAC() {
        return PAC;
    }

    /**
     * @param pAC the pAC to set
     */
    public void setPAC(String pAC) {
        PAC = pAC;
    }

    /**
     * @return the s2N
     */
    public String getS2N() {
        return S2N;
    }

    /**
     * @param s2n the s2N to set
     */
    public void setS2N(String s2n) {
        S2N = s2n;
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
