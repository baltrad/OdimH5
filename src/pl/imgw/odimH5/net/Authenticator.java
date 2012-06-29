/*******************************************************************************
*
* Copyright (C) 2009-2012 Institute of Meteorology and Water Management, IMGW
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
*******************************************************************************/

package pl.imgw.odimH5.net;

import org.apache.http.client.methods.HttpUriRequest;

/**
 *
 * @author szewczenko
 */
public interface Authenticator {
    
    /**
     * Add credentials to outgoing request.
     * @param request Http URI request to add credentials to
     * @param keyName Private key used to sign a message 
     */
    public void addCredentials(HttpUriRequest request, String keyName);
    
    /**
     * Authenticates request at servlet side.
     * @param message Message to authenticate
     * @param signature Signature to authenticate with
     * @param keyName Public key used to verify a message 
     * @return True upon successful authentication, false otherwise
     */
    public boolean authenticate(String message, String signture, 
            String keyName);
    
}
