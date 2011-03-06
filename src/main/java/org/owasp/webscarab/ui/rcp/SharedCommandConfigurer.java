/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.util.Assert;

/**
 * @author rdawes
 *
 */
public class SharedCommandConfigurer implements InitializingBean {
    Logger log = Logger.getLogger(this.getClass().getName());

    private CommandConfigurer commandConfigurer;

    private List<AbstractCommand> commands;

    /**
     * @param commandConfigurer the commandConfigurer to set
     */
    public void setCommandConfigurer(
            CommandConfigurer commandConfigurer) {
        this.commandConfigurer = commandConfigurer;
    }

    /**
     * @param commands the commands to set
     */
    public void setCommands(List<AbstractCommand> commands) {
        this.commands = commands;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        log.log(Level.INFO,"Creating SharedCommandConfigurer");
        if (commandConfigurer == null) {
            commandConfigurer = (CommandConfigurer) ApplicationServicesLocator.services().getService(CommandConfigurer.class);
        }
        Assert.notNull(commandConfigurer, "CommandConfigurer may not be null.");
        if (commands != null) {
            Iterator<AbstractCommand> it = commands.iterator();
            while (it.hasNext())
                commandConfigurer.configure(it.next());
        }

    }

}
