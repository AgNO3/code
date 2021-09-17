/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx;


import java.util.Arrays;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * @author mbechler
 *
 */
public final class MethodEntry {

    private final ObjectName name;
    private final String method;
    private final String[] signature;


    /**
     * @param n
     * @param m
     * @param signature
     * @throws MalformedObjectNameException
     */
    public MethodEntry ( String n, String m, String... signature ) throws MalformedObjectNameException {
        this(new ObjectName(n), m, signature);
    }


    /**
     * @param n
     * @param m
     * @param signature
     */
    public MethodEntry ( ObjectName n, String m, String... signature ) {
        this.name = n;
        this.method = m;
        this.signature = signature;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.name.hashCode() + 3 * this.method.hashCode() + 5 * Arrays.hashCode(this.signature);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( ! ( obj instanceof MethodEntry ) ) {
            return false;
        }
        MethodEntry me = (MethodEntry) obj;
        return this.name.equals(me.name) && this.method.equals(me.method) && Arrays.equals(this.signature, me.signature);
    }
}