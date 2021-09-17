/*
 * AhoCorasickDoubleArrayTrie Project
 *      https://github.com/hankcs/AhoCorasickDoubleArrayTrie
 *
 * Copyright 2008-2016 hankcs <me@hankcs.com>
 * You may modify and redistribute as long as this attribution remains.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.agno3.runtime.security.dict;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * An implementation of Aho Corasick algorithm based on Double Array Trie
 *
 * @author hankcs
 * @param <V>
 */
public abstract class AhoCorasickDoubleArrayTrie <V> {

    protected IntBufferHolder check;
    protected IntBufferHolder base;
    protected IntBufferHolder fail;

    protected ShortBufferHolder keyLenghts;
    protected int numKeys;

    /**
     * Output buffer
     * 
     * maxOutputs*i = len
     * maxOutputs*i+j = output
     */
    protected IntBufferHolder output;
    protected int maxOutputs;

    /**
     * the size of base and check array
     */
    protected int size;


    /**
     * @param out
     * @throws IOException
     */
    public void save ( Path out ) throws IOException {
        try ( FileChannel fc = FileChannel.open(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE) ) {
            ByteBuffer header = ByteBuffer.allocate(1024);
            header.putInt(1);
            header.putInt(this.size);
            header.putInt(this.maxOutputs);
            header.putInt(this.numKeys);

            long lenOff = header.capacity();
            long checkOff = lenOff + this.keyLenghts.bytes();
            long baseOff = checkOff + this.check.bytes();
            long failOff = baseOff + this.base.bytes();
            long outOff = failOff + this.fail.bytes();
            long extraOff = outOff + this.output.bytes();

            header.putLong(lenOff);
            header.putLong(checkOff);
            header.putLong(baseOff);
            header.putLong(failOff);
            header.putLong(outOff);
            header.putLong(extraOff);

            this.keyLenghts.writeTo(fc, lenOff);
            this.check.writeTo(fc, checkOff);
            this.base.writeTo(fc, baseOff);
            this.fail.writeTo(fc, failOff);
            this.output.writeTo(fc, outOff);

            long end = extraOff + writeExtra(out, fc, extraOff);
            header.putLong(end);

            header.flip();
            int written = 0;
            int pos = 0;
            while ( ( written = fc.write(header, pos) ) > 0 ) {
                pos += written;
            }
        }
    }


    /**
     * @param out
     * @param fc
     * @param extraOff
     * @return
     * @throws IOException
     */
    protected long writeExtra ( Path out, FileChannel fc, long extraOff ) throws IOException {
        return 0;
    }


    /**
     * @param in
     * @throws IOException
     */
    public void load ( Path in ) throws IOException {
        try ( FileChannel fc = FileChannel.open(in, StandardOpenOption.READ) ) {
            ByteBuffer header = ByteBuffer.allocate(1024);
            while ( fc.read(header) > 0 );
            header.rewind();
            int v = header.getInt();
            if ( v != 1 ) {
                throw new IllegalArgumentException("Invalid file version " + v); //$NON-NLS-1$
            }
            this.size = header.getInt();
            this.maxOutputs = header.getInt();
            this.numKeys = header.getInt();

            long lenOff = header.getLong();
            long checkOff = header.getLong();
            long baseOff = header.getLong();
            long failOff = header.getLong();
            long outOff = header.getLong();
            long extraOff = header.getLong();
            long end = header.getLong();

            this.keyLenghts = new ShortBufferHolder(in, lenOff, checkOff - lenOff);
            this.check = new IntBufferHolder(in, checkOff, baseOff - checkOff);
            this.base = new IntBufferHolder(in, baseOff, failOff - baseOff);
            this.fail = new IntBufferHolder(in, failOff, outOff - failOff);
            this.output = new IntBufferHolder(in, outOff, extraOff - outOff);
            loadExtra(in, fc, extraOff, end - extraOff);
        }
    }


    /**
     * @param fc
     * @throws IOException
     * 
     */
    protected void loadExtra ( Path ch, FileChannel fc, long start, long len ) throws IOException {}


    /**
     * Parse text
     * 
     * @param text
     *            The text
     * @param processor
     *            A processor which handles the output
     */
    public void parseText ( String text, MatchHandler<V> processor ) {
        int s = 0;
        for ( int i = 0; i < text.length(); ++i ) {
            s = getNextState(s, text.charAt(i));
            int[] hitArray = lookupOutput(s);
            if ( hitArray != null ) {
                for ( int hit : hitArray ) {
                    processor.hit(i + 1 - length(hit), i + 1, get(hit), hit);
                }
            }
        }
    }


    /**
     * @param hit
     */
    private int length ( int hit ) {
        return this.keyLenghts.get(hit);
    }


    /**
     * Get value by a String key, just like a map.get() method
     * 
     * @param key
     *            The key
     * @return the associated value
     */
    public V get ( String key ) {
        int index = exactMatchSearch(key);
        if ( index >= 0 ) {
            return get(index);
        }

        return null;
    }


    /**
     * Pick the value by index in value array <br>
     * Notice that to be more efficiently, this method DONOT check the parameter
     * 
     * @param index
     *            The index
     * @return The value
     */
    public abstract V get ( int index );


    /**
     * @param keyOrder
     * @param array
     * @throws IOException
     */
    protected abstract void importValues ( List<String> keyOrder, Map<String, V> array ) throws IOException;


    /**
     * Get the size of the keywords
     * 
     * @return the number of keywords
     */
    public int size () {
        return this.numKeys;
    };

    /**
     * Processor handles the output when hit a keyword, with more detail
     * 
     * @param <Val>
     */
    public interface MatchHandler <Val> {

        /**
         * Hit a keyword, you can use some code like text.substring(begin, end) to get the keyword
         * 
         * @param begin
         *            the beginning index, inclusive.
         * @param end
         *            the ending index, exclusive.
         * @param value
         *            the value assigned to the keyword
         * @param index
         *            the index of the value assigned to the keyword, you can use the integer as a perfect hash value
         */
        void hit ( int begin, int end, Val value, int index );
    }


    /**
     * @param currentState
     * @return outputs for that state
     */
    private int[] lookupOutput ( int currentState ) {
        int off = ( this.maxOutputs + 1 ) * currentState;
        int num = this.output.get(off);
        if ( num == 0 ) {
            return null;
        }
        off++;
        int[] outs = new int[num];
        for ( int i = 0; i < num; i++ ) {
            outs[ i ] = this.output.get(off + i);
        }
        return outs;
    }


    /**
     * @param currentState
     * @return failure state
     */
    private int fail ( int currentState ) {
        return this.fail.get(currentState);
    }


    /**
     * @param p
     * @return
     */
    private int check ( int p ) {
        return this.check.get(p);
    }


    /**
     * @param nodePos
     * @return
     */
    private int base ( int nodePos ) {
        return this.base.get(nodePos);
    }


    /**
     * transmit state, supports failure function
     *
     * @param currentState
     * @param character
     * @return
     */
    private int getNextState ( int currentState, char character ) {
        int state = currentState;
        int nextState = transitionWithRoot(currentState, character);
        while ( nextState == -1 ) {
            state = fail(state);
            nextState = transitionWithRoot(state, character);
        }
        return nextState;
    }


    /**
     * transition of a state, if the state is root and it failed, then returns the root
     *
     * @param nodePos
     * @param c
     * @return
     */
    private int transitionWithRoot ( int nodePos, char c ) {
        int b = base(nodePos);
        int p = b + c + 1;
        if ( b != check(p) ) {
            if ( nodePos == 0 )
                return 0;
            return -1;
        }
        return p;
    }


    /**
     * Build a AhoCorasickDoubleArrayTrie from a map
     * 
     * @param map
     *            a map containing key-value pairs
     * @throws IOException
     */
    public void build ( Map<String, V> map ) throws IOException {
        new Builder().build(map);
    }


    /**
     * match exactly by a key
     *
     * @param key
     *            the key
     * @return the index of the key, you can use it as a perfect hash function
     */
    public int exactMatchSearch ( String key ) {
        return exactMatchSearch(key, 0, 0, 0);
    }


    /**
     * match exactly by a key
     *
     * @param key
     * @param pos
     * @param len
     * @param nodePos
     * @return
     */
    private int exactMatchSearch ( String key, int pos, int len, int nodePos ) {
        int kl = len;
        int np = nodePos;
        if ( kl <= 0 )
            kl = key.length();
        if ( np <= 0 )
            np = 0;

        int r = -1;
        int b = base(np);
        int p;

        char[] keyChars = key.toCharArray();
        for ( int i = pos; i < kl; i++ ) {
            p = b + keyChars[ i ] + 1;
            if ( b == check(p) )
                b = base(p);
            else
                return r;
        }

        p = b;
        int n = base(p);
        if ( b == check(p) && n < 0 ) {
            r = -n - 1;
        }
        return r;
    }


    protected IntBufferHolder allocateInt ( int s ) throws IOException {
        return new IntBufferHolder(s);
    }


    protected ShortBufferHolder allocateShort ( int s ) throws IOException {
        return new ShortBufferHolder(s);
    }


    protected IntBufferHolder resizeInt ( IntBufferHolder old, int s ) throws IOException {
        if ( old == null ) {
            return allocateInt(s);
        }
        old.resize(s);
        return old;
    }

    /**
     * A builder to build the AhoCorasickDoubleArrayTrie
     */
    private class Builder {

        /**
         * the root state of trie
         */
        private State rootState = new State();
        /**
         * whether the position has been used
         */
        private boolean used[];
        /**
         * the allocSize of the dynamic array
         */
        private int allocSize;
        /**
         * a parameter controls the memory growth speed of the dynamic array
         */
        private int progress;
        /**
         * the next position to check unused memory
         */
        private int nextCheckPos;

        /**
         * maximum number of outputs for a state
         */
        protected int maxout;

        /**
         * output table of the Aho Corasick automata
         */
        protected int[][] out;


        /**
         * 
         */
        public Builder () {}


        /**
         * Build from a map
         * 
         * @param map
         *            a map containing key-value pairs
         * @throws IOException
         */
        public void build ( Map<String, V> map ) throws IOException {
            AhoCorasickDoubleArrayTrie.this.numKeys = map.size();
            List<String> keySet = new ArrayList<>(map.keySet());
            AhoCorasickDoubleArrayTrie.this.importValues(keySet, map);
            AhoCorasickDoubleArrayTrie.this.keyLenghts = allocateShort(AhoCorasickDoubleArrayTrie.this.numKeys);

            addAllKeyword(keySet);

            buildDoubleArrayTrie();
            this.used = null;

            constructFailureStates();
            this.rootState = null;

            AhoCorasickDoubleArrayTrie.this.maxOutputs = this.maxout;
            IntBufferHolder o = allocateInt(this.out.length * ( this.maxout + 1 ));
            for ( int i = 0; i < this.out.length; i++ ) {
                int[] outs = this.out[ i ];
                int off = i * ( this.maxout + 1 );
                if ( outs != null ) {
                    o.put(off, outs.length);
                    for ( int j = 0; j < outs.length; j++ ) {
                        o.put(off + j + 1, outs[ j ]);
                    }
                }
                else {
                    o.put(off, 0);
                }

            }
            AhoCorasickDoubleArrayTrie.this.output = o;
        }


        /**
         * fetch siblings of a parent node
         *
         * @param parent
         *            parent node
         * @param siblings
         *            parent node's child nodes, i . e . the siblings
         * @return the amount of the siblings
         */
        private int fetch ( State parent, List<Map.Entry<Integer, State>> siblings ) {
            if ( parent.isAcceptable() ) {
                State fakeNode = new State(- ( parent.getDepth() + 1 )); // 此节点是parent的子节点，同时具备parent的输出
                fakeNode.addEmit(parent.getLargestValueId());
                siblings.add(new AbstractMap.SimpleEntry<>(0, fakeNode));
            }
            for ( Map.Entry<Character, State> entry : parent.getSuccess().entrySet() ) {
                siblings.add(new AbstractMap.SimpleEntry<>(entry.getKey() + 1, entry.getValue()));
            }
            return siblings.size();
        }


        /**
         * add a keyword
         *
         * @param keyword
         *            a keyword
         * @param index
         *            the index of the keyword
         */
        private void addKeyword ( String keyword, int index ) {
            State currentState = this.rootState;
            for ( Character character : keyword.toCharArray() ) {
                currentState = currentState.addState(character);
            }
            currentState.addEmit(index);

            if ( keyword.length() > Short.MAX_VALUE ) {
                throw new IllegalArgumentException();
            }

            AhoCorasickDoubleArrayTrie.this.keyLenghts.put(index, (short) keyword.length());
        }


        /**
         * add a collection of keywords
         *
         * @param keywordSet
         *            the collection holding keywords
         */
        private void addAllKeyword ( Collection<String> keywordSet ) {
            int i = 0;
            for ( String keyword : keywordSet ) {
                addKeyword(keyword, i++);
            }
        }


        /**
         * construct failure table
         * 
         * @throws IOException
         */
        private void constructFailureStates () throws IOException {
            AhoCorasickDoubleArrayTrie.this.fail = allocateInt(AhoCorasickDoubleArrayTrie.this.size + 1);
            AhoCorasickDoubleArrayTrie.this.fail.put(1, AhoCorasickDoubleArrayTrie.this.base.get(0));
            this.out = new int[AhoCorasickDoubleArrayTrie.this.size + 1][];

            Queue<State> queue = new LinkedBlockingDeque<>();

            for ( State depthOneState : this.rootState.getStates() ) {
                depthOneState.setFailure(this.rootState, AhoCorasickDoubleArrayTrie.this.fail);
                queue.add(depthOneState);
                constructOutput(depthOneState);
            }

            while ( !queue.isEmpty() ) {
                State currentState = queue.remove();

                for ( Character transition : currentState.getTransitions() ) {
                    State targetState = currentState.nextState(transition);
                    queue.add(targetState);

                    State traceFailureState = currentState.failure();
                    while ( traceFailureState.nextState(transition) == null ) {
                        traceFailureState = traceFailureState.failure();
                    }
                    State newFailureState = traceFailureState.nextState(transition);
                    targetState.setFailure(newFailureState, AhoCorasickDoubleArrayTrie.this.fail);
                    targetState.addEmit(newFailureState.emit());
                    constructOutput(targetState);
                }
            }
        }


        /**
         * construct output table
         */
        private void constructOutput ( State targetState ) {
            Collection<Integer> emit = targetState.emit();
            if ( emit == null || emit.size() == 0 )
                return;
            int o[] = new int[emit.size()];
            Iterator<Integer> it = emit.iterator();
            for ( int i = 0; i < o.length; ++i ) {
                o[ i ] = it.next();
            }

            this.maxout = Math.max(this.maxout, o.length);
            this.out[ targetState.getIndex() ] = o;
        }


        private void buildDoubleArrayTrie () throws IOException {
            this.progress = 0;
            resize(AhoCorasickDoubleArrayTrie.this.numKeys);

            AhoCorasickDoubleArrayTrie.this.base.put(0, 1);
            this.nextCheckPos = 0;

            State r = this.rootState;

            List<Map.Entry<Integer, State>> siblings = new ArrayList<>(r.getSuccess().entrySet().size());
            fetch(r, siblings);
            insert(siblings);
        }


        /**
         * allocate the memory of the dynamic array
         *
         * @param newSize
         * @return
         * @throws IOException
         */
        private int resize ( int newSize ) throws IOException {
            AhoCorasickDoubleArrayTrie.this.base = resizeInt(AhoCorasickDoubleArrayTrie.this.base, newSize);
            AhoCorasickDoubleArrayTrie.this.check = resizeInt(AhoCorasickDoubleArrayTrie.this.check, newSize);
            boolean u[] = new boolean[newSize];
            if ( this.allocSize > 0 ) {
                System.arraycopy(this.used, 0, u, 0, this.allocSize);
            }
            this.used = u;
            return this.allocSize = newSize;
        }


        /**
         * insert the siblings to double array trie
         *
         * @param siblings
         *            the siblings being inserted
         * @return the position to insert them
         * @throws IOException
         */
        private int insert ( List<Map.Entry<Integer, State>> siblings ) throws IOException {
            int begin = 0;
            int pos = Math.max(siblings.get(0).getKey() + 1, this.nextCheckPos) - 1;
            int nonzero_num = 0;
            int first = 0;

            if ( this.allocSize <= pos )
                resize(pos + 1);

            outer:
            while ( true ) {
                pos++;

                if ( this.allocSize <= pos )
                    resize(pos + 1);

                if ( AhoCorasickDoubleArrayTrie.this.check.get(pos) != 0 ) {
                    nonzero_num++;
                    continue;
                }
                else if ( first == 0 ) {
                    this.nextCheckPos = pos;
                    first = 1;
                }

                begin = pos - siblings.get(0).getKey(); // 当前位置离第一个兄弟节点的距离
                if ( this.allocSize <= ( begin + siblings.get(siblings.size() - 1).getKey() ) ) {
                    // progress can be zero // 防止progress产生除零错误
                    double f = ( 1.05 > 1.0 * AhoCorasickDoubleArrayTrie.this.numKeys / ( this.progress + 1 ) ) ? 1.05
                            : 1.0 * AhoCorasickDoubleArrayTrie.this.numKeys / ( this.progress + 1 );
                    resize((int) ( this.allocSize * f ));
                }

                if ( this.used[ begin ] )
                    continue;

                for ( int i = 1; i < siblings.size(); i++ )
                    if ( AhoCorasickDoubleArrayTrie.this.check.get(begin + siblings.get(i).getKey()) != 0 )
                        continue outer;

                break;
            }

            // -- Simple heuristics --
            // if the percentage of non-empty contents in check between the
            // index
            // 'next_check_pos' and 'check' is greater than some constant value
            // (e.g. 0.9),
            // new 'next_check_pos' index is written by 'check'.
            if ( 1.0 * nonzero_num / ( pos - this.nextCheckPos + 1 ) >= 0.95 )
                this.nextCheckPos = pos;
            this.used[ begin ] = true;

            AhoCorasickDoubleArrayTrie.this.size = ( AhoCorasickDoubleArrayTrie.this.size > begin + siblings.get(siblings.size() - 1).getKey() + 1 )
                    ? AhoCorasickDoubleArrayTrie.this.size : begin + siblings.get(siblings.size() - 1).getKey() + 1;

            for ( Map.Entry<Integer, State> sibling : siblings ) {
                AhoCorasickDoubleArrayTrie.this.check.put(begin + sibling.getKey(), begin);
            }

            for ( Map.Entry<Integer, State> sibling : siblings ) {
                List<Map.Entry<Integer, State>> new_siblings = new ArrayList<>(sibling.getValue().getSuccess().entrySet().size() + 1);

                if ( fetch(sibling.getValue(), new_siblings) == 0 ) {
                    AhoCorasickDoubleArrayTrie.this.base.put(begin + sibling.getKey(), -sibling.getValue().getLargestValueId() - 1);
                    this.progress++;
                }
                else {
                    int h = insert(new_siblings); // dfs
                    AhoCorasickDoubleArrayTrie.this.base.put(begin + sibling.getKey(), h);
                }
                sibling.getValue().setIndex(begin + sibling.getKey());
            }
            return begin;
        }
    }

}
