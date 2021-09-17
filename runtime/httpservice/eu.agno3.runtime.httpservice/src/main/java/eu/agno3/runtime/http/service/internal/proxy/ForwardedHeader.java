/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal.proxy;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


class ForwardedHeader implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4607431012149152869L;

    private static final Logger log = Logger.getLogger(ForwardedHeader.class);

    private String bySpec;
    private String forSpec;
    private String hostSpec;
    private String protoSpec;

    private Map<String, String> extensions;


    /**
     * @return the bySpec
     */
    public String getBySpec () {
        return this.bySpec;
    }


    /**
     * @return the forSpec
     */
    public String getForSpec () {
        return this.forSpec;
    }


    /**
     * @return the hostSpec
     */
    public String getHostSpec () {
        return this.hostSpec;
    }


    /**
     * @return the protoSpec
     */
    public String getProtoSpec () {
        return this.protoSpec;
    }


    /**
     * @return the additional
     */
    public Map<String, String> getExtensions () {
        return this.extensions;
    }


    public static List<ForwardedHeader> parse ( String header ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Parsing header " + header); //$NON-NLS-1$
        }
        List<ForwardedHeader> res = new ArrayList<>();
        for ( Map<String, String> fwHeader : parseMap(header) ) {
            ForwardedHeader h = new ForwardedHeader();
            h.bySpec = fwHeader.remove("by"); //$NON-NLS-1$

            h.forSpec = fwHeader.remove("for"); //$NON-NLS-1$
            h.hostSpec = fwHeader.remove("host"); //$NON-NLS-1$
            h.protoSpec = fwHeader.remove("proto"); //$NON-NLS-1$
            h.extensions = fwHeader;
            res.add(h);
        }
        return res;
    }


    public static List<Map<String, String>> parseMap ( String header ) {
        List<Map<String, String>> attrs = new ArrayList<>();
        Map<String, String> cur = new HashMap<>();

        int pos = 0;
        int len = header.length();
        char last = 0;
        boolean quoted = false;

        StringBuilder token = new StringBuilder();
        String lastToken = null;

        while ( pos < len ) {
            char c = header.charAt(pos);
            if ( quoted && last != '\\' && c == '"' ) {
                quoted = false;
            }
            else if ( quoted ) {
                token.append(c);
            }
            else if ( c == '"' ) {
                quoted = true;
            }
            else if ( Character.isWhitespace(c) ) {}
            else if ( c == '=' ) {
                lastToken = token.toString();
                if ( StringUtils.isBlank(lastToken) ) {
                    throw new IllegalArgumentException("Invalid attribute"); //$NON-NLS-1$
                }
                token = new StringBuilder();
            }
            else if ( c == ';' ) {
                // end of element
                if ( !StringUtils.isBlank(lastToken) ) {
                    cur.put(lastToken, token.toString());
                    token = new StringBuilder();
                    lastToken = null;
                }
                attrs.add(cur);
                cur = new HashMap<>();
            }
            else if ( c == ',' ) {
                // end of attribute
                if ( !StringUtils.isBlank(lastToken) ) {
                    cur.put(lastToken, token.toString());
                    token = new StringBuilder();
                    lastToken = null;
                }
            }
            else {
                token.append(c);
            }
            pos++;
            last = c;
        }

        if ( quoted ) {
            throw new IllegalArgumentException("Unterminated quoted string"); //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(lastToken) ) {
            cur.put(lastToken, token.toString());
        }

        if ( !cur.isEmpty() ) {
            attrs.add(cur);
        }

        return attrs;
    }
}