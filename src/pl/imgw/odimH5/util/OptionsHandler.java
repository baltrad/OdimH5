/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.rainbow.RainbowModel;

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
    public final static String RADARS = "radars";
    public final static String ADDRESS = "address";
    public final static String LOGIN = "login";
    public final static String PASSWORD = "password";
    public final static String DIRECTORY = "directory";
    public final static String NRAYS = "nrays";
    public final static String FORMAT = "format";
    public final static String SERVER = "server";
    public final static String SENDER = "sender";


    /**
     * 
     * This method reads options from XML file and return XML document object
     * 
     * @param msgl
     * @param verbose
     * @return XML document
     */
    public static Document loadOptions(MessageLogger msgl, boolean verbose) {

        Document doc = null;
        try {
            DOMParser parser = new DOMParser();
            parser.parse(OPTION_XML_FILE);
            doc = parser.getDocument();
            msgl.showMessage("Parsing options file: " + OPTION_XML_FILE,
                    verbose);
        } catch (Exception e) {
            msgl.showMessage("Failed to parse options file: " + e.getMessage(),
                    true);
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
    public static RadarOptions[] getRadarOptions(Document doc) {

        NodeList radarList = doc.getElementsByTagName("radar");
        int counter = radarList.getLength();
        RadarOptions[] options = new RadarOptions[counter];

        for (int i = 0; i < counter; i++) {

            options[i] = new RadarOptions();

            options[i].setRadarName(radarList.item(i).getAttributes()
                    .getNamedItem(NAME).getNodeValue());
            
            options[i].setLocation(RainbowModel.getValueByName(radarList
                    .item(i), LOCATION, null));
            options[i].setRadarWMOName(RainbowModel.getValueByName(radarList
                    .item(i), WMO_ID, null));
            options[i].setFileName(RainbowModel.getValueByName(radarList
                    .item(i), FILE_NAME, null));
            options[i].setDir(RainbowModel.getValueByName(radarList.item(i),
                    DIRECTORY, null));
            options[i].setNrays(RainbowModel.getValueByName(radarList.item(i),
                    NRAYS, null));
            options[i].setFormat(RainbowModel.getValueByName(radarList.item(i),
                    FORMAT, null));

        }
        return options;
    }

    /**
     * 
     * This method reads Baltrad options from XML document
     * 
     * @param doc
     * @return
     */
    public static BaltradOptions getBaltrad(Document doc) {

        NodeList baltradList = doc.getElementsByTagName("baltrad");
        BaltradOptions options = new BaltradOptions();

        if (baltradList.getLength() == 0) {
            return options;
        }

        options.setSender(RainbowModel.getValueByName(baltradList.item(0),
                SENDER, null));
        options.setServer(RainbowModel.getValueByName(baltradList.item(0),
                SERVER, null));

        return options;
    }

    /**
     * 
     * This method reads FTP options from XML document
     * 
     * @param doc
     * @return
     */
    public static FTP_Options[] getFTPOptions(Document doc) {

        NodeList ftpList = doc.getElementsByTagName("ftp");
        int counter = ftpList.getLength();
        FTP_Options[] options = new FTP_Options[counter];

        for (int i = 0; i < counter; i++) {

            options[i] = new FTP_Options();

            String radars = RainbowModel.getValueByName(ftpList.item(i),
                    RADARS, null);

            if (radars != null && !radars.isEmpty())
                options[i].setRadars(radars.split(" "));

            options[i].setAddress(RainbowModel.getValueByName(ftpList.item(i),
                    ADDRESS, null));
            options[i].setLogin(RainbowModel.getValueByName(ftpList.item(i),
                    LOGIN, null));
            options[i].setPassword(RainbowModel.getValueByName(ftpList.item(i),
                    PASSWORD, null));
            options[i].setDir(RainbowModel.getValueByName(ftpList.item(i),
                    DIRECTORY, null));
        }
        return options;
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
        System.out.println("        <" + WMO_ID + ">WMO_ID</" + WMO_ID + ">");
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
        System.out.println("    </ftp>");
        System.out.println("    <baltrad>");
        // System.out.println("    <start_time>mm</start_time>");
        // System.out.println("    <" + REPETITION + " >mm</" + REPETITION +
        // ">");
        System.out.println("        <" + SERVER + ">HTTP_address</" + SERVER
                + ">");
        System.out.println("        <" + SENDER + ">Baltrad.IMGW.pl</" + SENDER
                + ">");
        System.out.println("    </baltrad>");
        System.out.println("</options>\n\n");
        System.out
                .println("<address> <login> and <password> are optional for FTP handling.");

    }

}
