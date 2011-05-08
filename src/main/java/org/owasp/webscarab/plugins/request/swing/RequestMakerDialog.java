/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request.swing;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;
import javax.wsdl.Message;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;

/**
 *
 * @author Lpz
 */
public class RequestMakerDialog  {
    /*
    public RequestMakerDialog() {
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
    }*/
}
