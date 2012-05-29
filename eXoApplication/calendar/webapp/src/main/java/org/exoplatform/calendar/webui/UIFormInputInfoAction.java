/*
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
 */
package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 26, 2010  
 */
public class UIFormInputInfoAction extends UIFormInputBase<String> {

  private Map<String, List<ActionData>> actionField_  = new HashMap<String, List<ActionData>>() ;

  final public static int TYPE_BUTTON = 2;

  public void processRender(WebuiRequestContext context) throws Exception
  {
    Writer w = context.getWriter();
    w.append("<span id=\"").append(getId()).append("\" class=\"").append(getId()).append("\">");
    if (value_ != null)
      w.write(value_);
    w.write("</span>");
    renderActions(this.getId(), w);
  }

  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
   
  public void removeActionField(String fieldName) throws Exception {
    actionField_.remove(fieldName) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}


  private void renderActions(String fieldKey, Writer w) throws Exception {
    List<ActionData> actions = getActionField(fieldKey) ;
    UIForm uiForm = (UIForm)getParent().getParent();
    if(actions != null) {
      for(ActionData action : actions) {
        String actionLabel = uiForm.getLabel("action." + action.getActionName());
        if(actionLabel.equals("action." + action.getActionName())) {
          actionLabel = action.getActionName();
        }
        String actionLink ="";
        
        if(action.getActionParameter() != null) {
          actionLink = (uiForm.event(action.getActionParameter(), action.getActionParameter())) ;
        }else {
          actionLink = (uiForm.event(action.getActionListener())) ;
        }
        if(actionLabel.lastIndexOf("-(") > 0)
        {
          String temp = actionLabel.substring(0, actionLabel.lastIndexOf("-(")) ;
          String sizeLabel = actionLabel.substring(actionLabel.lastIndexOf("-(")) ;
          if(temp.length() > 30) {
            actionLabel = temp.substring(0, 30) + "..." + sizeLabel ;}
        }
        if(action.getActionType() == ActionData.TYPE_ICON) {
          if(action.isShowLabel()) { w.append(actionLabel)  ;}
          w.append("<a style=\"display:inline-block;\" title=\"").append(action.getActionName()).append("\" href=\"").append(action.getActionListener()).append("\" target=\"_blank\" >")
         .append("<img src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" alt=\"\" class=\"").append(action.getCssIconClass()).append("\" />");
        } else if(action.getActionType() == ActionData.TYPE_LINK){
          w.append("<a title=\"").append(action.getActionName()).append("\" href=\"").append(actionLink).append("\" >")
          .append(actionLabel);
        } else if(action.getActionType() == TYPE_BUTTON){
          w.append("<a style=\"display:inline-block;\" title=\"").append(action.getActionName()).append("\" href=\"").append(actionLink).append("\" >")
          .append("<img src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" alt='' class=\"").append(action.getCssIconClass()).append("\" />");
        }
        w.write("</a> &nbsp;"); 
        if(action.isBreakLine()) w.write( "<br/>");
      }
    }
  }
  
  public UIFormInputInfoAction(String name, String bindingExpression, String value)
  {
     super(name, bindingExpression, String.class);
     this.value_ = value;
     readonly_ = true;
  }

  @Override
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    
  }
}
