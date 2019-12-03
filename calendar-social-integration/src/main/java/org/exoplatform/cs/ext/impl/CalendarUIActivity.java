package org.exoplatform.cs.ext.impl;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import javax.jcr.PathNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/cs/social-integration/plugin/space/CalendarUIActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = CalendarUIActivity.AcceptEventActionListener.class),
    @EventConfig(listeners = CalendarUIActivity.AssignTaskActionListener.class),
    @EventConfig(listeners = CalendarUIActivity.SetTaskStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class)}

)
public class CalendarUIActivity extends BaseUIActivity {
  private static final Log LOG                = ExoLogger.getLogger(CalendarUIActivity.class);

  private boolean          isAnswered         = false;

  private boolean          isInvited          = false;

  private boolean          isTaskAssignedToMe = false;

  private boolean          isTaskDone         = false;

  private boolean          eventNotFound      = false;

  private String           taskStatus;

  private String           eventId, calendarId;

  private CalendarEvent event =  null ;

  public CalendarUIActivity() {
    super();
  }

  public void init() {
    try {
      eventId = getActivity().getTemplateParams().get(CalendarSpaceActivityPublisher.EVENT_ID_KEY);
      calendarId = getActivity().getTemplateParams().get(CalendarSpaceActivityPublisher.CALENDAR_ID_KEY);
      String username = ConversationState.getCurrent().getIdentity().getUserId();      
      CalendarService calService = (CalendarService) PortalContainer.getInstance().getComponentInstanceOfType(CalendarService.class);
      try {
        event = calService.getGroupEvent(calendarId, eventId);
      } catch (PathNotFoundException pnf) {
        if (LOG.isDebugEnabled()) 
          LOG.debug("Couldn't find the event: " + eventId, pnf);
      }
      if (event == null) {
        eventNotFound = true;
        return;
      }
      Map<String, String> pars = new HashMap<String, String>();
      if (event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT)
          && event.getParticipantStatus() != null) {
        for (String part : event.getParticipantStatus()) {
          String[] entry = part.split(":");
          if (entry.length > 1)
            pars.put(entry[0], entry[1]);
          else
            pars.put(entry[0], Utils.EMPTY_STR);
        }
        if (pars.containsKey(username)) {
          isInvited = true;
          if (pars.get(username).equalsIgnoreCase(Utils.STATUS_YES)
              || pars.get(username).equalsIgnoreCase(Utils.STATUS_NO)) {
            isAnswered = true;
          }
        }
      } else if (event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_TASK)) {
        taskStatus = event.getEventState();
        String taskDelegator = event.getTaskDelegator();
        if (taskDelegator != null) {
          if (taskDelegator.indexOf(username) >= 0) {
            isTaskAssignedToMe = true;
          }
        }
      }

    } catch (Exception e) {
      // Exception from CalendarService gets group event
      if (LOG.isErrorEnabled())
        LOG.error("Could not calculate values of Calendar activity with event(task): " + eventId, e);
    }
  }

  /**
   * used by template
   * @see <code>CalendarUIActivity.gtmpl</code>
   */
  private String getTitleTemplate() {
    String typeOfEvent = getTypeOfEvent();
    String titleKey = "";
    if (CalendarSpaceActivityPublisher.EVENT_ADDED.equals(typeOfEvent)) {
      titleKey = "CalendarUIActivity.msg.event-add";
    } else if (CalendarSpaceActivityPublisher.TASK_ADDED.equals(typeOfEvent)) {
      titleKey = "CalendarUIActivity.msg.task-add";
    } else if (CalendarSpaceActivityPublisher.EVENT_UPDATED.equals(typeOfEvent)) {
      titleKey = "CalendarUIActivity.msg.event-update";
    } else if (CalendarSpaceActivityPublisher.TASK_UPDATED.equals(typeOfEvent)) {
      titleKey = "CalendarUIActivity.msg.task-update";
    }
    return WebuiRequestContext.getCurrentInstance()
                              .getApplicationResourceBundle()
                              .getString(titleKey);
  }

  /**
   *
   * used by template
   * @see <code>CalendarUIActivity.gtmpl</code>
   */
  private String getSummary() {
    return getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_SUMMARY_KEY);
  }

  /**
   *
   * used by template
   * @see <code>CalendarUIActivity.gtmpl</code>
   * @deprecated use {@link #getEventPreviewLinkInSpace()} instead
   */
  @Deprecated
  private String getEventLink() {
    String value = null;
    return (value = getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_LINK_KEY)) != null ? value
                                                                                                 : "";
  }

  /**
   * @return the taskStatus
   */
  public String getTaskStatus() {
    return taskStatus;
  }

  /**
   * @return the isTaskAssigned
   */
  public boolean isTaskAssigned() {
    return isTaskAssignedToMe;
  }

  /**
   * @return the isTaskDone
   */
  public boolean isTaskDone() {
    return isTaskDone;
  }

  /**
   * @return the eventId
   */
  public String getEventId() {
    return eventId;
  }

  /**
   * @param eventId the eventId to set
   */
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /**
   * @return the calendarId
   */
  public String getCalendarId() {
    return calendarId;
  }

  /**
   * @param calendarId the calendarId to set
   */
  public void setCalendarId(String calendarId) {
    this.calendarId = calendarId;
  }

  /**
   * @return the isAnswered
   */
  public boolean isAnswered() {
    return isAnswered;
  }

  /**
   * @return the isInvited
   */
  public boolean isInvited() {
    return isInvited;
  }

  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }

    return value;
  }

  public String getTypeOfEvent() {
    String type = "";
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      type = params.get(CalendarSpaceActivityPublisher.EVENT_TYPE_KEY);
    }

    return type;
  }

  //SimpleDateFormat dformat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

  public String getEventStartTime(WebuiBindingContext ctx) {
    String timeStr = getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_STARTTIME_KEY);
    if (timeStr == null) {
      return "";
    }
    long time = Long.valueOf(timeStr);

    return CalendarSpaceActivityPublisher.getDateTimeString(getLocale(ctx), time, event,CalendarSpaceActivityPublisher.getUserTimeZone());

  }
  
  private Locale getLocale(WebuiBindingContext ctx) {
    return ctx.getRequestContext().getLocale();
  }

  public String getDescription() {
    String des = getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_DESCRIPTION_KEY);
    //--- Check if description field contains web links then replace all occurrence by the corresponding html tag
    return des != null ? extractWebLinkFromText(des) : "";
  }
  private static String extractWebLinkFromText (String text) {
    Pattern calendarPattern = Pattern.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>???“”‘’]))");
    try {
        Matcher matcher = calendarPattern.matcher(text);
      if (matcher.find()) {
          return matcher.replaceAll("<a href=\"$1\" target=\"_blank\">$1</a>");
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Exception occurs when matching event description with web link patterns",e);
      }
    }
    return text;
  }

  public String getLocation() {
    String des = getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_LOCALE_KEY);
    return des != null ? des : "";
  }

  public String getEventEndTime(WebuiBindingContext ctx) {
    String timeStr = getActivityParamValue(CalendarSpaceActivityPublisher.EVENT_ENDTIME_KEY);
    if (timeStr == null) {
      return "";
    }
    long time = Long.valueOf(timeStr);

    return CalendarSpaceActivityPublisher.getDateTimeString(getLocale(ctx), time, event, CalendarSpaceActivityPublisher.getUserTimeZone());

  }  

  /**
   * 
   * Restore EVENT_LINK_KEY from the information of space and event
   * URL format is as follows. 
   * <ul>
   *    <li>[spaceHome]/[calendar]/invitation/detail/[username]/[event id]/[calendar type]</li>
   * </ul>
   * @return empty if the process is failed
   */
  private String getEventPreviewLinkInSpace() {
    SpaceService spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
    String spaceGroupId = Utils.getSpaceGroupIdFromCalendarId(calendarId);
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
	
    StringBuffer sb = new StringBuffer("");
    if (space != null) {
      sb.append(org.exoplatform.social.webui.Utils.getSpaceHomeURL(space)) 
        .append("/" + Utils.PAGE_NAGVIGATION)
        .append(CalendarSpaceActivityPublisher.INVITATION_DETAIL)
        .append(ConversationState.getCurrent().getIdentity().getUserId())
        .append("/").append(eventId)
        .append("/").append(event.getCalType());
    } 
    return sb.toString();	    
  }
  
  public static class AcceptEventActionListener extends EventListener<CalendarUIActivity> {

    @Override
    public void execute(Event<CalendarUIActivity> event) throws Exception {

      CalendarUIActivity uiComponent = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String paramStr = requestContext.getRequestParameter(OBJECTID);
      if (!uiComponent.isAnswered()) {
        boolean isAccepted = false;
        if (paramStr != null)
          isAccepted = Boolean.parseBoolean(paramStr);
        try {
          CalendarService calService = (CalendarService) PortalContainer.getInstance()
                                                                        .getComponentInstanceOfType(CalendarService.class);
          User user = (User) ConversationState.getCurrent()
                                              .getAttribute(CacheUserProfileFilter.USER_PROFILE);
          int answer = Utils.DENY;
          if (isAccepted)
            answer = Utils.ACCEPT;

          calService.confirmInvitation(user.getUserName(),
                                       user.getEmail(),
                                       user.getUserName(),
                                       org.exoplatform.calendar.service.Calendar.TYPE_PUBLIC,
                                       uiComponent.getCalendarId(),
                                       uiComponent.getEventId(),
                                       answer);
        } catch (Exception e) { // CalendarService.confirmInvitation
          if (LOG.isWarnEnabled())
            LOG.warn("Could not answer the invitation of event: " + uiComponent.getEventId());
          throw e;
        }
      }

      requestContext.addUIComponentToUpdateByAjax(uiComponent);
    }

  }

  public static class AssignTaskActionListener extends EventListener<CalendarUIActivity> {

    @Override
    public void execute(Event<CalendarUIActivity> event) throws Exception {
      CalendarUIActivity uiComponent = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      if (!uiComponent.isTaskAssigned()) {
        try {
          CalendarService calService = (CalendarService) PortalContainer.getInstance()
                                                                        .getComponentInstanceOfType(CalendarService.class);
          String remoteUser = requestContext.getRemoteUser();
          calService.assignGroupTask(uiComponent.getEventId(),
                                     uiComponent.getCalendarId(),
                                     remoteUser);
        } catch (Exception e) { // CalendarService.assignGroupTask
          if (LOG.isWarnEnabled())
            LOG.warn("Could not assign user for task: " + uiComponent.getEventId());
          throw e;
        }
      }
      requestContext.addUIComponentToUpdateByAjax(uiComponent);

    }

  }

  public static class SetTaskStatusActionListener extends EventListener<CalendarUIActivity> {

    @Override
    public void execute(Event<CalendarUIActivity> event) throws Exception {
      CalendarUIActivity uiComponent = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String param = requestContext.getRequestParameter(OBJECTID);
      try {
        CalendarService calService = (CalendarService) PortalContainer.getInstance()
                                                                      .getComponentInstanceOfType(CalendarService.class);

        if (param != null && !param.equalsIgnoreCase(uiComponent.getTaskStatus())) {
          calService.setGroupTaskStatus(uiComponent.getEventId(),
                                        uiComponent.getCalendarId(),
                                        param);
        }
      } catch (Exception e) { // CalendarService.setGroupTaskStatus
        if (LOG.isWarnEnabled())
          LOG.warn("Could not set task status for task: " + uiComponent.getEventId());
        throw e;
      }
      requestContext.addUIComponentToUpdateByAjax(uiComponent);
    }
  }
}