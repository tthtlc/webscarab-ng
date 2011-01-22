/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.domain;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
/**
 *
 * @author Lpz
 */
public class WebscarabTag extends HTML.Tag {

    public static class Form {
        private String method;
        private String action;

        public String getAction() {
            if(null == action || action.isEmpty())
                return "/";
            else
                return action;
        }

        public String getMethod() {
            if(null == method || method.isEmpty())
                return "GET"; //GET is default value for form[method]
            else
                return method;
        }
        
        public Form(HTML.Tag tag, MutableAttributeSet set)
        {
            this.method = (String)set.getAttribute(Attribute.METHOD);
            this.action = (String)set.getAttribute(Attribute.ACTION);
        }
    }

    public static class Input {
        

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
        private String type = "";
        private String name = "";
        private String value = "";
        
        public Input(HTML.Tag tag, MutableAttributeSet set) {
            this.type = (String)set.getAttribute(Attribute.TYPE);
            this.name = (String)set.getAttribute(Attribute.NAME);
            this.value = (String)set.getAttribute(Attribute.VALUE);
        }
    }

    private HTML.Tag tag;
    private MutableAttributeSet set;

    public WebscarabTag(HTML.Tag tag, MutableAttributeSet set) {
        this.tag = tag;
        this.set = set;
    }

    public HTML.Tag getTag() {
        return tag;
    }

    public MutableAttributeSet getSet() {
        return set;
    }
}
