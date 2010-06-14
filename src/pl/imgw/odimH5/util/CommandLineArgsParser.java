/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;

/**
 * Class implementing command line arguments parser functionality.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("static-access")
public class CommandLineArgsParser {

    // Constants
    public final static String FILE_OBJECT_OPTION = "f";
    public final static String PLATFORM_OPTION = "p";
    public final static String INPUT_FILE_OPTION = "i";
    public final static String OUTPUT_FILE_OPTION = "o";
    // public final static String DESCRIPTOR_OPTION = "d";
    public final static String VERBOSE_OPTION = "v";
    public final static String HELP_OPTION = "h";
    public final static String CONTINOUOS_OPTION = "c";
    public final static String ADDRESS_OPTION = "a";
    public final static String SENDER_OPTION = "s";
    public final static String RADAR_OPTION = "r";

    private final static String START_COMMAND = "java -jar odimH5.jar -i <descriptor_file : "
            + "data_file> -f <object> -p <platform> -o <output_file> -c <file_format> " 
            + "-a <address> -s <sender> -r <radar> [-v] [-h]";

    private final static String FILE_OBJECT_DESCRIPTION = "ODIM_H5 file object option\n"
            + "<arg>\n PVOL: polar volume\n CVOL: carthesian volume\n SCAN polar scan\n"
            + "RAY: single polar ray\n AZIM: azimuthal object\n IMAGE: 2-D cartesian image\n"
            + "COMP: cartesian composite image(s)\n XSEC: 2-D vertical cross section(s)\n"
            + "VP: 1-D vertical profile\n PIC: embedded graphical image";

    private final static String PLATFORM_DESCRIPTION = "processing software option\n"
            + "<arg>\n CASTOR: Météo France’s system\n EDGE: EEC Edge\n"
            + "FROG: Gamic FROG, MURAN...\n IRIS: Sigmet IRIS\n NORDRAD: NORDRAD\n"
            + "RADARNET: UKMO’s system\n RAINBOW: Gematronik Rainbow";

    private final static String INPUT_FILE_DESCRIPTION = "input file option\n"
            + "<arg>\n input file's path";

    private final static String OUTPUT_FILE_DESCRIPTION = "output file option\n"
            + "<arg>\n output file's path";

    private final static String CONTINOUOS_DESCRIPTION = "Baltrad feeder continuous work mode\n"
            + "<arg>\n RVOL: rainbow volume file\n H5: hdf5 file\n";

    private final static String ADDRESS_DESCRIPTION = "send file to http server\n"
            + "<arg>\n server address\n";

    private final static String SENDER_DESCRIPTION = "sender\n"
        + "<arg>\n sender name\n";

    private final static String RADAR_DESCRIPTION = "radar name\n"
            + "<arg>\n radar name\n";

    private final static String VERBOSE_DESCRIPTION = "verbose mode option";

    private final static String HELP_DESCRIPTION = "print this message";

    // Command line options
    private static Options options = null;
    // Command line arguments
    private static CommandLine cmd = null;

    // Create static options object
    static {
        options = new Options();

        Option file_object = OptionBuilder.withArgName(FILE_OBJECT_OPTION)
                .withArgName("arg").hasArg().withDescription(
                        FILE_OBJECT_DESCRIPTION).create(FILE_OBJECT_OPTION);

        Option platform = OptionBuilder.withArgName(PLATFORM_OPTION)
                .withArgName("arg").hasArg().withDescription(
                        PLATFORM_DESCRIPTION).create(PLATFORM_OPTION);

        Option input_file = OptionBuilder.withArgName(INPUT_FILE_OPTION)
                .withArgName("arg").hasArg().withDescription(
                        INPUT_FILE_DESCRIPTION).create(INPUT_FILE_OPTION);

        Option output_file = OptionBuilder.withArgName(OUTPUT_FILE_OPTION)
                .withArgName("arg").hasArg().withDescription(
                        OUTPUT_FILE_DESCRIPTION).create(OUTPUT_FILE_OPTION);

        Option continuous = OptionBuilder.withArgName(CONTINOUOS_OPTION)
                .withArgName("arg").hasArg().withDescription(
                        CONTINOUOS_DESCRIPTION).create(CONTINOUOS_OPTION);

        Option address = OptionBuilder.withArgName(ADDRESS_OPTION).withArgName(
                "arg").hasArg().withDescription(ADDRESS_DESCRIPTION).create(
                ADDRESS_OPTION);

        Option sender = OptionBuilder.withArgName(SENDER_OPTION).withArgName(
        "arg").hasArg().withDescription(SENDER_DESCRIPTION).create(
                SENDER_OPTION);
        
        Option radar = OptionBuilder.withArgName(RADAR_OPTION).withArgName(
                "arg").hasArg().withDescription(RADAR_DESCRIPTION).create(
                RADAR_OPTION);

        // Option descriptor = OptionBuilder.withArgName(DESCRIPTOR_OPTION)
        // .withDescription(DESCRIPTOR_DESCRIPTION).create(DESCRIPTOR_OPTION);

        Option verbose = OptionBuilder.withArgName(VERBOSE_OPTION)
                .withDescription(VERBOSE_DESCRIPTION).create(VERBOSE_OPTION);

        Option help = OptionBuilder.withArgName(HELP_OPTION).withDescription(
                HELP_DESCRIPTION).create(HELP_OPTION);

        options.addOption(file_object);
        options.addOption(platform);
        options.addOption(input_file);
        options.addOption(output_file);
        options.addOption(continuous);
        options.addOption(address);
        options.addOption(sender);
        options.addOption(radar);
        options.addOption(verbose);
        options.addOption(help);
    }

    /**
     * Method parses command line parameters.
     * 
     * @param args
     *            Command line parameters
     */
    public void parseCommandLineArgs(String[] args) {

        Parser parser = new PosixParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelpAndExit(1, START_COMMAND, options);
        }

        // Help mode is chosen
        if (cmd.hasOption(HELP_OPTION)) {
            printHelpAndExit(1, HELP_OPTION, options);
        }
        // Descriptor file generation mode (4 arguments) or conversion mode (2
        // arguments )
        // is chosen
        if (!(cmd.hasOption(INPUT_FILE_OPTION)
                && cmd.hasOption(OUTPUT_FILE_OPTION) ||

                cmd.hasOption(INPUT_FILE_OPTION)
                && cmd.hasOption(FILE_OBJECT_OPTION)
                && cmd.hasOption(PLATFORM_OPTION)
                && cmd.hasOption(OUTPUT_FILE_OPTION)
                
                || cmd.hasOption(CONTINOUOS_OPTION)

                || cmd.hasOption(ADDRESS_OPTION)
                && cmd.hasOption(SENDER_OPTION) &&
                cmd.hasOption(RADAR_OPTION))) {
            printHelpAndExit(1, START_COMMAND, options);
        }
    }

    /**
     * Method checks if a given command line argument is provided
     * 
     * @param option
     *            Option name
     * @return True if argument is provided, false otherwise
     */
    public boolean hasArgument(String option) {
        if (cmd.hasOption(option))
            return true;
        else
            return false;
    }

    /**
     * Method returns command line argument value
     * 
     * @param option
     *            Option name
     * @return Command line argument value
     */
    public String getArgumentValue(String option) {
        return cmd.getOptionValue(option);
    }

    /**
     * Method prints help information screen and terminates program.
     * 
     * @param exitCode
     *            Program exit code
     * @param startCommand
     *            Program launch command
     * @param options
     *            Command line parameters
     */
    public void printHelpAndExit(int exitCode, String startCommand,
            Options options) {
        // Help formatter
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(startCommand, options);
        System.exit(exitCode);
    }

}
