/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.challenge;


import java.util.List;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public interface ChallengerRegistry {

    /**
     * @param type
     * @return the challengers that are applicable for this object type
     */
    <T extends ConfigurationObject> List<Challenger<? super T>> getChallengers ( Class<T> type );

}
