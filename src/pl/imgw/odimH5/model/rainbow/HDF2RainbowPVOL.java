/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.Deflater;

import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.PVOL_H5;
import pl.imgw.odimH5.util.RadarOptions;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class HDF2RainbowPVOL {

    private String ver = "5.26.5";
    private String type = "vol";
    private String owner = "rainbow";
    private String startangle = "startangle";
    private static final String STOPANGLE = "360";
    private static final double BMWIDTH_SBAND = 0.94;
    private static final double WVLENGTH_SBAND = 0.05309;
    private static final String UNITID_SI = "SI";

    private HashMap<String, String> volume;
    private HashMap<String, String> radarinfo;
    private HashMap<String, String> scan;
    private Vector<String> dataset;
    private Vector<H5Group> datasetG;
    private PVOLSlicesCont[] slices;
    private int size;

    private String date;
    private String time;
    private String radarName = "";
    private int[] nbins;
    private int[] nrays;

    private HDF5Model hdf;
    private RainbowModel rb;
    private RadarOptions[] options;
    private boolean verbose;
    private H5File inputFile;
    private String outputFileName;

    /**
     * 
     * @param outputFileName
     * @param inputFileName
     * @param verbose
     * @param rb
     * @param options
     */
    public HDF2RainbowPVOL(String outputFileName, String inputFileName,
            boolean verbose, RainbowModel rb, RadarOptions[] options) throws Exception {

        this.rb = rb;
        this.options = options;
        this.verbose = verbose;
        this.outputFileName = outputFileName;

        hdf = rb.getHDFModel();

        inputFile = hdf.openHDF5File(inputFileName);
        Group rootHDF = hdf.getHDF5RootGroup(inputFile, verbose);

        dataset = new Vector<String>();
        datasetG = new Vector<H5Group>();
        List<?> memberList = rootHDF.getMemberList();
        Iterator<?> itr = memberList.iterator();
        while (itr.hasNext()) {
            H5Group group = ((H5Group) itr.next());
            if (group.getName().contains("dataset")) {
                dataset.add(group.getName());
                datasetG.add(group);
            }
        }
        size = dataset.size();

        volume = makeVolAtributes(rootHDF);
        radarinfo = makeRadarinfo(rootHDF);

        slices = makeSlices(rootHDF);

        makeRayinfoData(360);

        makeXMLHead();

        hdf.closeHDF5File(inputFile);

    }

    private HashMap<String, String> makeVolAtributes(Group rootHDF) {

        HashMap<String, String> vol = new HashMap<String, String>();
        String d = hdf.getHDF5StringValue(rootHDF, rb.H5_WHAT, PVOL_H5.DATE,
                verbose);
        String t = hdf.getHDF5StringValue(rootHDF, rb.H5_WHAT, PVOL_H5.TIME,
                verbose);

        if (outputFileName.isEmpty()) {
            outputFileName = d + t + "00dBZ.vol";
        }

        date = d.substring(0, 4) + "-" + d.substring(4, 6) + "-"
                + d.substring(6, 8);
        time = t.substring(0, 2) + ":" + t.substring(2, 4) + ":"
                + t.substring(4, 6);

        vol.put(PVOL_Rainbow.DATETIME, date + "T" + time);
        vol.put(PVOL_Rainbow.TYPE, type);
        vol.put(PVOL_Rainbow.OWNER, owner);

        return vol;

    }

    private HashMap<String, String> makeRadarinfo(Group rootHDF) {
        HashMap<String, String> radar = new HashMap<String, String>();

        double alt = hdf.getHDF5DoubleValue(rootHDF, rb.H5_WHERE,
                PVOL_H5.HEIGHT, verbose);
        double lat = hdf.getHDF5DoubleValue(rootHDF, rb.H5_WHERE, PVOL_H5.LAT,
                verbose);
        double lon = hdf.getHDF5DoubleValue(rootHDF, rb.H5_WHERE, PVOL_H5.LON,
                verbose);
        double bmwidth = hdf.getHDF5DoubleValue(rootHDF, rb.H5_HOW,
                PVOL_H5.BEAMWIDTH, verbose);
        double wvlength = hdf.getHDF5DoubleValue(rootHDF, rb.H5_HOW,
                PVOL_H5.WAVELENGTH, verbose);
        String source = hdf.getHDF5StringValue(rootHDF, rb.H5_WHAT,
                PVOL_H5.SOURCE, verbose);

        if (bmwidth == 0) {
            bmwidth = BMWIDTH_SBAND;
        }
        if (wvlength == 0) {
            wvlength = WVLENGTH_SBAND;
        }

        for (int i = 0; i < options.length; i++) {
            if (source.contains(options[i].getRadarSourceName())) {
                radarName = options[i].getRadarName();

                break;
            }
        }

        if (radarName.isEmpty()) {
            System.out.println("Add name of radar number " + source
                    + " to options.xml");
            radarName = "bornholm";
        }

        radar.put(PVOL_Rainbow.ALT, String.valueOf(alt));
        radar.put(PVOL_Rainbow.LAT, String.valueOf(lat));
        radar.put(PVOL_Rainbow.LON, String.valueOf(lon));
        radar.put(PVOL_Rainbow.BEAMWIDTH, String.valueOf(bmwidth));
        radar.put(PVOL_Rainbow.WAVELEN, String.valueOf(wvlength));
        radar.put(PVOL_Rainbow.ID, radarName);

        return radar;
    }

    private PVOLSlicesCont[] makeSlices(Group rootHDF) {

        PVOLSlicesCont slices[] = new PVOLSlicesCont[size];
        this.nbins = new int[size];
        this.nrays = new int[size];

        for (int i = 0; i < size; i++) {

            slices[i] = new PVOLSlicesCont();

            double elangle = hdf.getHDF5DoubleLeafValue(rootHDF,
                    dataset.get(i), rb.H5_WHERE, PVOL_H5.ELANGLE, verbose);
            double rstart = hdf.getHDF5DoubleLeafValue(rootHDF, dataset.get(i),
                    rb.H5_WHERE, PVOL_H5.RSTART, verbose);
            double rscale = hdf.getHDF5DoubleLeafValue(rootHDF, dataset.get(i),
                    rb.H5_WHERE, PVOL_H5.RSCALE, verbose);

            rscale = rscale / 1000; // m to km

            int nbins = hdf.getHDF5IntLeafValue(rootHDF, dataset.get(i),
                    rb.H5_WHERE, PVOL_H5.NBINS, verbose);
            int nrays = hdf.getHDF5IntLeafValue(rootHDF, dataset.get(i),
                    rb.H5_WHERE, PVOL_H5.NRAYS, verbose);
            
            this.nbins[i] = nbins;
            this.nrays[i] = nrays;

            int a1gate = hdf.getHDF5IntLeafValue(rootHDF, dataset.get(i),
                    rb.H5_WHERE, PVOL_H5.A1GATE, verbose);

            double gain = hdf.getHDF5DoubleLeafValue(datasetG.get(i),
                    rb.H5_DATA_1, rb.H5_WHAT, PVOL_H5.GAIN, verbose);
            double offset = hdf.getHDF5DoubleLeafValue(datasetG.get(i),
                    rb.H5_DATA_1, rb.H5_WHAT, PVOL_H5.OFFSET, verbose);

            double stoprange = nbins * rscale;

            slices[i].slice.put(PVOL_Rainbow.POSANGLE, String.valueOf(elangle));
            slices[i].slice.put(PVOL_Rainbow.STOPRANGE,
                    String.valueOf(stoprange));
            slices[i].slice
                    .put(PVOL_Rainbow.STARTANGLE, String.valueOf(rstart));
            slices[i].slice.put(PVOL_Rainbow.STOPANGLE, STOPANGLE);
            slices[i].slice.put(PVOL_Rainbow.RANGESTEP, String.valueOf(rscale));
            slices[i].slice.put(PVOL_Rainbow.RANGESAMP, "4");

            slices[i].rayinfo.put(PVOL_Rainbow.BLOBID, String.valueOf(i * 2));
            slices[i].rayinfo.put(PVOL_Rainbow.RAYS, String.valueOf(nrays));
            slices[i].rayinfo.put(PVOL_Rainbow.DEPTH, "16");

            slices[i].rawdata.put(PVOL_Rainbow.BLOBID,
                    String.valueOf(i * 2 + 1));
            slices[i].rawdata.put(PVOL_Rainbow.RAYS, String.valueOf(nrays));
            slices[i].rawdata.put(PVOL_Rainbow.TYPE, "dBZ");
            slices[i].rawdata.put(PVOL_Rainbow.BINS, String.valueOf(nbins));
            slices[i].rawdata.put(PVOL_Rainbow.MIN, "-31.5");
            slices[i].rawdata.put(PVOL_Rainbow.MAX, "95.5");
            slices[i].rawdata.put(PVOL_Rainbow.DEPTH, "8");

            String path = rb.H5_ROOT + dataset.get(i) + "/" + rb.H5_DATA_1
                    + "/" + rb.H5_DATA;
            int[][] dataset = hdf.getHDF5Dataset(inputFile, path, nrays, nbins,
                    verbose);

            dataset = hdf.transposeArray(dataset, nbins, nrays);

            slices[i].setDatasetFromHdf(dataset);

        }

        return slices;

    }

    private void makeXMLHead() {
        // Create XML document object
        Document od = rb.hdf.createXMLDocumentObject(verbose);

        Element root = od.createElement(PVOL_Rainbow.VOLUME);
        root.setAttribute(PVOL_Rainbow.VERSION, ver);
        root.setAttribute(PVOL_Rainbow.DATETIME, date + "T" + time);
        root.setAttribute(PVOL_Rainbow.TYPE, type);
        root.setAttribute(PVOL_Rainbow.OWNER, owner);
        od.appendChild(root);

        // <radarinfo>
        Element rinfoTag = od.createElement(PVOL_Rainbow.RADARINFO);
        rinfoTag.setAttribute(PVOL_Rainbow.ALT, radarinfo.get(PVOL_Rainbow.ALT));
        rinfoTag.setAttribute(PVOL_Rainbow.LON, radarinfo.get(PVOL_Rainbow.LON));
        rinfoTag.setAttribute(PVOL_Rainbow.LAT, radarinfo.get(PVOL_Rainbow.LAT));
        rinfoTag.setAttribute(PVOL_Rainbow.ID, radarinfo.get(PVOL_Rainbow.ID));
        rinfoTag.appendChild(rb.makeTag(PVOL_Rainbow.NAME, radarName, od));
        rinfoTag.appendChild(rb.makeTag(PVOL_Rainbow.BEAMWIDTH,
                radarinfo.get(PVOL_Rainbow.BEAMWIDTH), od));
        rinfoTag.appendChild(rb.makeTag(PVOL_Rainbow.WAVELEN,
                radarinfo.get(PVOL_Rainbow.WAVELEN), od));
        root.appendChild(rinfoTag);

        // <scan>
        Element scanTag = od.createElement(PVOL_Rainbow.SCAN);
        scanTag.setAttribute(PVOL_Rainbow.NAME, radarinfo.get(PVOL_Rainbow.ID)
                + "." + type);
        scanTag.setAttribute(PVOL_Rainbow.TIME, time);
        scanTag.setAttribute(PVOL_Rainbow.DATE, date);
        root.appendChild(scanTag);

        // <unitid>
        scanTag.appendChild(rb.makeTag(PVOL_Rainbow.UNITID, UNITID_SI, od));
        // <pargroup>
        Element pargroup = od.createElement(PVOL_Rainbow.PARGROUP);
        pargroup.setAttribute(PVOL_Rainbow.REFID, PVOL_Rainbow.SDFBASE);
        pargroup.appendChild(rb.makeTag(PVOL_Rainbow.NUMELE,
                String.valueOf(size), od));
        scanTag.appendChild(pargroup);

        // <slice>
        for (int i = 0; i < size; i++) {
            Element sliceTag = od.createElement(PVOL_Rainbow.SLICE);
            sliceTag.setAttribute(PVOL_Rainbow.REFID, String.valueOf(i));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.POSANGLE,
                    slices[i].slice.get(PVOL_Rainbow.POSANGLE), od));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.STOPRANGE,
                    slices[i].slice.get(PVOL_Rainbow.STOPRANGE), od));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.STARTANGLE,
                    slices[i].slice.get(PVOL_Rainbow.STARTANGLE), od));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.STOPANGLE,
                    slices[i].slice.get(PVOL_Rainbow.STOPANGLE), od));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.RANGESTEP,
                    slices[i].slice.get(PVOL_Rainbow.RANGESTEP), od));

            sliceTag.appendChild(rb.makeTag(PVOL_Rainbow.RANGESAMP,
                    slices[i].slice.get(PVOL_Rainbow.RANGESAMP), od));

            Element sliceDataTag = od.createElement(PVOL_Rainbow.SLICEDATA);
            sliceDataTag.setAttribute(PVOL_Rainbow.TIME, time);
            sliceDataTag.setAttribute(PVOL_Rainbow.DATE, date);

            Element rayinfoTag = od.createElement(PVOL_Rainbow.RAYINFO);
            rayinfoTag.setAttribute(PVOL_Rainbow.REFID, startangle);
            rayinfoTag.setAttribute(PVOL_Rainbow.BLOBID,
                    slices[i].rayinfo.get(PVOL_Rainbow.BLOBID));
            rayinfoTag.setAttribute(PVOL_Rainbow.RAYS,
                    slices[i].rayinfo.get(PVOL_Rainbow.RAYS));
            rayinfoTag.setAttribute(PVOL_Rainbow.DEPTH,
                    slices[i].rayinfo.get(PVOL_Rainbow.DEPTH));
            sliceDataTag.appendChild(rayinfoTag);

            Element rawdataTag = od.createElement(PVOL_Rainbow.RAWDATA);
            rawdataTag.setAttribute(PVOL_Rainbow.BLOBID,
                    slices[i].rawdata.get(PVOL_Rainbow.BLOBID));
            rawdataTag.setAttribute(PVOL_Rainbow.RAYS,
                    slices[i].rawdata.get(PVOL_Rainbow.RAYS));
            rawdataTag.setAttribute(PVOL_Rainbow.TYPE,
                    slices[i].rawdata.get(PVOL_Rainbow.TYPE));
            rawdataTag.setAttribute(PVOL_Rainbow.BINS,
                    slices[i].rawdata.get(PVOL_Rainbow.BINS));
            rawdataTag.setAttribute(PVOL_Rainbow.MIN,
                    slices[i].rawdata.get(PVOL_Rainbow.MIN));
            rawdataTag.setAttribute(PVOL_Rainbow.MAX,
                    slices[i].rawdata.get(PVOL_Rainbow.MAX));
            rawdataTag.setAttribute(PVOL_Rainbow.DEPTH,
                    slices[i].rawdata.get(PVOL_Rainbow.DEPTH));
            sliceDataTag.appendChild(rawdataTag);

            sliceTag.appendChild(sliceDataTag);

            scanTag.appendChild(sliceTag);

        }
        Comment comment = od.createComment(" END XML ");
        od.appendChild(comment);
        rb.hdf.saveXMLFile(od, outputFileName, verbose);

        for (int i = 0; i < size; i++) {
            saveData(outputFileName, makeRayinfoData(nrays[i]), i * 2, "qt");
            saveData(
                    outputFileName,
                    getByteArray(slices[i].getDatasetFromHdf(), nbins[i],
                            nrays[i]), i * 2 + 1, "qt");

        }
        

    }

    private byte[] getByteArray(int[][] dataArray, int x, int y) {
        byte[] output = new byte[x * y];
        int index = 0;
        for (int i = 0; i < x; i++)
            for (int j = 0; j < y; j++) {
                output[index] = (byte) dataArray[i][j];
                index++;
            }

        return output;
    }

    /**
     * 
     * @param fileName
     * @param data
     * @param blobNumber
     * @param size
     * @param compresion
     */
    private void saveData(String fileName, byte[] data, int blobNumber,
            String compresion) {

        // Create the compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compresion = "qt";
        // Give the compressor the data to compress
        compressor.setInput(data);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // You cannot use an array that's the same size as the orginal because
        // there is no guarantee that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        // Compress the data
        byte[] buf = new byte[1024];

        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get the compressed data
        byte[] compressedData = bos.toByteArray();

        int orgsize = data.length;
        int compsize = compressedData.length + 4;
        // System.out.println("orgsize: " + orgsize);
        // System.out.println("compsize: " + compsize);
        String beg = "<BLOB blobid=\"" + blobNumber + "\" size=\"" + compsize
                + "\" compression=\"" + compresion + "\">";
        String end = "</BLOB>";

        try {
            RandomAccessFile rf = new RandomAccessFile(fileName, "rw");
            FileChannel fc = rf.getChannel();
            fc.position(fc.size());
            fc.write(ByteBuffer.wrap((beg + "\n").getBytes()));

            byte[] bytes = ByteBuffer.allocate(4).putInt(orgsize).array();
            fc.write(ByteBuffer.wrap(bytes));
            fc.write(ByteBuffer.wrap(compressedData));
            // fc.write(ByteBuffer.allocate(1).put((byte) 0x0a));
            fc.write(ByteBuffer.wrap(("\n" + end + "\n").getBytes()));
            fc.close();
            rf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private byte[] makeRayinfoData(int size) {
        if (size == 0)
            size = 360;
        
        byte[] array = new byte[size * 2];

        int step = 182;
        if (size < 360)
            step = 65535 / size;
        
        for (int i = 0; i < size; i++) {

            int a = (i % 360) * step + 1;
            byte[] bytes = ByteBuffer.allocate(2).putChar((char) a).array();
            array[i * 2] = bytes[0];
            array[i * 2 + 1] = bytes[1];
        }
        return array;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getRadarName() {
        return radarName;
    }
    
    public static void main(String[] args) {
        String input = "/home/lwojtas/poligon/vol/baltrad/dk.h5";
        RadarOptions[] options = new RadarOptions[]{ };
        RainbowModel rb = new RainbowModel();
        rb.setHDFModel(new HDF5Model());
        try {
            HDF2RainbowPVOL hdf = new HDF2RainbowPVOL("dk.vol", input, true, rb, options);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
