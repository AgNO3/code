/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.krb5.ETypesUtil;


/**
 * @author mbechler
 *
 */
public class ImportKeyConfigurator extends RealmConfigurator<StatusOnlyResult, ImportKey, ImportKeyConfigurator> {

    /**
     * @param unit
     */
    public ImportKeyConfigurator ( ImportKey unit ) {
        super(unit);
    }


    /**
     * 
     * @param keytab
     * @return this configurator
     */
    public ImportKeyConfigurator keytab ( String keytab ) {
        getExecutionUnit().setKeytab(keytab);
        return this.self();
    }


    /**
     * @param keys
     * @return this configurator
     */
    public ImportKeyConfigurator krbKeys ( Collection<KerberosKey> keys ) {
        getExecutionUnit().setKeys(new ArrayList<>(keys));
        return this.self();
    }


    /**
     * @param keys
     * @return this configurator
     */
    public ImportKeyConfigurator keys ( Collection<KeyData> keys ) {
        List<KerberosKey> wrapped = new ArrayList<>();
        for ( KeyData kd : keys ) {
            KerberosPrincipal principal = new KerberosPrincipal(kd.getPrincipal());
            wrapped.add(new KerberosKey(principal, Base64.decodeBase64(kd.getData()), ETypesUtil.eTypeFromMITString(kd.getAlgorithm()), (int) kd
                    .getKvno()));
        }
        return krbKeys(wrapped);
    }
}
