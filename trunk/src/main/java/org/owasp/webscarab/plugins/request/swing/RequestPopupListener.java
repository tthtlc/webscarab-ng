/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author Lpz
 */
public class RequestPopupListener extends MouseAdapter {
    RequestPopupAdapter spa;
    public RequestPopupListener(RequestPopupAdapter spa) {
        this.spa = spa;
    }
    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger() && spa.hasSelectedConversations()) {
            spa.getRequestContextMenu().setConversation(spa.getConversation());
            spa.getRequestContextMenu().show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
}
