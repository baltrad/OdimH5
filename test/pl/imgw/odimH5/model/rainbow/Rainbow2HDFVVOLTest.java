/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.util.MessageLogger;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class Rainbow2HDFVVOLTest {
    
    byte[] file_buf;
    RainbowModel rb = new RainbowModel();
    {
        MessageLogger msg = new MessageLogger();
        rb.setMessageLogger(msg);
        HDF5Model hdf = new HDF5Model();
        hdf.setMessageLogger(msg);
        rb.setHDFModel(hdf);
    }
    
    @Before
    public void setUp() throws Exception {
        File file = new File("test-data/2013093006434000V.vol");
        int file_len = (int) file.length();
        file_buf = new byte[file_len];
        
        FileInputStream fis = new FileInputStream(file);
        fis.read(file_buf, 0, file_len);
        fis.close();
    }
    
    
    @Test
    public void shouldConstructRainbow2HDFVVOL1() throws Exception {

        Rainbow2HDFVVOL dbzvol = new Rainbow2HDFVVOL("", file_buf, true, rb, true);
        assertNotNull(dbzvol);
        dbzvol.makeH5();
        String name = dbzvol.getOutputFileName();
        File testFile = new File(name);
        assertTrue(testFile.exists());
//        testFile.delete();
        
        
    }

    /**
     * Test method for {@link pl.imgw.odimH5.model.rainbow.Rainbow2HDFVVOL#Rainbow2HDFVVOL(java.lang.String, byte[], boolean, pl.imgw.odimH5.model.rainbow.RainbowModel)}.
     */
    @Test @Ignore
    public void shouldConstructRainbow2HDFVVOL2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link pl.imgw.odimH5.model.rainbow.Rainbow2HDFVVOL#makeH5()}.
     */
    @Test @Ignore
    public void testMakeH5() {
        fail("Not yet implemented");
    }

}
