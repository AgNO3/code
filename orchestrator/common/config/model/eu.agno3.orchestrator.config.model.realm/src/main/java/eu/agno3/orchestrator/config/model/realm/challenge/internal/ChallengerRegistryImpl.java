/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.challenge.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.realm.challenge.Challenger;
import eu.agno3.orchestrator.config.model.realm.challenge.ChallengerRegistry;


/**
 * @author mbechler
 *
 */
@Component ( service = ChallengerRegistry.class )
public class ChallengerRegistryImpl implements ChallengerRegistry {

    private MultiValuedMap<Class<? extends ConfigurationObject>, Challenger<? extends ConfigurationObject>> challengers = new HashSetValuedHashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindChallenger ( Challenger<?> val ) {
        this.challengers.put(val.getObjectType(), val);
    }


    protected synchronized void unbindChallenger ( Challenger<?> val ) {
        this.challengers.removeMapping(val.getObjectType(), val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ValidatorRegistry#getValidators(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public synchronized <T extends ConfigurationObject> List<Challenger<? super T>> getChallengers ( Class<T> type ) {
        List<Challenger<? super T>> res = new ArrayList<>();
        for ( Class<? extends ConfigurationObject> parentType : findParents(type) ) {
            Collection<Challenger<? extends ConfigurationObject>> typeValidators = this.challengers.get(parentType);

            if ( typeValidators != null ) {
                res.addAll((Collection<? extends Challenger<? super T>>) typeValidators);
            }
        }
        return res;
    }


    /**
     * @param type
     * @return the parent types
     */
    private static List<Class<? extends ConfigurationObject>> findParents ( Class<? extends ConfigurationObject> type ) {
        List<Class<? extends ConfigurationObject>> res = new ArrayList<>();
        Deque<Class<? extends ConfigurationObject>> toCheck = new LinkedList<>();
        toCheck.add(type);

        while ( !toCheck.isEmpty() ) {
            handleObjectClass(res, toCheck);
        }

        return res;
    }


    /**
     * @param res
     * @param toCheck
     */
    @SuppressWarnings ( "unchecked" )
    private static void handleObjectClass ( List<Class<? extends ConfigurationObject>> res, Deque<Class<? extends ConfigurationObject>> toCheck ) {
        Class<? extends ConfigurationObject> t = toCheck.pop();

        if ( t.getAnnotation(ObjectTypeName.class) != null ) {
            res.add(t);
        }
        Class<?>[] interfaces = t.getInterfaces();
        if ( interfaces != null ) {
            for ( Class<?> intf : interfaces ) {
                if ( !ConfigurationObject.class.isAssignableFrom(intf) ) {
                    continue;
                }
                toCheck.add((Class<? extends ConfigurationObject>) intf);
            }
        }
    }
}
