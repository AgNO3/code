/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.system.config.util;


import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public class PropertyConfigBuilder {

    private Properties props;


    /**
     * 
     */
    private PropertyConfigBuilder () {
        this.props = new Properties();
    }


    /**
     * @return a fresh instance
     */
    public static final PropertyConfigBuilder get () {
        return new PropertyConfigBuilder();
    }


    /**
     * 
     * @return properties instance
     */
    public Properties build () {
        return this.props;
    }


    /**
     * 
     * @param key
     * @param b
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Boolean b ) {
        if ( b != null ) {
            this.props.setProperty(key, Boolean.toString(b));
        }
        return this;
    }


    /**
     * 
     * @param key
     * @param i
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Integer i ) {
        if ( i != null ) {
            this.props.setProperty(key, Integer.toString(i));
        }
        return this;
    }


    /**
     * 
     * @param key
     * @param l
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Long l ) {
        if ( l != null ) {
            this.props.setProperty(key, Long.toString(l));
        }
        return this;
    }


    /**
     * s
     * 
     * @param key
     * @param f
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Float f ) {
        if ( f != null ) {
            this.props.setProperty(key, Float.toString(f));
        }
        return this;
    }


    /**
     * 
     * @param key
     * @param s
     * @return this
     */
    public PropertyConfigBuilder p ( String key, String s ) {
        if ( !StringUtils.isBlank(s) ) {
            this.props.setProperty(key, s);
        }
        return this;
    }


    /**
     * @param key
     * @param u
     * @return this
     */
    public PropertyConfigBuilder p ( String key, URI u ) {
        if ( u != null && ! ( StringUtils.isBlank(u.getHost()) ) ) {
            this.props.setProperty(key, u.toString());
        }
        return this;
    }


    /**
     * 
     * @param key
     * @param d
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Duration d ) {
        if ( d != null ) {
            this.props.setProperty(key, d.toString());
        }
        return this;
    }


    /**
     * @param key
     * @param values
     * @return an encoded config value
     */
    public PropertyConfigBuilder p ( String key, Collection<String> values ) {
        if ( values == null || values.isEmpty() ) {
            return this;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( String val : values ) {
            if ( first ) {
                first = false;
            }
            else {
                sb.append(',');
            }

            sb.append(StringUtils.replace(val, ",", //$NON-NLS-1$
                "\\,")); //$NON-NLS-1$
        }

        this.props.setProperty(key, sb.toString());

        return this;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Map<String, String> parseStringMap ( Dictionary<String, Object> cfg, String prop, HashMap<String, String> def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            String parts[] = val.split("(?<!\\\\),"); //$NON-NLS-1$
            Map<String, String> map = new LinkedHashMap<>();

            if ( parts == null || parts.length == 0 ) {
                return Collections.EMPTY_MAP;
            }

            for ( String part : parts ) {
                parseMapEntry(val, map, part);
            }

            return map;
        }
        return def;
    }


    /**
     * @param val
     * @param map
     * @param part
     */
    private static void parseMapEntry ( String val, Map<String, String> map, String part ) {
        String partUnescaped = StringUtils.replace(part.trim(), "\\,", //$NON-NLS-1$
            ","); //$NON-NLS-1$
        String kv[] = partUnescaped.split("(?<!\\\\)="); //$NON-NLS-1$

        if ( kv == null || kv.length != 2 ) {
            throw new IllegalArgumentException("Cannot parse map value " + val); //$NON-NLS-1$
        }

        String key = StringUtils.replace(kv[ 0 ].trim(), "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        String value = StringUtils.replace(kv[ 1 ].trim(), "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        map.put(key, value);
    }


    /**
     * @param key
     * @param m
     * @return this
     */
    public PropertyConfigBuilder p ( String key, Map<String, String> m ) {
        if ( m.isEmpty() ) {
            return this;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( Entry<String, String> e : m.entrySet() ) {
            String escKey = e.getKey().replace(",", //$NON-NLS-1$
                "\\,") //$NON-NLS-1$
                    .replace("=", //$NON-NLS-1$
                        "\\="); //$NON-NLS-1$
            String escVal = e.getValue().replace(",", //$NON-NLS-1$
                "\\,") //$NON-NLS-1$
                    .replace(",", //$NON-NLS-1$
                        "\\="); //$NON-NLS-1$

            if ( first ) {
                first = false;
            }
            else {
                sb.append(","); //$NON-NLS-1$
            }
            sb.append(escKey);
            sb.append('=');
            sb.append(escVal);
        }
        this.props.setProperty(key, sb.toString());
        return this;
    }


    /**
     * @param key
     * @param m
     * @return this
     */
    public PropertyConfigBuilder pmultiValueMap ( String key, Map<String, ? extends Collection<String>> m ) {
        if ( m.isEmpty() ) {
            return this;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( Entry<String, ? extends Collection<String>> e : m.entrySet() ) {
            String escKey = e.getKey().replace(",", //$NON-NLS-1$
                "\\,") //$NON-NLS-1$
                    .replace("=", //$NON-NLS-1$
                        "\\="); //$NON-NLS-1$

            if ( first ) {
                first = false;
            }
            else {
                sb.append(","); //$NON-NLS-1$
            }
            sb.append(escKey);
            sb.append('=');

            boolean firstVal = true;
            for ( String val : e.getValue() ) {
                String escVal = val.replace(";", //$NON-NLS-1$
                    "\\;") //$NON-NLS-1$
                        .replace(",", //$NON-NLS-1$
                            "\\,") //$NON-NLS-1$
                        .replace(",", //$NON-NLS-1$
                            "\\="); //$NON-NLS-1$

                if ( firstVal ) {
                    firstVal = false;
                }
                else {
                    sb.append(';');
                }

                sb.append(escVal);
            }

        }
        this.props.setProperty(key, sb.toString());
        return this;
    }
}
