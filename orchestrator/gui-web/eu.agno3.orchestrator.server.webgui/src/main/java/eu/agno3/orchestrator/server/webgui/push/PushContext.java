/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.push;


import java.security.SecureRandom;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Hex;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "pushContext" )
public class PushContext {

    private static final String COMET_SESSION_ID = "comet.sessionId"; //$NON-NLS-1$
    private final SecureRandom rand = new SecureRandom();


    /**
     * 
     * @return the comet session id (stored in http session)
     */
    public synchronized String getCometSessionId () {

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

        String id = (String) session.getAttribute(COMET_SESSION_ID);

        if ( id == null ) {
            id = this.generateSessionId();
            session.setAttribute(COMET_SESSION_ID, id);
        }

        return id;
    }


    /**
     * @return
     */
    private String generateSessionId () {
        byte[] idBytes = new byte[16];
        this.rand.nextBytes(idBytes);
        return new String(Hex.encodeHex(idBytes));
    }
}
