/**
 *
 */
package org.owasp.webscarab.plugins.request.swing;

import java.awt.event.ActionEvent;
import java.net.URISyntaxException;
import org.owasp.webscarab.plugins.request.RequestCopyEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.jdesktop.swingx.JXButton;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.FuzzingVariable;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.plugins.request.FuzzingModel;
import org.owasp.webscarab.plugins.request.FuzzingTree;
import org.owasp.webscarab.services.ConversationGenerator;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.services.HttpService;
import org.owasp.webscarab.ui.rcp.SwingEventSubscriber;
import org.owasp.webscarab.ui.rcp.forms.AnnotationForm;
import org.owasp.webscarab.ui.rcp.forms.RequestForm;
import org.owasp.webscarab.ui.rcp.forms.ResponseForm;
import org.owasp.webscarab.ui.rcp.forms.support.ConversationFormSupport;
import org.owasp.webscarab.util.TransformRequestCommand;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.dialog.support.DialogPageUtils;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class RequestMakerView extends AbstractView {

    Logger log = Logger.getLogger(this.getClass().getName());
    private FormModel conversationFormModel;
    private FormModel annotationFormModel;
    private EventService eventService;
    private HttpService httpService;
    private Session session;
    private Fetcher activeFetcher;
    private Listener listener;
    private ConversationService conversationService;
    private RequestEditListener editListener = new RequestEditListener();
    private FetchCommand fetchCommand;
    private RevertCommand revertCommand;
    private String[] requestFields = new String[]{
        Conversation.PROPERTY_REQUEST_METHOD,
        Conversation.PROPERTY_REQUEST_URI,
        Conversation.PROPERTY_REQUEST_VERSION,
        Conversation.PROPERTY_REQUEST_HEADERS,
        Conversation.PROPERTY_REQUEST_CONTENT,
        Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT,};
    private String[] responseFields = new String[]{
        Conversation.PROPERTY_RESPONSE_VERSION,
        Conversation.PROPERTY_RESPONSE_STATUS,
        Conversation.PROPERTY_RESPONSE_MESSAGE,
        Conversation.PROPERTY_RESPONSE_HEADERS,
        Conversation.PROPERTY_RESPONSE_CONTENT,
        Conversation.PROPERTY_RESPONSE_FOOTERS,};

    public RequestMakerView() {
        conversationFormModel = ConversationFormSupport.createFormModel(new Conversation(), false, true, false);
        annotationFormModel = FormModelHelper.createUnbufferedFormModel(new Annotation());
        addEditListener();
        listener = new Listener();
        fetchCommand = new FetchCommand();
        revertCommand = new RevertCommand();
    }
    public FuzzingModel getFuzzingModel() {
        return (FuzzingModel) getApplicationContext().getBean("fuzzingModel");
    }
    private void addEditListener() {
        for (int i = 0, len = requestFields.length; i < len; i++) {
            ValueModel vm = conversationFormModel.getValueModel(requestFields[i]);
            vm.addValueChangeListener(editListener);
        }
    }

    private void removeEditListener() {
        for (int i = 0, len = requestFields.length; i < len; i++) {
            ValueModel vm = conversationFormModel.getValueModel(requestFields[i]);
            vm.removeValueChangeListener(editListener);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.dialog.AbstractDialogPage#createControl()
     */
    @Override
    protected JComponent createControl() {
        Form requestForm = new RequestForm(conversationFormModel);
        Form responseForm = new ResponseForm(conversationFormModel);
        Form annotationForm = new AnnotationForm(annotationFormModel);
        DialogPage page = new RequestMakerDialogPage(requestForm, responseForm, annotationForm);

        return DialogPageUtils.createStandardView(page, fetchCommand, revertCommand);
    }

    public void displayConversation(Conversation conversation) {
        Conversation request;
        if (conversation != null) {
            request = conversation.clone();
        } else {
            request = new Conversation();
        }
        removeEditListener();
        conversationFormModel.setFormObject(request);
        addEditListener();
    }

    private boolean isActiveFetcher(Fetcher fetcher) {
        return activeFetcher == fetcher;
    }

    private void fetchConversation(Conversation request) {
        FuzzingTree ft = new FuzzingTree(getFuzzingModel().getFuzzingMode());
        ArrayList<FuzzingVariable> flist = getFVariables(request);
        ft.construct(flist);

        if (ft.getPathsCount() == 0) {
            activeFetcher = new Fetcher(request);
            httpService.fetchResponses(activeFetcher, 1, false);
        } else {
            Conversation[] convTab = new Conversation[ft.getPathsCount()];
            //list with all paths
            LinkedList<LinkedList<NamedValue>> allLists = new LinkedList<LinkedList<NamedValue>>();
            for (int i = 0; i < ft.getPathsCount(); ++i) {
                LinkedList<NamedValue> ll = ft.getNamedValueList(i);
                if (ft.getMode() == FuzzingModel.FuzzingModes.RANDOM && allLists.contains(ll)) {
                    while (allLists.contains(ll)) {
                        ll = ft.getNamedValueList(i);
                    }
                }
                Conversation nrequest = modifyRequest(ll, request, ft);
                nrequest.setDate(new Date());
                convTab[i] = nrequest;
                allLists.add(ll);
            }
            activeFetcher = new Fetcher(convTab);
            httpService.fetchResponses(activeFetcher, 1, false);
        }
    }

    private void error(Exception e) {
        e.printStackTrace();
        log.log(Level.SEVERE, "doExecuteCommandError", e);
    }

    private void success(Conversation conversation) {
        getConversationService().addConversation(session, conversation);
        Annotation a = (Annotation) annotationFormModel.getFormObject();
        Annotation annotation = new Annotation();
        annotation.setAnnotation(a.getAnnotation());
        annotation.setId(conversation.getId());
        getConversationService().updateAnnotation(annotation);
        displayConversation(conversation);
    }

    public Conversation getConversation() {
        Conversation conversation = null;
        conversation = (Conversation) conversationFormModel.getFormObject();
        return conversation;
    }

    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        if (this.eventService != null) {
            this.eventService.unsubscribe(RequestCopyEvent.class, listener);
            this.eventService.unsubscribe(SessionEvent.class, listener);
        }
        this.eventService = eventService;
        if (this.eventService != null) {
            this.eventService.subscribe(RequestCopyEvent.class, listener);
            this.eventService.subscribe(SessionEvent.class, listener);
        }
    }

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * @return Returns the conversationService.
     */
    private ConversationService getConversationService() {
        if (conversationService == null) {
            conversationService = (ConversationService) getApplicationContext().getBean("conversationService");
        }
        return conversationService;
    }

    private ArrayList<FuzzingVariable> getFVariables(Conversation request) {
        ArrayList<FuzzingVariable> flist = new ArrayList<FuzzingVariable>();
        for (FuzzingVariable fv : getFuzzingModel().getFuzzingVariables()) {
            NamedValue[] rh = request.getRequestHeaders();
            URI u = request.getRequestUri();

            String uString = u.toString();
            //scoping url
            while (-1 != uString.indexOf(fv.getVariableName())) {
                flist.add(fv);
                uString = uString.replaceFirst(Pattern.quote(fv.getVariableName()), "");
            }
            //scoping request content
            if (request.getProcessedRequestContent() != null) {
                String c = new String(request.getProcessedRequestContent());
                while (-1 != c.indexOf(fv.getVariableName())) {
                    flist.add(fv);
                    c = c.replaceFirst(Pattern.quote(fv.getVariableName()), "");
                }
            }
            //request version
            String reqVer = request.getRequestVersion();
            while (-1 != reqVer.indexOf(fv.getVariableName())) {
                flist.add(fv);
                reqVer = reqVer.replaceFirst(Pattern.quote(fv.getVariableName()), "");
            }
            String reqM = request.getRequestMethod();
            while (-1 != reqM.indexOf(fv.getVariableName())) {
                flist.add(fv);
                reqM = reqM.replaceFirst(Pattern.quote(fv.getVariableName()), "");
            }
            for (int i = 0; i < rh.length; ++i) {
                NamedValue nv = rh[i];
                String name = nv.getName();
                String val = nv.getValue();
                while (-1 != name.indexOf(fv.getVariableName())) {
                    flist.add(fv);
                    name = name.replaceFirst(Pattern.quote(fv.getVariableName()), "");
                }
                while (-1 != val.indexOf(fv.getVariableName())) {
                    flist.add(fv);
                    val = val.replaceFirst(Pattern.quote(fv.getVariableName()), "");
                }

            }
        }
        return flist;
    }

    private Conversation modifyRequest(LinkedList<NamedValue> ll, Conversation orequest, FuzzingTree ft) {
        Conversation request = orequest.clone();
        for (int i = 0; i < ft.getHeight(); ++i) {
            for (NamedValue nv : ll) {
                NamedValue[] rh = request.getRequestHeaders();

                try {
                    URI u = request.getRequestUri();
                    if (u.toString().contains(nv.getName())) {
                        request.setRequestUri(new URI(u.toString().replaceFirst(Pattern.quote(nv.getName()), nv.getValue())));
                        continue;
                    }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(RequestMakerView.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                //content
                if (request.getProcessedRequestContent() != null) {
                    String c = new String(request.getProcessedRequestContent());
                    if (c.contains(nv.getName())) {
                        request.setProcessedRequestContent(c.replaceFirst(Pattern.quote(nv.getName()), nv.getValue()).getBytes());
                        continue;
                    }
                }
                //version
                if (request.getRequestVersion().contains(nv.getName())) {
                    request.setRequestVersion(request.getRequestVersion().replaceFirst(Pattern.quote(nv.getName()), nv.getValue()));
                    continue;
                }
                //method
                if (request.getRequestMethod().contains(nv.getName())) {
                    request.setRequestMethod(request.getRequestMethod().replaceFirst(Pattern.quote(nv.getName()), nv.getValue()));
                    continue;
                }
                boolean continueHeaders = true;
                for (int j = 0; j < rh.length && continueHeaders; ++j) {
                    NamedValue nvr = rh[j];
                    String name = nvr.getName();
                    String val = nvr.getValue();
                    if (name.contains(nv.getName())) {
                        //TODO:
                        //request.setRequestHeader(nvr);
                        continueHeaders = false;
                    }
                    if (val.contains(nv.getName())) {
                        //TODO: why requests have the same headers in db even though requests are correct? :(
                        val = val.replaceFirst(Pattern.quote(nv.getName()), nv.getValue());
                        request.setRequestHeader(new NamedValue(name, val));
                        continueHeaders = false;
                    }
                }
            }
        }
        return request;
    }

    private class Listener extends SwingEventSubscriber {

        /* (non-Javadoc)
         * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
         */
        @Override
        protected void handleEventOnEDT(EventServiceEvent evt) {
            if (evt instanceof RequestCopyEvent) {
                handleEvent((RequestCopyEvent) evt);
            }
            if (evt instanceof SessionEvent) {
                handleEvent((SessionEvent) evt);
            }
        }

        private void handleEvent(RequestCopyEvent mrce) {
            Object source = mrce.getSource();
            if(source.getClass().getName().startsWith("org.owasp.webscarab.plugins.request.swing.RequestContextMenu")) {
                displayConversation(mrce.getConversation());
                return;
            }
            if (!(source instanceof PageComponent)) {
                return;
            }
            PageComponent pc = (PageComponent) source;
            if (!pc.getContext().getPage().equals(getContext().getPage())) {
                return;
            }
            displayConversation(mrce.getConversation());
        }

        private void handleEvent(SessionEvent evt) {
            session = evt.getSession();
            displayConversation(null);
        }
    }

    private class Fetcher implements ConversationGenerator {

        private Conversation[] requests;
        private boolean executed = false;
        private int currentRequestInd = 0;

        public Fetcher(Conversation request) {
            this.requests = new Conversation[1];
            this.requests[0] = request;
        }

        public Fetcher(Conversation[] requests) {
            this.requests = new Conversation[requests.length];
            this.requests = requests;
        }
        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#errorFetchingResponse(org.owasp.webscarab.domain.Conversation, java.lang.Exception)
         */

        public void errorFetchingResponse(final Conversation request, final Exception e) {
            log.log(Level.SEVERE, "Error while fetching", e);
            if (!isActiveFetcher(this)) {
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    error(e);
                }
            });
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#getNextRequest()
         */
        public synchronized Conversation getNextRequest() {
            //if (!isActiveFetcher(this)) {
            //   return null;
            //S}
            //if (executed) {
             //   return null;
            //}
            if(currentRequestInd==requests.length)
                return null;
            executed = true;
            return requests[currentRequestInd++];
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#responseReceived(org.owasp.webscarab.domain.Conversation)
         */
        public void responseReceived(final Conversation conversation) {
            /*if (!isActiveFetcher(this)) {
            return;
            }*/
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    success(conversation);
                }
            });
        }
    }

    private class RequestEditListener implements PropertyChangeListener {

        /*
         * Ensures that any response fields are cleared as soon as the user starts editing the request fields
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
            for (int i = 0, len = responseFields.length; i < len; i++) {
                ValueModel vm = conversationFormModel.getValueModel(responseFields[i]);
                if (vm.getValue() != null) {
                    vm.setValue(null);
                }
            }
        }
    }

    private class FetchCommand extends ActionCommand {

        public FetchCommand() {
            super("fetchCommand");
        }

        protected void doExecuteCommand() {
            Conversation conversation = getConversation();
            fetchConversation(conversation);
        }
    }

    private class RevertCommand extends ActionCommand {

        public RevertCommand() {
            super("revertCommand");
        }

        protected void doExecuteCommand() {
            conversationFormModel.revert();
            annotationFormModel.revert();
        }
    }

    /**
     * @param httpService the httpService to set
     */
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    private class RequestMakerDialogPage extends FormBackedDialogPage implements ActionListener {

        private Form requestForm;
        private Form responseForm;
        private Form annotationForm;
        private JButton variablesBtn;

        public RequestMakerDialogPage(Form requestForm, Form responseForm,
                Form annotationForm) {
            super("requestMakerForm", requestForm);
            this.requestForm = requestForm;
            this.responseForm = responseForm;
            this.annotationForm = annotationForm;
            this.variablesBtn = new JXButton("Fuzzing variables");
            this.variablesBtn.addActionListener(this);
        }

        @Override
        protected JComponent createControl() {
            JPanel panel = getComponentFactory().createPanel(new BorderLayout());
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            splitPane2.setResizeWeight(0.3);
            JPanel requestPanel = new JPanel(new BorderLayout());
            requestPanel.add(requestForm.getControl(), BorderLayout.CENTER);
            CommandGroup transformCommands = getWindowCommandManager().createCommandGroup("transformRequest",
                    new Object[]{
                        TransformRequestCommand.createGetToPost(conversationFormModel),
                        TransformRequestCommand.createPostToMultipartPost(conversationFormModel),
                        TransformRequestCommand.createPostToGet(conversationFormModel),});
            requestPanel.add(transformCommands.createButtonBar(), BorderLayout.SOUTH);
            splitPane.setLeftComponent(requestPanel);
            splitPane.setRightComponent(responseForm.getControl());
            panel.add(splitPane, BorderLayout.CENTER);
            splitPane2.setRightComponent(this.annotationForm.getControl());
            splitPane2.setLeftComponent(this.variablesBtn);
            panel.add(splitPane2, BorderLayout.SOUTH);
            initPageValidationReporter();
            responseForm.getFormModel().validate();
            return panel;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == this.variablesBtn) {
                final HierarchicalFormModel formModel = FormModelHelper.createFormModel(getFuzzingModel(), true);
                FuzzingVariablesForm form = new FuzzingVariablesForm(formModel);
                FormBackedDialogPage page = new FormBackedDialogPage(form);
                TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page, null) {

                    protected void onAboutToShow() {
                        //setEnabled(page.isPageComplete());
                    }

                    @Override
                    protected boolean onFinish() {
                        formModel.commit();
                        return true;
                    }
                };
                dialog.setPreferredSize(new Dimension(500, 400));
                dialog.showDialog();
            }
        }
    }
}
