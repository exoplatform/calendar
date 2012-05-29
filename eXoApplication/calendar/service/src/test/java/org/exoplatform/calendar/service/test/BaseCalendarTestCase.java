/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.calendar.service.test;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS
 * @author : Hung nguyen
 *          hung.nguyen@exoplatform.com
 * May 7, 2008  
 */
public abstract class BaseCalendarTestCase extends BasicTestCase {

  protected static Log                  log                    = ExoLogger.getLogger("cs.calendar.services.test");

  protected static RepositoryService    repositoryService;

  protected static StandaloneContainer  container;

  protected final static String         REPO_NAME              = "repository".intern();

  protected final static String         SYSTEM_WS              = "system".intern();

  protected final static String         COLLABORATION_WS       = "collaboration".intern();

  protected static Node                 root_                  = null;

  protected SessionProvider             sessionProvider;

  private static SessionProviderService sessionProviderService = null;

  public static void init() {
    // we do this in static to save a few cycles
    initContainer();
    initJCR();
  }

  public BaseCalendarTestCase() throws Exception {
    init();
  }

  public void setUp() throws Exception {
    startSystemSession();
  }

  public void tearDown() throws Exception {
  }

  protected void startSystemSession() {
    sessionProvider = sessionProviderService.getSystemSessionProvider(null);
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sessionProvider = sessionProviderService.getSessionProvider(null);
  }

  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }

  /**
   * All elements of a list should be contained in the expected array of String
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    assertEquals(message, expected.size(), actual.size());
    assertTrue(message, expected.containsAll(actual));
  }

  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String[] expected, String[] actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }

  private static void initContainer() {
    try {
      String containerConf = BaseCalendarTestCase.class.getResource("/conf/portal/configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      container = StandaloneContainer.getInstance();
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("conf/portal/login.conf").toString();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginConf);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(), e);
    }
  }

  private static void initJCR() {
    try {
      repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

      // Initialize datas
      Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
      root_ = session.getRootNode();
      sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: " + e.getMessage(), e);
    }
  }

}
