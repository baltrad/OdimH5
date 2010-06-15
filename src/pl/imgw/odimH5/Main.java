/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5;

import pl.imgw.odimH5.controller.DataProcessorController;
import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;
import pl.imgw.odimH5.util.CommandLineArgsParser;
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

    public static final String VERSION = "2.6";
    
    /**
     * Main method creating
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        DataProcessorModel proc = new DataProcessorModel();
        Model rainbow = new Model();
        CommandLineArgsParser cmd = new CommandLineArgsParser();
        MessageLogger msgl = new MessageLogger();
        DataProcessorController cont = new DataProcessorController();
        rainbow.setDataProcessorModel(proc);
        rainbow.setMessageLogger(msgl);
        proc.setMessageLogger(msgl);
        cont.setMessageLogger(msgl);
        cont.setCmdParser(cmd);
        cont.setDataProcessorModel(proc);
        cont.setRAINBOWModel(rainbow);
        cont.startProcessor(args);
        
    }

}
