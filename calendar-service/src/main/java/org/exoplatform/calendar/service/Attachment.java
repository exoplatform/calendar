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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */

public class Attachment extends AbstractBean {
  
  private static final Log log = ExoLogger.getExoLogger(Attachment.class);

  private String   name;

  private String   mimeType;

  private long     size;

  private byte[]   imageBytes;

  private String   workspace;

  private String   resourceId;

  /**
   * This class use for keep data and infomation about attachments
   * the id will automatic generate when create new object
   */
  public Attachment() {
    setId("Attachment" + IdGenerator.generate());
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType_) {
    this.mimeType = mimeType_;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size_) {
    this.size = size_;
  }

  public String getName() {
    return name;
  }

  public void setName(String name_) {
    this.name = name_;
  }

  public String getDataPath() {
    try {
      if(getSesison() != null) {
        Node attachmentData = (Node) getSesison().getItem(getId());
        if(attachmentData != null) {
          return attachmentData.getPath();
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("The attachment note is not exist", e);
      }

    }
    return null;
  }

  private Session getSesison() throws Exception {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
    return repoService.getCurrentRepository().getSystemSession(workspace);
  }

  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
    } else
      imageBytes = null;
  }

  public InputStream getInputStream() throws Exception {
    if (imageBytes != null)
      return new ByteArrayInputStream(imageBytes);
    Node attachment;
    try {
      attachment = (Node) getSesison().getItem(getId());
    } catch (ItemNotFoundException e) {
      return null;
    } catch (PathNotFoundException ex) {
      return null;
    }
    return attachment.getNode("jcr:content").getProperty("jcr:data").getStream();
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  public String getWorkspace() {
    return workspace;
  }

  /**
   * keep id to make sure temp file will be removed after use uploaded file
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * get id to call download service remove temp file
   */
  public String getResourceId() {
    return resourceId;
  }
}
