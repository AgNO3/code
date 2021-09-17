/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11.internal;


import java.io.IOException;
import java.security.AuthProvider;
import java.security.Security;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component (
    service = PKCS11TokenConfiguration.class,
    configurationPid = PKCS11TokenConfiguration.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class PKCS11TokenConfigurationImpl implements PKCS11TokenConfiguration {

    private static final Logger log = Logger.getLogger(PKCS11TokenConfigurationImpl.class);

    private String id;
    private String library;
    private String pin;
    private int slotIndex;
    private String slotId;
    private String extraConfig;
    private String initArgs;

    private PKCS11Util pkcs11Util;
    private AuthProvider provider;


    @Reference
    protected synchronized void setPKCS11Util ( PKCS11Util p11util ) {
        this.pkcs11Util = p11util;
    }


    protected synchronized void unsetPKCS11Util ( PKCS11Util p11util ) {
        if ( this.pkcs11Util == p11util ) {
            this.pkcs11Util = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException, IOException {
        parseId(ctx);
        parsePropertyConfig(ctx, true);
        setupProvider();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) throws CryptoException, IOException {
        deactivate(ctx);
        activate(ctx);
    }


    /**
     * @param ctx
     * @throws CryptoException
     */
    protected void parseId ( ComponentContext ctx ) throws CryptoException {
        String idSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.ID);
        if ( StringUtils.isBlank(idSpec) ) {
            throw new CryptoException("No key instanceId given"); //$NON-NLS-1$
        }
        this.id = idSpec.trim();
    }


    /**
     * @param ctx
     * @throws CryptoException
     * @throws IOException
     */
    protected void parsePropertyConfig ( ComponentContext ctx, boolean require ) throws CryptoException, IOException {
        String librarySpec = (String) ctx.getProperties().get("library"); //$NON-NLS-1$
        if ( require && StringUtils.isBlank(librarySpec) ) {
            throw new CryptoException("PKCS11 token configuration needs library"); //$NON-NLS-1$
        }
        else if ( !StringUtils.isBlank(librarySpec) ) {
            this.library = librarySpec.trim();
        }

        this.pin = ConfigUtil.parseSecret(ctx.getProperties(), "pin", null); //$NON-NLS-1$

        String slotIdSpec = (String) ctx.getProperties().get("slotId"); //$NON-NLS-1$
        String slotIndexSpec = (String) ctx.getProperties().get("slotIndex"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(slotIdSpec) ) {
            this.slotId = slotIdSpec.trim();
        }
        else if ( !StringUtils.isBlank(slotIndexSpec) ) {
            this.slotIndex = Integer.parseInt(slotIndexSpec.trim());
        }
        else {
            this.slotIndex = 0;
        }

        String extraConfigSpec = (String) ctx.getProperties().get("extraConfig"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(extraConfigSpec) ) {
            this.extraConfig = extraConfigSpec;
        }

        String initArgsSpec = (String) ctx.getProperties().get("initArgs"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(initArgsSpec) ) {
            this.initArgs = initArgsSpec.trim();
        }
    }


    /**
     * @throws CryptoException
     * 
     */
    protected final void setupProvider () throws CryptoException {
        this.provider = this.pkcs11Util.getProviderFor(
            this.getLibrary(),
            this.getInstanceId(),
            this.getPIN(),
            this.getSlotId(),
            this.getSlotIndex(),
            this.getExtraConfig(),
            this.getInitArgs());
        Security.addProvider(this.provider);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        Security.removeProvider(this.provider.getName());
        try {
            this.pkcs11Util.close(this.provider);
        }
        catch ( CryptoException e ) {
            log.warn("Failed to close provider", e); //$NON-NLS-1$
        }
        this.provider = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getProvider()
     */
    @Override
    public AuthProvider getProvider () {
        return this.provider;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getInstanceId()
     */
    @Override
    public String getInstanceId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getLibrary()
     */
    @Override
    public String getLibrary () {
        return this.library;
    }


    /**
     * @param library
     *            the library to set
     */
    protected void setLibrary ( String library ) {
        this.library = library;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getPIN()
     */
    @Override
    public String getPIN () {
        return this.pin;
    }


    /**
     * @param pin
     *            the pin to set
     */
    protected void setPIN ( String pin ) {
        this.pin = pin;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getSlotIndex()
     */
    @Override
    public int getSlotIndex () {
        return this.slotIndex;
    }


    /**
     * @param slotIndex
     *            the slotIndex to set
     */
    protected void setSlotIndex ( int slotIndex ) {
        this.slotIndex = slotIndex;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getSlotId()
     */
    @Override
    public String getSlotId () {
        return this.slotId;
    }


    /**
     * @param slotId
     *            the slotId to set
     */
    protected void setSlotId ( String slotId ) {
        this.slotId = slotId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration#getExtraConfig()
     */
    @Override
    public String getExtraConfig () {
        return this.extraConfig;
    }


    /**
     * @param extraConfig
     *            the extraConfig to set
     */
    protected void setExtraConfig ( String extraConfig ) {
        this.extraConfig = extraConfig;
    }


    /**
     * @return the initArgs
     */
    @Override
    public String getInitArgs () {
        return this.initArgs;
    }


    /**
     * @param initArgs
     *            the initArgs to set
     */
    protected void setInitArgs ( String initArgs ) {
        this.initArgs = initArgs;
    }

}
