/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.challenge;


import java.util.Collection;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface Challenger <T extends ConfigurationObject> {

    /**
     * 
     * @return the object type under validation
     */
    Class<T> getObjectType ();


    /**
     * @param ctx
     * @param obj
     * @return generated challenges
     */
    Collection<ConfigApplyChallenge> generate ( ChallengeGenerationContext ctx, T obj );
}
