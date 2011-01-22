/**
 * 
 */
package org.owasp.webscarab.plugins.spider.swing;

import org.owasp.webscarab.plugins.spider.Spider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;

/**
 * @author lpz
 * 
 */
public class SpiderConfigCommand extends ApplicationWindowAwareCommand implements
        ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Spider spider;

    public SpiderConfigCommand() {
        super("spiderConfigCommand");
    }

    @Override
    protected void doExecuteCommand() {
        SpiderConfigDialog dialog = new SpiderConfigDialog(spider, applicationContext);
        
        dialog.showDialog();
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Spider getSpider() {
        if (spider == null) {
            spider = (Spider) applicationContext.getBean("spider");
        }
        return spider;
    }

    public void setSpider(Spider spider) {
        this.spider = spider;
    }
}
