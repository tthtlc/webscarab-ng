/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Dimension;
import org.owasp.webscarab.domain.WebScarabCookieManager;

import org.owasp.webscarab.ui.rcp.forms.CookieManagerForm;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author lpz
 *
 */
public class ShowCookieManagerCommand extends ApplicationWindowAwareCommand {

    private HierarchicalFormModel formModel;
    private CookieManagerForm cookieManagerForm;

    public WebScarabCookieManager getCookieManager() {
        return cookieManager;
    }

    public void setCookieManager(WebScarabCookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }
    private CookieManagerDialog dialog;
    private WebScarabCookieManager cookieManager;

    public ShowCookieManagerCommand() {
        super("showCookieManagerCommand");
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {

        formModel = FormModelHelper.createCompoundFormModel(cookieManager);
        cookieManagerForm = new CookieManagerForm(formModel, cookieManager);
        dialog = new CookieManagerDialog();
        dialog.setCallingCommand(this);
        dialog.setPreferredSize(new Dimension(500,400));
        dialog.showDialog();
    }

    private class CookieManagerDialog extends TitledPageApplicationDialog {

        public CookieManagerDialog() {
            super(new FormBackedDialogPage(cookieManagerForm), getParentWindowControl(), CloseAction.HIDE);
            setModal(false);
        }

        @Override
        protected boolean onFinish() {
            formModel.commit();
            return true;
        }

        @Override
        protected String getCancelCommandId() {
            return "closeCommand";
        }

        @Override
        protected String getFinishCommandId() {
            return "finishCommand";
        }

        @Override
        protected Object[] getCommandGroupMembers() {
            return new AbstractCommand[]{getFinishCommand(), getCancelCommand()};
        }

        @Override
        public void setMessage(Message message) {
            getDialogPage().setMessage(message);
        }
    }
}
