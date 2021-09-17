/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.EnumSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public final class PasswordUtil {

    private static final double LOG2 = Math.log(2);


    /**
     * 
     */
    private PasswordUtil () {}


    /**
     * 
     * @param val
     * @return the cardinatility of the contained character classes
     */
    public static int getBruteforceCardinality ( String val ) {
        int cardinality = 0;
        for ( CharacterClass cc : getCharacterClasses(val) ) {
            cardinality += cc.getBruteforceCardinality();
        }
        return cardinality;
    }


    /**
     * 
     * @param val
     * @return the contained character classes
     */
    public static Set<CharacterClass> getCharacterClasses ( String val ) {
        Set<CharacterClass> res = EnumSet.noneOf(CharacterClass.class);
        for ( char c : val.toCharArray() ) {
            res.add(getCharacterClass(c));
        }
        return res;
    }


    /**
     * @param c
     * @return the character class
     */
    public static CharacterClass getCharacterClass ( char c ) {
        if ( c >= 0x30 && c <= 0x39 ) {
            return CharacterClass.DIGIT;
        }
        else if ( c >= 0x41 && c <= 0x54 ) {
            return CharacterClass.UPPERCASE;
        }
        else if ( c >= 0x61 && c <= 0x7a ) {
            return CharacterClass.LOWERCASE;
        }
        else if ( c <= 0x7f ) {
            return CharacterClass.SYMBOL;
        }
        else {
            return CharacterClass.EXTENDED;
        }
    }


    /**
     * @param i
     * @return the logarithm to the base 2
     */
    public static float log2 ( int i ) {
        return (float) ( Math.log(i) / LOG2 );
    }


    /**
     * @param token
     * @param clz
     * @return the number of characters in the class
     */
    public static int countClass ( String token, CharacterClass clz ) {
        int count = 0;
        for ( char c : token.toCharArray() ) {
            if ( getCharacterClass(c) == clz ) {
                count++;
            }
        }

        return count;
    }


    /**
     * @param a
     * @param b
     * @return maximum number of combinations of the classes with the sizes a and b
     */
    public static int getPossibilities ( int a, int b ) {
        int possibilities = 0;
        int min = Math.min(a, b);
        int sum = a + b;

        for ( int i = 0; i <= min; i++ ) {
            possibilities += nChooseK(sum, i);
        }
        return possibilities;
    }


    /**
     * @param n
     * @param k
     * @return n choose k
     */
    public static int nChooseK ( int n, int k ) {
        if ( k > n ) {
            return 0;
        }
        if ( k == 0 ) {
            return 1;
        }
        int n1 = n;
        int res = 1;
        for ( int d = 1; d <= k; d++ ) {
            res = res * n1--;
            res = res / d;
        }
        return res;
    }


    /**
     * @param token
     * @param c
     * @return the number of occurances of the character
     */
    public static int countChars ( String token, char c ) {
        int num = 0;
        for ( char ch : token.toCharArray() ) {
            if ( ch == c ) {
                num++;
            }
        }
        return num;
    }
}
