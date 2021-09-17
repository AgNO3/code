/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.util.uuid;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public final class UUIDUtil {

    /**
     * 
     */
    private UUIDUtil () {}


    /**
     * @param uuid
     * @return 16 byte array
     */
    public static byte[] toBytes ( UUID uuid ) {
        byte[] bytes = new byte[16];
        System.arraycopy(fromLong(uuid.getMostSignificantBits()), 0, bytes, 0, 8);
        System.arraycopy(fromLong(uuid.getLeastSignificantBits()), 0, bytes, 8, 8);
        return bytes;
    }


    /**
     * @param value
     *            a 16 byte array
     * @return the UUID
     */
    public static UUID fromBytes ( byte[] value ) {
        byte[] msb = new byte[8];
        byte[] lsb = new byte[8];
        System.arraycopy(value, 0, msb, 0, 8);
        System.arraycopy(value, 8, lsb, 0, 8);
        return new UUID(asLong(msb), asLong(lsb));
    }


    private static byte[] fromLong ( long longValue ) {
        byte[] bytes = new byte[8];
        bytes[ 0 ] = (byte) ( longValue >> 56 );
        bytes[ 1 ] = (byte) ( ( longValue << 8 ) >> 56 );
        bytes[ 2 ] = (byte) ( ( longValue << 16 ) >> 56 );
        bytes[ 3 ] = (byte) ( ( longValue << 24 ) >> 56 );
        bytes[ 4 ] = (byte) ( ( longValue << 32 ) >> 56 );
        bytes[ 5 ] = (byte) ( ( longValue << 40 ) >> 56 );
        bytes[ 6 ] = (byte) ( ( longValue << 48 ) >> 56 );
        bytes[ 7 ] = (byte) ( ( longValue << 56 ) >> 56 );
        return bytes;
    }


    private static long asLong ( byte[] bytes ) {
        if ( bytes == null ) {
            return 0;
        }
        if ( bytes.length != 8 ) {
            throw new IllegalArgumentException("Expecting 8 byte values to construct a long"); //$NON-NLS-1$
        }
        long value = 0;
        for ( int i = 0; i < 8; i++ ) {
            value = ( value << 8 ) | ( bytes[ i ] & 0xff );
        }
        return value;
    }
}