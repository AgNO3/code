/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.security.terms.TermsService;


/**
 * @author mbechler
 *
 */
public abstract class AbstractTermsBean implements TermsBean {

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private TermsService termsService;


    /**
     * @param id
     * @return
     */
    protected TermsDefinition getDefinition ( String id ) {
        return this.termsService.getTermsById(id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getContents(java.lang.String, java.lang.String,
     *      java.util.Locale)
     */
    @Override
    public URL getContents ( String id, String fmt, Locale l ) throws IOException {
        return this.termsService.getContents(id, fmt, l);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getContents(java.lang.String, java.lang.String)
     */
    @Override
    public URL getContents ( String id, String fmt ) throws IOException {
        return getContents(id, fmt, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getTermsLabel(java.lang.String)
     */
    @Override
    public String getTermsLabel ( String id ) {
        return getDefinition(id).getLabel(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getTermsDescription(java.lang.String)
     */
    @Override
    public String getTermsDescription ( String id ) {
        return getDefinition(id).getDescription(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getFirstUnaccepted()
     */
    @Override
    public TermsDefinition getFirstUnaccepted () {
        Collection<TermsDefinition> unacceptedTerms = getUnacceptedTerms();
        Iterator<TermsDefinition> td = unacceptedTerms.iterator();
        if ( td.hasNext() ) {
            return td.next();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getUnacceptedTerms()
     */
    @Override
    public Collection<TermsDefinition> getUnacceptedTerms () {
        return this.termsService.getRequiredTerms(getPrincipal());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#doAccept(java.lang.String)
     */
    @Override
    public void doAccept ( String termsId ) {
        this.termsService.markAccepted(getPrincipal(), termsId);
    }


    private static UserPrincipal getPrincipal () {
        Subject subject = SecurityUtils.getSubject();
        UserPrincipal up = null;
        if ( subject != null && subject.isAuthenticated() && subject.getPrincipals() != null ) {
            up = subject.getPrincipals().oneByType(UserPrincipal.class);
        }
        return up;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getUnacceptedRedirect()
     */
    @Override
    public String getUnacceptedRedirect () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.terms.TermsBean#getViewLocation(java.lang.String)
     */
    @Override
    public String getViewLocation ( String id ) {
        try {
            return "/auth/termsView.xhtml?termsId=" + //$NON-NLS-1$
                    URLEncoder.encode(id, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            return null;
        }
    }

}
