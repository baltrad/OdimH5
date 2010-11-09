/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;


/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ParametersContainer {

    private String date = null;
    private String time = null;
    private String lon = null;
    private String lat = null;
    private String alt = null;
    // String place;
    private String source = null;
    private String type = null;
    // String lon0;
    // String lat0;
    // String ellps;
    private String projection = null;
    private String xsize = null;
    private String ysize = null;
    private String ulLon = null;
    private String ulLat = null;
    private String lrLon = null;
    private String lrLat = null;
    private String range = null;
    private String angles = null;
    private String az_angle = null;
    // String depth;
    private String dataDepth = null;
    private int flagDepth = 0;
    private String task = null;
    private String startepochs = null;
    private String endepochs = null;
    private String sw_version = null;
    private String beamwidth = null;
    private String wavelength = null;
    private String quantity = null;
    private String product = null;
    private String pacMethod = null;
    private String pacNumProd = null;
    private String prodpar = null;
    private String xscale = null;
    private String yscale = null;

    private String gain = null;
    private String offset = null;
    private String dataFileName = null;
    private SliceContainer[] slices = null;

    
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getXsize() {
        return xsize;
    }

    public void setXsize(String xsize) {
        this.xsize = xsize;
    }

    public String getYsize() {
        return ysize;
    }

    public void setYsize(String ysize) {
        this.ysize = ysize;
    }

    public String getUlLon() {
        return ulLon;
    }

    public void setUlLon(String ulLon) {
        this.ulLon = ulLon;
    }

    public String getUlLat() {
        return ulLat;
    }

    public void setUlLat(String ulLat) {
        this.ulLat = ulLat;
    }

    public String getLrLon() {
        return lrLon;
    }

    public void setLrLon(String lrLon) {
        this.lrLon = lrLon;
    }

    public String getLrLat() {
        return lrLat;
    }

    public void setLrLat(String lrLat) {
        this.lrLat = lrLat;
    }


    public String getAngles() {
        return angles;
    }

    public void setAngles(String angles) {
        this.angles = angles;
    }

    public String getAzAngle() {
        return az_angle;
    }

    public void setAzAngle(String azAngle) {
        this.az_angle = azAngle;
    }

    public String getDataDepth() {
        return dataDepth;
    }

    public void setDataDepth(String dataDepth) {
        this.dataDepth = dataDepth;
    }

    public int getFlagDepth() {
        return flagDepth;
    }

    public void setFlagDepth(int flagDepth) {
        this.flagDepth = flagDepth;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getStartepochs() {
        return startepochs;
    }

    public void setStartepochs(String startepochs) {
        this.startepochs = startepochs;
    }

    public String getEndepochs() {
        return endepochs;
    }

    public void setEndepochs(String endepochs) {
        this.endepochs = endepochs;
    }

    public String getSwVersion() {
        return sw_version;
    }

    public void setSwVersion(String sw_version) {
        this.sw_version = sw_version;
    }

    public String getBeamwidth() {
        return beamwidth;
    }

    public void setBeamwidth(String beamwidth) {
        this.beamwidth = beamwidth;
    }

    public String getWavelength() {
        return wavelength;
    }

    public void setWavelength(String wavelength) {
        this.wavelength = wavelength;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPacMethod() {
        return pacMethod;
    }

    public void setPacMethod(String pacMethod) {
        this.pacMethod = pacMethod;
    }

    public String getPacNumProd() {
        return pacNumProd;
    }

    public void setPacNumProd(String pacNumProd) {
        this.pacNumProd = pacNumProd;
    }

    public String getProdpar() {
        return prodpar;
    }

    public void setProdpar(String prodpar) {
        this.prodpar = prodpar;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getXscale() {
        return xscale;
    }

    public void setXscale(String xscale) {
        this.xscale = xscale;
    }

    public String getYscale() {
        return yscale;
    }

    public void setYscale(String yscale) {
        this.yscale = yscale;
    }


    public String getGain() {
        return gain;
    }

    public void setGain(String gain) {
        this.gain = gain;
    }
    
    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public SliceContainer[] getSlices() {
        return slices;
    }

    public void setSlices(SliceContainer[] slices) {
        this.slices = slices;
    }

}
