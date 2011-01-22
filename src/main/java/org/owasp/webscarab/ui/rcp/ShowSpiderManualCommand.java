/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.bushe.swing.event.EventService;
import org.owasp.webscarab.plugins.spider.swing.SpiderStartEvent;

import org.springframework.binding.form.FormModel;
import org.springframework.core.closure.Constraint;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.util.GuiStandardUtils;
import org.springframework.rules.PropertyConstraintProvider;
import org.springframework.rules.Rules;
import org.springframework.rules.constraint.property.PropertyConstraint;

/**
 * @author lpz
 *
 */
public class ShowSpiderManualCommand extends ApplicationWindowAwareCommand {

    private FormModel formModel;
    private SpiderManualForm spiderManualForm;
    private SpiderManualDialog dialog;
    private EventService eventService;

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public ShowSpiderManualCommand() {
        super("showSpiderManualCommand");
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {
        if (dialog == null) {
            formModel = FormModelHelper.createFormModel(new SpiderManualContents(), false);
            spiderManualForm = new SpiderManualForm();
            dialog = new SpiderManualDialog();
            spiderManualForm.setDialog(dialog);
            dialog.setCallingCommand(this);
            dialog.setCloseAction(CloseAction.HIDE);
            
        }
        dialog.showDialog();
    }

    private class SpiderManualDialog extends TitledPageApplicationDialog {

        public SpiderManualDialog() {
            super(new FormBackedDialogPage(spiderManualForm), getParentWindowControl(), CloseAction.HIDE);
            setModal(false);
        }

        @Override
        protected boolean onFinish() {
            return true;
        }
        public void conceal() {
            super.hide();
        }
        
        @Override
        protected String getCancelCommandId() {
            return "closeCommand";
        }

        @Override
        protected Object[] getCommandGroupMembers() {
            return new AbstractCommand[]{getCancelCommand()};
        }

        @Override
        public void setMessage(Message message) {
            getDialogPage().setMessage(message);
        }
    }

    private class SpiderManualForm extends AbstractForm {

        private static final String ERROR_URI = "Please enter proper URI or check your internet connection";
        private SpiderManualDialog dialog;
        private JTextField textBox;

        public SpiderManualForm() {
            super(formModel, "spiderManual");
        }

        /* (non-Javadoc)
         * @see org.springframework.richclient.form.AbstractForm#createFormControl()
         */
        @Override
        protected JComponent createFormControl() {
            JPanel panel = getComponentFactory().createPanel(new BorderLayout());
            panel.add(new JLabel("URL"), BorderLayout.CENTER);
            textBox = (JTextField) getBindingFactory().createBinding(JTextField.class, "url").getControl();
            textBox.setText("http://");
            panel.add(textBox, BorderLayout.CENTER);
            CommandGroup cg = CommandGroup.createCommandGroup(null, getSpiderCommands());
            JComponent buttonBar = cg.createButtonBar();
            GuiStandardUtils.attachDialogBorder(buttonBar);
            panel.add(buttonBar, BorderLayout.SOUTH);
            return panel;
        }

        private Object[] getSpiderCommands() {

            ActionCommand spiderFetch = new ActionCommand() {

                @Override
                protected void doExecuteCommand() {
                    URI uri = null;
                    try {
                        uri = new URI(textBox.getText());
                        URI[] uris = new URI[1];
                        uris[0] = uri;
                        String[] methods = new String[1];
                        methods[0] = "GET";
                        if (!uri.isAbsolute()) {
                            throw new URISyntaxException(textBox.getText(), "not absolute");
                        }
                        eventService.publish(new SpiderStartEvent(this, uris, methods, null));
                        dialog.conceal();
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(ShowSpiderManualCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };
            ActionCommand spiderFetchRec = new ActionCommand() {

                @Override
                protected void doExecuteCommand() {
                    URI uri;
                    try {
                        uri = new URI(textBox.getText());
                        URI[] uris = new URI[1];
                        uris[0] = uri;
                        String[] methods = new String[1];
                        methods[0] = "GET";
                        if (!uri.isAbsolute()) {
                            throw new URISyntaxException(textBox.getText(), "not absolute");
                        }
                        eventService.publish(new SpiderStartEvent(this, uris, methods, new Date()));
                        dialog.conceal();
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(ShowSpiderManualCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            spiderFetch.setCaption("Spider");
            spiderFetchRec.setCaption("Spider recuresively");
            spiderFetch.setLabel("Spider");
            spiderFetchRec.setLabel("Spider recuresively");
            return new AbstractCommand[]{spiderFetch, spiderFetchRec};
        }

        private void setDialog(SpiderManualDialog dialog) {
            this.dialog = dialog;
        }
    }

    private final class SpiderManualContents implements PropertyConstraintProvider {

        public final static String PROPERTY_URL = "url";
    

        private Rules validationRules;
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public SpiderManualContents() {
            initRules();
        }

        public PropertyConstraint getPropertyConstraint(String string) {
            return validationRules.getPropertyConstraint(string);
        }

        protected void initRules() {
            this.validationRules = new Rules(getClass()) {

                @Override
                protected void initRules() {
                    add(PROPERTY_URL, all(new Constraint[]{required(), uriconstraint()}));
                }
            };
        }
        protected Constraint uriconstraint() {
            return new URIConstraint();
        }
        class URIConstraint implements Constraint {
            //TODO: check it
            public boolean test(Object o) {
                try {
                    URI uri = new URI(o.toString());
                    if (!uri.isAbsolute()) {
                        return false;
                    }
                    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
                    return true;
                } catch (IOException ex) {
                } catch (URISyntaxException ex) {
                }
                return false;
            }

        }

    }
}
