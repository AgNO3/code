/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class PasswordResetToken extends SingleUseToken {

    /**
     * 
     */
    private static final long serialVersionUID = 3697852050785869859L;

    private UserPrincipal principal;


    /**
     * @return the principal
     */
    public UserPrincipal getPrincipal () {
        return this.principal;
    }


    /**
     * @param principal
     *            the principal to set
     */
    public void setPrincipal ( UserPrincipal principal ) {
        this.principal = principal;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SingleUseToken#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        super.writeExternal(out);
        this.principal.writeExternal(out);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SingleUseToken#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.principal = new UserPrincipal();
        this.principal.readExternal(in);
    }
}
