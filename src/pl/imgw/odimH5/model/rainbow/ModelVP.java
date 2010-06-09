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


/**
 * 
 * Under construction ....
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelVP {

    public static void createDescriptor(String fileName, byte[] fileBuff,
            boolean verbose, Model rb) {

        
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

        if (source.matches("BRZ")) {
            source = "PLC:" + Model.BRZ;
        } else if (source.matches("GDA")) {
            source = "PLC:" + Model.GDA;
        } else if (source.matches("LEG")) {
            source = "PLC:" + Model.LEG;
        } else if (source.matches("PAS")) {
            source = "PLC:" + Model.PAS;
        } else if (source.matches("POZ")) {
            source = "PLC:" + Model.POZ;
        } else if (source.matches("RAM")) {
            source = "PLC:" + Model.RAM;
        } else if (source.matches("RZE")) {
            source = "PLC:" + Model.RZE;
        } else if (source.matches("SWI")) {
            source = "PLC:" + Model.SWI;
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
    }

}
