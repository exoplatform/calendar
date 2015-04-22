/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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

package org.exoplatform.calendar.ws;

import javax.annotation.security.RolesAllowed;
import javax.jcr.query.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.Calendar.Type;
import org.exoplatform.calendar.service.CalendarCollection;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventDAO;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.service.MultiListAccess;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.ws.bean.AttachmentResource;
import org.exoplatform.calendar.ws.bean.CalendarResource;
import org.exoplatform.calendar.ws.bean.CategoryResource;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.calendar.ws.bean.EventResource;
import org.exoplatform.calendar.ws.bean.FeedResource;
import org.exoplatform.calendar.ws.bean.InvitationResource;
import org.exoplatform.calendar.ws.bean.RepeatResource;
import org.exoplatform.calendar.ws.bean.TaskResource;
import org.exoplatform.calendar.ws.common.Resource;
import org.exoplatform.calendar.ws.common.RestAPIConstants;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.webservice.cs.bean.End;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

@Path(CalendarRestApi.CAL_BASE_URI)
public class CalendarRestApi implements ResourceContainer {

  public final static String CAL_BASE_URI = RestAPIConstants.BASE_VERSION_URI + "/calendar";

  public static final String TEXT_ICS = "text/calendar";
  public static final MediaType TEXT_ICS_TYPE = new MediaType("text","calendar");
  
  // TODO: Why /cs/calendar is still being used here ?
  public final static String BASE_URL = "/cs/calendar";
  public final static String BASE_EVENT_URL = BASE_URL + "/event";


  public final static String CALENDAR_URI = "/calendars/";
  public final static String EVENT_URI = "/events/";
  public final static String TASK_URI = "/tasks/";
  public final static String ICS_URI = "/ics";
  public final static String ATTACHMENT_URI = "/attachments/";
  public final static String OCCURRENCE_URI = "/occurrences";
  public final static String CATEGORY_URI = "/categories/";
  public final static String FEED_URI = "/feeds/";
  public final static String RSS_URI = "/rss";
  public final static String INVITATION_URI ="/invitations/";
  public static final String HEADER_LINK = "Link";
  public static final String HEADER_LOCATION = "Location";
  
  private OrganizationService orgService;
  private int query_limit = 10;
  private SubResourceHrefBuilder subResourcesBuilder = new SubResourceHrefBuilder(this);

  private final static CacheControl cc = new CacheControl();
  
  static {
    cc.setNoCache(true);
    cc.setNoStore(true);
  }

  private Log log = ExoLogger.getExoLogger(CalendarRestApi.class);
  
  public CalendarRestApi(OrganizationService orgService, InitParams params) {
    this.orgService = orgService;
    
    if (params != null && params.getValueParam("query_limit") != null) {
      query_limit = Integer.parseInt(params.getValueParam("query_limit").getValue());
    }
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSubResources(@Context UriInfo uri) {
    Map<String, String[]> subResources = new HashMap<String, String[]>();
    subResources.put("subResourcesHref", subResourcesBuilder.buildResourceMap(uri));

    return Response.ok(subResources, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }

  /**
   * Search for calendars which:
   * the authenticated user is the owner of the calendar
   * the authenticated user belongs to the group of the calendar
   * the calendar has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param type    The calendar type to search for. It can be one of <code>personal, group, shared</code>.
   *                If this is omitted OR an unknown type is specified, it will search for <code>all</code> types.
   *
   * @param offset  The starting point when paging through a list of entities. Defaults to 0
   * 
   * @param limit   The maximum number of results when paging through a list of entities
   * 
   * @param fields  This is a list of comma-separated property's names
   *                which will be returned in the response
   * 
   * @param jsonp   The name of a JavaScript function to be used as the JSONP callback
   * 
   * @return        List of calendars
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @RolesAllowed("users")
  @Path("/calendars/")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getCalendars(@QueryParam("type") String type,
                                                @QueryParam("offset") int offset, 
                                                @QueryParam("limit") int limit,
                                                @QueryParam("fields") String fields,
                                                @QueryParam("jsonp") String jsonp,
                                                @Context UriInfo uri) {
    try {
      limit = parseLimit(limit);
      Type calType = Calendar.Type.UNDEFINED;

      if (type != null) {
        try {
          calType = Calendar.Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
          // Use default Type.UNDEFINED in any case of exception
        }
      }
      
      CalendarCollection<Calendar> cals = calendarServiceInstance().getAllCalendars(currentUserId(), calType.type(), offset, limit);
      if(cals == null || cals.isEmpty()) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();      
      
      String basePath = getBasePath(uri);
      Collection data = new LinkedList();
      Iterator<Calendar> calIterator = cals.iterator();
      while (calIterator.hasNext()) {
         Calendar cal = calIterator.next();         
         data.add(extractObject(new CalendarResource(cal, basePath), fields));
      }
      
      CollectionResource calData = new CollectionResource(data, cals.getFullSize());
      calData.setOffset(offset);
      calData.setLimit(limit);
      
      if (jsonp != null) {
        JsonValue value = new JsonGeneratorImpl().createJsonObject(calData);
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append('(').append(value).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).header(HEADER_LINK, buildFullUrl(uri, offset, limit, calData.getSize())).cacheControl(cc).build();
      }
      
      //
      return Response.ok(calData, MediaType.APPLICATION_JSON).header(HEADER_LINK, buildFullUrl(uri, offset, limit, calData.getSize())).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();

  }


  /**
   *  Creates a calendar if:
   * this is a personal calendar and the user is authenticated
   * this is a group calendar and the user is authenticated and belongs to the group
   */
  @POST
  @RolesAllowed("users")
  @Path("/calendars/")
	public Response createCalendar(CalendarResource cal, @Context UriInfo uriInfo) {
    Calendar calendar = new Calendar();    
    buildCalendar(calendar, cal);
    
		if (cal.getGroups() != null && cal.getGroups().length > 0) {
			// Create a group calendar
			if (isInGroups(cal.getGroups())) {
				calendarServiceInstance().savePublicCalendar(calendar, true);
			} else {
				return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
			}
		} else {
		  if (cal.getOwner() != null && !cal.getOwner().equals(currentUserId())) {  
		    return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
		  } else {
		    // Create a personal calendar
		    calendarServiceInstance().saveUserCalendar(currentUserId(), calendar, true);
		  }
		}
		
		return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, uriInfo.getAbsolutePath() + cal.getId()).cacheControl(cc).build();
	}

  /**
   * Returns the calendar if:
   * the authenticated user is the owner of the calendar
   * the authenticated user belongs to the group of the calendar
   * the calendar has been shared with the authenticated user or with a group of the authenticated user
   */
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCalendarById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                  @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      CalendarResource calData = null;
      if (this.hasViewCalendarPermission(cal, currentUserId())) {
        calData = new CalendarResource(cal, getBasePath(uriInfo));
      }
      
      if (calData == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      Object resource = extractObject(calData, fields);
      if (jsonp != null) {
        String json = null;
        if (resource instanceof Map) json = new JSONObject(resource).toString();
        else {
          JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
          json = generatorImpl.createJsonObject(resource).toString();
        }
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append('(').append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *  Updates the calendar if:
   * the authenticated user is the owner of the calendar
   * for group calendars, the authenticated user has edit rights on the calendar
   */
  @PUT
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  public Response updateCalendarById(@PathParam("id") String id, CalendarResource calObj) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      //Only allow to edit if user is owner of calendar, or have edit permission on group calendar
      //don't allow to edit shared calendar, or remote calendar
      if ((currentUserId().equals(cal.getCalendarOwner()) || cal.getGroups() != null) &&
          Utils.isCalendarEditable(currentUserId(), cal)) {
        buildCalendar(cal, calObj);
        calendarServiceInstance().saveCalendar(cal.getCalendarOwner(), cal, Integer.valueOf(cal.getCalType()), false);
        return Response.ok().cacheControl(cc).build();
      }
      
      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
    } catch (Exception e) {
      log.error(e);
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Deletes the calendar if:
   * the authenticated user is the owner of the calendar
   * for group calendars, the authenticated user has edit rights on the calendar
   * If it is a shared calendar the calendar is not shared anymore (but the original calndar is not deleted)
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  public Response deleteCalendarById(@PathParam("id") String id) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();

      cal.setCalType(calendarServiceInstance().getTypeOfCalendar(currentUserId(), id));
      if (Utils.isCalendarEditable(currentUserId(), cal) || cal.getCalType() == Calendar.TYPE_SHARED) {
        switch (cal.getCalType()) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().removeUserCalendar(cal.getCalendarOwner(), id);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().removePublicCalendar(id);
          break;
        case Calendar.TYPE_SHARED:
          if (this.hasViewCalendarPermission(cal, currentUserId())) {
            calendarServiceInstance().removeSharedCalendar(currentUserId(),id);
            break;
          }
        }
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }
  
  /**
   * Returns the iCalendar export if:
   * the calendar is public
   * the authenticated user is the owner of the calendar
   * the authenticated user belongs to the group of the calendar
   * the calendar has been shared with the authenticated user or with a group of the authenticated user
   */
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}/ics")
  @Produces(TEXT_ICS)
  public Response exportCalendarToIcs(@PathParam("id") String id) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId())) {
        int type = calendarServiceInstance().getTypeOfCalendar(currentUserId(),id);
        String username = currentUserId();
        if (type == -1) {
          //this is a workaround
          //calendarService can't find type of calendar correctly 
          type = Calendar.TYPE_PRIVATE;
          username = cal.getCalendarOwner();
        }
        
        CalendarImportExport iCalExport = calendarServiceInstance().getCalendarImportExports(CalendarService.ICALENDAR);
        ArrayList<String> calIds = new ArrayList<String>();
        calIds.add(id);
        OutputStream out = iCalExport.exportCalendar(username, calIds, String.valueOf(type), Utils.UNLIMITED);
        InputStream in = new ByteArrayInputStream(out.toString().getBytes());
        return Response.ok(in, TEXT_ICS_TYPE)
            .header("Cache-Control", "private max-age=600, s-maxage=120").
            header("Content-Disposition", "attachment;filename=\"" + cal.getName() + Utils.ICS_EXT).cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();

  }

  /**
   * Returns an event in the list when :
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/events/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEvents(@QueryParam("startTime") String start,
                                           @QueryParam("endTime") String end,
                                           @QueryParam("category") String category,
                                           @QueryParam("offset") int offset,
                                           @QueryParam("limit") int limit, 
                                           @QueryParam("returnSize") boolean returnSize,
                                           @QueryParam("fields") String fields,
                                           @QueryParam("jsonp") String jsonp,
                                           @QueryParam("expand") String expand,
                                           @Context UriInfo uri) throws Exception {
    limit = parseLimit(limit);
    String username = currentUserId();
    
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    
    //find all viewable calendars of user: public, user, group, shared calendars,
    String[] calIds = findViewableCalendars(username);
    EventQuery eventQuery = buildEventQuery(start, end, category, calIds, null, username, CalendarEvent.TYPE_EVENT, returnSize);
    ListAccess<CalendarEvent> events = evtDAO.findEventsByQuery(eventQuery);
    
    List data = new LinkedList();
    for (CalendarEvent event : events.load(offset, limit)) {
      data.add(buildEventResource(event, uri, expand, fields));
    }
    long fullSize = returnSize ? events.getSize() : -1;
    CollectionResource evData = new CollectionResource(data, fullSize);    
    evData.setOffset(offset);
    evData.setLimit(limit);
    
    ResponseBuilder response = buildJsonP(evData, jsonp);    
    if (returnSize) {
      response.header(HEADER_LINK, buildFullUrl(uri, offset, limit, fullSize));
    }
    
    //
    return response.build();
  }

  /**
   * Returns the event if:
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventById(@PathParam("id") String id,
                                                 @QueryParam("fields") String fields,
                                                 @QueryParam("expand") String expand,
                                                 @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      CalendarService service = calendarServiceInstance();
      CalendarEvent ev = service.getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      boolean inParticipant = false;
      String[] participant = ev.getParticipant();
      if (participant != null) {
        Arrays.sort(participant);
        if (Arrays.binarySearch(participant, currentUserId()) > -1) inParticipant = true;
      }
     
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {        
        Object resource = buildEventResource(ev, uriInfo, expand, fields);        
        return buildJsonP(resource, jsonp).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Updates the event if:
   * the authenticated user is the owner of the calendar of the event
   * for group calendars, the authenticated user has edit rights on the calendar
   * the calendar of the event has been shared with the authenticated user, with modification rights
   * the calendar of the event has been shared with a group of the authenticated user, with modification rights
   */
  @PUT
  @RolesAllowed("users")
  @Path("/events/{id}")
  public Response updateEventById(@PathParam("id") String id,  EventResource evObject) {
    try {
      CalendarEvent old = calendarServiceInstance().getEventById(id);
      if(old == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();

      Calendar cal = calendarServiceInstance().getCalendarById(old.getCalendarId());
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        buildEvent(old, evObject);
        
        int calType = -1;
        try {
          calType = Integer.parseInt(old.getCalType());
        }catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), old.getCalendarId());
        } 
        switch (calType) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().saveUserEvent(currentUserId(), old.getCalendarId(), old, false);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().savePublicEvent(old.getCalendarId(), old, false);
          break;
        case Calendar.TYPE_SHARED:
          calendarServiceInstance().saveEventToSharedCalendar(currentUserId(), old.getCalendarId(),old,false);
          break;

        default:
          break;
        }
        return Response.ok().cacheControl(cc).build();
      }
      
      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }


  /**
   * Deletes the event if:
   * the authenticated user is the owner of the calendar of the event
   * for group calendars, the authenticated user has edit rights on the calendar
   * the calendar of the event has been shared with the authenticated user, with modification rights
   * the calendar of the event has been shared with a group of the authenticated user, with modification rights
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/events/{id}")
  public Response deleteEventById(@PathParam("id") String id) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = Calendar.TYPE_ALL;
        try {
          calType = Integer.parseInt(ev.getCalType());
        } catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), ev.getCalendarId());
        }
        switch (calType) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().removeUserEvent(currentUserId(), ev.getCalendarId(), id);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().removePublicEvent(ev.getCalendarId(),id);
          break;
        case Calendar.TYPE_SHARED:
          calendarServiceInstance().removeSharedEvent(currentUserId(), ev.getCalendarId(), id);
          break;

        default:
          break;
        }
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *  Returns attachments if:
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}/attachments")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttachmentsFromEvent(@PathParam("id") String id, 
                                                                      @QueryParam("offset") int offset, 
                                                                      @QueryParam("limit") int limit,
                                                                      @QueryParam("fields") String fields,
                                                                      @QueryParam("jsonp") String jsonp,
                                                                      @Context UriInfo uriInfo) {
    try {
      limit = parseLimit(limit);
      
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if(ev == null || ev.getAttachment() == null) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      } else {
        Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
        boolean inParticipant = false;
        if (ev.getParticipant() != null) {
          String[] participant = ev.getParticipant();
          Arrays.sort(participant);
          int i = Arrays.binarySearch(participant, currentUserId());
          if (i > -1) inParticipant = true;
        }
      
        if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {
          Iterator<Attachment> it = ev.getAttachment().iterator();
          List attResource = new ArrayList();
          Utils.skip(it, offset);
          int counter = 0;
          String basePath = getBasePath(uriInfo);
          while (it.hasNext()) {
            Attachment a = it.next();
            attResource.add(extractObject(new AttachmentResource(a, basePath), fields));
            if(++counter == limit) break;
          }
          CollectionResource evData = new CollectionResource(attResource, ev.getAttachment().size());
          evData.setOffset(offset);
          evData.setLimit(limit);
          
          if (jsonp != null) {
            JsonValue value = new JsonGeneratorImpl().createJsonObject(evData);
            StringBuilder sb = new StringBuilder(jsonp);
            sb.append('(').append(value).append(");");
            return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize())).build();
          }
          
          //
          return Response.ok(evData, MediaType.APPLICATION_JSON).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize())).cacheControl(cc).build();
        }
        
        //
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Creates the attachment if:
   * the authenticated user is the owner of the calendar of the event
   * for group calendars, the authenticated user has edit rights on the calendar
   * the calendar of the event has been shared with the authenticated user, with modification rights
   * the calendar of the event has been shared with a group of the authenticated user, with modification rights
   */
  @POST
  @RolesAllowed("users")
  @Path("/events/{id}/attachments")
  @Consumes("multipart/*")
  public Response createAttachmentForEvent(@Context UriInfo uriInfo, @PathParam("id") String id, Iterator<FileItem> iter) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();

      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());

      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = Calendar.TYPE_ALL;
        List<Attachment> attachment = new ArrayList<Attachment>();
        try {
          calType = Integer.parseInt(ev.getCalType());
        } catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), ev.getCalendarId());
        }

        attachment.addAll(ev.getAttachment());
        while (iter.hasNext()) {
          FileItem file  = iter.next();
          String fileName = file.getName();
          if(fileName != null) {
            String mimeType = new MimeTypeResolver().getMimeType(fileName.toLowerCase());
            Attachment at = new Attachment();
            at.setMimeType(mimeType);
            at.setSize(file.getSize());
            at.setName(file.getName());
            at.setInputStream(file.getInputStream());
            attachment.add(at);
          }
        }
        ev.setAttachment(attachment);

        switch (calType) {
          case Calendar.TYPE_PRIVATE:
            calendarServiceInstance().saveUserEvent(currentUserId(), ev.getCalendarId(), ev, false);
            break;
          case Calendar.TYPE_PUBLIC:
            calendarServiceInstance().savePublicEvent(ev.getCalendarId(), ev, false);
            break;
          case Calendar.TYPE_SHARED:
            calendarServiceInstance().saveEventToSharedCalendar(currentUserId(), ev.getCalendarId(), ev, false);
            break;
          default:
            break;
        }

        StringBuilder attUri = new StringBuilder(getBasePath(uriInfo));
        attUri.append("/").append(ev.getId());
        attUri.append(ATTACHMENT_URI);
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, attUri.toString()).cacheControl(cc).build();
      }

      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Returns an event in the list when:
   * the calendar is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}/events")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventsByCalendar(@PathParam("id") String id,                                       
                                                             @QueryParam("startTime") String start,
                                                             @QueryParam("endTime") String end,
                                                             @QueryParam("category") String category,
                                                             @QueryParam("offset") int offset,
                                                             @QueryParam("limit") int limit,
                                                             @QueryParam("fields") String fields,
                                                             @QueryParam("jsonp") String jsonp,
                                                             @QueryParam("expand") String expand,
                                                             @QueryParam("returnSize") boolean returnSize,
                                                             @Context UriInfo uri) throws Exception {
    limit = parseLimit(limit);
    String username = currentUserId();
    
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    
    long fullSize = returnSize ? 0 : -1;
    List data = new LinkedList();
    Calendar calendar = service.getCalendarById(id);
    
    if (calendar != null) {
      String participant = null;
      if (calendar.getPublicUrl() == null && !hasViewCalendarPermission(calendar, username)) {
        participant = username;
      }

      EventQuery eventQuery = buildEventQuery(start, end, category, new String[] {id}, 
                                              id, participant, CalendarEvent.TYPE_EVENT, returnSize);
      ListAccess<CalendarEvent> events = evtDAO.findEventsByQuery(eventQuery);

      //
      for (CalendarEvent event : events.load(offset, limit)) {
        data.add(buildEventResource(event, uri, expand, fields));
      }        
      if (returnSize) {
        fullSize = events.getSize();
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    }
    //
    CollectionResource evData = new CollectionResource(data, fullSize);
    evData.setOffset(offset);
    evData.setLimit(limit);
    
    ResponseBuilder response = buildJsonP(evData, jsonp);
    
    if (returnSize) {
      response.header(HEADER_LINK, buildFullUrl(uri, offset, limit, fullSize));
    }
    
    //
    return response.build();
  }

  /**
   * Creates the event only if:
   * the authenticated user is the owner of the calendar
   * for group calendars, the authenticated user has edit rights on the calendar
   * the calendar has been shared with the authenticated user, with modification rights
   * the calendar has been shared with a group of the authenticated user, with modification rights
   */
  @POST
  @RolesAllowed("users")
  @Path("/calendars/{id}/events")
  public Response createEventForCalendar(@PathParam("id") String id, EventResource evObject, @Context UriInfo uriInfo) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      CalendarEvent evt = new CalendarEvent();
      buildEvent(evt, evObject);
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), id);      
        switch (calType) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().saveUserEvent(currentUserId(), id, evt, true);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().savePublicEvent(id, evt, true);
          break;
        case Calendar.TYPE_SHARED:
          calendarServiceInstance().saveEventToSharedCalendar(currentUserId(), id, evt, true);
          break;
        default:
          break;
        }
        
        String location = new StringBuilder(getBasePath(uriInfo)).append(EVENT_URI).append(evt.getId()).toString();
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, location).cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();

  }

  /**
   * An occurrence is represented as an Event resource type, without the recurrence information but with 
   * the recurrenceId. Returns an occurrence in the list when :
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}/occurrences")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOccurrencesFromEvent(@PathParam("id") String id,
                                                                     @QueryParam("offset") int offset,
                                                                     @QueryParam("limit") int limit,
                                                                     @QueryParam("start") String start,
                                                                     @QueryParam("end") String end,
                                                                     @QueryParam("fields") String fields,
                                                                     @QueryParam("jsonp") String jsonp,
                                                                     @QueryParam("expand") String expand,
                                                                     @Context UriInfo uriInfo) {
    try {
      limit = parseLimit(limit);
      java.util.Calendar[] dates = parseDate(start, end);
      
      CalendarEvent recurEvent = calendarServiceInstance().getEventById(id);
      if (recurEvent == null)  return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
      String timeZone = tz.getID();
      
      Map<String,CalendarEvent> occMap = calendarServiceInstance().getOccurrenceEvents(recurEvent, dates[0], dates[1], timeZone);
      if(occMap == null || occMap.isEmpty()) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      }
      
      Calendar cal = calendarServiceInstance().getCalendarById(recurEvent.getCalendarId());
      boolean inParticipant = false;
      if (recurEvent.getParticipant() != null) {
        String[] participant = recurEvent.getParticipant();
        Arrays.sort(participant);
        int i = Arrays.binarySearch(participant, currentUserId());
        if (i > -1) inParticipant = true;
      }
    
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {
        Collection data = new ArrayList();
        Iterator<CalendarEvent> evIter = occMap.values().iterator();
        Utils.skip(evIter, offset);
        
        int counter =0;
        while (evIter.hasNext()) {
          data.add(buildEventResource(evIter.next(), uriInfo, expand, fields));
          if(++counter == limit) break;
        }
        
        CollectionResource evData = new CollectionResource(data, occMap.values().size());
        evData.setOffset(offset);
        evData.setOffset(limit);
        
        //
        ResponseBuilder response = buildJsonP(evData, jsonp);
        
        response.header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize()));
        return response.build();
      }
      
      //
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Returns an task in the list when :
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/tasks/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasks(@QueryParam("startTime") String start,
                                          @QueryParam("endTime") String end,
                                          @QueryParam("category") String category,
                                          @QueryParam("offset") int offset,
                                          @QueryParam("limit") int limit,
                                          @QueryParam("fields") String fields,
                                          @QueryParam("jsonp") String jsonp,
                                          @QueryParam("expand") String expand,
                                          @QueryParam("returnSize") boolean returnSize,
                                          @Context UriInfo uriInfo) throws Exception {
    limit = parseLimit(limit);
    String username = currentUserId();
    
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    
    //find all viewable calendars of user: public, user, group, shared calendars,
    String[] calIds = findViewableCalendars(username);
    EventQuery eventQuery = buildEventQuery(start, end, category, calIds, null, username, CalendarEvent.TYPE_TASK, returnSize);
    ListAccess<CalendarEvent> events = evtDAO.findEventsByQuery(eventQuery);
    
    List data = new LinkedList();
    for (CalendarEvent event : events.load(offset, limit)) {
      data.add(buildTaskResource(event, uriInfo, expand, fields));
    }
    long fullSize = returnSize ? events.getSize() : -1;
    CollectionResource evData = new CollectionResource(data, fullSize);    
    evData.setOffset(offset);
    evData.setLimit(limit);
    
    ResponseBuilder response = buildJsonP(evData, jsonp);
    
    if (returnSize) {
      response.header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, fullSize));
    }
    
    //
    return response.build();
  }

  /**
   *
   * Returns the task if: same rules as /events/{id}
   * {@link #getEventById(String, String, String, String)}
   */
  @GET
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(@PathParam("id") String id,
                                                @QueryParam("fields") String fields,
                                                @QueryParam("expand") String expand,
                                                @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      boolean inParticipant = false;
      if (ev.getParticipant() != null) {
        String[] participant = ev.getParticipant();
        Arrays.sort(participant);
        if (Arrays.binarySearch(participant, currentUserId()) > -1) inParticipant = true;;
      }
    
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {        
        Object resource = buildTaskResource(ev, uriInfo, expand, fields);        
        return buildJsonP(resource, jsonp).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *
   * Updates the task if: same rules as /events/{id}
   * {@link #updateEventById(String, CalendarEvent)}
   */
  @PUT
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  public Response updateTaskById(@PathParam("id") String id, TaskResource evObject) {
    try {
      CalendarEvent old = calendarServiceInstance().getEventById(id);
      if (old == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(old.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = -1;
        try {
          calType = Integer.parseInt(old.getCalType());
        }catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), old.getCalendarId());
        } 
        buildEventFormTask(old, evObject);     
        switch (calType) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().saveUserEvent(currentUserId(), old.getCalendarId(), old, false);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().savePublicEvent(old.getCalendarId(), old, false);
          break;
        case Calendar.TYPE_SHARED:
          calendarServiceInstance().saveEventToSharedCalendar(currentUserId(), old.getCalendarId(), old,false);
          break;

        default:
          break;
        }
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }


  /**
   *  Deletes the task if: same rules as /events/{id}
   *  {@link #deleteEventById(String)} 
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  public Response deleteTaskById(@PathParam("id") String id) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = Calendar.TYPE_ALL;
        try {
          calType = Integer.parseInt(ev.getCalType());
        } catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), ev.getCalendarId());
        }
        switch (calType) {
        case Calendar.TYPE_PRIVATE:
          calendarServiceInstance().removeUserEvent(currentUserId(), ev.getCalendarId(), id);
          break;
        case Calendar.TYPE_PUBLIC:
          calendarServiceInstance().removePublicEvent(ev.getCalendarId(),id);
          break;
        case Calendar.TYPE_SHARED:
          calendarServiceInstance().removeSharedEvent(currentUserId(), ev.getCalendarId(), id);
          break;

        default:
          break;
        }
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Returns the attachment if: same rules as /events/{id}
   * {@link #getEventById(String, String, String, String)}
   */
  @GET
  @RolesAllowed("users")
  @Path("/attachments/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttachmentById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                    @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      CalendarEvent ev = this.findEventAttachment(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Attachment att = calendarServiceInstance().getAttachmentById(id);
      if(att == null)  return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      boolean inParticipant = false;
      if (ev.getParticipant() != null) {
        String[] participant = ev.getParticipant();
        Arrays.sort(participant);
        int i = Arrays.binarySearch(participant, currentUserId());
        if (i > -1) inParticipant = true;
      }
    
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {
          
        AttachmentResource evData = new AttachmentResource(att, getBasePath(uriInfo));
        Object resource = extractObject(evData, fields);
        if (jsonp != null) {
          String json = null;
          if (resource instanceof Map) json = new JSONObject(resource).toString();
          else {
            JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
            json = generatorImpl.createJsonObject(resource).toString();
          }
          StringBuilder sb = new StringBuilder(jsonp);
          sb.append('(').append(json).append(");");
          return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).build();
        }

        //
        return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).build();
      }

      //
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Deletes the attachment if: same rules as /events/{id}
   * {@link #deleteEventById(String)}
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/attachments/{id}")
  public Response deleteAttachmentById(@PathParam("id") String id) {
    try {
      CalendarEvent ev = this.findEventAttachment(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        calendarServiceInstance().removeAttachmentById(id);
        return Response.ok().cacheControl(cc).build();
      } 
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *  Returns the categories if an user is authenticated (the common categories + the personal categories)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/categories")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventCategories(@QueryParam("offset") int offset,
                                                          @QueryParam("limit") int limit,
                                                          @QueryParam("fields") String fields,
                                                          @QueryParam("jsonp") String jsonp,
                                                          @Context UriInfo uriInfo) {
    limit = parseLimit(limit);

    try {
      List<EventCategory> ecData = calendarServiceInstance().getEventCategories(currentUserId(), offset, limit);
      if(ecData == null || ecData.isEmpty()) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      Collection data = new ArrayList();
      
      String basePath = getBasePath(uriInfo);
      for(EventCategory ec:ecData) {
        data.add(extractObject(new CategoryResource(ec, basePath), fields));
      }

      CollectionResource resource = new CollectionResource(data, ecData.size());
      resource.setOffset(offset);
      resource.setLimit(limit);
      
      if (jsonp != null) {
        JsonValue json = new JsonGeneratorImpl().createJsonObject(resource);
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append('(').append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, resource.getSize())).cacheControl(cc).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, resource.getSize())).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }
  
  @GET
  @RolesAllowed("users")
  @Path("/categories/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventCategoryById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                       @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      List<EventCategory> data = calendarServiceInstance().getEventCategories(currentUserId());
      if(data == null || data.isEmpty()) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      }
      EventCategory category = null;
      for (int i = 0; i < data.size(); i++) {
        if(id.equals(data.get(i).getId())) {
          category = data.get(i);
          break;
        }
      }
      
      if(category == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      CategoryResource categoryR = new CategoryResource(category, getBasePath(uriInfo));
      Object resource = extractObject(categoryR, fields);
      if (jsonp != null) {
        String json = null;
        if (resource instanceof Map) json = new JSONObject((Map<?, ?>)resource).toString();
        else {
          JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
          json = generatorImpl.createJsonObject(resource).toString();
        }
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append('(').append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *
   *  Gets a feed with the given id    
   *  Returns the feed if the authenticated user is the owner of the feed
   */
  @GET
  @RolesAllowed("users")
  @Path("/feeds/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedById(@PathParam("id") String id,
                                                @QueryParam("fields") String fields,
                                                @QueryParam("expand") String expand,
                                                @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo) {
    try {
      FeedData feed = null;
      for (FeedData feedData : calendarServiceInstance().getFeeds(currentUserId())) {
        if (feedData.getTitle().equals(id)) {
          feed = feedData;
          break;
        }        
      }
      
      if(feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed syndFeed = input.build(new XmlReader(new ByteArrayInputStream(feed.getContent())));
      List<SyndEntry> entries = new ArrayList<SyndEntry>(syndFeed.getEntries());
      List<String> calIds = new ArrayList<String>();
      for (SyndEntry entry : entries) {
        String calendarId = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
        calIds.add(calendarId);
      }
      
      Object resource = buildFeedResource(feed, calIds, uriInfo, expand, fields);        
      return buildJsonP(resource, jsonp).build();      
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Updates a feed with the given id   
   * Updates the feed if the authenticated user is the owner of the feed 
   */
  @PUT
  @RolesAllowed("users")
  @Path("/feeds/{id}")
  public Response updateFeedById(@PathParam("id") String id, FeedResource feedResource) {
    try {
      FeedData feed = null;
      for (FeedData feedData : calendarServiceInstance().getFeeds(currentUserId())) {
        if (feedData.getTitle().equals(id)) {
          feed = feedData;
          break;
        }
      }

      if (feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();

      LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
      for (String calendarId : feedResource.getCalendarIds()) {
        Calendar calendar = calendarServiceInstance().getCalendarById(calendarId);
        int calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), calendarId);
        switch (calType) {
          case Calendar.TYPE_PRIVATE:
            calendars.put(Calendar.TYPE_PRIVATE + Utils.COLON + calendarId, calendar);
            break;
          case Calendar.TYPE_PUBLIC:
            calendars.put(Calendar.TYPE_PUBLIC + Utils.COLON + calendarId, calendar);
            break;
          case Calendar.TYPE_SHARED:
            calendars.put(Calendar.TYPE_SHARED + Utils.COLON + calendarId, calendar);
            break;
          default:
            break;
        }
      }
      
      //
      calendarServiceInstance().removeFeedData(currentUserId(),id);
      
      RssData rssData = new RssData();      
      rssData.setName(feedResource.getName() + Utils.RSS_EXT) ;
      rssData.setUrl(feed.getUrl()) ;
      rssData.setTitle(feedResource.getName()) ;
      rssData.setDescription(feedResource.getName());
      rssData.setLink(feed.getUrl());
      rssData.setVersion("rss_2.0") ;
      //
      calendarServiceInstance().generateRss(currentUserId(), calendars, rssData);
      
      return Response.ok().cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Deletes a feed with the given id    
   * Deletes the feed if the authenticated user is the owner of the feed 
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/feeds/{id}")
  public Response deleteFeedById(@PathParam("id") String id) {
    try {
      calendarServiceInstance().removeFeedData(currentUserId(),id);
      return Response.ok().cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   *
   *  Gets the RSS stream of the feed with the given id
   *  
   *  Returns the RSS stream if:
   *   - the calendar is public
   *   - the authenticated user is the owner of the calendar
   *   - the authenticated user belongs to the group of the calendar
   *   - the calendar has been shared with the authenticated user or with a group of the authenticated user
   */
  @GET
  @RolesAllowed("users")
  @Path("/feeds/{id}/rss")
  @Produces(MediaType.APPLICATION_XML)
  public Response getRssFromFeed(@PathParam("id") String id, @Context UriInfo uri) {
    try {
      String username = currentUserId();
      String feedname = id;
      FeedData feed = null;
      for (FeedData feedData : calendarServiceInstance().getFeeds(username)) {
        if (feedData.getTitle().equals(feedname)) {
          feed = feedData;
          break;
        }
      }

      if (feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed syndFeed = input.build(new XmlReader(new ByteArrayInputStream(feed.getContent())));
      List<SyndEntry> entries = new ArrayList<SyndEntry>(syndFeed.getEntries());
      List<CalendarEvent> events = new ArrayList<CalendarEvent>();
      List<Calendar> calendars = new ArrayList<Calendar>();
      for (SyndEntry entry : entries) {
        String calendarId = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
        calendars.add(calendarServiceInstance().getCalendarById(calendarId));
      }
      
      for (Calendar cal : calendars) {
        if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, username)) {
          int calType = calendarServiceInstance().getTypeOfCalendar(username, cal.getId());
          switch (calType) {
            case Calendar.TYPE_PRIVATE:
              events.addAll(calendarServiceInstance().getUserEventByCalendar(username, Arrays.asList(cal.getId())));
              break;
            case Calendar.TYPE_SHARED:
              events.addAll(calendarServiceInstance().getSharedEventByCalendars(username, Arrays.asList(cal.getId())));
              break;
            case Calendar.TYPE_PUBLIC:
              EventQuery eventQuery = new EventQuery();
              eventQuery.setCalendarId(new String[] { cal.getId() });
              events.addAll(calendarServiceInstance().getPublicEvents(eventQuery));
              break;
            default:
              break;
          }
        }
      }

      if(events.size() == 0) {
        return Response.status(HTTPStatus.NOT_FOUND).entity("Feed " + feedname + "is removed").cacheControl(cc).build();
      } 
      return Response.ok(makeFeed(username, events, feed, uri), MediaType.APPLICATION_XML).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
  }

  /**
   * Returns an invitation in the list if:<br/>
   * the authenticated user is the participant of the invitation<br/>
   * the authenticated user has edit rights on the calendar of the event of the invitation<br/>
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/invitations")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInvitations(@QueryParam("startTime") String start,
                                                  @QueryParam("endTime") String end,
                                                  @QueryParam("offset") int offset,
                                                  @QueryParam("limit") int limit,
                                                  @QueryParam("fields") String fields,
                                                  @QueryParam("jsonp") String jsonp,
                                                  @QueryParam("expand") String expand,
                                                  @QueryParam("returnSize") boolean returnSize,
                                                  @Context UriInfo uri) throws Exception {
    java.util.Calendar[] dates = parseDate(start, end);
    limit = parseLimit(limit);
    //uInvites and eInvites currently load duplicated results
    MultiListAccess invitations = new MultiListAccess(true);
    String username = currentUserId();

    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    //Find all invitations that user is participant
    EventQuery uQuery = new EventQuery();
    uQuery.setFromDate(dates[0]);
    uQuery.setToDate(dates[1]);
    uQuery.setParticipants(new String[] {username});
    ListAccess<Invitation> uInvites = evtDAO.findInvitationsByQuery(uQuery);
    //
    invitations.add(uInvites);

    //Find invitations of editable calendars    
    String[] calendarIds = findEditableCalendars(username);
    if (calendarIds.length > 0) {
      EventQuery eQuery = new EventQuery();
      eQuery.setFromDate(dates[0]);
      eQuery.setToDate(dates[1]);
      eQuery.setCalendarId(calendarIds);
      ListAccess<Invitation> eInvites = evtDAO.findInvitationsByQuery(eQuery);
      //
      invitations.add(eInvites);
    }
    
    List data = new LinkedList();
    for (Object invitation : invitations.load(offset, limit)) {
      data.add(buildInvitationResource((Invitation)invitation, uri, expand, fields));
    }
    long fullSize = returnSize ? invitations.getSize() : -1;
    CollectionResource ivData = new CollectionResource(data, fullSize);    
    ivData.setOffset(offset);
    ivData.setLimit(limit);

    ResponseBuilder response = buildJsonP(ivData, jsonp);
    
    if (returnSize) {
      response.header(HEADER_LINK, buildFullUrl(uri, offset, limit, fullSize));
    }
    
    //
    return response.build();
  }


  /**
   * Returns the invitation if:<br/>
   * the authenticated user is the participant of the invitation<br/>
   * the authenticated user has edit rights on the calendar of the event of the invitation<br/>
   */
  @GET
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInvitationById(@PathParam("id") String id, 
                                    @QueryParam("fields") String fields, 
                                    @QueryParam("jsonp") String jsonp,
                                    @QueryParam("expand") String expand,
                                    @Context UriInfo uriInfo) throws Exception {
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    Invitation invitation = evtDAO.getInvitationById(id);
    if (invitation == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    
    //dont return invitation if user is not participant and not have edit permission
    if (!username.equals(invitation.getParticipant())) {
      CalendarEvent event = service.getEventById(invitation.getEventId());
      Calendar calendar = service.getCalendarById(event.getCalendarId());

      if (!Utils.isCalendarEditable(username, calendar)) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      }
    }

    Object resource = buildInvitationResource(invitation, uriInfo, expand, fields);
    return buildJsonP(resource, jsonp).build();
  }

  /**
   *  Updates the invitation if the authenticated user is the participant of the invitation
   */
  @PUT
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  public Response updateInvitationById(@PathParam("id") String id, @QueryParam("status") String status) {
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    Invitation invitation = evtDAO.getInvitationById(id);
    if (invitation != null) {
      //Update only if user is participant
      if (invitation.getParticipant().equals(username)) {
        evtDAO.updateInvitation(id, status);
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    }    
  }

  /**
   * Deletes the invitation if the authenticated user has edit rights on the calendar of the event of the invitation
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  public Response deleteInvitationById(@PathParam("id") String invitationId) throws Exception {
    CalendarService calService = calendarServiceInstance();
    EventDAO evtDAO = calService.getEventDAO();
    String username = currentUserId();

    Invitation invitation = evtDAO.getInvitationById(invitationId);
    if (invitation == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();

    CalendarEvent event = calService.getEventById(invitation.getEventId());
    Calendar calendar = calService.getCalendarById(event.getCalendarId());

    if (Utils.isCalendarEditable(username, calendar)) {
      evtDAO.removeInvitation(invitationId);
      return Response.ok().cacheControl(cc).build();
    } else {
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
    }
  }

  /**
   * Returns an invitation in the list when:
   * the authenticated user is the participant of the invitation
   * the authenticated user has edit rights on the calendar of the event of the invitation
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}/invitations/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInvitationsFromEvent(@PathParam("id") String eventId,
                                                                  @QueryParam("offset") int offset, 
                                                                  @QueryParam("limit") int limit,
                                                                  @QueryParam("status") String status, 
                                                                  @QueryParam("fields") String fields,
                                                                  @QueryParam("jsonp") String jsonp,
                                                                  @QueryParam("expand") String expand,
                                                                  @Context UriInfo uriInfo) throws Exception {
    limit = parseLimit(limit);
    CalendarService calService = calendarServiceInstance();

    CalendarEvent event = calService.getEventById(eventId);
    String username = currentUserId();

    List<Invitation> invitations = Collections.<Invitation>emptyList();
    if (event != null) {
      //All invitations in event
      invitations = new LinkedList<Invitation>(Arrays.asList(event.getInvitations()));
      
      //Only return user's invitation if calendar is not editable
      Calendar calendar = calService.getCalendarById(event.getCalendarId());
      if (!Utils.isCalendarEditable(username, calendar)) {
        Iterator<Invitation> iter = invitations.iterator();
        
        while(iter.hasNext()) {
          if (!iter.next().getParticipant().equals(username)) {
            iter.remove();
          }
        }
      }
      
      //Return only invitation with specific status
      if (status != null) {
        Iterator<Invitation> iter = invitations.iterator();
        while(iter.hasNext()) {
          if (!iter.next().getStatus().equals(status)) {
            iter.remove();
          }
        }
      }
    }
    
    List data = new LinkedList();
    for (Invitation invitation : Utils.subList(invitations, offset, limit)) {
      data.add(buildInvitationResource(invitation, uriInfo, expand, fields));
    }
    int fullSize = invitations.size();
    
    CollectionResource ivData = new CollectionResource(data, fullSize);    
    ivData.setOffset(offset);
    ivData.setLimit(limit);

    ResponseBuilder response = buildJsonP(ivData, jsonp);
    response.header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, fullSize));
    return response.build();
  }

  @POST
  @RolesAllowed("users")
  @Path("/events/{id}/invitations/")
  public Response createInvitationForEvent(@PathParam("id") String id, @QueryParam("participant") String participant, 
                                           @QueryParam("status") String status, @Context UriInfo uriInfo) throws Exception {
    if (participant == null || participant.trim().isEmpty() || status == null) {
      return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc).build();
    }

    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    CalendarEvent event = service.getEventById(id);
    if (event != null) {
      Calendar calendar = service.getCalendarById(event.getCalendarId());
      if (!Utils.isCalendarEditable(username, calendar)) {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(cc).build();
      }
      
      Invitation invite = evtDAO.createInvitation(id, participant, status);
      if (invite != null) {
        String location = new StringBuilder(getBasePath(uriInfo)).append(INVITATION_URI).append(invite.getId()).toString();
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, location).cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc).build();
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
    }
  }

  /**
   * Parse date by ISO8601 standard
   * if start is null, start is current time
   * if end is null, end is current time plus 1 week
   * @param start
   * @param end
   * @return array of start, end date
   */
  private java.util.Calendar[] parseDate(String start, String end) {
    java.util.Calendar from = GregorianCalendar.getInstance();
    java.util.Calendar to = GregorianCalendar.getInstance();
    if(Utils.isEmpty(start)) {
      from = java.util.Calendar.getInstance();
      from.set(java.util.Calendar.HOUR, 0);
      from.set(java.util.Calendar.MINUTE, 0);
      from.set(java.util.Calendar.SECOND, 0);
      from.set(java.util.Calendar.MILLISECOND, 0);
    } else {
      from = ISO8601.parse(start);
    }
    if(Utils.isEmpty(end)) {
      to.add(java.util.Calendar.WEEK_OF_MONTH, 1);
    } else {
      to = ISO8601.parse(end);
    }
    return new java.util.Calendar[] {from, to};
  }
  
  /**
   * Doesn't allow limit parameter to exceed the default query_limit
   */
  private int parseLimit(int limit) {
    return (limit <=0 || limit > query_limit) ? query_limit : limit;
  }
  
  private String getBasePath(UriInfo uriInfo) {
    StringBuilder path = new StringBuilder(uriInfo.getBaseUri().toString());
    path.append(CAL_BASE_URI);
    return path.toString();
  }
  
  private String buildFullUrl(UriInfo uriInfo, int offset, int limit, long fullSize) {
      if (fullSize <= 0) {
        return "";
      }
      offset = offset < 0 ? 0 : offset;
      
      long prev = offset - limit;
      prev = offset > 0 && prev < 0 ? 0 : prev;
      long prevLimit = offset - prev;
      //
      StringBuilder sb = new StringBuilder();
      if (prev >= 0) {
        sb.append("<").append(uriInfo.getAbsolutePath()).append("?offset=");
        sb.append(prev).append("&limit=").append(prevLimit).append(">").append(Utils.SEMICOLON).append("rel=\"previous\",");
      }
      
      long next = offset + limit;
      //
      if (next < fullSize) {
        sb.append("<").append(uriInfo.getAbsolutePath()).append("?offset=");
        sb.append(next).append("&limit=").append(limit).append(">").append(Utils.SEMICOLON).append("rel=\"next\",");
      }
      
      //first page
      long firstLimit = limit > fullSize ? fullSize : limit;
      sb.append("<").append(uriInfo.getAbsolutePath()).append("?offset=0&limit=").append(firstLimit).append(">");
      sb.append(Utils.SEMICOLON).append("rel=\"first\",");
      //last page
      long lastIndex = fullSize - (fullSize % firstLimit);
      if (lastIndex == fullSize) {
        lastIndex = fullSize - firstLimit;
      }
      if (lastIndex > 0) {
        sb.append("<").append(uriInfo.getAbsolutePath()).append("?offset=").append(lastIndex);
        sb.append("&limit=").append(fullSize - lastIndex).append(">");
        sb.append(Utils.SEMICOLON).append("rel=\"last\"");
      }
      if (sb.charAt(sb.length() - 1) == ',') {
        sb.deleteCharAt(sb.length() - 1);
      }
      
      return sb.toString();
  }
  
  private static CalendarService calendarServiceInstance() {
    return (CalendarService)ExoContainerContext.getCurrentContainer()
              .getComponentInstanceOfType(CalendarService.class);
  }
  
  /**
   * 
   * @param auhtor : the feed create
   * @param events : list of event from data
   * @return
   * @throws Exception
   */
  private String makeFeed(String author, List<CalendarEvent> events, FeedData feedData, UriInfo uri) throws Exception {
    URI baseUri = uri.getBaseUri();
    String baseURL = baseUri.getScheme() + "://" + baseUri.getHost() + ":" + Integer.toString(baseUri.getPort());
    String baseRestURL = baseUri.toString();

    SyndFeed feed = new SyndFeedImpl();      
    feed.setFeedType("rss_2.0");
    feed.setTitle(feedData.getTitle());
    feed.setLink(baseURL + feedData.getUrl());
    feed.setDescription(feedData.getTitle());     
    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    SyndEntry entry;
    SyndContent description; 
    for(CalendarEvent event : events) {
      if (Utils.EVENT_NUMBER > 0 && Utils.EVENT_NUMBER <= entries.size()) break;
      entry = new SyndEntryImpl();
      entry.setTitle(event.getSummary());
      entry.setLink(baseRestURL + BASE_EVENT_URL + Utils.SLASH + author + Utils.SLASH + event.getId() 
                    + Utils.SPLITTER + event.getCalType() + Utils.ICS_EXT);    
      entry.setAuthor(author) ;
      description = new SyndContentImpl();
      description.setType(Utils.MIMETYPE_TEXTPLAIN);
      description.setValue(event.getDescription());
      entry.setDescription(description);        
      entries.add(entry);
      entry.getEnclosures() ;
    }
    feed.setEntries(entries);      
    feed.setEncoding("UTF-8") ;     
    SyndFeedOutput output = new SyndFeedOutput();      
    String feedXML = output.outputString(feed);      
    feedXML = StringUtils.replace(feedXML,"&amp;","&");  
    return feedXML;
  }

  private boolean isInGroups(String[] groups) {
  	Identity identity = ConversationState.getCurrent().getIdentity();
  	for (String group : groups) {
  		if (identity.isMemberOf(group)) {
  			return true;
  		}
  	}

  	return false;
	}
  
  private boolean hasViewCalendarPermission(Calendar cal, String username) throws Exception {
    if (cal.getCalendarOwner() != null && cal.getCalendarOwner().equals(username)) return true;
    else if (cal.getGroups() != null) {
      return isInGroups(cal.getGroups());
    } else if (cal.getViewPermission() != null) {
      return Utils.canEdit(orgService, cal.getViewPermission(), username);
    }
    return false;
  }
  
  private String[] findViewableCalendars(String username) throws Exception {
    CalendarService service = calendarServiceInstance();
    CalendarCollection<Calendar> calendars = service.getAllCalendars(username, Calendar.TYPE_ALL, 0, -1);
    List<String> calIds = new LinkedList<String>();
    for (Calendar cal : calendars) {
      calIds.add(cal.getId());
    }
    
    Calendar[] pubCals = service.getPublicCalendars().load(0, -1); 
    for (Calendar cal : pubCals) {
      calIds.add(cal.getId());
    }
    return calIds.toArray(new String[calIds.size()]);
  }
  
  private String[] findEditableCalendars(String username) {
    CalendarCollection<Calendar> calendars = calendarServiceInstance().getAllCalendars(username, Calendar.TYPE_ALL, 0, -1);
    Iterator<Calendar> iter = calendars.iterator();
    while (iter.hasNext()) {
      if (!Utils.isCalendarEditable(username, iter.next())) {
        iter.remove();
      }
    }
    String[] calendarIds = new String[calendars.size()];
    int i = 0;
    for (Calendar cal : calendars) {
      calendarIds[i++] = cal.getId();
    }
    return calendarIds;
  }
  
  private EventQuery buildEventQuery(String start, String end, String category, String[] calIds, String calendarPath,
                                               String participant, String eventType, boolean returnSize) {
    java.util.Calendar[] dates = parseDate(start, end);    
    
    //Find all invitations that user is participant
    EventQuery uQuery = new RestEventQuery();
    uQuery.setQueryType(Query.SQL);
    uQuery.setCalendarPath(calendarPath);
    uQuery.setCalendarId(calIds);
    if (category != null) {
      uQuery.setCategoryId(new String[] {category});      
    }
    if (participant != null) {
      uQuery.setParticipants(new String[] {participant});      
    }
    uQuery.setEventType(eventType);
    uQuery.setFromDate(dates[0]);
    uQuery.setToDate(dates[1]);
    uQuery.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    uQuery.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    return uQuery;
  }
  
  private CalendarEvent findEventAttachment(String attachmentID) throws Exception {
    int idx = attachmentID.indexOf("/calendars/");
    if (idx != -1) {
      int calendars =  idx + "/calendars/".length();
      int calendar = attachmentID.indexOf('/', calendars) + 1;
      int event = attachmentID.indexOf('/', calendar);
      if (calendar != -1 && event != -1) {
        String eventId = attachmentID.substring(calendar, event);
        return calendarServiceInstance().getEventById(eventId);      
      }      
    }
    return null;
  }
  
  private Object extractObject(Resource iv, String fields) {
    if (fields != null && iv != null) {
      String[] f = fields.split(",");
      
      if (f.length > 0) {
        JSONObject obj = new JSONObject(iv);        
        Map<String, Object> map = new HashMap<String, Object>();
        
        for (String name : f) {
          try {
            map.put(name, obj.get(name));
          } catch (JSONException e) {
            log.warn("Can't extract property {} from object {}", name, iv);            
          }
        }
        return map;
      }
    }
    return iv;
  }
  
  private String currentUserId() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }
  
  private void buildEvent(CalendarEvent old, EventResource evObject) {
    old.setDescription(evObject.getDescription());
    old.setEventState(evObject.getAvailability());
    if (evObject.getRepeat() != null) {
      RepeatResource repeat = evObject.getRepeat();
      if (repeat.getExclude() != null) {
        old.setExcludeId(repeat.getExclude());        
      } else {
        old.setExceptionIds(null);
      }
      if (repeat.getRepeatOn() != null) {
        old.setRepeatByDay(repeat.getRepeatOn().split(","));        
      } else {
        old.setRepeatByDay(null);
      }
      if (repeat.getRepeateBy() != null) {
        String[] repeatBy = repeat.getRepeateBy().split(",");
        long[] by = new long[repeatBy.length];
        for (int i = 0; i < repeatBy.length; i++) {
          try {
            by[i] = Integer.parseInt(repeatBy[i]);
          } catch (Exception e) {
          }
        }
        old.setRepeatByMonthDay(by);        
      } else {
        old.setRepeatByMonthDay(null);
      }
      
      if (repeat.getEnd() != null) {
        End end = repeat.getEnd();
        String val = end.getValue();
        if (val != null) {
          try {
            old.setRepeatUntilDate(ISO8601.parse(val).getTime());
          } catch (Exception e) {
            try {
              old.setRepeatCount(Long.parseLong(end.getValue()));                        
            } catch (Exception ex) {}
          }          
        }
        old.setRepeatType(end.getType());        
      }
      
      old.setRepeatInterval(repeat.getEvery());
    } else {
      old.setRepeatType(null);
    }
    old.setFromDateTime(ISO8601.parse(evObject.getFrom()).getTime());
    old.setLocation(evObject.getLocation());
    old.setPriority(evObject.getPriority());
    if (evObject.getReminder() != null) {
      old.setReminders(Arrays.asList(evObject.getReminder()));      
    } else {
      old.setReminders(null);
    }
    old.setStatus(evObject.getPrivacy());
    old.setSummary(evObject.getSubject());
    old.setToDateTime(ISO8601.parse(evObject.getTo()).getTime());
  }
  
  private void buildEventFormTask(CalendarEvent old, TaskResource evObject) {
    old.setDescription(evObject.getNote());  
    old.setFromDateTime(ISO8601.parse(evObject.getFrom()).getTime());
    old.setPriority(evObject.getPriority());
    if (evObject.getReminder() != null) {
      old.setReminders(Arrays.asList(evObject.getReminder()));      
    } else {
      evObject.setReminder(null);
    }
    old.setStatus(evObject.getStatus());
    old.setSummary(evObject.getName());
    old.setToDateTime(ISO8601.parse(evObject.getTo()).getTime());
    if (evObject.getDelegation() != null) {
      old.setTaskDelegator(StringUtils.join(evObject.getDelegation(), ","));
    } else {
      old.setTaskDelegator(null);
    }
  }
  
  private void buildCalendar(Calendar cal, CalendarResource calR) {
    cal.setCalendarColor(calR.getColor());
    cal.setCalendarOwner(calR.getOwner());    
    cal.setDescription(calR.getDescription());
    if (calR.getEditPermission() != null) {
      cal.setEditPermission(calR.getEditPermission().split(Utils.SEMICOLON));      
    } else {
      cal.setEditPermission(null);
    }
    cal.setGroups(calR.getGroups());
    cal.setName(calR.getName());
    cal.setPrivateUrl(calR.getPrivateURL());
    cal.setPublicUrl(calR.getPublicURL());
    cal.setTimeZone(calR.getTimeZone());
    if (calR.getViewPermision() != null) {
      cal.setViewPermission(calR.getViewPermision().split(Utils.SEMICOLON));         
    } else {
      cal.setViewPermission(null);
    }
  }
  
  private ResponseBuilder buildJsonP(Object resource, String jsonp) throws Exception {
    ResponseBuilder response = null;
    if (jsonp != null) {
      String json = null;
      if (resource instanceof Map) json = new JSONObject((Map<?, ?>)resource).toString();
      else {
        JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
        json = generatorImpl.createJsonObject(resource).toString();
      }
      StringBuilder sb = new StringBuilder(jsonp);
      sb.append('(').append(json).append(");");
      response = Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc);
    } else {
      response = Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc);
    }

    return response;
  }
  
  private Object buildTaskResource(CalendarEvent event,
                                   UriInfo uriInfo,
                                   String expand,
                                   String fields) throws Exception {
    CalendarService service = calendarServiceInstance();
    String basePath = getBasePath(uriInfo);
    TaskResource evtResource = new TaskResource(event, basePath);
    
    List<Expand> expands = Expand.parse(expand);
    for (Expand exp : expands) {
      if ("calendar".equals(exp.getField())) {
        Calendar cal = service.getCalendarById(event.getCalendarId());
        evtResource.setCal(new CalendarResource(cal, basePath));
      } else if ("categories".equals(exp.getField())) {
        String categoryId = event.getEventCategoryId();
        if (categoryId != null) {
          EventCategory evCat = service.getEventCategory(currentUserId(), categoryId);
          if (evCat != null) {
            CategoryResource[] catRs = new CategoryResource[] {new CategoryResource(evCat, basePath)};
            evtResource.setCats(Utils.<CategoryResource>subArray(catRs, exp.getOffset(), exp.getLimit()));
          }
        }
      } else if ("attachments".equals(exp.getField())) {
        if (event.getAttachment() != null) {
          List<AttachmentResource> attRs = new LinkedList<AttachmentResource>();
          for (Attachment att : event.getAttachment()) {
            attRs.add(new AttachmentResource(att, basePath));
          }
          attRs = Utils.subList(attRs, exp.getOffset(), exp.getLimit());
          evtResource.setAtts(attRs.toArray(new AttachmentResource[attRs.size()]));
        }
      }      
    }

    return extractObject(evtResource, fields);    
  }

  private Object buildEventResource(CalendarEvent ev, UriInfo uriInfo, String expand, String fields) throws Exception {
    CalendarService service = calendarServiceInstance();
    String basePath = getBasePath(uriInfo);
    EventResource evtResource = new EventResource(ev, basePath);
    
    List<Expand> expands = Expand.parse(expand);
    for (Expand exp : expands) {
      if ("calendar".equals(exp.getField())) {
        Calendar cal = service.getCalendarById(ev.getCalendarId());
        evtResource.setCal(new CalendarResource(cal, basePath));
      } else if ("categories".equals(exp.getField())) {
        String categoryId = ev.getEventCategoryId();
        if (categoryId != null) {
          EventCategory evCat = service.getEventCategory(currentUserId(), categoryId);
          if (evCat != null) {
            CategoryResource[] catRs = new CategoryResource[] {new CategoryResource(evCat, basePath)};
            evtResource.setCats(Utils.<CategoryResource>subArray(catRs, exp.getOffset(), exp.getLimit()));
          }
        }
      } else if ("originalEvent".equals(exp.getField())) {
        String orgId = ev.getOriginalReference();
        if (orgId != null) {
          CalendarEvent orgEv = service.getEventById(orgId);
          if (orgEv != null) {
            evtResource.setOEvent(new EventResource(orgEv, basePath));
          }
        }
      } else if ("attachments".equals(exp.getField())) {
        if (ev.getAttachment() != null) {
          List<AttachmentResource> attRs = new LinkedList<AttachmentResource>();
          for (Attachment att : ev.getAttachment()) {
            attRs.add(new AttachmentResource(att, basePath));
          }
          attRs = Utils.subList(attRs, exp.getOffset(), exp.getLimit());
          evtResource.setAtts(attRs.toArray(new AttachmentResource[attRs.size()]));
        }
      }      
    }

    return extractObject(evtResource, fields);    
  }
  
  private Object buildFeedResource(FeedData feed, List<String> calIds, UriInfo uriInfo, 
                                   String expand, String fields) throws Exception {
    CalendarService service = calendarServiceInstance();
    String basePath = getBasePath(uriInfo);
    FeedResource feedResource = new FeedResource(feed, calIds.toArray(new String[calIds.size()]), basePath);
    
    List<Expand> expands = Expand.parse(expand);
    for (Expand exp : expands) {
      if ("calendars".equals(exp.getField())) {
        List<Serializable> calendars = new ArrayList<Serializable>();
        for(String calId : Utils.subList(calIds, exp.getOffset(), exp.getLimit())) {        
          calendars.add(new CalendarResource(service.getCalendarById(calId), getBasePath(uriInfo)));
        }      
        feedResource.setCals(Utils.subList(calendars, exp.getOffset(), exp.getLimit()));
      }      
    }
    
    return extractObject(feedResource, fields);
  }
  
  private Object buildInvitationResource(Invitation invitation,
                                         UriInfo uriInfo,
                                         String expand,
                                         String fields) throws Exception {
    CalendarService service = calendarServiceInstance();
    String basePath = getBasePath(uriInfo);
    InvitationResource ivtResource = new InvitationResource(invitation, basePath);
    
    List<Expand> expands = Expand.parse(expand);
    for (Expand exp : expands) {
      if ("event".equals(exp.getField())) {
        CalendarEvent event = service.getEventById(invitation.getEventId());
        ivtResource.setEvt(new EventResource(event, basePath));
      }      
    }

    return extractObject(ivtResource, fields);    
  }
  
  public static class Expand {
    private String field;
    private int offset;
    private int limit;
    
    public Expand(String field, int offset, int limit) {
      this.field = field;
      this.offset = offset;
      this.limit = limit;
    }
    
    public static List<Expand> parse(String expand) {
      List<Expand> expands = new LinkedList<CalendarRestApi.Expand>();
      
      if (expand != null) {
        String[] frags = expand.split(",");
        List<String> tmp = new LinkedList<String>();
        for (int i = 0; i < frags.length; i++) {
          String str = frags[i].trim();
          if (!str.contains("(") && str.contains("")) {
            tmp.add(str);
          } else if (str.contains("(") && i + 1 < frags.length) {
            tmp.add(str + "," + frags[++i]);
          }
        }
                
        for (String exp : tmp) {        
          String fieldName = null;
          int offset = 0;
          int limit = -1;
          if (exp != null) {
            exp = exp.trim();
            int i =exp.indexOf('('); 
            if (i > 0) {
              fieldName = exp.substring(0, i).trim();
              try {
                offset = Integer.parseInt(exp.substring(exp.indexOf("offset:") + "offset:".length(), exp.indexOf(',')).trim());
                limit = Integer.parseInt(exp.substring(exp.indexOf("limit:") + "limit:".length(), exp.indexOf(')')).trim());
              } catch (Exception ex) {            
              }
            } else {
              fieldName = exp;
            }
          }
          
          expands.add(new Expand(fieldName, offset, limit));
        }      
      }
      
      return expands;
    }
    
    public String getField() {
      return field;
    }
    
    public int getOffset() {
      return offset;
    }
    
    public int getLimit() {
      return limit;
    }
  }
}
