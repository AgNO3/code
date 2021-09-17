/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.config.AgentConfigRequest;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.agent.server.data.AgentStateCacheEntry;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.server.base.component.AbstractComponentConnectorWatcher;
import eu.agno3.orchestrator.server.base.component.ComponentCertificateTracker;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.runtime.transaction.TransactionContext;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    AgentConnectorWatcherImpl.class, AgentConnectorWatcher.class
} )
public class AgentConnectorWatcherImpl extends AbstractComponentConnectorWatcher<AgentConfig> implements AgentConnectorWatcher {

    private static final Logger log = Logger.getLogger(AgentConnectorWatcherImpl.class);
    private DefaultServerServiceContext sctx;
    private ComponentCertificateTracker certificateTracker;


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindAgentLifecycleListener ( AgentLifecycleListener listener ) {
        bindLifecycleListener(listener);
    }


    protected synchronized void unbindAgentLifecycleListener ( AgentLifecycleListener listener ) {
        unbindLifecycleListener(listener);
    }


    @Reference
    protected synchronized void setAgentConfigProvider ( AgentConfigurationProvider prov ) {
        setConfigProvider(prov);
    }


    protected synchronized void unsetAgentConfigProvider ( AgentConfigurationProvider prov ) {
        unsetConfigProvider(prov);
    }


    @Reference
    protected synchronized void setCertificateTracker ( ComponentCertificateTracker ct ) {
        this.certificateTracker = ct;
    }


    protected synchronized void unsetCertificateTracker ( ComponentCertificateTracker ct ) {
        if ( this.certificateTracker == ct ) {
            this.certificateTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractComponentConnectorWatcher#getComponentConnectorState(java.util.UUID)
     */
    @Override
    public ComponentState getComponentConnectorState ( @NonNull UUID componentId ) {
        ComponentState componentConnectorState = super.getComponentConnectorState(componentId);
        if ( componentConnectorState != ComponentState.UNKNOWN ) {
            return componentConnectorState;
        }

        EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
        AgentStateCacheEntry ce = getAgentStateCache(em, componentId);
        if ( ce == null ) {
            return ComponentState.UNKNOWN;
        }
        return componentConnectorState;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.server.AgentConnectorWatcher#getActiveComponents()
     */
    @Override
    public Map<UUID, AgentInfo> getActiveComponents () {
        Set<@NonNull UUID> activeComponentIds = super.getActiveComponentIds();
        Map<UUID, AgentInfo> infos = new HashMap<>();

        if ( activeComponentIds == null || activeComponentIds.isEmpty() ) {
            return infos;
        }

        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
            TypedQuery<AgentStateCacheEntry> q = em
                    .createQuery("SELECT s FROM AgentStateCacheEntry s WHERE agentId IN :componentIds", AgentStateCacheEntry.class); //$NON-NLS-1$
            q.setParameter("componentIds", activeComponentIds); //$NON-NLS-1$

            for ( AgentStateCacheEntry e : q.getResultList() ) {
                AgentInfo makeAgentInfo = makeAgentInfo(e);
                if ( makeAgentInfo != null ) {
                    infos.put(e.getAgentId(), makeAgentInfo);
                }
            }
        }

        for ( UUID compId : activeComponentIds ) {
            if ( !infos.containsKey(compId) ) {
                AgentInfo ai = new AgentInfo();
                ai.setComponentId(compId);
                ai.setState(getComponentConnectorState(compId));
                infos.put(compId, ai);
            }
        }
        return infos;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.server.AgentConnectorWatcher#getAgentInfo(java.util.UUID)
     */
    @Override
    public AgentInfo getAgentInfo ( UUID agentId ) {
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
            TypedQuery<AgentStateCacheEntry> q = em
                    .createQuery("SELECT s FROM AgentStateCacheEntry s WHERE agentId = :agentId", AgentStateCacheEntry.class); //$NON-NLS-1$
            q.setParameter("agentId", agentId); //$NON-NLS-1$
            List<AgentStateCacheEntry> resultList = q.getResultList();
            if ( resultList.isEmpty() ) {
                return null;
            }
            return makeAgentInfo(resultList.get(0));
        }
    }


    /**
     * @param e
     * @return
     */
    private AgentInfo makeAgentInfo ( AgentStateCacheEntry e ) {
        AgentInfo ai = new AgentInfo();
        UUID agentId = e.getAgentId();
        if ( agentId == null ) {
            return null;
        }
        ai.setComponentId(agentId);
        ai.setState(getComponentConnectorState(agentId));
        ai.setProvisionState(e.getProvisionState());
        ai.setImageType(e.getImageType());
        ai.setCertificate(e.getCertificate());

        ai.setLastKnownHostName(e.getLastKnownHostName());
        ai.setLastKnownAddress(e.getLastKnownAddr());
        return ai;
    }


    /**
     * @param em2
     * @param componentId
     * @return
     */
    private static AgentStateCacheEntry getAgentStateCache ( EntityManager em, UUID componentId ) {
        return em.find(AgentStateCacheEntry.class, componentId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractComponentConnectorWatcher#setComponentState(java.util.UUID,
     *      eu.agno3.orchestrator.server.component.ComponentState)
     */
    @Override
    protected synchronized void setComponentState ( @NonNull UUID componentId, ComponentState state ) {
        super.setComponentState(componentId, state);
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
            AgentStateCacheEntry ce = getAgentStateCache(em, componentId);

            if ( ce == null ) {
                ce = new AgentStateCacheEntry();
                ce.setAgentId(componentId);
            }
            if ( ce.getCachedState() != state ) {
                ce.setCachedState(state);
                ce.setLastStateChange(DateTime.now());
            }
            em.persist(ce);
            em.flush();
            tx.commit();
        }
    }


    /**
     * @param componentId
     * @param msg
     */
    public synchronized void haveConfig ( @NonNull UUID componentId, @NonNull AgentConfigRequest msg ) {
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
            AgentStateCacheEntry ce = getAgentStateCache(em, componentId);

            if ( ce == null ) {
                ce = new AgentStateCacheEntry();
                ce.setAgentId(componentId);
            }
            ce.setImageType(msg.getImageType());
            ce.setLastKnownAddr(msg.getHostAddress());
            ce.setLastKnownHostName(msg.getHostName());
            ce.setLastStateChange(DateTime.now());
            X509Certificate cert = this.certificateTracker.getComponentCertificate(new AgentComponentPrincipal(componentId));
            if ( cert != null ) {
                ce.setCertificate(cert);
            }
            else {
                log.debug("Do not have a certificate for the agent"); //$NON-NLS-1$
            }
            ce.setCachedState(ComponentState.CONNECTING);
            em.persist(ce);
            em.flush();
            tx.commit();
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.component.AbstractComponentConnectorWatcher#newComponent(java.util.UUID)
     */
    @Override
    protected void newComponent ( @NonNull UUID componentId ) {

        EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
        if ( getAgentStateCache(em, componentId) != null ) {
            return;
        }

        if ( log.isInfoEnabled() ) {
            log.info("New agent connecting: " + componentId); //$NON-NLS-1$
        }
        super.newComponent(componentId);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.component.AbstractComponentConnectorWatcher#timeout(java.util.UUID)
     */
    @Override
    public void timeout ( @NonNull UUID componentId ) {
        log.warn(String.format("Agent %s has timed out", componentId)); //$NON-NLS-1$
        super.timeout(componentId);
    }

}
