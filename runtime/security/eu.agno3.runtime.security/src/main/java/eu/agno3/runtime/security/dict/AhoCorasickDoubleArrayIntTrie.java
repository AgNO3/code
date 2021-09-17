/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2016 by mbechler
 */
package eu.agno3.runtime.security.dict;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class AhoCorasickDoubleArrayIntTrie extends AhoCorasickDoubleArrayTrie<Integer> {

    private IntBufferHolder v;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayTrie#get(int)
     */
    @Override
    public Integer get ( int index ) {
        return this.v.get(index);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayTrie#importValues(java.lang.Object[])
     */
    @Override
    protected void importValues ( List<String> keyOrder, Map<String, Integer> vals ) throws IOException {
        this.v = allocateInt(keyOrder.size());
        int idx = 0;
        for ( String key : keyOrder ) {
            this.v.put(idx, vals.get(key));
            idx++;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayTrie#writeExtra(java.nio.file.Path,
     *      java.nio.channels.FileChannel, long)
     */
    @Override
    protected long writeExtra ( Path out, FileChannel fc, long extraOff ) throws IOException {
        return this.v.writeTo(fc, extraOff);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.dict.AhoCorasickDoubleArrayTrie#loadExtra(java.nio.file.Path,
     *      java.nio.channels.FileChannel, long, long)
     */
    @Override
    protected void loadExtra ( Path ch, FileChannel fc, long start, long len ) throws IOException {
        this.v = new IntBufferHolder(ch, start, len);
    }
}
