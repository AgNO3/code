/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import java.io.IOException;
import java.io.StringReader;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.FactoryConfigurationError;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;


/**
 * @author mbechler
 *
 */
public class CasSingleLogoutFilter extends PathMatchingFilter {

    /**
     * 
     */
    private static final String SAMLP_NS = "urn:oasis:names:tc:SAML:2.0:protocol"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(CasSingleLogoutFilter.class);

    private XmlParserFactory xmlParserFactory;
    private AbstractCasRealm realm;


    /**
     * @param xpf
     * @param realm
     * 
     */
    public CasSingleLogoutFilter ( XmlParserFactory xpf, AbstractCasRealm realm ) {
        this.xmlParserFactory = xpf;
        this.realm = realm;
        setName("casSingleLogoutFilter"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#doFilterInternal(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws ServletException, IOException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        if ( "POST".equals(httpReq.getMethod()) ) { //$NON-NLS-1$
            log.debug("This seems to be a single logout request"); //$NON-NLS-1$

            StringBuffer buf = new StringBuffer();
            int c;

            while ( ( c = req.getReader().read() ) > 0 ) {
                buf.append((char) c);
            }

            String payload = buf.toString().substring("logoutRequest=".length()); //$NON-NLS-1$

            URLCodec codec = new URLCodec();

            String decoded;
            try {
                decoded = codec.decode(payload);
            }
            catch ( DecoderException e ) {
                log.warn("Failed to decode payload", e); //$NON-NLS-1$
                httpResp.setStatus(400);
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Logout request: " + decoded); //$NON-NLS-1$
            }

            try {
                DocumentBuilder db = this.xmlParserFactory.createDocumentBuilder();
                Document parsed = db.parse(new InputSource(new StringReader(decoded)));

                NodeList logoutRequest = parsed.getElementsByTagNameNS(SAMLP_NS, "LogoutRequest"); //$NON-NLS-1$
                if ( logoutRequest == null || logoutRequest.getLength() != 1 ) {
                    log.warn("Failed locate session ID element from SLO request"); //$NON-NLS-1$
                    httpResp.setStatus(400);
                    return;
                }

                Element logoutRequestElement = (Element) logoutRequest.item(0);
                NodeList sessionIndexElems = logoutRequestElement.getElementsByTagNameNS(SAMLP_NS, "SessionIndex"); //$NON-NLS-1$

                if ( sessionIndexElems == null || sessionIndexElems.getLength() != 1 ) {
                    log.warn("Failed locate session ID element from SLO request"); //$NON-NLS-1$
                    httpResp.setStatus(400);
                    return;
                }

                Element item = (Element) sessionIndexElems.item(0);
                String sessionTicketId = item.getTextContent();

                if ( sessionTicketId == null ) {
                    log.warn("Failed to extract session ID from SLO request"); //$NON-NLS-1$
                    httpResp.setStatus(400);
                    return;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Performing single logout for session id " + sessionTicketId); //$NON-NLS-1$
                }
                this.realm.doLogout(sessionTicketId, req, resp);
            }
            catch (
                FactoryConfigurationError |
                SAXException |
                XMLParserConfigurationException e ) {
                log.warn("Failed to parse SLO XML document", e); //$NON-NLS-1$
                httpResp.setStatus(500);
            }

            return;
        }

        chain.doFilter(req, resp);
    }
}
