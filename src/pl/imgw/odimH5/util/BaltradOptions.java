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

    private String sender = "";
    private String server = "";

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    /**
     * 
     * Returns true if any of its fields is empty
     * 
     * @return
     */
    public boolean isEmpty() {
        
        if(sender == null || sender.isEmpty())
            return true;
        if(server == null || server.isEmpty())
            return true;
        return false;
    }

}
