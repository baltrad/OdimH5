/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.ParametersContainer;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.OptionContainer;

/**
 * 
 * It contains static methods creating XML file of IMAGE objects descriptor
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelImage {

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
     * @param rb
     *            Rainbow class model
     */
    @SuppressWarnings("static-access")
    public static void createDescriptor(String fileNameOut, byte[] fileBuff,
            boolean verbose, Model rb, OptionContainer[] options) {

        boolean isDirect = false;
        if (fileNameOut.endsWith(".h5"))
            isDirect = true;

        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.PRODUCT, verbose);
        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        NodeList nodeList = null;
        ParametersContainer cont = new ParametersContainer();

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);
        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

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
            source = radarName;
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
        String type = rb.getValueByName(radarPicture, "projection", "type");
        cont.setType(type);
        String lon0 = rb.getValueByName(radarPicture, "lon_0", null);
        String lat0 = rb.getValueByName(radarPicture, "lat_0", null);
        String ellps = rb.getValueByName(radarPicture, "ellps", null);

        cont.setProjection(rb.parseRAINBOWProjection(lon0, lat0, type, ellps,
                rb.EARTH_RAD));

        cont.setXsize(rb.getValueByName(radarPicture, "projection", "size_x"));
        cont.setYsize(rb.getValueByName(radarPicture, "projection", "size_y"));
        cont.setUlLon(rb.getValueByName(radarPicture, "projection", "lon_ul"));
        cont.setUlLat(rb.getValueByName(radarPicture, "projection", "lat_ul"));
        cont.setLrLon(rb.getValueByName(radarPicture, "projection", "lon_lr"));
        cont.setLrLat(rb.getValueByName(radarPicture, "projection", "lat_lr"));

        int dataBlobNumber = Integer.parseInt(rb.getValueByName(radarPicture,
                "datamap", "blobid"));
        String depth = rb.getValueByName(radarPicture, "datamap", "depth");
        cont.setDataDepth(depth);
        int dataDepth = Integer.parseInt(depth);
        int flagDepth = Integer.parseInt(rb.getValueByName(radarPicture,
                "flagmap", "depth"));
        cont.setFlagDepth(flagDepth);
        int flagBlobNumber = Integer.parseInt(rb.getValueByName(radarPicture,
                "flagmap", "blobid"));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "task", verbose);
        cont.setTask(rb.getRAINBOWMetadataElement(nodeList, "name", verbose));
        cont.setStartepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setEndepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setDate(rb.parseRAINBOWDate(date, verbose));
        cont.setTime(rb.parseRAINBOWTime(time, verbose));

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "product", verbose);
        cont.setSwVersion(rb.getRAINBOWMetadataElement(nodeList, "version",
                verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        cont.setBeamwidth(rb.getRAINBOWMetadataElement(nodeList, "", verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        cont.setWavelength(rb.getRAINBOWMetadataElement(nodeList, "", verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "product", verbose);
        String datatype = rb.getRAINBOWMetadataElement(nodeList, "datatype",
                verbose);
        if (datatype.matches(rb.TYPE_DBZ))
            datatype = rb.QNT_TH;
        else if (datatype.matches(rb.TYPE_DBR))
            datatype = rb.QNT_RATE;
        else if (datatype.matches(rb.TYPE_DBA))
            datatype = rb.QNT_ACRR;
        else if (datatype.matches(rb.TYPE_V))
            datatype = rb.QNT_VRAD;
        else if (datatype.matches(rb.TYPE_H))
            datatype = rb.QNT_H;
        cont.setQuantity(datatype);

        String product = rb
                .getRAINBOWMetadataElement(nodeList, "name", verbose);

        String pacMethod = null;
        String pacNumProd = null;
        String prodpar = "";

        String res = "";
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "dispres", false);
        res = rb.getRAINBOWMetadataElement(nodeList, "", false);

        if (product.matches("CAPPI")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "algtype", verbose);
            product = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertKMtoM(rb.convertRAINBOWParam(prodpar));
        } else if (product.matches("SRI")) {
            // product = "RR";
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "sriheight", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertKMtoM(rb.convertRAINBOWParam(prodpar));
        } else if (product.matches("PAC")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "numprod", verbose);
            pacNumProd = rb.convertRAINBOWParam(rb.getRAINBOWMetadataElement(
                    nodeList, "", verbose));
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "algtype", verbose);
            pacMethod = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "timeinterval",
                    verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertTimeInterval(prodpar);
            prodpar = rb.convertRAINBOWParam(prodpar);
            product = "RR";
        } else if (product.matches("PPI")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "ele", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertRAINBOWParam(prodpar);

        } else if (product.matches("MAX")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertKMtoM(rb.convertRAINBOWParam(prodpar));
            nodeList = rb
                    .getRAINBOWNodesByName(inputDoc, "disphorres", verbose);
            res = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        } else if (product.matches("HSHEAR")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertKMtoM(rb.convertRAINBOWParam(prodpar));
        } else if (product.matches("EHT")) {
            product = "ETOP";
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "minz", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertRAINBOWParam(prodpar);
        } else if (product.matches("VIL")) {
            nodeList = rb.getRAINBOWNodesByName(inputDoc, "height", verbose);
            prodpar = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
            prodpar = rb.convertKMtoM(rb.convertRAINBOWParam(prodpar));
        }

        cont.setProdpar(prodpar);

        cont.setProduct(product);
        cont.setPacMethod(pacMethod);
        cont.setPacNumProd(pacNumProd);

        res = String
                .valueOf(Double.parseDouble(rb.convertRAINBOWParam(res)) * 1000);
        cont.setYscale(res);
        cont.setXscale(res);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        cont.setStartdate(rb.parseRAINBOWDate(rb.getRAINBOWMetadataElement(
                nodeList, "date", verbose), verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        cont.setStarttime(rb.parseRAINBOWTime(rb.getRAINBOWMetadataElement(
                nodeList, "time", verbose), verbose));
        cont.setGain(rb.getRAINBOWGain(cont.getOffset(), max, dataDepth));

        // prepare actual dataset
        int width = Integer.parseInt(cont.getXsize());
        int height = Integer.parseInt(cont.getYsize());

        int firstBlob = rb.getMin(flagBlobNumber, dataBlobNumber);

        DataBufferContainer dataBuff = rb.getRainbowDataSection(fileBuff,
                dataBlobNumber, dataDepth, firstBlob, verbose);
        DataBufferContainer maskBuff = rb.getRainbowDataSection(fileBuff,
                flagBlobNumber, flagDepth, firstBlob, verbose);

        // Inflate radar data section and mask section
        int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(dataBuff
                .getDataBuffer(), width, height, verbose);
        byte[] infMaskBuff = rb.inflate1DRAINBOWDataSection(maskBuff
                .getDataBuffer(), maskBuff.getDataBufferLength(), verbose);
        // Create range mask

        infDataBuff = rb.createRAINBOWMask(infDataBuff, width, height,
                infMaskBuff, flagDepth, verbose);

        // =============================================================
        // Create XML document object

        if (isDirect) {

            ModelImageH5.createDescriptor(cont, rb, fileNameOut, infDataBuff,
                    verbose);

        } else {
            String dataDir = rb.proc.createDirectory("data", verbose);
            String dataFileName = dataDir + File.separator + rb.H5_DATA_N
                    + ".dat";
            cont.setDataFileName(dataFileName);

            Document od = ModelImageXML.createDescriptor(cont, rb, verbose);
            // cont prepares this class to make a condition for direct HDF
            // creation

            // Save XML document in file
            rb.proc.saveXMLFile(od, fileNameOut, verbose);
            // Save data buffer to file
            rb.writeRAINBOWData(infDataBuff, dataFileName, verbose);
        }
    }
}
