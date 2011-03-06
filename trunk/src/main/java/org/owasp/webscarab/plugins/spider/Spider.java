/**
 *
 */
package org.owasp.webscarab.plugins.spider;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.StringReader;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.dao.UriDao;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.ui.rcp.URIFoundEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.FormValueConfiguration;
import org.owasp.webscarab.domain.HeaderConfiguration;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.domain.SetCookieEvent;
import org.owasp.webscarab.domain.WebScarabCookieManager;
import org.owasp.webscarab.domain.WebscarabTag;
import org.owasp.webscarab.plugins.proxy.ListenerConfiguration;
import org.owasp.webscarab.plugins.proxy.Proxy;

import org.owasp.webscarab.plugins.spider.swing.SpiderFetchEvent;
import org.owasp.webscarab.plugins.spider.swing.SpiderStartEvent;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.SpiderConfig;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.statusbar.StatusBar;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;

/**
 * @author lpz
 */
public class Spider extends ApplicationServicesAccessor implements ApplicationContextAware, EventSubscriber  {

    private Proxy proxy;
    //list of visited uris
    private ArrayList<URI> uris;
    private ApplicationContext applicationContext;
    private EventService eventService;
    private ConversationService conversationService;
    private WebScarabCookieManager webscarabCookieManager;
    private Session session;
    private int threadCount = 0;
    private final Object spiderThreadsMonitor = new Object();
    private boolean enabled = true;
    private UriDao uriDao;
    private SpiderConfig spiderConfig;
    final private ArrayList<String> fetchedURIs;

    public UriDao getUriDao() {

        return uriDao;
    }

    public void setUriDao(UriDao uriDao) {
        this.uriDao = uriDao;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Spider() {
        uris = new ArrayList<URI>();
        fetchedURIs = new ArrayList<String>();
    }

    public ArrayList<URI> getURIs() {
        uris.clear();
        for (URI u : uriDao.getAll()) {
            uris.add(u);
        }
        return uris;
    }

    public String getUriHash(URI uri) {
        return uri.toASCIIString();
    }

    synchronized public boolean checkIfCanBeFetched(URI uri) {
        String hash = getUriHash(uri);
        if (hash.matches(Spider.this.spiderConfig.getAllowedPattern())
                && !hash.matches(Spider.this.spiderConfig.getDisallowedPattern())) {
            //notifing spider view
            URIFoundEvent ufe = new URIFoundEvent(Spider.this, uri, new Date());
            this.getEventService().publish(ufe);
            return true;
        }
        return false;
    }

    public void clearFetchedUris() {
        fetchedURIs.clear();
    }

    synchronized public void addFetchedUri(URI uri) {
        fetchedURIs.add(getUriHash(uri));
    }

    public void setURIs(ArrayList<URI> uris) {
        this.uris = uris;
    }

    public boolean addURI(URI uri) {
        if (this.checkURI(uri)) {
            uris.add(uri);
            uriDao.saveUri(uri);
            return true;
        }
        return false;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setCookieManager(WebScarabCookieManager cookieManager) {
        this.webscarabCookieManager = cookieManager;
    }

    public WebScarabCookieManager getCookieManager() {
        return webscarabCookieManager;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        if (eventService != null) {
            getEventService().subscribeStrongly(SessionEvent.class, this);
            getEventService().subscribeStrongly(SpiderFetchEvent.class, this);
            getEventService().subscribeStrongly(SpiderStartEvent.class, this);
            getEventService().subscribeStrongly(URIFoundEvent.class, this);
        }
    }

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
        if (conversationService != null) {
            //eventService.subscribeStrongly(URIFoundEvent.class, uriListener);
        }
    }

    /**
     * @param session
     *            the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    public boolean useProxy() {
        return true;
    }

    /**
     * Returns Java Proxy object
     */
    public java.net.Proxy getProxyForSpider() {
        ListenerConfiguration lc = proxy.getListeners().get(0);
        SocketAddress sa = new InetSocketAddress(lc.getHostName(), lc.getPort());
        return new java.net.Proxy(java.net.Proxy.Type.HTTP, sa);
    }

    /**
     * Checks if given uri can by analyzed by spider
     * @param uri
     *            the URI to check
     */
    public boolean checkURI(URI uri) {
        try {
            URL url = uri.toURL();
            HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
            String contentType = ucon.getContentType();
            if ((contentType != null) && contentType.matches(this.spiderConfig.getFetchPattern())) {
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
        return false;
    }

    public void fetchURI(URI uri, String method, Date start, String parameters) {
        FetchUriThread ft = new FetchUriThread();
        ft.setUri(uri);
        ft.setMethod(method);
        ft.setConversationService(conversationService);
        ft.setSession(session);
        ft.setParameters(parameters);
        ft.setStart(start);
        Thread t = new Thread(ft);

        t.start();

    }

    public void fetchURIs(URI[] uri, String[] methods, Date start, String[] parameters) {
        for (int i = 0; i < uri.length; ++i) {
            fetchURI(uri[i], methods[i], start, parameters[i]);
        }
    }

    public void onEvent(EventServiceEvent ese) {
        if (ese instanceof SessionEvent) {
            SessionEvent event = (SessionEvent) ese;
            spiderConfig = (SpiderConfig) applicationContext.getBean("spiderConfig");
            if (event.getType() == SessionEvent.SESSION_CHANGED) {
                setSession(event.getSession());
            }
            /*
             * Checking for event about URL and working only if spider
             * is enabled
             */
        } else if ((ese instanceof URIFoundEvent) && this.isEnabled()) {
            URIFoundEvent event = (URIFoundEvent) ese;
            URI uri = event.getURI();
            //no need to do anything - uri already checked
            if (uris.contains(uri)) {
                return;
            } else {
                uris.add(uri);
            }
            URI[] uuris = new URI[1];
            String[] methods = new String[1];
            methods[0] = "GET";
            uuris[0] = uri;
            Date start = event.getStart();
            if (start != null) {
                Calendar c1 = new GregorianCalendar();
                Calendar c2 = new GregorianCalendar();
                c2.setTime(new Date());
                c1.setTime(start);
                c1.set(Calendar.SECOND, c1.get(Calendar.SECOND) + spiderConfig.getMaxSeconds());
                if (c1.after(c2)) {
                    eventService.publish(new SpiderFetchEvent(this, uuris, methods, event.getStart()));
                }
            }

        } else if (ese instanceof SpiderFetchEvent) {
            SpiderFetchEvent cse = (SpiderFetchEvent) ese;
            this.fetchURIs(cse.getURIs(), cse.getMethods(), cse.getStart(), cse.getParameters());
        } else if (ese instanceof SpiderStartEvent) {
            SpiderStartEvent cse = (SpiderStartEvent) ese;
            this.clearFetchedUris();
            this.fetchURIs(cse.getURIs(), cse.getMethods(), cse.getStart(), cse.getParameters());
        }

    }

    public EventService getEventService() {
        return eventService;
    }

    /**
     * A HTML parser callback used by this class to detect links and forms.
     */
    public class HTMLParse extends HTMLEditorKit {

        @Override
        public HTMLEditorKit.Parser getParser() {
            return super.getParser();
        }
    }

    protected class Parser
            extends HTMLEditorKit.ParserCallback {

        protected URL base;
        private URI origin;
        private Date start;
        private HashMap<WebscarabTag.Form, ArrayList<WebscarabTag.Input>> forms;
        private WebscarabTag.Form currentForm = null;
        private ArrayList<WebscarabTag.Input> currentInputs = null;

        public Parser(URL base) {
            this.base = base;
            forms = new HashMap<WebscarabTag.Form, ArrayList<WebscarabTag.Input>>();
        }

        public HashMap<WebscarabTag.Form, ArrayList<WebscarabTag.Input>> getForms() {
            return forms;
        }

        @Override
        public void handleSimpleTag(HTML.Tag t,
                MutableAttributeSet a, int pos) {
            if (t == HTML.Tag.INPUT) {
                String name = (String) a.getAttribute(HTML.Attribute.NAME);
                currentInputs.add(new WebscarabTag.Input(t, a));
                return;
            }
            String href = (String) a.getAttribute(HTML.Attribute.HREF);
            if ((href == null) && (t == HTML.Tag.FRAME)) {
                href = (String) a.getAttribute(HTML.Attribute.SRC);
            }
            if (href == null) {
                return;
            }
            int i = href.indexOf('#');
            if (i != -1) {
                href = href.substring(0, i);
            }

            if (href.toLowerCase().startsWith("mailto:")) {
                Logger.getLogger(Spider.class.getName()).log(Level.INFO, Spider.this.getMessage("spider.mailFound.message"), href);
                return;
            }
            if (href.toLowerCase().startsWith("javascript:")) {
                return;
            }
            try {
                handleLink(base, href);
            } catch (URISyntaxException ex) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void handleStartTag(HTML.Tag t,
                MutableAttributeSet a, int pos) {
            // handle the same way
            if (HTML.Tag.FORM == t) {
                currentForm = new WebscarabTag.Form(t, a);
                currentInputs = new ArrayList<WebscarabTag.Input>();
            } else if (HTML.Tag.INPUT == t) {
                currentInputs.add(new WebscarabTag.Input(t, a));
            } else {
                handleSimpleTag(t, a, pos);
            }
        }

        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
            if (HTML.Tag.FORM == t) {
                forms.put(currentForm, currentInputs);
                currentInputs = new ArrayList<WebscarabTag.Input>();
                currentForm = null;
            }
        }

        protected void handleLink(URL base, String str) throws URISyntaxException {
            try {
                URL url = new URL(base, str);
                if (url.getHost().equalsIgnoreCase(origin.getHost())) {
                    URIFoundEvent ufe = new URIFoundEvent(Spider.this, url.toURI(), start);
                    Spider.this.getEventService().publish(ufe);
                }
            } catch (MalformedURLException e) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, str, e);
            }
        }

        private void setStart(Date start) {
            this.start = start;
        }

        private void setOrigin(URI or) {
            this.origin = or;
        }
    }
    /*
     * Thread for single spider fetch
     */

    private class FetchUriThread implements Runnable {

        private ReadWriteLock lock = new ReentrantReadWriteLock();
        private URI uri;
        private String method;
        private ConversationService conversationService;
        private Session session;
        private SpiderContentHandlerFactory fctry = new SpiderContentHandlerFactory();
        private Date start = null;
        private String parameters;

        public void setStart(Date start) {
            this.start = start;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }

        public void setMethod(String m) {
            this.method = m;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public void setConversationService(ConversationService conversationService) {
            this.conversationService = conversationService;
        }

        public void updateStatusBar(int i) {
            ApplicationWindow aw = Application.instance().getActiveWindow();
            StatusBar sb = aw.getStatusBar();
            sb.setVisible(true);
            if (i != 0) {
                sb.setMessage(String.format(Spider.this.getMessage("spider.statusBar.yes.message"),i)); 
            } else {
                sb.setMessage( Spider.this.getMessage("spider.statusBar.no.message"));
            }
        }

        public synchronized void incrementThreads() throws InterruptedException {

            synchronized (spiderThreadsMonitor) {
                threadCount++;
                if (threadCount >= spiderConfig.getMaxThreads()) {
                    spiderThreadsMonitor.wait();
                }
                updateStatusBar(threadCount);
            }
        }

        public synchronized void decrementThreads() {

            synchronized (spiderThreadsMonitor) {
                threadCount--;
                updateStatusBar(threadCount);
                spiderThreadsMonitor.notify();
            }
        }

        public synchronized boolean newThreadAllowed() {
            return threadCount < spiderConfig.getMaxThreads();
        }

        private void createConvarsation(String method, HttpURLConnection ucon, byte[] content) throws IOException {
            Conversation conv = new Conversation();
            conv.setRequestUri(uri);
            conv.setDate(new Date(ucon.getDate()));
            conv.setRequestMethod(method);
            Lock rl = lock.readLock();
            rl.lock();
            try {
                Collection<Integer> ids = conversationService.getConversationIds(this.session);
                Iterator<Integer> it = ids.iterator();
                int max = Integer.MIN_VALUE;
                while (it.hasNext()) {
                    int val = it.next();
                    if (val > max) {
                        max = val;
                    }
                }
                conv.setId(max + 1);
                conv.setSource("spider");
                conv.setResponseContent(content);
                String first = ucon.getHeaderFields().values().toArray()[0].toString().replace("[", "").replace("]", "");
                String[] head = first.split(" ");
                NamedValue[] nv = getResponseHeaders(ucon);
                conv.setResponseHeaders(nv);
                conv.setRequestVersion("HTTP/1.1"); //TODO
                conv.setResponseVersion(head[0]);
                conv.setResponseStatus(head[1]);
                conv.setResponseMessage(first.split(head[1] + " ")[1]);
                conv.setRequestHeaders(getRequestHeaders(uri));
                conversationService.addConversation(session, conv);
            } finally {
                rl.unlock();
            }
        }

        private HttpURLConnection prepareConnection(URL url, String data) throws IOException {
            HttpURLConnection ucon = (HttpURLConnection) (useProxy() ? url.openConnection(getProxyForSpider()) : url.openConnection());
            ucon.setRequestMethod(method.toUpperCase());
            synchronized (spiderConfig.getHeaderConfigurations()) {
                Iterator<HeaderConfiguration> it = spiderConfig.getHeaderConfigurations().iterator();
                while (it.hasNext()) {
                    HeaderConfiguration hc = it.next();
                    ucon.setRequestProperty(hc.getHeaderName(), hc.getHeaderValue());
                }
            }
            ucon.setRequestProperty("Host", url.getHost());
            String cookie = webscarabCookieManager.getCookieString(uri);
            if (!cookie.isEmpty()) {
                ucon.setRequestProperty("Cookie", cookie);
            }
            //ucon.setRequestProperty("Proxy-Connection", proxyConnection);
            if (method.equalsIgnoreCase("post")) {
                ucon.setDoOutput(true);
            }
            ucon.connect();
            if (method.equalsIgnoreCase("post")) {
                OutputStreamWriter wr = new OutputStreamWriter(ucon.getOutputStream());
                wr.write(data);
                wr.flush();
            }
            return ucon;
        }

        private void checkForSetCookie(NamedValue[] nv, URI uri) {
            for (int i = 0; i < nv.length; ++i) {
                if (nv[i].getName().equalsIgnoreCase("set-cookie")) {
                    Spider.this.getEventService().publish(new SetCookieEvent(Spider.this, uri, nv[i].getValue()));
                }
            }
        }

        private String getValueForInput(String name, String value) {
            String newVal = value == null ? "" : value;
            synchronized (spiderConfig) {
                Iterator<FormValueConfiguration> it = spiderConfig.getFormValuesConfigurations().iterator();
                while (it.hasNext()) {
                    FormValueConfiguration fvc = it.next();
                    if (name.matches(fvc.getFormName()) && (fvc.getOverwrite() || newVal.isEmpty())) {
                        newVal = fvc.getFormValue();
                    }
                }
            }
            return newVal;
        }

        private void followForms(URL base, HashMap<WebscarabTag.Form, ArrayList<WebscarabTag.Input>> forms) throws MalformedURLException, URISyntaxException {
            Iterator<WebscarabTag.Form> it = forms.keySet().iterator();
            while (it.hasNext()) {
                WebscarabTag.Form form = it.next();
                boolean isPost = form.getMethod().equalsIgnoreCase("post");
                ArrayList<WebscarabTag.Input> inputs = forms.get(form);
                StringBuilder query = new StringBuilder((isPost ? "" : "?"));
                Iterator<WebscarabTag.Input> itInputs = inputs.iterator();
                while (itInputs.hasNext()) {
                    WebscarabTag.Input input = itInputs.next();
                    if (input.getName() == null) //submit buttons and so on
                    {
                        continue; //skipping empty inputs
                    }
                    query.append(input.getName());
                    query.append("=");
                    query.append(getValueForInput(input.getName(), input.getValue()));
                    if (itInputs.hasNext()) {
                        query.append("&");
                    }
                }
                URI[] uris = new URI[1];
                String q = query.toString();
                String[] methods = new String[1];
                String[] parameters = new String[1];
                parameters[0] = isPost ? q : "";
                uris[0] = isPost ? new URL(base, form.getAction()).toURI() : new URL(base, form.getAction() + q).toURI();
                methods[0] = form.getMethod();
                Spider.this.getEventService().publish(new SpiderFetchEvent(Spider.this, uris, methods, new Date(), parameters));
            }
        }

        private void setParameters(String parameters) {
            this.parameters = parameters;
        }

        public class plain extends ContentHandler {

            public Object getContent(URLConnection urlc) throws IOException {
                StringBuffer out = new StringBuffer();
                PushbackInputStream in;
                int c;

                in = new PushbackInputStream(urlc.getInputStream());
                try {
                    c = in.read();
                    while (-1 < c) {
                        out.append((char) c);
                        c = in.read();
                    }
                } finally {
                    in.close();
                }
                out.append('\n');
                return out.toString();
            }
        }

        class SpiderContentHandlerFactory implements ContentHandlerFactory {

            public ContentHandler createContentHandler(String contentType) {
                if (contentType.matches(Spider.this.spiderConfig.getFetchPattern())) {
                    return new plain();
                }
                return null;
            }
        }

        public void run() {
            try {
                if (!checkIfCanBeFetched(uri)) {
                    return;
                } else {
                    addFetchedUri(uri);
                }
                this.incrementThreads();

                HttpURLConnection ucon = prepareConnection(uri.toURL(), parameters);
                String contentType = ucon.getContentType();
                if ((contentType != null) && contentType.matches(Spider.this.spiderConfig.getFetchPattern())) {
                    ContentHandler h = this.fctry.createContentHandler(ucon.getContentType());

                    byte[] content = null;
                    Object o = h.getContent(ucon);
                    if (o instanceof String) {
                        content = o.toString().getBytes();
                    }
                    if (!useProxy()) {
                        createConvarsation(this.method, ucon, content);
                    } else {
                        checkForSetCookie(getResponseHeaders(ucon), uri);
                    }
                    StringReader r = new StringReader(o.toString());
                    HTMLEditorKit.Parser parser = new HTMLParse().getParser();
                    Parser p = new Parser(uri.toURL());
                    p.setStart(this.start);
                    p.setOrigin(uri);
                    parser.parse(r, p, enabled);

                    if (Spider.this.spiderConfig.isFollowForms()) {
                        this.followForms(uri.toURL(), p.getForms());
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Spider.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                this.decrementThreads();
            }
        }

        private NamedValue[] getResponseHeaders(URLConnection ucon) {
            int size = ucon.getHeaderFields().size() - 1;
            NamedValue[] nvalues = new NamedValue[size];
            Iterator<String> keys = ucon.getHeaderFields().keySet().iterator();
            Iterator<List<String>> values = ucon.getHeaderFields().values().iterator();
            keys.next();
            values.next();
            for (int i = 0; i < size; ++i) {
                nvalues[i] = new NamedValue(keys.next(), listToString(values.next()));
            }
            return nvalues;
        }
        /*
         *
         */

        private String listToString(Object o) {
            List<String> list = (List<String>) o;
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }

        private NamedValue[] getRequestHeaders(URI uri) {

            ArrayList<NamedValue> list = new ArrayList<NamedValue>();
            Iterator<HeaderConfiguration> it = spiderConfig.getHeaderConfigurations().iterator();
            while (it.hasNext()) {
                HeaderConfiguration hc = it.next();
                list.add(new NamedValue(hc.getHeaderName(), hc.getHeaderValue()));
            }
            list.add(new NamedValue("Host", uri.getHost()));
            String cookie = Spider.this.getCookieManager().getCookieString(uri);
            if (!cookie.isEmpty()) {
                list.add(new NamedValue("Cookie", cookie));
            }
            //list.add(new NamedValue("Proxy-Connection", proxyConnection));
            NamedValue[] nvalues = new NamedValue[list.size()];
            nvalues = list.toArray(nvalues);
            return nvalues;
        }
    }

    class Listener {

        private HeaderConfiguration configuration;

        public Listener(HeaderConfiguration configuration) throws IOException {
            this.configuration = configuration;

        }

        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
