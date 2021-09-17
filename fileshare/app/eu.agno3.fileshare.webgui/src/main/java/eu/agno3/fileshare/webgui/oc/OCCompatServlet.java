/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.oc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jetty.io.EofException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class OCCompatServlet extends HttpServlet {

    /**
     * 
     */
    private static final String JSON = "json"; //$NON-NLS-1$
    private static final String XML = "xml"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(OCCompatServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 5289473211567416681L;

    private static final ObjectMapper OM = new ObjectMapper();
    private static final JsonFactory JF;

    private static final String SHARES_PREFIX = "/ocs/v1.php/apps/files_sharing/api/v1/shares"; //$NON-NLS-1$
    private static final String SHARES_PREFIX_ALT = "/index.php/ocs/apps/files_sharing/api/v1/shares"; //$NON-NLS-1$

    private static final String CAPABILITIES_PREFIX = "/ocs/v1.php/cloud/capabilities"; //$NON-NLS-1$
    private static final String CAPABILITIES_PREFIX_ALT = "/index.php/ocs/cloud/capabilities"; //$NON-NLS-1$

    private static final String USER_PREFIX = "/ocs/v1.php/cloud/user"; //$NON-NLS-1$
    private static final String USER_PREFIX_ALT = "/index.php/ocs/cloud/user"; //$NON-NLS-1$


    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service ( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        req.setAttribute("bypass-error-handler", true); //$NON-NLS-1$

        String reqPath = WebUtils.getPathWithinApplication(req);

        if ( log.isTraceEnabled() ) {
            log.trace("called " + reqPath); //$NON-NLS-1$
        }

        try {
            if ( reqPath.startsWith(SHARES_PREFIX) ) {
                handleShares(req, resp, reqPath.substring(SHARES_PREFIX.length()));
                return;
            }
            else if ( reqPath.startsWith(SHARES_PREFIX_ALT) ) {
                handleShares(req, resp, reqPath.substring(SHARES_PREFIX_ALT.length()));
                return;
            }
            else if ( reqPath.startsWith(CAPABILITIES_PREFIX) || reqPath.startsWith(CAPABILITIES_PREFIX_ALT) ) {
                handleCapabilities(req, resp);
                return;
            }
            else if ( reqPath.startsWith(USER_PREFIX) || reqPath.startsWith(USER_PREFIX_ALT) ) {
                handleUser(req, resp);
                return;
            }
        }
        catch ( EofException e ) {
            log.trace("EOF", e); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Sending 404 for " + req.getRequestURI()); //$NON-NLS-1$
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    /**
     * @param req
     * @param resp
     */
    private static void handleUser ( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        String format = req.getParameter("format"); //$NON-NLS-1$
        UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
        if ( format == null || XML.equals(format) ) {
            genXMLUser(up, resp);
        }
        else {
            genJSONUser(up, resp);
        }
    }


    /**
     * @param up
     * @param resp
     * @throws IOException
     */
    private static void genJSONUser ( UserPrincipal up, HttpServletResponse resp ) throws IOException {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> ocs = new HashMap<>();
        res.put("ocs", ocs); //$NON-NLS-1$
        Map<String, Object> meta = new HashMap<>();
        ocs.put("meta", meta); //$NON-NLS-1$

        meta.put(
            "status", //$NON-NLS-1$
            "ok"); //$NON-NLS-1$
        meta.put("statuscode", 100); //$NON-NLS-1$
        meta.put(
            "message", //$NON-NLS-1$
            "Ok"); //$NON-NLS-1$

        Map<String, Object> data = new HashMap<>();

        data.put("id", up.getUserId().toString()); //$NON-NLS-1$
        data.put(
            "display-name", //$NON-NLS-1$
            up.getUserName() + "@" + up.getRealmName()); //$NON-NLS-1$
        data.put(
            "email", //$NON-NLS-1$
            ""); //$NON-NLS-1$

        ocs.put("data", data); //$NON-NLS-1$

        try ( JsonGenerator gen = JF.createGenerator(resp.getOutputStream()) ) {
            gen.writeObject(res);
        }
    }


    /**
     * @param up
     * @param resp
     * @throws IOException
     */
    private static void genXMLUser ( UserPrincipal up, HttpServletResponse resp ) throws IOException {
        String xml = "<?xml version=\"1.0\" ?><ocs>" + //$NON-NLS-1$
                "<meta><status>ok</status><statuscode>100</statuscode><message>Ok</message></meta>" + //$NON-NLS-1$
                "<data><id>" + //$NON-NLS-1$
                up.getUserId() + "</id>" + //$NON-NLS-1$
                "<display-name>" + //$NON-NLS-1$
                up.getUserName() + "@" + up.getRealmName() //$NON-NLS-1$
                + "</display-name>" + //$NON-NLS-1$
                "<email></email></data></ocs>"; //$NON-NLS-1$
        resp.getWriter().write(xml);
    }


    /**
     * @param req
     * @param resp
     * @throws IOException
     */
    private static void handleCapabilities ( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        String format = req.getParameter("format"); //$NON-NLS-1$

        if ( format == null || XML.equals(format) ) {
            genXMLCapabilities(resp);
        }
        else {
            genJSONCapabilities(resp);
        }
    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void genJSONCapabilities ( HttpServletResponse resp ) throws IOException {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> ocs = new HashMap<>();
        res.put("ocs", ocs); //$NON-NLS-1$
        Map<String, Object> meta = new HashMap<>();
        ocs.put("meta", meta); //$NON-NLS-1$

        meta.put(
            "status", //$NON-NLS-1$
            "ok"); //$NON-NLS-1$
        meta.put("statuscode", 100); //$NON-NLS-1$
        meta.put(
            "message", //$NON-NLS-1$
            "Ok"); //$NON-NLS-1$

        Map<String, Object> data = new HashMap<>();

        Map<String, Object> fileshareCaps = new HashMap<>();
        fileshareCaps.put("api_enabled", false); //$NON-NLS-1$
        data.put("file_sharing", fileshareCaps); //$NON-NLS-1$
        ocs.put("data", data); //$NON-NLS-1$

        try ( JsonGenerator gen = JF.createGenerator(resp.getOutputStream()) ) {
            gen.writeObject(res);
        }

    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void genXMLCapabilities ( HttpServletResponse resp ) throws IOException {
        String xml = "<?xml version=\"1.0\" ?><ocs>" + //$NON-NLS-1$
                "<meta><status>ok</status><statuscode>100</statuscode><message>Ok</message></meta>" + //$NON-NLS-1$
                "<data><capabilities><file_sharing>" + //$NON-NLS-1$
                "<api_enabled>false</api_enabled>" + //$NON-NLS-1$
                "</file_sharing></capabilities></data></ocs>"; //$NON-NLS-1$
        resp.getWriter().write(xml);
    }


    /**
     * @param req
     * @param resp
     * @throws IOException
     */
    private static void handleShares ( HttpServletRequest req, HttpServletResponse resp, String relative ) throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("handleShares %s (%s)", relative, req.getMethod())); //$NON-NLS-1$
            Enumeration<String> headers = req.getHeaderNames();
            while ( headers.hasMoreElements() ) {
                String header = headers.nextElement();
                Enumeration<String> values = req.getHeaders(header);
                while ( values.hasMoreElements() ) {
                    log.trace(String.format("%s: %s", header, values.nextElement())); //$NON-NLS-1$
                }
            }
        }

        String path = req.getParameter("path"); //$NON-NLS-1$
        String format = req.getParameter("format"); //$NON-NLS-1$

        if ( format != null && !JSON.equals(format) && !XML.equals(format) ) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Requested path %s format %s", path, format)); //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(relative) && "POST".equals(req.getMethod()) ) { //$NON-NLS-1$
            log.debug("Create link share"); //$NON-NLS-1$

            if ( format == null || XML.equals(format) ) {
                createXMLShare(resp);
            }
            else if ( JSON.equals(format) ) {
                createJSONShare(resp);
            }

        }
        else if ( StringUtils.isBlank(relative) && "GET".equals(req.getMethod()) ) { //$NON-NLS-1$
            log.debug("List shares"); //$NON-NLS-1$
            if ( format == null || XML.equals(format) ) {
                genXMLShares(resp);
            }
            else {
                genJSONShares(resp);
            }
        }
        else if ( "DELETE".equals(req.getMethod()) ) { //$NON-NLS-1$
            log.debug("Delete link share"); //$NON-NLS-1$
        }
    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void createXMLShare ( HttpServletResponse resp ) throws IOException {
        String xml = "<?xml version=\"1.0\" ?><ocs>" + //$NON-NLS-1$
                "<meta><status>failure</status><statuscode>400</statuscode><message>Not usable</message></meta>" + //$NON-NLS-1$
                "<data></data></ocs>"; //$NON-NLS-1$
        resp.getWriter().write(xml);
    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void createJSONShare ( HttpServletResponse resp ) throws IOException {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> ocs = new HashMap<>();
        res.put("ocs", ocs); //$NON-NLS-1$
        Map<String, Object> meta = new HashMap<>();
        ocs.put("meta", meta); //$NON-NLS-1$

        meta.put(
            "status", //$NON-NLS-1$
            "failure"); //$NON-NLS-1$
        meta.put("statuscode", 400); //$NON-NLS-1$
        meta.put(
            "message", //$NON-NLS-1$
            "Not usable"); //$NON-NLS-1$

        List<Object> data = new ArrayList<>();
        ocs.put("data", data); //$NON-NLS-1$

        try ( JsonGenerator gen = JF.createGenerator(resp.getOutputStream()) ) {
            gen.writeObject(res);
        }
    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void genJSONShares ( HttpServletResponse resp ) throws IOException {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> ocs = new HashMap<>();
        res.put("ocs", ocs); //$NON-NLS-1$
        Map<String, Object> meta = new HashMap<>();
        ocs.put("meta", meta); //$NON-NLS-1$

        meta.put(
            "status", //$NON-NLS-1$
            "ok"); //$NON-NLS-1$
        meta.put("statuscode", 100); //$NON-NLS-1$
        meta.put(
            "message", //$NON-NLS-1$
            ""); //$NON-NLS-1$

        List<Object> data = new ArrayList<>();
        ocs.put("data", data); //$NON-NLS-1$

        Map<String, Object> fakeEntry = new HashMap<>();
        fakeEntry.put("share_type", -1); //$NON-NLS-1$
        // data.add(fakeEntry);

        try ( JsonGenerator gen = JF.createGenerator(resp.getOutputStream()) ) {
            gen.writeObject(res);
        }
    }


    /**
     * @param resp
     * @throws IOException
     */
    private static void genXMLShares ( HttpServletResponse resp ) throws IOException {
        String xml = "<?xml version=\"1.0\" ?><ocs>" + //$NON-NLS-1$
                "<meta><status>ok</status><statuscode>100</statuscode><message /></meta>" + //$NON-NLS-1$
                "<data></data></ocs>"; //$NON-NLS-1$
        resp.getWriter().write(xml);
    }

}
