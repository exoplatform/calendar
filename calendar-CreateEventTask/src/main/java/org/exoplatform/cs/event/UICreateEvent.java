package org.exoplatform.cs.event;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.*;
import org.exoplatform.webui.form.validator.MandatoryValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Racha
 * Date: 01/11/12
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
@ComponentConfig(
        lifecycle = UIFormLifecycle.class,
        template = "classpath:groovy/webui/create/UICreateEvent.gtmpl",
        events = {
                @EventConfig(
                        listeners = UICreateEvent.NextActionListener.class,
                        phase = org.exoplatform.webui.event.Event.Phase.DECODE
                ),
                @EventConfig(
                        listeners = UICreateEvent.CancelActionListener.class,
                        phase = org.exoplatform.webui.event.Event.Phase.DECODE
                )
        }
)

public class UICreateEvent extends UIForm {

    static List<SelectItemOption<String>> options = new ArrayList();
    private static Log log = ExoLogger.getLogger(UICreateEvent.class);
    static String CHOIX = "Choix".intern();

    static String TITLE = "Title".intern();

    static String END_EVENT = "EndEvent".intern();
    static String CALENDAR = "Calendar".intern();
    static String Start_EVENT = "StartEvent".intern();

    public UICreateEvent()
            throws Exception {


        addUIFormInput(new UIFormRadioBoxInput(CHOIX, CHOIX, options));
        addUIFormInput(new UIFormStringInput(TITLE, TITLE, null).addValidator(MandatoryValidator.class, new Object[0]));
        addUIFormInput(new UIFormDateTimeInput(Start_EVENT, Start_EVENT, null, false).addValidator(MandatoryValidator.class, new Object[0]));
        addUIFormInput(new UIFormDateTimeInput(END_EVENT, END_EVENT, null, false).addValidator(MandatoryValidator.class, new Object[0]));
        addUIFormInput(new UIFormSelectBox(CALENDAR, CALENDAR, options));


    }

    public String[] getActions() {
        return new String[]{"Next", "Cancel"};
    }


    static public class NextActionListener extends EventListener<UICreateEvent> {


        public void execute(Event<UICreateEvent> event)
                throws Exception {

            log.info("#################### Next Action was triggered");


        }
    }


    static public class CancelActionListener extends EventListener<UICreateEvent> {


        public void execute(Event<UICreateEvent> event)
                throws Exception {

            log.info("#################### Cancel  Action was triggered");


        }
    }


    static {
        options.add(new SelectItemOption("Event"));
        options.add(new SelectItemOption("Task"));
    }


}
