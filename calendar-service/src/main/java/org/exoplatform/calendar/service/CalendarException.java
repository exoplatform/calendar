/*
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.calendar.service;


/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class CalendarException extends RuntimeException {
  private static final long serialVersionUID = -5373338604126226579L;
  private final CalendarError error;

  public CalendarException() {
    this(null);
  }
  
  public CalendarException(CalendarError error) {
      super(error.toString());
      this.error = error;
  }

  public CalendarException(CalendarError error, String message) {
      super(message);

      //
      this.error = error;
  }

  public CalendarException(CalendarError error, String message, Throwable cause) {
      super(message, cause);

      //
      this.error = error;
  }

  public CalendarException(CalendarError error, Throwable cause) {
      super(cause);

      //
      this.error = error;
  }

  public CalendarError getError() {
      return error;
  }
  
  public static enum CalendarError {
    
  }
}
