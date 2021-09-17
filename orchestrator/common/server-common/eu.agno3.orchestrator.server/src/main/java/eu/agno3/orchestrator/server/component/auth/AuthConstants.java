/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;


/**
 * @author mbechler
 *
 */
public final class AuthConstants {

    /**
     * 
     */
    private AuthConstants () {}

    /**
     * 
     */
    public static final String SYSTEM_USER = "system"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String AGENT_ID_OID_NUMBER = "1.3.6.1.4.1.44756.1.1.1"; //$NON-NLS-1$

    /**
     * 
     */
    public static final ASN1ObjectIdentifier AGENT_ID_OID = new ASN1ObjectIdentifier(AGENT_ID_OID_NUMBER);

    /**
     * 
     */
    public static final String SERVER_ID_OID_NUMBER = "1.3.6.1.4.1.44756.1.1.2"; //$NON-NLS-1$

    /**
     * 
     */
    public static final ASN1ObjectIdentifier SERVER_ID_OID = new ASN1ObjectIdentifier(SERVER_ID_OID_NUMBER);

    /**
     * 
     */
    private static final String GUI_ID_OID_NUMBER = "1.3.6.1.4.1.44756.1.1.3"; //$NON-NLS-1$

    /**
     * 
     */
    public static final ASN1ObjectIdentifier GUI_ID_OID = new ASN1ObjectIdentifier(GUI_ID_OID_NUMBER);

    private static final Map<String, String> OID_MAP = new HashMap<>();

    static {
        OID_MAP.put(AuthConstants.AGENT_ID_OID_NUMBER, "agentId"); //$NON-NLS-1$
        OID_MAP.put(AuthConstants.SERVER_ID_OID_NUMBER, "serverId"); //$NON-NLS-1$
        OID_MAP.put(AuthConstants.GUI_ID_OID_NUMBER, "guiId"); //$NON-NLS-1$
    }


    /**
     * @return the orchestrator OIDs
     * 
     */
    public static Map<String, String> getOidMap () {
        return Collections.unmodifiableMap(OID_MAP);
    }
}
