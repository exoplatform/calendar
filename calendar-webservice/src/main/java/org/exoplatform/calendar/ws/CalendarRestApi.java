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
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.ws.bean.AttachmentResource;
import org.exoplatform.calendar.ws.bean.CalendarResource;
import org.exoplatform.calendar.ws.bean.CategoryResource;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.calendar.ws.bean.ErrorResource;
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
import org.exoplatform.container.xml.ValueParam;
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

/**
 * This rest service class provides entry point for calendar resources.
 */
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

  private final static CacheControl nc = new CacheControl();

  public static final String DEFAULT_CAL_NAME = "calendar";
  
  public static final String DEFAULT_EVENT_NAME = "default";
  
  public static final String[] RP_WEEKLY_BYDAY = CalendarEvent.RP_WEEKLY_BYDAY.clone();
  
  public static final String[] EVENT_AVAILABILITY = {CalendarEvent.ST_AVAILABLE, CalendarEvent.ST_BUSY, CalendarEvent.ST_OUTSIDE};
  
  public static final String[] REPEATTYPES = CalendarEvent.REPEATTYPES.clone();
  
  public static final String[] PRIORITY = CalendarEvent.PRIORITY.clone();
  
  public static final String[] TASK_STATUS = CalendarEvent.TASK_STATUS.clone();

  private static final String[] INVITATION_STATUS = {"", "maybe", "yes", "no"};
  
  static {
    Arrays.sort(RP_WEEKLY_BYDAY);
    Arrays.sort(EVENT_AVAILABILITY);
    Arrays.sort(REPEATTYPES);
    Arrays.sort(PRIORITY);
    Arrays.sort(TASK_STATUS);
    Arrays.sort(INVITATION_STATUS);
  }

  private final CacheControl cc = new CacheControl();
  
  static {
    nc.setNoCache(true);
    nc.setNoStore(true);    
  }

  private Log log = ExoLogger.getExoLogger(CalendarRestApi.class);
  
  /**
   * Constructor helps to configure the rest service with parameters.
   *
   * Here is the configuration parameters:
   * - query_limit        maximum number of objects returned by a collection query, default value: 10.
   * - cache_maxage       time in milliseconds returned in the cache-control header, default value:  604800.
   *
   * @param  orgService
   *         eXo organization service implementation.
   *
   * @param  params
   *         Object contains the configuration parameters.
   */
  public CalendarRestApi(OrganizationService orgService, InitParams params) {
    this.orgService = orgService;
    
    int maxAge = 604800;
    if (params != null) {
      if (params.getValueParam("query_limit") != null) {
        query_limit = Integer.parseInt(params.getValueParam("query_limit").getValue());        
      }
      
      ValueParam cacheConfig = params.getValueParam("cache_maxage");
      if (cacheConfig != null) {
        try {
          maxAge = Integer.parseInt(cacheConfig.getValue());
        } catch (Exception ex) {
          log.warn("Can't parse {} to maxAge, use the default value {}", cacheConfig, maxAge);
        }
      }
    }
    cc.setPrivate(true);
    cc.setMaxAge(maxAge);
    cc.setSMaxAge(maxAge);
  }

  /**
   * Returns all the available sub-resources of this API in JSON.
   *
   * @request  GET: http://localhost:8080/rest/private/v1/calendar
   *
   * @format  JSON
   *
   * @response
   *    {
   *        "subResourcesHref": [
   *            "http://localhost:8080/rest/private/v1/calendar/calendars",
   *            "http://localhost:8080/rest/private/v1/calendar/events",
   *            "http://localhost:8080/rest/private/v1/calendar/tasks"
   *         ]
   *     }
   *
   * @return  URLs of all REST services provided by this API, in absolute form.
   *
   * @authentication
   *
   * @anchor  CalendarRestApi.getSubResources
   */
  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSubResources(@Context UriInfo uri) {
    Map<String, String[]> subResources = new HashMap<String, String[]>();
    subResources.put("subResourcesHref", subResourcesBuilder.buildResourceMap(uri));

    return Response.ok(subResources, MediaType.APPLICATION_JSON).cacheControl(nc).build();
  }

  /**
   * Searches for calendars by a type (personal/group/shared), returns calendars that the user has access permission.
   *
   * @param type Type of calendar, can be *personal*, *group* or *shared*. If omitted or unknown, it searches for *all* types.
   * 
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of calendars returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   *        
   * @param returnSize Default is *false*. If set to *true*, the total number of matched calendars will be returned in JSON,
   *        and a "Link" header is added. This header contains "first", "last", "next" and "previous" links.
   *        
   * @param fields Comma-separated list of selective calendar attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   *        
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/calendars?type=personal&fields=id,name
   *
   * @format  JSON
   *
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "editPermission": "",
   *       "viewPermission": "",
   *       "privateURL": null,
   *       "publicURL": null,
   *       "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *       "description": null,
   *       "color": "asparagus",
   *       "timeZone": "Europe/Brussels",
   *       "name": "John Smith",
   *       "type": "0",
   *       "owner": "john",
   *       "groups": null,
   *       "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *       "id": "john-defaultCalendarId"
   *     },
   *     {
   *       "editPermission": "/platform/users/:*.*;",
   *       "viewPermission": "*.*;",
   *       "privateURL": null,
   *       "publicURL": null,
   *       "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/calendar8b8f65e77f00010122b425ec81b80da9/ics",
   *       "description": null,
   *       "color": "asparagus",
   *       "timeZone": "Europe/Brussels",
   *       "name": "Users",
   *       "type": "0",
   *       "owner": null,
   *       "groups": [
   *         "/platform/users"
   *       ],
   *       "href": "http://localhost:8080/rest/private/v1/calendar/calendars/calendar8b8f65e77f00010122b425ec81b80da9",
   *       "id": "calendar8b8f65e77f00010122b425ec81b80da9"
   *     }
   *   ],
   *   "size": -1,
   *   "offset": 0
   * }
   *  
   * @return  List of calendars in JSON.
   *
   * @authentication
   *
   * @anchor  CalendarRestApi.getCalendars
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @RolesAllowed("users")
  @Path("/calendars/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCalendars(@QueryParam("type") String type,
                                                @QueryParam("offset") int offset, 
                                                @QueryParam("limit") int limit,
                                                @QueryParam("returnSize") boolean returnSize,
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
      if(cals == null || cals.isEmpty()) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();      
      
      String basePath = getBasePath(uri);
      Collection data = new LinkedList();
      Iterator<Calendar> calIterator = cals.iterator();
      while (calIterator.hasNext()) {
         Calendar cal = calIterator.next();         
         data.add(extractObject(new CalendarResource(cal, basePath), fields));
      }
      
      CollectionResource calData = new CollectionResource(data, returnSize ? cals.getFullSize() : -1);
      calData.setOffset(offset);
      calData.setLimit(limit);
      
      ResponseBuilder okResult;
      if (jsonp != null) {
        JsonValue value = new JsonGeneratorImpl().createJsonObject(calData);
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append("(").append(value).append(");");
        okResult = Response.ok(sb.toString(), new MediaType("text", "javascript"));
      } else {
        okResult = Response.ok(calData, MediaType.APPLICATION_JSON);
      }
      
      if (returnSize) {
        okResult.header(HEADER_LINK, buildFullUrl(uri, offset, limit, calData.getSize()));
      }
      
      //
      return okResult.cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();

  }

  /**
   * Creates a calendar based on calendar attributes sent in the request body. Can be personal or group calendar.
   * 
   * This accepts HTTP POST request, with JSON object (cal) represents a CalendarResource. Example:
   *    {
   *      name: "Calendar name",
   *      description: "Description of the calendar"
   *   }
   * 
   * @param cal JSON object contains attributes of calendar object to create.
   *        All attributes are optional. If specified explicitly, calendar name must not empty, 
   *        contains only letter, digit, space, "-", "_" characters. Default value of calendar name is: calendar.
   *
   * @request  POST: http://localhost:8080/rest/private/v1/calendar/calendars
   *
   * @response  HTTP status code: 
   *            201 if created successfully, and HTTP header *location* href that points to the newly created calendar.
   *            401 if the user does not have create permission, 503 if any error during save process.
   *
   * @return  HTTP status code.
   *
   * @authentication
   *
   * @anchor  CalendarRestApi.createCalendar
   */
  @POST
  @RolesAllowed("users")
  @Path("/calendars/")
  public Response createCalendar(CalendarResource cal, @Context UriInfo uriInfo) {
    Calendar calendar = new Calendar();
    if (cal.getName() == null) {
      cal.setName(DEFAULT_CAL_NAME);
    }
    Response error = buildCalendar(calendar, cal);
    if (error != null) {
      return error;
    }
    
		if (cal.getGroups() != null && cal.getGroups().length > 0) {
			// Create a group calendar
			if (isInGroups(cal.getGroups())) {
				calendarServiceInstance().savePublicCalendar(calendar, true);
			} else {
				return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
			}
		} else {
		  if (cal.getOwner() != null && !cal.getOwner().equals(currentUserId())) {  
		    return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
		  } else {
		    // Create a personal calendar
		    calendarServiceInstance().saveUserCalendar(currentUserId(), calendar, true);
		  }
		}
		
		return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, uriInfo.getAbsolutePath() + cal.getId()).cacheControl(nc).build();
	}

  /**
   * Search for a calendar by its id, in one of conditions:
   * The authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the calendar to be retrieved.
   * 
   * @param fields Comma-separated list of selective calendar attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned. 
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/calendars/{id}
   *
   * @format  JSON
   *
   * @response
   * {
   *   "editPermission": "/platform/users/:*.*;",
   *   "viewPermission": "*.*;",
   *   "privateURL": null,
   *   "publicURL": null,
   *   "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/calendar8b8f65e77f00010122b425ec81b80da9/ics",
   *   "description": null,
   *   "color": "asparagus",
   *   "timeZone": "Europe/Brussels",
   *   "name": "Users",
   *   "type": "2",
   *   "owner": null,
   *   "groups": [
   *     "/platform/users"
   *   ],
   *   "href": "http://localhost:8080/rest/private/v1/calendar/calendars/calendar8b8f65e77f00010122b425ec81b80da9",
   *   "id": "calendar8b8f65e77f00010122b425ec81b80da9"
   * }
   * @return  Calendar as JSON.
   *
   * @authentication
   * 
   * @anchor  CalendarRestApi.getCalendarById
   */
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCalendarById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                  @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      CalendarService service = calendarServiceInstance();
      Calendar cal = service.getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      cal.setCalType(service.getTypeOfCalendar(currentUserId(), cal.getId()));

      Date lastModified = new Date(cal.getLastModified());
      ResponseBuilder preCondition = request.evaluatePreconditions(lastModified);
      if (preCondition != null) {
        return preCondition.build();
      }
      
      CalendarResource calData = null;
      if (this.hasViewCalendarPermission(cal, currentUserId())) {
        calData = new CalendarResource(cal, getBasePath(uriInfo));
      }      
      if (calData == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      Object resource = extractObject(calData, fields);
      if (jsonp != null) {
        String json = null;
        if (resource instanceof Map) json = new JSONObject(resource).toString();
        else {
          JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
          json = generatorImpl.createJsonObject(resource).toString();
        }
        StringBuilder sb = new StringBuilder(jsonp);
        sb.append("(").append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).lastModified(lastModified).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).lastModified(lastModified).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Update a calendar specified by id, in one of conditions:
   * the authenticated user is the owner of the calendar,
   * OR for group calendars, the authenticated user has edit permission on the calendar.
   * 
   * This accepts HTTP PUT request, with JSON object (calObj) in the request body, and calendar id in the path.
   * All the attributes of JSON object are optional, any absent/invalid ones will be ignored.
   * *id*, *href* and URLs attributes are Read-only.
   *  
   * @param id Identity of the updated calendar.
   * 
   * @param calObj JSON object contains attributes of calendar object to update, all the attributes are optional.
   * 
   * @request  PUT: http://localhost:8080/rest/private/v1/calendar/calendars/demo-defaultCalendarId
   * 
   * @response  HTTP status code: 200 if updated successfully, 404 if calendar not found,
   *            401 if the user does not have edit permission, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.updateCalendarById
   */
  @PUT
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  public Response updateCalendarById(@PathParam("id") String id, CalendarResource calObj) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();      

      //Only allow to edit if user is owner of calendar, or have edit permission on group calendar
      //don't allow to edit shared calendar, or remote calendar
      if ((currentUserId().equals(cal.getCalendarOwner()) || cal.getGroups() != null) &&
          Utils.isCalendarEditable(currentUserId(), cal)) {
        Response error = buildCalendar(cal, calObj);
        if (error != null) {
          return error;
        } else {
          int type = calendarServiceInstance().getTypeOfCalendar(currentUserId(), cal.getId());
          calendarServiceInstance().saveCalendar(cal.getCalendarOwner(), cal, type, false);
          return Response.ok().cacheControl(nc).build();          
        }
      }
      
      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
    } catch (Exception e) {
      log.error(e);
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Deletes a calendar specified by id, in one of conditions:
   * - the authenticated user is the owner of the calendar.
   * - for group calendars, the authenticated user has edit permission on the calendar.
   * - if it is a shared calendar, the calendar is not shared anymore (but not deleted).
   * 
   * @param id Identity of the calendar to be deleted.
   * 
   * @request  DELETE: http://localhost:8080/rest/private/v1/calendar/calendars/demo-defaultCalendarId
   * 
   * @response  HTTP status code: 200 if updated successfully, 404 if calendar not found,
   *            401 if the user does not have permissions, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteCalendarById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/calendars/{id}")
  public Response deleteCalendarById(@PathParam("id") String id) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if(cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

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
        return Response.ok().cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  
  
  /**
   * Exports a calendar specified by id into iCal formated file, with one of conditions:
   * The calendar is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the exported calendar.
   * 
   * @request GET: http://localhost:8080/rest/private/v1/calendar/calendars/demo-defaultCalendarId/ics
   * 
   * @format text/calendar
   * 
   * @response ICS file on success, or HTTP status code 404 on failure.
   * 
   * @return  ICS file on success, or HTTP status code 404 on failure.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.exportCalendarToIcs
   */
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}/ics")
  @Produces(TEXT_ICS)
  public Response exportCalendarToIcs(@PathParam("id") String id, @Context Request request) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);      
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();      
      
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
        byte[] data = out.toString().getBytes();
        
        byte[] hashCode = digest(data).getBytes();        
        EntityTag tag = new EntityTag(new String(hashCode));
        ResponseBuilder preCondition = request.evaluatePreconditions(tag);
        if (preCondition != null) {
          return preCondition.build();
        }
        
        InputStream in = new ByteArrayInputStream(data);  
        return Response.ok(in, TEXT_ICS_TYPE)
            .header("Content-Disposition", "attachment;filename=\"" + cal.getName() + Utils.ICS_EXT)
            .cacheControl(cc).tag(tag).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();

  }  

  /**
   * Searches for an event by id, in one of conditions:
   * The calendar of the event is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event,
   * OR the calendar of the event has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the event.
   * 
   * @param fields Comma-separated list of selective event attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of property names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *        }
   *        It returns:
   *        {
   *            "calendar": {
   *              "editPermission": "",
   *              "viewPermission": "",
   *              "privateURL": null,
   *              "publicURL": null,
   *              "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *              "description": null,
   *              "color": "asparagus",
   *              "timeZone": "Europe/Brussels",
   *              "name": "John Smith",
   *              "type": "0",
   *              "owner": "john",
   *              "groups": null,
   *              "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *              "id": "john-defaultCalendarId"
   *            },
   *        }
   * 
   * @request GET: http://localhost:8080/rest/private/v1/calendar/events/Event123
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "to": "2015-07-24T01:30:00.000Z",
   *   "attachments": [],
   *   "from": "2015-07-24T01:00:00.000Z",
   *   "categories": [
   *     "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll"
   *   ],
   *   "categoryId": "defaultEventCategoryIdAll",
   *   "availability": "busy",
   *   "repeat": {},
   *   "reminder": [],
   *   "privacy": "private",
   *   "recurrenceId": null,
   *   "participants": [
   *     "john"
   *   ],
   *   "originalEvent": null,
   *   "description": null,
   *   "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *   "subject": "event123",
   *   "location": null,
   *   "priority": "none",
   *   "href": "http://localhost:8080/rest/private/v1/calendar/events/Eventa9c5b87b7f00010178ce661a6beb020d",
   *   "id": "Eventa9c5b87b7f00010178ce661a6beb020d"
   * }
   * 
   * @return  Event as JSON object.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getEventById
   */
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventById(@PathParam("id") String id,
                                                 @QueryParam("fields") String fields,
                                                 @QueryParam("expand") String expand,
                                                 @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      CalendarService service = calendarServiceInstance();
      CalendarEvent ev = service.getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      Date lastModified = new Date(ev.getLastModified());
      ResponseBuilder preCondition = request.evaluatePreconditions(lastModified);
      if (preCondition != null) {
        return preCondition.build();
      }
      
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      boolean inParticipant = false;
      String[] participant = ev.getParticipant();
      if (participant != null) {
        Arrays.sort(participant);
        if (Arrays.binarySearch(participant, currentUserId()) > -1) inParticipant = true;
      }
     
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {
        Object resource = buildEventResource(ev, uriInfo, expand, fields);        
        return buildJsonP(resource, jsonp).cacheControl(cc).lastModified(lastModified).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Updates an event specified by id, in one of conditions:
   * The authenticated user is the owner of the calendar of the event,
   * OR for group calendars, the user has edit permission on the calendar,
   * OR the calendar has been shared with the user, with edit permission,
   * OR the calendar has been shared with a group of the user, with edit permission.
   * 
   * This accepts HTTP PUT request, with JSON object (evObject) in the request body, and event id in the path.
   * All the attributes of JSON object are optional, any absent/invalid ones will be ignored. 
   * Read-only attributes: *id* and *href*, *originalEvent*, *calendar*, *recurrentId* will be ignored too.
   *  
   * @param id Identity of the updated event.
   * 
   * @param evObject JSON object contains event attributes, all are optional.
   *        If provided explicitly (not null), attributes are checked with some rules:
   *        1. *subject* must not be empty.
   *        2. *availability* can only be one of "available", "busy", "outside".
   *        3. *repeat.repeatOn* can only be one of "MO", "TU", "WE", "TH", "FR", "SA", "SU".
   *        4. *repeat.repeatBy* must be >= 1 and <= 31.
   *        5. *repeat.repeatType* must be one of "norepeat", "daily", "weekly", "monthly", "yearly".
   *        6. *from* date must be earlier than *to* date.
   *        7. *priority* must be one of "none", "high", "normal", "low".
   *        8. *privacy* can only be "public" or "private".
   * 
   * @request PUT: http://localhost:8080/rest/private/v1/calendar/events/Event123
   * 
   * @response  HTTP status code: 200 if updated successfully, 400 if parameters are not valid, 404 if event does not exist,
   *            401 if the user does not have edit permission, 503 if any error during save process.
   * 
   * @return HTTP status code
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.updateEventById
   */
  @PUT
  @RolesAllowed("users")
  @Path("/events/{id}")
  public Response updateEventById(@PathParam("id") String id,  EventResource evObject) {
    try {
      CalendarEvent old = calendarServiceInstance().getEventById(id);
      if(old == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

      Calendar cal = calendarServiceInstance().getCalendarById(old.getCalendarId());
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        Response error = buildEvent(old, evObject);
        if (error != null) {
          return error;
        }
        
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
        return Response.ok().cacheControl(nc).build();
      }
      
      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Deletes an event specified by id, in one of conditions:
   * The authenticated user is the owner of the calendar of the event,
   * OR for group calendars, the user has edit permission on the calendar,
   * OR the calendar has been shared with the user, with edit permission,
   * OR the calendar has been shared with a group of the user, with edit permission.
   * 
   * @param id Identity of the event.
   * 
   * @request DELETE: http://localhost:8080/rest/private/v1/calendar/events/Event123
   * 
   * @response  HTTP status code: 200 if deleted successfully, 404 if event not found,
   *            401 if the user does not have edit permission, 503 if any error during save process.
   * 
   * @return HTTP status code
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteEventById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/events/{id}")
  public Response deleteEventById(@PathParam("id") String id) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
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
        return Response.ok().cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }
  

  /**
   * Returns attachments of an event specified by event id, in one of conditions:
   * The calendar of the event is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the event.
   *
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of attachments returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param fields Comma-separated list of selective attachment attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned. 
   * 
   * @request GET: http://localhost:8080/rest/private/v1/calendar/events/Event123/attachments
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "mimeType": "image/jpeg",
   *       "weight": 31249,
   *       "name": "test.jpg",
   *       "href": "...",
   *       "id": "..."
   *     }
   *   ],
   *   "size": 1,
   *   "offset": 0
   * }
   *  
   * @return  Attachments in JSON, or HTTP status 404 if event not found.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getAttachmentsFromEvent
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
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
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
            sb.append("(").append(value).append(");");
            return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(nc).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize())).build();
          }
          
          //
          return Response.ok(evData, MediaType.APPLICATION_JSON).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize())).cacheControl(nc).build();
        }
        
        //
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Creates attachments for an event specified by id, in one of conditions:
   * The authenticated user is the owner of the calendar of the event,
   * OR for group calendars, the user has edit permission on the calendar,
   * OR the calendar has been shared with the user, with edit permission,
   * OR the calendar has been shared with a group of the user, with edit permission.
   * 
   * This accepts HTTP POST request, with files in HTTP form submit request, and the event id in path.
   * 
   * @param id Identity of the event. 
   *
   * @param iter Iterator of org.apache.commons.fileupload.FileItem objects.
   *        (eXo Rest framework uses Apache file upload to parse the input stream of HTTP form submit request, and inject FileItem objects).
   *
   * @request POST: http://localhost:8080/rest/private/v1/calendar/events/Event123/attachments
   * 
   * @response  HTTP status code:
   *            201 if created successfully, and HTTP header *location* href that points to the newly created attachments.
   *            404 if event not found, 401 if the user does not have create permission, 503 if any error during save process.
   * 
   * @return HTTP status code
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.createAttachmentForEvent
   */
  @POST
  @RolesAllowed("users")
  @Path("/events/{id}/attachments")
  @Consumes("multipart/*")
  public Response createAttachmentForEvent(@Context UriInfo uriInfo, @PathParam("id") String id, Iterator<FileItem> iter) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

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
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, attUri.toString()).cacheControl(nc).build();
      }

      //
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Returns events of a calendar specified by id, in one of conditions:
   * The calendar is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of a calendar to search for events.
   * 
   * @param startTime Date that complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *from* this date.
   *        Default: current server time.
   * 
   * @param endTime Date that complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *to* this date.
   *        Default: current server time + 1 week.
   * 
   * @param category Search for this category only. If not specified, search events of all categories.
   *
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of events returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param returnSize Default is *false*. If set to *true*, the total number of matched calendars will be returned in JSON,
   *        and a "Link" header is added. This header contains "first", "last", "next" and "previous" links.
   * 
   * @param fields Comma-separated list of selective event properties to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of event attribute names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *        }
   *        It returns:
   *        {
   *            "calendar": {
   *            "editPermission": "",
   *            "viewPermission": "",
   *            "privateURL": null,
   *            "publicURL": null,
   *            "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *            "description": null,
   *            "color": "asparagus",
   *            "timeZone": "Europe/Brussels",
   *            "name": "John Smith",
   *            "type": "0",
   *            "owner": "john",
   *            "groups": null,
   *            "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *            "id": "john-defaultCalendarId"
   *            },
   *        }
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/calendars/myCalId/events?category=meeting&expand=calendar,categories(1,5)
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "to": "2015-07-24T01:30:00.000Z",
   *       "attachments": [],
   *       "from": "2015-07-24T01:00:00.000Z",
   *       "categories": [
   *         "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll"
   *       ],
   *       "categoryId": "defaultEventCategoryIdAll",
   *       "availability": "busy",
   *       "repeat": {},
   *       "reminder": [],
   *       "privacy": "private",
   *       "recurrenceId": null,
   *       "participants": [
   *         "john"
   *       ],
   *       "originalEvent": null,
   *       "description": null,
   *       "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *       "subject": "event123",
   *       "location": null,
   *       "priority": "none",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/events/Eventa9c5b87b7f00010178ce661a6beb020d",
   *       "id": "Eventa9c5b87b7f00010178ce661a6beb020d"
   *     }
   *   ],
   *   "size": -1,
   *   "offset": 0
   * }
   * 
   * @return  List of events in JSON, or HTTP status 404 if the calendar is not found.
   * 
   * @authentication
   *     
   * @anchor CalendarRestApi.getEventsByCalendar
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
      if (calendar.hasChildren()) {
        String participant = null;
        if (calendar.getPublicUrl() == null && !hasViewCalendarPermission(calendar, username)) {
          participant = username;
        }
        
        EventQuery eventQuery = buildEventQuery(start, end, category, Arrays.asList(calendar), 
                                                id, participant, CalendarEvent.TYPE_EVENT);
        ListAccess<CalendarEvent> events = evtDAO.findEventsByQuery(eventQuery);
        
        //
        for (CalendarEvent event : events.load(offset, limit)) {
          data.add(buildEventResource(event, uri, expand, fields));
        }
        if (returnSize) {
          fullSize = events.getSize();
        }        
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
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
   * Creates an event in a calendar specified by id, in one of conditions:
   * The authenticated user is the owner of the calendar,
   * OR for group calendars, the user has edit permission on the calendar,
   * OR the calendar has been shared with the user, with edit permission,
   * OR the calendar has been shared with a group of the user, with edit permission.
   * 
   * This accepts HTTP POST request, with JSON object (evObject) in the request body.
   * 
   * @param evObject JSON object contains attributes of event.
   *        All attributes are optional. If provided explicitly (not null), attributes are checked with some rules:
   *        1. *subject* must not be empty, default value is: default.
   *        2. *availability* can only be one of "available", "busy", "outside".
   *        3. *repeat.repeatOn* can only be one of "MO", "TU", "WE", "TH", "FR", "SA", "SU".
   *        4. *repeat.repeatBy* must be >= 1 and <= 31.
   *        5. *repeat.repeatType* must be one of "norepeat", "daily", "weekly", "monthly", "yearly".
   *        6. *from* date must be earlier than *to* date.
   *        7. *priority* must be one of "none", "high", "normal", "low".
   *        8. *privacy* can only be public or private.
   * 
   * @param id Identity of the calendar.
   * 
   * @request  POST: http://localhost:8080/rest/private/v1/calendar/calendars/myCalId/events
   *
   * @response  HTTP status code: 
   *            201 if created successfully, and HTTP header *location* href that points to the newly created event.
   *            400 if provided attributes are not valid (not pass the rule of evObject).
   *            404 if calendar not found.
   *            401 if the user does not have create permission.
   *            503 if any error during save process.
   * 
   * @return  HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.createEventForCalendar
   */
  @POST
  @RolesAllowed("users")
  @Path("/calendars/{id}/events")
  public Response createEventForCalendar(@PathParam("id") String id, EventResource evObject, @Context UriInfo uriInfo) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();      
      CalendarEvent evt = new CalendarEvent();
      if (evObject.getSubject() == null) {
        evObject.setSubject(DEFAULT_EVENT_NAME);        
      }
      if (evObject.getCategoryId() == null) {
        evObject.setCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
      }
      Response error = buildEvent(evt, evObject);
      if (error != null) {
        return error;
      }
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
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, location).cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();

  }  

  /**
   * Returns occurrences of a recurring event specified by id, in one of conditions:
   * the calendar of the event is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the event.
   * 
   * @param start Date complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for occurrences *from* this date.
   *        Default: current server time.
   * 
   * @param end Date complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for occurrences *to* this date.
   *        Default: current server time + 1 week.
   *
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of occurrences returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param returnSize Default is *false*. If set to *true*, the total number of matched calendars will be returned in JSON,
   *        and a "Link" header is added. This header contains "first", "last", "next" and "previous" links.
   * 
   * @param fields Comma-separated list of selective attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of property names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *        }
   *        It returns:
   *        {
   *            "calendar": {
   *              "editPermission": "",
   *              "viewPermission": "",
   *              "privateURL": null,
   *              "publicURL": null,
   *              "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *              "description": null,
   *              "color": "asparagus",
   *              "timeZone": "Europe/Brussels",
   *              "name": "John Smith",
   *              "type": "0",
   *              "owner": "john",
   *              "groups": null,
   *              "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *              "id": "john-defaultCalendarId"
   *            },
   *        }
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/events/Event123/occurences?offset=1&limit=5
   * 
   * @format  JSON
   * 
   * @response 
   * {
   *   "limit": 0,
   *   "data": [
   *     {
   *       "to": "2015-07-24T02:30:00.000Z",
   *       "attachments": [],
   *       "from": "2015-07-24T02:00:00.000Z",
   *       "categories": [
   *         "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll"
   *       ],
   *       "categoryId": "defaultEventCategoryIdAll",
   *       "availability": "busy",
   *       "repeat": {},
   *       "reminder": [],
   *       "privacy": "private",
   *       "recurrenceId": null,
   *       "participants": [],
   *       "originalEvent": null,
   *       "description": null,
   *       "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *       "subject": "rep123",
   *       "location": null,
   *       "priority": "none",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/events/Eventaa3c68ee7f00010171ac205a54bc1419",
   *       "id": "Eventaa3c68ee7f00010171ac205a54bc1419"
   *     },
   *     {}
   *   ],
   *   "size": -1,
   *   "offset": 10
   * }
   * 
   * @return        List of occurrences.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getOccurrencesFromEvent
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
                                                                     @QueryParam("returnSize") boolean returnSize,
                                                                     @Context UriInfo uriInfo) {
    try {
      limit = parseLimit(limit);
      java.util.Calendar[] dates = parseDate(start, end);
      
      CalendarEvent recurEvent = calendarServiceInstance().getEventById(id);
      if (recurEvent == null)  return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
      String timeZone = tz.getID();
      
      Map<String,CalendarEvent> occMap = calendarServiceInstance().getOccurrenceEvents(recurEvent, dates[0], dates[1], timeZone);
      if(occMap == null || occMap.isEmpty()) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
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
        
        int fullSize = returnSize ? occMap.values().size() : -1;
        CollectionResource evData = new CollectionResource(data, fullSize);
        evData.setOffset(offset);
        evData.setOffset(limit);
        
        //
        ResponseBuilder response = buildJsonP(evData, jsonp);
        if (returnSize) {
          response.header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, evData.getSize()));          
        }
        return response.build();
      }
      
      //
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  
  
  /**
   * Returns tasks of a calendar specified by id, in one of conditions:
   * The calendar is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the task is delegated to the user,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the calendar.
   * 
   * @param startTime Date complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for tasks *from* this date.
   *        Default: current server time.
   * 
   * @param endTime Date complies ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for tasks *to* this date.
   *        Default: current server time + 1 week.
   * 
   * @param category Filter the tasks by this category if specified.
   *
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of tasks returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param returnSize Default is *false*. If set to *true*, the total number of matched calendars will be returned in JSON,
   *        and a "Link" header is added. This header contains "first", "last", "next" and "previous" links.
   * 
   * @param fields Comma-separated list of selective task attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of property names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *        }
   *        It returns:
   *        {
   *            "calendar": {
   *              "editPermission": "",
   *              "viewPermission": "",
   *              "privateURL": null,
   *              "publicURL": null,
   *              "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *              "description": null,
   *              "color": "asparagus",
   *              "timeZone": "Europe/Brussels",
   *              "name": "John Smith",
   *              "type": "0",
   *              "owner": "john",
   *              "groups": null,
   *              "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *              "id": "john-defaultCalendarId"
   *            },
   *        }
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/myCalId/tasks?category=meeting&expand=calendar,categories(1,5)
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "to": "2015-07-18T12:00:00.000Z",
   *       "attachments": [],
   *       "from": "2015-07-18T11:30:00.000Z",
   *       "categories": [
   *         "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll"
   *       ],
   *       "categoryId": "defaultEventCategoryIdAll",
   *       "reminder": [],
   *       "delegation": [
   *         "john"
   *       ],
   *       "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *       "name": "caramazov",
   *       "priority": "none",
   *       "note": null,
   *       "status": "",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/tasks/Event99f63db07f0001016dd7a4b4e0e7125c",
   *       "id": "Event99f63db07f0001016dd7a4b4e0e7125c"
   *     }
   *   ],
   *   "size": -1,
   *   "offset": 0
   * }
   * 
   * @return  List of tasks in JSON, or HTTP status 404 if calendar not found.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getTasksByCalendar
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/calendars/{id}/tasks")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasksByCalendar(@PathParam("id") String id,
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

      EventQuery eventQuery = buildEventQuery(start, end, category, Arrays.asList(calendar), 
                                              id, participant, CalendarEvent.TYPE_TASK);
      ListAccess<CalendarEvent> events = evtDAO.findEventsByQuery(eventQuery);

      //
      for (CalendarEvent event : events.load(offset, limit)) {
        data.add(buildTaskResource(event, uri, expand, fields));
      }
      if (returnSize) {
        fullSize = events.getSize();
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
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
   * Creates a task for a calendar specified by id, in one of conditions:
   * The user is the owner of the calendar,
   * OR for group calendars, the user has edit permission on the calendar,
   * OR the calendar has been shared with the user, with edit permission,
   * OR the calendar has been shared with a group of the user, with edit permission.
   * 
   * This accepts HTTP POST request, with JSON object (evObject) in the request body. Example:
   *   {
   *      "name": "...", "note": "...",
   *      "categoryId": "",
   *      "from": "...", "to": "...",
   *      "delegation": ["...", ""], "priority": "", 
   *      "reminder": [],
   *      "status": ""
   *   }
   * 
   * @param evObject JSON object contains attributes of task.
   *        All attributes are optional. If provided explicitly (not null), attributes are checked with some rules:
   *        1. *name* must not be empty, default value is: "default".
   *        2. *from* date must be earlier than *to* date.
   *        3. *priority* must be one of "none", "high", "normal", "low".
   *        4. *status* must be one of "needs-action", "completed", "in-progress", "canceled".
   * 
   * @param id Identity of the calendar.
   * 
   * @request  POST: http://localhost:8080/rest/private/v1/calendar/calendars/myCalId/tasks
   * 
   * @response  HTTP status code: 
   *            201 if created successfully, and HTTP header *location* href that points to the newly created task.
   *            400 if attributes are invalid (not pass the rule of evObject).
   *            404 if calendar not found.
   *            401 if the user does not have create permission.
   *            503 if any error during save process.
   * 
   * @return  HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.createTaskForCalendar
   */
  @POST
  @RolesAllowed("users")
  @Path("/calendars/{id}/tasks")
  public Response createTaskForCalendar(@PathParam("id") String id, TaskResource evObject, @Context UriInfo uriInfo) {
    try {
      Calendar cal = calendarServiceInstance().getCalendarById(id);
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();      
      
      CalendarEvent evt = new CalendarEvent();
      evt.setEventType(CalendarEvent.TYPE_TASK);
      if (evObject.getName() == null) {
        evObject.setName(DEFAULT_EVENT_NAME);        
      }
      if (evObject.getCategoryId() == null) {
        evObject.setCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
      }
      Response error = buildEventFromTask(evt, evObject);
      if (error != null) {
        return error;
      }
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
        
        String location = new StringBuilder(getBasePath(uriInfo)).append(TASK_URI).append(evt.getId()).toString();
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, location).cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();

  }

  /**
   *
   * Returns a task specified by id, in one of conditions:
   * the calendar of the task is public;
   * OR the authenticated user is the owner of the calendar;
   * OR the user belongs to the group of the calendar;
   * OR the task is delegated to the user;
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the task.
   * 
   * @param fields Comma-separated list of selective task properties to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of task attributes names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *        }
   *        It returns:
   *        {
   *            "calendar": {
   *              "editPermission": "",
   *              "viewPermission": "",
   *              "privateURL": null,
   *              "publicURL": null,
   *              "icsURL": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId/ics",
   *              "description": null,
   *              "color": "asparagus",
   *              "timeZone": "Europe/Brussels",
   *              "name": "John Smith",
   *              "type": "0",
   *              "owner": "john",
   *              "groups": null,
   *              "href": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *              "id": "john-defaultCalendarId"
   *            },
   *        }
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/tasks/Task123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "to": "2015-07-18T12:00:00.000Z",
   *   "attachments": [],
   *   "from": "2015-07-18T11:30:00.000Z",
   *   "categories": [
   *     "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll"
   *   ],
   *   "categoryId": "defaultEventCategoryIdAll",
   *   "reminder": [],
   *   "delegation": [
   *     "john"
   *   ],
   *   "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId",
   *   "name": "caramazov",
   *   "priority": "none",
   *   "note": null,
   *   "status": "",
   *   "href": "http://localhost:8080/rest/private/v1/calendar/tasks/Event99f63db07f0001016dd7a4b4e0e7125c",
   *   "id": "Event99f63db07f0001016dd7a4b4e0e7125c"
   * } 
   *  
   * @return  Task in JSON format, or HTTP status 404 if task not found, or 503 if any other failure.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getTaskById
   */
  @GET
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(@PathParam("id") String id,
                                                @QueryParam("fields") String fields,
                                                @QueryParam("expand") String expand,
                                                @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if(ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      Date lastModified = new Date(ev.getLastModified());
      ResponseBuilder preCondition = request.evaluatePreconditions(lastModified);
      if (preCondition != null) {
        return preCondition.build();
      }
      
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      boolean inParticipant = false;
      if (ev.getParticipant() != null) {
        String[] participant = ev.getParticipant();
        Arrays.sort(participant);
        if (Arrays.binarySearch(participant, currentUserId()) > -1) inParticipant = true;;
      }
    
      if (cal.getPublicUrl() != null || this.hasViewCalendarPermission(cal, currentUserId()) || inParticipant) {        
        Object resource = buildTaskResource(ev, uriInfo, expand, fields);        
        return buildJsonP(resource, jsonp).cacheControl(cc).lastModified(lastModified).build();
      } else {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();        
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   *
   * Updates a task specified by id, in one of conditions:
   * the calendar of the task is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the task is delegated to the user,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * This accepts HTTP PUT request, with JSON object (evObject) in the request body, and task id in the path.
   * All the attributes are optional, any absent/invalid attributes will be ignored.
   * *id*, *href*, *calendar* are Read-only.
   * For example:
   *   {
   *      "name": "...", "note": "...",
   *      "categoryId": "",
   *      "from": "...", "to": "...",
   *      "delegation": ["...", ""], "priority": "", 
   *      "reminder": [],
   *      "status": ""
   *   }
   *  
   * @param id Identity of the task.
   * 
   * @param evObject JSON object contains attributes of the task to be updated, all attributes are optional.
   *        If provided explicitly (not null), attributes are checked with some rules:
   *        1. *name* must not be empty.
   *        2. *from* date must be earlier than *to* date.
   *        3. *priority* must be one of "none", "high", "normal", "low".
   *        4. *status* must be one of "needs-action", "completed", "in-progress", "canceled".
   * 
   * @request  PUT: http://localhost:8080/rest/private/v1/calendar/tasks/Task123
   * 
   * @response  HTTP status code:
   *            200 if updated successfully,
   *            404 if task not found,
   *            400 if attributes are invalid, 
   *            401 if the user does not have edit permission,
   *            503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.updateTaskById
   */
  @PUT
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  public Response updateTaskById(@PathParam("id") String id, TaskResource evObject) {
    try {
      CalendarEvent old = calendarServiceInstance().getEventById(id);
      if (old == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(old.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        int calType = -1;
        try {
          calType = Integer.parseInt(old.getCalType());
        }catch (NumberFormatException e) {
          calType = calendarServiceInstance().getTypeOfCalendar(currentUserId(), old.getCalendarId());
        } 
        buildEventFromTask(old, evObject);
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
        return Response.ok().cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Deletes a task specified by id, in one of conditions:
   * the calendar of the task is public;
   * OR the authenticated user is the owner of the calendar;
   * OR the user belongs to the group of the calendar;
   * OR the task is delegated to the user;
   * OR the calendar has been shared with the user or with a group of the user.
   *  
   * @param id Identity of the task.
   * 
   * @request  DELETE: http://localhost:8080/rest/private/v1/calendar/tasks/Task123
   * 
   * @response  HTTP status code:
   *            200 if deleted successfully, 404 if task not found,
   *            401 if the user does not have permission, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteTaskById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/tasks/{id}")
  public Response deleteTaskById(@PathParam("id") String id) {
    try {
      CalendarEvent ev = calendarServiceInstance().getEventById(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
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
        return Response.ok().cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Queries an attachment (of an event/task) by attachment id, in one of conditions:
   * The calendar of the event/task is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event or is delegated to the task,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the attachment.
   * 
   * @param fields Comma-separated list of selective attachment attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/attachments/att123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "weight": 38569,
   *   "mimeType": "image/png",
   *   "name": "test.png",
   *   "href": "...",
   *   "id": "..."
   * }
   *  
   * @return  Attachment info in JSON.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getAttachmentById
   */
  @GET
  @RolesAllowed("users")
  @Path("/attachments/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttachmentById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                    @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      id = AttachmentResource.decode(id);
      CalendarEvent ev = this.findEventAttachment(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      Attachment att = calendarServiceInstance().getAttachmentById(id);
      if(att == null)  return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

      Date lastModified = new Date(att.getLastModified());
      ResponseBuilder preCondition = request.evaluatePreconditions(lastModified);
      if (preCondition != null) {
        return preCondition.build();
      }
      
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
          sb.append("(").append(json).append(");");
          return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).lastModified(lastModified).build();
        }

        //
        return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).lastModified(lastModified).build();
      }

      //
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Deletes an attachment (of an event/task) specified by attachment id, in one of conditions:
   * The calendar of the event/task is public,
   * OR the authenticated user is the owner of the calendar,
   * OR the user belongs to the group of the calendar,
   * OR the user is a participant of the event or is delegated to the task,
   * OR the calendar has been shared with the user or with a group of the user.
   * 
   * @param id Identity of the attachment.
   * 
   * @request  DELETE: http://localhost:8080/rest/private/v1/calendar/attachments/att123
   * 
   * @response  HTTP status code:
   *            200 if deleted successfully, 404 if attachment not found,
   *            401 if the user does not have permission, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteAttachmentById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/attachments/{id}")
  public Response deleteAttachmentById(@PathParam("id") String id) {
    try {
      id = AttachmentResource.decode(id);
      CalendarEvent ev = this.findEventAttachment(id);
      if (ev == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      Calendar cal = calendarServiceInstance().getCalendarById(ev.getCalendarId());
      if (cal == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      if (Utils.isCalendarEditable(currentUserId(), cal)) {
        calendarServiceInstance().removeAttachmentById(id);
        return Response.ok().cacheControl(nc).build();
      } 
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Returns the categories (common and personal categories).
   *
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of categories returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param fields Comma-separated list of selective category attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/categories?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "name": "defaultEventCategoryNameAll",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll",
   *       "id": "defaultEventCategoryIdAll"
   *     },
   *     {...}
   *   ],
   *   "size": 6,
   *   "offset": 0
   * }
   * 
   * @return  List of categories.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getEventCategories
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
      if(ecData == null || ecData.isEmpty()) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
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
        sb.append("(").append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, resource.getSize())).cacheControl(nc).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, resource.getSize())).cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  
  
  /**
   * Returns a category specified by id if it is a common category or is a personal category of the user.
   * 
   * @param id Identity of the category.
   * 
   * @param fields Comma-separated list of selective category attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/categories/cat123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "name": "defaultEventCategoryNameAll",
   *   "href": "http://localhost:8080/rest/private/v1/calendar/categories/defaultEventCategoryIdAll",
   *   "id": "defaultEventCategoryIdAll"
   * }
   *  
   * @return  A category in JSON.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getEventCategoryById
   */
  @GET
  @RolesAllowed("users")
  @Path("/categories/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEventCategoryById(@PathParam("id") String id, @QueryParam("fields") String fields, 
                                       @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      List<EventCategory> data = calendarServiceInstance().getEventCategories(currentUserId());
      if(data == null || data.isEmpty()) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      }
      EventCategory category = null;
      for (int i = 0; i < data.size(); i++) {
        if(id.equals(data.get(i).getId())) {
          category = data.get(i);
          break;
        }
      }
      
      if(category == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
      Date lastModified = new Date(category.getLastModified());
      ResponseBuilder preCondition = request.evaluatePreconditions(lastModified);
      if (preCondition != null) {
        return preCondition.build();
      }
      
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
        sb.append("(").append(json).append(");");
        return Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(cc).lastModified(lastModified).build();
      }
      
      //
      return Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(cc).lastModified(lastModified).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   *
   * Gets a feed with the given id. The user must be the owner of the feed.
   *  
   * @param id The title of the feed.
   * 
   * @param fields Comma-separated list of selective feed attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Used to ask for more attributes of a sub-resource, instead of its link only. 
   *        This is a comma-separated list of attribute names. For example: expand=calendar,categories. In case of collections, 
   *        you can specify offset (default: 0), limit (default: *query_limit*). For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            "id": "...", 
   *            "calendar": "http://localhost:8080/rest/private/v1/calendar/calendars/demo-defaultCalendarId"
   *            ...
   *        }
   *        It returns:
   *        {
   *            "id": "...", 
   *            "calendar": 
   *            {
   *              "id": "...",
   *              "name":"demo-defaultId",
   *              ...
   *            }
   *            ...
   *        }
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/feeds/feed123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * {
   *   "calendars": [
   *     "http://localhost:8080/rest/private/v1/calendar/calendars/john-defaultCalendarId"
   *   ],
   *   "calendarIds": [
   *     "john-defaultCalendarId"
   *   ],
   *   "rss": "/v1/calendar/feeds/Calendar_Feed/rss",
   *   "name": "Calendar_Feed",
   *   "href": "http://localhost:8080/rest/private/v1/calendar/feeds/Calendar_Feed",
   *   "id": "Calendar_Feed"
   * }
   *  
   * @return  Feed in JSON.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getFeedById
   */
  @GET
  @RolesAllowed("users")
  @Path("/feeds/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedById(@PathParam("id") String id,
                                                @QueryParam("fields") String fields,
                                                @QueryParam("expand") String expand,
                                                @QueryParam("jsonp") String jsonp, @Context UriInfo uriInfo, @Context Request request) {
    try {
      FeedData feed = null;
      for (FeedData feedData : calendarServiceInstance().getFeeds(currentUserId())) {
        if (feedData.getTitle().equals(id)) {
          feed = feedData;
          break;
        }        
      }      
      if(feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      byte[] data = feed.getContent();
      
      byte[] hashCode = digest(data).getBytes();        
      EntityTag tag = new EntityTag(new String(hashCode));
      ResponseBuilder preCondition = request.evaluatePreconditions(tag);
      if (preCondition != null) {
        return preCondition.build();
      }
      
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed syndFeed = input.build(new XmlReader(new ByteArrayInputStream(data)));
      List<SyndEntry> entries = new ArrayList<SyndEntry>(syndFeed.getEntries());
      List<String> calIds = new ArrayList<String>();
      for (SyndEntry entry : entries) {
        String calendarId = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
        calIds.add(calendarId);
      }
      
      Object resource = buildFeedResource(feed, calIds, uriInfo, expand, fields);        
      return buildJsonP(resource, jsonp).cacheControl(cc).tag(tag).build();      
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Updates a feed with the given id. The user must be the owner of the feed. 
   * 
   * This accepts HTTP PUT request, with JSON object (feedResource) in the request body, and feed id in the path.
   * All the feed attributes are optional, any absent/invalid attributes will be ignored.
   * *id* and *href* are auto-generated and cannot be edited by the users.
   * For example:
   *   {
   *      "name": "..",
   *      "calendarIds": ["...", "..."]
   *   }
   *  
   * @param id The title of the feed.
   * 
   * @param feedResource JSON object contains attributes of the feed to be updated, all the attributes are optional.
   * 
   * @request  PUT: http://localhost:8080/rest/private/v1/calendar/feeds/feed123
   * 
   * @response  HTTP status code:
   *            200 if updated successfully, 404 if feed not found, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.updateFeedById
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

      if (feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

      LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
      if (feedResource.getCalendarIds() != null) {
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
      }
      
      //
      calendarServiceInstance().removeFeedData(currentUserId(),id);
      
      RssData rssData = new RssData();      
      if (feedResource.getName() != null) {
        rssData.setName(feedResource.getName() + Utils.RSS_EXT) ;
        rssData.setTitle(feedResource.getName()) ;
        rssData.setDescription(feedResource.getName());        
      }
      rssData.setUrl(feed.getUrl()) ;
      rssData.setLink(feed.getUrl());
      rssData.setVersion("rss_2.0") ;
      //
      calendarServiceInstance().generateRss(currentUserId(), calendars, rssData);
      
      return Response.ok().cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }  

  /**
   * Deletes a feed with the given id. The user must be the owner of the feed.
   * 
   * @param id The title of the feed.
   * 
   * @request  DELETE: http://localhost:8080/rest/private/v1/calendar/feeds/feed123
   * 
   * @response  HTTP status code:
   *            200 if delete successfully, 503 if any error during save process.
   * 
   * @return HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteFeedById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/feeds/{id}")
  public Response deleteFeedById(@PathParam("id") String id) {
    try {
      calendarServiceInstance().removeFeedData(currentUserId(),id);
      return Response.ok().cacheControl(nc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   *  Gets the RSS stream of a feed with the given id. The user must be the owner of the feed.
   *   
   * @param id The title of the feed.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/feeds/feed123/rss
   * 
   * @format application/xml
   * 
   * @response RSS
   *  
   * @return  Calendar RSS.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getRssFromFeed
   */
  @GET
  @RolesAllowed("users")
  @Path("/feeds/{id}/rss")
  @Produces(MediaType.APPLICATION_XML)
  public Response getRssFromFeed(@PathParam("id") String id, @Context UriInfo uri, @Context Request request) {
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

      if (feed == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      
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
        return Response.status(HTTPStatus.NOT_FOUND).entity("Feed " + feedname + "is removed").cacheControl(nc).build();
      } 
      String xml = makeFeed(username, events, feed, uri);
      
      byte[] hashCode = digest(xml.getBytes()).getBytes();
      EntityTag tag = new EntityTag(new String(hashCode));
      ResponseBuilder preCondition = request.evaluatePreconditions(tag);
      if (preCondition != null) {
        return preCondition.build();
      }
      
      return Response.ok(xml, MediaType.APPLICATION_XML).cacheControl(cc).tag(tag).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
    return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(nc).build();
  }

  /**
   * Returns an invitation with specified id if one of conditions:
   * The authenticated user is the participant of the invitation,
   * OR the user has edit permission on the calendar of the event of the invitation.
   * 
   * @param id Identity of the invitation.
   * 
   * @param fields Comma-separated list of selective invitation attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Use expand=event to get more event attributes instead of only link.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/invitations/evt123:root
   * 
   * @format  JSON
   * 
   * @response 
   * {
   *   "participant": "root",
   *   "event": "http://localhost:8080/rest/private/v1/calendar/events/Event9b014f9e7f00010166296f35cf2af06b",
   *   "status": "",
   *   "href": "http://localhost:8080/rest/private/v1/calendar/invitations/Event9b014f9e7f00010166296f35cf2af06b:root",
   *   "id": "Event9b014f9e7f00010166296f35cf2af06b:root"
   * }
   *  
   * @return  Invitation as JSON.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.getInvitationById
   */
  @GET
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInvitationById(@PathParam("id") String id, 
                                    @QueryParam("fields") String fields, 
                                    @QueryParam("jsonp") String jsonp,
                                    @QueryParam("expand") String expand,
                                    @Context UriInfo uriInfo, @Context Request request) throws Exception {
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    Invitation invitation = evtDAO.getInvitationById(id);
    if (invitation == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
        
    EntityTag tag = new EntityTag(String.valueOf(invitation.hashCode()));
    ResponseBuilder preCondition = request.evaluatePreconditions(tag);
    if (preCondition != null) {
      return preCondition.build();
    }
    
    //dont return invitation if user is not participant and not have edit permission
    if (!username.equals(invitation.getParticipant())) {
      CalendarEvent event = service.getEventById(invitation.getEventId());
      Calendar calendar = service.getCalendarById(event.getCalendarId());

      if (!Utils.isCalendarEditable(username, calendar)) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
      }
    }

    Object resource = buildInvitationResource(invitation, uriInfo, expand, fields);
    return buildJsonP(resource, jsonp).cacheControl(cc).tag(tag).build();
  }  

  /**
   * Replies to an invitation specified by id. The user must be the invitee.
   *  
   * @param id Identity of the invitation.
   * 
   * @param status New status to update ("", "maybe", "yes", "no").
   * 
   * @request  PUT: http://localhost:8080/rest/private/v1/calendar/invitations/evt123:root
   * 
   * @response  HTTP status code:
   *            200 if updated successfully, 404 if invitation not found, 400 if status is invalid,
   *            401 if the user does not have permission, 503 if any error during save process.
   * 
   * @return  HTTP status code.
   * 
   * @authentication
   *  
   * @anchor CalendarRestApi.updateInvitationById
   */
  @PUT
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  public Response updateInvitationById(@PathParam("id") String id, @QueryParam("status") String status) {
    if (Arrays.binarySearch(INVITATION_STATUS, status) < 0) {
      return buildBadResponse(new ErrorResource("status must be one of: " + StringUtils.join(INVITATION_STATUS, ","), "status"));
    }
    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    Invitation invitation = evtDAO.getInvitationById(id);
    if (invitation != null) {
      //Update only if user is participant
      if (invitation.getParticipant().equals(username)) {
        evtDAO.updateInvitation(id, status);
        return Response.ok().cacheControl(nc).build();
      } else {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
    }    
  }  

  /**
   * Deletes an invitation with specified id. The authenticated user must have edit permission on the calendar.
   *  
   * @param id Identity of the invitation.
   * 
   * @request  DELETE: http://localhost:8080/rest/private/v1/calendar/invitations/evt123:root
   * 
   * @response  HTTP status code:
   *            200 if deleted successfully, 404 if invitation not found,
   *            401 if the user does not have permission, 503 if any error during save process.
   * 
   * @return    HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteInvitationById
   */
  @DELETE
  @RolesAllowed("users")
  @Path("/invitations/{id}")
  public Response deleteInvitationById(@PathParam("id") String id) throws Exception {
    CalendarService calService = calendarServiceInstance();
    EventDAO evtDAO = calService.getEventDAO();
    String username = currentUserId();

    Invitation invitation = evtDAO.getInvitationById(id);
    if (invitation == null) return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();

    CalendarEvent event = calService.getEventById(invitation.getEventId());
    Calendar calendar = calService.getCalendarById(event.getCalendarId());

    if (Utils.isCalendarEditable(username, calendar)) {
      evtDAO.removeInvitation(id);
      return Response.ok().cacheControl(nc).build();
    } else {
      return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
    }
  }  

  /**
   * Returns invitations of an event specified by id when:
   * the authenticated user is the participant of the invitation,
   * OR the authenticated user has edit permission on the calendar of the event.
   * 
   * @param id Identity of the event.
   *
   * @param status Filter the invitations by this status if specified.
   * 
   * @param offset The starting point when paging the result. Default is *0*.
   * 
   * @param limit Maximum number of invitations returned.
   *        If omitted or exceeds the *query limit* parameter configured for the class, *query limit* is used instead.
   * 
   * @param returnSize Default is *false*. If set to *true*, the total number of matched invitations will be returned in JSON,
   *        and a "Link" header is added. This header contains "first", "last", "next" and "previous" links.
   * 
   * @param fields Comma-separated list of selective invitation attributes to be returned. All returned if not specified.
   * 
   * @param jsonp The name of a JavaScript function to be used as the JSONP callback.
   *        If not specified, only JSON object is returned.
   * 
   * @param expand Use expand=event to get more event attributes instead of only link.
   * 
   * @request  GET: http://localhost:8080/rest/private/v1/calendar/events/evt123/invitations
   * 
   * @format  JSON
   * 
   * @response 
   * {
   *   "limit": 10,
   *   "data": [
   *     {
   *       "participant": "john",
   *       "event": "http://localhost:8080/rest/private/v1/calendar/events/Event9b014f9e7f00010166296f35cf2af06b",
   *       "status": "",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/invitations/Event9b014f9e7f00010166296f35cf2af06b:john",
   *       "id": "Event9b014f9e7f00010166296f35cf2af06b:john"
   *     },
   *     {
   *       "participant": "root",
   *       "event": "http://localhost:8080/rest/private/v1/calendar/events/Event9b014f9e7f00010166296f35cf2af06b",
   *       "status": "",
   *       "href": "http://localhost:8080/rest/private/v1/calendar/invitations/Event9b014f9e7f00010166296f35cf2af06b:root",
   *       "id": "Event9b014f9e7f00010166296f35cf2af06b:root"
   *     }
   *   ],
   *   "size": -1,
   *   "offset": 0
   * }
   * 
   * @return  Invitations as JSON.
   * 
   * @authentication
   *  
   * @anchor  CalendarRestApi.getInvitationsFromEvent
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GET
  @RolesAllowed("users")
  @Path("/events/{id}/invitations/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInvitationsFromEvent(@PathParam("id") String id,
                                                                  @QueryParam("offset") int offset, 
                                                                  @QueryParam("limit") int limit,
                                                                  @QueryParam("returnSize") boolean returnSize,
                                                                  @QueryParam("status") String status, 
                                                                  @QueryParam("fields") String fields,
                                                                  @QueryParam("jsonp") String jsonp,
                                                                  @QueryParam("expand") String expand,
                                                                  @Context UriInfo uriInfo) throws Exception {
    limit = parseLimit(limit);
    CalendarService calService = calendarServiceInstance();

    CalendarEvent event = calService.getEventById(id);
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
    
    CollectionResource ivData = new CollectionResource(data, returnSize ? fullSize : -1);    
    ivData.setOffset(offset);
    ivData.setLimit(limit);

    ResponseBuilder response = buildJsonP(ivData, jsonp);
    if (returnSize) {
      response.header(HEADER_LINK, buildFullUrl(uriInfo, offset, limit, fullSize));
    }
    return response.build();
  }  

  /**
   *  Creates an invitation in an event specified by event id, in one of conditions:
   *  the authenticated user is the participant of the event,
   *  OR the authenticated user has edit permission on the calendar of the event.
   *  This accepts invitation resource in request body, for example : {"participant":"root","status":""}.
   *  
   * 
   * @param id Identity of the event.
   * 
   * @request  POST: http://localhost:8080/rest/private/v1/calendar/events/evt123/invitations
   * 
   * @response  HTTP status code:
   *            201 if created successfully, and HTTP header *location* href that points to the newly created invitation.
   *            400 if participant is invalid.
   *            404 if event not found.
   *            401 if the authenticated user does not have permission.
   *            503 if any error during save process.
   * 
   * @return  HTTP status code.
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.createInvitationForEvent
   */
  @POST
  @RolesAllowed("users")
  @Path("/events/{id}/invitations/")
  public Response createInvitationForEvent(@PathParam("id") String id, InvitationResource invitation, @Context UriInfo uriInfo) throws Exception {
    if(invitation == null) {
      return buildBadResponse(new ErrorResource("Invitation information must not null", "invitation"));
    }
    String participant = invitation.getParticipant();
    String status = invitation.getStatus();
    if (participant == null || participant.trim().isEmpty()) {
      return buildBadResponse(new ErrorResource("participant must not null or empty", "participant"));
    }
    if (Arrays.binarySearch(INVITATION_STATUS, status) < 0) {
      return buildBadResponse(new ErrorResource("status must be one of: " + StringUtils.join(INVITATION_STATUS, ","), "status"));
    }

    CalendarService service = calendarServiceInstance();
    EventDAO evtDAO = service.getEventDAO();
    String username = currentUserId();
    
    CalendarEvent event = service.getEventById(id);
    if (event != null) {
      Calendar calendar = service.getCalendarById(event.getCalendarId());
      if (!Utils.isCalendarEditable(username, calendar)) {
        return Response.status(HTTPStatus.UNAUTHORIZED).cacheControl(nc).build();
      }
      
      Invitation invite = evtDAO.createInvitation(id, participant, status);
      if (invite != null) {
        String location = new StringBuilder(getBasePath(uriInfo)).append(INVITATION_URI).append(invite.getId()).toString();
        return Response.status(HTTPStatus.CREATED).header(HEADER_LOCATION, location).cacheControl(nc).build();
      } else {
        return buildBadResponse(new ErrorResource(participant + " has already been participant, can't create more", "participant"));
      }
    } else {
      return Response.status(HTTPStatus.NOT_FOUND).cacheControl(nc).build();
    }
  }

  private Response buildBadResponse(ErrorResource error) {    
    return Response.status(HTTPStatus.BAD_REQUEST).entity(error).type(MediaType.APPLICATION_JSON)
        .cacheControl(nc).build();
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
      to.set(java.util.Calendar.HOUR, 0);
      to.set(java.util.Calendar.MINUTE, 0);
      to.set(java.util.Calendar.SECOND, 0);
      to.set(java.util.Calendar.MILLISECOND, 0);
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
  
  private List<Calendar> findViewableCalendars(String username) throws Exception {
    CalendarService service = calendarServiceInstance();
    //private calendar
    List<Calendar> uCals = service.getUserCalendars(username, true);
    //group calendar
    Set<String> groupIds = ConversationState.getCurrent().getIdentity().getGroups();
    List<GroupCalendarData> gCals = service.getGroupCalendars(groupIds.toArray(new String[groupIds.size()]), true, username);
    //shared calendar
    GroupCalendarData sCals = service.getSharedCalendars(username, true);
    if (sCals != null) {
        gCals.add(sCals);
    }
    //public calendar
    Calendar[] publicCals = service.getPublicCalendars().load(0, -1);
    
    List<Calendar> results = new LinkedList<Calendar>();    
    results.addAll(Arrays.asList(publicCals));
    for (GroupCalendarData data : gCals) {
        if (data.getCalendars() != null) {
            for (Calendar cal : data.getCalendars()) {
                results.add(cal);
            }
        }
    }
    results.addAll(uCals);

    return results;
  }
  
  private List<Calendar> findEditableCalendars(String username) throws Exception {
    List<Calendar> calendars = findViewableCalendars(username);
    Iterator<Calendar> iter = calendars.iterator();
    while (iter.hasNext()) {
      if (!Utils.isCalendarEditable(username, iter.next())) {
        iter.remove();
      }
    }
    return calendars;
  }
  
  private EventQuery buildEventQuery(String start, String end, String category, List<Calendar> calendars, String calendarPath,
                                               String participant, String eventType) {
    java.util.Calendar[] dates = parseDate(start, end);    
    
    //Find all invitations that user is participant
    EventQuery uQuery = new RestEventQuery();
    uQuery.setQueryType(Query.SQL);
    uQuery.setCalendarPath(calendarPath);
    List<String> calIds = new LinkedList<String>();
    if (calendars != null) {
      for (Calendar cal : calendars) {
        calIds.add(cal.getId());
      }
      uQuery.setCalendarId(calIds.toArray(new String[calIds.size()]));
    }
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
  
  private Response buildEvent(CalendarEvent old, EventResource evObject) {
    String catId = evObject.getCategoryId(); 
    setEventCategory(old, catId);
    if (evObject.getDescription() != null) {
      old.setDescription(evObject.getDescription());      
    }
    String eventState = evObject.getAvailability();
    if (eventState != null) {
      if (Arrays.binarySearch(EVENT_AVAILABILITY, eventState) < 0) {
        return buildBadResponse(new ErrorResource("availability must be one of " + StringUtils.join(EVENT_AVAILABILITY, ","), "availability"));
      } else {
        old.setEventState(eventState);
      }
    }
    if (evObject.getRepeat() != null) {
      RepeatResource repeat = evObject.getRepeat();
      if (repeat.getExclude() != null) {
        old.setExceptionIds(Arrays.asList(repeat.getExclude()));        
      }
      if (repeat.getRepeatOn() != null) {
        String[] reptOns = repeat.getRepeatOn().split(",");
        for (String on : reptOns) {
          if (Arrays.binarySearch(RP_WEEKLY_BYDAY, on) < 0) {
            return buildBadResponse(new ErrorResource("repeatOn can only contains " + StringUtils.join(RP_WEEKLY_BYDAY, ","), "repeatOn"));
          }
        }
        old.setRepeatByDay(reptOns);
      }
      if (repeat.getRepeateBy() != null) {
        String[] repeatBy = repeat.getRepeateBy().split(",");
        long[] by = new long[repeatBy.length];
        for (int i = 0; i < repeatBy.length; i++) {
          try {
            by[i] = Integer.parseInt(repeatBy[i]);
            if (by[i] < 1 || by[i] > 31) {
              return buildBadResponse(new ErrorResource("repeatBy must be >= 1 and <= 31", "repeatBy"));
            }
          } catch (Exception e) {
          }
        }
        old.setRepeatByMonthDay(by);        
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
        String reptType = end.getType();
        if (reptType != null) {
          if (Arrays.binarySearch(REPEATTYPES, reptType) < 0) {
            return buildBadResponse(new ErrorResource("repeat type must be one of " + StringUtils.join(REPEATTYPES, ","), "end.type"));
          } else {
            old.setRepeatType(end.getType());            
          }
        }
      }

      int every = repeat.getEvery();
      if (every < 1 || every > 30) {
        every = 1;
      }
      old.setRepeatInterval(repeat.getEvery());
    }
    
    java.util.Calendar[] fromTo = parseDate(evObject.getFrom(), evObject.getTo());
    if (fromTo[0].after(fromTo[1]) || fromTo[0].equals(fromTo[1])) {
      return buildBadResponse(new ErrorResource("\"from\" date must be before \"to\" date", "from"));
    }
    old.setFromDateTime(fromTo[0].getTime());
    if (evObject.getLocation() != null) {
      old.setLocation(evObject.getLocation());      
    }
    String priority = evObject.getPriority();
    if (priority != null) {
      if (Arrays.binarySearch(PRIORITY, priority) < 0) {
        return buildBadResponse(new ErrorResource("priority must be one of " + StringUtils.join(PRIORITY, ","), "priority"));
      } else {
        old.setPriority(evObject.getPriority());        
      }
    }
    if (evObject.getReminder() != null) {
      old.setReminders(Arrays.asList(evObject.getReminder()));      
    }
    String privacy = evObject.getPrivacy();
    if (privacy != null) {
      if (!CalendarEvent.IS_PRIVATE.equals(privacy) && !CalendarEvent.IS_PUBLIC.equals(privacy)) {
        return buildBadResponse(new ErrorResource("privacy can only be public or private", "privacy"));
      } else {
        old.setPrivate(CalendarEvent.IS_PRIVATE.equals(privacy));
      }
    }
    String subject = evObject.getSubject();
    if (subject != null) {
      subject = subject.trim();
      if (subject.isEmpty()) {
        return buildBadResponse(new ErrorResource("subject must not be empty", "subject"));
      } else {
        old.setSummary(subject);
      }
    }
    old.setToDateTime(fromTo[1].getTime());
    return null;
  }
  
  private Response buildEventFromTask(CalendarEvent old, TaskResource evObject) {
    String catId = evObject.getCategoryId(); 
    setEventCategory(old, catId);
    if (evObject.getNote() != null) {
      old.setDescription(evObject.getNote());        
    }
    java.util.Calendar[] fromTo = parseDate(evObject.getFrom(), evObject.getTo());    
    if (fromTo[0].after(fromTo[1]) || fromTo[0].equals(fromTo[1])) {
      return buildBadResponse(new ErrorResource("\"from\" date must be before \"to\" date", "from"));
    }
    old.setFromDateTime(fromTo[0].getTime());
    String priority = evObject.getPriority();
    if (priority != null) {
      if (Arrays.binarySearch(PRIORITY, priority) < 0) {
        return buildBadResponse(new ErrorResource("priority must be one of " + StringUtils.join(PRIORITY, ","), "priority"));
      } else {
        old.setPriority(evObject.getPriority());        
      }
    }
    if (evObject.getReminder() != null) {
      old.setReminders(Arrays.asList(evObject.getReminder()));      
    }
    String status = evObject.getStatus(); 
    if (status != null && !status.isEmpty()) {
      if (Arrays.binarySearch(TASK_STATUS, status) < 0) {
        return buildBadResponse(new ErrorResource("status must be one of " + StringUtils.join(TASK_STATUS, ","), "status"));
      } else {
        old.setStatus(status);
      }
    }
    String name = evObject.getName();
    if (name != null) {
      name = name.trim();
      if (name.isEmpty()) {
        return buildBadResponse(new ErrorResource("name must not be empty", "name"));
      } else {
        old.setSummary(evObject.getName());
      }
    }
    old.setToDateTime(fromTo[1].getTime());
    if (evObject.getDelegation() != null) {
      old.setTaskDelegator(StringUtils.join(evObject.getDelegation(), ","));
    }
    return null;
  }
  
  private void setEventCategory(CalendarEvent old, String catId) {
    if (catId != null) {
      try {
        EventCategory cat = calendarServiceInstance().getEventCategory(currentUserId(), catId);
        if (cat != null) {
          old.setEventCategoryId(cat.getId());
          old.setEventCategoryName(cat.getName());
        }
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
      }
    }
  }

  private Response buildCalendar(Calendar cal, CalendarResource calR) {    
    if (calR.getColor() != null) {
      cal.setCalendarColor(calR.getColor());
    }
    if (calR.getOwner() != null) {
      cal.setCalendarOwner(calR.getOwner());
    }
    if (calR.getDescription() != null) {
      cal.setDescription(calR.getDescription());      
    }
    if (calR.getEditPermission() != null) {
      cal.setEditPermission(calR.getEditPermission().split(Utils.SEMICOLON));      
    }
    if (calR.getGroups() != null) {
      cal.setGroups(calR.getGroups());      
    }
    String name = calR.getName();
    if (name != null) {
      name = name.trim();
      if (name.isEmpty() || containSpecialChar(name)) {
        return buildBadResponse(new ErrorResource("calendar name is empty or contains special characters", "name"));
      } else {
        cal.setName(calR.getName());
      }
    }
    if (calR.getPrivateURL() != null) {
      cal.setPrivateUrl(calR.getPrivateURL());      
    }
    if (calR.getPublicURL() != null) {
      cal.setPublicUrl(calR.getPublicURL());      
    }
    if (calR.getTimeZone() != null) {
      cal.setTimeZone(calR.getTimeZone());
    }
    if (calR.getViewPermission() != null) {
      cal.setViewPermission(calR.getViewPermission().split(Utils.SEMICOLON));
    }
    return null;
  }
  
  private boolean containSpecialChar(String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || Character.isSpaceChar(c)) {
        continue;
      }
      return true;
    }
    return false;
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
      sb.append("(").append(json).append(");");
      response = Response.ok(sb.toString(), new MediaType("text", "javascript")).cacheControl(nc);
    } else {
      response = Response.ok(resource, MediaType.APPLICATION_JSON).cacheControl(nc);
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
        cal.setCalType(calendarServiceInstance().getTypeOfCalendar(currentUserId(), cal.getId()));
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
        cal.setCalType(calendarServiceInstance().getTypeOfCalendar(currentUserId(), cal.getId()));
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
  
  private String digest(byte[] data) throws Exception {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] hashCode = md5.digest(data);
    //Can't compile if return byte[] due to the bug from eXo rest framework
    return String.valueOf(hashCode);
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
                offset = Integer.parseInt(exp.substring(exp.indexOf("offset:") + "offset:".length(), exp.indexOf(",")).trim());
                limit = Integer.parseInt(exp.substring(exp.indexOf("limit:") + "limit:".length(), exp.indexOf(")")).trim());
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
