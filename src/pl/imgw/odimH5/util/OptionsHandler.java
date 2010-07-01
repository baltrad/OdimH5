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
    private String startTime;
    private String endTime;

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
    public static OptionContainer[] getOptions(Document doc) {

        NodeList radarList = doc.getElementsByTagName("radar");
        int counter = radarList.getLength();
        OptionContainer[] options = new OptionContainer[counter];

        for (int i = 0; i < counter; i++) {

            options[i] = new OptionContainer();

            options[i].setRadarName(radarList.item(i).getAttributes()
                    .getNamedItem("name").getNodeValue());
            options[i].setAddress(Model.getValueByName(radarList.item(i),
                    "address", null));
            options[i].setLogin(Model.getValueByName(radarList.item(i),
                    "login", null));
            options[i].setPassword(Model.getValueByName(radarList.item(i),
                    "password", null));
            options[i].setRemoteDir(Model.getValueByName(radarList.item(i),
                    "remote_dir", null));
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
        System.out.println("        <address>IP</address>");
        System.out.println("        <login>LOGIN</login>");
        System.out.println("        <password>PASS</password>");
        System.out.println("        <remote_dir>DIR</remote_dir>");
        System.out.println("    </radar>");
//        System.out.println("    <start_time>mm</start_time>");
        System.out.println("    <repetition_time>mm</repetition_time>");
        System.out.println("    <server>HTTP_address</server>");
        System.out.println("    <sender>Baltrad.IMGW.pl</sender>");
        System.out.println("</options>\n\n");
        System.out.println("<address> <login> and <password> are optional for FTP handling.");
        

    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

}
