/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.challenge.ChallengeGenerationContext;


/**
 * @author mbechler
 * 
 */
public class ChallengeGeneratingReferenceVisitor extends AbstractReferenceVisitor {

    private final ModelObjectChallengeUtil challengeGenerator;
    private final List<ConfigApplyChallenge> challenges = new ArrayList<>();
    private ChallengeGenerationContext context;


    /**
     * @param challengeGenerator
     * @param context
     */
    public ChallengeGeneratingReferenceVisitor ( ModelObjectChallengeUtil challengeGenerator, ChallengeGenerationContext context ) {
        this.context = context;
        this.challengeGenerator = challengeGenerator;
    }


    @Override
    public void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException {
        this.challenges.addAll(this.challengeGenerator.generateSingleObject(this.context, val));
    }


    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object doGetReference )
            throws ModelServiceException, ModelObjectException {}


    /**
     * @return generated challenges
     */
    public List<ConfigApplyChallenge> getChallenges () {
        return this.challenges;
    }
}
