/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.runtime.util.ip;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class IpUtil {

    private static final Logger log = Logger.getLogger(IpUtil.class);


    /**
     * 
     */
    private IpUtil () {}

    /**
     * 
     */
    private static final String BYTE_COMPONENTS_ERR = "Illegal address, component must be byte valued"; //$NON-NLS-1$

    protected static final Pattern V4_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){1,3}"); //$NON-NLS-1$
    protected static final Pattern V6_MAPPED_V4_PATTERN = Pattern
            .compile("(((0+:){6}|::)|((0+:){5}|::)ffff:)(\\d{1,3}(\\.\\d){1,3})", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private static final String CLOSE_BRACKET = "]"; //$NON-NLS-1$
    private static final String OPEN_BRACKET = "["; //$NON-NLS-1$
    private static final Pattern V6_PATTERN = Pattern.compile("(::|[0-9a-f]{1,4}:?){0,7}(::|[0-9a-f]{1,4})(%\\w*)?", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private static final String DOUBLE_COLON = "::"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SPEC_SEPARATOR_V6 = "%"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ADDR_SEP_V6 = ":"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ADDR_SEP_V4 = "."; //$NON-NLS-1$


    /**
     * @param address
     * @return whether this can be parsed as a IPv4 address
     */
    public static boolean isV4Address ( String address ) {
        if ( StringUtils.isBlank(address) ) {
            return false;
        }
        return isRegularV4Address(address) || isV6MappedV4Address(address);
    }


    /**
     * @param address
     * @return whether this is a regular ipv4 address
     */
    public static boolean isRegularV4Address ( String address ) {
        if ( StringUtils.isBlank(address) ) {
            return false;
        }
        return V4_PATTERN.matcher(address).matches();
    }


    /**
     * @param address
     * @return whether this is a v6 mapped ipv4 address
     */
    public static boolean isV6MappedV4Address ( String address ) {
        if ( StringUtils.isBlank(address) ) {
            return false;
        }
        return V6_MAPPED_V4_PATTERN.matcher(address).matches();
    }


    /**
     * 
     * @param address
     * @return whether this address is parsable as a IPv6 address
     */
    public static boolean isV6Address ( String address ) {
        if ( StringUtils.isBlank(address) ) {
            return false;
        }
        if ( isBracketed(address) ) {
            return isV6Address(stripBrackets(address));
        }
        return V6_PATTERN.matcher(address).matches();
    }


    private static String stripBrackets ( String address ) {
        return address.substring(1, address.length() - 1);
    }


    private static boolean isBracketed ( String address ) {
        return address.startsWith(OPEN_BRACKET) && address.endsWith(CLOSE_BRACKET);
    }


    /**
     * 
     * @param address
     * @return the parsed address
     */
    public static short[] parse ( String address ) {
        if ( isV4Address(address) ) {
            return parseV4(address);
        }

        if ( isV6Address(address) ) {
            return parseV6(address);
        }

        throw new IllegalArgumentException("Could not dermine address type for " + address); //$NON-NLS-1$
    }


    /**
     * @param data
     * @return string representation of address
     */
    public static String toString ( short[] data ) {
        if ( data.length == 16 ) {
            return toStringV6(data, null);
        }
        else if ( data.length == 4 ) {
            return String.format("%d.%d.%d.%d", data[ 0 ], data[ 1 ], data[ 2 ], data[ 3 ]); //$NON-NLS-1$
        }
        throw new IllegalArgumentException("Could not dermine address type for " + Arrays.toString(data)); //$NON-NLS-1$
    }


    /**
     * @param data
     * @param scope
     * @return V6 string representation of address
     */
    public static String toStringV6 ( short[] data, String scope ) {

        List<String> component = new ArrayList<>();
        int[] longestZeroRun = findLongestZeroRun(data);

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Found longest zero run at %d len %d", longestZeroRun[ 0 ], longestZeroRun[ 1 ])); //$NON-NLS-1$
        }

        if ( longestZeroRun[ 0 ] == 0 && longestZeroRun[ 1 ] == 8 ) {
            // zero address
            return "::"; //$NON-NLS-1$
        }

        int i = 0;
        while ( i < 8 ) {
            if ( i == longestZeroRun[ 0 ] ) {
                addDoubleColonComponents(component, longestZeroRun, i);
                i += longestZeroRun[ 1 ];
                continue;
            }

            component.add(String.format("%x", getComponentShortValue(data, i))); //$NON-NLS-1$
            i++;
        }

        return buildFromComponents(component, scope);
    }


    /**
     * @param data
     * @param i
     * @return
     */
    private static int getComponentShortValue ( short[] data, int i ) {
        return ( data[ 2 * i ] << 8 ) + data[ 2 * i + 1 ];
    }


    /**
     * @param component
     * @param longestZeroRun
     * @param i
     */
    private static void addDoubleColonComponents ( List<String> component, int[] longestZeroRun, int i ) {
        component.add(StringUtils.EMPTY);
        if ( i == 0 || i + longestZeroRun[ 1 ] == 8 ) {
            // need an extra component for :: prefix or suffix
            component.add(StringUtils.EMPTY);
        }
    }


    /**
     * @param component
     * @return
     */
    private static String buildFromComponents ( List<String> component, String scope ) {
        return StringUtils.join(component, IpUtil.ADDR_SEP_V6).concat(makeScopeSuffix(scope));
    }


    private static String makeScopeSuffix ( String scope ) {
        return scope != null ? IpUtil.SPEC_SEPARATOR_V6 + scope : StringUtils.EMPTY;
    }


    /**
     * @param data
     * @return
     */
    private static int[] findLongestZeroRun ( short[] data ) {
        int[] longestZeroRun = new int[] {
            -1, 0
        };
        int currentPos = -1;
        int currentRunLength = 0;

        for ( int i = 0; i < 8; i++ ) {
            if ( componentIsNull(data, i) ) {
                if ( stillInRun(currentPos, currentRunLength, i) ) {
                    currentRunLength++;
                }
                else {
                    currentPos = i;
                    currentRunLength = 1;
                }
            }
            else if ( stillInRun(currentPos, currentRunLength, i) ) {
                updateLongestRun(longestZeroRun, currentPos, currentRunLength);
            }
        }

        updateLongestRun(longestZeroRun, currentPos, currentRunLength);
        return longestZeroRun;
    }


    /**
     * @param data
     * @param i
     *            component numer (0..7)
     * @return
     */
    private static boolean componentIsNull ( short[] data, int i ) {
        return data[ 2 * i ] == 0x0 && data[ 2 * i + 1 ] == 0x0;
    }


    /**
     * @param currentPos
     * @param currentRunLength
     * @param i
     * @return
     */
    private static boolean stillInRun ( int currentPos, int currentRunLength, int i ) {
        return currentPos >= 0 && currentPos + currentRunLength == i;
    }


    /**
     * @param longestZeroRun
     * @param currentPos
     * @param currentRunLength
     */
    private static void updateLongestRun ( int[] longestZeroRun, int currentPos, int currentRunLength ) {
        if ( currentRunLength >= longestZeroRun[ 1 ] ) {
            longestZeroRun[ 0 ] = currentPos;
            longestZeroRun[ 1 ] = currentRunLength;
        }
    }


    /**
     * 
     * @param address
     * @return the parsed v4 address
     * @throws IllegalArgumentException
     *             if the address cannot be parsed
     */
    public static short[] parseV4 ( String address ) {
        if ( isRegularV4Address(address) ) {
            return parseRegularV4(address);
        }
        else if ( IpUtil.isV6MappedV4Address(address) ) {
            return parseV6MappedV4Address(address);
        }
        throw new IllegalArgumentException("Cannot parse address as IPv4 address " + address); //$NON-NLS-1$
    }


    /**
     * 
     * @param address
     * @return the parsed v6 mapped v4 address
     */
    public static short[] parseV6MappedV4Address ( String address ) {
        Matcher v4MappedMatcher = V6_MAPPED_V4_PATTERN.matcher(address);
        if ( !v4MappedMatcher.matches() ) {
            throw new IllegalArgumentException("Parsed address does not seem to be a v6 mapped v4 address"); //$NON-NLS-1$
        }

        return parseRegularV4(v4MappedMatcher.group(6));
    }


    /**
     * @param addr
     * @return parsed ip value
     */
    public static short[] parseRegularV4 ( String addr ) {
        List<Integer> comp = parseComponents(addr);
        switch ( comp.size() ) {
        case 4:
            return make4ComponentAddr(comp);
        case 3:
            return make3ComponentAddr(comp);
        case 2:
            return make2ComponentAddr(comp);
        default:
            throw new IllegalArgumentException("Illegal address specification"); //$NON-NLS-1$
        }

    }


    /**
     * @return
     */
    private static List<Integer> parseComponents ( String address ) {
        List<Integer> comp = new ArrayList<>();
        try ( Scanner s = new Scanner(address) ) {
            s.useDelimiter(Pattern.quote(ADDR_SEP_V4));

            for ( int i = 0; i < 4; i++ ) {
                if ( !s.hasNext() ) {
                    break;
                }
                comp.add(s.nextInt());
            }

            if ( s.hasNext() ) {
                throw new IllegalArgumentException("Illegal address, trailing garbage"); //$NON-NLS-1$
            }
        }
        return comp;
    }


    /**
     * @param comp
     * @return
     */
    private static short[] make2ComponentAddr ( List<Integer> comp ) {
        if ( comp.get(0) < 0 || comp.get(0) > 0xFF ) {
            throw new IllegalArgumentException(BYTE_COMPONENTS_ERR);
        }

        if ( comp.get(1) < 0 || comp.get(1) > 0xFFFFFF ) {
            throw new IllegalArgumentException("Illegal address, component must be < 0xFFFFFF"); //$NON-NLS-1$
        }

        short high = (short) ( ( comp.get(1) >> 2 ) & 0xFF );
        short mid = (short) ( ( comp.get(1) >> 2 ) & 0xFF );
        short low = (short) ( comp.get(1) & 0xFF );

        return new short[] {
            comp.get(0).shortValue(), high, mid, low
        };
    }


    /**
     * @param comp
     * @return
     */
    private static short[] make3ComponentAddr ( List<Integer> comp ) {
        for ( int i = 0; i < 2; i++ ) {
            if ( comp.get(i) < 0 || comp.get(i) > 0xFF ) {
                throw new IllegalArgumentException(BYTE_COMPONENTS_ERR);
            }
        }

        if ( comp.get(2) < 0 || comp.get(2) > 0xFFFF ) {
            throw new IllegalArgumentException("Illegal address, component must be < 0xFFFF"); //$NON-NLS-1$
        }

        short high = (short) ( ( comp.get(2) >> 1 ) & 0xFF );
        short low = (short) ( comp.get(2) & 0xFF );

        return new short[] {
            comp.get(0).shortValue(), comp.get(1).shortValue(), high, low
        };
    }


    /**
     * @param comp
     * @return
     */
    private static short[] make4ComponentAddr ( List<Integer> comp ) {

        for ( int i = 0; i < 4; i++ ) {
            if ( comp.get(i) < 0 || comp.get(i) > 0xFF ) {
                throw new IllegalArgumentException(BYTE_COMPONENTS_ERR);
            }
        }

        return new short[] {
            comp.get(0).shortValue(), comp.get(1).shortValue(), comp.get(2).shortValue(), comp.get(3).shortValue()
        };
    }


    /**
     * 
     * @param addr
     * @return the parsed address
     */
    public static short[] parseV6 ( String addr ) {
        String realAddress = addr;
        if ( isBracketed(realAddress) ) {
            realAddress = stripBrackets(realAddress);
        }
        int scopeSepPos = realAddress.indexOf(SPEC_SEPARATOR_V6);
        if ( scopeSepPos != -1 ) {
            realAddress = realAddress.substring(0, scopeSepPos);
        }

        return parseAddressV6(realAddress, getDoubleSeparatorExpansion(realAddress));
    }


    /**
     * 
     * @param addr
     * @return the address scope
     */
    public static String parseV6Scope ( String addr ) {

        String realAddress = addr;
        if ( isBracketed(realAddress) ) {
            realAddress = stripBrackets(realAddress);
        }

        String scope = null;
        int scopeSepPos = realAddress.indexOf(SPEC_SEPARATOR_V6);
        if ( scopeSepPos != -1 ) {
            scope = realAddress.substring(scopeSepPos + 1);
        }

        return scope;
    }


    /**
     * @param address
     * @param parsed
     * @param scopeSpec
     * @param doubleSepSkip
     * @return
     */
    private static short[] parseAddressV6 ( String address, int doubleSepSkip ) {
        short[] parsed = new short[16];
        try ( Scanner scanner = new Scanner(address) ) {
            scanner.useDelimiter(ADDR_SEP_V6);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Parse address %s", address)); //$NON-NLS-1$
            }

            if ( doubleSepSkip > 0 && log.isTraceEnabled() ) {
                log.trace(String.format("Double colon replaces %d components", doubleSepSkip)); //$NON-NLS-1$
            }

            int i = 0;
            while ( i < 16 ) {
                i = parseComponent(doubleSepSkip, parsed, scanner, i);
            }

            if ( scanner.hasNext() ) {
                throw new IllegalArgumentException("Illegal address, trailing garbage"); //$NON-NLS-1$
            }
        }

        return parsed;
    }


    /**
     * @param doubleSepSkip
     * @param parsed
     * @param scanner
     * @param i
     * @return
     */
    private static int parseComponent ( int doubleSepSkip, short[] parsed, Scanner scanner, int i ) {
        int pos = i;
        if ( doubleSepSkip > 0 && scanner.hasNext(StringUtils.EMPTY) ) {
            log.trace("Found double colon"); //$NON-NLS-1$
            scanner.next(StringUtils.EMPTY);
            pos += doubleSepSkip * 2;
            if ( pos == 16 ) {
                return pos;
            }
        }

        int block = scanner.nextInt(16);
        if ( block < 0 || block > 0xFFFF ) {
            throw new IllegalArgumentException("Illegal address, component must be short valued"); //$NON-NLS-1$
        }

        short high = (short) ( block >> 8 );
        short low = (short) ( block & 0xFF );

        parsed[ pos ] = high;
        parsed[ pos + 1 ] = low;
        return pos + 2;
    }


    /**
     * @param address
     * @param realAddress
     * @return
     */
    private static int getDoubleSeparatorExpansion ( String realAddress ) {
        int numSeps = StringUtils.countMatches(realAddress, ADDR_SEP_V6);
        int doubleSepSkip = 0;
        int pos = realAddress.indexOf(DOUBLE_COLON);
        if ( pos != -1 ) {
            doubleSepSkip = 8 - numSeps;
        }

        if ( pos == 0 || pos == realAddress.length() - 2 ) {
            doubleSepSkip++;
        }

        if ( realAddress.indexOf(DOUBLE_COLON, pos + 2) != -1 ) {
            throw new IllegalArgumentException("Multiple double colons present in address."); //$NON-NLS-1$
        }

        return doubleSepSkip;
    }

}
