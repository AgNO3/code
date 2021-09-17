/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return default system configuration impl
     */
    public MailingConfiguration createMailingConfiguration () {
        return new MailingConfigurationImpl();
    }

}
