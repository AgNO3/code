/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 6, 2016 by mbechler
 */
package eu.agno3.runtime.security.login;


/**
 * @author mbechler
 *
 */
public class PasswordLoginChallenge extends AbstractLoginChallenge<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 8481897331981784901L;

    /**
     * 
     */
    public static final String PRIMARY_ID = "password"; //$NON-NLS-1$


    /**
     * 
     */
    public PasswordLoginChallenge () {
        this(PRIMARY_ID);

    }


    /**
     * @param id
     */
    public PasswordLoginChallenge ( String id ) {
        super(
            id,
            true,
            "password", //$NON-NLS-1$
            "password.description"); //$NON-NLS-1$
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
        return "password"; //$NON-NLS-1$
    }

}
