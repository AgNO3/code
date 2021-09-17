/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


/**
 * @author mbechler
 *
 */
class ConfigTestResultTypeWrapper implements ConfigTestResult {

    private ConfigTestResultImpl delegate;
    private String objectType;


    /**
     * @param objectType
     * @param delegate
     */
    public ConfigTestResultTypeWrapper ( String objectType, ConfigTestResultImpl delegate ) {
        this.objectType = objectType;
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#addEntry(eu.agno3.orchestrator.config.model.validation.ConfigTestResultEntry)
     */
    @Override
    public ConfigTestResult addEntry ( ConfigTestResultEntry e ) {
        if ( e.getObjectType() == null ) {
            e.setObjectType(this.objectType);
        }
        this.delegate.addEntry(e);
        return this;
    }


    /**
     * @param sev
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResult addEntry ( ConfigTestResultSeverity sev, String msgTemplate, String... args ) {
        return addEntry(new ConfigTestResultEntry(sev, msgTemplate, args));
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResult info ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.INFO, msgTemplate, args);
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResult warn ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.WARNING, msgTemplate, args);
    }


    /**
     * 
     * @param msgTemplate
     * @param args
     * @return this
     */
    @Override
    public ConfigTestResult error ( String msgTemplate, String... args ) {
        return addEntry(ConfigTestResultSeverity.ERROR, msgTemplate, args);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#withType(java.lang.String)
     */
    @Override
    public ConfigTestResult withType ( String ot ) {
        return new ConfigTestResultTypeWrapper(ot, this.delegate);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#state(eu.agno3.orchestrator.config.model.validation.ConfigTestState)
     */
    @Override
    public ConfigTestResult state ( ConfigTestState st ) {
        this.delegate.state(st);
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestResult#get()
     */
    @Override
    public ConfigTestResultImpl get () {
        return this.delegate.get();
    }

}
