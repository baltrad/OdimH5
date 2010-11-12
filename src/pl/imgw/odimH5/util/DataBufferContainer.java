/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009
 *
 * maciej.szewczykowski@imgw.pl
 */

package pl.imgw.odimH5.util;

/**
 * Class implementing data container functionality.
 *
 * @author szewczenko
 * @version 1.0
 * @since 1.0
 */
public class DataBufferContainer {

        // Data buffer length
        private int buffLen = 0;
        // Data buffer
        private byte[] dataBuff = {0};
        // Number of bits used to describe one pixel
//        private int depth = 0;
        

        
//        public int getDepth() {
//            return depth;
//        }
//        public void setDepth(int depth) {
//            this.depth = depth;
//        }
        /**
         * Method returns data buffer length
         *
         * @return Data buffer length
         */
        public int getDataBufferLength() {
            return this.buffLen;
        }
        /**
         * Method sets data buffer length
         *
         * @param buffLen Data buffer length
         */
        public void setDataBufferLength( int buffLen ) {
            this.buffLen = buffLen;
        }
        /**
         * Method returns data buffer
         *
         * @return Data buffer
         */
        public byte[ ] getDataBuffer() {
            return this.dataBuff;
        }
        /**
         * Method sets data buffer
         *
         * @param dataBuff Data buffer
         */
        public void setDataBuffer( byte[ ] dataBuff ) {
            this.dataBuff = dataBuff;
        }

}
