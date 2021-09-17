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


/**
 * @author mbechler
 * 
 * @param <T>
 *            response value type
 *
 */
public abstract class AbstractLoginChallenge <T> implements LoginChallenge<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 4861624748547211665L;

    private transient T response;
    private String id;
    private String labelId;
    private String descriptionId;
    private boolean required;
    private boolean prompted;
    private boolean complete;


    /**
     * 
     */
    protected AbstractLoginChallenge ( String id, boolean required, String labelId, String descriptionId ) {
        this.id = id;
        this.required = required;
        this.labelId = labelId;
        this.descriptionId = descriptionId;
    }


    protected boolean isSecret () {
        return true;
    }


    private void writeObject ( ObjectOutputStream oos ) throws IOException {
        oos.defaultWriteObject();

        if ( this.response != null && !isSecret() ) {
            oos.writeBoolean(true);
            oos.writeObject(this.response);
        }
        else {
            oos.writeBoolean(false);
        }
    }


    @SuppressWarnings ( "unchecked" )
    private void readObject ( ObjectInputStream ois ) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        boolean haveResponse = ois.readBoolean();
        if ( haveResponse ) {
            this.response = (T) ois.readObject();
        }
        else {
            this.prompted = false;
            this.complete = false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getLabelId()
     */
    @Override
    public String getLabelId () {
        return this.labelId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getDescriptionId()
     */
    @Override
    public String getDescriptionId () {
        return this.descriptionId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getRequired()
     */
    @Override
    public boolean getRequired () {
        return this.required;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#isPrompted()
     */
    @Override
    public boolean isPrompted () {
        return this.prompted;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#markPrompted()
     */
    @Override
    public void markPrompted () {
        this.prompted = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#isComplete()
     */
    @Override
    public boolean isComplete () {
        return this.complete;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#markComplete()
     */
    @Override
    public void markComplete () {
        this.complete = true;
    }


    @Override
    public void reset () {
        this.complete = false;
        this.prompted = false;
        this.response = null;
    }


    @Override
    public boolean isResetOnFailure () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#validateResponse()
     */
    @Override
    public boolean validateResponse () {
        return !this.getRequired() || getResponse() != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getResponse()
     */
    @Override
    public T getResponse () {
        return this.response;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#setResponse(java.lang.Object)
     */
    @Override
    public void setResponse ( T response ) {
        if ( this.complete ) {
            // don't allow changing values after the respose was marked completed
            return;
        }
        this.response = response;
        markPrompted();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.getClass().getSimpleName() + ":" + this.getId(); //$NON-NLS-1$
    }
}
