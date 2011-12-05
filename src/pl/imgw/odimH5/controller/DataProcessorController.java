/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.controller;

import ncsa.hdf.hdf5lib.HDF5Constants;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
import pl.imgw.odimH5.model.rainbow.ModelImage;
import pl.imgw.odimH5.model.rainbow.ModelRHI;
import pl.imgw.odimH5.model.rainbow.ModelVP;
import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.util.CommandLineArgsParser;
import pl.imgw.odimH5.util.LocalFeeder;
import pl.imgw.odimH5.util.LogsHandler;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.RadarOptions;
import eu.baltrad.frame.model.BaltradFrame;
import eu.baltrad.frame.model.BaltradFrameHandler;

/**
 * Controller class for data processing routines.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class DataProcessorController {

    // Constants
    public final static String RAINBOW_PLATFORM = "RAINBOW";

    // Reference to DataProcessorModel object
    private HDF5Model hdf;
    // Reference to RAINBOWModel object
    private RainbowModel rainbow;
    // Reference to CommandLineArgsParser object
    private CommandLineArgsParser cmd;
    // Reference to MessageLogger object
    private MessageLogger msgl;

    // Variables
    boolean verbose;

    /**
     * Data processing control method
     * 
     * @param args
     *            Command line arguments
     * @throws Exception
     */
    @SuppressWarnings("static-access")
    public void startProcessor(String[] args) throws Exception {

        // Parse command line arguments
        cmd.parseCommandLineArgs(args);

        // Check if verbose mode is chosen
        verbose = cmd.hasArgument(cmd.VERBOSE_OPTION) ? true : false;

        // Load options
        Document doc = OptionsHandler.loadOptions(msgl, verbose);

        // Select operation mode depending on the command line arguments
        // provided
        if (cmd.hasArgument(cmd.INPUT_FILE_OPTION)
                && cmd.hasArgument(cmd.OUTPUT_FILE_OPTION)
                && !cmd.hasArgument(cmd.FILE_OBJECT_OPTION)
                && !cmd.hasArgument(cmd.PLATFORM_OPTION)) {

            msgl.showMessage("Conversion from xml descriptor mode selected",
                    true);

            // HDF5 file identifier
            int file_id = -1;
            // HDF5 file operation status
            @SuppressWarnings("unused")
            int status = -1;

            // Get a list of top-level nodes
            NodeList topLevelNodes = hdf.getTopLevelNodes(hdf.parseDescriptor(
                    cmd.getArgumentValue(cmd.INPUT_FILE_OPTION), verbose));
            // Append root path to top-level nodes
            hdf.appendRootPath(topLevelNodes);
            // Create new HDF5 file
            file_id = hdf.H5Fcreate_wrap(
                    cmd.getArgumentValue(cmd.OUTPUT_FILE_OPTION),
                    HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT,
                    HDF5Constants.H5P_DEFAULT, verbose);
            // Create HDF5 file based on XML descriptor
            hdf.H5FcreateFromXML(topLevelNodes, file_id, verbose);
            // Close HDF5 file
            status = hdf.H5Fclose_wrap(file_id, verbose);

            msgl.showMessage("Conversion completed.", true);

        } else if (cmd.hasArgument(cmd.INPUT_FILE_OPTION)
                && cmd.hasArgument(cmd.FILE_OBJECT_OPTION)
                && cmd.hasArgument(cmd.PLATFORM_OPTION)) {

            String fileNameOut = "";
            String fileNameIn = cmd.getArgumentValue(cmd.INPUT_FILE_OPTION);

            if (cmd.hasArgument(cmd.OUTPUT_FILE_OPTION))
                fileNameOut = cmd.getArgumentValue(cmd.OUTPUT_FILE_OPTION);
            // else
            // fileName = cmd.getArgumentValue(cmd.INPUT_FILE_OPTION) + ".h5";

            msgl.showMessage("Conversion mode selected", true);

            if (doc == null) {
                OptionsHandler.exampleOptionXML();
                System.out.println("\nCreate and/or edit file "
                        + OptionsHandler.OPTION_XML_FILE);
                return;
            }

            RadarOptions[] options = OptionsHandler.getRadarOptions(doc);
            // Read input file
            byte[] fileBuff = hdf.readDataFile(
                    cmd.getArgumentValue(cmd.INPUT_FILE_OPTION), verbose);

            // Data processing depending on platform type
            if (cmd.getArgumentValue(cmd.PLATFORM_OPTION).equals(
                    RAINBOW_PLATFORM)) {

                if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.PVOL)) {

                    // only input files with ".vol" extention will be accepted
                    if (fileNameIn.endsWith("vol")) {

                        Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL(fileNameOut,
                                fileBuff, verbose, rainbow, options);

                        if (!vol.correct) {
                            return;
                        }

                        if (vol.getOutputFileName().endsWith("hdf")
                                || vol.getOutputFileName().endsWith("h5"))
                            vol.makeH5();
                        else
                            vol.makeXML();
                    } else if (fileNameIn.endsWith("h5")
                            || fileNameIn.endsWith("hdf")) {

                        //no single convertion from hdf to vol handle so far
                        HDF2RainbowPVOL hdf = new HDF2RainbowPVOL(fileNameOut,
                                fileNameIn, verbose, rainbow, options);

                    }
                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.IMAGE)) {
                    fileNameOut = ModelImage.createDescriptor(fileNameOut,
                            fileBuff, verbose, rainbow, options);

                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.VP)) {
                    fileNameOut = ModelVP.createDescriptor(fileNameOut,
                            fileBuff, verbose, rainbow, options);

                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.RHI)) {
                    fileNameOut = ModelRHI.createDescriptor(fileNameOut,
                            fileBuff, verbose, rainbow, options);
                }

            }

            // Other platforms will come here at a later time...
            if (!fileNameOut.isEmpty())
                msgl.showMessage("Descriptor preparation completed.", true);

        } else if (cmd.hasArgument(cmd.CONTINOUOS_OPTION)) {

            msgl.showMessage("Operational feeder mode selected", true);

            if (doc == null) {
                OptionsHandler.exampleOptionXML();
                System.out.println("\nCreate and/or edit file "
                        + OptionsHandler.OPTION_XML_FILE);
                return;
            }

            LocalFeeder worker = new LocalFeeder(doc, rainbow, hdf, msgl,
                    verbose);
            try {
                worker.start();
            } catch (Exception e) {
                LogsHandler.saveProgramLogs(e.getMessage());
                e.printStackTrace();
            }

        } else if (cmd.hasArgument(cmd.INPUT_FILE_OPTION)
                && cmd.hasArgument(cmd.HOST_ADDRESS_OPTION)
                && cmd.hasArgument(cmd.SENDER_OPTION)
                && cmd.hasArgument(cmd.RADAR_OPTION)) {

            msgl.showMessage("Sending file to server", true);

            BaltradFrameHandler bfh = new BaltradFrameHandler(
                    cmd.getArgumentValue(cmd.HOST_ADDRESS_OPTION));

            String a = bfh.createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                    cmd.getArgumentValue(cmd.SENDER_OPTION),
                    cmd.getArgumentValue(cmd.RADAR_OPTION),
                    cmd.getArgumentValue(cmd.INPUT_FILE_OPTION));

            // System.out.println("BFDataHdr:");
            // System.out.println(a);

            BaltradFrame bf = new BaltradFrame(a,
                    cmd.getArgumentValue(cmd.INPUT_FILE_OPTION));

            bfh.handleBF(bf);

        }
    }

    /**
     * Method returns reference to DataProcessorModel object.
     * 
     * @return Reference to DataProcessorModel object
     */
    public HDF5Model getHDFModel() {
        return hdf;
    }

    /**
     * Method sets reference to DataProcessorModel object.
     * 
     * @param proc
     *            Reference to DataProcessorModel object
     */
    public void setHDFModel(HDF5Model proc) {
        this.hdf = proc;
    }

    /**
     * Method returns reference to RAINBOWModel object.
     * 
     * @return Reference to RAINBOWModel object
     */
    public RainbowModel getRAINBOWModel() {
        return rainbow;
    }

    /**
     * Method sets reference to RAINBOWModel object.
     * 
     * @param rainbow
     *            Reference to RAINBOWModel object
     */
    public void setRAINBOWModel(RainbowModel rainbow) {
        this.rainbow = rainbow;
    }

    /**
     * Method returns reference to CommandLineArgsParser object.
     * 
     * @return Reference to CommandLineArgsParser object
     */
    public CommandLineArgsParser getCmdParser() {
        return cmd;
    }

    /**
     * Method sets reference to CommandLineArgsParser object.
     * 
     * @param cmd
     *            Reference to CommandLineArgsParser object
     */
    public void setCmdParser(CommandLineArgsParser cmd) {
        this.cmd = cmd;
    }

    /**
     * Method returns reference to MessageLogger object.
     * 
     * @return Reference to MessageLogger object
     */
    public MessageLogger getMessageLogger() {
        return msgl;
    }

    /**
     * Method sets reference to MessageLogger object
     * 
     * @param msgl
     *            Reference to MessageLogger object
     */
    public void setMessageLogger(MessageLogger msgl) {
        this.msgl = msgl;
    }

}