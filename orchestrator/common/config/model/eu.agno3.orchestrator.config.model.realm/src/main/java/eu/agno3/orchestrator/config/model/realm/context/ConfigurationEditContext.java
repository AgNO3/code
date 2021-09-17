/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.context;


import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 * @param <TMutable>
 * 
 */
public class ConfigurationEditContext <T extends ConfigurationObject, TMutable extends T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3752536677519933229L;
    private TMutable current;
    private @Nullable T structuralDefaults;
    private @Nullable T inheritedValues;
    private @Nullable T enforcedValues;
    private ConfigurationState configState;
    private boolean inner;


    /**
     * @return the current
     */
    public TMutable getCurrent () {
        return this.current;
    }


    /**
     * @param current
     *            the current to set
     */
    public void setCurrent ( TMutable current ) {
        this.current = current;
    }


    /**
     * @return the structrualDefaults
     */
    public @Nullable T getStructuralDefaults () {
        return this.structuralDefaults;
    }


    /**
     * @param structrualDefaults
     *            the structrualDefaults to set
     */
    public void setStructuralDefaults ( @Nullable T structrualDefaults ) {
        this.structuralDefaults = structrualDefaults;
    }


    /**
     * @return the inheritedValues
     */
    public @Nullable T getInheritedValues () {
        return this.inheritedValues;
    }


    /**
     * @param inheritedValues
     *            the inheritedValues to set
     */
    public void setInheritedValues ( @Nullable T inheritedValues ) {
        this.inheritedValues = inheritedValues;
    }


    /**
     * @return the enforcedValues
     */
    public @Nullable T getEnforcedValues () {
        return this.enforcedValues;
    }


    /**
     * @param enforcedValues
     *            the enforcedValues to set
     */
    public void setEnforcedValues ( @Nullable T enforcedValues ) {
        this.enforcedValues = enforcedValues;
    }


    /**
     * @return the configuration state
     */
    public ConfigurationState getConfigurationState () {
        return this.configState;
    }


    /**
     * @param configState
     *            the configState to set
     */
    public void setConfigurationState ( ConfigurationState configState ) {
        this.configState = configState;
    }


    /**
     * @param inner
     */
    public void setInner ( boolean inner ) {
        this.inner = inner;
    }


    /**
     * @return the inner
     */
    public boolean isInner () {
        return this.inner;
    }
}
