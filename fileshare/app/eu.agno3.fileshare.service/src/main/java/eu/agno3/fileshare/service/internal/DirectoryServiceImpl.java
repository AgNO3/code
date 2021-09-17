/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Set;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidEntityException;
import eu.agno3.fileshare.exceptions.NamingConflictException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.DirectoryService;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;


/**
 * @author mbechler
 *
 */
@Component ( service = DirectoryService.class )
public class DirectoryServiceImpl implements DirectoryService {

    private DefaultServiceContext ctx;
    private AccessControlService accessControl;
    private BlockStorageService blockStore;
    private VFSServiceInternal vfs;
    private RecursiveModificationTimeTracker modTracker;


    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setBlockStorageService ( BlockStorageService bss ) {
        this.blockStore = bss;
    }


    protected synchronized void unsetBlockStorageService ( BlockStorageService bss ) {
        if ( this.blockStore == bss ) {
            this.blockStore = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        this.modTracker = rmt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        if ( this.modTracker == rmt ) {
            this.modTracker = null;
        }
    }


    @Override
    public VFSContainerEntity getById ( EntityKey id ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
            VFSContainerEntity e = v.load(id, VFSContainerEntity.class);
            this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            if ( e == null ) {
                throw new EntityNotFoundException();
            }
            return e.cloneShallow(true);
        }
    }


    @Override
    public VFSContainerEntity create ( EntityKey parentId, VFSContainerEntity directory ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE_FOLDER"); //$NON-NLS-1$
            audit.builder().entity(directory);

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(parentId).begin(tx) ) {
                VFSContainerEntity parent = v.load(parentId, VFSContainerEntity.class);
                audit.builder().parentEntity(parent);
                this.accessControl.checkAccess(v, parent, GrantPermission.UPLOAD);

                if ( parent == null ) {
                    throw new EntityNotFoundException("Parent directory does not exist"); //$NON-NLS-1$
                }

                audit.builder().parentEntity(parent);

                DateTime now = DateTime.now();
                checkDirectory(directory);
                checkConflict(v, parent, directory);
                directory.setOwner(parent.getOwner());
                directory.setAllowFileOverwrite(parent.getAllowFileOverwrite());
                directory.setSendNotifications(parent.getSendNotifications());
                User currentUser = this.accessControl.getCurrentUser();
                Grant g = this.accessControl.getTokenAuthGrant(v, directory);
                audit.builder().grant(g);
                v.addChild(parent, directory);
                ServiceUtil.setCreated(currentUser, now, directory, g);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, directory, g);
                directory.setSecurityLabel(ServiceUtil.deriveSecurityLabel(
                    tx,
                    this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration(),
                    parent,
                    currentUser != null ? currentUser.getSecurityLabel() : null));

                ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, parent, g);
                v.save(directory, parent);
                audit.builder().entity(directory);
                audit.builder().parentEntity(parent);
                v.refresh(directory, parent);
                v.commit();
                tx.commit();
                return directory.cloneShallow(true);
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create directory", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void setAllowFileOverwrite ( EntityKey id, boolean allowFileOverride ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(allowFileOverride ? "SET_ALLOW_FILEOVERRIDE" //$NON-NLS-1$
                    : "UNSET_ALLOWFILEOVERRIDE"); //$NON-NLS-1$
            try ( VFSContext v = this.vfs.getVFS(id).begin(false) ) {
                VFSContainerEntity container = v.load(id, VFSContainerEntity.class);
                audit.builder().entity(container);
                audit.builder().parentEntity(v.getParent(container));
                this.accessControl.checkOwner(v, container);
                if ( container == null ) {
                    throw new EntityNotFoundException();
                }
                container.setAllowFileOverwrite(allowFileOverride);
                v.save(container);
                v.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    @Override
    public void setSendNotifications ( EntityKey id, boolean sendNotifications ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(sendNotifications ? "SET_SENDNOTIFICATIONS" //$NON-NLS-1$
                    : "UNSET_SENDNOTIFICATIONS"); //$NON-NLS-1$
            try ( VFSContext v = this.vfs.getVFS(id).begin(false) ) {
                VFSContainerEntity container = v.load(id, VFSContainerEntity.class);
                audit.builder().entity(container);
                audit.builder().parentEntity(v.getParent(container));
                this.accessControl.checkOwner(v, container);
                if ( container == null ) {
                    throw new EntityNotFoundException();
                }
                container.setSendNotifications(sendNotifications);
                v.save(container);
                v.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    /**
     * @param directory
     * @throws InvalidEntityException
     * 
     */
    private static void checkDirectory ( VFSEntity directory ) throws InvalidEntityException {
        ServiceUtil.checkFileName(directory.getLocalName());
    }


    /**
     * @param directory
     * @throws FileshareException
     */
    private static void checkConflict ( VFSContext v, VFSContainerEntity parent, VFSContainerEntity directory ) throws FileshareException {
        Set<? extends VFSEntity> children = v.getChildren(parent);
        if ( children == null ) {
            return;
        }
        for ( VFSEntity e : children ) {
            if ( e.getLocalName().equals(directory.getLocalName()) ) {
                throw new NamingConflictException(e.getLocalName(), parent.getLocalName(), "Provided name does already exist"); //$NON-NLS-1$
            }
        }
    }

}
