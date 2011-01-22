/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Date;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.bushe.swing.event.EventService;

/**
 *
 * @author Lpz
 */
public class SpiderContextMenu extends JPopupMenu {

    private EventService eventService;
    private URI[] selectedUris = new URI[0];
    private String[] selectedMethods = new String[0];

    public SpiderContextMenu() {
        JMenuItem fetch = new JMenuItem("Spider");
        JMenuItem fetchRec = new JMenuItem("Spider recursively");

        fetch.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                eventService.publish(new SpiderStartEvent(this, selectedUris, selectedMethods, null));
            }
        });
        fetchRec.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JMenuItem source = (JMenuItem) event.getSource();
                eventService.publish(new SpiderStartEvent(this, selectedUris, selectedMethods, new Date()));
            }
        });
        this.add(fetch);
        this.add(fetchRec);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setSelectedMethods(String[] methods) {
        this.selectedMethods = methods;
    }

    public void setSelectedUris(URI[] selectedUris) {
        this.selectedUris = selectedUris;
    }
}
