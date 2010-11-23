/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow531;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.ParametersContainer;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.RadarOptions;

/**
 * 
 * It contains static methods creating XML file of IMAGE objects descriptor
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelImage531 {

    /**
     * Method creates descriptor file for RAINBOW software platform. This
     * descriptor is created for IMAGE objects defined by ODIM_H5 specification.
     * 
     * @param fileNameOut
     *            Output name of XML file
     * @param fileBuff
     *            Input file data in a byte array
     * @param verbose
     *            Verbose mode
     * @param rb531
     *            Rainbow 5.31.1 class model
     */
    @SuppressWarnings("static-access")
    public static String createDescriptor(String fileNameOut, byte[] fileBuff,
            boolean verbose, Model531 rb531, RadarOptions[] options) {

        boolean isDirect = false;
        if (fileNameOut.endsWith(".h5") || fileNameOut.isEmpty())
            isDirect = true;

        byte[] hdrBuff = rb531.getRAINBOWMetadata(fileBuff, rb531.PRODUCT, verbose);
        Document inputDoc = rb531.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        NodeList nodeList = null;
        ParametersContainer cont = new ParametersContainer();

        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "data", verbose);
        String date = rb531.getRAINBOWMetadataElement(nodeList, "date", verbose);
        String time = rb531.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "radarsensor", verbose);
        String source = rb531.getRAINBOWMetadataElement(nodeList, "id", verbose);

        String radarName = "";
        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarWMOName();
                break;
            }
        }

        if (radarName.isEmpty()) {
            System.out.println("Add " + source + " to options.xml");
            System.exit(0);
        } else {
            source = "WMO:" + radarName;
        }
        cont.setSource(source);

        NodeList pictureList = inputDoc.getElementsByTagName("radarpicture");
        int datasetSize = pictureList.getLength();
        Node radarPicture = null;
        for (int i = 0; i < datasetSize; i++) {
            String placeid = pictureList.item(i).getAttributes().getNamedItem(
                    "placeid").getNodeValue();
            if (placeid.matches("top") || placeid.matches("etop")) {
                radarPicture = pictureList.item(i);
                break;
            }

        }

        cont.setOffset(radarPicture.getAttributes().getNamedItem("min")
                .getNodeValue());

        String max = radarPicture.getAttributes().getNamedItem("max")
                .getNodeValue();
        String type = rb531.getValueByName(radarPicture, "projection", "type");
        cont.setType(type);
        String lon0 = rb531.getValueByName(radarPicture, "lon_0", null);
        String lat0 = rb531.getValueByName(radarPicture, "lat_0", null);
        String ellps = rb531.getValueByName(radarPicture, "ellps", null);

        cont.setProjection(rb531.parseRAINBOWProjection(lon0, lat0, type, ellps,
                rb531.EARTH_RAD));

        cont.setXsize(rb531.getValueByName(radarPicture, "projection", "size_x"));
        cont.setYsize(rb531.getValueByName(radarPicture, "projection", "size_y"));
        cont.setUlLon(rb531.getValueByName(radarPicture, "projection", "lon_ul"));
        cont.setUlLat(rb531.getValueByName(radarPicture, "projection", "lat_ul"));
        cont.setLrLon(rb531.getValueByName(radarPicture, "projection", "lon_lr"));
        cont.setLrLat(rb531.getValueByName(radarPicture, "projection", "lat_lr"));

        int dataBlobNumber = Integer.parseInt(rb531.getValueByName(radarPicture,
                "datamap", "blobid"));
        String depth = rb531.getValueByName(radarPicture, "datamap", "depth");
        cont.setDataDepth(depth);
        int dataDepth = Integer.parseInt(depth);
        int flagDepth = Integer.parseInt(rb531.getValueByName(radarPicture,
                "flagmap", "depth"));
        cont.setFlagDepth(flagDepth);
        int flagBlobNumber = Integer.parseInt(rb531.getValueByName(radarPicture,
                "flagmap", "blobid"));

        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "task", verbose);
        cont.setTask(rb531.getRAINBOWMetadataElement(nodeList, "name", verbose));
        cont.setStartepochs(rb531.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setEndepochs(rb531.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setDate(rb531.parseRAINBOWDate(date, verbose));
        cont.setTime(rb531.parseRAINBOWTime(time, verbose));

        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "product", verbose);
        cont.setSwVersion(rb531.getRAINBOWMetadataElement(nodeList, "version",
                verbose));
        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        cont.setBeamwidth(rb531.getRAINBOWMetadataElement(nodeList, "", verbose));
        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        cont.setWavelength(rb531.getRAINBOWMetadataElement(nodeList, "", verbose));
        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "product", verbose);
        String datatype = rb531.getRAINBOWMetadataElement(nodeList, "datatype",
                verbose);
        if (datatype.matches(rb531.TYPE_DBZ))
            datatype = rb531.QNT_TH;
        else if (datatype.matches(rb531.TYPE_DBR))
            datatype = rb531.QNT_RATE;
        else if (datatype.matches(rb531.TYPE_DBA))
            datatype = rb531.QNT_ACRR;
        else if (datatype.matches(rb531.TYPE_V))
            datatype = rb531.QNT_VRAD;
        else if (datatype.matches(rb531.TYPE_H))
            datatype = rb531.QNT_H;
        cont.setQuantity(datatype);

        String product = rb531
                .getRAINBOWMetadataElement(nodeList, "name", verbose);

        String pacMethod = null;
        String pacNumProd = null;
        String prodpar = "";

        String res = "";
        nodeList = rb531.getRAINBOWNodesByName(inputDoc, "dispres", false);
        res = rb531.getRAINBOWMetadataElement(nodeList, "", false);

        if (product.matches("CAPPI")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "algtype", verbose);
            product = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertKMtoM(rb531.convertRAINBOWParam(prodpar));
        } else if (product.matches("SRI")) {
            // product = "RR";
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "sriheight", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertKMtoM(rb531.convertRAINBOWParam(prodpar));
        } else if (product.matches("PAC")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "numprod", verbose);
            pacNumProd = rb531.convertRAINBOWParam(rb531.getRAINBOWMetadataElement(
                    nodeList, "", verbose));
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "algtype", verbose);
            pacMethod = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "timeinterval",
                    verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertTimeInterval(prodpar);
            prodpar = rb531.convertRAINBOWParam(prodpar);
            product = "RR";
        } else if (product.matches("PPI")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "ele", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertRAINBOWParam(prodpar);

        } else if (product.matches("MAX")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertKMtoM(rb531.convertRAINBOWParam(prodpar));
            nodeList = rb531
                    .getRAINBOWNodesByName(inputDoc, "disphorres", verbose);
            res = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
        } else if (product.matches("HSHEAR")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertKMtoM(rb531.convertRAINBOWParam(prodpar));
        } else if (product.matches("EHT")) {
            product = "ETOP";
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "minz", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertRAINBOWParam(prodpar);
        } else if (product.matches("VIL")) {
            nodeList = rb531.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb531.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb531.convertKMtoM(rb531.convertRAINBOWParam(prodpar));
        }

        cont.setProdpar(prodpar);

        cont.setProduct(product);
        cont.setPacMethod(pacMethod);
        cont.setPacNumProd(pacNumProd);

        res = String
                .valueOf(Double.parseDouble(rb531.convertRAINBOWParam(res)) * 1000);
        cont.setYscale(res);
        cont.setXscale(res);
        cont.setGain(rb531.getRAINBOWGain(cont.getOffset(), max, dataDepth));

        // prepare actual dataset
        int width = Integer.parseInt(cont.getXsize());
        int height = Integer.parseInt(cont.getYsize());

        int firstBlob = rb531.getMin(flagBlobNumber, dataBlobNumber);

        DataBufferContainer dataBuff = rb531.getRainbowDataSection(fileBuff,
                dataBlobNumber, firstBlob, verbose);
        DataBufferContainer maskBuff = rb531.getRainbowDataSection(fileBuff,
                flagBlobNumber, firstBlob, verbose);

        // Inflate radar data section and mask section
        int[][] infDataBuff = rb531.inflate2DRAINBOWDataSection(dataBuff
                .getDataBuffer(), width, height, verbose);
        byte[] infMaskBuff = rb531.inflate1DRAINBOWDataSection(maskBuff
                .getDataBuffer(), maskBuff.getDataBufferLength(), verbose);
        // Create range mask

        infDataBuff = rb531.createRAINBOWMask(infDataBuff, width, height,
                infMaskBuff, flagDepth, verbose);

        // =============================================================
        // Create XML document object

        
        if (isDirect) {

            if(fileNameOut.isEmpty())
                fileNameOut = cont.getTime() + cont.getType() + ".h5";
            
            ModelImage531H5.createDescriptor(cont, rb531, fileNameOut, infDataBuff,
                    verbose);

        } else {
            String dataDir = rb531.proc.createDirectory("data", verbose);
            String dataFileName = dataDir + File.separator + rb531.H5_DATA_N
                    + ".dat";
            cont.setDataFileName(dataFileName);

            Document od = ModelImage531XML.createDescriptor(cont, rb531, verbose);
            // cont prepares this class to make a condition for direct HDF
            // creation

            // Save XML document in file
            rb531.proc.saveXMLFile(od, fileNameOut, verbose);
            // Save data buffer to file
            rb531.writeRAINBOWData(infDataBuff, dataFileName, verbose);
        }
        return fileNameOut;
    }
}
