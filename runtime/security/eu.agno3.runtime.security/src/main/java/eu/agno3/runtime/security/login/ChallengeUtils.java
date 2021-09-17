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
public final class ChallengeUtils {

    /**
     * 
     */
    private ChallengeUtils () {}


    /**
     * 
     * @param sess
     * @return the primary username challenge
     */
    public static String getUsername ( LoginSession sess ) {
        return getUsername(sess, UsernameLoginChallenge.PRIMARY_ID);
    }


    /**
     * 
     * @param sess
     * @param id
     * @return the username challenge with the given id
     */
    public static String getUsername ( LoginSession sess, String id ) {
        return getStringResponse(sess, UsernameLoginChallenge.class, id);
    }


    /**
     * 
     * @param sess
     * @return the primary password challenge
     */
    public static String getPassword ( LoginSession sess ) {
        return getPassword(sess, PasswordLoginChallenge.PRIMARY_ID);
    }


    /**
     * 
     * @param sess
     * @param id
     * @return the password challenge with the given id
     */
    public static String getPassword ( LoginSession sess, String id ) {
        return getStringResponse(sess, PasswordLoginChallenge.class, id);
    }


    /**
     * 
     * @param sess
     * @return the primary password change challenge
     */
    public static String getChangePassword ( LoginSession sess ) {
        return getChangePassword(sess, PasswordChangeLoginChallenge.PRIMARY_ID);
    }


    /**
     * 
     * @param sess
     * @param id
     * @return the password change challenge with the given id
     */
    public static String getChangePassword ( LoginSession sess, String id ) {
        return getStringResponse(sess, PasswordChangeLoginChallenge.class, id);
    }


    static <T extends LoginChallenge<String>> String getStringResponse ( LoginSession sess, Class<T> type, String id ) {
        LoginChallenge<String> challenge = sess.getChallenge(type, id);
        if ( challenge == null ) {
            return null;
        }
        if ( !challenge.validateResponse() ) {
            return null;
        }
        return challenge.getResponse();
    }
}
