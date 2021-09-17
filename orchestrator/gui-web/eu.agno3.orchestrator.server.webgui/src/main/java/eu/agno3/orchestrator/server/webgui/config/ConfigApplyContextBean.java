/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.util.List;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;


/**
 * @author mbechler
 *
 */
public interface ConfigApplyContextBean extends Serializable {

    /**
     * @return revision to apply
     */
    Long getRevision ();


    /**
     * @return challenges
     */
    List<ConfigApplyChallenge> getChallenges ();


    /**
     * @return apply context
     */
    ConfigApplyContext getApplyContext ();


    /**
     * @return apply info
     */
    ConfigApplyInfo getApplyInfo ();


    /**
     * @return whether challenges were processed successfully
     * 
     */
    boolean handleChallenges ();

}
