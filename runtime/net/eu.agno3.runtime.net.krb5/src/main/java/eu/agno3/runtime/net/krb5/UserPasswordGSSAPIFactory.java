/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;


/**
 * @author mbechler
 *
 */
public class UserPasswordGSSAPIFactory extends AbstractCachingGSSAPIFactory implements GSSAPISubjectFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 4441576429117641950L;

    private final KerberosPrincipal principal;
    private final SortedMap<Integer, String> passwords;
    private final boolean clientOnly;
    private final Map<Integer, String> overrideSalts;


    /**
     * 
     * @param user
     * @param password
     * @param krbRealm
     */
    public UserPasswordGSSAPIFactory ( String user, String password, String krbRealm ) {
        this(String.format("%s@%s", user, krbRealm), password, false, Collections.EMPTY_MAP); //$NON-NLS-1$

    }


    /**
     * 
     * @param userAndRealm
     * @param password
     * @param overrideSalt
     * @param clientOnly
     */
    public UserPasswordGSSAPIFactory ( String userAndRealm, String password, boolean clientOnly, Map<Integer, String> overrideSalt ) {
        this.clientOnly = clientOnly;
        this.overrideSalts = overrideSalt;
        this.principal = new KerberosPrincipal(userAndRealm); // $NON-NLS-1$
        this.passwords = new TreeMap<>();
        this.passwords.put(0, password);
    }


    /**
     * @param princ
     * @param passwords
     * @param clientOnly
     * @param overrideSalts
     */
    public UserPasswordGSSAPIFactory ( KerberosPrincipal princ, SortedMap<Integer, String> passwords, boolean clientOnly,
            Map<Integer, String> overrideSalts ) {
        this.clientOnly = clientOnly;
        this.overrideSalts = overrideSalts;
        this.principal = princ;
        this.passwords = passwords;
    }


    /**
     * @param user
     * @param passwords
     * @param krbRealm
     * @param clientOnly
     * @param overrideSalt
     */
    public UserPasswordGSSAPIFactory ( String user, SortedMap<Integer, String> passwords, String krbRealm, boolean clientOnly,
            Map<Integer, String> overrideSalt ) {
        this.clientOnly = clientOnly;
        this.overrideSalts = overrideSalt;
        this.principal = new KerberosPrincipal(String.format("%s@%s", user, krbRealm)); //$NON-NLS-1$
        this.passwords = passwords;
    }


    @Override
    protected Subject getSubjectInternal () throws KerberosException {
        if ( this.clientOnly ) {
            return Krb5SubjectUtil.getInitiatorSubject(this.principal, this.passwords.get(this.passwords.size() - 1), null, false);
        }
        return Krb5SubjectUtil.getInitiateAndAcceptSubject(this.principal, this.passwords, this.overrideSalts, null, false);
    }


    /**
     * @return the principal
     */
    @Override
    public KerberosPrincipal getPrincipal () {
        return this.principal;
    }
}
