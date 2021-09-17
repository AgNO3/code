/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;

import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public class DomainPasswordInformation extends DomainInformation {

    /**
     * 
     */
    public static final int DOMAIN_PASSWORD_COMPLEX = 0x1;

    /**
     * 
     */
    public static final int DOMAIN_PASSWORD_NO_ANON_CHANGE = 0x2;

    /**
     * 
     */
    public static final int DOMAIN_PASSWORD_NO_CLEAR_CHANGE = 0x4;

    /**
     * 
     */
    public static final int DOMAIN_PASSWORD_LOCKOUT_ADMINS = 0x8;

    /**
     * 
     */
    public static final int DOMAIN_PASSWORD_STORE_CLEARTEXT = 0x10;

    /**
     * 
     */
    public static final int DOMAIN_REFUSE_PASSWORD_CHANGE = 0x20;

    private int minPasswordLength;
    private int passwordHistoryLength;

    private int passwordProperties;
    private Duration maxPasswordAge;
    private Duration minPasswordAge;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.DomainInformation#getDomainInformationClass()
     */
    @Override
    public int getDomainInformationClass () {
        return 1;
    }


    /**
     * @return the minPasswordLength
     */
    public int getMinPasswordLength () {
        return this.minPasswordLength;
    }


    /**
     * @return the passwordHistoryLength
     */
    public int getPasswordHistoryLength () {
        return this.passwordHistoryLength;
    }


    /**
     * @return the passwordProperties
     */
    public int getPasswordProperties () {
        return this.passwordProperties;
    }


    /**
     * @return the minPasswordAge
     */
    public Duration getMinPasswordAge () {
        return this.minPasswordAge;
    }


    /**
     * @return the maxPasswordAge
     */
    public Duration getMaxPasswordAge () {
        return this.maxPasswordAge;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {
        this.minPasswordLength = buf.dec_ndr_short();
        this.passwordHistoryLength = buf.dec_ndr_short();
        this.passwordProperties = buf.dec_ndr_long();
        this.maxPasswordAge = Duration.millis(-1 * buf.dec_ndr_hyper() / 10000);
        this.minPasswordAge = Duration.millis(-1 * buf.dec_ndr_hyper() / 10000);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {
        buf.enc_ndr_short(this.minPasswordLength);
        buf.enc_ndr_short(this.passwordHistoryLength);
        buf.enc_ndr_long(this.passwordProperties);
        buf.enc_ndr_hyper(this.maxPasswordAge.getMillis() * -10000);
        buf.enc_ndr_hyper(this.minPasswordAge.getMillis() * -10000);
    }

}
