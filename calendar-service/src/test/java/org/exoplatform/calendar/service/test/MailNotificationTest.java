package org.exoplatform.calendar.service.test;

import org.exoplatform.calendar.service.*;
import org.exoplatform.services.security.ConversationState;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.exoplatform.calendar.service.impl.MailNotification;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MailNotificationTest extends BaseCalendarServiceTestCase {

  private OrganizationService organizationService;

  private MailService         mailService;

  private CalendarService     calendarService;

  private final static String USERNAMEROOT = "root";

  public void setUp() throws Exception {
    super.setUp();
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    mailService = Mockito.mock(MailService.class);
    calendarService = getContainer().getComponentInstanceOfType(CalendarService.class);

    begin();

    UserHandler userHandler = organizationService.getUserHandler();
    User userLionel = userHandler.createUserInstance("lionel");
    userHandler.createUser(userLionel, true);
    userLionel.setFirstName("Lionel");
    userLionel.setLastName("Messi");
    userLionel.setEmail("lionel@gmail.com");
    userHandler.saveUser(userLionel, true);
    User userRonaldo = userHandler.createUserInstance("cristiano");
    userHandler.createUser(userRonaldo, true);
    userRonaldo.setFirstName("Cristiano");
    userRonaldo.setLastName("Ronaldo");
    userRonaldo.setEmail("cristiano@gmail.com");
    userHandler.saveUser(userRonaldo, true);

    ConversationState.getCurrent().setAttribute("UserProfile", organizationService.getUserHandler().findUserByName(USERNAMEROOT));
  }

  public void tearDown() throws Exception {
    super.tearDown();
    organizationService.getUserProfileHandler().removeUserProfile("lionel", true);
    organizationService.getUserHandler().removeUser("lionel", true);
    organizationService.getUserProfileHandler().removeUserProfile("cristiano", true);
    organizationService.getUserHandler().removeUser("cristiano", true);
    end();
  }

  public void testShouldSendMailToParticipantAfterCreateEventInPublicCalendar() throws Exception {
    // Test property 'exo.email.smtp.from' must not be null and must be set to default if not already defined.
    assertNotNull(MailNotification.EXO_EMAIL_SMTP_FROM);
    if (System.getProperty("exo.email.smtp.from") == null) {
      assertEquals("noreply@exoplatform.com", MailNotification.EXO_EMAIL_SMTP_FROM);
    }
    // Given
    Calendar calendar = createGroupCalendar(new String[] { "/platform/users", "/organization/management/executive-board" },
                                            "CalendarName",
                                            "CalendarDesscription");
    CalendarEvent newEvent = new CalendarEvent();
    newEvent.setCalendarId(calendar.getId());
    newEvent.setCalType(String.valueOf(Utils.PUBLIC_TYPE));
    newEvent.setSummary("Meeting");
    String[] participants = new String[] {"lionel", "samuel", "cristiano"};
    newEvent.setParticipant(participants);
    LocalDateTime fromDateTime = LocalDateTime.now();
    newEvent.setFromDateTime(Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    newEvent.setToDateTime(Date.from(fromDateTime.plusHours(2).atZone(ZoneId.systemDefault()).toInstant()));
    java.util.Calendar calFrom = java.util.Calendar.getInstance();
    calFrom.setTime(newEvent.getFromDateTime());
    java.util.Calendar calTo = java.util.Calendar.getInstance();
    calTo.setTime(newEvent.getToDateTime());
    Attachment attachment = new Attachment();
    attachment.setName("attachment1.txt");
    attachment.setMimeType("plain/text");
    attachment.setInputStream(new ByteArrayInputStream("text".getBytes()));
    newEvent.setAttachment(Arrays.asList(attachment));
    calendarService.savePublicEvent(calendar.getId(), newEvent, true);

    MailNotification mail = new MailNotification(mailService, organizationService, calendarService);

    // When
    mail.sendEmail(newEvent, USERNAMEROOT);

    // Then
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    Mockito.verify(mailService, Mockito.times(2)).sendMessage(messageCaptor.capture());
    List<Message> messages = messageCaptor.getAllValues();
    Message messageLionel = messages.get(0);
    Assert.assertTrue(messageLionel.getSubject().startsWith("[invitation] Meeting"));
    Assert.assertEquals("Root Root<" + MailNotification.EXO_EMAIL_SMTP_FROM + ">", messageLionel.getFrom());
    Assert.assertEquals("lionel@gmail.com", messageLionel.getTo());
    Assert.assertNotNull(messageLionel.getAttachment());
    Assert.assertEquals(2, messageLionel.getAttachment().size()); // ics file + attachment
    Message messageCristiano = messages.get(1);
    Assert.assertTrue(messageCristiano.getSubject().startsWith("[invitation] Meeting"));
    Assert.assertEquals("Root Root<" + MailNotification.EXO_EMAIL_SMTP_FROM + ">", messageCristiano.getFrom());
    Assert.assertEquals("cristiano@gmail.com", messageCristiano.getTo());
    Assert.assertNotNull(messageCristiano.getAttachment());
    Assert.assertEquals(2, messageCristiano.getAttachment().size()); // ics file + attachment
  }
  public void testShouldSendMailToParticipantAfterCreateEventInPrivateCalendar() throws Exception {
    // Test property 'exo.email.smtp.from' must not be null and must be set to default if not already defined.
    assertNotNull(MailNotification.EXO_EMAIL_SMTP_FROM);
    if (System.getProperty("exo.email.smtp.from") == null) {
      assertEquals("noreply@exoplatform.com", MailNotification.EXO_EMAIL_SMTP_FROM);
    }
    // Given
    Calendar calendar = createPrivateCalendar(USERNAMEROOT,"personalCalendar","it is a personal calendar");
    CalendarEvent newEvent = new CalendarEvent();
    newEvent.setCalendarId(calendar.getId());
    newEvent.setCalType(String.valueOf(Utils.PRIVATE_TYPE));
    newEvent.setSummary("Meeting");
    String[] participants = new String[] {"lionel", "samuel", "cristiano"};
    newEvent.setParticipant(participants);
    LocalDateTime fromDateTime = LocalDateTime.now();
    newEvent.setFromDateTime(Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    newEvent.setToDateTime(Date.from(fromDateTime.plusHours(2).atZone(ZoneId.systemDefault()).toInstant()));
    java.util.Calendar calFrom = java.util.Calendar.getInstance();
    calFrom.setTime(newEvent.getFromDateTime());
    java.util.Calendar calTo = java.util.Calendar.getInstance();
    calTo.setTime(newEvent.getToDateTime());
    Attachment attachment = new Attachment();
    attachment.setName("attachment1.txt");
    attachment.setMimeType("plain/text");
    attachment.setInputStream(new ByteArrayInputStream("text".getBytes()));
    newEvent.setAttachment(Arrays.asList(attachment));
    calendarService.saveUserEvent(USERNAMEROOT,calendar.getId(), newEvent, true);

    MailNotification mail = new MailNotification(mailService, organizationService, calendarService);

    // When
    mail.sendEmail(newEvent, USERNAMEROOT);

    // Then
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    Mockito.verify(mailService, Mockito.times(2)).sendMessage(messageCaptor.capture());
    List<Message> messages = messageCaptor.getAllValues();
    Message messageLionel = messages.get(0);
    Assert.assertTrue(messageLionel.getSubject().startsWith("[invitation] Meeting"));
    Assert.assertEquals("Root Root<" + MailNotification.EXO_EMAIL_SMTP_FROM + ">", messageLionel.getFrom());
    Assert.assertEquals("lionel@gmail.com", messageLionel.getTo());
    Assert.assertNotNull(messageLionel.getAttachment());
    Assert.assertEquals(2, messageLionel.getAttachment().size()); // ics file + attachment
    Message messageCristiano = messages.get(1);
    Assert.assertTrue(messageCristiano.getSubject().startsWith("[invitation] Meeting"));
    Assert.assertEquals("Root Root<" + MailNotification.EXO_EMAIL_SMTP_FROM + ">", messageCristiano.getFrom());
    Assert.assertEquals("cristiano@gmail.com", messageCristiano.getTo());
    Assert.assertNotNull(messageCristiano.getAttachment());
    Assert.assertEquals(2, messageCristiano.getAttachment().size()); // ics file + attachment
  }

}
