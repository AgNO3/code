/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.login;


/**
 * @author mbechler
 *
 */
public class TermsLoginChallenge extends AbstractLoginChallenge<Boolean> {

    /**
     * 
     */
    private static final long serialVersionUID = 8518734818885865556L;


    /**
     * @param id
     */
    public TermsLoginChallenge ( String id ) {
        super(id, true, null, null);
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
        return "terms"; //$NON-NLS-1$
    }

}
