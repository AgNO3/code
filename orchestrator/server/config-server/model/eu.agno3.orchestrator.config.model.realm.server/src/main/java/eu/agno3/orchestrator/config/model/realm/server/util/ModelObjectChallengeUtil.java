/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.challenge.ChallengeGenerationContext;
import eu.agno3.orchestrator.config.model.realm.challenge.Challenger;
import eu.agno3.orchestrator.config.model.realm.challenge.ChallengerRegistry;


/**
 * @author mbechler
 *
 */
@Component ( service = ModelObjectChallengeUtil.class )
public class ModelObjectChallengeUtil {

    private ChallengerRegistry challengerRegistry;


    @Reference
    protected synchronized void bindChallengerRegistry ( ChallengerRegistry cr ) {
        this.challengerRegistry = cr;
    }


    protected synchronized void unbindChallengerRegistry ( ChallengerRegistry cr ) {
        if ( this.challengerRegistry == cr ) {
            this.challengerRegistry = null;
        }
    }


    /**
     * 
     * @param obj
     * @return generated challenges
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    public List<ConfigApplyChallenge> generateChallenges ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {
        return generateChallenges(new EmptyReferenceWalkerContext(), obj);
    }


    /**
     * @param ctx
     * @param obj
     * @return generated challenges
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    public List<ConfigApplyChallenge> generateChallenges ( ReferenceWalkerContext ctx, ConfigurationObject obj )
            throws ModelServiceException, ModelObjectException {
        ChallengeGenerationContext cgctx = null;
        ChallengeGeneratingReferenceVisitor visitor = new ChallengeGeneratingReferenceVisitor(this, cgctx);
        ReferenceWalker.walk(ctx, obj, new RecursiveReferenceVisitor(visitor));
        return visitor.getChallenges();
    }


    /**
     * @param ctx
     * @param val
     * @return generated challenges
     */
    public <T extends ConfigurationObject> Collection<ConfigApplyChallenge> generateSingleObject ( ChallengeGenerationContext ctx, T val ) {
        @SuppressWarnings ( "unchecked" )
        List<Challenger<? super T>> challengers = getChallengers((Class<T>) val.getType());
        if ( challengers == null ) {
            return Collections.EMPTY_LIST;
        }
        List<ConfigApplyChallenge> challenges = new ArrayList<>();
        for ( Challenger<? super T> c : challengers ) {
            challenges.addAll(c.generate(ctx, val));
        }
        return challenges;
    }


    /**
     * @param val
     * @return
     */
    private <T extends ConfigurationObject> List<Challenger<? super T>> getChallengers ( Class<T> type ) {
        return this.challengerRegistry.getChallengers(type);
    }

}
