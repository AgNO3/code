/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TokenShare;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.types.uri.URIUtil;


/**
 * @author mbechler
 *
 */
@Named ( "tokenRedisplayBean" )
@ViewScoped
public class TokenRedisplayBean implements Serializable {

    private static final Logger log = Logger.getLogger(TokenRedisplayBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -3842306122020408776L;

    @Inject
    private FileshareServiceProvider fsp;

    private UUID cachedForGrant;
    private TokenShare cachedShare;


    /**
     * 
     * @param tg
     * @return the share object for the token grant
     */
    public TokenShare getShareFor ( TokenGrant tg ) {
        if ( this.cachedForGrant == null || !this.cachedForGrant.equals(tg.getId()) ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Loading share for " + tg); //$NON-NLS-1$
                }
                ShareProperties props = new ShareProperties();
                if ( !this.fsp.getConfigurationProvider().getFrontendConfiguration().isWebFrontendURIReliable() ) {
                    props.setOverrideBaseURI(URIUtil.getCurrentBaseUri());
                }
                this.cachedShare = this.fsp.getShareService().recreateTokenShare(tg, props);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                return null;
            }
            this.cachedForGrant = tg.getId();
        }
        return this.cachedShare;
    }
}
