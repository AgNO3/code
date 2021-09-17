/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.system.base.units.file.contents.TemplateContentProvider;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;


/**
 * @author mbechler
 * @param <T>
 *            configuration type
 * @param <TJob>
 *            configuration job type
 * 
 */
public class ConfigurationJobContext <T extends ConfigurationInstance, TJob extends ConfigurationJob> {

    private static final Logger log = Logger.getLogger(ConfigurationJobContext.class);

    private List<Method> curMethodCalls = new ArrayList<>();

    private TJob job;
    private T newConfig;
    private Optional<T> curConfig = Optional.empty();

    @NonNull
    private Class<T> cfgClass;

    private MatcherProxyBuilder proxyBuilder;
    private ConfigurationJobServiceContext sctx;

    private BaseSystemInformationContext sysinfoCtx;

    private NetworkInformation netInfo;

    private StorageInformation storageInfo;

    private PlatformInformation platformInfo;

    private Class<?> builderClass;

    private final DateTime now;
    private final boolean force;


    /**
     * @param builder
     * @param proxyClassLoader
     * @param cfgClass
     * @param j
     * @param sctx
     * @param sysinfoCtx
     * @param nc
     * @param cur
     * @param force
     */
    public ConfigurationJobContext ( ConfigJobBuilder<T, TJob> builder, ClassLoader proxyClassLoader, @NonNull Class<T> cfgClass, TJob j,
            ConfigurationJobServiceContext sctx, BaseSystemInformationContext sysinfoCtx, T nc, Optional<T> cur, boolean force ) {
        this.force = force;
        this.builderClass = builder.getClass();
        this.sctx = sctx;
        this.sysinfoCtx = sysinfoCtx;
        this.proxyBuilder = new MatcherProxyBuilder(proxyClassLoader);
        this.cfgClass = cfgClass;
        this.job = j;
        this.newConfig = nc;
        this.curConfig = cur;
        this.now = DateTime.now();
    }


    /**
     * @return the configuration job
     */
    public TJob job () {
        return this.job;
    }


    /**
     * @return the new configuration
     */
    public T cfg () {
        return this.newConfig;
    }


    /**
     * @return the currently applied configuration, null if none
     */
    public Optional<T> cur () {
        return this.curConfig;
    }


    /**
     * @return the now
     */
    public DateTime getNow () {
        return this.now;
    }


    /**
     * @return the force
     */
    public boolean isForce () {
        return this.force;
    }


    /**
     * @return the service context
     */
    public ConfigurationJobServiceContext sctx () {
        return this.sctx;
    }


    /**
     * 
     * @return the network runtime information
     * @throws SystemInformationException
     */
    public NetworkInformation netInfo () throws SystemInformationException {
        if ( this.netInfo == null ) {
            this.netInfo = this.sysinfoCtx.getNetworkInformation();
        }
        return this.netInfo;
    }


    /**
     * 
     * @return the storage runtime information
     * @throws SystemInformationException
     */
    public StorageInformation storageInfo () throws SystemInformationException {
        if ( this.storageInfo == null ) {
            this.storageInfo = this.sysinfoCtx.getStorageInformation();
        }
        return this.storageInfo;
    }


    /**
     * 
     * @return the platform runtime information
     * @throws SystemInformationException
     */
    public PlatformInformation platformInfo () throws SystemInformationException {
        if ( this.platformInfo == null ) {
            this.platformInfo = this.sysinfoCtx.getPlatformInformation();
        }
        return this.platformInfo;
    }


    /**
     * @param tplName
     * @return a template content provider for the specified template
     * @throws IOException
     */
    public TemplateContentProvider tpl ( String tplName ) throws IOException {
        return new TemplateContentProvider(this.builderClass, tplName, makeTemplateContext(this.cfg(), null));
    }


    /**
     * @param tplName
     * @param extraContext
     *            extra context variables
     * @return a template content provider for the specified template
     * @throws IOException
     */
    public TemplateContentProvider tpl ( String tplName, Map<String, Serializable> extraContext ) throws IOException {
        return new TemplateContentProvider(this.builderClass, tplName, makeTemplateContext(this.cfg(), extraContext));
    }


    /**
     * @param nc
     * @return
     */
    private Serializable makeTemplateContext ( ConfigurationObject cfg, Map<String, Serializable> extraContext ) {
        HashMap<String, Serializable> ctx = new HashMap<>();
        if ( extraContext != null ) {
            ctx.putAll(extraContext);
        }
        ctx.put("cfg", cfg); //$NON-NLS-1$

        if ( cfg.getRevision() == null ) {
            throw new IllegalArgumentException("Configuration revision is not set"); //$NON-NLS-1$
        }
        ctx.put("rev", cfg.getRevision()); //$NON-NLS-1$
        ctx.put("generatedDate", getNow()); //$NON-NLS-1$
        return ctx;
    }


    /**
     * @return whether a full update is required
     */
    public boolean needUpdate () {
        return this.job.getApplyInfo().isForce() || this.curConfig == null;
    }


    /**
     * @return whether to make sure services are not restarted
     */
    public boolean preventRestart () {
        return this.job.getNoRestart();
    }


    /**
     * @return a matcher for the changed method
     * @throws MatcherException
     */
    public @NonNull T match () throws MatcherException {
        this.curMethodCalls.clear();
        try {
            @Nullable
            T proxy = this.proxyBuilder.buildMatcherProxy(this.cfgClass, this.curMethodCalls);

            if ( proxy == null ) {
                throw new MatcherException("Root matcher is null"); //$NON-NLS-1$
            }

            return proxy;
        }
        catch ( Exception e ) {
            throw new MatcherException("Failed to build matcher proxy:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param o
     * @return whether the specified configuration path changed
     * @throws MatcherException
     */
    public boolean changed ( Object o ) throws MatcherException {

        if ( this.needUpdate() ) {
            return true;
        }

        if ( this.curMethodCalls.isEmpty() ) {
            throw new IllegalStateException("No matching took place"); //$NON-NLS-1$
        }

        List<Method> m = this.curMethodCalls;

        try {
            Object cv = null;
            if ( this.curConfig.isPresent() ) {
                cv = this.proxyBuilder.applyMethodChain(this.curConfig.get(), m);
            }
            Object nv = this.proxyBuilder.applyMethodChain(this.newConfig, m);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("New value: %s, cur value: %s", nv, cv)); //$NON-NLS-1$
            }

            return !doValuesMatch(cv, nv);
        }
        catch ( Exception e ) {
            throw new MatcherException("Failed to get config values", e); //$NON-NLS-1$
        }
    }


    private static boolean doValuesMatch ( Object cv, Object nv ) {
        if ( cv == null && nv == null ) {
            return false;
        }
        else if ( cv == null ) {
            return true;
        }
        else if ( nv == null ) {
            return true;
        }
        else {
            // TODO: this only works for primitivate values
            return cv.equals(nv);
        }
    }

}
