/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.types.DeduplicatedGlobal;


class ReferenceReplacementHandler extends AbstractReferenceReplacementHandler implements ReferenceHandler {

    private static final Logger log = Logger.getLogger(ReferenceReplacementHandler.class);

    @NonNull
    private EntityManager em;

    @NonNull
    private PersistenceUtil pu;

    @NonNull
    private ReplacementStrategy strategy;


    /**
     * @param em
     * @param pu
     * @param strategy
     */
    public ReferenceReplacementHandler ( @NonNull EntityManager em, @NonNull PersistenceUtil pu, @NonNull ReplacementStrategy strategy ) {
        this.em = em;
        this.pu = pu;
        this.strategy = strategy;
    }


    @Override
    protected Object handlePrimitiveReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, ConfigurationObject value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {
        ConfigurationObject outerInherits = null;

        if ( obj.getInherits() != null ) {
            outerInherits = (ConfigurationObject) doGetReference(obj.getInherits(), property.getReadMethod());
        }

        if ( value.getId() == null || ( ( value.getVersion() == null || value.getVersion() == 0 ) && value.getRevision() == null ) ) {
            // is not yet persisted
            return this.strategy.handleUnpersistedRef(this.em, this.pu, (AbstractConfigurationObject<?>) obj, value, outerInherits, false);
        }

        // is persisted
        return this.strategy.handlePersistedRef(this.em, this.pu, (AbstractConfigurationObject<?>) obj, value, outerInherits, false);
    }


    @Override
    protected List<ConfigurationObject> handleReferenceCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj,
            Collection<ConfigurationObject> value ) throws ModelObjectException, ModelServiceException {

        List<ConfigurationObject> replaceWithCollection = new LinkedList<>();

        for ( ConfigurationObject refObj : value ) {
            if ( refObj.getId() == null || ( ( refObj.getVersion() == null || refObj.getVersion() == 0 ) && refObj.getRevision() == null ) ) {
                // is not yet persisted
                replaceWithCollection
                        .add(this.strategy.handleUnpersistedRef(this.em, this.pu, (AbstractConfigurationObject<?>) obj, refObj, null, true));
            }
            else {
                // is persisted
                replaceWithCollection
                        .add(this.strategy.handlePersistedRef(this.em, this.pu, (AbstractConfigurationObject<?>) obj, refObj, null, true));
            }
        }
        return replaceWithCollection;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePropertyCollection(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.util.Collection)
     */
    @Override
    protected Collection<Object> handlePropertyCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj, Collection<Object> value )
            throws ModelObjectException, ModelServiceException {
        List<Object> ret = new LinkedList<>();
        for ( Object o : value ) {
            if ( o instanceof DeduplicatedGlobal ) {
                ret.add(this.replaceWithDeduplicatedInstance((DeduplicatedGlobal) o));
            }
            else {
                ret.add(o);
            }
        }
        return ret;
    }


    /**
     * @param o
     * @return
     */
    private DeduplicatedGlobal replaceWithDeduplicatedInstance ( DeduplicatedGlobal o ) {
        DeduplicatedGlobal found = this.em.find(o.getClass(), o.getDerivedId());
        if ( found == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating DeduplicatedGlobal instance " + o); //$NON-NLS-1$
            }
            this.em.persist(o);
            return o;
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Using existing DeduplicatedGlobal instance %s", //$NON-NLS-1$
                found));
        }

        if ( found.getVersion() != o.getVersion() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Deduplicated has new version, replacing " + found); //$NON-NLS-1$
            }
            found.replace(o);
            this.em.persist(found);
        }

        return found;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePrimitiveProperty(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.lang.Object, java.beans.PropertyDescriptor)
     */
    @Override
    protected Object handlePrimitiveProperty ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        if ( value instanceof DeduplicatedGlobal ) {
            return this.replaceWithDeduplicatedInstance((DeduplicatedGlobal) value);
        }
        return value;
    }
}