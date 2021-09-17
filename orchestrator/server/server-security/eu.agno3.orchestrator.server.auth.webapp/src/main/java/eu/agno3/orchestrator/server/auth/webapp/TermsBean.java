/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.security.web.gui.terms.AbstractTermsBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "termsBean" )
public class TermsBean extends AbstractTermsBean {

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
            return "/termsView.xhtml?termsId=" + //$NON-NLS-1$
                    URLEncoder.encode(id, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            return null;
        }
    }

}
