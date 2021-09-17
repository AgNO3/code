/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.subject.PeerUtil;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "shareTabsBean" )
public class ShareTabsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7853364047821667637L;

    private String tab;
    private String peer;

    @Inject
    private PeerUtil peerUtil;

    private PeerInfo peerInfo;


    /**
     * @return the tab
     */
    public String getTab () {
        return this.tab;
    }


    /**
     * @param tab
     *            the tab to set
     */
    public void setTab ( String tab ) {
        this.tab = tab;
    }


    /**
     * @param peer
     *            the peer to set
     */
    public void setPeer ( String peer ) {
        this.peer = peer;
        if ( !StringUtils.isBlank(peer) ) {
            try {
                this.peerInfo = this.peerUtil.decodePeerInfo(peer);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
    }


    /**
     * @return the peer
     */
    public String getPeer () {
        return this.peer;
    }


    /**
     * @return the peerInfo
     */
    public PeerInfo getPeerInfo () {
        return this.peerInfo;
    }


    /**
     * 
     * @return null
     */
    public String subject () {
        setTab("subject"); //$NON-NLS-1$
        return null;
    }


    /**
     * 
     * @return null
     */
    public String mail () {
        setTab("mail"); //$NON-NLS-1$
        return null;
    }


    /**
     * 
     * @return null
     */
    public String link () {
        setTab("link"); //$NON-NLS-1$
        return null;
    }


    /**
     * 
     * @return null
     */
    public String cancel () {
        setTab(null);
        return null;
    }


    /**
     * 
     * @param pi
     * @return the tab so select for the given peer
     */
    public static String getTabFromPeerInfo ( PeerInfo pi ) {

        if ( pi instanceof SubjectPeerInfo ) {
            return "subject"; //$NON-NLS-1$
        }
        else if ( pi instanceof MailPeerInfo ) {
            return "mail"; //$NON-NLS-1$
        }
        else if ( pi instanceof TokenPeerInfo ) {
            return "link"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }
}
