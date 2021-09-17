/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.token;


import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationToken;


/**
 * @author mbechler
 *
 */
public class RealmTokenToken implements AuthenticationToken {

    private static final Logger log = Logger.getLogger(RealmTokenToken.class);

    /**
     * 
     */
    private static final long serialVersionUID = 5050194463115512914L;
    private String token;
    private String host;
    private String realm;
    private int kvno;
    private boolean properIV;


    /**
     * @param token
     * @param kvno
     */
    public RealmTokenToken ( String token, int kvno ) {
        this.token = token;
        this.kvno = kvno;
    }


    /**
     * 
     * @param token
     * @param kvno
     * @param host
     */
    public RealmTokenToken ( String token, int kvno, String host ) {
        this.token = token;
        this.kvno = kvno;
        this.host = host;
    }


    /**
     * @return the kvno
     */
    public int getKvno () {
        return this.kvno;
    }


    /**
     * @return the properIV
     */
    public boolean isProperIV () {
        return this.properIV;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public String getCredentials () {
        return this.token;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public Object getPrincipal () {
        return null;
    }


    /**
     * @return the host
     */
    public String getHost () {
        return this.host;
    }


    /**
     * @param realm
     */
    public void setRealm ( String realm ) {
        this.realm = realm;
    }


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("RealmTokenToken: %s-%s", getRealm(), getCredentials()); //$NON-NLS-1$
    }


    /**
     * 
     * @param data
     * @return token from string representation
     */
    public static RealmTokenToken createFromString ( String data ) {
        int sepPos = data.indexOf('-');
        if ( sepPos < 0 ) {
            log.warn("No realm separator found"); //$NON-NLS-1$
            return null;
        }

        String realm;
        String realToken;
        int kvno = 0;
        boolean legacy = false;
        int sep2Pos = data.indexOf('~', sepPos + 1);
        if ( sep2Pos < 0 ) {
            sep2Pos = data.indexOf('-', sepPos + 1);
            legacy = true;
        }
        if ( sep2Pos >= 0 ) {
            realm = data.substring(0, sepPos);
            realToken = data.substring(sep2Pos + 1);
            String kvnoString = data.substring(sepPos + 1, sep2Pos);
            try {
                kvno = Integer.parseInt(kvnoString);
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Failed to parse kvno", e); //$NON-NLS-1$
                return null;
            }
        }
        else {
            realm = data.substring(0, sepPos);
            realToken = data.substring(sepPos + 1);
        }

        RealmTokenToken tok = new RealmTokenToken(realToken, kvno);
        tok.properIV = !legacy;
        tok.setRealm(realm);
        return tok;
    }

}
