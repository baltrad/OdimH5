/*
 * BaltradNode :: Radar data exchange and communication system
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2010
 *
 * maciej.szewczykowski@imgw.pl
 */

/***************************************************************************************************
 *
 * Copyright (C) 2009-2010 Institute of Meteorology and Water Management, IMGW
 *
 * This file is part of the BaltradDex software.
 *
 * BaltradDex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BaltradDex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the BaltradDex software.  If not, see http://www.gnu.org/licenses.
 *
 ***************************************************************************************************/

package pl.imgw.odimH5.util;

//import eu.baltrad.dex.log.model.LogManager;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * Class encapsulating standard message structure and functionality to be used
 * within Baltrad data exchange, storage and production systems.
 * 
 * @author <a href="mailto:maciej.szewczykowski@imgw.pl>Maciej Szewczykowski</a>
 * @version 1.0
 * @since 1.0
 */
public class BaltradFrame extends MultipartEntity {
    // ----------------------------------------------------------------------------------------
    // Constants
    // XML element / document encoding
    private static final String XML_ENCODING = "UTF-8";
    // XML elements / available MIME types
    public static final String MIME_MULTIPART = "multipart/form-data";
    // Character set
    private static final Charset CHARSET = Charset.forName(XML_ENCODING);
    // Multipart message parts identifiers
    public static final String XML_PART = "<bf_xml/>";
    public static final String FILE_PART = "<bf_file/>";
    // ----------------------------------------------------------------------------------------
    // Variables
    // Log manager
    private MessageLogger msg;
    private boolean verbose;

    // ------------------------------------------------------------------------------------------
    // Methods
    /**
     * Default constructor.
     */
    public BaltradFrame() {
    }

    /**
     * Constructor creates data frame.
     * 
     * @param xmlHdrStr
     *            XML header string
     * @param absFilePath
     *            Absolute file path
     */
    public BaltradFrame(MessageLogger msg, String xmlHdrStr,
            String absFilePath, boolean verbose) {
        try {
            // Create XML string header using given encoding
            StringBody sbXMLHeader = new StringBody(xmlHdrStr, MIME_MULTIPART,
                    CHARSET);
            // Create file content body
            File f = new File(absFilePath);
            ContentBody cbFile = new FileBody(f, MIME_MULTIPART);
            // Add XML header string
            this.addPart(XML_PART, sbXMLHeader);
            // Add file body content
            this.addPart(FILE_PART, cbFile);
            this.msg = msg;
            this.verbose = verbose;
        } catch (UnsupportedEncodingException e) {
            msg.showMessage(
                    "Unsupported encoding " + "type: " + e.getMessage(),
                    verbose);
        }
    }

    /**
     * Constructor creates message frame.
     * 
     * @param xmlHdrStr
     *            XML header string
     */
    public BaltradFrame(String xmlHdrStr) {
        try {
            // Create XML string header using given encoding
            StringBody sbXMLHeader = new StringBody(xmlHdrStr, MIME_MULTIPART,
                    CHARSET);
            // Add XML header string
            this.addPart(XML_PART, sbXMLHeader);
        } catch (UnsupportedEncodingException e) {
            msg.showMessage(
                    "Unsupported encoding " + "type: " + e.getMessage(),
                    verbose);
        }
    }
}
// --------------------------------------------------------------------------------------------------