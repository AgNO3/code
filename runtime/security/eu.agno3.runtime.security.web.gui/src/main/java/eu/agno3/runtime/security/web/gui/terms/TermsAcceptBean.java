/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.security.terms.TermsDefinition;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "agsec_termsAcceptBean" )
public class TermsAcceptBean extends AbstractTermsHandlerBean {

    /**
     * 
     */
    private static final long serialVersionUID = -8457050959658014412L;

    private static final Logger log = Logger.getLogger(TermsAcceptBean.class);

    String termId;
    private boolean accept;


    /**
     * @return the accept
     */
    public boolean getAccept () {
        return this.accept;
    }


    /**
     * @param accept
     *            the accept to set
     */
    public void setAccept ( boolean accept ) {
        this.accept = accept;
    }


    /**
     * @return
     */
    @Override
    protected String getTermsId () {
        if ( !this.termLoaded ) {
            this.termLoaded = true;
            TermsDefinition td = this.terms.getFirstUnaccepted();
            if ( td != null ) {
                this.termId = td.getId();
                if ( this.definition == null ) {
                    this.definition = td;
                }
            }
            else {
                this.definition = null;
            }
        }
        return this.termId;
    }


    /**
     * @return null, potential redirect to saved URL
     */
    public String doAccept () {
        if ( !this.accept ) {
            // msg
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Accepting " + this.getTermsId()); //$NON-NLS-1$
        }

        this.terms.doAccept(this.getTermsId());

        reset();

        TermsDefinition next = getDefinition();
        if ( next != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Continue with " + next.getId()); //$NON-NLS-1$
            }
            return null;
        }

        log.debug("All complete"); //$NON-NLS-1$
        return doRedirect();
    }


    private static String doRedirect () {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();
        SavedRequest origReq = WebUtils.getAndClearSavedRequest(req);
        if ( origReq != null && AccessControlFilter.GET_METHOD.equalsIgnoreCase(origReq.getMethod()) && origReq.getRequestUrl() != null ) {
            String reqUrl = origReq.getRequestUrl();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Original request URI is %s", //$NON-NLS-1$
                    reqUrl));
            }
            try {
                ec.redirect(reqUrl);
            }
            catch ( IOException e ) {
                log.warn("Redirection failed", e); //$NON-NLS-1$
            }
        }
        else {
            log.debug("No saved URL"); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * 
     */
    @Override
    protected void reset () {
        this.accept = false;
        this.termId = null;
        super.reset();
    }
}
