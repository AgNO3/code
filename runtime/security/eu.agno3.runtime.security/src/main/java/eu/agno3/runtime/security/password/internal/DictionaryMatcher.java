/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author mbechler
 *
 */
public class DictionaryMatcher implements PasswordMatcher {

    private List<Dictionary> dictionaries;

    private final AhoCorasickDictionary dictionary;

    static final Map<Character, char[]> L33T_MAP = new HashMap<>();


    static {
        L33T_MAP.put('a', new char[] {
            '4', '@'
        });

        L33T_MAP.put('b', new char[] {
            '8'
        });
        L33T_MAP.put('c', new char[] {
            '(', '{', '[', '<'
        });
        L33T_MAP.put('e', new char[] {
            '3'
        });
        L33T_MAP.put('g', new char[] {
            '6', '9'
        });
        L33T_MAP.put('i', new char[] {
            '1', '!', '|'
        });
        L33T_MAP.put('l', new char[] {
            '1', '|', '7'
        });
        L33T_MAP.put('o', new char[] {
            '0'
        });
        L33T_MAP.put('s', new char[] {
            '$', '5'
        });
        L33T_MAP.put('t', new char[] {
            '+', '7'
        });
        L33T_MAP.put('x', new char[] {
            '%'
        });
        L33T_MAP.put('z', new char[] {
            '2'
        });

    }


    /**
     * @param dictionaries
     * 
     */
    public DictionaryMatcher ( List<Dictionary> dictionaries ) {
        this.dictionary = null;
        this.dictionaries = dictionaries;
    }


    /**
     * @param dictionaries
     * @throws IOException
     * 
     */
    public DictionaryMatcher ( Collection<URL> dictionaries ) throws IOException {
        this.dictionary = new AhoCorasickDictionary(dictionaries);
    }


    /**
     * @param dict
     * @throws IOException
     * 
     */
    public DictionaryMatcher ( Path dict ) throws IOException {
        this.dictionary = new AhoCorasickDictionary(dict);
    }


    /**
     * @param dict
     * 
     */
    public DictionaryMatcher ( AhoCorasickDictionary dict ) {
        this.dictionary = dict;
    }


    /**
     * @return the dictionary
     */
    public AhoCorasickDictionary getDictionary () {
        return this.dictionary;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {

        List<MatchEntry> matches = new LinkedList<>();

        String lowerCasePw = password.toLowerCase(Locale.ROOT);
        doMatch(lowerCasePw, password, matches, false, null);

        Map<Character, List<Character>> applicableSubs = getApplicableSubstitutions(lowerCasePw);
        for ( Map<Character, Character> substitution : enumateSubstitutions(applicableSubs) ) {
            doMatch(unapplySubstitution(lowerCasePw, substitution), password, matches, true, substitution);
        }
        return matches;
    }


    /**
     * @param lowerCasePw
     * @param substitution
     * @return
     */
    static String unapplySubstitution ( String lowerCasePw, Map<Character, Character> substitution ) {
        StringBuilder substituted = new StringBuilder(lowerCasePw.length());
        for ( char c : lowerCasePw.toCharArray() ) {
            if ( substitution.containsKey(c) ) {
                substituted.append(substitution.get(c));
            }
            else {
                substituted.append(c);
            }
        }
        return substituted.toString();
    }


    /**
     * @param password
     * @return substitution entries
     */
    public static Map<Character, List<Character>> getApplicableSubstitutions ( String password ) {
        Map<Character, List<Character>> applicableSubs = new HashMap<>();
        for ( Entry<Character, char[]> e : L33T_MAP.entrySet() ) {
            for ( char subch : e.getValue() ) {
                if ( password.indexOf(subch) >= 0 ) {
                    if ( !applicableSubs.containsKey(subch) ) {
                        applicableSubs.put(subch, new LinkedList<>(Arrays.asList(e.getKey())));
                    }
                    else {
                        applicableSubs.get(subch).add(e.getKey());
                    }
                }
            }
        }
        return applicableSubs;
    }


    /**
     * @param password
     * @param applicableSubs
     * @return
     */
    private static Iterable<Map<Character, Character>> enumateSubstitutions ( Map<Character, List<Character>> applicableSubs ) {
        if ( applicableSubs.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }

        return new SubstitutionIterable(applicableSubs);
    }


    /**
     * @param password
     * @param origPassword
     * @param matches
     * @param lowerCasePw
     * @param l33t
     * @param subs
     */
    private boolean doMatch ( String password, String origPassword, List<MatchEntry> matches, boolean l33t, Map<Character, Character> substitution ) {
        if ( this.dictionary != null ) {
            return this.dictionary.doMatch(password, origPassword, matches, l33t, substitution);
        }
        return doMatchNaive(password, origPassword, matches, l33t, substitution);
    }


    /**
     * @param password
     * @param origPassword
     * @param matches
     * @param l33t
     * @param substitution
     * @return
     */
    boolean doMatchNaive ( String password, String origPassword, List<MatchEntry> matches, boolean l33t, Map<Character, Character> substitution ) {
        boolean haveMatch = false;
        for ( int i = 0; i < password.length(); i++ ) {
            for ( int j = password.length(); j >= i + 1; j-- ) {
                String word = password.substring(i, j);
                for ( Dictionary dict : this.dictionaries ) {
                    int rank = dict.getMatchRank(word);
                    if ( rank > 0 ) {
                        haveMatch = true;
                        matches.add(new DictionaryMatchEntry(origPassword.substring(i, j), i, rank, dict.getName(), l33t, substitution));
                    }
                }
            }
        }
        return haveMatch;
    }

}
