/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


/**
 * @author mbechler
 *
 */
public class TokenSubjectsDAVNode extends SubjectBaseNode {

    /**
     * 
     */
    public static final String SUBJECTS_TOKENS = SubjectsDAVNode.SUBJECTS_PATH + "/tokens"; //$NON-NLS-1$


    /**
     * @param layout
     */
    public TokenSubjectsDAVNode ( DAVLayout layout ) {
        super("tokens", layout); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return SUBJECTS_TOKENS;
    }

}
