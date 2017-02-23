package org.exoplatform.calendar.model;

import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.calendar.util.Constants;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * A new Calendar model for new Calendar Service API.
 *
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class Calendar extends AbstractModel {

  private String               name;

  private String               calendarColor = Constants.N_POWDER_BLUE;

  private String               description;

  private String               timeZone;

  private String               locale;

  private String               calendarOwner;

  private String[]             viewPermission;

  private String[]             editPermission;

  private String[]             groups;

  private String               publicUrl;

  private String               privateUrl;

  private boolean              _isPublic      = false;

  private boolean       remote = false;
  
  private boolean hasChildren = false;

  public static final String   CAL_PREFIX = "calendar";

  public Calendar() {
    this(CAL_PREFIX + IdGenerator.generate());
  }

  public Calendar(String compositeId) {
    super(compositeId);
    timeZone = TimeZone.getDefault().getID();
    locale = Locale.getDefault().getISO3Country();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getEditPermission() {
    return editPermission;
  }

  public void setEditPermission(String[] editPermission) {
    this.editPermission = editPermission;
  }

  public String[] getViewPermission() {
    return viewPermission;
  }

  public void setViewPermission(String[] viewPermission) {
    this.viewPermission = viewPermission;
  }

  public String[] getGroups() {
    return groups;
  }

  public void setGroups(String[] groups) {
    this.groups = groups;
  }

  public boolean isPublic() {
    return _isPublic;
  }

  public void setPublic(boolean isPublic) {
    this._isPublic = isPublic;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLocale() {
    return locale;
  }

  public void setCalendarColor(String calendarColor) {
    this.calendarColor = calendarColor;
  }

  public String getCalendarColor() {
    return calendarColor;
  }

  public void setCalendarOwner(String calendarOwner) {
    this.calendarOwner = calendarOwner;
  }

  public String getCalendarOwner() {
    return calendarOwner;
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = removeDomainName(publicUrl);
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public void setPrivateUrl(String privateUrl) {
    this.privateUrl = removeDomainName(privateUrl);
  }

  public String getPrivateUrl() {
    return privateUrl;
  }
  
  // This method used to back compatible with old url data contain domain name
  private String removeDomainName(String url) {
    if (url != null && url.indexOf("http") == 0) {
      url = url.substring(url.indexOf(":") + 3);
      url = url.substring(url.indexOf("/"));
    }
    return url;
  }

  public boolean isRemote() {
    return remote;
  }

  public void setRemote(boolean remote) {
    this.remote = remote;
  }

  public boolean hasChildren() {
    return hasChildren;
  }

  public void setHasChildren(boolean children) {
    this.hasChildren = children;
  }

  /**
   * used to compare 2 calendars or between a calendar and an object
   *
   * @param o a particular object
   * @return true false
   */
  @Override
  public boolean equals(Object o)
  {
    if(o == null) {
      return false;
    }
    if (o instanceof Calendar) {
      
      Calendar calendar = (Calendar) o;
      if(getId() == null) {
        return calendar.getId() == null;
      }
      return getId().equals(calendar.getId());
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    return getId().hashCode();
  }
}
