/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.rainbow.Model;

/**
 * 
 * Handles options for Baltrad Feeder
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class OptionsHandler {

    public final static String OPTION_XML_FILE = "options.xml";

    public final static String WMO_ID = "WMO_id";
    public final static String FILE_NAME = "file_name";
    public final static String ADDRESS = "address";
    public final static String LOGIN = "login";
    public final static String PASSWORD = "password";
    public final static String DIRECTORY = "directory";
    public final static String REPETITION = "repetition_time";
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
                    verbose);
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
                    .getNamedItem("name").getNodeValue());

            options[i].setRadarWMOName(Model.getValueByName(radarList.item(i),
                    WMO_ID, null));
            options[i].setFileName(Model.getValueByName(radarList.item(i),
                    FILE_NAME, null));
            options[i].setDir(Model.getValueByName(radarList.item(i),
                    DIRECTORY, null));
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
        
        if(baltradList.getLength() == 0) {
            return null;
        }
        BaltradOptions options = new BaltradOptions();
        
            
            options.setSender(Model.getValueByName(baltradList.item(0),
                    SENDER, null));
            options.setServer(Model.getValueByName(baltradList.item(0), SERVER,
                    null));
        
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

            options[i].setAddress(Model.getValueByName(ftpList.item(i),
                    ADDRESS, null));
            options[i].setLogin(Model.getValueByName(ftpList.item(i), LOGIN,
                    null));
            options[i].setPassword(Model.getValueByName(ftpList.item(i),
                    PASSWORD, null));
            options[i].setDir(Model.getValueByName(ftpList.item(i), DIRECTORY,
                    null));
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
    public static String getElementByName(Document doc, String argName) {

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
        System.out.println("        <" + FILE_NAME + ">FILE NAME PREFIX</"
                + FILE_NAME + ">");
        System.out.println("    </radar>");
        System.out.println("    <ftp>");
        System.out.println("        <" + ADDRESS + ">FTP</" + ADDRESS + ">");
        System.out.println("        <" + LOGIN + ">LOGIN</" + LOGIN + ">");
        System.out.println("        <" + PASSWORD + ">PASS</" + PASSWORD + ">");
        System.out
                .println("        <" + DIRECTORY + ">DIR</" + DIRECTORY + ">");
        System.out.println("    </ftp>");
        System.out.println("    <baltrad>");
        // System.out.println("    <start_time>mm</start_time>");
        // System.out.println("    <" + REPETITION + " >mm</" + REPETITION +
        // ">");
        System.out.println("        <" + SERVER + ">HTTP_address</" + SERVER + ">");
        System.out.println("        <" + SENDER + ">Baltrad.IMGW.pl</" + SENDER
                + ">");
        System.out.println("    </baltrad>");
        System.out.println("</options>\n\n");
        System.out
                .println("<address> <login> and <password> are optional for FTP handling.");

    }

}
