/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingComparator;
import eu.agno3.runtime.elasticsearch.MappingStatus;


/**
 * @author mbechler
 *
 */
@Component ( service = MappingComparator.class )
public class MappingComparatorImpl implements MappingComparator {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(MappingComparatorImpl.class);
    /**
     * 
     */
    private static final String MAPPING_PROPERTIES = "properties"; //$NON-NLS-1$
    private static final String DYNAMIC_TEMPLATES = "dynamic_templates"; //$NON-NLS-1$
    private static final String[] IGNORE_MAPPING_FIELDS = new String[] {
        "norms" //$NON-NLS-1$
    };


    @Override
    public MappingStatus needsUpdate ( MappingMetaData src, MappingMetaData tgt, MappingMetaData targetDefaultMapping ) throws IOException {

        if ( Mapping.DEFAULT.equals(src.type()) && Mapping.DEFAULT.equals(tgt.type()) ) {
            if ( !src.getSourceAsMap().equals(tgt.getSourceAsMap()) ) {
                return MappingStatus.NEEDS_UPDATE;
            }
            return MappingStatus.CURRENT;
        }

        return diffMapping(src, tgt, targetDefaultMapping);
    }


    /**
     * @param src
     * @param tgt
     * @throws IOException
     */
    @SuppressWarnings ( "unchecked" )
    private static MappingStatus diffMapping ( MappingMetaData currentMapping, MappingMetaData targetMapping, MappingMetaData targetDefaultMapping )
            throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Found mapping %s, current:\n%s\n\ntarget %s:\n%s", //$NON-NLS-1$
                currentMapping.type(),
                currentMapping.source(),
                targetMapping.type(),
                targetMapping.source()));
        }

        Map<String, Object> src = currentMapping.sourceAsMap();
        Map<String, Object> tgt = targetMapping.sourceAsMap();
        Map<String, Object> def = targetDefaultMapping != null ? targetDefaultMapping.sourceAsMap() : Collections.EMPTY_MAP;

        Map<String, Object> srcStaticProps = (Map<String, Object>) src.remove(MAPPING_PROPERTIES);
        Map<String, Object> tgtStaticProps = (Map<String, Object>) tgt.remove(MAPPING_PROPERTIES);
        Map<String, Object> defStaticProps = (Map<String, Object>) def.remove(MAPPING_PROPERTIES);

        List<Map<String, Object>> srcDynamicMappings = (List<Map<String, Object>>) src.remove(DYNAMIC_TEMPLATES);
        List<Map<String, Object>> tgtDynamicMappings = (List<Map<String, Object>>) tgt.remove(DYNAMIC_TEMPLATES);
        List<Map<String, Object>> defDynamicMappings = (List<Map<String, Object>>) def.remove(DYNAMIC_TEMPLATES);

        MappingStatus status;
        if ( ( status = checkOtherSettings(src, tgt) ) != MappingStatus.CURRENT ) {
            return status;
        }

        if ( ( status = checkDynamicMappings(srcDynamicMappings, tgtDynamicMappings) ) != MappingStatus.CURRENT ) {
            return status;
        }

        log.debug("Non-field settings are the same"); //$NON-NLS-1$

        if ( tgtStaticProps == null ) {
            tgtStaticProps = Collections.EMPTY_MAP;
        }
        if ( defStaticProps == null ) {
            defStaticProps = Collections.EMPTY_MAP;
        }
        return checkFieldMappings(srcStaticProps, tgtStaticProps, defStaticProps, tgtDynamicMappings, defDynamicMappings);
    }


    /**
     * @param srcStaticProps
     * @param tgtStaticProps
     * @param defStaticProps
     * @param tgtDynamicMappings
     * @param defDynamicMappings
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    static MappingStatus checkFieldMappings ( Map<String, Object> srcStaticProps, Map<String, Object> tgtStaticProps,
            Map<String, Object> defStaticProps, List<Map<String, Object>> tgtDynamicMappings, List<Map<String, Object>> defDynamicMappings ) {
        for ( Entry<String, Object> entry : srcStaticProps.entrySet() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Found field " + entry.getKey()); //$NON-NLS-1$
                log.trace("Mapping " + entry.getValue()); //$NON-NLS-1$
            }

            Map<String, Object> srcMapping = (Map<String, Object>) entry.getValue();

            if ( tgtStaticProps.containsKey(entry.getKey()) ) {
                if ( mappingsEqual(tgtStaticProps.get(entry.getKey()), entry.getValue()) ) {
                    log.trace("Target contains the same mapping, good"); //$NON-NLS-1$
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Target contains a different mapping, reindex"); //$NON-NLS-1$
                        log.debug("Target mapping " + tgtStaticProps.get(entry.getKey())); //$NON-NLS-1$
                    }
                    return MappingStatus.NEEDS_REINDEX;
                }
            }
            else if ( defStaticProps.containsKey(entry.getKey()) ) {
                if ( mappingsEqual(defStaticProps.get(entry.getKey()), entry.getValue()) ) {
                    log.trace("Default mapping contains the same mapping, good"); //$NON-NLS-1$
                }
                else {
                    log.debug("Default mapping contains a different mapping, reindex"); //$NON-NLS-1$
                    return MappingStatus.NEEDS_REINDEX;
                }
            }
            else {
                String type = (String) srcMapping.get("type"); //$NON-NLS-1$
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Is dynamically mapped, look up definition for %s type %s", entry.getKey(), type)); //$NON-NLS-1$
                    log.trace("Local " + tgtDynamicMappings); //$NON-NLS-1$
                    log.trace("Global " + defDynamicMappings); //$NON-NLS-1$
                }

                Map<String, Object> dynMapping = lookupDynamicMapping(tgtDynamicMappings, entry.getKey(), type);

                if ( dynMapping == null ) {
                    log.trace("Falling back to default dynamic mappings"); //$NON-NLS-1$
                    dynMapping = lookupDynamicMapping(defDynamicMappings, entry.getKey(), type);
                }

                if ( dynMapping == null ) {
                    log.trace("No dynamic mapping found, is using defaults"); //$NON-NLS-1$
                }
                else if ( mappingsEqual(dynMapping, srcMapping) ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace("Dynamic mapping matches, good"); //$NON-NLS-1$
                        log.trace(srcMapping);
                        log.trace(dynMapping);
                    }
                }
                else {
                    log.debug("Dynamic mapping is different, reindex"); //$NON-NLS-1$
                    return MappingStatus.NEEDS_REINDEX;
                }
            }
        }
        return MappingStatus.CURRENT;
    }


    /**
     * @param srcDynamicMappings
     * @param tgtDynamicMappings
     */
    static MappingStatus checkDynamicMappings ( List<Map<String, Object>> srcDynamicMappings, List<Map<String, Object>> tgtDynamicMappings ) {
        if ( !Objects.equals(srcDynamicMappings, tgtDynamicMappings) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Dynamic mappings have changed, force reindex"); //$NON-NLS-1$
                log.debug("Source: " + srcDynamicMappings); //$NON-NLS-1$
                log.debug("Target: " + tgtDynamicMappings); //$NON-NLS-1$
            }
            return MappingStatus.NEEDS_REINDEX;
        }
        return MappingStatus.CURRENT;
    }


    /**
     * @param src
     * @param tgt
     */
    static MappingStatus checkOtherSettings ( Map<String, Object> src, Map<String, Object> tgt ) {
        if ( !src.equals(tgt) ) {
            // Lets ignore for the moment that there could be dynamically mapped fields that are not actually used
            if ( log.isDebugEnabled() ) {
                log.debug("Non-property settings have changed, force reindex"); //$NON-NLS-1$
                log.debug("Source: " + src); //$NON-NLS-1$
                log.debug("Target: " + tgt); //$NON-NLS-1$
            }

            return MappingStatus.NEEDS_REINDEX;
        }

        return MappingStatus.CURRENT;
    }


    /**
     * @param object
     * @param value
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static boolean mappingsEqual ( Object tgt, Object src ) {
        Map<String, Object> tm = new TreeMap<>((Map<String, Object>) tgt);
        Map<String, Object> sm = new TreeMap<>((Map<String, Object>) src);

        for ( String ignore : IGNORE_MAPPING_FIELDS ) {
            tm.remove(ignore);
            sm.remove(ignore);
        }

        boolean eq = mapEquals(tm, sm);
        if ( !eq && log.isDebugEnabled() ) {
            log.debug("Mappings differ:"); //$NON-NLS-1$
            log.debug("Source " + sm); //$NON-NLS-1$
            log.debug("Target " + tm); //$NON-NLS-1$
        }

        return eq;
    }


    private static boolean mapEquals ( Map<String, Object> tgt, Map<String, Object> src ) {
        if ( tgt.keySet().equals(src.keySet()) ) {
            // return false;
        }
        return tgt.equals(src);
    }


    /**
     * @param dynMappings
     * @param entry
     * @param type
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static Map<String, Object> lookupDynamicMapping ( List<Map<String, Object>> dynMappings, String field, String type ) {
        if ( dynMappings == null ) {
            return null;
        }

        boolean isString = "text".equals(type) || //$NON-NLS-1$
                "keyword".equals(type); //$NON-NLS-1$

        for ( Map<String, Object> mapEntry : dynMappings ) {
            for ( Entry<String, Object> v : mapEntry.entrySet() ) {
                Map<String, Object> dynm = (Map<String, Object>) v.getValue();

                String match = (String) dynm.get("match"); //$NON-NLS-1$
                String matchType = (String) dynm.get("match_mapping_type"); //$NON-NLS-1$
                Map<String, Object> mapping = (Map<String, Object>) dynm.get("mapping"); //$NON-NLS-1$

                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Have dynamic_template %s matching %s type %s", v.getKey(), match, matchType)); //$NON-NLS-1$
                }

                if ( match == null || match.equals("*") ) { //$NON-NLS-1$
                }
                else if ( !match.equals(field) ) {
                    continue;
                }

                if ( isString && "string".equals(matchType) ) { //$NON-NLS-1$
                    // string matches both keyword and text
                }
                else if ( matchType != null && !matchType.equals(type) ) {
                    continue;
                }

                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Dynamic_template %s matching %s type %s == %s: %s", v.getKey(), match, matchType, type, mapping)); //$NON-NLS-1$
                }
                return mapping;
            }
        }
        return null;
    }
}
