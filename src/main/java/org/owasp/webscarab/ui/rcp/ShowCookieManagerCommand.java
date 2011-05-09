/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Dimension;
import org.owasp.webscarab.domain.WebScarabCookieManager;

import org.owasp.webscarab.ui.rcp.forms.CookieManagerForm;
import org.springframework.beans.BeansException;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class ShowCookieManagerCommand extends ApplicationWindowAwareCommand implements ApplicationContextAware {

    private HierarchicalFormModel formModel;
    private CookieManagerForm cookieManagerForm;
    private ApplicationContext applicationContext;
    
    public WebScarabCookieManager getCookieManager() {
        return (WebScarabCookieManager) applicationContext.getBean("WebScarabCookieManager") ;
    }

    private CookieManagerDialog dialog;

    public ShowCookieManagerCommand() {
        super("showCookieManagerCommand");
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {
        WebScarabCookieManager cookieManager = getCookieManager();
        formModel = FormModelHelper.createCompoundFormModel(cookieManager);
        cookieManagerForm = new CookieManagerForm(formModel, cookieManager);
        dialog = new CookieManagerDialog();
        dialog.setCallingCommand(this);
        dialog.setPreferredSize(new Dimension(500,400));
        dialog.showDialog();
    }

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        applicationContext = ac;
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
