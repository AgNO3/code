/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.CredentialChallenge;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.crypto.InstanceCredentialWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;


/**
 * @author mbechler
 *
 */
public abstract class BaseConfigApplyContextBean implements ConfigApplyContextBean {

    /**
     * 
     */
    private static final long serialVersionUID = -2332179342612831768L;
    private ConfigApplyInfo applyInfo = new ConfigApplyInfo();
    private ConfigApplyContext applyContext;

    @Inject
    private InstanceCredentialWrapper credWrapper;


    @Override
    public Long getRevision () {
        if ( this.applyContext == null ) {
            return null;
        }
        return this.applyContext.getRevision();
    }


    @Override
    public List<ConfigApplyChallenge> getChallenges () {
        ConfigApplyContext ac = this.applyContext;
        if ( ac == null ) {
            return Collections.EMPTY_LIST;
        }
        return ac.getChallenges();
    }


    protected abstract InstanceStructuralObject getTargetInstance ();


    /**
     * @return the applyContext
     */
    @Override
    public ConfigApplyContext getApplyContext () {
        return this.applyContext;
    }


    /**
     * @param applyContext
     *            the applyContext to set
     */
    protected void setApplyContext ( ConfigApplyContext applyContext ) {
        this.applyContext = applyContext;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean#getApplyInfo()
     */
    @Override
    public ConfigApplyInfo getApplyInfo () {
        return this.applyInfo;
    }


    /**
     * @param applyInfo
     *            the applyInfo to set
     */
    public void setApplyInfo ( ConfigApplyInfo applyInfo ) {
        this.applyInfo = applyInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean#handleChallenges()
     */
    @Override
    public boolean handleChallenges () {
        List<ConfigApplyChallenge> responses = new ArrayList<>();
        List<ConfigApplyChallenge> challenges = getChallenges();
        try {
            for ( ConfigApplyChallenge chlg : challenges ) {
                responses.add(toResponse(chlg));
            }
            this.applyInfo.setChallengeResponses(responses);
            return true;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return false;
        }
    }


    /**
     * @param chlg
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private ConfigApplyChallenge toResponse ( ConfigApplyChallenge chlg )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( chlg instanceof CredentialChallenge ) {
            return cryptCredentials((CredentialChallenge) chlg);
        }

        return chlg;
    }


    /**
     * @param chlg
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private ConfigApplyChallenge cryptCredentials ( CredentialChallenge chlg )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        InstanceStructuralObject instance = getTargetInstance();
        if ( instance == null ) {
            throw new IllegalStateException("Don't have target instance"); //$NON-NLS-1$
        }

        return new CredentialChallenge(chlg, this.credWrapper.wrap(instance, new UsernamePasswordCredential(chlg.getUsername(), chlg.getPassword())));
    }

}
