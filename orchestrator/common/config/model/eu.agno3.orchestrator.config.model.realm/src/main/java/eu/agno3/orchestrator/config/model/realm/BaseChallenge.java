/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.List;


/**
 * @author mbechler
 *
 */
public abstract class BaseChallenge implements ConfigApplyChallenge {

    /**
     * 
     */
    private static final long serialVersionUID = -7258919233759668671L;

    private String key;
    private boolean required;

    private String messageBase;
    private String labelTemplate;
    private List<String> labelArgs;


    /**
     * 
     */
    public BaseChallenge () {}


    /**
     * 
     * @param key
     * @param required
     */
    public BaseChallenge ( String key, boolean required ) {
        this.key = key;
        this.required = required;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge#getKey()
     */
    @Override
    public String getKey () {
        return this.key;
    }


    /**
     * @param key
     *            the key to set
     */
    public void setKey ( String key ) {
        this.key = key;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge#isRequired()
     */
    @Override
    public boolean isRequired () {
        return this.required;
    }


    /**
     * @param required
     *            the required to set
     */
    public void setRequired ( boolean required ) {
        this.required = required;
    }


    /**
     * @return the messageBase
     */
    @Override
    public String getMessageBase () {
        return this.messageBase;
    }


    /**
     * @param messageBase
     *            the messageBase to set
     */
    public void setMessageBase ( String messageBase ) {
        this.messageBase = messageBase;
    }


    /**
     * @return the labelTemplate
     */
    @Override
    public String getLabelTemplate () {
        return this.labelTemplate;
    }


    /**
     * @param labelTemplate
     *            the labelTemplate to set
     */
    public void setLabelTemplate ( String labelTemplate ) {
        this.labelTemplate = labelTemplate;
    }


    /**
     * @return the labelArgs
     */
    @Override
    public List<String> getLabelArgs () {
        return this.labelArgs;
    }


    /**
     * @param labelArgs
     *            the labelArgs to set
     */
    public void setLabelArgs ( List<String> labelArgs ) {
        this.labelArgs = labelArgs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge#verify()
     */
    @Override
    public boolean verify () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("[%s]%s", this.getType(), this.getKey()); //$NON-NLS-1$
    }
}
