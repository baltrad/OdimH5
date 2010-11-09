/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import pl.imgw.odimH5.util.DataBufferContainer;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class SliceContainer {

    private String startTime = null;
    private String startDate = null;
    private String endTime = null;
    private String endDate = null;
    private String pangle = null;
    private String bins = null;
    private String srange = null;
    private String rstep = null;
    private String rays = null;
    private String a1gate = null;
    private String datatype = null;
    private String min = null;
    private String gain = null;
    private int dataDepth = 0;
    private DataBufferContainer dataBuff = null;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String sliceTime) {
        this.startTime = sliceTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String sliceDate) {
        this.startDate = sliceDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPangle() {
        return pangle;
    }

    public void setPangle(String pangle) {
        this.pangle = pangle;
    }

    public String getBins() {
        return bins;
    }

    public void setBins(String bins) {
        this.bins = bins;
    }

    public String getSrange() {
        return srange;
    }

    public void setSrange(String srange) {
        this.srange = srange;
    }

    public String getRstep() {
        return rstep;
    }

    public void setRstep(String rstep) {
        this.rstep = rstep;
    }

    public String getRays() {
        return rays;
    }

    public void setRays(String rays) {
        this.rays = rays;
    }

    public String getA1gate() {
        return a1gate;
    }

    public void setA1gate(String a1gate) {
        this.a1gate = a1gate;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getGain() {
        return gain;
    }

    public void setGain(String gain) {
        this.gain = gain;
    }

    public int getDataDepth() {
        return dataDepth;
    }

    public void setDataDepth(int dataDepth) {
        this.dataDepth = dataDepth;
    }

    public DataBufferContainer getDataBuff() {
        return dataBuff;
    }

    public void setDataBuff(DataBufferContainer dataBuff) {
        this.dataBuff = dataBuff;
    }
    
}
