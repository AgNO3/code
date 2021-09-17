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
public class SubjectsDAVNode extends SubjectBaseNode {

    /**
     * 
     */
    public static final String SUBJECTS_PATH = "/.subjects"; //$NON-NLS-1$


    /**
     * @param layout
     */
    public SubjectsDAVNode ( DAVLayout layout ) {
        super(".subjects", layout); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return SUBJECTS_PATH;
    }

}
