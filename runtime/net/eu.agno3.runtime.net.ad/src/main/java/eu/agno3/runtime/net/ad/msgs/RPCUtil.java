/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.math.BigInteger;

import org.joda.time.DateTime;

import jcifs.SmbConstants;
import jcifs.dcerpc.rpc.unicode_string;
import jcifs.dcerpc.ndr.NdrBuffer;


/**
 * @author mbechler
 *
 */
public class RPCUtil {

    /**
     * @param buf
     * @return
     */
    static DateTime decodeFiletime ( NdrBuffer buf ) {
        buf.align(8);
        long last = buf.dec_ndr_long() & 0xffffffffL;
        long first = buf.dec_ndr_long() & 0xffffffffL;
        if ( first != 0x7fffffffL && last != 0xffffffffL && first != 0L && last != 0L ) {
            BigInteger lastBigInt = BigInteger.valueOf(last);
            BigInteger firstBigInt = BigInteger.valueOf(first);
            BigInteger completeBigInt = lastBigInt.add(firstBigInt.shiftLeft(32));
            completeBigInt = completeBigInt.divide(BigInteger.valueOf(10000L));
            completeBigInt = completeBigInt.add(BigInteger.valueOf(-SmbConstants.MILLISECONDS_BETWEEN_1970_AND_1601));
            return new DateTime(completeBigInt.longValue());
        }

        return null;
    }


    /**
     * @param buf
     * @param efPtr
     * @param effectiveName2
     */
    static void decodeStringVal ( NdrBuffer buf, int ptr, unicode_string str ) {
        if ( ptr == 0 ) {
            return;
        }
        int maxlength = buf.dec_ndr_long();
        buf.advance(4);
        int length = buf.dec_ndr_long();
        if ( str.buffer == null ) {
            str.buffer = new short[maxlength];
        }
        for ( int i = 0; i < length; i++ ) {
            str.buffer[ i ] = (short) buf.dec_ndr_short();
        }
    }


    /**
     * @param buf
     * @param effectiveName2
     * @return
     */
    static int decodeStringRef ( NdrBuffer buf, unicode_string str ) {
        str.length = (short) buf.dec_ndr_short();
        str.maximum_length = (short) buf.dec_ndr_short();
        return buf.dec_ndr_long();
    }

}
