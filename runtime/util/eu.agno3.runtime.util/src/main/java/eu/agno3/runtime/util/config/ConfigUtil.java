/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.util.config;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public final class ConfigUtil {

    /**
     * 
     */
    private ConfigUtil () {}


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static boolean parseBoolean ( Dictionary<String, Object> cfg, String prop, boolean def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return Boolean.parseBoolean(val.trim());
        }
        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static int parseInt ( Dictionary<String, Object> cfg, String prop, int def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return Integer.parseInt(val.trim());
        }
        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static long parseLong ( Dictionary<String, Object> cfg, String prop, long def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return Long.parseLong(val.trim());
        }
        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static float parseFloat ( Dictionary<String, Object> cfg, String prop, float def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return Float.parseFloat(val.trim());
        }
        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static String parseString ( Dictionary<String, Object> cfg, String prop, String def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return val.trim();
        }
        return def;
    }


    /**
     * 
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Duration parseDuration ( Dictionary<String, Object> cfg, String prop, Duration def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return DurationUtil.parseDurationCompat(val);
        }
        return def;
    }


    /**
     * 
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Long parseByteSize ( Dictionary<String, Object> cfg, String prop, Long def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            return BytesUtil.parseByteSizeCompat(val);
        }
        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Map<String, String> parseStringMap ( Dictionary<String, Object> cfg, String prop, Map<String, String> def ) {
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
        String partUnescaped = StringUtils.replace(
            part.trim(),
            "\\,", //$NON-NLS-1$
            ","); //$NON-NLS-1$
        String kv[] = partUnescaped.split("(?<!\\\\)="); //$NON-NLS-1$

        if ( kv == null || kv.length != 2 ) {
            throw new IllegalArgumentException("Cannot parse map value " + val); //$NON-NLS-1$
        }

        String key = StringUtils.replace(
            kv[ 0 ].trim(),
            "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        String value = StringUtils.replace(
            kv[ 1 ].trim(),
            "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        map.put(key, value);
    }


    /**
     * 
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Map<String, List<String>> parseStringMultiMap ( Dictionary<String, Object> cfg, String prop, Map<String, List<String>> def ) {
        String val = (String) cfg.get(prop);
        if ( !StringUtils.isBlank(val) ) {
            String parts[] = val.split("(?<!\\\\),"); //$NON-NLS-1$
            Map<String, List<String>> map = new HashMap<>();

            if ( parts == null || parts.length == 0 ) {
                return Collections.EMPTY_MAP;
            }

            for ( String part : parts ) {
                parseMultiMapEntry(val, map, part);
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
    private static void parseMultiMapEntry ( String val, Map<String, List<String>> map, String part ) {
        String partUnescaped = StringUtils.replace(
            part.trim(),
            "\\,", //$NON-NLS-1$
            ","); //$NON-NLS-1$
        String kv[] = partUnescaped.split("(?<!\\\\)="); //$NON-NLS-1$

        if ( kv == null ) {
            throw new IllegalArgumentException("Cannot parse map value " + val); //$NON-NLS-1$
        }

        if ( kv.length == 1 ) {
            String key = StringUtils.replace(
                kv[ 0 ].trim(),
                "\\=", //$NON-NLS-1$
                "="); //$NON-NLS-1$
            map.put(key, Collections.EMPTY_LIST);
            return;
        }

        if ( kv.length != 2 ) {
            throw new IllegalArgumentException("Invalid map value " + val); //$NON-NLS-1$
        }

        String key = StringUtils.replace(
            kv[ 0 ].trim(),
            "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        String value = StringUtils.replace(
            kv[ 1 ].trim(),
            "\\=", //$NON-NLS-1$
            "="); //$NON-NLS-1$
        map.put(key, parseMultiMapItem(value));

    }


    /**
     * @param value
     * @return
     */
    private static List<String> parseMultiMapItem ( String value ) {
        String parts[] = value.split("(?<!\\\\);"); //$NON-NLS-1$

        if ( parts == null || parts.length == 0 ) {
            return Collections.EMPTY_LIST;
        }

        List<String> stringVals = new ArrayList<>();
        for ( String part : parts ) {
            stringVals.add(StringUtils.replace(
                part.trim(),
                "\\;", //$NON-NLS-1$
                ";")); //$NON-NLS-1$
        }

        return stringVals;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Collection<String> parseStringCollection ( Dictionary<String, Object> cfg, String prop, Collection<String> def ) {
        return parseStringCollection((String) cfg.get(prop), def);
    }


    /**
     * @param def
     * @param propVal
     * @return
     */
    private static Collection<String> parseStringCollection ( String propVal, Collection<String> def ) {
        if ( !StringUtils.isBlank(propVal) ) {
            String vals[] = propVal.split("(?<!\\\\),"); //$NON-NLS-1$
            List<String> stringVals = new ArrayList<>();

            if ( vals == null || vals.length == 0 ) {
                return Collections.EMPTY_LIST;
            }

            for ( String val : vals ) {
                stringVals.add(StringUtils.replace(
                    val.trim(),
                    "\\,", //$NON-NLS-1$
                    ",")); //$NON-NLS-1$
            }
            return stringVals;
        }

        return def;
    }


    /**
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     */
    public static Set<String> parseStringSet ( Dictionary<String, Object> cfg, String prop, Set<String> def ) {
        Collection<String> res = parseStringCollection(cfg, prop, def);
        if ( res != null ) {
            return new HashSet<>(res);
        }
        return null;
    }


    /**
     * Parses a secret property
     * 
     * The property value can be either specified directly through the property
     * or indirectly through a file specified by the property with the File suffix.
     * 
     * Leading/trailing whitespace is stripped from the file contents.
     * 
     * @param cfg
     * @param prop
     * @param def
     * @return the config value
     * @throws IOException
     */
    public static String parseSecret ( Dictionary<String, Object> cfg, String prop, String def ) throws IOException {
        String val = parseString(cfg, prop, null);
        if ( !StringUtils.isBlank(val) ) {
            return val;
        }

        String fileProp = prop + "File"; //$NON-NLS-1$
        String fv = parseString(cfg, fileProp, null);
        if ( StringUtils.isEmpty(fv) ) {
            return def;
        }
        return ( new String(Files.readAllBytes(Paths.get(fv)), StandardCharsets.UTF_8) ).trim();
    }
}
