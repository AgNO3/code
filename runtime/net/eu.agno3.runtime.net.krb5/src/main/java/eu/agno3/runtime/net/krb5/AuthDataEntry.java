/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.util.Arrays;


/**
 * @author mbechler
 *
 */
public class AuthDataEntry {

    private int type;
    private byte[] data;


    /**
     * @param type
     * @param data
     */
    public AuthDataEntry ( int type, byte[] data ) {
        this.type = type;
        this.data = Arrays.copyOf(data, data.length);
    }


    /**
     * @return the type
     */
    public int getType () {
        return this.type;
    }


    /**
     * @return the data
     */
    public byte[] getData () {
        return this.data;
    }

}
