/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.bushe.swing.event.EventService;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.plugins.request.RequestCopyEvent;
import org.springframework.richclient.application.PageComponentContext;

/**
 *
 * @author Lpz
 */
public class RequestContextMenu extends JPopupMenu {

    private EventService eventService;
    private Conversation conversation = null;
    private PageComponentContext pageComponentContext = null;

    public RequestContextMenu() {
        JMenuItem request = new JMenuItem("To Request Maker...");//TODO resources
        request.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                RequestContextMenu.this.pageComponentContext.getPage().showView("requestMakerView");
                eventService.publish(new RequestCopyEvent(this, conversation));
            }
        });
        this.add(request);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setPageComponentContext(PageComponentContext pageComponentContext) {
        this.pageComponentContext = pageComponentContext;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
