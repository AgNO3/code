/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class ConfigTestResultImpl implements Serializable, ConfigTestResult {

    /**
     * 
     */
    private static final long serialVersionUID = -3864299812101235491L;

    private UUID testId;
    private ConfigTestState state = ConfigTestState.UNKNOWN;
    private List<ConfigTestResultEntry> entries = new ArrayList<>();
    private List<ViolationEntry> violations = new ArrayList<>();

    private String defaultObjectType;


    /**
     * 
     */
    public ConfigTestResultImpl () {}


    /**
     * @param state
     */
    public ConfigTestResultImpl ( ConfigTestState state ) {
        this.state = state;
    }


    /**
     * @return the state
     */
    public ConfigTestState getState () {
        return this.state;
    }


    /**
     * @return the testId
     */
    public UUID getTestId () {
        return this.testId;
    }


    /**
     * @param testId
     *            the testId to set
     */
    public void setTestId ( UUID testId ) {
        this.testId = testId;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( ConfigTestState state ) {
        this.state = state;
    }


    /**
     * 
     * @param st
     * @return this
     */
    @Override
    public ConfigTestResultImpl state ( ConfigTestState st ) {
        setState(st);
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#withType(java.lang.String)
     */
    @Override
    public ConfigTestResult withType ( String objectType ) {
        return new ConfigTestResultTypeWrapper(objectType, this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#get()
     */
    @Override
    public ConfigTestResultImpl get () {
        return this;
    }


    /**
     * @return the entries
     */
    public List<ConfigTestResultEntry> getEntries () {
        return this.entries;
    }


    /**
     * @param entries
     *            the entries to set
     */
    public void setEntries ( List<ConfigTestResultEntry> entries ) {
        this.entries = entries;
    }


    /**
     * 
     * @param e
     * @return this
     */
    @Override
    public ConfigTestResultImpl addEntry ( ConfigTestResultEntry e ) {
        this.entries.add(e);
        return this;
    }


    /**
     * @param sev
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResultImpl addEntry ( ConfigTestResultSeverity sev, String msgTemplate, String... args ) {
        return addEntry(new ConfigTestResultEntry(sev, msgTemplate, args));
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResultImpl info ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.INFO, msgTemplate, args);
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResultImpl warn ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.WARNING, msgTemplate, args);
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResultImpl error ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.ERROR, msgTemplate, args);
    }


    /**
     * @return the violations
     */
    public List<ViolationEntry> getViolations () {
        return this.violations;
    }


    /**
     * @param violations
     *            the violations to set
     */
    public void setViolations ( List<ViolationEntry> violations ) {
        this.violations = violations;
    }


    /**
     * @return default object type
     */
    public String getDefaultObjectType () {
        return this.defaultObjectType;
    }


    /**
     * @param defaultObjectType
     *            the defaultObjectType to set
     */
    public void setDefaultObjectType ( String defaultObjectType ) {
        this.defaultObjectType = defaultObjectType;
    }
}
