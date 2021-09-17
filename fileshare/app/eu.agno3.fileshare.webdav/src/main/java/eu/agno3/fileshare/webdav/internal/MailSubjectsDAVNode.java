/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


/**
 * @author mbechler
 *
 */
public class MailSubjectsDAVNode extends SubjectBaseNode {

    /**
     * 
     */
    public static final String SUBJECTS_MAIL = SubjectsDAVNode.SUBJECTS_PATH + "/mail"; //$NON-NLS-1$


    /**
     * @param l
     * 
     */
    public MailSubjectsDAVNode ( DAVLayout l ) {
        super("mail", l); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return SUBJECTS_MAIL;
    }

}
