/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader.contribs;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.FactoryContribution;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractSinglePropertiesConfigContribution implements ConfigContribution {

    private static final String FACTORY_SEPARATOR = "@"; //$NON-NLS-1$
    private static final String PID_SEPERATOR = "#"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(SingleFileConfigContribution.class);
    private final int priority;
    private final Map<String, Map<String, Object>> regularProperties = new LinkedHashMap<>();
    private final Map<String, Map<String, FactoryContribution>> factoryContributions = new LinkedHashMap<>();


    /**
     * @param priority
     * 
     */
    public AbstractSinglePropertiesConfigContribution ( int priority ) {
        this.priority = priority;
    }


    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * @return the regularProperties
     */
    @Override
    public Map<String, Map<String, Object>> getRegularProperties () {
        return this.regularProperties;
    }


    /**
     * @return the factoryContributions
     */
    @Override
    public Map<String, Map<String, FactoryContribution>> getFactoryContributions () {
        return this.factoryContributions;
    }


    @Override
    public void load () {
        try ( InputStream in = getInputStream() ) {
            if ( in == null ) {
                return;
            }
            Properties p = new Properties();
            p.load(in);
            this.parseProperties(p);
        }
        catch ( FileNotFoundException e ) {
            log.error("Config file does not exist:", e); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.error("Failed to read config file:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#reload(java.lang.String)
     */
    @Override
    public void reload ( String hint ) {
        this.load();
    }


    protected abstract InputStream getInputStream () throws IOException;


    /**
     * @param p
     */
    private void parseProperties ( Properties p ) {
        for ( Entry<Object, Object> entry : p.entrySet() ) {

            if ( ! ( entry.getKey() instanceof String ) ) {
                continue;
            }
            String key = (String) entry.getKey();

            if ( key.indexOf(PID_SEPERATOR) < 0 ) {
                log.warn("Expected key format is PID#ATTR, found " + key); //$NON-NLS-1$
                continue;
            }

            final String pid = key.substring(0, key.indexOf(PID_SEPERATOR));
            final String attr = key.substring(key.indexOf(PID_SEPERATOR) + 1);

            final int factorySepIdx = pid.indexOf(FACTORY_SEPARATOR);

            if ( factorySepIdx >= 0 ) {
                String realPid = pid.substring(0, factorySepIdx);
                String instanceId = pid.substring(factorySepIdx + 1);
                handleFactoryProperty(realPid, instanceId, attr, entry.getValue());
            }
            else {
                handleRegularProperty(pid, attr, entry.getValue());
            }
        }
    }


    /**
     * @param pid
     * @param attr
     * @param object
     */
    private void handleRegularProperty ( String pid, String attr, Object value ) {
        Map<String, Object> pidProperties = this.regularProperties.get(pid);

        if ( pidProperties == null ) {
            pidProperties = new LinkedHashMap<>();
            this.regularProperties.put(pid, pidProperties);
        }

        pidProperties.put(attr, value);
    }


    /**
     * @param realPid
     * @param instanceId
     * @param attr
     * @param object
     */
    private void handleFactoryProperty ( String realPid, String instanceId, String attr, Object value ) {
        Map<String, FactoryContribution> contribs = this.factoryContributions.get(realPid);

        if ( contribs == null ) {
            contribs = new LinkedHashMap<>();
            this.factoryContributions.put(realPid, contribs);
        }

        FactoryContribution contrib = contribs.get(instanceId);

        if ( contrib == null ) {
            contrib = new FactoryContributionImpl(this, realPid, instanceId);
            contribs.put(instanceId, contrib);
        }

        contrib.getProperties().put(attr, value);
    }

}