/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.ParametersContainer;
import pl.imgw.odimH5.util.RadarOptions;


/**
 * 
 * Under construction ....
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelVP {

    public static String createDescriptor(String fileName, byte[] fileBuff,
            boolean verbose, Model rb, RadarOptions[] options) {

        
        //I'll finish that later ....
        
        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.PRODUCT, verbose);
        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        NodeList nodeList = null;
        ParametersContainer cont = new ParametersContainer();

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "name", verbose);
        
        cont.setDate(rb.parseRAINBOWDate(date, verbose));
        cont.setTime(rb.parseRAINBOWTime(time, verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        String radarName = "";
        for(int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())){
                radarName = options[i].getRadarWMOName();
                break;
            }
        }

        if(radarName.isEmpty()) {
            System.out.println("Add " + source + " to options.xml");
            System.exit(0);
        } else {
            source = "WMO:" + radarName;
        }
        
        cont.setSource(source);
        

        String dataDir = rb.proc.createDirectory("data", verbose);
        String dataFileName = dataDir + File.separator + rb.H5_DATA_N + ".dat";
        cont.setDataFileName(dataFileName);
        
     // Create XML document object
        Document od = rb.proc.createXMLDocumentObject(verbose);

        Comment comment = od
                .createComment("ODIM_H5 descriptor file, platform: RAINBOW,"
                        + " file object: " + rb.VP);

        od.appendChild(comment);
        Element root = od.createElement(rb.H5_GROUP);
        root.setAttribute(rb.H5_OBJECT_NAME, rb.H5_ROOT);
        od.appendChild(root);
        
        
        // Save XML document in file
        rb.proc.saveXMLFile(od, fileName, verbose);
        return fileName;
    }

}
