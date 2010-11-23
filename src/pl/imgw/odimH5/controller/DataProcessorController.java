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

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;
import pl.imgw.odimH5.model.rainbow.ModelImage;
import pl.imgw.odimH5.model.rainbow.ModelPVOL;
import pl.imgw.odimH5.model.rainbow.ModelRHI;
import pl.imgw.odimH5.model.rainbow.ModelVP;
import pl.imgw.odimH5.model.rainbow531.Model531;
import pl.imgw.odimH5.model.rainbow531.Model531PVOL;
import pl.imgw.odimH5.util.BaltradLocalFeeder;
import pl.imgw.odimH5.util.CommandLineArgsParser;
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
    public final static String RAINBOW531_PLATFORM = "RAINBOW531";
    public final static String RAINBOW_PLATFORM = "RAINBOW";

    // Reference to DataProcessorModel object
    private DataProcessorModel proc;
    // Reference to RAINBOWModel object
    private Model rainbow;
    private Model531 rainbow531;
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
     */
    @SuppressWarnings("static-access")
    public void startProcessor(String[] args) {

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
                    verbose);

            // HDF5 file identifier
            int file_id = -1;
            // HDF5 file operation status
            @SuppressWarnings("unused")
            int status = -1;

            // Get a list of top-level nodes
            NodeList topLevelNodes = proc.getTopLevelNodes(proc
                    .parseDescriptor(cmd
                            .getArgumentValue(cmd.INPUT_FILE_OPTION), verbose));
            // Append root path to top-level nodes
            proc.appendRootPath(topLevelNodes);
            // Create new HDF5 file
            file_id = proc.H5Fcreate_wrap(cmd
                    .getArgumentValue(cmd.OUTPUT_FILE_OPTION),
                    HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT,
                    HDF5Constants.H5P_DEFAULT, verbose);
            // Create HDF5 file based on XML descriptor
            proc.H5FcreateFromXML(topLevelNodes, file_id, verbose);
            // Close HDF5 file
            status = proc.H5Fclose_wrap(file_id, verbose);

            msgl.showMessage("Conversion completed.", verbose);

        } else if (cmd.hasArgument(cmd.INPUT_FILE_OPTION)
                && cmd.hasArgument(cmd.FILE_OBJECT_OPTION)
                && cmd.hasArgument(cmd.PLATFORM_OPTION)) {

            String fileName = "";
            if (cmd.hasArgument(cmd.OUTPUT_FILE_OPTION))
                fileName = cmd.getArgumentValue(cmd.OUTPUT_FILE_OPTION);
            // else
            // fileName = cmd.getArgumentValue(cmd.INPUT_FILE_OPTION) + ".h5";

            msgl.showMessage("Conversion mode selected", verbose);

            if (doc == null) {
                OptionsHandler.exampleOptionXML();
                System.out.println("\nCreate and/or edit file "
                        + OptionsHandler.OPTION_XML_FILE);
                return;
            }

            RadarOptions[] options = OptionsHandler.getRadarOptions(doc);
            // Read input file
            byte[] fileBuff = proc.readDataFile(cmd
                    .getArgumentValue(cmd.INPUT_FILE_OPTION), verbose);
            // Data processing depending on platform type
            if (cmd.getArgumentValue(cmd.PLATFORM_OPTION).equals(
                    RAINBOW_PLATFORM)) {

                if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.PVOL)) {
                    ModelPVOL.createDescriptor(fileName, fileBuff, verbose,
                            rainbow, options);

                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.IMAGE)) {
                    ModelImage.createDescriptor(fileName, fileBuff, verbose,
                            rainbow, options);

                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.VP)) {
                    ModelVP.createDescriptor(fileName, fileBuff, verbose,
                            rainbow, options);

                } else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.RHI)) {
                    ModelRHI.createDescriptor(fileName, fileBuff, verbose,
                            rainbow, options);
                }

            } else if (cmd.getArgumentValue(cmd.PLATFORM_OPTION).equals(
                    RAINBOW531_PLATFORM)) {

                if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                        rainbow.PVOL)) {
                    Model531PVOL.createDescriptor(fileName, fileBuff, verbose,
                            rainbow531, options);

                }
                // else if (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                // rainbow.IMAGE)) {
                // ModelImage.createDescriptor(fileName, fileBuff, verbose,
                // rainbow, options);
                //
                // } else if
                // (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                // rainbow.VP)) {
                // ModelVP.createDescriptor(fileName, fileBuff, verbose,
                // rainbow, options);
                //
                // } else if
                // (cmd.getArgumentValue(cmd.FILE_OBJECT_OPTION).equals(
                // rainbow.RHI)) {
                // ModelRHI.createDescriptor(fileName, fileBuff, verbose,
                // rainbow, options);
                // }

            }

            // Other platforms will come here at a later time...
            msgl.showMessage("Descriptor preparation completed.", verbose);

        } else if (cmd.hasArgument(cmd.CONTINOUOS_OPTION)) {

            msgl.showMessage("Operational feeder mode selected", verbose);

            if (doc == null) {
                OptionsHandler.exampleOptionXML();
                System.out.println("\nCreate and/or edit file "
                        + OptionsHandler.OPTION_XML_FILE);
                return;
            }

            BaltradLocalFeeder worker = new BaltradLocalFeeder(doc, rainbow,
                    rainbow531, proc, verbose);
            worker.start();

        } else if (cmd.hasArgument(cmd.INPUT_FILE_OPTION)
                && cmd.hasArgument(cmd.ADDRESS_OPTION)
                && cmd.hasArgument(cmd.SENDER_OPTION)
                && cmd.hasArgument(cmd.RADAR_OPTION)) {

            msgl.showMessage("Sending file to server", verbose);

            BaltradFrameHandler bfh = new BaltradFrameHandler(cmd
                    .getArgumentValue(cmd.ADDRESS_OPTION));

            String a = bfh.createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                    cmd.getArgumentValue(cmd.SENDER_OPTION), cmd
                            .getArgumentValue(cmd.RADAR_OPTION), cmd
                            .getArgumentValue(cmd.INPUT_FILE_OPTION));

            // System.out.println("BFDataHdr:");
            // System.out.println(a);

            BaltradFrame bf = new BaltradFrame(a, cmd
                    .getArgumentValue(cmd.INPUT_FILE_OPTION));

            bfh.handleBF(bf);

        }
    }

    /**
     * Method returns reference to DataProcessorModel object.
     * 
     * @return Reference to DataProcessorModel object
     */
    public DataProcessorModel getDataProcessorModel() {
        return proc;
    }

    /**
     * Method sets reference to DataProcessorModel object.
     * 
     * @param proc
     *            Reference to DataProcessorModel object
     */
    public void setDataProcessorModel(DataProcessorModel proc) {
        this.proc = proc;
    }

    /**
     * Method returns reference to RAINBOWModel object.
     * 
     * @return Reference to RAINBOWModel object
     */
    public Model531 getRAINBOW531Model() {
        return rainbow531;
    }

    /**
     * Method sets reference to RAINBOWModel object.
     * 
     * @param rainbow
     *            Reference to RAINBOWModel object
     */
    public void setRAINBOW531Model(Model531 rainbow531) {
        this.rainbow531 = rainbow531;
    }

    /**
     * Method returns reference to RAINBOWModel object.
     * 
     * @return Reference to RAINBOWModel object
     */
    public Model getRAINBOWModel() {
        return rainbow;
    }

    /**
     * Method sets reference to RAINBOWModel object.
     * 
     * @param rainbow
     *            Reference to RAINBOWModel object
     */
    public void setRAINBOWModel(Model rainbow) {
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
