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
public class UsernameLoginChallenge extends AbstractLoginChallenge<String> {

    /**
     * 
     */
    public static final String PRIMARY_ID = "username"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 6401197107172899455L;


    /**
     * 
     */
    public UsernameLoginChallenge () {
        this(PRIMARY_ID);
    }


    /**
     * @param id
     */
    public UsernameLoginChallenge ( String id ) {
        super(
            id,
            true,
            "username", //$NON-NLS-1$
            "username.description"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.AbstractLoginChallenge#isSecret()
     */
    @Override
    protected boolean isSecret () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getType()
     */
    @Override
    public String getType () {
        return "username"; //$NON-NLS-1$
    }

}
