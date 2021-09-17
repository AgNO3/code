/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info;


/**
 * @author mbechler
 * @param <T>
 *            the type of system information provided
 * 
 */
public interface SystemInformationProvider <T extends SystemInformation> {

    /**
     * @return the system information provided by this provider
     * @throws SystemInformationException
     *             if data collection fails
     */
    T getInformation () throws SystemInformationException;
}
