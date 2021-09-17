/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationEntryImpl;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
public class ObjectValidationContextImpl implements ObjectValidationContext {

    private static final Logger log = Logger.getLogger(ObjectValidationContextImpl.class);

    private ObjectValidationContextImpl parent;
    private boolean hasErrors = false;
    private Collection<ViolationEntry> violations;
    private Stack<String> path = new Stack<>();
    private String objectType;
    private ConfigurationObject obj;
    private boolean abst;

    private Map<ServiceStructuralObject, ConfigurationInstance> contextServices;


    /**
     * @param obj
     * @param abstr
     * @param entryCollection
     * @param contextServices
     * 
     */
    public ObjectValidationContextImpl ( ConfigurationObject obj, boolean abstr, Collection<ViolationEntry> entryCollection,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) {
        this.obj = obj;
        this.abst = abstr;
        this.contextServices = contextServices;
        if ( obj.getId() != null ) {
            this.path.push("obj:" + obj.getId().toString()); //$NON-NLS-1$
        }
        this.objectType = ObjectTypeUtil.getObjectType(obj);
        this.violations = entryCollection;
    }


    /**
     * 
     */
    private ObjectValidationContextImpl ( ObjectValidationContextImpl parent, ConfigurationObject obj, List<String> localPath,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) {
        this.obj = obj;
        this.contextServices = contextServices;
        this.path.addAll(parent.path);
        if ( localPath != null ) {
            this.path.addAll(localPath);
        }
        this.parent = parent;
        this.objectType = ObjectTypeUtil.getObjectType(obj);
        this.abst = parent.abst;
    }


    /**
     * @return the obj
     */
    public ConfigurationObject getObj () {
        return this.obj;
    }


    /**
     * @return the path
     */
    public Stack<String> getPath () {
        return this.path;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext#hasErrors()
     */
    @Override
    public boolean hasErrors () {
        return this.hasErrors;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext#isAbstract()
     */
    @Override
    public boolean isAbstract () {
        return this.abst;
    }


    /**
     * @param type
     * @return a parent object of this type
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends ConfigurationObject> Optional<T> findParent ( Class<T> type ) {
        if ( this.obj != null && type.isAssignableFrom(this.obj.getClass()) ) {
            return Optional.of((T) this.obj);
        }

        if ( this.parent != null ) {
            return this.parent.findParent(type);
        }

        return Optional.empty();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext#findContext(java.lang.Class,
     *      java.lang.String)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationInstance> Optional<T> findContext ( Class<T> type, String service ) {
        for ( Entry<ServiceStructuralObject, ConfigurationInstance> e : this.contextServices.entrySet() ) {

            if ( e.getKey().getServiceType().equals(service) ) {
                return Optional.of((T) e.getValue());
            }

        }
        return Optional.empty();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext#addViolation(java.lang.String,
     *      eu.agno3.orchestrator.config.model.validation.ViolationLevel, java.lang.Object[])
     */
    @Override
    public void addViolation ( String msgKey, ViolationLevel level, Object... msgArgs ) {

        this.addEntry(new ViolationEntryImpl(level, this.objectType, new ArrayList<>(this.path), msgKey, convertArgs(msgArgs)));
    }


    /**
     * @param e
     */
    private void addEntry ( ViolationEntryImpl e ) {

        if ( e.getLevel() == ViolationLevel.ERROR ) {
            this.hasErrors = true;
        }

        if ( this.parent != null ) {
            this.parent.addEntry(e);
        }
        else {
            if ( this.violations.add(e) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Adding violation %s at %s (%s)", e.getMessageTemplate(), e.getPath(), e.getObjectType())); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * @param msgArgs
     * @return
     */
    private static List<String> convertArgs ( Object[] msgArgs ) {
        List<String> args = new ArrayList<>();
        for ( Object obj : msgArgs ) {
            args.add(obj.toString());
        }
        return args;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ObjectValidationContext#addViolation(java.lang.String,
     *      java.lang.String, eu.agno3.orchestrator.config.model.validation.ViolationLevel, java.lang.Object[])
     */
    @Override
    public void addViolation ( String msgKey, String objectPath, ViolationLevel level, Object... msgArgs ) {
        Stack<String> fullPath = new Stack<>();
        for ( String pathElem : this.path ) {
            fullPath.push(pathElem);
        }
        for ( String prop : StringUtils.split(objectPath, '/') ) {
            fullPath.push(prop);
        }

        this.addEntry(new ViolationEntryImpl(level, this.objectType, new ArrayList<>(fullPath), msgKey, convertArgs(msgArgs)));
    }


    /**
     * @param name
     * @param refObj
     * @return a sub context for the reference
     */
    public ObjectValidationContextImpl makeReferenceContext ( String name, ConfigurationObject refObj ) {
        return new ObjectValidationContextImpl(this, refObj, name == null ? null : Arrays.asList(StringUtils.split(name, '/')), this.contextServices);
    }


    /**
     * @param collectionName
     * @param cfgItm
     * @return a sub context for the reference
     */
    public ObjectValidationContextImpl makeCollectionReferenceContext ( String collectionName, ConfigurationObject cfgItm ) {
        List<String> itmPath = new ArrayList<>();
        if ( collectionName != null ) {
            itmPath.addAll(Arrays.asList(StringUtils.split(collectionName, '/')));
        }
        itmPath.add("col:" + cfgItm.getId()); //$NON-NLS-1$
        return new ObjectValidationContextImpl(this, cfgItm, itmPath, this.contextServices);
    }

}
