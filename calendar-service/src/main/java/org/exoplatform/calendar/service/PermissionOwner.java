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

import org.exoplatform.services.security.MembershipEntry;

public class PermissionOwner {
  /**
   * id of Permission Owner is - userId: like john if owner is an user -
   * groupId: like /platform/users if owner is a group - membershipId: like
   * /platform/users:*.manager used to evaluate equality
   **/
  private String             id;

  /**
   * owner = user --> empty string 
   * owner = group --> groupId 
   * owner = membership --> groupId
   **/
  private String             groupId;

  private String             membership;

  private String             ownerType;

  public static final String USER_OWNER       = "user";

  public static final String GROUP_OWNER      = "group";

  public static final String MEMBERSHIP_OWNER = "membership";

  public String getId() {
    return id;
  }

  public void setId(String permissionId) {
    id = permissionId;
  }

  public String getMembership() {
    return membership;
  }

  public void setMembership(String membership) {
    this.membership = membership;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  /**
   * example of permission statement - membership: /platform/users/:*.manager -
   * user: demo - group: /organization/management/executive-board/:*.*
   */
  public static PermissionOwner createPermissionOwnerFrom(String permissionStatement) {
    PermissionOwner owner = new PermissionOwner();
    owner.setId(permissionStatement);

    /* user permission */
    if (permissionStatement.indexOf(Utils.SLASH_COLON) == -1) {
      owner.setGroupId("");
      owner.setMembership("");
      owner.setOwnerType(USER_OWNER);
      return owner;
    }

    int indexOfSlashColon = permissionStatement.indexOf(Utils.SLASH_COLON);
    owner.setGroupId(permissionStatement.substring(0, indexOfSlashColon));

    /* membership permission */
    if (permissionStatement.indexOf(Utils.ANY) == -1) {
      int indexAnyOf = permissionStatement.indexOf(Utils.ANY_OF);
      owner.setMembership(permissionStatement.substring(indexAnyOf + 2,
                                                        permissionStatement.length()));
      owner.setOwnerType(MEMBERSHIP_OWNER);
      return owner;
    }

    /* group permission */
    owner.setMembership(MembershipEntry.ANY_TYPE);
    owner.setOwnerType(GROUP_OWNER);
    return owner;
  }

  /**
   * takes the string after the last "/" of group id and replace special
   * character by space
   * 
   * @return
   */
  private String truncateGroupId() {
    String[] groupIdParts = groupId.split(Utils.SLASH);
    char[] newGroupId = groupIdParts[groupIdParts.length - 1].toCharArray();
    newGroupId[0] = Character.toUpperCase(newGroupId[0]); /*
                                                           * upper case the
                                                           * first character
                                                           */
    return new String(newGroupId).replaceAll("[^a-zA-Z0-9]+", " "); /*
                                                                     * replace
                                                                     * special
                                                                     * character
                                                                     * by space
                                                                     */
  }

  /**
   * translate membership *.* to anybody
   * 
   * @return
   */
  private String getMeaningfulMembership() {
    if (membership.equals("*"))
      return "Anybody";
    return membership;
  }

  /**
   * returns a readable permission under form: user or membership in group
   * 
   * @return
   */
  public String getMeaningfulPermissionOwnerStatement() {
    if (ownerType.equals(USER_OWNER))
      return id;
    else if (ownerType.equals(GROUP_OWNER))
      return "Anybody in " + truncateGroupId();
    return getMeaningfulMembership() + " in " + truncateGroupId();
  }

  /**
   * get the owner statement for the permission
   * 
   * @return
   */
  @Override
  public String toString() {
    return id;
  }

  /**
   * compare 2 permissions owner equality happens when 2 permission owner has
   * the same type and id
   * 
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PermissionOwner))
      return false;
    PermissionOwner owner = ((PermissionOwner) o);
    return id.equals(owner.getId());
  }

  @Override
  public int hashCode() {
    return Math.abs(id.hashCode());
  }
}