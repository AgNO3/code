/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


/**
 * @author mbechler
 * 
 */
public class TransactedMessageContext extends BaseMessageContext implements MessageContext {

    private boolean automaticTransaction = false;


    /**
     * @param automaticTransaction
     *            the automaticTransaction to set
     */
    public void setAutomaticTransaction ( boolean automaticTransaction ) {
        this.automaticTransaction = automaticTransaction;
    }


    /**
     * @return the automaticTransaction
     */
    public boolean isAutomaticTransaction () {
        return this.automaticTransaction;
    }

}
