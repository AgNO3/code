/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2016 by mbechler
 */
package eu.agno3.runtime.security.login;


/**
 * @author mbechler
 *
 */
public class OTPLoginChallenge extends AbstractLoginChallenge<String> {

    /**
     * 
     */
    private static final long serialVersionUID = -4857171538331805947L;

    /**
     * 
     */
    public static final String PRIMARY_ID = "pin"; //$NON-NLS-1$


    /**
     * 
     */
    public OTPLoginChallenge () {
        this(PRIMARY_ID);

    }


    /**
     * @param id
     */
    public OTPLoginChallenge ( String id ) {
        super(
            id,
            true,
            "pin", //$NON-NLS-1$
            "pin.description"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.AbstractLoginChallenge#isResetOnFailure()
     */
    @Override
    public boolean isResetOnFailure () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getType()
     */
    @Override
    public String getType () {
        return "otp"; //$NON-NLS-1$
    }

}
