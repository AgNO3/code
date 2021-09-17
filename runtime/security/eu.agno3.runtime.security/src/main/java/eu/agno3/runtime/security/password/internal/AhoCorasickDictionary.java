/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2016 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayIntTrie;
import eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayTrie;


/**
 * @author mbechler
 *
 */
public class AhoCorasickDictionary {

    private static final Logger log = Logger.getLogger(AhoCorasickDictionary.class);

    private AhoCorasickDoubleArrayTrie<Integer> trie;


    /**
     * Loads a prebuilt dictionary
     * 
     * @param file
     * @throws IOException
     */
    public AhoCorasickDictionary ( Path file ) throws IOException {
        this.trie = loadMapped(file);
    }


    /**
     * @param dictionaries
     * @throws IOException
     */
    public AhoCorasickDictionary ( Collection<URL> dictionaries ) throws IOException {
        this.trie = loadDictionaries(dictionaries);
    }


    /**
     * @param vals
     * @throws IOException
     */
    public AhoCorasickDictionary ( Map<String, Integer> vals ) throws IOException {
        this.trie = new AhoCorasickDoubleArrayIntTrie();
        this.trie.build(vals);
    }


    /**
     * @return the trie
     */
    public AhoCorasickDoubleArrayTrie<Integer> getTrie () {
        return this.trie;
    }


    /**
     * @param dictionaries
     * @return
     * @throws IOException
     */
    private static AhoCorasickDoubleArrayTrie<Integer> loadDictionaries ( Collection<URL> dictionaries ) throws IOException {
        Map<String, Integer> vals = new HashMap<>();
        for ( URL dict : dictionaries ) {
            try ( InputStream stream = dict.openStream() ) {
                parseDict(vals, stream);
            }
        }
        AhoCorasickDoubleArrayTrie<Integer> trie = new AhoCorasickDoubleArrayIntTrie();
        trie.build(vals);
        return trie;

    }


    /**
     * @param vals
     * @param stream
     * @throws IOException
     */
    static void parseDict ( Map<String, Integer> vals, InputStream stream ) throws IOException {
        try ( InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
              // $NON-NLS-1$
              BufferedReader r = new BufferedReader(isr) ) {
            String line;
            int rank = 1;
            while ( ( line = r.readLine() ) != null ) {
                line = line.trim();
                vals.put(line, rank);
                rank++;
            }
        }
    }


    /**
     * @param file
     * @return
     * @throws IOException
     */
    private static AhoCorasickDoubleArrayTrie<Integer> loadMapped ( Path file ) throws IOException {
        AhoCorasickDoubleArrayTrie<Integer> trie = new AhoCorasickDoubleArrayIntTrie();
        trie.load(file);
        return trie;
    }

    /**
     * 
     * @author mbechler
     *
     */
    public static class DictionaryValue {

        private int rank;


        /**
         * @param rank
         * 
         */
        public DictionaryValue ( int rank ) {
            this.rank = rank;
        }


        /**
         * @return dictionary rank
         */
        public int rank () {
            return this.rank;
        }

    }


    /**
     * @param password
     * @param origPassword
     * @param matches
     * @param l33t
     * @param substitution
     * @return whether a match was found
     */
    public boolean doMatch ( String password, String origPassword, List<MatchEntry> matches, boolean l33t, Map<Character, Character> substitution ) {
        long startTs = System.currentTimeMillis();
        int matchSize = matches.size();
        this.trie.parseText(password, ( start, end, rank, idx ) -> {
            String substring = origPassword.substring(start, end);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Matched %s [%d,%d] rank %d", substring, start, end, rank)); //$NON-NLS-1$
            }
            matches.add(new DictionaryMatchEntry(substring, start, rank, StringUtils.EMPTY, l33t, substitution));
        });

        if ( log.isDebugEnabled() ) {
            log.debug("Took " + ( System.currentTimeMillis() - startTs )); //$NON-NLS-1$
        }
        return matches.size() > matchSize;
    }


    /**
     * @param args
     */
    public static void main ( String[] args ) {
        if ( args.length < 2 ) {
            System.err.println("Usage: <output> <input...>"); //$NON-NLS-1$
            System.exit(-1);
            return;
        }

        String out = args[ 0 ];
        Map<String, Integer> vals = new TreeMap<>();

        try {
            for ( int i = 1; i < args.length; i++ ) {
                String dict = args[ i ];
                try ( FileInputStream fis = new FileInputStream(dict) ) {
                    parseDict(vals, fis);
                }
            }

            AhoCorasickDoubleArrayTrie<Integer> trie = new AhoCorasickDoubleArrayIntTrie();
            trie.build(vals);
            trie.save(Paths.get(out));
        }
        catch ( IOException e ) {
            e.printStackTrace(System.err);
        }

    }
}
