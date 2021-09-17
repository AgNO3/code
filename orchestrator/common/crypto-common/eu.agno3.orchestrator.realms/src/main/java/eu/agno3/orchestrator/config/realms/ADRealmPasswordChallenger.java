/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.CredentialChallenge;
import eu.agno3.orchestrator.config.model.realm.challenge.ChallengeGenerationContext;
import eu.agno3.orchestrator.config.model.realm.challenge.Challenger;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;


/**
 * @author mbechler
 *
 */
@Component ( service = Challenger.class )
public class ADRealmPasswordChallenger implements Challenger<ADRealmConfig> {

    private static final Logger log = Logger.getLogger(ADRealmPasswordChallenger.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.challenge.Challenger#getObjectType()
     */
    @Override
    public Class<ADRealmConfig> getObjectType () {
        return ADRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.challenge.Challenger#generate(eu.agno3.orchestrator.config.model.realm.challenge.ChallengeGenerationContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public Collection<ConfigApplyChallenge> generate ( ChallengeGenerationContext ctx, ADRealmConfig obj ) {
        // leaving always requires an admin user
        if ( !obj.getDoLeave() && ( !obj.getDoJoin() || obj.getJoinType() != ADJoinType.ADMIN ) ) {
            return Collections.EMPTY_LIST;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("May need password for joining/leaving domain " + obj.getRealmName()); //$NON-NLS-1$
        }

        CredentialChallenge chlg = new CredentialChallenge(obj.getId() + "-domainAdminPass", true); //$NON-NLS-1$
        chlg.setMessageBase(RealmsConfigMessages.BASE);
        chlg.setLabelTemplate(obj.getDoJoin() ? "adrealm.join.credentialsRequired" //$NON-NLS-1$
                : "adrealm.leave.credentialsRequired");//$NON-NLS-1$
        chlg.setLabelArgs(Arrays.asList(obj.getRealmName()));
        return Arrays.asList(chlg);
    }

}
