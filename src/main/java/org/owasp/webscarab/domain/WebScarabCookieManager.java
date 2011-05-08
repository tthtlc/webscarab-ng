/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.domain;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.dao.CookiesDao;

/**
 * Class used in WebScarab for cookie managment
 * @author Lpz
 */
public class WebScarabCookieManager implements EventSubscriber {

    public static final String PROPERTY_COOKIES = "cookieConfigurations";
    private CookiesDao cookiesDao;
    private Logger logger = Logger.getLogger(getClass().getName());
    private EventService eventService;
    //private CookieStore cookieStore;
    private List<CookieConfiguration> cookieConfigurations = new ArrayList<CookieConfiguration>();

    public WebScarabCookieManager() {
        logger.info("Starting webscarab cookie manager");
    }

    public CookiesDao getCookiesDao() {
        return cookiesDao;
    }

    public void setCookiesDao(CookiesDao cookiesDao) {
        this.cookiesDao = cookiesDao;
    }
    
    public List<CookieConfiguration> getCookieConfigurations() {
        synchronized(this) {
            cookieConfigurations.clear();
            for(CookieConfiguration cc: cookiesDao.getAll())
                cookieConfigurations.add(cc);
        }
        return cookieConfigurations;
    }
    public void setCookieConfigurations(List<CookieConfiguration> newCookies) {
        synchronized(this) {
            Collection<CookieConfiguration> oldCookies = cookiesDao.getAll();
            for(CookieConfiguration nc: newCookies) {
                cookiesDao.update(nc);
            }
             for(CookieConfiguration oc: oldCookies) {
                if(!BaseEntity.contains(newCookies, oc))
                    cookiesDao.delete(oc.getId());
            }
        }
        this.cookieConfigurations = newCookies;
    }
    public boolean cookiesContains(Collection<CookieConfiguration> cookies, CookieConfiguration cookie){
        boolean foundVar = false;
        for (int i=0; i < cookies.size(); ++i) {
            int vid = cookie.getId();
            int nid = ((CookieConfiguration)cookies.toArray()[i]).getId();
            if (nid==vid) {
                foundVar = true;
                break;
            }
        }
        return foundVar;
    }
    public int getCookieCount() {
        return cookiesDao.getAll().size();
    }

    public void addCookie(URI uri, String cookie) {
        List<HttpCookie> list = HttpCookie.parse(cookie);
        CookieConfiguration cc = new CookieConfiguration(uri.toString(), cookie);
        synchronized(this) {
            cookiesDao.update(cc);
        }
    }

    public void remove(int id) {
        synchronized(this) {
            cookiesDao.delete(id);
        }
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        this.eventService.subscribeStrongly(SetCookieEvent.class, this);
    }
    /*
     * Converts uri cookies into header string
     * @param uri
     *      uri for cookies
     */

    public String getCookieString(URI uri) {
        CookieConfiguration ret = new CookieConfiguration();
        synchronized(this) {
            Collection<CookieConfiguration> ccs = cookiesDao.getAll();
            for(CookieConfiguration cc: ccs) {
                URI curi = URI.create(cc.getCookieUri());
                if(curi.getHost().equals( uri.getHost())) //TODO: check domain
                    ret = cc;
            }
        }
        return ret.getCookieValue();
    }

    public void onEvent(EventServiceEvent ese) {
        if (ese instanceof SetCookieEvent) {
            SetCookieEvent sce = (SetCookieEvent) ese;
            synchronized(this) {
                cookiesDao.update(new CookieConfiguration(sce.getUri().toString(), sce.getCookieValue()));
            }
        }
    }

}
