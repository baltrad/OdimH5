/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.ParametersContainer;
import pl.imgw.odimH5.util.DataBufferContainer;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;

/**
 * 
 * It contains static methods creating XML file of RHI objects descriptor
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelRHI {

    /**
     * 
     * Method creates descriptor file for RAINBOW software platform. This
     * descriptor is created for RHI objects defined by ODIM_H5 specification.
     * 
     * @param fileName
     *            Output name of XML file
     * @param fileBuff
     *            Input file data in a byte array
     * @param verbose
     *            Verbose mode
     * @param rb
     *            Rainbow class model
     */
    public static String createDescriptor(String fileName, byte[] fileBuff,
            boolean verbose, RainbowModel rb) {

        RadarOptions[] options = OptionsHandler.getOpt().getRadarOptions();
        
        boolean isDirect = false;
        if (fileName.endsWith(".h5"))
            isDirect = true;

        byte[] hdrBuff = rb.getRAINBOWMetadata(fileBuff, rb.PRODUCT, verbose);
        Document inputDoc = rb.parseRAINBOWMetadataBuffer(hdrBuff, verbose);

        NodeList nodeList = null;
        ParametersContainer cont = new ParametersContainer();

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "data", verbose);
        String date = rb.getRAINBOWMetadataElement(nodeList, "date", verbose);
        String time = rb.getRAINBOWMetadataElement(nodeList, "time", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "lon_1", verbose);
        String lon_1 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "lat_1", verbose);
        String lat_1 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "disphorres", verbose);
        String res_x = rb.convertRAINBOWParam(rb.getRAINBOWMetadataElement(
                nodeList, "", verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "dispvertres", verbose);
        String res_y = rb.convertRAINBOWParam(rb.getRAINBOWMetadataElement(
                nodeList, "", verbose));
        res_x = String.valueOf(Double.parseDouble(res_x) * 1000);
        res_y = String.valueOf(Double.parseDouble(res_y) * 1000);
        // nodeList = rb.getRAINBOWNodesByName(inputDoc, "x_0", verbose);
        // String x_0 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "x_1", verbose);
        String x_1 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        // nodeList = rb.getRAINBOWNodesByName(inputDoc, "z_0", verbose);
        // String z_0 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        // nodeList = rb.getRAINBOWNodesByName(inputDoc, "z_1", verbose);
        // String z_1 = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "ele", verbose);
        String ele = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "azi", verbose);
        String azi = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "projection", verbose);
        String xsize = rb
                .getRAINBOWMetadataElement(nodeList, "size_x", verbose);
        String ysize = rb
                .getRAINBOWMetadataElement(nodeList, "size_y", verbose);

        cont.setDate(rb.parseRAINBOWDate(date, verbose));
        cont.setTime(rb.parseRAINBOWTime(time, verbose));
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarinfo", verbose);

        String source = rb.getRAINBOWMetadataElement(nodeList, "id", verbose);

        String radarName = "";
        for (int i = 0; i < options.length; i++) {
            if (source.matches(options[i].getRadarName())) {
                radarName = options[i].getRadarId();
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

        cont.setXsize(xsize);
        cont.setYsize(ysize);
        cont.setXscale(res_x);
        cont.setYscale(res_y);
        cont.setLon(lon_1);
        cont.setLat(lat_1);
        cont.setAzAngle(azi);
        cont.setAngles(ele);
        cont.setRange(x_1);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "datamap", verbose);
        int dataBlobNumber = Integer.parseInt(rb.getRAINBOWMetadataElement(
                nodeList, "blobid", verbose));
        String depth = rb.getRAINBOWMetadataElement(nodeList, "depth", verbose);
        cont.setDataDepth(depth);
        int dataDepth = Integer.parseInt(depth);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "product", verbose);
        String version = rb.getRAINBOWMetadataElement(nodeList, "version",
                verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "beamwidth", verbose);
        String beamwidth = rb.getRAINBOWMetadataElement(nodeList, "", verbose);
        nodeList = rb.getRAINBOWNodesByName(inputDoc, "wavelen", verbose);
        String wavelength = rb.getRAINBOWMetadataElement(nodeList, "", verbose);

        cont.setStartepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setEndepochs(rb.convertRAINBOWDate2Epoch(date, time, verbose));
        cont.setSwVersion(version);
        cont.setBeamwidth(beamwidth);
        cont.setWavelength(wavelength);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "product", verbose);
        String product = rb
                .getRAINBOWMetadataElement(nodeList, "name", verbose);
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

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "radarpicture", verbose);
        String min = rb.getRAINBOWMetadataElement(nodeList, "min", verbose);
        String max = rb.getRAINBOWMetadataElement(nodeList, "max", verbose);
        String gain = rb.getRAINBOWGain(min, max, dataDepth);

        cont.setProduct(product);
        cont.setQuantity(datatype);
        cont.setGain(gain);
        cont.setOffset(min);

        // prepare actual dataset
        int width = Integer.parseInt(xsize);
        int height = Integer.parseInt(ysize);

        nodeList = rb.getRAINBOWNodesByName(inputDoc, "flagmap", verbose);
        int flagDepth = Integer.parseInt(rb.getRAINBOWMetadataElement(nodeList,
                "depth", verbose));
        cont.setFlagDepth(flagDepth);
        int flagBlobNumber = Integer.parseInt(rb.getRAINBOWMetadataElement(
                nodeList, "blobid", verbose));
        int firstBlob = rb.getMin(flagBlobNumber, dataBlobNumber);

        DataBufferContainer dataBuff = rb.getRainbowDataSection(fileBuff,
                dataBlobNumber, firstBlob, verbose);
        DataBufferContainer maskBuff = rb.getRainbowDataSection(fileBuff,
                flagBlobNumber, firstBlob, verbose);

        // Inflate radar data section and mask section
        int[][] infDataBuff = rb.inflate2DRAINBOWDataSection(dataBuff, width,
                height, verbose);
        byte[] infMaskBuff = rb.inflate1DRAINBOWDataSection(maskBuff, verbose);
        // Create range mask

        infDataBuff = rb.createRAINBOWMask(infDataBuff, width, height,
                infMaskBuff, flagDepth, verbose);

        if (isDirect) {

            ModelRHI_H5.createDescriptor(cont, rb, fileName, infDataBuff,
                    verbose);

        } else {

            // Save data buffer to file
            String dataDir = rb.hdf.createDirectory("data", verbose);
            String dataFileName = dataDir + File.separator + rb.H5_DATA_1
                    + ".dat";

            cont.setDataFileName(dataFileName);

            Document od = ModelRHI_XML.createDescriptor(cont, rb, verbose);

            rb.writeRAINBOWData(infDataBuff, dataFileName, verbose);

            // Save XML document in file
            rb.hdf.saveXMLFile(od, fileName, verbose);

        }
        return fileName;
    }

}
