/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.EntityType;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSContainerChange;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public final class Util {

    private static final Logger log = Logger.getLogger(Util.class);


    private Util () {}


    /**
     * @param vc
     * @param vch
     * @param container
     * @return whether the deleted container was replaced by another entity
     */
    public static boolean wasReplacedLater ( VFSContext vc, VFSContainerChange vch, VFSContainerEntity container ) {
        boolean replaced = false;
        try {
            if ( vc.canResolveByName() ) {
                replaced = vc.resolveRelative(container, new String[] {
                    vch.getEntityName()
                }) != null;
            }
            else {
                for ( VFSEntity vfsEntity : vc.getChildren(container) ) {
                    if ( vch.getEntityName().equals(vfsEntity.getLocalName()) && typeMatches(vch.getEntityType(), vfsEntity.getEntityType()) ) {
                        replaced = true;
                        break;
                    }
                }
            }

            if ( replaced ) {
                log.debug(String.format(
                    "Resource was replaced %s @ %s", //$NON-NLS-1$
                    vch.getEntityName(),
                    vch.getContainer()));
            }
        }
        catch ( EntityNotFoundException e ) {
            log.trace("Entity not found", e); //$NON-NLS-1$
        }
        catch ( FileshareException e ) {
            log.debug("Failure looking up children", e); //$NON-NLS-1$
        }
        return replaced;
    }


    /**
     * @param entityType
     * @param entityType2
     * @return whether the type matches in a way so that the generated URL is the same
     */
    private static boolean typeMatches ( EntityType a, EntityType b ) {
        return ( a == EntityType.DIRECTORY && b == EntityType.DIRECTORY ) || ( a != EntityType.DIRECTORY && b != EntityType.DIRECTORY );
    }


    /**
     * @param dt
     * @param layout
     * @return the entity key for the root node
     * @throws FileshareException
     */
    public static EntityKey getRootId ( FileshareDAVTreeProviderInternal dt, DAVLayout layout ) throws FileshareException {
        if ( layout == DAVLayout.OWNCLOUD ) {
            ContainerEntity userRoot = dt.getBrowseService().getUserRoot();
            if ( userRoot != null ) {
                return userRoot.getEntityKey();
            }
        }
        return new NativeEntityKey(dt.getAccessControl().getCurrentUser().getId());
    }
}
