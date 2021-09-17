/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.model.shortcut.Shortcut;
import eu.agno3.fileshare.model.shortcut.ShortcutType;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ShortcutService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.FlaggingServiceInternal;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
@Component ( service = ShortcutService.class )
public class ShortcutServiceImpl implements ShortcutService {

    private static final Logger log = Logger.getLogger(ShortcutServiceImpl.class);

    private DefaultServiceContext ctx;
    private AccessControlService accessControl;
    private FlaggingServiceInternal flaggingService;
    private VFSServiceInternal vfs;


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
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference
    protected synchronized void setFlaggingService ( FlaggingServiceInternal fs ) {
        this.flaggingService = fs;
    }


    protected synchronized void unsetFlaggingService ( FlaggingServiceInternal fs ) {
        if ( this.flaggingService == fs ) {
            this.flaggingService = null;
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


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.ShortcutService#getUserShortcuts()
     */
    @Override
    public List<Shortcut> getUserShortcuts () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            List<Shortcut> res = new LinkedList<>();
            addSubjectShorts(tx, res);
            addEntityShortcuts(tx, res);
            addMailShortcuts(tx, res);
            addLinkShortcuts(tx, res);
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user shortcuts", e); //$NON-NLS-1$
        }
    }


    /**
     * @param res
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    private void addLinkShortcuts ( EntityTransactionContext tx, List<Shortcut> res ) throws AuthenticationException, UserNotFoundException {
        User currentUser = this.accessControl.getCurrentUser(tx);
        if ( currentUser != null ) {
            if ( currentUser.getLinksFavorite() ) {
                Shortcut sc = new Shortcut();
                sc.setType(ShortcutType.PEER_LINK);
                res.add(sc);
            }
        }
    }


    /**
     * @param res
     * @throws FileshareException
     */
    private void addMailShortcuts ( EntityTransactionContext tx, List<Shortcut> res ) throws FileshareException {
        for ( String mailAddress : this.flaggingService.getFavoriteMails(tx) ) {
            Shortcut sc = new Shortcut();
            sc.setType(ShortcutType.PEER_MAIL);
            sc.setLabel(mailAddress);
            res.add(sc);
        }
    }


    /**
     * @param res
     * @throws FileshareException
     * @throws AuthenticationException
     */
    private void addEntityShortcuts ( EntityTransactionContext tx, List<Shortcut> res ) throws FileshareException, AuthenticationException {
        for ( VFSEntity e : this.flaggingService.getFavoriteEntities(tx) ) {
            if ( e instanceof VFSFileEntity ) {
                continue;
            }

            Shortcut sc = new Shortcut();
            sc.setType(ShortcutType.FAVORITE);
            sc.setLabel(e.getLocalName());
            try ( VFSContext v = this.vfs.getVFS(e.getEntityKey()).begin(tx) ) {
                if ( !this.accessControl.isOwner(v, e) ) {
                    if ( !this.accessControl.hasAccess(v, e, GrantPermission.READ) ) {
                        continue;
                    }
                    Grant g = this.accessControl.getAnySubjectGrant(v, e);

                    if ( g != null ) {
                        sc.setGrantId(g.getId());
                    }
                    else {
                        g = this.accessControl.getTokenAuthGrant(v, e);
                        if ( g != null ) {
                            sc.setGrantId(g.getId());
                        }
                    }
                }
                else if ( e.getOwner() instanceof Group ) {
                    sc.setFromGroup(true);
                }
                sc.setTargetId(e.getEntityKey());
                res.add(sc);
            }
            catch ( EntityNotFoundException ex ) {
                log.warn("Failed to get shortcut entity " + e, ex); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param res
     * @throws FileshareException
     */
    private void addSubjectShorts ( EntityTransactionContext tx, List<Shortcut> res ) throws FileshareException {
        for ( Subject s : this.flaggingService.getFavoriteSubjects(tx) ) {
            Shortcut sc = new Shortcut();
            sc.setTargetId(new NativeEntityKey(s.getId()));
            sc.setSubjectId(s.getId());

            if ( s instanceof User ) {
                sc.setLabel( ( (User) s ).getUserDisplayName());
                sc.setType(ShortcutType.PEER);
            }
            else if ( s instanceof Group ) {
                sc.setLabel( ( (Group) s ).getName());
                if ( this.accessControl.isMember((Group) s) ) {
                    if ( s instanceof VirtualGroup ) {
                        try ( VFSContext v = this.vfs.getVFS( ( (VirtualGroup) s ).getVfs()).begin(tx) ) {
                            sc.setTargetId(v.getRoot().getEntityKey());
                        }
                        catch ( FileshareException e ) {
                            log.warn("Failed to get group root", e); //$NON-NLS-1$
                            continue;
                        }
                    }
                    else {
                        if ( s.getSubjectRoot() == null ) {
                            continue;
                        }
                        sc.setTargetId(s.getSubjectRoot().getEntityKey());
                    }
                    sc.setType(ShortcutType.MEMBER_GROUP);
                }
                else {
                    sc.setType(ShortcutType.PEER_GROUP);
                }
            }
            res.add(sc);
        }
    }
}
