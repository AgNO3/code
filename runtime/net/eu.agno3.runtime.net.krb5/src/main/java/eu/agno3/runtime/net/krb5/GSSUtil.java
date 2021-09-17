/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.lang.reflect.Field;
import java.security.Key;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

import com.sun.security.jgss.AuthorizationDataEntry;
import com.sun.security.jgss.ExtendedGSSContext;
import com.sun.security.jgss.InquireType;

import sun.security.jgss.spnego.SpNegoContext;
import sun.security.krb5.internal.Ticket;


/**
 * @author mbechler
 *
 */
public class GSSUtil {

    private static final Logger log = Logger.getLogger(GSSUtil.class);

    private static final String GSSCONTEXT = "sun.security.jgss.GSSContextImpl"; //$NON-NLS-1$
    private static final String KRB5CONTEXT = "sun.security.jgss.krb5.Krb5Context"; //$NON-NLS-1$

    private static Field GET_MECH_CONTEXT;
    private static Field GET_SPNEGO_MECH_CONTEXT;
    private static Field GET_SERVICE_TICKET;


    static {

        try {
            GET_MECH_CONTEXT = GSSUtil.class.getClassLoader().loadClass(GSSCONTEXT).getDeclaredField("mechCtxt"); //$NON-NLS-1$
            GET_MECH_CONTEXT.setAccessible(true);
            GET_SPNEGO_MECH_CONTEXT = SpNegoContext.class.getDeclaredField("mechContext"); //$NON-NLS-1$
            GET_SPNEGO_MECH_CONTEXT.setAccessible(true);
            GET_SERVICE_TICKET = GSSUtil.class.getClassLoader().loadClass(KRB5CONTEXT).getDeclaredField("serviceTicket"); //$NON-NLS-1$
            GET_SERVICE_TICKET.setAccessible(true);
        }
        catch (
            ClassNotFoundException |
            SecurityException |
            NoSuchFieldException e ) {
            log.error("Incompatible JRE, cannot access service ticket", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ctx
     * @return the authorization data entries
     * @throws GSSException
     */
    public static List<AuthDataEntry> getAuthDataFromContext ( GSSContext ctx ) throws GSSException {
        ExtendedGSSContext ectx = (ExtendedGSSContext) ctx;
        AuthorizationDataEntry[] authzData = (AuthorizationDataEntry[]) ectx.inquireSecContext(InquireType.KRB5_GET_AUTHZ_DATA);

        List<AuthDataEntry> entries = new LinkedList<>();
        if ( authzData == null ) {
            log.debug("No authorization data"); //$NON-NLS-1$
            return entries;
        }

        for ( AuthorizationDataEntry e : authzData ) {
            entries.add(new AuthDataEntry(e.getType(), e.getData()));
        }

        return entries;
    }


    /**
     * @param ctx
     * @return the GSS session key
     * @throws GSSException
     */
    public static Key getSessionKey ( GSSContext ctx ) throws GSSException {
        ExtendedGSSContext ectx = (ExtendedGSSContext) ctx;
        return ( (Key) ectx.inquireSecContext(InquireType.KRB5_GET_SESSION_KEY) );
    }


    /**
     * 
     * @param ctx
     * @return the auth time as KerberosTime string
     * @throws GSSException
     */
    public static String getAuthTime ( GSSContext ctx ) throws GSSException {
        ExtendedGSSContext ectx = (ExtendedGSSContext) ctx;
        return ( (String) ectx.inquireSecContext(InquireType.KRB5_GET_AUTHTIME) );
    }


    /**
     * 
     * @param ctx
     * @return the service ticket flags
     * @throws GSSException
     */
    public static boolean[] getTicketFlags ( GSSContext ctx ) throws GSSException {
        ExtendedGSSContext ectx = (ExtendedGSSContext) ctx;
        return ( (boolean[]) ectx.inquireSecContext(InquireType.KRB5_GET_TKT_FLAGS) );
    }


    /**
     * @param ctx
     * @return the KVNO of the used service ticket
     * @throws KerberosException
     */
    public static int getServiceKVNO ( GSSContext ctx ) throws KerberosException {
        ExtendedGSSContext ectx = (ExtendedGSSContext) ctx;

        try {
            if ( GSSCONTEXT.equals(ectx.getClass().getName()) ) {
                Object o = GET_MECH_CONTEXT.get(ectx);

                if ( o instanceof SpNegoContext ) {
                    o = GET_SPNEGO_MECH_CONTEXT.get(o);
                }

                if ( GSSCONTEXT.equals(o.getClass().getName()) ) {
                    o = GET_MECH_CONTEXT.get(o);
                }

                if ( KRB5CONTEXT.equals(o.getClass().getName()) ) {
                    o = GET_SERVICE_TICKET.get(o);
                }

                if ( o instanceof Ticket ) {
                    Ticket t = (Ticket) o;
                    return t.encPart.getKeyVersionNumber();
                }
            }

        }
        catch (
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new KerberosException("Failed to retrieve service ticket", e); //$NON-NLS-1$
        }

        throw new KerberosException("Failed to get KVNO from context"); //$NON-NLS-1$
    }
}
