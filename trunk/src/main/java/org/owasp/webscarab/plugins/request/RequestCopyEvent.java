/**
 *
 */
package org.owasp.webscarab.plugins.request;

import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Conversation;

/**
 * @author rdawes
 *
 */
public class RequestCopyEvent implements EventServiceEvent {

    private Object source;

    private Conversation conversation;

    public RequestCopyEvent(Object source, Conversation conversation) {
        this.source = source;
        this.conversation = conversation;
    }

    /**
     * @return the conversation
     */
    public Conversation getConversation() {
        return this.conversation;
    }

    /**
     * @return the source
     */
    public Object getSource() {
        return this.source;
    }
}
