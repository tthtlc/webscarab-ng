/**
 * 
 */
package org.owasp.webscarab.plugins.spider.swing;

import org.owasp.webscarab.plugins.proxy.Proxy;
import org.owasp.webscarab.plugins.spider.Spider;
import org.springframework.beans.BeansException;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.form.FormModelHelper;
/**
 * @author lpz
 * 
 */
public class SpiderCommand extends ApplicationWindowAwareCommand implements
		ApplicationContextAware {

	private ApplicationContext applicationContext;

	private Proxy proxy;

        private Spider spider;

	public SpiderCommand() {
		super("spiderCommand");
	}

	@Override
	protected void doExecuteCommand() {
		final HierarchicalFormModel model = FormModelHelper
				.createCompoundFormModel(getProxy());
                this.getApplicationWindow().getPage().showView("spiderView");
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private Proxy getProxy() {
		if (proxy == null)
			proxy = (Proxy) applicationContext.getBean("proxy");
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

        private Spider getSpider() {
		if (spider == null)
			spider = (Spider) applicationContext.getBean("spider");
		return spider;
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}
}
