/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import java.util.HashMap;

import pl.imgw.odimH5.util.DataBufferContainer;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class BlobHandler {
    
    final static char[] blobStart = "<BLOB ".toCharArray();
    final static char[] blobEnd = "</BLOB>".toCharArray();
    final static char[] blobId = "blobid=\"".toCharArray();
    
    public HashMap<Integer, DataBufferContainer> getBlobs(byte[] fileBuff,
            int depth, boolean verbose) {
        
        HashMap<Integer, DataBufferContainer> blobs = null;
        DataBufferContainer dataBuff = new DataBufferContainer();
        
        blobs.put(0, dataBuff);
        
        int offset = 0;
        int starting = 0;
        int ending = 0;
        int indexS = 0;
        int indexE = 0;
        while(offset < fileBuff.length) {
            
            
            //szukamy otwierającego znacznika
            if(fileBuff[offset] == blobStart[indexS] && indexS < blobStart.length) {
                                   
                
                indexS++;
            } else if (indexS > 0){
                indexS = 0;
            }
            
            //szukamy zamykającego znacznika
            if(indexS == blobStart.length) {
                if(fileBuff[offset] == blobEnd[indexE] && indexE < blobEnd.length) {
                    
                    indexE++;
                    
                } else if (indexE > 0) {
                    indexE = 0;
                }
            }
            
            
            
            
            
            
            
            offset++;
        }
        
        return null;
    }

}
