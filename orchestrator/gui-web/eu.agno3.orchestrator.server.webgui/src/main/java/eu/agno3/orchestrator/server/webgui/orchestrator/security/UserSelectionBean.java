/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "orch_userSelectionBean" )
public class UserSelectionBean extends AbstractSelectionBean<@Nullable String, @Nullable UserPrincipal, AbstractModelException> {

    private static final Logger log = Logger.getLogger(UserSelectionBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -8274744888610804644L;

    private static final String UTF8 = "UTF-8"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.base.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected String getId ( @Nullable UserPrincipal obj ) {
        return encodeSingleSelection(obj);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#parseId(java.lang.String)
     */
    @Override
    protected @Nullable String parseId ( String id ) {
        return id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.base.AbstractSelectionBean#fetchObject(java.util.UUID)
     */
    @Override
    protected UserPrincipal fetchObject ( @Nullable String selection ) throws AbstractModelException {
        if ( selection == null ) {
            return null;
        }

        String[] parts = StringUtils.split(selection, '|');
        if ( parts.length != 3 ) {
            return null;
        }

        try {
            return new UserPrincipal(URLDecoder.decode(parts[ 1 ], UTF8), UUID.fromString(parts[ 2 ]), URLDecoder.decode(parts[ 0 ], UTF8));
        }
        catch ( UnsupportedEncodingException e ) {
            log.debug("Unsupported encoding", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        ExceptionHandler.handle(e);
    }


    public static final String encodeSingleSelection ( UserPrincipal obj ) {
        try {
            if ( obj == null ) {
                return null;
            }
            return URLEncoder.encode(String.format(
                "%s|%s|%s", //$NON-NLS-1$
                URLEncoder.encode(obj.getUserName(), UTF8),
                URLEncoder.encode(obj.getRealmName(), UTF8),
                obj.getUserId().toString()), UTF8);
        }
        catch ( UnsupportedEncodingException e ) {
            log.debug("Unsupported encoding", e); //$NON-NLS-1$
            return null;
        }
    }
}
