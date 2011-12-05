/**
 * (C) 2011 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import ncsa.hdf.object.h5.H5File;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.PictureFromArray;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class HDF2PNG {

    private HDF5Model hdf;

    public HDF2PNG(String inputFileName, boolean verbose) {

        HDF5Model hdf = new HDF5Model();
        MessageLogger msgl = new MessageLogger();
        hdf.setMessageLogger(msgl);
        H5File inputFile;
        inputFile = hdf.openHDF5File(inputFileName);

        String path = "/dataset1/data1/data";

        int[][] dataset = hdf.getHDF5Dataset(inputFile, path, 1900, 2200, true);
        saveImage(dataset, ColorScales.getODCScale(),
                inputFileName.replace("hdf", "png"));

    }

    private static void saveImage(int[][] data, Set<MapColor> scale,
            String title) {

        int w = data.length;
        int h = data[0].length;

        // Color rgb = new Color(200, 200, 200);
        // Color rgb2 = new Color(127, 127, 127);
        // Image reflectivity = Transparency.makeColorTransparent(pic.getImg(),
        // rgb, rgb2);
        // // reflectivity = Transparency.makeColorTransparent(pic.getImg(),
        // rgb2);
        //
        String path = "layer3.png";

        // load source images
        BufferedImage overlay;

        try {
            overlay = ImageIO.read(new File(path));

            byte[][] array = new byte[overlay.getWidth()][overlay.getHeight()];

            PictureFromArray pic = new PictureFromArray(data, scale, overlay);

            // Save as PNG
            File file = new File("newimage.png");
            ImageIO.write(pic.getImg(), "png", file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

    }

}
