/**
 * 
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.awt.Dimension;

import org.owasp.webscarab.plugins.spider.Spider;
import org.owasp.webscarab.util.SpiderConfig;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.context.ApplicationContext;
import org.springframework.richclient.dialog.CompositeDialogPage;
import org.springframework.richclient.dialog.TabbedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author lpz
 * 
 */
public class SpiderConfigDialog {
     private ApplicationContext applicationContext;
    TitledPageApplicationDialog dialog;
    CompositeDialogPage tabbedPage = new TabbedDialogPage(
            "spiderConfigPage");
    SpiderConfigMainForm main;
    SpiderConfigHeaderForm header;
    SpiderConfigValuesForm values;
    SpiderConfig spiderConfig = new SpiderConfig();
    Spider spider;
    HierarchicalFormModel hfm;

    SpiderConfigDialog(Spider s, ApplicationContext applicationContext) {
        spiderConfig = (SpiderConfig)applicationContext.getBean("spiderConfig");
        dialog = new TitledPageApplicationDialog(tabbedPage) {

            @Override
            protected boolean onFinish() {
                hfm.commit();
                return true;
            }
        };
        spider = s;
        hfm = FormModelHelper.createFormModel(spiderConfig);
        main = new SpiderConfigMainForm(hfm);
        header = new SpiderConfigHeaderForm(hfm);
        values = new SpiderConfigValuesForm(hfm);
        tabbedPage.addForm(main);
        tabbedPage.addForm(header);
        tabbedPage.addForm(values);

    }

    public SpiderConfig getSpiderConfig() {
        return spiderConfig;
    }

    public void setSpiderConfig(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
    }

    public void showDialog() {
        dialog.setPreferredSize(new Dimension(500, 400));
        dialog.showDialog();
    }
}
