/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.bushe.swing.event.EventService;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.util.swing.UriTreeModel;
import org.owasp.webscarab.util.swing.renderers.UriRenderer;
import org.springframework.richclient.application.support.AbstractView;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.owasp.webscarab.plugins.spider.swing.SpiderContextMenu;
import org.owasp.webscarab.plugins.spider.swing.SpiderPopupAdapter;
import org.owasp.webscarab.plugins.spider.swing.SpiderPopupListener;

/**
 * @author rdawes
 *
 */
public class SiteMapView extends AbstractView implements SpiderPopupAdapter {

    private ArrayList<URI> uris;
    private URI[] selectedUris = new URI[0];
    private String[] selectedMethods = new String[0];
    private FilterList<Conversation> conversationList;
    private EventService eventService;
    private Listener listener = new Listener();
    private SpiderPopupListener popuplistener;
    private JTree uriTree;
    private UriTreeModel uriTreeModel;
    private SpiderContextMenu spiderPopup;

    public SiteMapView() {
        uris = new ArrayList<URI>();
        uriTreeModel = new UriTreeModel();
         popuplistener = new SpiderPopupListener(this);
    }

    public void setSpiderPopup(SpiderContextMenu popup) {
        this.spiderPopup = popup;
        this.spiderPopup.setEventService(this.eventService);
    }
    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#createControl()
     */

    @Override
    protected JComponent createControl() {
        uriTree = new JTree(uriTreeModel);
        uriTree.setRootVisible(false);
        uriTree.setShowsRootHandles(true);
        uriTree.setCellRenderer(new UriRenderer());
        uriTree.addTreeSelectionListener(listener);
        uriTree.addMouseListener(popuplistener);
        JScrollPane scrollPane = getComponentFactory().createScrollPane(
                uriTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(200, 30));
        return scrollPane;
    }

    /**
     * @param conversationList
     *            the conversationList to set
     */
    public void setConversationList(EventList<Conversation> conversationList) {
        if (this.conversationList != null) {
            this.conversationList.removeListEventListener(listener);
            this.conversationList.dispose();
        }
        if (conversationList != null) {
            this.conversationList = new FilterList<Conversation>(
                    conversationList, new SuccessfulConversationMatcherEditor());
            populateExisting();
            this.conversationList.addListEventListener(listener);
        } else {
            this.conversationList = null;
        }
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private void populateExisting() {

        conversationList.getReadWriteLock().readLock().lock();
        uriTreeModel.clear();
        uris.clear();
        Iterator<Conversation> it = conversationList.iterator();
        while (it.hasNext()) {
            URI uri = it.next().getRequestUri();
            uris.add(uri);
            uriTreeModel.add(uri);
        }
        conversationList.getReadWriteLock().readLock().unlock();

    }

    public URI[] getURIs() {
        return this.selectedUris;
    }

    public String[] getSelectedMethods() {
        return selectedMethods;
    }

    public URI[] getSelectedURIs() {
        return this.selectedUris;
    }

    public SpiderContextMenu getSpiderContextMenu() {
        return spiderPopup;
    }

    private class Listener implements ListEventListener<Conversation>,
            TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            TreePath[] paths = uriTree.getSelectionPaths();
            SiteMapView.this.selectedUris = new URI[0];
            URISelectionEvent use = null;
            if (paths == null || paths.length == 0) {
                use = new URISelectionEvent(SiteMapView.this, new URI[0]);
            } else {
                URI[] sel = new URI[paths.length];
                String[] selm = new String[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    sel[i] = (URI) paths[i].getLastPathComponent();
                    selm[i] = "GET";
                }
                use = new URISelectionEvent(SiteMapView.this, sel);
                SiteMapView.this.selectedUris = sel;
                SiteMapView.this.selectedMethods = selm;

            }
            eventService.publish(use);
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
         */
        public void listChanged(ListEvent<Conversation> evt) {
            while (evt.next()) {
                int index = evt.getIndex();
                if (evt.getType() == ListEvent.DELETE) {
                    URI uri = uris.remove(index);
                    if (!uris.contains(uri)) {
                        uriTreeModel.remove(uri);
                    }
                } else if (evt.getType() == ListEvent.INSERT) {
                    URI uri = conversationList.get(index).getRequestUri();
                    if (!uris.contains(uri)) {
                        uriTreeModel.add(uri);
                    }
                    uris.add(index, uri);
                }
            }
        }
    }

    private class SuccessfulConversationMatcherEditor extends AbstractMatcherEditor<Conversation> {

        private Matcher<Conversation> matcher;

        public SuccessfulConversationMatcherEditor() {
            matcher = new Matcher<Conversation>() {

                public boolean matches(Conversation conversation) {
                    String status = conversation.getResponseStatus();
                    switch (status.charAt(0)) {
                        case '2':
                            return true;
                        case '3':
                            return true;
                        default:
                            return false;
                    }
                }
            };
        }

        public Matcher<Conversation> getMatcher() {
            return matcher;
        }
    }
}
