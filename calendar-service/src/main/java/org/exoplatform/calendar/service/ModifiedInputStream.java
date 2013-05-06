/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : vietnq
 *          vietnq@exoplatform.com
 * Apr 12, 2013 
 * 
 */

/** 
 *  This class provides methods to modify an input stream before importing.
 *  Sometime, user's input stream is not standard conformed. We need to survive erroneous data
 *  @see CAL-514, CAL-524
 */

public class ModifiedInputStream {

  private static final int CR = 13;
  private static final int LF = 10;
  private static final int SPACE = 32;
  private static final int COLON = 58;
  private static final int SEMICOLON = 59;
  private static final int MINUS = 45;

  public static InputStream getIcsModifiedStream(InputStream in) throws IOException {
    
    int prevChar = -1; //the last char read
    int currentPos = 0; //current position that is read by input stream
    int readChar;
    
    BufferedInputStream bis = new BufferedInputStream(in);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    while((readChar = bis.read()) > -1) {
      currentPos++;
      if(prevChar == LF) { // if the read char is beginning of a line
        // only test if the line begins with a property
        if(readChar != SPACE) {
          if(!beginWithAProperty(bis, readChar, currentPos)) {
            baos.write(SPACE);
          }
        }
      }
      baos.write(readChar);
      prevChar = readChar;
    }
    bis.close();
    in.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private static boolean beginWithAProperty(BufferedInputStream bis, int readChar, int currentPos) throws IOException {
    if(!Character.isLetter((char)readChar)) {
      return false;
    }
    if(readChar >= 'a') {
      return false;
    }
    String testString = getPseudoProperty(bis, readChar, currentPos);
    return isProperty(testString);
  }
  
  private static boolean isProperty(String str) throws IOException {
    char lastChar = str.charAt(str.length() - 1);
    // the property is followed by a COLON or a SEMICOLON
    return lastChar == (char)COLON || lastChar == (char)SEMICOLON;
  }
  
  /*
   * Gets the beginning of a line
   * The method read the input stream at most 20 characters, until it meets a lower case character or
   * the colon/semicolon or a non-letter character except MINUS.
   */
  private static String getPseudoProperty(BufferedInputStream bis, int c, int currentPos) throws IOException {
    bis.mark(currentPos); // mark the current position
    
    StringBuilder sb = new StringBuilder(Character.toString((char)c));
    int count = 0;
    int tmpChar;
    while(count < 20) {
      tmpChar = bis.read();
      count++;
      if(tmpChar == CR || tmpChar == LF) {
        break;
      }
      // stop when meeting the colon or semicolon
      if(tmpChar == SEMICOLON || tmpChar == COLON) {
        sb.append((char)tmpChar);
        break;
      }
      // stop when meeting a non-letter character and different than MINUS
      if(!Character.isLetter((char)tmpChar) && tmpChar != MINUS) {
        break;
      }
      // stop when meeting a lower case char
      if(tmpChar >= 'a') { 
        break;
      }
    }
    
    bis.reset(); // return to the marked position
    return sb.toString();
  }
}
