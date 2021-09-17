/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi;


import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public enum ComponentState {

    ACTIVE(ComponentConfigurationDTO.ACTIVE),
    SATISFIED(ComponentConfigurationDTO.SATISFIED),
    UNSATISFIED_CONFIGURATION(ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION),
    UNSATISFIED_REFERENCE(ComponentConfigurationDTO.UNSATISFIED_REFERENCE);

    private int stateCode;


    ComponentState ( int stateCode ) {
        this.stateCode = stateCode;
    }


    /**
     * Get the OSGi state code for this state
     * 
     * @return state code
     */
    public int getStateCode () {
        return this.stateCode;
    }


    /**
     * Gets the enum value for a state code
     * 
     * @param stateCode
     * @return enum value for state
     */
    public static ComponentState fromStateCode ( int stateCode ) {
        for ( ComponentState s : ComponentState.values() ) {
            if ( s.getStateCode() == stateCode ) {
                return s;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown component state code %d", stateCode)); //$NON-NLS-1$
    }
}
