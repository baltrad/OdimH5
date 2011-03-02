/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5;

import pl.imgw.odimH5.controller.DataProcessorController;
import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.util.CommandLineArgsParser;
import pl.imgw.odimH5.util.LogsHandler;
import pl.imgw.odimH5.util.MessageLogger;

/**
 * Main class containing the main() function called by the operating system upon
 * program start.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class Main {

    public static final String VERSION = "2.14";
    
    /**
     * Main method creating
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        LogsHandler.saveProgramLogs("System starts");
        
        HDF5Model proc = new HDF5Model();
        RainbowModel rainbow = new RainbowModel();
        CommandLineArgsParser cmd = new CommandLineArgsParser();
        MessageLogger msgl = new MessageLogger();
        DataProcessorController cont = new DataProcessorController();
                
        rainbow.setHDFModel(proc);
        rainbow.setMessageLogger(msgl);
        proc.setMessageLogger(msgl);
        cont.setMessageLogger(msgl);
        cont.setCmdParser(cmd);
        cont.setHDFModel(proc);
        cont.setRAINBOWModel(rainbow);
        cont.startProcessor(args);
        
    }

}
