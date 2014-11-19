package org.exoplatform.calendar.ws;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.ws.common.RestSecurityService;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/rest-security-configuration.xml")
})
public class TestRestSecurityService extends AbstractKernelTest {
  private RestSecurityService service;
  
  public void setUp() throws Exception {
    super.setUp();
    service = (RestSecurityService)getContainer().getComponentInstanceOfType(RestSecurityService.class); 
  }

  public void testNoConfig() {
    login("john");
    assertTrue(service.hasPermission("/"));    
  }
  
  public void testSimple() {
    login("john");
    assertFalse(service.hasPermission("/v1/calendar"));
    
    login("john", "*:/platform/administrators");
    assertTrue(service.hasPermission("/v1/calendar"));
  }
  
  public void testComplex() {
    login("john");
    assertTrue(service.hasPermission("/v1/social"));
    assertFalse(service.hasPermission("/v1/calendar/event"));
    
    login("john", "*:/platform/administrators");
    assertTrue(service.hasPermission("/v1/calendar/event"));
  }
  
  public void testSuperUser() {
    login("john");
    assertFalse(service.hasPermission("/v1/calendar"));
    
    login("root");
    assertTrue(service.hasPermission("/v1/calendar"));
  }
  
  protected void login(String username, String ...memberships) {
    List<MembershipEntry> membershipEntries = new LinkedList<MembershipEntry>();
    for (String ms : memberships) {
      String[] tmp = ms.split(":");
      MembershipEntry membershipEntry = new MembershipEntry(tmp[1], tmp[0]);
      membershipEntries.add(membershipEntry);      
    }
    
    Identity identity = new Identity(username, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
  
  protected String currentUser() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }  
}