/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class BaltradOptions {

    //private String sender = "";
    private String hostAddress = "";
    //private int port = 0;

    /*public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }*/

    public String getHostAddress() { return hostAddress; }

    public void setHostAddress( String hostAddress ) { this.hostAddress = hostAddress; }

    //public int getPort() { return port; }

    //public void setPort( int port ) { this.port = port; }

    /**
     * 
     * Returns true if any of its fields is empty
     * 
     * @return
     */
    public boolean isEmpty() {
        
        //if(sender == null || sender.isEmpty())
        //    return true;
        if( hostAddress == null || hostAddress.isEmpty() )
            return true;
        //if( port == 0 )
         //   return true;
        return false;
    }

}
