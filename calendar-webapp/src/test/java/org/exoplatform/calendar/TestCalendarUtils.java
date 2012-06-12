package org.exoplatform.calendar;


import junit.framework.TestCase;

public class TestCalendarUtils extends TestCase {
  
  public void testGetLocationDisplayString() {
    assertEquals("", CalendarUtils.getLocationDisplayString(""));
    
    assertEquals("Viet nam", CalendarUtils.getLocationDisplayString("Viet nam"));
    
    String vietnamLocation = "Vietnam(Vietnamese)";
    String countryname = "VNM";
    
    assertEquals(vietnamLocation, CalendarUtils.getLocationDisplayString(countryname));
  }
  
  public void testGenerateTimeZoneLabel() {
    assertEquals("", CalendarUtils.generateTimeZoneLabel(""));
    
    assertEquals("Viet nam", CalendarUtils.generateTimeZoneLabel("Viet nam"));
    
    String vietnamTimezone = "Asia/Ho_Chi_Minh";
    String display = "(GMT +07:00) Asia/Ho_Chi_Minh";
    
    assertEquals(display, CalendarUtils.generateTimeZoneLabel(vietnamTimezone));
  }
  
  public void testCleanValue() throws Exception {
    String values = "";
    assertEquals("", CalendarUtils.cleanValue(values));
    values = "root,";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = "root, ";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = " root     ,root ";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = " root, root , root ";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = ",root,";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = " , root , ";
    assertEquals("root", CalendarUtils.cleanValue(values));
    values = " , root , root abc ";
    assertEquals("root,root abc", CalendarUtils.cleanValue(values));
    values = "demo, root, exo/test  ,   platform/user ";
    assertEquals("demo,root,exo/test,platform/user", CalendarUtils.cleanValue(values));
  }
}

