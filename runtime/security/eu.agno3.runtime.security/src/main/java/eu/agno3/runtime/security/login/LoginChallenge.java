/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 6, 2016 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 * @param <T>
 *            response value type
 */
public interface LoginChallenge <T> extends Serializable {

    /**
     * 
     * @return challenge id
     */
    String getId ();


    /**
     * 
     * @return whether an answer is required
     */
    boolean getRequired ();


    /**
     * 
     * @return type of this challenge
     */
    String getType ();


    /**
     * 
     * @return label for challenge
     */
    String getLabelId ();


    /**
     * 
     * @return description for challenge
     */
    String getDescriptionId ();


    /**
     * 
     * @return whether the response is valid
     */
    public boolean validateResponse ();


    /**
     * 
     * @return response value
     */
    T getResponse ();


    /**
     * 
     * @param response
     */
    void setResponse ( T response );


    /**
     * @return whether this is marked prompted (ie has been presented to the user)
     */
    boolean isPrompted ();


    /**
     * 
     */
    void markPrompted ();


    /**
     * 
     * @return whether this is marked complete (ie has been used and can no longer be changed)
     */
    boolean isComplete ();


    /**
     * 
     */
    void markComplete ();


    /**
     * @return whether this challenge should be reset when an error occurs
     */
    boolean isResetOnFailure ();


    /**
     * 
     */
    void reset ();

}
