/**
 *
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.awt.event.ActionEvent;
import org.owasp.webscarab.ui.rcp.*;
import java.awt.Dimension;
import java.net.URI;

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

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.plugins.spider.Spider;

/**
 * @author lpz
 *
 */
public class SpiderView extends AbstractView implements SpiderPopupAdapter{
    //

    private String SPIDER_TOGGLE_ON = "ON";
    private String SPIDER_TOGGLE_OFF = "OFF";
    private String SPIDER_FETCH = "Spider";
    private String SPIDER_OPTIONS = "Configure";
    private EventService eventService;
    private Listener listener = new Listener();
    private ButtonListener btnListener = new ButtonListener();
    private SpiderListener spiderViewListener = new SpiderListener();
    private JTree uriTree;
    private UriTreeModel uriTreeModel;
    private Spider spider;
    JToggleButton tbtn = getComponentFactory().createToggleButton(SPIDER_TOGGLE_ON);
    JButton fetchBtn = getComponentFactory().createButton(SPIDER_FETCH);
    JButton spiderOptBtn = getComponentFactory().createButton(SPIDER_OPTIONS);
    URI[] selectedURIs = null;
    String[] selectedMethods = null;
    private SpiderContextMenu spiderPopup;
    private SpiderPopupListener popuplistener;

    public SpiderView() {
        uriTreeModel = new UriTreeModel();
        tbtn.setIcon(getIconSource().getIcon("spider.small"));
        tbtn.setEnabled(true);
        fetchBtn.setEnabled(false);
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
        
        JPanel main = getComponentFactory().createPanel();
        BorderLayout sl = new BorderLayout();
        JScrollPane scrollPane = getComponentFactory().createScrollPane(uriTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel buttons = getComponentFactory().createPanel();
        main.setLayout(sl);

        tbtn.setSelected(true);

        tbtn.addActionListener(btnListener);
        fetchBtn.addActionListener(btnListener);
        spiderOptBtn.addActionListener(btnListener);
        buttons.add(tbtn);
        buttons.add(fetchBtn);
        buttons.add(spiderOptBtn);

        scrollPane.setMinimumSize(new Dimension(200, 30));
        buttons.setMinimumSize(new Dimension(200, 60));

        main.add(buttons, BorderLayout.PAGE_START);
        main.add(scrollPane, BorderLayout.CENTER);
        return main;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        if (eventService != null) {
            eventService.subscribeStrongly(URIFoundEvent.class, this.spiderViewListener);
            eventService.subscribeStrongly(SessionEvent.class, this.spiderViewListener);
        }
    }

    public URI[] getSelectedURIs() {
        return selectedURIs;
    }

    public String[] getSelectedMethods() {
        return selectedMethods;
    }

    public SpiderContextMenu getSpiderContextMenu() {
        return spiderPopup;
    }

    private class ButtonListener implements ActionListener {

        /*
         * Listener for all buttons on spider view
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            //toggle spider button
            if (source == tbtn) {
                boolean btnVal = tbtn.isSelected();
                if (btnVal) {
                    tbtn.setText(SPIDER_TOGGLE_ON);
                } else {
                    tbtn.setText(SPIDER_TOGGLE_OFF);
                }
                spider.setEnabled(btnVal);
            } else if (source == fetchBtn) {
                eventService.publish(new SpiderStartEvent(this, getSelectedURIs(), getSelectedMethods(), new Date()));
            } else if (source == spiderOptBtn) {
                SpiderConfigDialog dialog = new SpiderConfigDialog(spider, SpiderView.this.getApplicationContext());
                dialog.showDialog();
            }
        }
    }

    private class Listener implements ListEventListener<Conversation>,
            TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            TreePath[] paths = uriTree.getSelectionPaths();
            URISelectionEvent use = null;
            if (paths == null || paths.length == 0) {
                use = new URISelectionEvent(SpiderView.this, new URI[0]);
                fetchBtn.setEnabled(false);
            } else {
                URI[] sel = new URI[paths.length];
                String[] selm = new String[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    sel[i] = (URI) paths[i].getLastPathComponent();
                    selm[i] = "GET";
                }
                use = new URISelectionEvent(SpiderView.this, sel);
                selectedURIs = sel;
                selectedMethods = selm;
                fetchBtn.setEnabled(true);
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
                /*int index = evt.getIndex();
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
                }*/
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

    private class SpiderListener implements EventSubscriber {

        public void onEvent(EventServiceEvent ese) {
            if ((ese instanceof URIFoundEvent)) {
                URIFoundEvent cse = (URIFoundEvent) ese;
                URI uri = cse.getURI();
                if (spider.addURI(uri)) {
                    uriTreeModel.add(uri);
                }
            } else if (ese instanceof SessionEvent) {
                //populating uris
                SpiderView.this.spider = (Spider)getApplicationContext().getBean("spider");
                for (URI u : spider.getURIs()) {
                    uriTreeModel.add(u);
                }
            }

        }
    }
   
}
