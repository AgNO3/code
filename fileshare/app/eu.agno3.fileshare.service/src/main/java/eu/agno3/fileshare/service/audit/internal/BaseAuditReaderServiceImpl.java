/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.fileshare.service.audit.internal;


import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.AuditReaderService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public abstract class BaseAuditReaderServiceImpl implements AuditReaderService {

    protected static final String MOVE_ENTITY_TYPE = "move-entity"; //$NON-NLS-1$
    protected static final String MULTI_ENTITY_TYPE = "multi-entity"; //$NON-NLS-1$
    protected static final String SINGLE_ENTITY_TYPE = "single-entity"; //$NON-NLS-1$
    protected static final String TYPE = "type"; //$NON-NLS-1$
    protected static final String TARGET_PARENT_ID = "targetParentId"; //$NON-NLS-1$
    protected static final String TARGET_ENTITY_ID = "targetEntityId"; //$NON-NLS-1$
    protected static final String TARGET_ENTITY_IDS = "targetEntityIds"; //$NON-NLS-1$
    protected static final String SOURCE_ENTITY_IDS = "sourceEntityIds"; //$NON-NLS-1$
    protected static final String SOURCE_ENTITY_PARENT_IDS = "sourceEntityParentIds"; //$NON-NLS-1$
    protected static final String TARGET_ID = "targetId"; //$NON-NLS-1$
    protected static final String ACTION = "action"; //$NON-NLS-1$

    /**
     * Actions for which events should be associated with the parent entity
     */
    public static final String[] INCLUDE_PARENT_ACTIONS = new String[] {
        "DOWNLOAD", //$NON-NLS-1$
        "DOWNLOAD_FOLDER", //$NON-NLS-1$
        "RENAME", //$NON-NLS-1$
        "DELETE", //$NON-NLS-1$
        "MOVE", //$NON-NLS-1$
        "CREATE", //$NON-NLS-1$
        "CREATE_FOLDER" //$NON-NLS-1$
    };
    protected static final int MAX_PAGESIZE = 100;

    private DefaultServiceContext sctx;

    private AccessControlService accessControl;

    private PolicyEvaluator policyEvaluator;
    private VFSServiceInternal vfs;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> properties ) {
        // unused
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
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
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        this.policyEvaluator = pe;
    }


    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        if ( this.policyEvaluator == pe ) {
            this.policyEvaluator = null;
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


    @Override
    public List<MapEvent> getAllEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset, int pageSize )
            throws FileshareException {

        if ( !isAvailable() ) {
            return Collections.EMPTY_LIST;
        }

        try ( VFSContext v = this.vfs.getVFS(entityId).begin(true) ) {
            VFSEntity persistent = v.load(entityId);
            this.accessControl.checkOwner(v, persistent);
            this.policyEvaluator.checkPolicy(v, persistent, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            if ( persistent == null ) {
                throw new EntityNotFoundException();
            }

            return fetchEntityEvents(entityId, start, end, filterActions, offset, pageSize);
        }
    }


    protected abstract List<MapEvent> fetchEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset,
            int pageSize ) throws FileshareException;


    @Override
    public long getEntityEventCount ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions ) throws FileshareException {
        if ( !isAvailable() ) {
            return 0;
        }
        try ( VFSContext v = this.vfs.getVFS(entityId).begin(true) ) {
            VFSEntity persistent = v.load(entityId);

            if ( entityId == null ) {
                throw new EntityNotFoundException();
            }
            this.accessControl.checkOwner(v, persistent);
            this.policyEvaluator.checkPolicy(v, persistent, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            if ( persistent == null ) {
                throw new EntityNotFoundException();
            }

            return countEntityEvents(entityId, start, end, filterActions);
        }
    }


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @return
     */
    protected abstract long countEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions )
            throws FileshareException;

}
