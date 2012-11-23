/*
Copyright 2012 Estonian Meteorological and Hydrological Institute

This file is part of BaltradFrame.

BaltradFrame is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

BaltradFrame is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with BaltradFrame. If not, see <http://www.gnu.org/licenses/>.
*/

package pl.imgw.odimH5.net.util;

import org.keyczar.exceptions.KeyczarException;

public class KeyczarSigner implements Signer {
  private org.keyczar.Signer signer;
  
  /**
   * Constructor.
   *
   * @param keyLocation location of the key to use for signing
   */
  public KeyczarSigner(String keyLocation) {
    try {
      signer = new org.keyczar.Signer(keyLocation);
    } catch (KeyczarException e) {
      throw new RuntimeException("could not create Keyczar Signer", e);
    }
  }
  
  @Override
  public String sign(String message) {
    try {
      return signer.sign(message);
    } catch (KeyczarException e) {
      throw new RuntimeException("signing failed", e);
    }
  }
}
