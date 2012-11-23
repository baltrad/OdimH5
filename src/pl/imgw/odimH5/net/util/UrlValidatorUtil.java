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

package pl.imgw.odimH5.net.util;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * URL validator utility.
 * @author Maciej Szewczykowski | maciej@baltrad.eu
 * @version 1.1.0
 * @since 1.1.0
 */
public class UrlValidatorUtil {
    
    /** Schemes */
    private static final String[] SCHEMES = {"http", "https"}; 
    /** Validator */
    private UrlValidator urlValidator;
    
    /**
     * Constructor.
     */
    public UrlValidatorUtil() {
        this.urlValidator = new UrlValidator(SCHEMES);
    }
    
    /**
     * Performs validation.
     * @param url URL address to validate.
     * @return True upon successful validation, false otherwise
     */
    public boolean validate(String url) { 
        return urlValidator.isValid(url);
    }
}
