/**
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Feb 26, 2010
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UICalendarSettingFeedTab.gtmpl",
    events = {
        @EventConfig(listeners = UICalendarSettingFeedTab.ShowPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UICalendarSettingFeedTab.DeleteActionListener.class, phase = Phase.DECODE
                     , confirm = "UICalendarSettingFeedTab.msg.confirm-delete"),
        @EventConfig(listeners = UICalendarSettingFeedTab.RssActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UICalendarSettingFeedTab.EditActionListener.class, phase = Phase.DECODE)
    }
) 
public class UICalendarSettingFeedTab extends UIFormInputWithActions {
  private Map<String, List<ActionData>> actionField_  = new HashMap<String, List<ActionData>>() ;
  public static String[] BEAN_FIELD = {"feed"};
  private static String[] ACTION = {"Rss", "Edit", "Delete"} ;
  
  public UICalendarSettingFeedTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
    
    UIGrid grid = addChild(UIGrid.class, null , "UIFeedList") ;
    grid.configure("feed", BEAN_FIELD, ACTION) ;
    grid.getUIPageIterator().setId("FeedListIterator");
    CalendarService calendarService = CalendarUtils.getCalendarService();
    setFeedList(calendarService.getFeeds(CalendarUtils.getCurrentUser()));
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
 
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  
  public UIFormRadioBoxInput getUIFormRadioBoxInput (String id) {
    return findComponentById(id);
  }
 
  @SuppressWarnings("unchecked")
  public List<FeedData> getData() throws Exception {
    return new LinkedList<FeedData>(getChild(UIGrid.class).getUIPageIterator().getCurrentPageData());
  }
  
  public UIPageIterator  getUIPageIterator() {  return getChild(UIGrid.class).getUIPageIterator() ; }
  
  public long getAvailablePage(){ return getChild(UIGrid.class).getUIPageIterator().getAvailablePage() ;}
  
  public long getCurrentPage() { return getChild(UIGrid.class).getUIPageIterator().getCurrentPage();}
  
  public void setFeedList(List<FeedData> feedList) throws Exception {
    LazyPageList<FeedData> pageList = new LazyPageList<FeedData>(new ListAccessImpl<FeedData>(FeedData.class, feedList), 10);
    getChild(UIGrid.class).getUIPageIterator().setPageList(pageList) ;
  }
  protected void updateCurrentPage(int page) throws Exception{
    getChild(UIGrid.class).getUIPageIterator().setCurrentPage(page) ;
  }

  static  public class ShowPageActionListener extends EventListener<UICalendarSettingFeedTab> {
    @Override
    public void execute(Event<UICalendarSettingFeedTab> event) throws Exception {
      UICalendarSettingFeedTab uiForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);           
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UICalendarSettingFeedTab> {
    @Override
    public void execute(Event<UICalendarSettingFeedTab> event) throws Exception {
      UICalendarSettingFeedTab uiform = event.getSource() ;
      String title = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String username = CalendarUtils.getCurrentUser();
      calendarService.removeFeedData(username, title);
      uiform.setFeedList(calendarService.getFeeds(username));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
    }
  }
  
  static  public class EditActionListener extends EventListener<UICalendarSettingFeedTab> {
    @Override
    public void execute(Event<UICalendarSettingFeedTab> event) throws Exception {
      String title = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarSettingFeedTab uiform = event.getSource() ;      
      UIPopupContainer popupContainer = uiform.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      UIEditFeed uiEditFeed = popupAction.activate(UIEditFeed.class, 500) ;
      uiEditFeed.setNew(false);
      FeedData selectedFeed = null;
      for (FeedData feed : uiform.getData())
        if (feed.getTitle().equals(title)) selectedFeed = feed;
      if (selectedFeed != null) uiEditFeed.setFeed(selectedFeed);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;           
    }
  }
  
  static  public class RssActionListener extends EventListener<UICalendarSettingFeedTab> {
    @Override
    public void execute(Event<UICalendarSettingFeedTab> event) throws Exception {
      UICalendarSettingFeedTab uiform = event.getSource() ;
      String title = event.getRequestContext().getRequestParameter(OBJECTID) ;
      List<FeedData> feeds = new ArrayList<FeedData>() ;
      for (FeedData feedData : uiform.getData())
        if (feedData.getTitle().equals(title)) {
          feeds.add(feedData);
          break;
        }
      UIPopupContainer popupContainer = uiform.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      UIFeed uiFeed = popupAction.activate(UIFeed.class, 600) ;
      uiFeed.setFeeds(feeds);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
    }
  }
  
}
