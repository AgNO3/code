/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class PeerUtil {

    private static final Logger log = Logger.getLogger(PeerUtil.class);

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param peerString
     * @return peerInfo
     * @throws FileshareException
     */
    public PeerInfo decodePeerInfo ( String peerString ) throws FileshareException {
        if ( StringUtils.isBlank(peerString) ) {
            return null;
        }

        String peer = peerString;
        try {
            peer = URLDecoder.decode(peer, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            if ( log.isDebugEnabled() ) {
                log.warn("Failed to decode peer info", e); //$NON-NLS-1$
            }
            return null;
        }

        if ( peer.startsWith("subject-") ) { //$NON-NLS-1$
            String subjId = peer.substring(8);
            SubjectPeerInfo spi = new SubjectPeerInfo();
            spi.setSubject(this.fsp.getSubjectService().getSubjectInfo(UUID.fromString(subjId)));
            spi.setHaveSharedFrom(true);
            spi.setHaveSharedTo(true);
            return spi;
        }
        else if ( peer.startsWith("mail-") ) { //$NON-NLS-1$
            String mail = peer.substring(5);
            MailPeerInfo mpi = new MailPeerInfo();
            mpi.setMailAddress(mail);
            return mpi;
        }
        else if ( "token".equals(peer) ) { //$NON-NLS-1$
            return new TokenPeerInfo();
        }

        return null;
    }
}
