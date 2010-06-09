/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class implementing message logging functionality.
 *
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class MessageLogger {

    private final static String DATE_FORMAT_STRING = "[hh:mm:ss.SSSS] ";

    /**
     * Method prints log message on the screen
     *
     * @param message The message to display
     * @param show Messages are logged if this parameter is true
     */
    public void showMessage( String message, boolean show ) {
        if( show ) {
            SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT_STRING );
            Date date = new Date();
            String dateString = dateFormat.format( date );
            System.out.println( dateString + message );
        }
    }

}
