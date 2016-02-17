/**
 * Copyright (C) 2015 eXo Platform SAS.
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
  
package org.exoplatform.calendar.storage;


public interface GenericDAO<T, ID> {

  /**
   * @param id
   * @return
   */
  T getById(ID id);


  /**
   * Persist the entity object into database.
   *
   * @param entity
   */
  T save(T entity);

  /**
   * Update the specified entity argument with the most recent state.
   * <p>
   * If the entity does not exist, it throws NoSuchEntityException
   *
   * @param entity
   * @return
   */
  T update(T entity) throws NoSuchEntityException;

  /**
   * Remove the entity with given id if exists.
   *
   * @param id
   * @return 
   */
  T remove(ID id);

  /**
   * @return
   */
  T newInstance();
}
