/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.service;

import java.util.Objects;

import org.exoplatform.calendar.model.AbstractModel;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventCategory extends AbstractModel {
  
  private static final long serialVersionUID = 3773092354485644604L;

  private String  name;

  private boolean isDataInit = false;

  public EventCategory() {
    super("eventCategory" + IdGenerator.generate());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getLocalizedName() {
    return Utils.getLocalizedName(this);
  }

  public void setDataInit(boolean isDataInit) {
    this.isDataInit = isDataInit;
  }

  public boolean isDataInit() {
    return isDataInit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    EventCategory that = (EventCategory) o;
    return isDataInit == that.isDataInit &&
            Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name, isDataInit);
  }
}
