/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;

import pl.imgw.odimH5.util.FTPApacheHandler;
import pl.imgw.odimH5.util.FTPContainer;
import pl.imgw.odimH5.util.FTPHandler;
import pl.imgw.odimH5.util.MessageLogger;
import pl.imgw.odimH5.util.OptionsHandler;
import pl.imgw.odimH5.util.UtSocketFactory;
/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTPHandlerTest {
    
    File file = new File("test-data", "T_PAGZ41_C_LZIB_20130313075500.hdf");
    
//    @Test
//    public void sentFileApacheFTPTest() {
//        
//        FTPApacheHandler ftp = new FTPApacheHandler();
//        assertTrue(ftp.sendFile(file, "PAS"));
//    }
    
    @Test
    public void multiplySendTest() {
        FTPHandler ftp;

        ftp = new FTPHandler();
        assertTrue(ftp.sendFile(file, "PAS"));

    }
    
    

}
