/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dbus;


import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.freedesktop.dbus.types.Variant;


/**
 * @author mbechler
 * 
 */
public final class DBUSUtil {

    /**
     * 
     */
    private DBUSUtil () {}


    static Object dumpByteArray ( Logger l, Object val ) {
        Object tmpVal = val;
        try {
            CharsetDecoder dec = Charset.forName("UTF-8").newDecoder(); //$NON-NLS-1$
            dec.onMalformedInput(CodingErrorAction.REPORT);
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer buf = ByteBuffer.wrap((byte[]) tmpVal);
            tmpVal = dec.decode(buf);
        }
        catch ( CharacterCodingException ex ) {
            l.trace("Failed to decode to string:", ex); //$NON-NLS-1$
            tmpVal = Arrays.toString((byte[]) tmpVal);
        }
        return tmpVal;
    }


    static Object dumpArrayObject ( Logger l, Object val ) {
        Object tmpVal = val;
        if ( tmpVal instanceof Object[] ) {
            tmpVal = Arrays.deepToString((Object[]) tmpVal);
        }
        else if ( tmpVal instanceof byte[] ) {
            tmpVal = dumpByteArray(l, tmpVal);
        }
        return tmpVal;
    }


    static void dumpProperties ( Logger log, Level l, Map<String, Variant<?>> props ) {
        for ( Entry<String, Variant<?>> e : props.entrySet() ) {
            Object val = e.getValue().getValue();

            if ( val.getClass().isArray() ) {
                val = dumpArrayObject(log, val);
            }
            log.log(l, String.format("%s: %s (%s)", e.getKey(), val, val.getClass())); //$NON-NLS-1$
        }
    }

}
