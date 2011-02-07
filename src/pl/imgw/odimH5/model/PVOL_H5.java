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
public class PVOL_H5 {

    // what
    public static final String OBJECT = "object";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String SOURCE = "source";
    public static final String VERSION = "version";

    // where
    public static final String LON = "lon";
    public static final String LAT = "lat";
    public static final String HEIGHT = "height";

    // how
    public static final String STARTEPOCHS = "startepochs";
    public static final String ENDEPOCHS = "endepochs";
    public static final String SYSTEM = "system";
    public static final String SOFTWARE = "software";
    public static final String SW_VERSION = "sw_version";
    public static final String BEAMWIDTH = "beamwidth";
    public static final String WAVELENGTH = "wavelength";

    // dataset what
    public static final String PRODUCT = "product";
    public static final String STARTDATE = "startdate";
    public static final String STARTTIME = "starttime";
    public static final String ENDDATE = "enddate";
    public static final String ENDTIME = "endtime";

    // dataset where
    public static final String ELANGLE = "elangle";
    public static final String NBINS = "nbins";
    public static final String RSTART = "rstart";
    public static final String RSCALE = "rscale";
    public static final String NRAYS = "nrays";
    public static final String A1GATE = "a1gate";

    // dataset what
    public static final String QUANTITY = "quantity";
    public static final String GAIN = "gain";
    public static final String OFFSET = "offset";
    public static final String NODATA = "nodata";
    public static final String UNDETECT = "undetect";

    // dataset data what
    public static final String CHUNK = "chunk";
    public static final String CLASS = "CLASS";
    public static final String DATA_SIZE = "data_size";
    public static final String DATA_TYPE = "data_type";
    public static final String DIMENSIONS = "dimensions";
    public static final String GZIP_LEVEL = "gzip_level";
    public static final String IM_VER = "IMAGE_VERSION";

    // quality index
    public static final String QI_GAIN = "qi_gain";
    public static final String QI_OFFSET = "qi_offset";
    public static final String QI_NBINS = "qi_nbins";
    public static final String QI_NRAYS = "qi_nrays";
    public static final String GEN_a = "GEN_a";
    public static final String GEN_b = "GEN_b";
    public static final String SYS_QCOn = "SYS_QCOn"; 
    public static final String SYS_QIOn = "SYS_QIOn";
    public static final String SYS_Freq = "SYS_Freq";
    public static final String SYS_QIFreq = "SYS_QIFreq";
    public static final String SYS_Beam = "SYS_Beam";
    public static final String SYS_QIBeam = "SYS_QIBeam";
    public static final String SYS_Elev = "SYS_Elev";
    public static final String SYS_QIElev = "SYS_QIElev";
    public static final String SYS_Azim = "SYS_Azim";
    public static final String SYS_QIAzim = "SYS_QIAzim";
    public static final String SYS_Filter = "SYS_Filter";
    public static final String SYS_QIFilter = "SYS_QIFilter";
    public static final String SYS_Detect = "SYS_Detect";
    public static final String SYS_QIDetect = "SYS_QIDetect";
    public static final String SYS_Speed = "SYS_Speed";
    public static final String SYS_QISpeed = "SYS_QISpeed";
    public static final String SYS_Radome = "SYS_Radome";
    public static final String SYS_QIRadome = "SYS_QIRadome";
    public static final String SYS_Calibr = "SYS_Calibr";
    public static final String SYS_QICalibr = "SYS_QICalibr";
    public static final String SYS_TSamp = "SYS_TSamp";
    public static final String SYS_QITSamp = "SYS_QITSamp";
    public static final String SYS_RSamp = "SYS_RSamp";
    public static final String SYS_QIRSamp = "SYS_QIRSamp";
    public static final String AH_QCOn = "AH_QCOn";
    public static final String AH_QIOn = "AH_QIOn";
    public static final String AH_QI1 = "AH_QI1";
    public static final String AH_QI0 = "AH_QI0";
    public static final String AV_QCOn = "AV_QCOn";
    public static final String AV_QIOn = "AV_QIOn";
    public static final String AV_QI1 = "AV_QI1";
    public static final String AV_QI0 = "AV_QI0";
    public static final String GC_QCOn = "GC_QCOn";
    public static final String GC_QIOn = "GC_QIOn";
    public static final String GC_QI = "GC_QI";
    public static final String GC_QIUn = "GC_QIUn";
    public static final String GC_MinPbb = "GC_MinPbb";
    public static final String SPIKE_QCOn = "SPIKE_QCOn";
    public static final String SPIKE_QIOn = "SPIKE_QIOn";
    public static final String SPIKE_QI = "SPIKE_QI";
    public static final String SPIKE_QIUn = "SPIKE_QIUn";
    public static final String SPIKE_Diff = "SPIKE_Diff";
    public static final String SPIKE_Azim = "SPIKE_Azim";
    public static final String SPIKE_Refl = "SPIKE_Refl";
    public static final String SPIKE_Perc = "SPIKE_Perc";
    public static final String RSPEC_QCOn = "RSPEC_QCOn";
    public static final String RSPEC_QIOn = "RSPEC_QIOn";
    public static final String RSPEC_QI = "RSPEC_QI";
    public static final String RSPEC_QIUn = "RSPEC_QIUn";
    public static final String RSPEC_Grid = "RSPEC_Grid";
    public static final String RSPEC_Num = "RSPEC_Num";
    public static final String RSPEC_Step = "RSPEC_Step";
    public static final String SPEC_QCOn = "SPEC_QCOn";
    public static final String SPEC_QIOn = "SPEC_QIOn";
    public static final String SPEC_QI = "SPEC_QI";
    public static final String SPEC_QIUn = "SPEC_QIUn";
    public static final String SPEC_Grid = "SPEC_Grid";
    public static final String SPEC_Num = "SPEC_Num";
    public static final String SPEC_Step = "SPEC_Step";
    public static final String PBB_QIOn = "PBB_QIOn";
    public static final String PBB_QCOn = "PBB_QCOn";
    public static final String PBB_Max = "PBB_Max";
    public static final String PBB_QIUn = "PBB_QIUn";
    public static final String ATT_QCOn = "ATT_QCOn";
    public static final String ATT_QIOn = "ATT_QIOn";
    public static final String ATT_a = "ATT_a";
    public static final String ATT_b = "ATT_b";
    public static final String ATT_QIUn = "ATT_QIUn";
    public static final String ATT_QI1 = "ATT_QIUn";
    public static final String ATT_QI0 = "ATT_QI0";
    public static final String ATT_Refl = "ATT_Refl";
    public static final String ATT_Last = "ATT_Last";
    public static final String ATT_Sum = "ATT_Sum";
    public static final String SUM_QCOn = "SUM_QCOn";
    public static final String SUM_QIOn = "SUM_QIOn";
    public static final String SUM_QI1 = "SUM_QI1";
    public static final String SUM_QI0 = "SUM_QI0";

}
