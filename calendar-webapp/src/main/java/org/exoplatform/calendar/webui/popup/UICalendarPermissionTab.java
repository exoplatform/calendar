package org.exoplatform.calendar.webui.popup;

import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.webui.listener.ActionListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;

import java.util.*;

/**
 * class description
 *
 * @author Created by The eXo Platform SAS
 *         <br/>Anh-Tu Nguyen
 *         <br/><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com<a/>
 *         <br/>12 07, 2012
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template  = "app:/templates/calendar/webui/UIPopup/UICalendarPermissionTab.gtmpl"
)
public class UICalendarPermissionTab extends UIFormInputWithActions
{
  private static final Log LOG = ExoLogger.getExoLogger(UICalendarPermissionTab.class);

  private Set<Permission> calendarPermissions;

  public static final String PERMISSION_GRID = "UIPermissionGrid";

  private Map<String, List<ActionData>> actionField_;

  public UICalendarPermissionTab(String componentId) throws Exception
  {
    super(componentId);
    setComponentConfig(getClass(), null);
    actionField_ = new HashMap<String, List<ActionData>>();
    UIPermissionGrid permissionGrid = new UIPermissionGrid(PERMISSION_GRID);
    addChild(permissionGrid);
  }

  public void setCalendarPermissions(Set<Permission> permissions)
  {
    calendarPermissions = permissions;
  }

  public Set<Permission> getCalendarPermissions()
  {
    return calendarPermissions;
  }

  @Override
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception
  {
    actionField_.put(fieldName, actions) ;
  }

  public List<ActionData> getActionField(String fieldName)
  {
    return actionField_.get(fieldName);
  }

  /**
   * init data to display
   * calendarPermissions must be defined at this moment
   * constructor must be called before
   *
   */
  public void init() throws Exception
  {
    UIPermissionGrid permissionGrid = getChildById(PERMISSION_GRID);

    Iterator<Permission> it = calendarPermissions.iterator();
    while (it.hasNext())
    {
      permissionGrid.addEntry(it.next());
    }
  }

  /**
   * represents a permission with its owner and his rights / permission type
   *
   */
  public static class Permission
  {
    private PermissionOwner owner;

    /* correspond to the right */
    private Map<String, PermissionType> permissionTypeMap;

    public Permission(PermissionOwner owner)
    {
      this.owner = owner;
      permissionTypeMap = new HashMap<String, PermissionType>();

      /* by default we use add available permissions but set to notAllowed */
      permissionTypeMap.put(PermissionType.VIEW_CALENDAR, new PermissionType(PermissionType.VIEW_CALENDAR));
      permissionTypeMap.put(PermissionType.EDIT_CALENDAR, new PermissionType(PermissionType.EDIT_CALENDAR));
    }

    public Map<String, PermissionType> getPermissionTypeList() {
      return permissionTypeMap;
    }

    /**
     * modify permission to allowed on a specific permission in the map
     *
     * @param aRight the permission to modify
     */
    public void addRight(PermissionType aRight)
    {
      permissionTypeMap.get(aRight.getType()).setAllowed(true);
    }

    public void addRight(String aRight)
    {
      permissionTypeMap.get(aRight).setAllowed(true);
    }

    public PermissionOwner getOwner()
    {
      return owner;
    }

    @Override
    public boolean equals(Object o)
    {
      if ( !(o instanceof Permission)) return false;
      if (! ((Permission) o).getOwner().equals(owner) ) return false;
      return true;
    }

    @Override
    public int hashCode()
    {
      return owner.hashCode();
    }

    /**
     * convert from an array string of permission statements to
     * a set of permission
     *
     * @param permissions
     * @return
     */
    public static Set<Permission> valueOf(String[] permissions)
    {
      Set<Permission> permissionList = new HashSet<Permission>();
      for (String aPermission : permissions)
      {
        permissionList.add(new Permission( new PermissionOwner(aPermission) ));
      }
      return permissionList;
    }

    /**
     * add right to a group of permissions
     *
     * @return permissions new set of permissions with right modified
     */
    public static Set<Permission> addRightToGroupOfPermissions(Set<Permission> permissions, String aRight)
    {
      Iterator<Permission> it = permissions.iterator();
      while (it.hasNext())
      {
        it.next().addRight(aRight);
      }
      return permissions;
    }
  }

  public static class PermissionType
  {
    private String type;

    /* is the permission is allowed for the element */
    private boolean isAllowed;

    public static final String VIEW_CALENDAR = "view_calendar";

    public static final String EDIT_CALENDAR = "edit_calendar";

    public PermissionType(String type)
    {
      this.type = type;
      isAllowed = false;
    }

    public String getType() { return type; }

    public void setAllowed(boolean value)
    {
      isAllowed = value;
    }

    public boolean isAllowed()
    {
      return isAllowed;
    }

    public static PermissionType[] values()
    {
      PermissionType[] permissionTypes = { new PermissionType(VIEW_CALENDAR), new PermissionType(EDIT_CALENDAR) };
      return permissionTypes;
    }
  }

  public static class PermissionOwner
  {
    private String groupId;

    private String userId;

    private String membership;

    private String ownerType;

    public static final String USER_OWNER = "user";

    public static final String GROUP_OWNER = "group";

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

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getOwnerType() {
      return ownerType;
    }

    public void setOwnerType(String ownerType) {
      this.ownerType = ownerType;
    }

    /**
     * create a PermissionOwner object from the permission statement
     * example of permission statement: /platform/users/:*.manager
     *
     * @param permissionStatement
     */
    public PermissionOwner(String permissionStatement)
    {
      int indexOfSlashColon = permissionStatement.indexOf(CalendarUtils.SLASH_COLON);
      groupId = permissionStatement.substring(0, indexOfSlashColon);
      int indexOfDot = permissionStatement.indexOf(CalendarUtils.DOT);
      if (indexOfDot == -1) /* without . it's user permission */
      {
        userId = permissionStatement.substring(indexOfSlashColon + 2, permissionStatement.length());
        ownerType = USER_OWNER;
        return ;
      }

      /* it's group permission */
      userId = permissionStatement.substring(indexOfSlashColon + 2, indexOfDot);
      membership = permissionStatement.substring(indexOfDot + 1, permissionStatement.length());
      ownerType = GROUP_OWNER;
    }

    /**
     * takes the string after the last "/" of group id
     *
     * @return
     */
    private String truncateGroupId()
    {
      String[] groupIdParts = groupId.split(CalendarUtils.SLASH);
      return groupIdParts[groupIdParts.length - 1];
    }

    /**
     * translate * to anybody
     *
     * @return
     */
    private String getMeaningfulMembership()
    {
      if (membership.equals(CalendarUtils.STAR))
        return "Anybody";
      return membership;
    }

    /**
     * returns a readable permission under form: user or membership in group
     *
     * @return
     */
    public String getMeaningfulPermissionOwnerStatement()
    {
      if (ownerType.equals(USER_OWNER))
        return userId + " in " + truncateGroupId();
      else if (ownerType.equals(GROUP_OWNER))
        return getMeaningfulMembership() + " in " + truncateGroupId();
      return null;
    }

    /**
     * get the owner statement for the permission
     *
     * @return
     */
    @Override
    public String toString()
    {
      StringBuffer owner = new StringBuffer(groupId).append(CalendarUtils.COLON).append(userId);
      if (ownerType.equals(GROUP_OWNER)) return owner.append(CalendarUtils.DOT).append(membership).toString();
      return owner.toString();
    }

    /**
     * compare 2 permissions owner
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
      if ( !(o instanceof PermissionOwner) ) return false;
      PermissionOwner owner = ((PermissionOwner) o);

      if (!owner.getOwnerType().equals(ownerType)) return false;
      if (!owner.getGroupId().equals(groupId)) return false;
      if (!owner.getUserId().equals(userId)) return false;

      if (owner.getOwnerType().equals(GROUP_OWNER))
        if (!owner.getMembership().equals(membership)) return false;
      return true;
    }

    @Override
    public int hashCode()
    {
      return (groupId + userId + membership).hashCode();
    }
  }
}
