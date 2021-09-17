/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.05.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.security.password.PasswordGenerationException;
import eu.agno3.runtime.security.password.PasswordGenerator;
import eu.agno3.runtime.security.password.PasswordType;


/**
 * @author mbechler
 *
 */
@Component ( service = PasswordGenerator.class )
public class PasswordGeneratorImpl implements PasswordGenerator {

    /**
     * 
     */
    private static final double LOG2 = Math.log(2);

    private static final List<Character> RANDOM_CHARS = new ArrayList<>();
    private static final Set<Character> BLACKLIST_CHARS = new HashSet<>(Arrays.asList('l', '0'));

    static {

        for ( char c = 33; c < 126; c++ ) {
            if ( !BLACKLIST_CHARS.contains(c) ) {
                RANDOM_CHARS.add(c);
            }
        }
    }

    private SecureRandomProvider secureRandomProvider;

    private SecureRandom secureRandom;


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
        }
    }


    /**
     * @return the secureRandom
     */
    public SecureRandom getSecureRandom () {
        if ( this.secureRandom == null ) {
            this.secureRandom = this.secureRandomProvider.getSecureRandom();
        }
        return this.secureRandom;
    }


    /**
     * Test only
     * 
     * @param secureRandom
     *            the secureRandom to set
     */
    public void setSecureRandom ( SecureRandom secureRandom ) {
        this.secureRandom = secureRandom;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws PasswordGenerationException
     *
     * @see eu.agno3.runtime.security.password.PasswordGenerator#generate(eu.agno3.runtime.security.password.PasswordType,
     *      int, java.util.Locale)
     */
    @Override
    public String generate ( PasswordType type, int entropy, Locale l ) throws PasswordGenerationException {

        int base = getBase(type);

        byte[] random = new byte[entropy / 8 + (int) ( Math.ceil(Math.log(base + 1) / LOG2) / 8 + 1 )];
        getSecureRandom().nextBytes(random);

        int[] converted = baseConvert(new BigInteger(1, random), base);
        int[] truncated = new int[(int) ( entropy / ( Math.ceil(Math.log(base + 1) / LOG2) ) ) + 1];
        System.arraycopy(converted, converted.length - truncated.length, truncated, 0, truncated.length);

        return translatePassword(type, l, truncated);
    }


    /**
     * @param type
     * @param l
     * @param truncated
     * @return
     * @throws PasswordGenerationException
     */
    private String translatePassword ( PasswordType type, Locale l, int[] truncated ) throws PasswordGenerationException {

        switch ( type ) {
        case DEFAULT:
        case DICEWARE:
            return translateDiceware(truncated, l);
        case NUMBERS:
            return translateNumbers(truncated);
        case RANDOM:
            return translateRandom(truncated);

        default:
            throw new IllegalArgumentException();
        }
    }


    /**
     * @param truncated
     * @return
     */
    private static String translateRandom ( int[] truncated ) {
        StringBuilder sb = new StringBuilder();

        for ( int i : truncated ) {
            sb.append(RANDOM_CHARS.get(i));
        }
        return sb.toString();
    }


    /**
     * @param truncated
     * @return
     */
    private static String translateNumbers ( int[] truncated ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( int i : truncated ) {
            if ( !first ) {
                sb.append(' ');
            }
            else {
                first = false;
            }
            sb.append(i);
        }

        return sb.toString();
    }


    /**
     * @param truncated
     * @return
     * @throws PasswordGenerationException
     * @throws IOException
     */
    private String translateDiceware ( int[] truncated, Locale l ) throws PasswordGenerationException {
        String translated[] = new String[truncated.length];
        try ( InputStream in = getDicewareWordList(l);
              InputStreamReader ir = new InputStreamReader(in, Charset.forName("UTF-8")); //$NON-NLS-1$
              BufferedReader br = new BufferedReader(ir) ) {

            int lineNum = 0;
            String line;
            while ( ( line = br.readLine() ) != null ) {
                for ( int i = 0; i < truncated.length; i++ ) {
                    if ( truncated[ i ] == lineNum ) {
                        translated[ i ] = line.trim();
                    }
                }
                lineNum++;
            }
        }
        catch ( IOException e ) {
            throw new PasswordGenerationException("Failed to read word list", e); //$NON-NLS-1$
        }

        return StringUtils.join(translated, StringUtils.SPACE);
    }


    /**
     * @param l
     * @return
     */
    private InputStream getDicewareWordList ( Locale l ) {
        return this.getClass().getClassLoader().getResourceAsStream("eu/agno3/runtime/security/password/diceware/wordlist_en.txt"); //$NON-NLS-1$
    }


    /**
     * @param type
     * @return
     */
    private static int getBase ( PasswordType type ) {
        int base;

        switch ( type ) {
        case DEFAULT:
        case DICEWARE:
            base = 7776;
            break;
        case NUMBERS:
            base = 1000;
            break;
        case RANDOM:
            base = RANDOM_CHARS.size();
            break;
        default:
            throw new IllegalArgumentException();
        }
        return base;
    }


    /**
     * @param random
     * @param base
     * @return base converted number
     */
    public static int[] baseConvert ( BigInteger random, int base ) {
        BigInteger bigBase = BigInteger.valueOf(base);
        List<Integer> output = new LinkedList<>();

        BigInteger cur = random;

        while ( cur.signum() > 0 ) {
            BigInteger res[] = cur.divideAndRemainder(bigBase);
            BigInteger div = res[ 0 ];
            BigInteger rem = res[ 1 ];
            output.add(rem.intValueExact());
            cur = div;
        }

        if ( output.isEmpty() ) {
            return new int[] {
                0
            };
        }

        int[] res = new int[output.size()];
        Iterator<Integer> it = output.iterator();
        for ( int i = res.length - 1; i >= 0; i-- ) {
            Integer num = it.next();
            res[ i ] = num;
        }

        return res;
    }
}
