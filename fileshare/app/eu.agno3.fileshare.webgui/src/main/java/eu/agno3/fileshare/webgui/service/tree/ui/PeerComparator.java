/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.webgui.service.file.compare.FileSortHelpers;


/**
 * @author mbechler
 *
 */
public class PeerComparator implements Comparator<PeerInfo>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6722669702739112653L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( PeerInfo o1, PeerInfo o2 ) {

        if ( o1 instanceof SubjectPeerInfo && o2 instanceof SubjectPeerInfo ) {
            return FileSortHelpers.sortBySubject( ( (SubjectPeerInfo) o1 ).getSubject(), ( (SubjectPeerInfo) o2 ).getSubject());
        }
        else if ( o1 instanceof SubjectPeerInfo ) {
            return -1;
        }
        else if ( o2 instanceof SubjectPeerInfo ) {
            return 1;
        }
        else if ( o1 instanceof MailPeerInfo && o2 instanceof MailPeerInfo ) {
            return ( (MailPeerInfo) o1 ).getMailAddress().compareTo( ( (MailPeerInfo) o2 ).getMailAddress());
        }
        else if ( o1 instanceof MailPeerInfo ) {
            return -1;
        }
        else if ( o2 instanceof MailPeerInfo ) {
            return 1;
        }

        return 0;
    }

}
