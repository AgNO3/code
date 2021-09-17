/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;


/**
 * @author mbechler
 *
 */
public class UserRootDAVNode extends EntityDAVNode {

    /**
     * 
     */
    public static final String USER_FILES_NAME = "user"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String USER_FILES_PATH = "/user"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String USER_FILES_DISPLAY_NAME = "My files"; //$NON-NLS-1$


    /**
     * @param parentKey
     * @param userRoot
     * @param layout
     */
    public UserRootDAVNode ( EntityKey parentKey, VFSContainerEntity userRoot, DAVLayout layout ) {
        super(userRoot, parentKey, layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#isOverrideResourceType()
     */
    @Override
    public boolean isOverrideResourceType () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> res = super.getExtraProperties(l);
        res.add(new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.USER_ROOT_RESOURCE_TYPE, Constants.NOT_SHAREABLE_RESOURCE_TYPE
        }));
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return USER_FILES_PATH;
    }


    @Override
    public String getDisplayName () {
        return USER_FILES_DISPLAY_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getPathName()
     */
    @Override
    public String getPathName () {
        return USER_FILES_NAME;
    }

}
