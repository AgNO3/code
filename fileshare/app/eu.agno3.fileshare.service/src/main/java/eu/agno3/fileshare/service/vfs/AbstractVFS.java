/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public class AbstractVFS {

    private static final Logger log = Logger.getLogger(AbstractVFS.class);

    private String id;
    private SecurityLabel label;
    private VirtualGroup group;
    private boolean readOnly = true;
    private DefaultServiceContext sctx;
    private boolean initialized;
    private Tika tika = new Tika();
    private Set<EntityKey> mappedEntityNegativeCache = new HashSet<>();

    private boolean sharable;

    private String groupName;


    /**
     * 
     */
    public AbstractVFS () {
        super();
    }


    @Reference
    protected synchronized void setDefaultServiceContext ( DefaultServiceContext dsc ) {
        this.sctx = dsc;
    }


    protected synchronized void unsetDefaultServiceContext ( DefaultServiceContext dsc ) {
        if ( this.sctx == dsc ) {
            this.sctx = null;
        }
    }


    /**
     * @param ctx
     */
    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.id = ConfigUtil.parseString(ctx.getProperties(), "instanceId", null); //$NON-NLS-1$
        this.groupName = ConfigUtil.parseString(ctx.getProperties(), "name", this.id); //$NON-NLS-1$
        this.sharable = ConfigUtil.parseBoolean(ctx.getProperties(), "sharable", false); //$NON-NLS-1$
        String labelId = ConfigUtil.parseString(ctx.getProperties(), "securityLabel", null); //$NON-NLS-1$

        try {
            if ( !StringUtils.isBlank(this.id) ) {
                initialize(labelId);
            }
            else {
                log.error("No VFS id configureed"); //$NON-NLS-1$
            }

            this.initialized = true;
        }
        catch ( Exception e ) {
            log.error("Failed to create VFS group", e); //$NON-NLS-1$
        }

    }


    /**
     * @return the group
     */
    public VirtualGroup getGroup () {
        return this.group;
    }


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @return the label
     */
    public SecurityLabel getLabel () {
        return this.label;
    }


    /**
     * @return the initialized
     */
    public boolean isInitialized () {
        return this.initialized;
    }


    /**
     * @return the readOnly
     */
    public boolean isReadOnly () {
        return this.readOnly;
    }


    /**
     * 
     * @return the service context
     */
    public DefaultServiceContext getServiceContext () {
        return this.sctx;
    }


    /**
     * @param v
     * @return the detected mime type
     */
    public String detectFileType ( VFSFileEntity v ) {
        return this.tika.detect(v.getLocalName());
    }


    /**
     * @return the mappedEntityNegativeCache
     */
    public Set<EntityKey> getMappedEntityNegativeCache () {
        return this.mappedEntityNegativeCache;
    }


    /**
     * @param ctx
     */
    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.initialized = false;
    }


    /**
     * @throws EntityTransactionException
     * 
     */
    private void initialize ( String l ) throws EntityTransactionException {
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            TypedQuery<VirtualGroup> q = em.createQuery("SELECT g FROM VirtualGroup g WHERE vfs = :vfsId", VirtualGroup.class); //$NON-NLS-1$
            q.setParameter("vfsId", this.id); //$NON-NLS-1$

            VirtualGroup g;
            List<VirtualGroup> res = q.getResultList();
            if ( res.isEmpty() ) {
                g = new VirtualGroup();
                g.setId(UUID.randomUUID());
                g.setCreated(DateTime.now());
                g.setLastModified(DateTime.now());
                g.setRealm("VIRTUAL"); //$NON-NLS-1$
                g.setName(this.groupName);
                g.setVfs(this.id);
                log.info("Initializing VFS group for " + this.id); //$NON-NLS-1$
                em.persist(g);
                em.flush();
                em.refresh(g);
            }
            else {
                g = res.get(0);
            }

            this.group = g;

            this.label = l != null ? ServiceUtil.getOrCreateSecurityLabel(tx, l)
                    : ServiceUtil
                            .getOrCreateSecurityLabel(tx, this.sctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefaultLabel());

            tx.commit();
        }
    }


    /**
     * @return whether the entities here should be sharable
     */
    public boolean isSharable () {
        return this.sharable;
    }

}