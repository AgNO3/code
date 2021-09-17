/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2013 by mbechler
 */
package eu.agno3.runtime.configloader.internal;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.BundleTracker;

import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.configloader.FactoryContribution;
import eu.agno3.runtime.configloader.ReconfigurationListener;
import eu.agno3.runtime.configloader.contribs.DirectoryConfigContribution;
import eu.agno3.runtime.configloader.contribs.SingleFileConfigContribution;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ConfigLoader.class
}, immediate = true )
public class ConfigLoaderImpl implements ConfigLoader {

    private static final String CONFIG_FILE_PROPERTY = "config.file"; //$NON-NLS-1$
    private static final String CONFIG_DIR_PROPERTY = "config.dir"; //$NON-NLS-1$
    private static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ConfigLoaderImpl.class);

    private static final Set<String> CONFIG_KEY_IGNORES = new HashSet<>();

    static {
        CONFIG_KEY_IGNORES.add(Constants.SERVICE_PID);
        CONFIG_KEY_IGNORES.add(ConfigurationAdmin.SERVICE_FACTORYPID);
        CONFIG_KEY_IGNORES.add(ConfigurationAdmin.SERVICE_BUNDLELOCATION);
    }

    private ConfigurationAdmin configAdmin;

    private Set<Configuration> managedFactoryConfigurations = new HashSet<>();
    private BundleTracker<ConfigContribution> bundleConfigTracker;

    private Set<ConfigContribution> sources = new LinkedHashSet<>();
    private MultiValuedMap<String, ConfigContribution> pidToSource = new HashSetValuedHashMap<>();

    private Set<ReconfigurationListener> listeners = new HashSet<>();


    /**
     * 
     * @param cm
     */
    @Reference
    public synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    /**
     * 
     * @param cm
     */
    public synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindReconfigurationListener ( ReconfigurationListener l ) {
        this.listeners.add(l);
    }


    protected synchronized void unbindReconfigurationListener ( ReconfigurationListener l ) {
        this.listeners.remove(l);
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting configuration bundle tracker and loading basic configuration"); //$NON-NLS-1$
        loadConfig();
        this.bundleConfigTracker = new BundleTracker<>(
            context.getBundleContext(),
            Bundle.UNINSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING,
            new BundleConfigurationTracker(this));
        this.bundleConfigTracker.open();
    }


    /**
     * 
     */
    private void loadConfig () {
        try {
            loadGlobalConfigDirectory();
            loadGlobalConfigFile();
            this.updateAll();
        }
        catch ( Exception e ) {
            log.warn("Failed to load configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void loadGlobalConfigDirectory () {
        String configDirProperty = System.getProperty(CONFIG_DIR_PROPERTY);

        if ( configDirProperty != null ) {
            configDirProperty = configDirProperty.trim();
        }

        if ( configDirProperty != null && !configDirProperty.isEmpty() ) {
            configDirProperty = configDirProperty.replace(
                "${user.home}", //$NON-NLS-1$
                System.getProperty("user.home")); //$NON-NLS-1$

            File baseDir = new File(configDirProperty);
            if ( log.isDebugEnabled() ) {
                log.debug("Adding config directory " + baseDir); //$NON-NLS-1$
            }
            addConfigSource(new DirectoryConfigContribution(baseDir, 10), false);

            File defaultDir = new File(baseDir, "defaults"); //$NON-NLS-1$

            if ( defaultDir.isDirectory() && defaultDir.canRead() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Adding default directory " + baseDir); //$NON-NLS-1$
                }
                addConfigSource(new DirectoryConfigContribution(defaultDir, -500), false);
            }

        }
        else {
            log.warn("No config directory specified"); //$NON-NLS-1$
        }
    }


    private void loadGlobalConfigFile () {
        String configFileProperty = System.getProperty(CONFIG_FILE_PROPERTY);

        if ( configFileProperty != null ) {
            configFileProperty = configFileProperty.trim();
        }

        if ( configFileProperty != null && !configFileProperty.isEmpty() ) {
            for ( String configFile : StringUtils.split(configFileProperty, ',') ) {
                configFile = configFile.replace(
                    "${user.home}", //$NON-NLS-1$
                    System.getProperty("user.home")); //$NON-NLS-1$
                addConfigSource(new SingleFileConfigContribution(new File(configFile), 0), false);
            }
        }
        else {
            log.debug("No config file(s) specified"); //$NON-NLS-1$
        }
    }


    /**
     * @param contrib
     */
    public synchronized void addConfigSource ( ConfigContribution contrib ) {
        addConfigSource(contrib, true);
    }


    /**
     * @param contrib
     * @param autoLoad
     *            also update the affected configuration
     */
    public synchronized void addConfigSource ( ConfigContribution contrib, boolean autoLoad ) {

        if ( this.sources.contains(contrib) ) {
            return;
        }

        contrib.load();
        this.sources.add(contrib);

        Set<String> affectsRegularPids = contrib.getRegularProperties().keySet();
        Map<String, Set<String>> affectsInstances = findFactoryInstances(Arrays.asList(contrib));

        if ( affectsRegularPids.isEmpty() && affectsInstances.isEmpty() ) {
            return;
        }
        try {
            this.notifyReconfigure();
            for ( String pid : affectsRegularPids ) {
                this.pidToSource.put(pid, contrib);
                if ( autoLoad ) {
                    this.updateRegularProperties(pid, false);
                }
            }

            for ( Entry<String, Set<String>> e : affectsInstances.entrySet() ) {
                this.pidToSource.put(e.getKey(), contrib);
                if ( autoLoad ) {
                    updateAllAffectedInstances(e.getValue(), e.getKey());
                }
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to add configuration source", e); //$NON-NLS-1$
        }
        finally {
            this.notifyReconfigureDone();
        }
    }


    /**
     * 
     */
    private void notifyReconfigureDone () {
        for ( ReconfigurationListener reconfigurationListener : this.listeners ) {
            reconfigurationListener.finishReconfigure();
        }
    }


    /**
     * 
     */
    private void notifyReconfigure () {
        for ( ReconfigurationListener reconfigurationListener : this.listeners ) {
            reconfigurationListener.startReconfigure();
        }
    }


    /**
     * @param contrib
     */
    public synchronized void removeConfigSource ( ConfigContribution contrib ) {

        if ( !this.sources.contains(contrib) ) {
            return;
        }

        this.sources.remove(contrib);

        Set<String> affectsRegularPids = contrib.getRegularProperties().keySet();
        Map<String, Set<String>> affectsInstances = findFactoryInstances(Arrays.asList(contrib));

        try {
            this.notifyReconfigure();
            for ( String pid : affectsRegularPids ) {
                this.pidToSource.removeMapping(pid, contrib);
                this.updateRegularProperties(pid, false);
            }

            for ( Entry<String, Set<String>> e : affectsInstances.entrySet() ) {
                this.pidToSource.removeMapping(e.getKey(), contrib);
                updateAllAffectedInstances(e.getValue(), e.getKey());
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to remove configuration source", e); //$NON-NLS-1$
        }
        finally {
            this.notifyReconfigureDone();
        }
    }


    private void updateAllAffectedInstances ( Set<String> instances, String factoryPid ) throws IOException {
        for ( String instanceId : instances ) {
            this.updateFactoryInstanceProperties(factoryPid, instanceId, false);
        }
    }


    protected void updateAll () throws IOException {
        log.debug("Updating all configurations"); //$NON-NLS-1$
        for ( String pid : this.pidToSource.keySet() ) {
            this.updatePid(pid, false);
        }
    }


    protected void updatePid ( String pid, boolean force ) throws IOException {
        Collection<ConfigContribution> scs = this.pidToSource.get(pid);

        if ( scs == null || scs.isEmpty() ) {
            return;
        }

        Set<String> instances = findFactoryInstances(scs).get(pid);
        this.updateRegularProperties(pid, force);
        if ( instances != null ) {
            updateAllAffectedInstances(instances, pid);
        }
    }


    private static Map<String, Set<String>> findFactoryInstances ( Collection<ConfigContribution> scs ) {
        Map<String, Set<String>> instances = new HashMap<>();

        for ( ConfigContribution contrib : scs ) {
            for ( Entry<String, Map<String, FactoryContribution>> e : contrib.getFactoryContributions().entrySet() ) {
                String factoryPid = e.getKey();
                for ( String instanceId : e.getValue().keySet() ) {
                    if ( !instances.containsKey(factoryPid) ) {
                        instances.put(factoryPid, new HashSet<String>());
                    }
                    instances.get(factoryPid).add(instanceId);
                }
            }
        }
        return instances;
    }


    protected Dictionary<String, Object> getRegularProperties ( String pid ) {
        Collection<ConfigContribution> collection = this.pidToSource.get(pid);

        if ( collection == null || collection.isEmpty() ) {
            return null;
        }

        List<ConfigContribution> scs = new ArrayList<>(collection);

        Collections.sort(scs, new Comparator<ConfigContribution>() {

            @Override
            public int compare ( ConfigContribution o1, ConfigContribution o2 ) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });

        Hashtable<String, Object> properties = null;

        for ( ConfigContribution contrib : scs ) {
            Map<String, Object> props = contrib.getRegularProperties().get(pid);

            if ( props == null ) {
                continue;
            }

            if ( properties == null ) {
                properties = new Hashtable<>();
            }

            properties.putAll(props);
        }

        return properties;
    }


    protected void updateRegularProperties ( String pid, boolean force ) throws IOException {

        Dictionary<String, Object> values = this.getRegularProperties(pid);

        if ( values == null ) {
            return;
        }

        Configuration config = this.configAdmin.getConfiguration(pid, null);

        if ( config != null && checkChanged(config, values, force) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Updating configuration of '%s'", pid)); //$NON-NLS-1$
                dumpConfig(values);
            }

            config.update(values);

            verify(config);
        }
    }


    /**
     * @param values
     * @param config
     * @throws IOException
     */
    private void verify ( Configuration config ) throws IOException {
        Configuration toMatch;
        if ( !StringUtils.isBlank(config.getFactoryPid()) ) {
            toMatch = this.configAdmin.getConfiguration(config.getPid(), null);
        }
        else {
            toMatch = this.configAdmin.getConfiguration(config.getPid(), null);
        }

        Set<String> expkeys = config.getProperties() != null ? new HashSet<>(Collections.list(config.getProperties().keys())) : Collections.EMPTY_SET;
        Set<String> havekeys = config.getProperties() != null ? new HashSet<>(Collections.list(toMatch.getProperties().keys()))
                : Collections.EMPTY_SET;

        if ( !expkeys.equals(havekeys) ) {
            log.warn("Expected keys:" + expkeys); //$NON-NLS-1$
            log.warn("Have keys:" + havekeys); //$NON-NLS-1$
            return;
        }
        for ( String key : expkeys ) {
            Object vala = toMatch.getProperties().get(key);
            Object valb = config.getProperties().get(key);
            if ( !Objects.equals(vala, valb) ) {
                log.warn(String.format("Mismatch on key %s: expect %s have %s", key, vala, valb)); //$NON-NLS-1$
            }
        }
    }


    private static void dumpConfig ( Dictionary<String, Object> values ) {
        if ( log.isDebugEnabled() ) {
            Enumeration<String> keys = values.keys();
            while ( keys.hasMoreElements() ) {
                String key = keys.nextElement();
                Object val = values.get(key);
                log.debug(String.format(" + '%s'='%s'", key, val)); //$NON-NLS-1$
            }
        }
    }


    protected Dictionary<String, Object> getFactoryInstanceProperties ( String pid, String instanceId ) {
        Collection<ConfigContribution> collection = this.pidToSource.get(pid);

        if ( collection == null || collection.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No configuration source found for pid " + pid); //$NON-NLS-1$
            }
            return null;
        }

        List<ConfigContribution> scs = new ArrayList<>(collection);

        Collections.sort(scs, new Comparator<ConfigContribution>() {

            @Override
            public int compare ( ConfigContribution o1, ConfigContribution o2 ) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });

        Hashtable<String, Object> properties = new Hashtable<>();
        boolean found = false;
        for ( ConfigContribution config : scs ) {
            Map<String, FactoryContribution> contribs = config.getFactoryContributions().get(pid);

            if ( contribs == null || contribs.isEmpty() ) {
                continue;
            }

            FactoryContribution contrib = contribs.get(instanceId);

            if ( contrib != null ) {
                found = true;
            }

            if ( contrib == null || contrib.getProperties().isEmpty() ) {
                continue;
            }

            properties.putAll(contrib.getProperties());
        }

        if ( !found ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Configuration removed for " + pid); //$NON-NLS-1$
            }
            return null;
        }

        properties.put(INSTANCE_ID, instanceId);
        return properties;
    }


    /**
     * @param pid
     * @return
     * @throws InvalidSyntaxException
     * @throws IOException
     */
    private Configuration getExistingFactoryConfiguration ( String pid, String instanceId ) throws IOException {
        FilterBuilder fb = FilterBuilder.get();
        String filter = fb.and(fb.eq(ConfigurationAdmin.SERVICE_FACTORYPID, pid), fb.eq(INSTANCE_ID, instanceId)).toString();

        Configuration[] configs;
        try {
            configs = this.configAdmin.listConfigurations(filter);
        }
        catch ( InvalidSyntaxException e ) {
            throw new IllegalArgumentException(e);
        }

        if ( configs == null || configs.length == 0 ) {
            return null;
        }

        if ( configs.length > 1 ) {
            throw new IOException("Multiple configuration instances matching the instance id"); //$NON-NLS-1$
        }

        return configs[ 0 ];
    }


    protected void updateFactoryInstanceProperties ( String pid, String instanceId, boolean force ) throws IOException {
        Dictionary<String, Object> values = this.getFactoryInstanceProperties(pid, instanceId);

        Configuration config = this.getExistingFactoryConfiguration(pid, instanceId);

        if ( values == null || values.isEmpty() ) {
            if ( config != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Removing factory configuration for pid %s instance %s", pid, instanceId)); //$NON-NLS-1$
                }
                config.delete();
                this.managedFactoryConfigurations.remove(config);
            }
            return;
        }

        if ( config == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding factory configuration for pid %s instance %s", pid, instanceId)); //$NON-NLS-1$
            }
            config = this.configAdmin.createFactoryConfiguration(pid, null);
            this.managedFactoryConfigurations.add(config);
        }

        if ( checkChanged(config, values, force) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Updating factory configuration for pid %s instance %s", pid, instanceId)); //$NON-NLS-1$
                dumpConfig(values);
            }

            config.update(values);
            verify(config);
        }
    }


    /**
     * @param config
     * @param force
     * @param values
     * @return
     */
    private static boolean checkChanged ( Configuration config, Dictionary<String, Object> newConf, boolean force ) {

        if ( force ) {
            return true;
        }

        if ( config == null ) {
            log.trace("Config is null"); //$NON-NLS-1$
            return true;
        }

        Dictionary<String, Object> oldConf = config.getProperties();

        if ( oldConf == null || newConf == null ) {
            log.trace("Old or new config is null"); //$NON-NLS-1$
            return true;
        }

        if ( checkKeyDiff(newConf, oldConf) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("keyDiff new->old"); //$NON-NLS-1$
            }
            return true;
        }

        if ( checkKeyDiff(oldConf, newConf) ) {
            log.trace("keyDiff old->new"); //$NON-NLS-1$
            return true;
        }

        Set<String> keys = keysToSet(newConf.keys());

        for ( String key : keys ) {
            if ( compareValues(newConf, oldConf, key) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("valDiff " + key); //$NON-NLS-1$
                }
                return true;
            }
        }
        return false;
    }


    private static boolean compareValues ( Dictionary<String, Object> newConf, Dictionary<String, Object> oldConf, String key ) {
        Object oldVal = oldConf.get(key);
        Object newVal = newConf.get(key);

        if ( oldVal == null && newVal == null ) {
            return false;
        }
        if ( oldVal == null ) {
            return true;
        }
        else if ( newVal == null ) {
            return true;
        }
        else if ( !oldVal.equals(newVal) ) {
            return true;
        }

        return false;
    }


    private static boolean checkKeyDiff ( Dictionary<String, Object> newConf, Dictionary<String, Object> oldConf ) {
        Set<String> oldKeyDiff = keysToSet(oldConf.keys());
        oldKeyDiff.removeAll(keysToSet(newConf.keys()));

        if ( !oldKeyDiff.isEmpty() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Difference " + StringUtils.join(oldKeyDiff, ',')); //$NON-NLS-1$
            }
            return true;
        }

        return false;
    }


    /**
     * @param keys
     * @return
     */
    private static Set<String> keysToSet ( Enumeration<String> keys ) {
        Set<String> vals = new HashSet<>();

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();

            if ( !CONFIG_KEY_IGNORES.contains(key) ) {
                vals.add(key);
            }
        }

        return vals;
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.bundleConfigTracker.close();
        shutdown();
    }


    /**
     * 
     */
    public synchronized void shutdown () {
        for ( Configuration config : this.managedFactoryConfigurations ) {
            removeFactoryConfiguration(config);
        }

        this.managedFactoryConfigurations.clear();
    }


    /**
     * @param config
     */
    private static void removeFactoryConfiguration ( Configuration config ) {
        String pid = config.getPid();
        if ( log.isDebugEnabled() ) {
            log.debug("Removing managed factory configuration " + pid); //$NON-NLS-1$
        }
        try {
            config.delete();
        }
        catch ( IOException e ) {
            log.error("Failed to remove managed factory configuration " + pid, e); //$NON-NLS-1$
        }
    }


    @Override
    public Collection<ConfigContribution> getSourcesForPid ( String pid ) {
        return this.pidToSource.get(pid);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * 
     * @see eu.agno3.runtime.configloader.ConfigLoader#reload(java.lang.String)
     */
    @Override
    public synchronized void reload ( String hint ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Reloading " + hint); //$NON-NLS-1$
        }
        for ( ConfigContribution contrib : this.sources ) {
            refreshContribution(hint, contrib);
        }

        try {
            this.notifyReconfigure();
            if ( hint == null || hint.isEmpty() ) {
                this.updateAll();
            }
            else if ( hint.indexOf('@') >= 0 ) {
                int sepPos = hint.indexOf('@');
                String factoryPid = hint.substring(0, sepPos);
                String instanceId = hint.substring(sepPos + 1);
                this.updateFactoryInstanceProperties(factoryPid, instanceId, false);
            }
            else {
                this.updatePid(hint, false);
            }
        }
        finally {
            this.notifyReconfigureDone();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.configloader.ConfigLoader#forceReload(java.lang.String)
     */
    @Override
    public synchronized void forceReload ( String hint ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Force reloading " + hint); //$NON-NLS-1$
        }
        for ( ConfigContribution contrib : this.sources ) {
            refreshContribution(hint, contrib);
        }

        if ( hint == null || hint.isEmpty() ) {
            return;
        }
        try {
            this.notifyReconfigure();
            if ( hint.indexOf('@') >= 0 ) {
                int sepPos = hint.indexOf('@');
                String factoryPid = hint.substring(0, sepPos);
                String instanceId = hint.substring(sepPos + 1);
                this.updateFactoryInstanceProperties(factoryPid, instanceId, true);
            }
            else {
                this.updatePid(hint, true);
            }
        }
        finally {
            this.notifyReconfigureDone();
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.ConfigLoader#reload(java.util.Set)
     */
    @Override
    public synchronized void reload ( Set<String> pids ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Reloading PIDs " + pids); //$NON-NLS-1$
        }

        try {
            this.notifyReconfigure();
            List<String> removals = new ArrayList<>();

            for ( String pid : pids ) {
                for ( ConfigContribution contrib : this.sources ) {
                    refreshContribution(pid, contrib);
                }

                checkRemovals(removals, pid);
            }

            Set<String> unordered = new HashSet<>(pids);
            unordered.removeAll(removals);
            List<String> ordered = new ArrayList<>();
            ordered.addAll(removals);
            ordered.addAll(unordered);

            for ( String pid : ordered ) {
                if ( pid.indexOf('@') >= 0 ) {
                    int sepPos = pid.indexOf('@');
                    String factoryPid = pid.substring(0, sepPos);
                    String instanceId = pid.substring(sepPos + 1);
                    this.updateFactoryInstanceProperties(factoryPid, instanceId, false);
                }
                else {
                    this.updatePid(pid, false);
                }
            }

        }
        finally {
            this.notifyReconfigureDone();
        }
    }


    /**
     * @param removals
     * @param pid
     */
    private void checkRemovals ( List<String> removals, String pid ) {
        if ( pid.indexOf('@') >= 0 ) {
            int sepPos = pid.indexOf('@');
            String factoryPid = pid.substring(0, sepPos);
            String instanceId = pid.substring(sepPos + 1);

            if ( !this.pidToSource.containsKey(factoryPid) ) {
                removals.add(pid);
                return;
            }

            boolean found = false;
            for ( ConfigContribution cc : this.pidToSource.get(factoryPid) ) {

                if ( cc.getFactoryContributions().get(factoryPid) != null && cc.getFactoryContributions().get(factoryPid).containsKey(instanceId) ) {
                    found = true;
                    break;
                }
            }

            if ( !found ) {
                removals.add(pid);
            }
        }
        else {
            if ( !this.pidToSource.containsKey(pid) ) {
                removals.add(pid);
            }
        }
    }


    /**
     * @param hint
     * @param contrib
     */
    protected void refreshContribution ( String hint, ConfigContribution contrib ) {
        Set<String> oldPids = new HashSet<>(findFactoryInstances(Arrays.asList(contrib)).keySet());
        oldPids.addAll(contrib.getRegularProperties().keySet());
        contrib.reload(hint);
        Set<String> newPids = new HashSet<>(findFactoryInstances(Arrays.asList(contrib)).keySet());
        newPids.addAll(contrib.getRegularProperties().keySet());

        Set<String> removedPids = new HashSet<>(oldPids);
        removedPids.removeAll(newPids);
        Set<String> addedPids = new HashSet<>(newPids);
        addedPids.removeAll(oldPids);

        for ( String pid : removedPids ) {
            this.pidToSource.removeMapping(pid, contrib);
        }

        for ( String pid : addedPids ) {
            this.pidToSource.put(pid, contrib);
        }
    }
}
