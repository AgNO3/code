/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.context;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;


/**
 * @author mbechler
 *
 */
public class ConfigApplyContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3835011610790609212L;
    private long revision;

    private List<ConfigApplyChallenge> challenges = new ArrayList<>();


    /**
     * @param l
     */
    public void setRevision ( long l ) {
        this.revision = l;
    }


    /**
     * @return the revision
     */
    public long getRevision () {
        return this.revision;
    }


    /**
     * @return the challenges
     */
    public List<ConfigApplyChallenge> getChallenges () {
        return this.challenges;
    }


    /**
     * @param challenges
     *            the challenges to set
     */
    public void setChallenges ( List<ConfigApplyChallenge> challenges ) {
        this.challenges = challenges;
    }
}
