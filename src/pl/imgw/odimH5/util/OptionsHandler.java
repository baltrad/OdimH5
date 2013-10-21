/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.AplicationConstans;
import pl.imgw.odimH5.model.rainbow.RainbowModel;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * 
 * Handles options' file
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class OptionsHandler {

    public final static String OPTION_XML_FILE = "options.xml";

    public static final String NAME = "name";
    public static final String LOCATION = "location";
    public final static String WMO_ID = "WMO_id";
    public final static String FILE_NAME = "file_name";
    public final static String ORIGINATOR = "originator";
    public final static String PRODUCT_ID = "product_id";
    public final static String RADARS = "radars";
    public final static String ADDRESS = "address";
    public final static String LOGIN = "login";
    public final static String PASSWORD = "password";
    public final static String DIRECTORY = "directory";
    public final static String SUBFOLDERS = "subfolders";
    public final static String NRAYS = "nrays";
    public final static String FORMAT = "format";
    public final static String HOST_ADDRESS = "host_address";
    public static final String PORT_NUMBER = "port_number";
    public final static String SENDER = "sender";

    public final static String SIMULATED = "simulated";
    public final static String PULSEWIDTH = "pulsewidth";
    public final static String RXBANDWIDTH = "RXbandwidth";
    public final static String TXLOSS = "TXloss";
    public final static String RXLOSS = "RXloss";
    public final static String RADOMELOSS = "radomeloss";
    public final static String ANTGAIN = "antgain";
    public final static String GASATTN = "gasattn";
    public final static String RADCONSTH = "radconstH";
    public final static String RADCONSTV = "radconstV";
    public final static String NOMTXPOWER = "nomTXpower";
    public final static String TXPOWER = "TXpower";
    public final static String VSAMPLES = "Vsamples";
    public final static String AZMETHOD = "azmethod";
    public final static String BINMETHOD = "binmethod";
    public final static String MALFUNC = "malfunc";
    public final static String NEZ = "NEZ";
    public final static String RAC ="RAC";
    public final static String PAC = "PAC";
    public final static String S2N = "S2N";

//    private static String getOptionPath() {
//        InitAppUtil init = InitAppUtil.getInstance();
//        return new File(init.getConfDir(), OPTION_XML_FILE).getPath();
//    }
    
    private MessageLogger msgl;
    private boolean verbose;
    
    private static Document doc = loadOptions();
    private RadarOptions[] radarOptions;
    private BaltradOptions baltradOptions;
    private Map<String, List<FTPContainer>> ftpOptions;
    private FTP_Options[] oldFTPOptions;
    
    private static OptionsHandler options = new OptionsHandler();
    
    private OptionsHandler() {};
    
    public static OptionsHandler getOpt() {
        return options;
    }
    
    private static String getOptionPath() {
        return new File(AplicationConstans.CONF, OPTION_XML_FILE).getPath();
    }
    /**
     * 
     * This method reads options from XML file and return XML document object
     * 
     * @param msgl
     * @param verbose
     * @return XML document
     */
    private static Document loadOptions() {
        Document doc = null;
        try {
            DOMParser parser = new DOMParser();
            parser.parse(getOptionPath());
            doc = parser.getDocument();

        } catch (Exception e) {
            System.out.println("Loading options failed!");
            exampleOptionXML();
            System.exit(0);
        }
        return doc;
    }

    /**
     * 
     * This method reads Baltrad Feeder options from XML document
     * 
     * @param doc
     * @return
     */
    private void loadRadarOptions() {

        NodeList radarList = doc.getElementsByTagName("radar");
        int counter = radarList.getLength();
        radarOptions = new RadarOptions[counter];

        for (int i = 0; i < counter; i++) {

            radarOptions[i] = new RadarOptions();

            radarOptions[i].setRadarName(radarList.item(i).getAttributes()
                    .getNamedItem(NAME).getNodeValue());
            
            radarOptions[i].setLocation(RainbowModel.getValueByName(radarList
                    .item(i), LOCATION, null));
            radarOptions[i].setRadarId(RainbowModel.getValueByName(radarList
                    .item(i), WMO_ID, null));
            radarOptions[i].setFileName(RainbowModel.getValueByName(radarList
                    .item(i), FILE_NAME, null));
            radarOptions[i].setOriginator(RainbowModel.getValueByName(radarList
                    .item(i), ORIGINATOR, null));
            radarOptions[i].setProductId(RainbowModel.getValueByName(radarList
                    .item(i), PRODUCT_ID, null));
            radarOptions[i].setDir(RainbowModel.getValueByName(radarList.item(i),
                    DIRECTORY, null));
            radarOptions[i].setNrays(RainbowModel.getValueByName(radarList.item(i),
                    NRAYS, null));
            radarOptions[i].setFormat(RainbowModel.getValueByName(radarList.item(i),
                    FORMAT, null));
            
            // attributes '/how' group in ODIM H5 2.1
            radarOptions[i].setSimulated(RainbowModel.getValueByName(radarList.item(i),
                    SIMULATED, null));
            radarOptions[i].setPulsewidth(RainbowModel.getValueByName(radarList.item(i),
                    PULSEWIDTH, null));
            radarOptions[i].setRXbandwidth(RainbowModel.getValueByName(radarList.item(i),
                    RXBANDWIDTH, null));
            radarOptions[i].setTXloss(RainbowModel.getValueByName(radarList.item(i),
                    TXLOSS, null));
            radarOptions[i].setRXloss(RainbowModel.getValueByName(radarList.item(i),
                    RXLOSS, null));
            radarOptions[i].setRadomeloss(RainbowModel.getValueByName(radarList.item(i),
                    RADOMELOSS, null));
            radarOptions[i].setAntgain(RainbowModel.getValueByName(radarList.item(i),
                    ANTGAIN, null));
            radarOptions[i].setGasattn(RainbowModel.getValueByName(radarList.item(i),
                    GASATTN, null));
            radarOptions[i].setRadconstH(RainbowModel.getValueByName(radarList.item(i),
                    RADCONSTH, null));
            radarOptions[i].setRadconstV(RainbowModel.getValueByName(radarList.item(i),
                    RADCONSTV, null));
            radarOptions[i].setNomTXpower(RainbowModel.getValueByName(radarList.item(i),
                    NOMTXPOWER, null));
            radarOptions[i].setTXpower(RainbowModel.getValueByName(radarList.item(i),
                    TXPOWER, null));
            radarOptions[i].setVsamples(RainbowModel.getValueByName(radarList.item(i),
                    VSAMPLES, null));
            radarOptions[i].setAzmethod(RainbowModel.getValueByName(radarList.item(i),
                    AZMETHOD, null));
            radarOptions[i].setBinmethod(RainbowModel.getValueByName(radarList.item(i),
                    BINMETHOD, null));
            radarOptions[i].setMalfunc(RainbowModel.getValueByName(radarList.item(i),
                    MALFUNC, null));
            radarOptions[i].setNEZ(RainbowModel.getValueByName(radarList.item(i),
                    NEZ, null));
            radarOptions[i].setRAC(RainbowModel.getValueByName(radarList.item(i),
                    RAC, null));
            radarOptions[i].setPAC(RainbowModel.getValueByName(radarList.item(i),
                    PAC, null));
            radarOptions[i].setS2N(RainbowModel.getValueByName(radarList.item(i),
                    S2N, null));

        }
        
    }

    /**
     * 
     * This method reads Baltrad options from XML document
     * 
     * @param doc
     * @return
     */
    private void loadBaltrad() {

        if (doc == null)
            doc = loadOptions();

        NodeList baltradList = doc.getElementsByTagName("baltrad");
        baltradOptions = new BaltradOptions();

        if (baltradList.getLength() != 0)
            baltradOptions.setHostAddress(RainbowModel.getValueByName(
                    baltradList.item(0), HOST_ADDRESS, null));
    }

    /**
     * 
     * This method reads FTP options from XML document
     * 
     * @param doc
     * @return
     */
    private void loadOldFTPOptions() {

        if(doc == null)
            doc = loadOptions();
        
        NodeList ftpList = doc.getElementsByTagName("ftp");
        int counter = ftpList.getLength();
        oldFTPOptions = new FTP_Options[counter];

        for (int i = 0; i < counter; i++) {

            oldFTPOptions[i] = new FTP_Options();

            String radars = RainbowModel.getValueByName(ftpList.item(i),
                    RADARS, null);

            if (radars != null && !radars.isEmpty())
                oldFTPOptions[i].setRadars(radars.split(" "));

            oldFTPOptions[i].setAddress(RainbowModel.getValueByName(ftpList.item(i),
                    ADDRESS, null));
            oldFTPOptions[i].setLogin(RainbowModel.getValueByName(ftpList.item(i),
                    LOGIN, null));
            oldFTPOptions[i].setPassword(RainbowModel.getValueByName(ftpList.item(i),
                    PASSWORD, null));
            oldFTPOptions[i].setDir(RainbowModel.getValueByName(ftpList.item(i),
                    DIRECTORY, null));
        }
    }

    /**
     * 
     * This method reads FTP options from XML document
     * 
     * @param doc
     * @return
     */
    private void loadFTPOptions() {
        
        if(doc == null)
            doc = loadOptions();

        ftpOptions = new HashMap<String, List<FTPContainer>>();
        
        NodeList ftpList = doc.getElementsByTagName("ftp");
        int counter = ftpList.getLength();

        for (int i = 0; i < counter; i++) {

            String r = RainbowModel.getValueByName(ftpList.item(i),
                    RADARS, null);
            if(r == null)
                continue;
            
            String radars[] = r.split(" ");
            
            String address = (RainbowModel.getValueByName(ftpList.item(i),
                    ADDRESS, null));
            String login = (RainbowModel.getValueByName(ftpList.item(i),
                    LOGIN, null));
            String p = RainbowModel.getValueByName(ftpList.item(i), PASSWORD, null);
            if(p == null)
                p = "";
                
            StringBuilder pass = new StringBuilder(p);
            String remoteDir = (RainbowModel.getValueByName(ftpList.item(i),
                    DIRECTORY, null));
            
            String s = (RainbowModel.getValueByName(ftpList.item(i),
                    SUBFOLDERS, null));

            
            int subfolders = FTPContainer.NO_SUBFOLDERS;
            
            try {
                subfolders = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                subfolders = FTPContainer.NO_SUBFOLDERS;
            }
            
            if(subfolders < 0 || subfolders > 2) {
                subfolders = FTPContainer.NO_SUBFOLDERS;
            }
            
//            boolean subfolder = true;
//
//            if(s == null) {
//                s = "true";
//            }
//            
//            if (s.toLowerCase().matches("true")) {
//                subfolder = true;
//            } else if (s.toLowerCase().matches("false"))
//                subfolder = false;

            for (String radar : radars) {
                if (!ftpOptions.containsKey(radar)) {
                    List<FTPContainer> ftpc = new LinkedList<FTPContainer>();
                    ftpOptions.put(radar, ftpc);
                }
                ftpOptions.get(radar).add(
                        new FTPContainer(address, login, pass, remoteDir,
                                subfolders));

            }
            
            
        }
    }
    
    /**
     * 
     * Helper method
     * 
     * @param doc
     * @param arg
     * @return
     */
    public static int getTime(Document doc, String arg) {

        String time = getElementByName(doc, arg);
        int i = 0;
        try {
            i = Integer.valueOf(time);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return i;

    }

    /**
     * Helper method
     * 
     * @param doc
     * @param argName
     * @return
     */
    private static String getElementByName(Document doc, String argName) {

        NodeList nodeList = null;
        nodeList = doc.getElementsByTagName(argName);
        return nodeList.item(0).getFirstChild().getNodeValue();
    }

    /**
     * This method displays on screen an example XML file for Baltrad Feeder
     * 
     */
    public static void exampleOptionXML() {

        System.out.println("<?xml version=\"1.0\" ?>");
        System.out.println("<!-- FTP options -->");
        System.out.println("<options>");
        System.out.println("    <radar name=\"NAME\">");
        System.out.println("        <" + WMO_ID + ">WMO_ID,location</" + WMO_ID + ">");
        System.out.println("        <" + FILE_NAME + ">FILE_NAME_PREFIX</"
                + FILE_NAME + ">");
        System.out.println("        <" + DIRECTORY + ">LOCAL_DIR</" + DIRECTORY
                + ">");
        System.out.println("        <" + NRAYS + ">number of rays</" + NRAYS
                + ">");
        System.out.println("        <" + FORMAT + ">file format</" + FORMAT
                + ">");
        System.out.println("    </radar>");
        System.out.println("    <ftp>");
        System.out.println("        <" + RADARS + ">LIST OF RADARS</" + RADARS
                + ">");
        System.out.println("        <" + ADDRESS + ">FTP_SERVER</" + ADDRESS
                + ">");
        System.out.println("        <" + LOGIN + ">LOGIN</" + LOGIN + ">");
        System.out.println("        <" + PASSWORD + ">PASS</" + PASSWORD + ">");
        System.out
                .println("        <" + DIRECTORY + ">DIR</" + DIRECTORY + ">");
        System.out.println("        <" + SUBFOLDERS + ">true/false</" + SUBFOLDERS + ">");
        System.out.println("    </ftp>");
        System.out.println("    <baltrad>");
        // System.out.println("    <start_time>mm</start_time>");
        // System.out.println("    <" + REPETITION + " >mm</" + REPETITION +
        // ">");
        System.out.println("        <" + HOST_ADDRESS + ">node_address</" + HOST_ADDRESS
                + ">");
        System.out.println("    </baltrad>");
        System.out.println("</options>\n\n");
        System.out
                .println("<address> <login> and <password> are optional for FTP handling.");

    }

    /**
     * @return the radarOptions
     */
    public RadarOptions[] getRadarOptions() {
        if(radarOptions == null)
            loadRadarOptions();
        return radarOptions;
    }

    /**
     * @return the baltradOptions
     */
    public BaltradOptions getBaltradOptions() {
        if(baltradOptions == null)
            loadBaltrad();
        return baltradOptions;
    }

    /**
     * @return the ftpOptions
     */
    public Map<String, List<FTPContainer>> getFtpOptions() {
        if(ftpOptions == null)
            loadFTPOptions();
        return ftpOptions;
    }

    /**
     * @return the oldFTPOptions
     */
    public FTP_Options[] getOldFTPOptions() {
        if(oldFTPOptions == null)
            loadOldFTPOptions();
        return oldFTPOptions;
    }

    /**
     * @param msgl the msgl to set
     */
    public void setMsgl(MessageLogger msgl) {
        this.msgl = msgl;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    
    
}
