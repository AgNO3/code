/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import java.util.Locale;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResultEntry;

import eu.agno3.runtime.util.sid.SID;


/**
 * @author mbechler
 *
 */
public class LDAPAttributeMatcher {

    /**
     * @param style
     * @param val
     * @param toMatch
     * @param e
     * @return whether the value equals toMatch
     */
    public static boolean matchAttribute ( AttributeMatchStyle style, Attribute val, String toMatch, SearchResultEntry e ) {
        switch ( style ) {
        case SID:
            return matchSID(val.getValueByteArray(), toMatch);
        case RID:
            // bit of a hack
            return matchRID(val.getValueAsInteger(), toMatch, e);
        case STRING_IGNORECASE:
            return toMatch.equalsIgnoreCase(val.getValue());
        case STRING:
        default:
            return toMatch.equals(val.getValue());
        }
    }


    /**
     * @param rid
     * @param toMatch
     * @param e
     * @return
     */
    private static boolean matchRID ( Integer rid, String toMatch, SearchResultEntry e ) {
        Attribute sid = e.getAttribute("objectSid"); //$NON-NLS-1$
        if ( rid == null || sid == null ) {
            return false;
        }
        SID userSid = SID.fromBinary(sid.getValueByteArray());
        SID domSid = userSid.getParent();
        SID ridSid = new SID(domSid, rid);
        SID t = SID.fromString(toMatch.toUpperCase(Locale.ROOT));
        return ridSid.equals(t);
    }


    /**
     * @param valueByteArray
     * @param toMatch
     * @return
     */
    private static boolean matchSID ( byte[] valueByteArray, String toMatch ) {
        SID s = SID.fromBinary(valueByteArray);
        SID t = SID.fromString(toMatch.toUpperCase(Locale.ROOT));
        return s.equals(t);
    }
}
