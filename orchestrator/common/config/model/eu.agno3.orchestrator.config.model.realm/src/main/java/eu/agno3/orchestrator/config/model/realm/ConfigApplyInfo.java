/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class ConfigApplyInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4668987776664356210L;
    boolean force;
    private List<ConfigApplyChallenge> challengeResponses = new ArrayList<>();


    /**
     * @return whether to force re-application of config
     */
    public boolean getForce () {
        return this.force;
    }


    /**
     * @return whether to force re-application of config
     */
    public boolean isForce () {
        return this.force;
    }


    /**
     * @param force
     *            the force to set
     */
    public void setForce ( boolean force ) {
        this.force = force;
    }


    /**
     * @return the challenges
     */
    public List<ConfigApplyChallenge> getChallengeResponses () {
        return this.challengeResponses;
    }


    /**
     * @param challenges
     *            the challenges to set
     */
    public void setChallengeResponses ( List<ConfigApplyChallenge> challenges ) {
        this.challengeResponses = challenges;
    }

}
