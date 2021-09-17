/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class ListDictionary implements Dictionary {

    private String name;
    private Map<String, Integer> words = new HashMap<>();


    /**
     * @param name
     * @param words
     * 
     */
    public ListDictionary ( String name, List<String> words ) {
        this.name = name;

        int idx = 1;
        for ( String word : words ) {
            this.words.put(word, idx);
            idx++;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.Dictionary#getName()
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.Dictionary#getMatchRank(java.lang.String)
     */
    @Override
    public int getMatchRank ( String word ) {
        Integer found = this.words.get(word);

        if ( found != null ) {
            return found;
        }

        return 0;
    }


    /**
     * @param name
     * @param u
     * @return a dictionary read from a file (ranked by position)
     * @throws IOException
     */
    public static ListDictionary fromStream ( String name, InputStream u ) throws IOException {
        List<String> words = new LinkedList<>();

        try ( InputStreamReader isr = new InputStreamReader(u, Charset.forName("UTF-8"));//$NON-NLS-1$
              BufferedReader r = new BufferedReader(isr) ) {
            String line;
            while ( ( line = r.readLine() ) != null ) {
                words.add(line.trim());
            }
        }
        return new ListDictionary(name, words);
    }
}
