/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 6, 2016 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class PasswordChangeLoginChallenge extends AbstractLoginChallenge<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 6401197107172899455L;

    /**
     * 
     */
    public static final String PRIMARY_ID = "pwchange"; //$NON-NLS-1$

    private Integer estimatedChangeEntropy;
    private int minimumEntropy;

    private String oldCreds;

    private UserPrincipal principal;


    /**
     * 
     */
    public PasswordChangeLoginChallenge () {
        this(PasswordChangeLoginChallenge.PRIMARY_ID);
    }


    /**
     * @param id
     */
    public PasswordChangeLoginChallenge ( String id ) {
        super(
            id,
            true,
            "newPassword", //$NON-NLS-1$
            "newPassword.description"); //$NON-NLS-1$
    }


    private void writeObject ( ObjectOutputStream oos ) throws IOException {
        oos.defaultWriteObject();

        if ( this.oldCreds != null && !isSecret() ) {
            oos.writeBoolean(true);
            oos.writeUTF(this.oldCreds);
        }
        else {
            oos.writeBoolean(false);
        }
    }


    private void readObject ( ObjectInputStream ois ) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        boolean haveOldCreds = ois.readBoolean();
        if ( haveOldCreds ) {
            this.oldCreds = ois.readUTF();
        }
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
     * 
     * @return label id for password confirmation
     */
    public String getConfirmLabelId () {
        return "newPasswordConfirm"; //$NON-NLS-1$
    }


    /**
     * 
     * @return description id for password confirmation
     */
    public String getConfirmDescriptionId () {
        return "newPasswordConfirm.description"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getType()
     */
    @Override
    public String getType () {
        return "pwchange"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.AbstractLoginChallenge#reset()
     */
    @Override
    public void reset () {
        super.reset();
        this.estimatedChangeEntropy = null;
    }


    /**
     * 
     * @return the minimum entropy to require for the new password
     */
    public int getMinimumEntropy () {
        return this.minimumEntropy;
    }


    /**
     * @param minimumEntropy
     *            the minimumEntropy to set
     */
    public void setMinimumEntropy ( int minimumEntropy ) {
        this.minimumEntropy = minimumEntropy;
    }


    /**
     * @return the estimatedChangeEntropy
     */
    public Integer getEstimatedChangeEntropy () {
        return this.estimatedChangeEntropy;
    }


    /**
     * @param estimatedChangeEntropy
     *            the estimatedChangeEntropy to set
     */
    public void setEstimatedChangeEntropy ( Integer estimatedChangeEntropy ) {
        this.estimatedChangeEntropy = estimatedChangeEntropy;
    }


    /**
     * @return the oldCreds
     */
    public String getOldCreds () {
        return this.oldCreds;
    }


    /**
     * @param oldCreds
     */
    public void setOldCredentials ( String oldCreds ) {
        this.oldCreds = oldCreds;
    }


    /**
     * @param up
     */
    public void setPrincipal ( UserPrincipal up ) {
        this.principal = up;
    }


    /**
     * @return the principal
     */
    public UserPrincipal getPrincipal () {
        return this.principal;
    }

}
