/**
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

====================================================== 
    Release Notes - eXo Collaboration - Version 2.2.5
====================================================== 

===============
1 Introduction
===============

** eXo Collaboration Suite provides a rich, Web 2.0 browser-based interface with comprehensive
messaging, shared calendars, address books,  indexing, archival and search capabilities.

*eXo Address Book manages private and public contacts in your organization. A contact
lets you keep precious information such as personal or professional address and other
reaching information such as telephone, fax, email or IM. We support VCard format
import/export so that you can consolidate your existing contacts into a single place.
Additionally, you can refer your contacts from other applications such as eXo Calendar, eXo Mail.

*eXo Calendar helps you to organize and order time in your life more easily and efficiently. It
also manages your shared and public agendas in your organization. eXo Calendar provides
iCal support that you can use to exchange event/task information with other applications.
Searching for events/tasks in calendars also very convenient with full text search and advanced search with many criteria.

*eXo Mail is a mail client that is built with a variety of features designed to make your e-mail
experience more productive. It offers several ways to to view and organize your mails in-box and conversation.

*eXo Chat is a live web-based Instant Messaging system. It is a Jabber chat implemented over the open xmpp protocol. With it, you can send instant message, exchange files real-time, store, export your conversation's history and sort by date, week, month. Group conversation very useful for co-working and group meeting online. eXo Chat comes in 2 flavours, the Chat application an webOS friendly windowed application and Chat Bar, an ubiquitous tiny chat bar that you can put on classic pages so that portal users can chat from wherever they are.


=============
2 What's new?
=============


    * General
	- Portuguese Translation
        - Performance improvements 
        - New Look in calendar Application 
        - New Look in chatbar application
        - DOM improvement 
        - Many bugs fixes
     
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6.0_0 or higher
        Application Server : tomcat-6.0.20 and up 
        Building Tools: Maven 3 and up
        openfire server version 3.6.4 for more information visit here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Chat+Configuration

- Collaboration suite quick start guide
  Collaboration suite have 2 servers need to run at same time to use:
    +) tomcat: this is main tomcat server include Collaboration web applications and all dependencies.     
    +) exo-chatserver: a Jabber server used for Chat applications

Need to set the JAVA_HOME variable for run Collaboration suite's servers.
+) How to start Collaboration sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.6"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
   
   
   
   * Start tomcat server
   
     +) On the Windows platform
       Open a DOS prompt command, go to tomcat/bin and type the command:
        "gatein.bat run" for production
        "gatein-dev.bat run" for development 


     +) On Unix/Linux/cygwin
       Open a terminal, go to tomcat/bin and type the command:
         "./gatein.sh run" for production
         "./gatein-dev.sh run" for development
    
   * Start eXo-chatserver:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-chatserver/bin and type the command:        
         run_demo.bat for using chat in csdemo

     +) On Unix/Linux
       Open a terminal, go to eXo-chatserver/bin and type the command:
         ./run_demo.sh for using chat in csdemo
		 
   * If on provided chat-server have no run*.* file you can reference here http://wiki.exoplatform.org/xwiki/bin/view/CS/Install%20Guide#HLaunchthechatserverforcs2.0      

To enable mail notifications, quickly you can use your own email account for example :

# EMail
gatein.email.smtp.username=*youracount@server.com*
gatein.email.smtp.password=*yourpassword*
gatein.email.smtp.host=smtp.gmail.com
gatein.email.smtp.port=465
gatein.email.smtp.starttls.enable=true
gatein.email.smtp.auth=true
gatein.email.smtp.socketFactory.port=465
gatein.email.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory

in this file tomcat/gatein/conf/configuration.properties

-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:   
   CS demo portal  
      http://localhost:8080/

You can log into the portal with the following accounts: root, john, mary, demo.
All those accounts have the default password "gtn".



==============
4 KNOWN ISSUES
==============
 
   
===========
5 RESOURCES
===========

     
     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org/browse/CS
     Community site      http://www.exoplatform.org
     Community forum     http://forums.exoplatform.org
     Community gatein    http://www.jboss.org/gatein/ 
     Developers wiki     http://wiki.exoplatform.org/xwiki/bin/view/CS/
     Blog                http://blog.exoplatform.org/tags/cs/
     Download            http://sourceforge.net/projects/exo-collab/
     Source              http://svn.exoplatform.org/projects/cs/tags

===========
6 CHANGELOG
===========

- 2.2.5

** Bug
    * [CS-4631] - Bad french statement in calendar
    * [CS-4725] - The calendar application counts wrong
    * [CS-4821] - Unknown error when export message by delegated user
    * [CS-5354] - [Calendar] The Calendar portlet must use all the available space to be displayed on
    * [CS-5420] - [Calendar] On View Day, Event located is in wrong time range
    * [CS-5432] - Calendar: Error on creating an event or task with option All day 
    * [CS-5433] - [Mail] Button Next Page doesn't work, Button Last Page works wrong
    * [CS-5440] - Wrong URL of calendar feed
    * [CS-5444] - Wrong error message when creating an account in CS Mail
    * [CS-5445] - The calendar check boxes not un-cheched when unchecked the calendar check box
    * [CS-5446] - The rename of a personal folder name is bugged
    * [CS-5448] - Synchronize folder if a folder on Gmail is created don't work
    * [CS-5461] - [Wiki] Page content is displayed in front of a chatbar window
    * [CS-5463] - IE7 UI - Calendar - Some icons are not lined-up
    * [CS-5464] - FF UI - Calendar - Almost all views are seriously broken
    * [CS-5472] - [Calendar] Simple search doesn't work
    * [CS-5493] - [Contact] Nothing happens when click on print preview button
    * [CS-5505] - [Contact] Cannot add tag by Add tag action

** Feedback
    * [CS-4900] - Some errors in text of Mail 
    * [CS-5435] - Calendar activities : poor usability

** Improvement
    * [CS-4761] - Remote Calendar : When suscribing to a remote URL we should automatically add the name and description from the content of the ICS
    * [CS-4791] - Sort calendar search results by date by default
    * [CS-5033] - [CS][Calendar] current date is not red-highlighted anymore when changing to next month
    * [CS-5126] - [Mail] Advanced DMS UI/UXP Improvement
    * [CS-5317] - Soft re-looking of calendar
    * [CS-5380] - [Calendar] Calendars popups improvements
    * [CS-5428] - Externalize some JCR Workspace properties

- 2.2.4

** Bug
    * [CS-4602] - Check displaying Add participant form of Calendar in French
    * [CS-4780] - Calendar - Alret message form is invalid display
    * [CS-4806] - Left pane of calendar inside a space is shown as default
    * [CS-4814] - [CS-Calendar] High light date is incorrect when add event with [All day] option
    * [CS-5302] - [Mail] Can't not read mail after checking
    * [CS-5360] - KS: Problem when show default group calendars
    * [CS-5383] - IE7: Remove scrollbar in Search Results form
    * [CS-5392] - [Mail] Can not sync imap folder
    * [CS-5397] - [Calendar] Quick add event form disappear after submit
    * [CS-5402] - [Mail] IE9/FF5/Chrome12: Error in Calendar application
    * [CS-5412] - getissues REST service returns events from the following day if it starts at midnight
    * [CS-5418] - [Mail] Unknow error when download attach file from mail
    * [CS-5419] - [Mail] Java script error when Save attach file to server

** Improvement
    * [CS-5132] - [Calendar][Improvement] Inform msg shown when participant list is empty can make users confused
    * [CS-5327] - Space Activity Stream tab - tell to which space the message was created for
    * [CS-5379] - [Calendar] Soft relooking
    * [CS-5401] - REST service to retrieve calendar events in a date range

- 2.2.3

** Bug
    * [CS-4508] - Chat bar invalid link access to mail, calendar, contacts on csdemo
    * [CS-4758] - Calendar - right click menu on an event/task does not work
    * [CS-5070] - [CS][Mail] Can not get mail - error msg "Email account setting is invalid!"
    * [CS-5129] - [Calendar] IE7: Remove scrollbar in confirmation form when add new event
    * [CS-5130] - [Calendar] IE7: Error in preview event
    * [CS-5134] - [Mail] Error in Manage Accounts menu
    * [CS-5135] - [Mail] Error in Add folder menu
    * [CS-5147] - [CS][Mail] Incorrect number of unread mails in Inbox right after being received
    * [CS-5167] - [CS][Calendar] Timerange displayed at the header of Task/Event is wrong after task/event is dragged & dropped randomly - JS error
    * [CS-5176] - [CS][Chat][IE8] Broken UI in Account list
    * [CS-5177] - [CS][Chat][IE8] Can not close chat window
    * [CS-5180] - Wrong date default when adding new event for next day - Daily view - Calendar
    * [CS-5187] - Nothing happen when click on Home icon
    * [CS-5189] - Nothing happen when create new folder with blank name
    * [CS-5257] - ChatBar portlet raises javascript error under IE-7, IE-8
    * [CS-5278] - IE9-Calendar: Can not drag & drop Event/Task and Don't show Add Event form when left click on working pane
    * [CS-5283] - [CS][Chat] IE8 Broken GUI in contact list of Chat Room
    * [CS-5284] - [Mail] Not show received mails in mail list with POP 3 method
    * [CS-5288] - [Mail] Not show attachments that has space and no extension in file name (IMAP method)
    * [CS-5297] - Backport patch for slow display of Calendar for a very large number of user (20K) for 3.5
    * [CS-5301] - [Mail] Scroll bar in Create Account form
    * [CS-5303] - PLF Integrated social with calendar, could not interact with action after created event
    * [CS-5304] - PLF Integrated social with calendar, could not interact with action after created event
    * [CS-5305] - Participant's event cannot be displayed after accept and import from email's invitation
    * [CS-5307] - [CS] Gadget MyTasks request rest service sequential cause portal's time out function doesn't work
    * [CS-5309] - [Mail] Create account button is covered in IE 7
    * [CS-5320] - Incorrect label in Calendar portlet
    * [CS-5326] - [Mail]  There is no message after refresh Mail application
    * [CS-5337] - Cannot drag n drop an event
    * [CS-5338] - UI problem in cs 
    * [CS-5342] - [Packaging] Remove safely redundant the package client.openfire from CS server 
    * [CS-5343] - [Calendar] Invitations email is not send after edit event
    * [CS-5351] - [Calendar] Wrong confirm delete message
    * [CS-5353] - Chat : chat room participants are not displayed
    * [CS-5355] - Cannot resize an event
    * [CS-5356] - [Mail] Can not display preview of attachment that's image type.
    * [CS-5359] - [Chat] Name of an user is still displayed on list after she leaves a room
    * [CS-5362] - CLONE - Minicalendar isn't translated into French in email
    * [CS-5364] - Lose border in Calendar Group form
    * [CS-5365] - Lose border in Share Calendar form
    * [CS-5366] - Shared user/group is not listed on Share Calendar form
    * [CS-5367] - CLONE - [Calendar] UI bug when adding reminder email, participants in French
    * [CS-5370] - [CS]: Show duplicate pop-up messge after change edit mode of chatbar porltet
    * [CS-5371] - IE7: Error in Export contact form
    * [CS-5372] - No message to alert that message is sent on receipter side
    * [CS-5374] - Impossible to open CS bar's shortcuts in PLF
    * [CS-5375] - CLONE - The highlight is disappear after adding participant 
    * [CS-5376] - CLONE - Cannot open ics file in the invitation's email with MS Office Outlook 2003
    * [CS-5382] -  After refreshing browser, event/task is returned to original position
    * [CS-5391] - [Mail] Can not delete account
    * [CS-5394] - [Mail] Unknown error when edit delegate account
    * [CS-5395] - [Calendar] Wrong tooltip message
    * [CS-5398] - [Mail][Firefox 3.6] Bold line appear in UI
    * [CS-5407] - [Mail] Add and save attachment to server appear in CS standalone

** Feedback
    * [CS-5296] - Improve default calendar view

** Improvement
    * [CS-5179] - Support IE 8
    * [CS-5292] - UIPopup - check and improv. the UIPopup calls
    * [CS-5318] - Improve some templates to fit PLF3.5 skin style
    * [CS-5319] - Change the target xsd on xml file
    * [CS-5358] - [Calendar] Avoid hostname hardcode in RSS URL

- 2.2.2

** Bug
    * [CS-5071] - [CS][Mail] Don't show "attachment" icon right after getting mails with attached files into Inbox
    * [CS-5138] - [Chat] IE7: Error in Chat form
    * [CS-5142] - [CS][Calendar] UI of Minicalendar on left menu is broken - FF
    * [CS-5148] - [CS][AddressBook] GUI of "Add Contact" window is broken
    * [CS-5149] - [CS][Chat] Broken GUI in contact list of Chat Room
    * [CS-5185] - Should show child nodes of selected folder in the left pane
    * [CS-5188] - Show full attachment link
    * [CS-5190] - Nothing happen when save file is not existing
    * [CS-5242] - Cannot access to Calendar in Space
    * [CS-5243] - Cannot share addressbook
    * [CS-5255] - CS 2.2.x unit test fail
    * [CS-5258] - [CS] [Calendar] Js problem that causes a blockage of the menu
    * [CS-5260] - [Mail] Can not delete the file attachments in draft mail
    * [CS-5270] - [Mail] Select acount context menu lose style sheet
    * [CS-5272] - Cannot export a calendar which contains tasks
    * [CS-5274] - Cannot Edit, Move, Delete ... contact
    * [CS-5279] - [Mail] IE9/FF5/Chrome12: Lose border in Manage Account 
    * [CS-5281] - Find and fix all JCR Sessions leaks
    * [CS-5282] - Backport - Chat - there is no user notification of the session timeout
    * [CS-5286] - Attachment in mail - cannot view, download, save to dms
    * [CS-5287] - The position of text displayed incorrectly
    * [CS-5290] - Not change color when edit tags in Mail

** Improvement
    * [CS-4980] - [DOM] CalendarUIActivity optimization
    * [CS-5067] - [PLF3.5 Skin] - Redesign of Chat Bar
    * [CS-5123] - [Gadget] My Task and Upcoming event  UI improvement for new skin plf 3.5
    * [CS-5172] - Check compatible of UI new skin changed for UITabContainer, UIHorizontalTabs, UIPopupWindow
    * [CS-5241] - Support UIPopupWindow improvement - CS
    * [CS-5250] - Add priority for skin modules

- 2.2.1

** Bug
    * [CS-4864] - [CS-mail] Don't stop get mail and don't show arlert message when check mail with incorrect account
    * [CS-4984] - CS - Mail - Get message "Please select messages" when right-click on a message and select a menu item
    * [CS-5002] - Display "null: stop" at the first moment getting mail
    * [CS-5030] - [Chatbar] Search is case sensitive when trying to add a contact
    * [CS-5068] - CS - Mail - render UIMessagePreview is error, show exception in terminal.
    * [CS-5109] - CLONE - Ui problem with some events in week view 
    * [CS-5120] - Created event in a space context is not displayed 
    * [CS-5121] - [Calendar] Left Panel is not correctly displayed in Calendar of Space in PLF 3.5
    * [CS-5131] - [Calendar] Can not send remind email
    * [CS-5137] - Don't show list activity for added event/taks on space
    * [CS-5143] - [CS][Calendar] Menu is hidden --> can not logout by clicking on LogOut button - IE
    * [CS-5146] - [CS][Calendar] Timerange is displayed incorrectly on tooltip of Task/Event
    * [CS-5162] - Build CS failed because of changes from GateIn 
    * [CS-5169] - CS application links on AdminBar don't work
    * [CS-5175] - ChatBar button url are wrong in csdemo
    * [CS-5224] - [Mail] Can not check mail
    * [CS-5229] - Delete openfire.jar artifact from EAR distribution

** Improvement
    * [CS-4946] - Tasks and events are not created in calendar
    * [CS-5102] - Change calendar name display.
    * [CS-5124] - [Mail] First mail account configuration must be more visual
    * [CS-5128] - [Calendar] Should close form afer selecting user to share calendar
    * [CS-5208] - Performances Issues for QACAP
    * [CS-5223] - - Event category, default user calendar listbox must be filled by configuration 
    * [CS-5244] - CS 2.1.x Portuguese Translation

- 2.2.0-GA

** Bug
    * [CS-4541] - [Contact] Invalid contact list display when move contact
    * [CS-4560] - Calendar: Show exception when import Invalid file type or format
    * [CS-4649] - Mail - Can not get and send mail with exoplatform.vn server
    * [CS-4665] - Password require when send mail
    * [CS-4667] - The popup menu is not usually shown valid when right click at one mouse point
    * [CS-4688] - Unexpected error message display when click calendar settings
    * [CS-4725] - The calendar application counts wrong
    * [CS-4733] - Cannot load and merge the bundle
    * [CS-4736] - [chatbar] Contact list is not shown after changing status to online
    * [CS-4737] - [chatbar] user at "Free to chat" can not do some permitted actions
    * [CS-4752] - Right click on calendar to add event but selected calendar in event form is wrong
    * [CS-4753] - Switch tab in add event detail form, selected calendar alway back to default
    * [CS-4762] - Remote Calendar : Repeating events are not imported/synchronized correclty
    * [CS-4770] - [CS] Always show message when send a mail with attach file with name have accent
    * [CS-4776] - [Rss] french translation for rss reader portlet
    * [CS-4781] - Calendar- Some UI error on IE 7
    * [CS-4784] - Calendar's left panel always disappears after some actions
    * [CS-4786] - Closing search in calendar changes the displayed week
    * [CS-4789] - Contact - View a contact - link of home website is invalid
    * [CS-4792] - Calendar - Date format is not used when displaying search results
    * [CS-4793] - IncompatibleClassChangeError when adding a calendar event in space
    * [CS-4794] - Mail - Can not get mail
    * [CS-4805] - Invalid message display when delete a calendar
    * [CS-4807] - [PLF] [IE7] Intranet Home page - vertical scroll of Upcoming Events frame
    * [CS-4812] - Mail - Delegate user list is invalid in case delegate multiple account
    * [CS-4815] - [CS-Mail] Delegated user can not open Mail application
    * [CS-4816] - [Calendar-IE7] Left pane always hide after refresh browser
    * [CS-4819] - CS - IE7: Next Week icon is displayed wrong on Title bar
    * [CS-4820] - Delegated user cannot write mail
    * [CS-4822] - [PLF] Need to keep the space calendar of new created space as default selected when add event on calendar of space
    * [CS-4823] - Parsing datetime error at last week of March 2011 in week view
    * [CS-4826] - Repeating event is not shown in List view
    * [CS-4839] - Exception when show month view with event spreads over 2 months view
    * [CS-4845] - [Calendar] mini calendar and month view is wrong title day of week name
    * [CS-4846] - Job starts before loading some nodetypes
    * [CS-4847] - [MAIL] Print message : only the first page is printed
    * [CS-4852] - [Mail] need to enter password when user get mail for the account without check on "save password"
    * [CS-4853] - Mail service is unstably:Can not get mail and stop server.
    * [CS-4854] - "Refresh News" on RSS portlet is not translated in French
    * [CS-4858] - [CS-calendar] the deleted user still display on the share list of the shared calendar
    * [CS-4866] - [CS-mail] Don't show search icon when search user on To field on Chromium
    * [CS-4867] - [CS-mail] can not send mail when check return receipt
    * [CS-4869] - Offline messages are marked with current date
    * [CS-4870] - Untraslated labels in sharing calendar
    * [CS-4872] - Untranslated label in the search of agenda
    * [CS-4881] - [Calendar] reminder by e mail is display time incorrect
    * [CS-4888] - Chat - there is no user notification of the session timeout
    * [CS-4896] - Memory leaks in check mail service
    * [CS-4903] - [Calendar-event] Can not add new event by select add new event on right click 
    * [CS-4904] - [Calendar] Some event at the end day of the next month from romote calendare, don't display on month view of the month
    * [CS-4905] - [Calendar] Need show message when select Generate URL without select any calendar
    * [CS-4909] - RepositoryException when stopping tomcat PLF 3.5 M2
    * [CS-4911] - Show page not found in special case on CS
    * [CS-4917] - [CS-Agenda] don't keep the checked reminder by email and show notification popup
    * [CS-4918] - Can not add a contact with a long mail address in the address book
    * [CS-4935] - Allday repeating event doesn't show in week view
    * [CS-4938] - Event's from and to datetime in quick add event in dayview alway set to today
    * [CS-4943] - [CS] Show "Email address is invalid" send mail from contact app
    * [CS-4947] - Can not add daily repeated event that never end
    * [CS-4949] - CLONE - Wrong date availability in schedule tab in add/edit event form 
    * [CS-4957] - [social-integration] Added event is not listed in activities stream
    * [CS-4962] - [PLF] Unknown error when exporting all address books
    * [CS-4964] -  Repeat event UI error when add event 
    * [CS-4975] - SetCurrentIdentityFilter causes ChatBar to crash
    * [CS-4988] - CS - Mail - Exception when viewing attached file
    * [CS-4990] - Bad french traduction in portlet Address book in bloc Contact details
    * [CS-5004] - [CS] Javascript error shown when Selenium script is running more than 5 times (standalone only) 
    * [CS-5015] - [CS][Calendar] Hidden Calendar on left menu are checked automatically when righ clicking on an event/task to view
    * [CS-5016] - [CS][Calendar] Reminder popup isn't shown
    * [CS-5017] - Mail: Do not show form to select user for delegation
    * [CS-5018] - UI error with Repeating event form
    * [CS-5021] - [CS][Calendar] Reminder mail isn't sent after configuring Reminder setting for task/event
    * [CS-5027] - [IE7] Calendar - view Week - event card located in wrong time range
    * [CS-5032] - [CS][Calendar][Search] Can not search Task/Event with number (0,1,2,...) in Search box 
    * [CS-5034] - [CS][Calendar] Javascript error - mini context menu did not disappear after right clicking to Add/Edit/Export/Delete task or event
    * [CS-5037] - [CS][Chat] no invitation window shown after adding contact into Contact list
    * [CS-5038] - [CS] This portlet encountered an error and could not be displayed
    * [CS-5050] - [CS][Mail] Abnormal case caused Unknown Error - Exception occurred when trying to stop getting mail
    * [CS-5053] - CS - Mail - Filter {With Attachment} does not work
    * [CS-5065] - CLONE - In Calendar, the identifier is displayed in the list of participants of an event  instead of name, surname and mail
    * [CS-5081] - Error with Calendar list when configure My tasks gadget in Intranet Home page
    * [CS-5106] - Chat - exception when add all contact at once
    * [CS-5158] - [Calendar] Can not send remind email

** Improvement
    * [CS-2983] - should show warning message when create event with invitation mail but don't enter email address yet
    * [CS-4824] - Improve repeating event rule
    * [CS-4834] - only contacts selected on the current page are added.
    * [CS-4865] - CLONE - Do not display left panes of Calendar in spaces
    * [CS-4878] - Highlight today not unique enough in Mac then it's hard to know what is date today  
    * [CS-4961] - Upload file when select, no need to click to upload button 
    * [CS-5059] - Navigation API - new GateIn Implementation

** New Feature
    * [CS-756] - Subscribe to remote calendar
    * [CS-4370] - "Leave message on server" mail options
    * [CS-4860] - define a service that allowed all settings to be preconfigured by admins and possibly set as not modifiable by users
    * [CS-5026] - Show  event on agenda when tasks/events begin at a day and end at another day.  

- 2.2.0-Beta02

** Bug
    * [CS-4736] - [chatbar] Contact list is not shown after changing status to online
    * [CS-4737] - [chatbar] user at "Free to chat" can not do some permitted actions
    * [CS-4762] - Remote Calendar : Repeating events are not imported/synchronized correclty
    * [CS-4776] - [Rss] french translation for rss reader portlet
    * [CS-4781] - Calendar- Some UI error on IE 7
    * [CS-4789] - Contact - View a contact - link of home website is invalid
    * [CS-4805] - Invalid message display when delete a calendar
    * [CS-4807] - [PLF] [IE7] Intranet Home page - vertical scroll of Upcoming Events frame
    * [CS-4816] - [Calendar-IE7] Left pane always hide after refresh browser
    * [CS-4819] - CS - IE7: Next Week icon is displayed wrong on Title bar
    * [CS-4820] - Delegated user cannot write mail
    * [CS-4822] - [PLF] Need to keep the space calendar of new created space as default selected when add event on calendar of space
    * [CS-4839] - Exception when show month view with event spreads over 2 months view
    * [CS-4845] - [Calendar] mini calendar and month view is wrong title day of week name
    * [CS-4846] - Job starts before loading some nodetypes
    * [CS-4847] - [MAIL] Print message : only the first page is printed
    * [CS-4854] - "Refresh News" on RSS portlet is not translated in French
    * [CS-4869] - Offline messages are marked with current date
    * [CS-4870] - Untraslated labels in sharing calendar
    * [CS-4872] - Untranslated label in the search of agenda
    * [CS-4881] - [Calendar] reminder by e mail is display time incorrect
    * [CS-4903] - [Calendar-event] Can not add new event by select add new event on right click 
    * [CS-4918] - Can not add a contact with a long mail address in the address book

** Improvement
    * [CS-2983] - should show warning message when create event with invitation mail but don't enter email address yet
    * [CS-4824] - Improve repeating event rule
    * [CS-4865] - CLONE - Do not display left panes of Calendar in spaces

** New Feature
    * [CS-756] - Subscribe to remote calendar
    * [CS-4370] - "Leave message on server" mail options
    * [CS-4860] - define a service that allowed all settings to be preconfigured by admins and possibly set as not modifiable by users



- 2.2.0-Beta01

** Bug
    * [CS-3839] - Show 2 next room when click on previous/next room
    * [CS-3859] - Menu is not disappeared when select 'Offline' status
    * [CS-3862] - Error in the position of pop-up when click on user name
    * [CS-4166] - Unknown error when copy/paste or cut/paste 1 contact while user does not have edit right
    * [CS-4182] - Contact list is disappeared after right click
    * [CS-4213] - Do not show form to add new feed when click on Add button in Calendar Setting form
    * [CS-4239] - Mail-Number of unread messages is not displayed
    * [CS-4306] - Calendar "Quick add" bar UI fixes
    * [CS-4332] - exception when starting server 
    * [CS-4343] - Yahoo Mail can not receive the attachment from eXo Mail
    * [CS-4344] - Chat-UI broken when resize the chat pop up
    * [CS-4346] - [Contact]: Unknown error when add some user/group separated with the first one by some comma ","
    * [CS-4347] -  [Calendar] Event/Task is moved to out of working pane when move scroll bar of Browser
    * [CS-4350] - [Contact] Drag and drop contacts of a personal address book from specific tag to shared address book with edit right
    * [CS-4353] -  Can't Drag and drop contacts from personal address book to a specific tag in tags list
    * [CS-4355] - Can't Drag and drop contacts from personal address book to shared address book with edit right
    * [CS-4360] - UI broken when select user for [Task delegation]
    * [CS-4362] - [Calendar] Displaying time of event is incorrect when set Time interval is greater than 30 minutes
    * [CS-4363] -  [Mail] has problem when user uses 2 browsers to delete account mail 
    * [CS-4410] - Can not do some action on the public calendar
    * [CS-4425] - Mail-Imap- create a filter and apply do not work well
    * [CS-4436] - Can not run cs with csdemo 
    * [CS-4442] - initialize application data wrong when adding application to Social Space.
    * [CS-4450] - Tasks Gadget bug with max height
    * [CS-4458] -  Scrolling the weeks doesn't change the central view
    * [CS-4463] - [eXoMail] Write new mail UI is broken
    * [CS-4465] - event is not shown full when spread in 2 lines in month view.
    * [CS-4479] - Can't select file which is create in Site Explorer from server to attach in mail when compose new mail
    * [CS-4487] - [Portal] has exception when delete user
    * [CS-4488] - openfire deployment uses the hardcoded value eXo.env.dependenciesDir + "/repository" for the location of the local maven repository
    * [CS-4491] - Can not delete a mail after is moved into other folder
    * [CS-4494] - CLONE -CS not compatible with Java5
    * [CS-4514] - Error on sign in with IE8, IE7
    * [CS-4517] - [Calendar] Add Event form - calendar list is shown invalid
    * [CS-4519] - IE6 UI problems
    * [CS-4542] - [Contact] Invalid message display when user have edit permission move a shared contact
    * [CS-4545] - Can not check mail in specified folder after changing folder.
    * [CS-4546] - [CS] Still add even sucessfully when input invalid time
    * [CS-4555] - [CS] Register an account, text validation is not display
    * [CS-4559] - My task gadget is not properly showed in Chrome
    * [CS-4563] - My Tasks gadget setting screen does not show the button correctly
    * [CS-4566] - Calendar right panel does not show after login and can not show by change show hidden option 
    * [CS-4571] - wrong date time when right click on time table to add event or task
    * [CS-4572] - Show alert message error when use Switch view mode function with some portlet
    * [CS-4580] - Build and deploy CS failed with a clean local repository.
    * [CS-4584] - MyTasks gadget: incorrect url to calendar page
    * [CS-4589] - Error when reply to all on the new checked message with attachment
    * [CS-4590] - Can not create an event with attached PDF files
    * [CS-4597] - Chat- Room is broken on IE7 when F5 in platform
    * [CS-4617] - Show exception when add new task or even on calender with Reminder by email is checked
    * [CS-4619] - Mail - can not get mail
    * [CS-4624] - Show incorrect time when add new event/task
    * [CS-4629] - [Calendar] Dragging and dropping event in week view responses too slowly.
    * [CS-4632] - [Chrome] calendar , mail attachments are downloaded with wrong names
    * [CS-4634] - System files and directories must be hidden
    * [CS-4635] - Remaining dependency declaration
    * [CS-4654] - social integration: lost space avatar on activity stream.
    * [CS-4656] - Can not see attachment in the new arrival file
    * [CS-4666] - Problem with RSS feed in Calendar
    * [CS-4669] - Blank display when doing action in Mail
    * [CS-4689] - undefined field in Calendar RSS form
    * [CS-4693] - My tasks gadget does not show upcoming tasks 
    * [CS-4699] - Chat- error when sending file
    * [CS-4701] - [Smoke_CS_RHEL_02]  message when send mail successfully
    * [CS-4702] - [Smoke_CS_RHEL_04] Can not add contact on chat on IE7 because Dont show message to confirm when user accept invtation
    * [CS-4716] - IE 7 - Chat - right click menu on a contact display not well
    * [CS-4719] - RSS reader does not work because remove of xml-parser 
    * [CS-4750] - [CS-Mail] Unknown error and exception in console when check mail in special case
    * [CS-4751] - [CS-Mail] Can not delete a mail in tag
    * [CS-4754] - Double click to event, selected calendar alway default private
    * [CS-4759] - [CS]Show message when send email using Pop3
    * [CS-4763] - Remote calendar : change "Reload" by "Refresh" to refresh a remote calendar
    * [CS-4784] - Calendar's left panel always disappears after some actions
    * [CS-4785] - Can't redisplay a hidden calendar
    * [CS-4793] - IncompatibleClassChangeError when adding a calendar event in space
    * [CS-4799] - Event rollover does not work with attachment
    * [CS-4802] - Mail - No alert message in case delegate to invalid user name
    * [CS-4803] - Mail- Unknown error when add folder by delegated user without full permission
    * [CS-4804] - Mail - Delegated user with full permission can not tag, move a message
    * [CS-4811] - Repeating event does not show in month view
    * [CS-4825] - Delegated user can add folder successfully without Full permission

** Documentation
    * [CS-4622] - Update CS developer guide for default presence status
    * [CS-4726] - Update data structure changes from AIO to PLF

** Improvement
    * [CS-359] - Automatic event adding on event accept
    * [CS-2673] - safari: some parts of working pane should be not marked when resize event in Week view or Day view or Work week view
    * [CS-3541] - Default Group calendar names should be "Default"
    * [CS-3542] - use different default colors for default group calendars
    * [CS-3881] - Better default calendar settings
    * [CS-4217] - Always keep mm/dd/yyyy format in Schedule tab when create/edit event
    * [CS-4405] - show private url of iCalendar
    * [CS-4457] - Update Icon for attach file from DMS
    * [CS-4472] - Design new application registry icons for portlets
    * [CS-4595] - [Mail] check attachments in mail message without get content.
    * [CS-4612] - Mail: Cannot send/receive email with incoming/outgoing SSL protocol
    * [CS-4621] - Please automate the creation of the chatserver archive and attach it to the build like for tomcat.
    * [CS-4636] - We should highlight current week, month when click next/back week/monnth on time board.
    * [CS-4670] - Make Security Domain configurable
    * [CS-4671] - Update gadgets
    * [CS-4673] - http://localhost:8080 should point to the demo portal
    * [CS-4682] - Improve data structure to store RSS feed in Calendar
    * [CS-4692] - Hide "collaboration:/exo:applications" and directories 
    * [CS-4703] - [CHAT] history action button should be integrated


** New Feature
    * [CS-375] - Event details on rollover
    * [CS-1436] - Email account delegation
    * [CS-2449] - Last unread mails gadget
    * [CS-2450] - Tasks Gadget
    * [CS-3880] - Permissions on public addressbooks
    * [CS-4339] - Upcoming Events gadget
    * [CS-4685] - [Chat] User gets notification when their contact change status in chat


- 2.1.1 

** Bug
    * [CS-4094] - IE: Value in password field is not shown after ticking on check-box to download message or move to the back/next step 
    * [CS-4162] - Mail content is not valid in sepcial case
    * [CS-4452] - Mail: Can not do any action on mail sent from Gmail
    * [CS-4521] - Event or task time goes after 24h
    * [CS-4539] - Activity for new added event show incorrect end time
    * [CS-4540] - Unknown error when download attached file from Mail
    * [CS-4546] - [CS] Still add even sucessfully when input invalid time
    * [CS-4547] - [Calendar]Show exception when don't have events to export in this calendar
    * [CS-4548] - [Mail]Show exception when move message to folder
    * [CS-4549] - [Mail]Show exception click Sent mail
    * [CS-4550] - [Mail]Problem when delete message
    * [CS-4553] - [Mail] Show exception when add/edit filter
    * [CS-4555] - [CS] Register an account, text validation is not display
    * [CS-4563] - My Tasks gadget setting screen does not show the button correctly
    * [CS-4567] - Cluster configuration is wrong for Collaboration workspace
    * [CS-4571] - wrong date time when right click on time table to add event or task
    * [CS-4572] - Show alert message error when use Switch view mode function with some portlet
    * [CS-4590] - Can not create an event with attached PDF files


** Improvement
    * [CS-4476] - Do not display left panes of Calendar in spaces


- 2.1.0 GA

** Bug
    * [CS-1519] - Error when sort sender mail
    * [CS-3525] - [SAFARI] Fetching bar is always shown with  the same content : Start...stop
    * [CS-3710] - Bad input for folder name is silently ignored
    * [CS-3711] - Exception when add 1 tag (include special character) for an email
    * [CS-3944] - Exo mail could not connect to the exchange server 
    * [CS-4109] - Js message error after add application to a page
    * [CS-4162] - Mail content is not valid in sepcial case
    * [CS-4266] -  Synchronize folder is invalid in case edit folder in Gmail
    * [CS-4360] - UI broken when select user for [Task delegation]
    * [CS-4373] - Problem of refresh and unread message displaying
    * [CS-4390] - Chat portlet does not work on portal classic 
    * [CS-4394] - Chat - Unread message on chat tab is missed when refresh browser
    * [CS-4400] - Copy/Patse: There are no contacs message display invalidly
    * [CS-4402] - Message counld not be sent
    * [CS-4410] - Can not do some action on the public calendar
    * [CS-4418] - [Canlendar] User do not have edit permission still drag and drop event
    * [CS-4436] - Can not run cs with csdemo 
    * [CS-4442] - initialize application data wrong when adding application to Social Space.
    * [CS-4443] - chatbar user status is always "free to chat" event if user did not yet login 
    * [CS-4450] - Tasks Gadget bug with max height
    * [CS-4451] - Can not load fck editor 
    * [CS-4456] - User auto offline after refresh or login
    * [CS-4461] - SEVERE: Can not record Activity for space when contact updated 
    * [CS-4463] - [eXoMail] Write new mail UI is broken
    * [CS-4465] - event is not shown full when spread in 2 lines in month view.
    * [CS-4474] - Duplicate calendars for space groups
    * [CS-4479] - Can't select file which is create in Site Explorer from server to attach in mail when compose new mail
    * [CS-4480] - Should use default group calendar for space calendar 
    * [CS-4506] - [Chat] Char bar does not work
    * [CS-4516] - Chat works only for predefined users
    * [CS-4517] - [Calendar] Add Event form - calendar list is shown invalid

** Documentation
    * [CS-4503] - Reference Guide

** Improvement
    * [CS-4376] - Attach from DMS
    * [CS-4454] - MyTasks gadget : Hover on the fll title bar
    * [CS-4457] - Update Icon for attach file from DMS
    * [CS-4475] - preselect space calendar when add event/task
    * [CS-4476] - Do not display left panes of Calendar in spaces
    * [CS-4477] - More info about events in space activity stream
    * [CS-4499] - Social Integration : leverage activity plugin

** New Feature
    * [CS-1325] - Implement Timezone behaviour
    * [CS-2450] - Tasks Gadget
    * [CS-3338] - Save to DMS
    * [CS-3643] - Default presence status
    * [CS-3654] - Tags as personal lists
    * [CS-3990] - Share personal calendar to group
    * [CS-4338] - Access to Mail, Contacts and Calendar from ChatBar
    * [CS-4339] - Upcomming Events gadget
    * [CS-4388] - eXo Spaces integration for Calendar
    * [CS-4389] - eXo Spaces integration for AddressBook

** Task
    * [CS-4301] - merge CSCometd.js with Cometd.js from platform
    * [CS-4303] - duplicate  properties file in resource bundle  _en  and .properties
    * [CS-4371] - move all components declared in jars in extension
    * [CS-4377] - Upgrade to Gatein 3.1
    * [CS-4395] -  Add plug-in to initialize default data when space in social created
    * [CS-4427] - Update fisheye URL in pom.xml
    * [CS-4434] - update commons version 
    * [CS-4447] - Organize css following the introduction of gatein 3.1

- 2.1.0 CR03

** Bug
    * [CS-4462] - [eXoMail] Functional exception management is missing.
    * [CS-4483] - Warning Unable to coerce 'Event' into a LONG
    * [CS-4492] -  Chat Conservation form UI is broken in IE 7
    * [CS-4493] - User using IE7 to login can not send file to another user
    * [CS-4502] - remove "Powered By eXo Platform" in chat bar

** Improvement
    * [CS-4482] - Revamp UI composer from in mail app when the header button too long  

** New Feature
    * [CS-4338] - Access to Mail, Contacts and Calendar from ChatBar

** Task
    * [CS-4511] - Package web service in EAR extension
    * [CS-4512] - Remove extension config jar from the ear
    * [CS-4513] - Remove jboss-web.xml from extension war
    * [CS-4520] - Release CS 2.1.0-CR03


- 2.1.0 CR02

** Bug
    * [CS-4360] - UI broken when select user for [Task delegation]
    * [CS-4461] - SEVERE: Can not record Activity for space when contact updated 
    * [CS-4465] - event is not shown full when spread in 2 lines in month view.
    * [CS-4488] - openfire deployment uses the hardcoded value eXo.env.dependenciesDir + "/repository" for the location of the local maven repository
    
** Task
    * [CS-4371] - move all components declared in jars in extension
    * [CS-4447] - Organize css following the introduction of gatein 3.1
    * [CS-4505] - Release CS 2.1.0-CR02

- 2.1.0 CR01

** Bug
    * [CS-4410] - Can not do some action on the public calendar
    * [CS-4436] - Can not run cs with csdemo 
    * [CS-4442] - initialize application data wrong when adding application to Social Space.
    * [CS-4450] - Tasks Gadget bug with max height
    * [CS-4451] - Can not load fck editor 
    * [CS-4458] -  Scrolling the weeks doesn't change the central view
    * [CS-4463] - [eXoMail] Write new mail UI is broken
    * [CS-4486] - German translation instead of french for Calendar

** Improvement
    * [CS-4457] - Update Icon for attach file from DMS

** New Feature
    * [CS-2450] - Tasks Gadget

** Task
    * [CS-4395] -  Add plugin to initialize default data when space in social created
    * [CS-4469] - base structure for eXo Collaboration Reference Guide
    * [CS-4485] - Release CS 2.1.0-CR01

- 2.0.0 GA

** Bug
    * [CS-4043] - Attachment file is not shown when view messages in Sent folder
    * [CS-4244] - Calendar - Show error message when all event categories are deteted
    * [CS-4252] - Time field is not shown in some cases
    * [CS-4260] - Problem in mail without remember password
    * [CS-4264] - Don't update fetched message in Message pane while checking mail
    * [CS-4265] - Imap- Can't check mail any more after stopped check mail
    * [CS-4266] -  Synchronize folder is invalid in case edit folder in Gmail
    * [CS-4308] - Calendar - Error message is showed when all Event categories are deleted
    * [CS-4309] - [MAIL] - Add contact form does not close by click to close button 
    * [CS-4313] - Chat: Chat app is blocked with new created user
    * [CS-4315] -  [Contact] Unknown error when click one tag of share contact
    * [CS-4316] - [Contact] Can't Drag and drop contacts from a shared address book without edit right to personal address book
    * [CS-4327] - Can't Maximize reading pane
    * [CS-4330] - problem when select event category of shared events.
    * [CS-4332] - exception when starting server 
    * [CS-4333] - Cannot get mail by pop3 account
** Feedback
    * [CS-4218] - Contact auto-complete blocks input on slow server
** Task
    * [CS-4297] - ChangeLanguage link on csdemo is not nice
    * [CS-4314] - Do not delete events when delete event category contains these events.

- 2.0.0 CR02


** Bug
    * [CS-2975] - Calendar RSS feed does not generate links for more than one calendar
    * [CS-3559] - Chat bar can not be displayed in Vista & Mac Skin
    * [CS-3725] - Webmail: Date format error (wrong pattern used)
    * [CS-3729] - [DEV] Can not load FCKEditor when send an email 
    * [CS-3823] - Display name of created event when open form to create new event
    * [CS-4102] - Can not see default navigation on cs extension in tomcat with some pc
    * [CS-4165] - Calendar: parameter "first day of the week" not taken into account
    * [CS-4170] - Home page of eXo Mail is only in English
    * [CS-4180] - chat on jboss does not work
    * [CS-4181] - Error when print contact with extension
    * [CS-4211] - Error in displaying right-click menu (in Extension)
    * [CS-4225] -  CSdemo: Return no message when try to view sent messages by Today | This week | Last 30 days
    * [CS-4226] - Reschedule event with mouse on "Schedule" tab does not work
    * [CS-4241] - IE7: ICAL icon is not shown when Enable public access calendar
    * [CS-4247] - Event - There is no Confirmation Link in Invatation email
    * [CS-4252] - Time field is not shown in some cases
    * [CS-4253] - Check mail inexactly when limit time to check 
    * [CS-4259] - Exception when send mail without enable SMTP authentication
    * [CS-4263] - Alert message is not shown to inform user that invalid username/password
    * [CS-4264] - Don't update fetched message in Message pane while checking mail
    * [CS-4269] - Calendar: some typo in French
    * [CS-4270] - Chat - the user's id is displayed instead of the user name
    * [CS-4271] - Adressbook refreshes to empty state after mouse move
    * [CS-4280] - Auto apply server locale to UI event user not setting 
    * [CS-4291] - Only eXo Chat IM is displayed
    * [CS-4294] - Chat room loss when refreshing the browser page or even changing menu
    * [CS-4299] - Remove the use of exoservice gmail Account from MailService configuration

** Feedback
    * [CS-4164] - csdemo and cs-extention doesn't use the same workspace
    * [CS-4272] - demo homepage illustration is UGLY
    * [CS-4273] - quicksearch should run on hit ENTER

** Improvement
    * [CS-2762] - Limit size of .ics files generated for feeds
    * [CS-3003] - Allow Whitespace in chat room name
    * [CS-3276] - Safari 4 support
    * [CS-3333] - Share a temporary selection of contacts
    * [CS-3528] - Perform XMPP authentication based on current user credentials
    * [CS-3544] - Better support for move messages in IMAP
    * [CS-3682] - highlight links in chat room
    * [CS-3835] - More concise contact sheet
    * [CS-3882] - Do not display empty group of calendars
    * [CS-3885] - Calendar UI glitches
    * [CS-3887] - Mail ui glitches
    * [CS-3945] - Configurable exclusions for group adressbooks
    * [CS-4008] - Public feed have add more function 
    * [CS-4298] - Preserve Chat bar state when browsing

** New Feature
    * [CS-1316] - Send mail to group of users
    * [CS-2813] - Messages Sync
    * [CS-3655] - Select All in AddressBook
    * [CS-3693] - public contacts attributes based on portal's user profile
    * [CS-3908] - Autocomplete in To: CC: and BCC contact app: 
 

- 2.0.0 CR01

** Bug
    * [CS-3817] - "Time" field is not disabled when check "All day" while creating Event/Task
    * [CS-4045] - Mistakes in french internationalization in Chat popups
    * [CS-4060] - JBoss cannot start
    * [CS-4161] - vertical scrollbar
    * [CS-4170] - Home page of eXo Mail is only in English
    * [CS-4174] - Agenda: it lacks the French translations 
    * [CS-4175] - unknown error when create feed navigation on rss reader porlet
    * [CS-4178] -  Mails : Cannot add a folder 
    * [CS-4183] - Address book : missing french traduction in contacts
    * [CS-4193] - Calendar: the "date format" (jj/mm/aaaa) setting is only partially taken into account

** Improvement
    * [CS-3834] - Distinctive icon for shared contacts
    * [CS-3955] - Create user profiles lazily
   
** Task
    * [CS-3634] - Apply gatein migration prerequisites
    * [CS-4096] - move components declaration to src/main/resources

- 2.0.0 Beta02
** Bug
    * [CS-3561] - Missing upload icon in import calendar form
    * [CS-3706] - Can not quick add event after setting time is AM/PM
    * [CS-3732] - Drag and drop of a contact with no edit permission produces an unknown error
    * [CS-3784] - Can not open sub-folder has sub-folder
    * [CS-3833] - Unknown error when delete permission of share address book/contact
    * [CS-3851] - ChatBar does'nt work with Internet Explorer 7
    * [CS-3854] - Unknown error when delete permission of share calendar
    * [CS-3855] - Can not add multi users at the same time for shared calendar
    * [CS-3857] - [Unplanned] AddressBook Portlet is using OrganizationService instead of ContactService for Importing Groups
    * [CS-3865] - password prompted even after save
    * [CS-3868] - Can not send mail for user has multi email
    * [CS-3874] - Can not move some message at the same time
    * [CS-3924] - Always show message in Inforbar 
    * [CS-3975] - Can not add IM Contact when creating new contact
    * [CS-3976] - Can not send multi email at the same time
    * [CS-3977] - Exception when share contact for owner
    * [CS-3981] - Can not delete or move the sent messages in Sent folder to others
    * [CS-3982] - User is not prompted for password
    * [CS-3985] - Messages are disappeared after editing filter without changing
    * [CS-3991] - Unknown error when edit 1 event in List view
    * [CS-3994] - Unknown error when double click on event when see it in List view
    * [CS-3996] - Exception when importe 1 message at the second time
    * [CS-3999] - Unknown error when edit  share permission of contact
    * [CS-4014] - Contact's image is not show
    * [CS-4018] - Error in displaying buttons in Add Tag form
    * [CS-4021] - Can not get mail with csdemo
    * [CS-4026] - Hiding calendars with duplicate named does not work
    * [CS-4028] - Can not add group user to send remind when create an event in Mail application
    * [CS-4031] - [unplanned] Can not view image attachment  of event/task with csdemo
    * [CS-4037] - Can not get mail without select folder
    * [CS-4038] - Right click menu still display event when action is done
    * [CS-4039] - UI error with forms has 2 tabs or more (in csdemo)
    * [CS-4040] - Error message pop-up (with csdemo)
    * [CS-4060] - JBoss cannot start
    * [CS-4070] - The first check-box is not checked when open form to add tag, send mail...
    * [CS-4084] - Unknown error when add group has edit permission for group calendar
    * [CS-4089] - IE7: Error in upload form 
    * [CS-4095] - IE7: Show  error message when drag and drop event/task in Month view
    * [CS-4108] - The check-box is not unchecked 
    * [CS-4124] - Can not get mail using pop3
    * [CS-4127] - [Unplanned] French accentuated characters are corrupted
    * [CS-4133] - Exception when a user is deleted
    * [CS-4168] -  Problem in RSS Portlet Refresh

** Improvement
    * [CS-3276] - Safari 4 support
    * [CS-3883] - Improve RSS feeds content
    * [CS-3945] - Configurable exclusions for group adressbooks

** New Feature
    * [CS-842] - Revamp Feeds management
    * [CS-3655] - Select All in AddressBook
    * [CS-3888] - Public iCal URL for calendars
    * [CS-3889] - Multi-calendar RSS Feeds
    * [CS-3906] - revamp demo portal 

    
- 2.0.0 Beta01

** Bug
    * [CS-3772] - Do not display tag's name in [Tag Message] form
    * [CS-3783] - Little error when folder's name is long
    * [CS-3790] - Still show message's content when an email is removed from tag
    * [CS-3791] - Unknown error when select user to send reminder while creating 1 event in Mail application
    * [CS-3849] - Can not join un-public room when user put right password 
    * [CS-3907] - Impossible to send event invitation
    * [CS-3913] - Chat does not work :   "Keystore was tampered" 
    * [CS-3953] -  Fix for labels i18n
    * [CS-3989] - Users can chat to each other but can not send file when user cancel add contact invitation
    * [CS-4010] - Little error in Contact form when email address is long

** Documentation
    * [CS-3788] - Update documentation auto-complete in To: CC: and BCC:

** Improvement
    * [CS-3673] - Room of administrative messages should be assigned a name 
    * [CS-3682] - highlight links in chat room
    * [CS-3835] - More concise contact sheet
    * [CS-3882] - Do not display empty group of calendars
    * [CS-3937] - Chat server packaging improvements

** New Feature
    * [CS-1316] - Send mail to group of users
    * [CS-3888] - Public iCal URL for calendars
    * [CS-3908] - Auto-complete in To: CC: and BCC contact app

** Task
    * [CS-2731] - Optimize calendar queries
    * [CS-3700] - Replace text "Remove" by icon in forms to add/edit event or task
    * [CS-3797] - Release CS 2.0 - Alpha2
    * [CS-3884] - Using "HashSet<String>" instead of "HashMap<String, String>", if  values==keys
    * [CS-3917] - Rename eXoLiveroom.sh
    * [CS-3918] - "eXo undefined" when starting with gatein.sh
    * [CS-3927] - Remove scroll bar in [Change image] form
    * [CS-3948] - rename the folder inside eXoChatServer.2.0.0-XXX.zip
    * [CS-3951] - Update the build to deploy binary bundles on release
    * [CS-3952] - chat peristence in erroneous ppath
    * [CS-3959] - Upgrade to GateIn GA


- 2.0.0 Alpha02


** Bug
    * [CS-3539] - lost last message when show message from "today" in special case
    * [CS-3606] - Chat room loss when refreshing the browser page or even changing menu
    * [CS-3708] - Some mails do not display message's content
    * [CS-3720] - ContinuationService getUserToken NPE exception when run both csdemo and extension
    * [CS-3768] - Can not save event/task in special case 
    * [CS-3769] - Can not send remind email for more than 1 user
    * [CS-3775] - Unknown error when send an email with attachment
    * [CS-3776] - Do not show event/task of imported calendar
    * [CS-3782] - csdemo and rest-csdemo webapps are not deployed
    * [CS-3785] - Can not move message when it is not original message
    * [CS-3789] - After reading 1 imported email, the number unread email of folder is not discounted
    * [CS-3821] - Can not get mail from pop3
    * [CS-3837] - Set align for attributes of UserProfile  
    * [CS-3838] - Show popup message when create or delete account
    * [CS-3877] - NPE on PopupReminder at startup
    * [CS-3907] - Impossible to send event invitation
    * [CS-3913] - Cannot make Chat work
    * [CS-3920] - JBoss patch doesn't contain the specific server.xml with SSO



** Improvement
    * [CS-3333] - Share a temporary selection of contacts
    * [CS-3528] - Perform XMPP authentication based on current user credentials
    * [CS-3544] - Better support for move messages in IMAP
    * [CS-3771] - remove wasted space
    * [CS-3825] - Put a cleaner message in Chat application when the chat server is not available / remove stack trace
    * [CS-3921] - Auto-complete should provide easy way to select email when user has multiple email

** New Feature
    * [CS-382] - Autocomplete in To: CC: and BCC:
    * [CS-3693] - public contacts attributes based on portal's user profile

** Task
    * [CS-1138] - Re-factoring : JCRDataStorage does not implement DataStorage
    * [CS-2429] - add xsd to the xml configurations
    * [CS-3422] - Migrate webservices to WS 2
    * [CS-3427] - Upgrade openfire dependency
    * [CS-3770] - Enable csdemo portal
    * [CS-3794] - Find a way to configure 2 openfire
    * [CS-3795] - Cleanup dependencies
    * [CS-3796] - Upgrade to gatein beta5
    * [CS-3858] - Chat service don't support multi-portal mode
    * [CS-3866] - Edit the background in [Delete account] form
    * [CS-3867] - Edit the background in [Attach] form while creating event or task

 

- 2.0.0 Alpha01


** Bug
    * [CS-3473] - after report messages are spam, can't do these messages are not spam 
    * [CS-3560] - should disable chat room when  user offline 
    * [CS-3672] - Don't show warning message when user create new room without permission 
    * [CS-3674] - Calendar porlet loss action bar button when run on AIL project
    * [CS-3676] - WebOS, IE7, Chat: Can not send file 
    * [CS-3686] - AddressBook Picker gets all addresses
    * [CS-3688] - All-in-one :Mail:  Message pane: invalid display
    * [CS-3719] - java script loading order error
    * [CS-3749] - Event/Task is automatic returned to the created date/time after drag and drop
    * [CS-3750] - Unknown error when generate RSS
    * [CS-3753] - Error in Mail pop-up

** Improvement
    * [CS-3328] - Make categories translatable
    * [CS-3528] - Chat Login : Get password from DB or LDAP

** Task
    * [CS-1991] - Remove hard coded references to /portal from web\csportal\src\main\webapp\templates\home.gtmpl
    * [CS-1992] - Remove hard coded references to /portal from web\csportal\src\main\webapp\templates\sidebar.gtmpl
    * [CS-2271] - Deprecate shared UI components
    * [CS-2272] - Remove deprecated components
    * [CS-2389] - Guard permission management in service
    * [CS-2616] - Check compatible when kernel clean up configuration.xml
    * [CS-2649] - Update Validator class from Portal to replace static Utils check function
    * [CS-3173] - Cometd study and update for mail service
    * [CS-3635] - Create source code subtree
    * [CS-3636] - Split csportal between extension and demo
    * [CS-3666] - Remove <testFailureIgnore>false</testFailureIgnore> from parent pom of CS projects
    * [CS-3717] - upgrade to Gatein beta4
    * [CS-3718] - Incorrect inheritance in pkg/pom.xml


** Sub-task
    * [CS-2454] - [DEV] occur some fails when check an email with big attachment
    * [CS-3382] - DEV:  if add star for message can't resize Detail message pane when view in Vertical layout
    * [CS-3456] - DEV: IMAP folders containing '/' not handled properly
    * [CS-3752] - [DEV] Chat: failure to send file 
    * [CS-3760] - [DEV] should disable chat room when user offline
    * [CS-3762] - [DEV]  Don't show warning message when user create new room without permission 




