/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author Lpz
 */
public class SpiderPopupListener extends MouseAdapter {
    SpiderPopupAdapter spa;
    public SpiderPopupListener(SpiderPopupAdapter spa) {
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
        if (e.isPopupTrigger()) {
            spa.getSpiderContextMenu().setSelectedUris(spa.getSelectedURIs());
            spa.getSpiderContextMenu().setSelectedMethods(spa.getSelectedMethods());
            spa.getSpiderContextMenu().show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
}
