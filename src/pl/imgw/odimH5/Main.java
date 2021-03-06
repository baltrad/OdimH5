/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Ground Based Remote Sensing Department, Institute of Meteorology and Water Management
 *
 * OdimH5 is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * OdimH5 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License along with OdimH5.  If not, see <http://www.gnu.org/licenses/>
 * 
 * Maciej Szewczykowski, 2009
 * maciej.szewczykowski@imgw.pl
 * Łukasz Wojtas 2010
 * lukasz.wojtas@imgw.pl
 * 
 */

package pl.imgw.odimH5;

import java.io.File;

import pl.imgw.odimH5.controller.DataProcessorController;
import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.util.CommandLineArgsParser;
import pl.imgw.odimH5.util.LogsHandler;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.OptionsHandler;
import sun.misc.Cleaner;

/**
 * Main class containing the main() function called by the operating system upon
 * program start.
 * 
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class Main {

    //public static final String VERSION = "2.14b_linux64";
    public static final String ODIM_H5 = "OdimH5";
    
    // address separator
    public static final String ADDR_SEPARATOR = "/";

    /**
     * Main method creating
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        
        /*File file = new File(getProgPath());
        if (!file.exists()) {
            file.mkdirs();
        }*/
        
        LogsHandler.saveProgramLogs("System starts");
        
        cleanTmpFiles();

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
       
        OptionsHandler.getOpt().setMsgl(msgl);
        
        cont.startProcessor(args);
        

    }

    /*public static final String HOME = System.getProperty("user.home");

    
    public static String getProgPath() {
        return new File(HOME, ODIM_H5).getPath();
    }*/
    
    private static void cleanTmpFiles() {
        File folder = new File(AplicationConstans.TMP);
        for(File f : folder.listFiles()) {
            f.delete();
        }
    }
    
}
