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
package org.exoplatform.cs.calendar.test;

import org.exoplatform.portal.test.selenium.tools.AbstractPortalTestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class CategoryTest extends AbstractPortalTestCase {

	public void setUp() throws Exception {
	  setUp("http://localhost:8080/", "*chrome");
    loginAsRoot();
    selenium.open("/portal/private/classic/calendar");
    selenium.waitForPageToLoad(DEFAULTTIMEOUT);
    assertTrue(selenium.isTextPresent("Calendar Portlet"));
  }

  public void tearDown() throws Exception {
    logout();
    super.tearDown();
  }


  private void loadAddCategoryUI() throws InterruptedException {
    selenium.click("//div[@onclick='eXo.calendar.UICalendarPortlet.showMainMenu(this, event);']");
		selenium.click("//div[@id='tmpMenuElement']/div/div[2]/div/div/a[3]/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("Event categories")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
  }

  /**
   * @testId 002
   * @throws InterruptedException
   */
  public void testAddCategorie() throws InterruptedException {
    loadAddCategoryUI();

    int idx = getLastIndex("//div[@id='UIEventCategoryList']/table/tbody/tr");

    selenium.type("eventCategoryName", "demo");
		selenium.type("description", "test");
		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("demo")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

    //delete the created category
		selenium.click("//div[@id='UIEventCategoryList']/table/tbody/tr[" + (idx + 1) + "]/td[2]/div/div/a[2]/img");
		assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete[\\s\\S]$"));
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (!selenium.isTextPresent("demo")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[3]/div/div/div");
	}


  /**
   *
   * @testId 003
   * @throws Exception
   */
  public void testAddEmptyCategory() throws Exception {
		loadAddCategoryUI();

		selenium.type("description", "test");
		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("eXo Messages")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		assertTrue(selenium.isTextPresent("The field \"Event category\" is required"));
  } 

    /**
   *
   * @testId 004
   * @throws Exception
   */
  public void testAddExistingCategory() throws Exception {
		loadAddCategoryUI();

    int idx = getLastIndex("//div[@id='UIEventCategoryList']/table/tbody/tr");
    selenium.type("eventCategoryName", "Meeting");
		selenium.type("description", "test");
		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) break;
      if(selenium.isElementPresent("//div[@id='UIEventCategoryList']/table/tbody/tr[" + (idx + 1) + "]/td[1]"))
        fail("this should not add an element in the list of category");
			Thread.sleep(1000);
		}

  }

  /**
   *
   * @testId 005
   * @throws Exception
   */
  public void testSpecialCharactereInCategory1() throws Exception {
		isSpecialCharacterFail("&");
  }

  /**
   *
   * @testId 005
   * @throws Exception
   */
  public void testSpecialCharactereInCategory2() throws Exception {
		isSpecialCharacterFail("@");
  }  

  /**
   *
   * @testId 005
   * @throws Exception
   */
  public void testSpecialCharactereInCategory3() throws Exception {
		isSpecialCharacterFail("$");
  }

  /**
   *
   * @testId 005
   * @throws Exception
   */
  public void testSpecialCharactereInCategory4() throws Exception {
		isSpecialCharacterFail("%");
  }

  private void isSpecialCharacterFail(String character) throws InterruptedException {
    loadAddCategoryUI();

    selenium.type("eventCategoryName", "de" + character + "mo");
    selenium.type("description", "test");
		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("eXo Messages")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		assertTrue(selenium.isTextPresent("Category name is invalid or exists!"));
  }

  /**
   * @testId 006
   * @throws Exception
   */
  public void testEditCategForm() throws Exception {
		selenium.click("//form[@id='UIWeekView']/div[1]/a[3]/div");

		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("Event categories")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
  }


  private void loadEditCategoryUI() throws InterruptedException {
    selenium.click("//form[@id='UIWeekView']/div[1]/a[3]/div");

		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent("Event categories")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
  }

  /**
   * @testId 007
   * @throws Exception
   */
  public void testEditCategName() throws Exception {
    loadEditCategoryUI();

    String name = selenium.getText("//div[@id='UIEventCategoryList']/table/tbody/tr[2]/td[1]");

    selenium.click("//div[@id='UIEventCategoryList']/table/tbody/tr[2]/td[2]/div/div/a[1]/img");


    for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (name.equals(selenium.getValue("eventCategoryName"))) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
    String newName = name + " modified";
    selenium.type("eventCategoryName", newName);

    selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent(newName)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
  } 

  /**
   * @testId 010
   * @throws Exception
   */
  public void testDeleteCategory() throws Exception {
		loadEditCategoryUI();

    // get the name of the category we want to delete
    String name = selenium.getText("//div[@id='UIEventCategoryList']/table/tbody/tr[2]/td[1]");
    

    // delete the category
    selenium.click("//div[@id='UIEventCategoryList']/table/tbody/tr[2]/td[2]/div/div/a[2]/img");
		assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete[\\s\\S]$"));

    // check if the category has been deleted
    for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (!selenium.isTextPresent(name)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}


    //recreate the category to not perturbate the other tests
    selenium.type("eventCategoryName", name);
		selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
		for (int second = 0;; second++) {
			if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
			try { if (selenium.isTextPresent(name)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}    
  }

    /**
   * @testId 011
   * @throws Exception
   */
  public void testDeleteAllCategory() throws Exception {
		loadEditCategoryUI();

    List categoriesDeleted = new ArrayList();

    //While there is 2 colums in the table. (when it's empty, there is only 1 column)
    while(selenium.isElementPresent("//div[@id='UIEventCategoryList']/table/tbody/tr[1]/td[2]")) {
      // get the name of the category we want to delete
      String name = selenium.getText("//div[@id='UIEventCategoryList']/table/tbody/tr[1]/td[1]");
      categoriesDeleted.add(name);
      // delete the category
      selenium.click("//div[@id='UIEventCategoryList']/table/tbody/tr[1]/td[2]/div/div/a[2]/img");
      assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete[\\s\\S]$"));

      // check if the category has been deleted
      for (int second = 0;; second++) {
        if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
        try { if (!selenium.isTextPresent(name)) break; } catch (Exception e) {}
        Thread.sleep(1000);
      }
    }

    assertTrue(selenium.isTextPresent("Empty data"));

    //recreate the categories to not perturbate the other tests
    Iterator it = categoriesDeleted.iterator();

    while(it.hasNext()) {
      String name = (String) it.next();
      selenium.type("eventCategoryName", name);
      selenium.click("//form[@id='UIEventCategoryForm']/div/div/div/table/tbody/tr/td/a[1]/div/div/div");
      for (int second = 0;; second++) {
        if (second >= DEFAULTTIMEOUTSEC) fail("timeout");
        try { if (selenium.isTextPresent(name)) break; } catch (Exception e) {}
        Thread.sleep(1000);
      }
    }
  }

}