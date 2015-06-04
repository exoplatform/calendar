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
 * This rest service class provides entry point for calendar resources
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
   * Contructor help to configure the rest service with parameters.
   *
   * Here is the configuration parameters:
   * - query_limit        maximum objects returned for a collection query, default value: 10.
   * - cache_maxage  time in miliseconds return in the cache-control header, default value:  604800
   *
   * @param  orgService
   *         exo organization service implementation
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
          log.warn("Can't parse {} to maxAge, use the defalt value {}", cacheConfig, maxAge);
        }
      }
    }
    cc.setPrivate(true);
    cc.setMaxAge(maxAge);
    cc.setSMaxAge(maxAge);
  }

  /**
   * Return all the available subresources as json, in order to navigate easily in the REST API.
   *
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar
   *
   * @format  json
   *
   * @response
   *    {
   *        "subResourcesHref": [
   *            "http://localhost:8080/rest/calendar/calendars",
   *            "http://localhost:8080/rest/calendar/events",
   *            "http://localhost:8080/rest/calendar/tasks"
   *         ]
   *     }
   *
   * @return  All hrefs of available entry-point of calendar service
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
   * Search for calendars which:
   * - the authenticated user is the owner of the calendar.
   * - the authenticated user belongs to the group of the calendar.
   * - the calendar has been shared with the authenticated user or with a group of the authenticated user.
   *
   * @param  type
   *         The calendar type to search for. It can be one of *personal, group, shared*.
   *         If this is omitted OR an unknown type is specified, it will search for *all* types.
   *
   * @param  offset
   *         The starting point when paging through a list of entities. Defaults to *0*.
   *
   * @param  limit
   *         The maximum number of results when paging through a list of entities, if not specify or exceed
   *         the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *         (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   *
   * @param  resturnSize
   *         Tell the service if it must return the total size of the returned collection result, 
   *         and the *link* http headers. 
   *         It can be true or false, by default, it's *false* 
   *
   * @param  fields
   *         This is a list of comma-separated property's names of response json object,
   *         if not specified, it return the json will all available properties.
   *
   * @param  jsonp
   *         The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *         json object is returned. 
   *
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/calendars?type=personal&fields=id,name
   *
   * @format  JSON
   *
   * @response 
   * [
   *   {
   *      id: "defaultId",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/calendars/defaultId",
   *      name: "calName",
   *      description: "...",
   *      type: "personal",
   *      timezone: "...",
   *      color: "...",
   *      owner: "...",
   *      viewPermission: "",
   *      editPermission: "",
   *      group: ["", ""],
   *      publicURL: "",
   *      privateURL: "",
   *      icsURL: ""
   *   }, 
   *   {id...}
   * ]
   * @return  List of calendars
   *
   * @authentication
   *
   * @anchor  CalendarRestApi.getCalendars
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @RolesAllowed("users")
  @Path("/calendars/")
  @Produces({MediaType.APPLICATION_JSON})
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
   * Creates a calendar if:
   * - this is a personal calendar and the user is authenticated.
   * - this is a group calendar and the user is authenticated and belongs to the group.
   * 
   * This entry point only allow http POST request, with json object (cal) in the request body. Example:
   *    {
   *      name: "Calendar name",
   *      description: "Description of the calendar",
   *      timezone: "...",
   *      color: "...",
   *      owner: "...",
   *      viewPermission: "...",
   *      editPermission: "...",
   *      group: ["", ""],
   *      publicURL: "", privateURL: ""
   *   }
   * 
   * @param  cal
   *         JSON object contains attributes of calendar object to create.
   *         All attributes are optional. If specified explicitly, calendar name must not empty, 
   *         contains only letter, digit, space, "-", "_" characters. Default value of calendar name is: calendar.
   *
   * @request  POST: http://localhost:8080/portal/rest/v1/calendar/calendars
   *
   * @response  HTTP status code: 
   *            201 if created successfully, and http header *location* href point to the newly created calendar.
   *            401 if user don't have create permission, 503 if there is any error during the save process
   *
   * @return  http status code
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
   * Returns the calendar with the specified id parameter if:
   * - The authenticated user is the owner of the calendar
   * - The authenticated user belongs to the group of the calendar
   * - The calendar has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param  id
   *         identity of the calendar to retrieve
   * 
   * @param  fields
   *         This is a list of comma-separated property's names of response json object,
   *         if not specified, it return the json will all available properties (id, href, name, description, type, timezone, 
   *         color, owner, viewPermission, editPermision, groups, publicURL, privateURL, icsURL)
   * 
   * @param  jsonp
   *         The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *         json object is returned 
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/calendars/{id}
   *
   * @format  JSON
   *
   * @response
   *  {
   *      id: "demo-defaultCalendarId",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId",
   *      name: "calName",
   *      description: "...",
   *      type: "personal",
   *      timezone: "...",
   *      color: "...",
   *      owner: "...",
   *      viewPermission: "",
   *      editPermission: "",
   *      group: ["", ""],
   *      publicURL: "",
   *      privateURL: "",
   *      icsURL: ""
   *   }
   * @return  calendar as JSON object
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
   * Update the calendar with specified id if:
   * - the authenticated user is the owner of the calendar
   * - for group calendars, the authenticated user has edit rights on the calendar
   * 
   * This entry point only allow http PUT request, with json object (calObj) in the request body, and calendar id in the path.
   * All the attributes of json object are optional, any omited attributes, or non-exists one will be ignored. *id* and *href* can't
   * be updated, they also be ignored.
   * For example:
   * {
   *      name: "calName",
   *      description: "...",
   *      type: "personal",
   *      timezone: "...",
   *      color: "...",
   *      owner: "...",
   *      viewPermission: "",
   *      editPermission: "",
   *      group: ["", ""],
   *      publicURL: "",
   *      privateURL: ""
   *   }
   *  
   * @param id
   *        identity of the calendar to update
   * 
   * @param calObj  
   *        json object contains attributes of calendar object to update, all the attributes are optional. 
   *        The calendar name must not empty, contains only letter, digit, space, "-", "_" characters
   * 
   * @request  PUT: http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId
   * 
   * @response  HTTP status code: 200 if updated successfully, 404 if calendar with provided id doesnt exists,
   *            401 if user don't have create permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Delete the calendar with the specified id if:
   * - the authenticated user is the owner of the calendar.
   * - for group calendars, the authenticated user has edit rights on the calendar.
   * - If it is a shared calendar the calendar is not shared anymore (but the original calendar is not deleted).
   * 
   * @param  id  
   *         identity of the calendar to delete
   * 
   * @request  DELETE: http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId
   * 
   * @response  HTTP status code: 200 if updated successfully, 404 if calendar with provided id doesnt exists,
   *            401 if user don't have create permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Returns an iCalendar formated file which is exported from the calendar with specified id if:
   * - the calendar is public
   * - the authenticated user is the owner of the calendar
   * - the authenticated user belongs to the group of the calendar
   * - the calendar has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id   
   *        identity of the calendar to retrieve ICS file
   * 
   * @request GET: http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId/ics
   * 
   * @format text/calendar
   * 
   * @response ICS file of calendar, or HTTP status code: 404 if not found
   * 
   * @return  ICS file or Http status code
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
   * Returns an event with specified id parameter if:
   * - the calendar of the event is public
   * - the authenticated user is the owner of the calendar of the event
   * - the authenticated user belongs to the group of the calendar of the event
   * - the authenticated user is a participant of the event
   * - the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id              
   *        identity of event to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar,categories. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit*) value into param. For example, expand=categories(1,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendar: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendar: {
   *            id: "...",
   *            name:"demo-"efaultId",
   *            ....
   *        }
   *            ....
   *        }
   * 
   * @request GET: http://localhost:8080/portal/rest/v1/calendar/events/Event123
   * 
   * @format JSON
   * 
   * @response 
   * {
   *      id: "defaultId",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/events/Event123",
   *      subject: "..",
   *      description: "...",
   *      from: "...",
   *      to: "...",
   *      calendar: "...",
   *      categories: ["...", ""],
   *      location: "",
   *      priority: "", 
   *      repeat: {...},
   *      recurrenceId: "...",
   *      originalEvent: "...",
   *      reminder: [],
   *      attachment: [],
   *      participants: [],
   *      privacy: "",
   *      availability: "" 
   * }
   * 
   * @return        event as json object
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
   * Updates the event with specified id if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   * 
   * This entry point only allow http PUT request, with json object (evObject) in the request body, and event id in the path.
   * All the attributes of json object are optional, any omited attributes, or non-exists one will be ignored. 
   * Or read-only attributes: *id* and *href*, *originalEvent*, *calendar*, *recurrentId* can't be updated, they also be ignored.
   * For example:
   * {
   *      subject: "..",
   *      description: "...",
   *      categoryId: "",
   *      from: "...", to: "...",
   *      location: "",
   *      priority: "", 
   *      repeat: {...},
   *      reminder: [],
   *      privacy: "",
   *      availability: ""
   * }
   *  
   * @param id             
   *        identity of the event to update
   * 
   * @param evObject  
   *        json object contains attributes of event object to update, all the attributes are optional.
   *        If provided explitly (not null), attributes are checked with some rules:
   *        1. subject must not be empty
   *        2. availability can only be one of "available", "busy", "outside"
   *        3. repeat.repeatOn can only be one of"MO", "TU", "WE", "TH", "FR", "SA", "SU"
   *        4. repeat.repeatBy must be >= 1 and <= 31
   *        5. repeat.repeatType must be one of "norepeat", "daily", "weekly", "monthly", "yearly"
   *        6. "from" date must be before "to" date
   *        7. priority must be one of "none", "high", "normal", "low"
   *        8. privacy can only be public or private
   * 
   * @request PUT: http://localhost:8080/portal/rest/v1/calendar/events/Event123
   * 
   * @response  HTTP status code: 200 if updated successfully, 400 if parameters are not valid 
   *            404 if event with provided id doesnt exists,
   *            401 if user don't have create permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Delete an event with specified id parameter if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   * 
   * @param id  
   *        identity of the event to delete
   * 
   * @request DELETE: http://localhost:8080/portal/rest/v1/calendar/events/Event123
   * 
   * @response  HTTP status code: 200 if delete successfully, 404 if event with provided id doesnt exists,
   *            401 if user don't have permission, 503 if there is any error during the save process
   * 
   * @return status code
   * 
   * @authentication
   * 
   * @anchor CalendarRestApi.deleteCalendarById
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
   * Returns attachments of an event with specified id if:
   * - the calendar of the event is public
   * - the authenticated user is the owner of the calendar of the event
   * - the authenticated user belongs to the group of the calendar of the event
   * - the authenticated user is a participant of the event
   * - the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id        
   *        identity of event that being query for attachments
   *
   * @param offset  
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit   
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param fields  
   *        This is a list of comma-separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp   
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned. 
   * 
   * @request GET: http://localhost:8080/portal/rest/v1/calendar/events/Event123/attachments
   * 
   * @format JSON
   * 
   * @response 
   *  [
   *    {
   *        id: "...",
   *        href: "...",
   *        name: "...",
   *        mimeType: "...",
   *        weight: "..."
   *    }, {...}
   *  ]
   *  
   * @return        List of attachments as json array
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
   * Creates attachments for an event with specified id if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   * 
   * This entry point only allow http POST request, with file input stream in the http form submit, and the id of event in the path
   * 
   * @param id      
   *        identity of event to create attachment 
   *
   * @param iter   
   *        Iterator of org.apache.commons.fileupload.FileItem object 
   *        (eXo rest framework use apache file upload to parse the input stream of http form submit request, and inject FileItem objects
   *
   * @request POST: http://localhost:8080/portal/rest/v1/calendar/events/Event123/attachments
   * 
   * @response  HTTP status code:
   *            201 if created successfully, and http header *location* href point to the newly created attachment resource.
   *            404 if event to create attachment not found 
   *            401 if user don"t have create permission, 503 if there is any error during the save process
   * 
   * @return http status code
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
   * Returns events of an calendar with specified id when:
   * - the calendar is public
   * - the authenticated user is the owner of the calendar of the event
   * - the authenticated user belongs to the group of the calendar of the event
   * - the authenticated user is a participant of the event
   * - the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id              
   *        identity of a calendar to search for events
   * 
   * @param start         
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *from* this date.
   *        Default: current server time.
   * 
   * @param end           
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *to* this date
   *        Default: current server time + 1 week.
   * 
   * @param category  
   *        search for this category only. If not specify, search event of any category
   *
   * @param offset       
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit         
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param resturnSize  
   *        tell the service if it must return the total size of the returned collection result, and the *link* http headers. 
   *        It can be true or false, by default, it's *false*
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar,categories. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit* of the rest service) value into param, for example: expand=categories(1,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendar: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendar: {
   *            id: "...",
   *            name:"demo-defaultId",
   *            ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/myCalId/events?category=meeting&expand=calendar,categories(1,5)
   * 
   * @format JSON
   * 
   * @response 
   * [
   *   {
   *      id: "myEventId",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/events/myEventId",
   *      subject: "..", description: "...",
   *      from: "...", to: "...",
   *      calendar: "...", categories: ["...", "..."],
   *      location: "", priority: "", 
   *      repeat: {...},
   *      recurrenceId: "...", originalEvent: "...",
   *      reminder: [], attachment: [], participants: [],
   *      privacy: "", availability: "" 
   *   }, 
   *   {id...}
   * ]
   * 
   * @return  List of events of a specific calendar
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
   * Creates an event in a calendar with specified id only if:
   * - the authenticated user is the owner of the calendar
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar has been shared with the authenticated user, with modification rights
   * - the calendar has been shared with a group of the authenticated user, with modification rights
   * 
   * This entry point only allow http POST request, with json object (evObject) in the request body. Example:
   *    {
   *      categoryId: "",
   *      subject: "..", description: "...",
   *      from: "...", to: "...",
   *      location: "", priority: "", 
   *      repeat: {...},
   *      reminder: [],
   *      privacy: "", availability: "" 
   *   }
   * 
   * @param evObject    
   *        json object contains attributes of event object to create.
   *        All attribute are optional. If provided explitly (not null), attributes are checked with some rules:
   *        1. subject must not be empty, default value is: default
   *        2. availability can only be one of "available", "busy", "outside"
   *        3. repeat.repeatOn can only be one of"MO", "TU", "WE", "TH", "FR", "SA", "SU"
   *        4. repeat.repeatBy must be >= 1 and <= 31
   *        5. repeat.repeatType must be one of "norepeat", "daily", "weekly", "monthly", "yearly"
   *        6. "from" date must be before "to" date
   *        7. priority must be one of "none", "high", "normal", "low"
   *        8. privacy can only be public or private
   * 
   * @param id                
   *        identity of the *calendar* to create event
   * 
   * @request  POST: http://localhost:8080/portal/rest/v1/calendar/calendars/myCalId/events
   *
   * @response  HTTP status code: 
   *            201 if created successfully, and http header *location* href point to the newly created event.
   *            400 if provided attributes are not valid (not following the rule of evObject)
   *            404 if no calendar found with provided id.
   *            401 if user don't have create permission, 503 if there is any error during the save process.
   * 
   * @return  http status code
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
   * Returns occurrences of a recurring event with specified id when :
   * the calendar of the event is public
   * the authenticated user is the owner of the calendar of the event
   * the authenticated user belongs to the group of the calendar of the event
   * the authenticated user is a participant of the event
   * the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id             
   *        identity of recurrence event, if event not exists, return 404 http status code
   * 
   * @param start         
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *from* this date.
   *        Default: current server time.
   * 
   * @param end           
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *to* this date
   *        Default: current server time + 1 week.
   *
   * @param offset       
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit         
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param resturnSize  
   *        tell the service if it must return the total size of the returned collection result, and the *link* http headers. 
   *        It can be true or false, by default, it's *false*
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar,categories. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit* of the rest service) value into param, for example: expand=categories(1,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendar: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendar: {
   *            id: "...",
   *            name:"demo-defaultId",
   *            ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/events/Event123/occurences?offset=1&limit=5
   * 
   * @format  JSON
   * 
   * @response 
   * [
   *   {
   *      id: "myEventId",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/events/myEventId",
   *      subject: "..", description: "...",
   *      from: "...", to: "...",
   *      calendar: "...", categories: ["...", ""],
   *      location: "", priority: "", 
   *      repeat: {...},
   *      recurrenceId: "...", originalEvent: "...",
   *      reminder: [], attachment: [], participants: [],
   *      privacy: "", availability: "" 
   *   }, 
   *   {id...}
   * ]
   * 
   * @return        List of occurrence events
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
   * Returns tasks of a calendar with specified id when:
   * - the calendar is public
   * - the authenticated user is the owner of the calendar of the task
   * - the authenticated user belongs to the group of the calendar of the task
   * - the authenticated user is delegated by the task
   * - the calendar of the task has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id              
   *        identity of a calendar to search for tasks
   * 
   * @param start         
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *from* this date.
   *        Default: current server time.
   * 
   * @param end           
   *        date follow ISO8601 (YYYY-MM-DDThh:mm:ssTZD). Search for events *to* this date
   *        Default: current server time + 1 week.
   * 
   * @param category  
   *        search for this category only. If not specify, search task of any category
   *
   * @param offset       
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit         
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param resturnSize  
   *        tell the service if it must return the total size of the returned collection result, and the *link* http headers. 
   *        It can be true or false, by default, it's *false*
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar,categories. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit* of the rest service) value into param, for example: expand=categories(1,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendar: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendar: {
   *            id: "...",
   *            name:"demo-defaultId",
   *            ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/myCalId/tasks?category=meeting&expand=calendar,categories(1,5)
   * 
   * @format JSON
   * 
   * @response 
   * [
   *   {
   *      id: "Task123",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/tasks/Task123",
   *      name: "..", note: "...",
   *      from: "...", to: "...",
   *      calendar: "...", categories: ["...", ""],
   *      delegation: ["...", ""], priority: "", 
   *      reminder: [], attachment: [],
   *      status: ""
   *   }, 
   *   {id...}
   * ]
   * 
   * @return  List of tasks of a specific calendar
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
   * Creates a task for a calendar with specified id only if:
   * - the authenticated user is the owner of the calendar
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar has been shared with the authenticated user, with modification rights
   * - the calendar has been shared with a group of the authenticated user, with modification rights
   * 
   * This entry point only allow http POST request, with json object (evObject) in the request body. Example:
   *    {
   *      name: "..", note: "...",
   *      categoryId: "",
   *      from: "...", to: "...",
   *      delegation: ["...", ""], priority: "", 
   *      reminder: [],
   *      status: ""
   *   }
   * 
   * @param evObject    
   *        json object contains attributes of task object to create.
   *        All attribute are optional. If provided explitly (not null), attributes are checked with some rules:
   *        1. name must not be empty, default value is: "default".
   *        2. "from" date must be before "to" date
   *        3. priority must be one of "none", "high", "normal", "low"
   *        4. status must be one of "needs-action", "completed", "in-progress", "canceled"
   * 
   * @param id                
   *        identity of the *calendar* to create task
   * 
   * @request  POST: http://localhost:8080/portal/rest/v1/calendar/calendars/myCalId/tasks
   * 
   * @response  HTTP status code: 
   *            201 if created successfully, and http header *location* href point to the newly created task.
   *            400 if provided attributes are not valid (not following the rule of evObject)
   *            404 if no calendar found with provided id.
   *            401 if user don't have create permission, 503 if there is any error during the save process.
   * 
   * @return  http status code
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
   * Returns a task with specified id if: same rules as /events/{id}
   * {@link #getEventById(String, String, String, String)}
   * 
   * @param id              
   *        identity of task to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar,categories. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit* of the rest service) value into param, for example: expand=categories(1,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendar: "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendar: {
   *            id: "...",
   *            name:"demo-defaultId",
   *            ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/tasks/Task123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   *   {
   *      id: "Task123",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/tasks/Task123",
   *      name: "..", note: "...",
   *      from: "...", to: "...",
   *      calendar: "...", categories: ["...", ""],
   *      delegation: ["...", ""], priority: "", 
   *      reminder: [], attachment: [],
   *      status: ""
   *   } 
   *  
   * @return  return task as json object
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
   * Updates a task with the specified id if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   * 
   * This entry point only allow http PUT request, with json object (evObject) in the request body, and task id in the path.
   * All the attributes of json object are optional, any omited attributes, or non-exists one will be ignored. *id* and *href*, *calendar* can't
   * be updated, they also be ignored.
   * For example:
   *   {
   *      name: "..", note: "...",
   *      categoryId: "...",
   *      from: "...", to: "...",
   *      delegation: ["...", ""], priority: "", 
   *      reminder: [],
   *      status: ""
   *   } 
   *  
   * @param id             
   *        identity of the task to update
   * 
   * @param evObject  
   *        json object contains attributes of task object to update, all the attributes are optional.
   *        If provided explitly (not null), attributes are checked with some rules:
   *        1. name must not be empty
   *        2. "from" date must be before "to" date
   *        3. priority must be one of "none", "high", "normal", "low"
   *        4. status must be one of "needs-action", "completed", "in-progress", "canceled" 
   * 
   * @request  PUT: http://localhost:8080/portal/rest/v1/calendar/tasks/Task123
   * 
   * @response  HTTP status code:
   *            200 if updated successfully, 404 if task with provided id doesnt exists,
   *            400 if provided attributes are not valid, 
   *            401 if user don't have create permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Deletes a task with specified id if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   *  
   * @param id  
   *        identity of the task to delete
   * 
   * @request  DELETE: http://localhost:8080/portal/rest/v1/calendar/tasks/Task123
   * 
   * @response  HTTP status code:
   *            200 if delete successfully, 404 if task with provided id doesnt exists,
   *            401 if user don't have permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Returns an attachment with specified id if:
   * - the calendar of the event is public
   * - the authenticated user is the owner of the calendar of the event
   * - the authenticated user belongs to the group of the calendar of the event
   * - the authenticated user is a participant of the event
   * - the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
   * 
   * @param id              
   *        identity of attachment to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/attachments/att123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   *   {
   *      id: "att123",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/attachments/att123",
   *      name: "..", mimeType: "...",
   *      weight: ""
   *   } 
   *  
   * @return  return attachment info as json object
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
   * Deletes an attachment with specified id if:
   * - the authenticated user is the owner of the calendar of the event
   * - for group calendars, the authenticated user has edit rights on the calendar
   * - the calendar of the event has been shared with the authenticated user, with modification rights
   * - the calendar of the event has been shared with a group of the authenticated user, with modification rights
   * 
   * @param id  
   *        identity of the attachment to delete
   * 
   * @request  DELETE: http://localhost:8080/portal/rest/v1/calendar/attachments/att123
   * 
   * @response  HTTP status code:
   *            200 if delete successfully, 404 if event that contains attachment doen't exists
   *            401 if user don't have permission, 503 if there is any error during the save process
   * 
   * @return status code
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
   * Returns the categories if an user is authenticated (the common categories + the personal categories)
   *
   * @param offset       
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit         
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param fields        
   *        This is a list of comma separated property"s names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/categories?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   * [
   *   {
   *      id: "myCat",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/categories/myCat",
   *      name: ".." 
   *   }, 
   *   {id...}
   * ]
   * 
   * @return  List of event categories
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
   * Returns the event category by id and it belongs to user
   * 
   * @param id              
   *        identity of event category to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/categories/cat123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   *   {
   *      id: "cat123",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/categories/cat123",
   *      name: ".." 
   *   }
   *  
   * @return  return event category info as json object
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
   * Gets a feed with the given id    
   * Returns the feed if the authenticated user is the owner of the feed
   *  
   * @param id              
   *        title of feed to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=calendar. In case of collections, 
   *        you can put offset (default: 0), limit (default: *query_limit* of the rest service) value into param, for example: expand=calendar(0,5).
   *        Instead of: 
   *        {
   *            id: "...", 
   *            calendars: {
   *                        "http://localhost:8080/portal/rest/v1/calendar/calendars/demo-defaultCalendarId"
   *                        }
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            calendars: {
   *                id: "...",
   *                name:"demo-defaultId",
   *            ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/feeds/feed123?fields=id,name
   * 
   * @format JSON
   * 
   * @response 
   *   {
   *      id: "feed123",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/feeds/feed123",
   *      name: "..", rss: "...",
   *      calendars: ["...", ".."]
   *   }
   *  
   * @return  return feed as json object
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
   * Updates a feed with the given id   
   * Updates the feed if the authenticated user is the owner of the feed 
   * 
   * This entry point only allow http PUT request, with json object (feedResource) in the request body, and feed id in the path.
   * All the attributes of json object are optional, any omited attributes, or non-exists one will be ignored. *id* and *href* attributes can't
   * be updated, they also be ignored.
   * For example:
   *   {
   *      name: "..",
   *      calendarIds: ["...", ".."]
   *   }
   *  
   * @param id             
   *        title of the feed to update
   * 
   * @param feedResource  
   *        json object contains attributes of feed object to update, all the attributes are optional 
   * 
   * @request  PUT: http://localhost:8080/portal/rest/v1/calendar/feeds/feed123
   * 
   * @response  HTTP status code:
   *            200 if updated successfully, 404 if feed with provided id doesnt exists,
   *            503 if there is any error during the save process
   * 
   * @return status code
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
   * Deletes a feed with the given id    
   * Deletes the feed if the authenticated user is the owner of the feed 
   * 
   * @param id  
   *        title of the feed to delete
   * 
   * @request  DELETE: http://localhost:8080/portal/rest/v1/calendar/feeds/feed123
   * 
   * @response  HTTP status code:
   *            200 if delete successfully,
   *            503 if there is any error during the save process
   * 
   * @return status code
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
   *  Gets the RSS stream of the feed with the given id
   *  Returns the RSS stream if:
   *   - the calendar is public
   *   - the authenticated user is the owner of the calendar
   *   - the authenticated user belongs to the group of the calendar
   *   - the calendar has been shared with the authenticated user or with a group of the authenticated user
   *   
   * @param id              
   *        title of the feed 
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/feeds/feed123/rss
   * 
   * @format application/xml
   * 
   * @response rss
   *  
   * @return  return rss from feed
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
   * Returns an invitation with specified id if:
   * - the authenticated user is the participant of the invitation
   * - the authenticated user has edit rights on the calendar of the event of the invitation
   * 
   * @param id              
   *        identity of invitation to find
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=event
   *        Instead of: 
   *        {
   *            id: "...", 
   *            event: "http://localhost:8080/portal/rest/v1/calendar/events/evt123"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            event: {
   *                id: "...",
   *                name:"myEvent",
   *                ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/invitations/evt123:root
   * 
   * @format  JSON
   * 
   * @response 
   *   {
   *      id: "evt123:root",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/events/evt123",
   *      event: "..", participant: "...",
   *      status: ""
   *   }
   *  
   * @return  invitation as json object
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
   * Update the invitation if the authenticated user is the participant of the invitation
   * This entry point only allow http PUT request, with id of invitation in the path, and the status
   *  
   * @param id             
   *        identity of the invitation to update
   * 
   * @param status      
   *        new status to update ("", "maybe", "yes", "no")
   * 
   * @request  PUT: http://localhost:8080/portal/rest/v1/calendar/invitations/evt123:root
   * 
   * @response  HTTP status code:
   *            200 if updated successfully,
   *            404 if invitation with provided id doesnt exists,
   *            400 if status is not valid,
   *            401 if user don't have create permission, 503 if there is any error during the save process
   * 
   * @return  status code
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
   * Delete an invitation with specified id if the authenticated user has edit rights on the calendar of the event of the invitation
   *  
   * @param id 
   *        identity of the invitation to delete
   * 
   * @request  DELETE: http://localhost:8080/portal/rest/v1/calendar/invitations/evt123:root
   * 
   * @response  HTTP status code:
   *            200 if delete successfully,
   *            404 if invitation with provided id doesnt exists,
   *            401 if user don't have permission,
   *            503 if there is any error during the save process
   * 
   * @return status code
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
   * Returns invitations of an event with specified id when:
   * the authenticated user is the participant of the invitation
   * the authenticated user has edit rights on the calendar of the event of the invitation
   * 
   * @param id         
   *        identity of event to search for invitations
   *
   * @param status           
   *        search for this status only. If not specify, search invitation of any status ("", "maybe", "yes", "no")
   * 
   * @param offset            
   *        The starting point when paging through a list of entities. Defaults to *0*.
   * 
   * @param limit              
   *        The maximum number of results when paging through a list of entities, if not specify or exceed
   *        the *query_limit* configuration of calendar rest service, it will use the *query_limit* 
   *        (see more on {@link #CalendarRestApi(OrganizationService, InitParams)} java doc)
   * 
   * @param fields        
   *        This is a list of comma separated property's names of response json object,
   *        if not specified, it return the json will all available properties.
   * 
   * @param jsonp        
   *        The name of a JavaScript function to be used as the JSONP callback, if not specified, only
   *        json object is returned.
   * 
   * @param expand     
   *        used to ask for a full representation of a subresource, instead of only its link. 
   *        This is a list of comma-separated property's names. For example: expand=event
   *        Instead of: 
   *        {
   *            id: "...", 
   *            event: "http://localhost:8080/portal/rest/v1/calendar/events/evt123"
   *            ....
   *        }
   *        It returns:
   *        {
   *            id: "...", 
   *            event: {
   *                id: "...",
   *                name:"myEvent",
   *                ....
   *            }
   *            ....
   *        }
   * 
   * @request  GET: http://localhost:8080/portal/rest/v1/calendar/events/evt123/invitations
   * 
   * @format  JSON
   * 
   * @response 
   * [
   *   {
   *      id: "evt123:root",
   *      href: "http://localhost:8080/portal/rest/v1/calendar/events/evt123",
   *      event: "..", participant: "...",
   *      status: ""
   *   }, 
   *   {id...}
   * ]
   * 
   * @return  List of invitations of a specific event
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
   *  Creates an invitation in the event with the given id . Creates the invitation only if:
   *  - the authenticated user is the participant of the invitation
   *  - the authenticated user has edit rights on the calendar of the event of the invitation
   * 
   * @param id                       
   *        identity of the *event* to create invitation
   * 
   * @param participant        
   *        name of participant (userId). If not provided or empty, return 400 status code
   * 
   * @param status               
   *        status of invitation ("", "maybe", "yes", "no")
   * 
   * @request  POST: http://localhost:8080/portal/rest/v1/calendar/events/evt123/invitations
   * 
   * @response  HTTP status code:
   *            201 if created successfully, and http header *location* href point to the newly created event.
   *            400 if participant or status is not valid.
   *            404 if event not found with provided id.
   *            401 if user don't have create permission,
   *            503 if there is any error during the save process.
   * 
   * @return  http status code
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
