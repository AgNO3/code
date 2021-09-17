/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public class AddMissingReferenceHandler extends AbstractReferenceReplacementHandler {

    private static final Logger log = Logger.getLogger(AddMissingReferenceHandler.class);

    private ObjectTypeRegistry reg;


    /**
     * @param reg
     */
    public AddMissingReferenceHandler ( ObjectTypeRegistry reg ) {
        this.reg = reg;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handleEmptyReference(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.beans.PropertyDescriptor)
     */
    @Override
    protected Object handleEmptyReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectNotFoundException {
        Class<? extends ConfigurationObject> refType = ObjectTypeUtil.findObjectType(property.getPropertyType());
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Empty Reference in %s.%s of type %s", obj.getType().getName(), property.getName(), refType)); //$NON-NLS-1$
        }
        ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>> otd = this.reg.getConcrete(refType);
        ConfigurationObject inst = otd.newInstance();
        if ( obj instanceof AbstractConfigurationObject && inst instanceof AbstractConfigurationObject
                && ctx instanceof PersistentReferenceWalkerContext ) {
            PersistentReferenceWalkerContext pctx = (PersistentReferenceWalkerContext) ctx;
            ObjectReferenceUtil.prepareObjectForPersist(
                pctx.getEnityManager(),
                pctx.getPersistenceUtil(),
                ( (AbstractConfigurationObject<?>) obj ).getAnchor(),
                (AbstractConfigurationObject<?>) inst);
            pctx.getEnityManager().persist(inst);
            pctx.getEnityManager().refresh(obj);
        }
        else {
            log.debug("Cannot persist new object"); //$NON-NLS-1$
        }
        return inst;

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handleReferenceCollection(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.util.Collection)
     */
    @Override
    protected List<ConfigurationObject> handleReferenceCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj,
            Collection<ConfigurationObject> value ) throws ModelObjectException, ModelServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace("Collection in " + obj.getType().getName()); //$NON-NLS-1$
        }
        if ( ctx instanceof PersistentReferenceWalkerContext ) {
            PersistentReferenceWalkerContext pctx = (PersistentReferenceWalkerContext) ctx;
            for ( ConfigurationObject cfg : value ) {
                if ( value instanceof AbstractConfigurationObject<?> ) {
                    ObjectReferenceUtil
                            .fillInMissing(pctx.getEnityManager(), pctx.getPersistenceUtil(), (AbstractConfigurationObject<?>) cfg, this.reg);
                }
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePrimitiveReference(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject, java.beans.PropertyDescriptor)
     */
    @Override
    protected Object handlePrimitiveReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, ConfigurationObject value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {

        if ( value instanceof AbstractConfigurationObject<?> && ctx instanceof PersistentReferenceWalkerContext ) {
            PersistentReferenceWalkerContext pctx = (PersistentReferenceWalkerContext) ctx;
            ObjectReferenceUtil.fillInMissing(pctx.getEnityManager(), pctx.getPersistenceUtil(), (AbstractConfigurationObject<?>) value, this.reg);
        }

        return null;
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
        return null;
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
        return null;
    }

}
