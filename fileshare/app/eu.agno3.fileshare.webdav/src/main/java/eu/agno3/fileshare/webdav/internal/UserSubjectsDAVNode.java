/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


/**
 * @author mbechler
 *
 */
public class UserSubjectsDAVNode extends SubjectBaseNode {

    /**
     * 
     */
    public static final String SUBJECTS_USERS = SubjectsDAVNode.SUBJECTS_PATH + "/users"; //$NON-NLS-1$ ss


    /**
     * @param l
     * 
     */
    public UserSubjectsDAVNode ( DAVLayout l ) {
        super("users", l); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return SUBJECTS_USERS; // $NON-NLS-1$
    }

}
