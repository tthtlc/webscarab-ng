/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.owasp.webscarab.plugins.request.swing;

import org.owasp.webscarab.domain.Conversation;

/**
 *
 * @author Lpz
 */
public interface RequestPopupAdapter {
    RequestContextMenu getRequestContextMenu();
    Conversation getConversation();
    boolean hasSelectedConversations();
}
