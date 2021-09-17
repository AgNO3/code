/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.config.model.validation.ConfigTestChallenge;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultEntry;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultSeverity;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.ConfigLocalizationProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "configTestBean" )
@ViewScoped
public class ConfigTestBean implements Serializable {

    /**
     * 
     */
    private static final String BLANK_ICON = "ui-icon-blank"; //$NON-NLS-1$
    private static final String ERROR_ICON = "ui-icon-alert"; //$NON-NLS-1$
    private static final String INFO_ICON = "ui-icon-info"; //$NON-NLS-1$
    private static final String WARN_ICON = "ui-messages-warn-icon"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ConfigTestBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 2011474211509571528L;

    private String objectPath;
    private String objectType;

    private ConfigTestResultImpl results;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigLocalizationProvider configLocalizer;
    private List<ConfigTestChallenge> challenges;


    /**
     * @return the objectPath
     */
    public String getObjectPath () {
        return this.objectPath;
    }


    /**
     * @param objectPath
     *            the objectPath to set
     */
    public void setObjectPath ( String objectPath ) {
        try {
            this.objectPath = URLDecoder.decode(objectPath, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    /**
     * @return the objectType
     */
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectType ( String objectType ) {
        try {
            this.objectType = URLDecoder.decode(objectType, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    public String getTestTemplate () {
        String typeName = getObjectType();
        if ( typeName == null ) {
            log.warn("Called with NULL"); //$NON-NLS-1$
            return null;
        }

        String prefix = "urn:agno3:objects:1.0:"; //$NON-NLS-1$

        if ( !typeName.startsWith(prefix) ) {
            throw new FacesException("Failed to determine edit view for type " + typeName); //$NON-NLS-1$
        }

        String local = typeName.substring(prefix.length()).replace(':', '/');
        String editView = String.format("/tpl/test/%s.xhtml", local); //$NON-NLS-1$

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Resolved view %s for type %s", editView, typeName)); //$NON-NLS-1$
        }

        return editView;
    }


    /**
     * @return the results
     */
    public ConfigTestResultImpl getResults () {
        return this.results;
    }


    /**
     * @return the challenges
     */
    public List<ConfigTestChallenge> getChallenges () {
        return this.challenges;
    }


    /**
     * @param testResults
     */
    public void setResults ( ConfigTestResultImpl testResults ) {
        this.results = testResults;
        List<ConfigTestChallenge> chlgs = new ArrayList<>();
        for ( ConfigTestResultEntry tre : this.results.getEntries() ) {
            if ( tre instanceof ConfigTestChallenge ) {
                chlgs.add((ConfigTestChallenge) tre);
            }
        }
        this.challenges = chlgs;
    }


    public boolean isRunning () {
        ConfigTestResultImpl r = this.results;
        if ( r == null ) {
            return false;
        }

        return r.getState() == ConfigTestState.QUEUED || r.getState() == ConfigTestState.RUNNING;
    }


    public String getStateIcon () {
        ConfigTestResultImpl r = this.results;
        if ( r == null ) {
            return null;
        }

        switch ( r.getState() ) {

        case QUEUED:
        case RUNNING:
            return null;

        case SUCCESS:
            return "ui-icon-check"; //$NON-NLS-1$

        case WARNING:
            return WARN_ICON;

        default:
        case FAILURE:
        case NO_TEST:
        case UNKNOWN:
        case VALIDATION:
            return ERROR_ICON;

        }
    }


    public String getStateMessage () {
        ConfigTestResultImpl r = this.results;
        if ( r == null ) {
            return null;
        }
        return GuiMessages.get("config.test.state." + r.getState().name()); //$NON-NLS-1$
    }


    public String getViolationIcon ( Object o ) {
        if ( ! ( o instanceof ViolationLevel ) ) {
            return BLANK_ICON;
        }

        ViolationLevel l = (ViolationLevel) o;
        switch ( l ) {
        case SUGGESTION:
            return INFO_ICON;

        case WARNING:
            return WARN_ICON;

        default:
        case ERROR:
            return ERROR_ICON;

        }
    }


    public String translateViolationLevel ( Object o ) {
        if ( ! ( o instanceof ViolationLevel ) ) {
            return StringUtils.EMPTY;
        }

        ViolationLevel l = (ViolationLevel) o;
        return l.name();
    }


    public String getEntryIcon ( Object o ) {
        if ( ! ( o instanceof ConfigTestResultSeverity ) ) {
            return BLANK_ICON;
        }

        ConfigTestResultSeverity s = (ConfigTestResultSeverity) o;
        switch ( s ) {

        case INFO:
            return INFO_ICON;

        case WARNING:
            return WARN_ICON;

        default:
        case ERROR:
        case UNKNOWN:
            return ERROR_ICON;

        }
    }


    public String translateEntrySeverity ( Object o ) {
        if ( ! ( o instanceof ConfigTestResultSeverity ) ) {
            return StringUtils.EMPTY;
        }

        ConfigTestResultSeverity s = (ConfigTestResultSeverity) o;
        return s.name();
    }


    public String formatViolation ( ViolationEntry e ) {
        return this.configLocalizer.getViolationMessage(e.getObjectType(), e.getMessageTemplate(), e.getMessageArgs());
    }


    public String formatEntry ( ConfigTestResultEntry e ) {

        ConfigTestResultImpl r = this.results;
        String ot = e.getObjectType();

        if ( ot == null && r != null ) {
            ot = r.getDefaultObjectType();
        }

        if ( "base".equals(ot) ) { //$NON-NLS-1$
            return GuiMessages.format("test." + e.getMessageTemplate(), e.getMessageArgs()); //$NON-NLS-1$
        }

        return this.configLocalizer.getViolationMessage(ot, "test." + e.getMessageTemplate(), e.getMessageArgs()); //$NON-NLS-1$
    }


    public String refresh () {
        ConfigTestResultImpl r = this.results;

        if ( r == null || r.getTestId() == null ) {
            log.trace("No result"); //$NON-NLS-1$
            return null;
        }

        if ( !EnumSet.of(ConfigTestState.QUEUED, ConfigTestState.RUNNING).contains(r.getState()) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Already complete in state " + r.getState()); //$NON-NLS-1$
            }
            return null;
        }

        try {
            log.debug("Update from remote"); //$NON-NLS-1$
            ConfigTestResultImpl u = this.ssp.getService(ConfigTestService.class).update(r);
            setResults(u);
            if ( u.getState() != ConfigTestState.QUEUED && u.getState() != ConfigTestState.RUNNING ) {
                RequestContext.getCurrentInstance().execute("PF('configTestPoller').stop();"); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }

}
