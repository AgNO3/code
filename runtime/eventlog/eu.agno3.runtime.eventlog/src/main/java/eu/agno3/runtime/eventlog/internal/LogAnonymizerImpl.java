/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.eventlog.Anonymizer;
import eu.agno3.runtime.eventlog.AnonymizerConfig;
import eu.agno3.runtime.eventlog.AnonymizerRule;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.eventlog.LogAnonymizer;


/**
 * @author mbechler
 *
 */
@Component ( service = LogAnonymizer.class )
public class LogAnonymizerImpl implements LogAnonymizer {

    private static final Logger log = Logger.getLogger(LogAnonymizerImpl.class);
    private Map<String, AnonymizerConfig> configs = new HashMap<>();
    private Map<String, Anonymizer> anonymizers = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, updated = "updatedConfig" )
    protected synchronized void bindConfig ( AnonymizerConfig ac ) {
        String matchStream = ac.getMatchStream() != null ? ac.getMatchStream() : EventLogger.DEFAULT_STREAM;
        this.configs.put(matchStream, ac);
    }


    protected synchronized void updatedConfig ( AnonymizerConfig ac ) {

    }


    protected synchronized void unbindConfig ( AnonymizerConfig ac ) {
        String matchStream = ac.getMatchStream() != null ? ac.getMatchStream() : EventLogger.DEFAULT_STREAM;
        this.configs.remove(matchStream, ac);
    }


    @Reference
    protected synchronized void bindAnonymizer ( Anonymizer a ) {
        this.anonymizers.put(a.getId(), a);
    }


    protected synchronized void unbindAnonymizer ( Anonymizer a ) {
        this.anonymizers.remove(a.getId());
    }


    /**
     * @param stream
     * @return
     */
    private AnonymizerConfig getConfig ( String stream ) {
        AnonymizerConfig ac = this.configs.get(stream);
        if ( ac != null ) {
            return ac;
        }
        return this.configs.get(EventLogger.DEFAULT_STREAM);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.LogAnonymizer#anonymize(java.lang.String, java.util.Map)
     */
    @Override
    public Map<?, ?> anonymize ( String stream, Map<Object, Object> map ) {
        AnonymizerConfig ac = getConfig(stream);
        if ( ac == null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("No config for " + stream); //$NON-NLS-1$
            }
            return map;
        }

        for ( Entry<String, AnonymizerRule> entry : ac.getRules().entrySet() ) {
            String[] segments = StringUtils.split(entry.getKey(), '.');
            Anonymizer anon = null;
            if ( !"redact".equals(entry.getValue().getType()) ) { //$NON-NLS-1$
                anon = this.anonymizers.get(entry.getValue().getType());
                if ( anon == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format(
                            "Anonymizer %s for %s not found, redacting field", //$NON-NLS-1$
                            entry.getValue().getType(),
                            entry.getKey()));
                    }
                }
            }
            anonymizeField(map, segments, 0, segments.length, anon, entry.getValue().getOpts());
        }

        return map;
    }


    /**
     * @param map
     * @param segments
     * @param i
     * @param length
     * @param anon
     * @param opts
     */
    @SuppressWarnings ( "unchecked" )
    private static void anonymizeField ( Map<Object, Object> map, String[] segments, int i, int length, Anonymizer anon, Map<String, String> opts ) {

        String curkey = segments[ i ];
        Object val = map.get(segments[ i ]);

        if ( val == null ) {
            // does not exist
            return;
        }

        if ( i == length - 1 ) {
            // at target
            String replace = null;
            if ( val instanceof String && anon != null ) {
                replace = anon.anonymize((String) val, opts);
            }
            if ( replace == null ) {
                map.remove(curkey);
            }
            else {
                map.put(curkey, replace);
            }
            return;
        }
        else if ( val instanceof Map ) {
            anonymizeField((Map<Object, Object>) val, segments, i + 1, length, anon, opts);
        }
        else {
            log.debug("Redacting unknown item " + val); //$NON-NLS-1$
            map.remove(curkey);
        }
    }

}
