/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 *
 */
public class BooleanChallenge extends BaseChallenge {

    /**
     * 
     */
    private static final long serialVersionUID = -8260224295572224717L;

    private Boolean value;


    /**
     * 
     */
    public BooleanChallenge () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge#getType()
     */
    @Override
    public String getType () {
        return "boolean"; //$NON-NLS-1$
    }


    /**
     * 
     * @param key
     * @param required
     */
    public BooleanChallenge ( String key, boolean required ) {
        super(key, required);
    }


    /**
     * @return the value
     */
    public Boolean getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( Boolean value ) {
        this.value = value;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.BaseChallenge#toString()
     */
    @Override
    public String toString () {
        return super.toString() + '=' + this.getValue();
    }
}
