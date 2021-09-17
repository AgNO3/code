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


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 *
 */
class State {

    protected final int depth;
    private State failure = null;
    private Set<Integer> emits = null;
    private Map<Character, State> success = new TreeMap<>();

    private int index;


    State () {
        this(0);
    }


    State ( int depth ) {
        this.depth = depth;
    }


    int getDepth () {
        return this.depth;
    }


    void addEmit ( int keyword ) {
        if ( this.emits == null ) {
            this.emits = new TreeSet<>(Collections.reverseOrder());
        }
        this.emits.add(keyword);
    }


    Integer getLargestValueId () {
        if ( this.emits == null || this.emits.size() == 0 )
            return null;

        return this.emits.iterator().next();
    }


    void addEmit ( Collection<Integer> em ) {
        for ( int emit : em ) {
            addEmit(emit);
        }
    }


    Collection<Integer> emit () {
        return this.emits == null ? Collections.<Integer> emptyList() : this.emits;
    }


    boolean isAcceptable () {
        return this.depth > 0 && this.emits != null;
    }


    State failure () {
        return this.failure;
    }


    void setFailure ( State failState, IntBufferHolder fail ) {
        this.failure = failState;
        fail.put(this.index, failState.index);
    }


    private State nextState ( Character character, boolean ignoreRootState ) {
        State nextState = this.success.get(character);
        if ( !ignoreRootState && nextState == null && this.depth == 0 ) {
            nextState = this;
        }
        return nextState;
    }


    State nextState ( Character character ) {
        return nextState(character, false);
    }


    State nextStateIgnoreRootState ( Character character ) {
        return nextState(character, true);
    }


    State addState ( Character character ) {
        State nextState = nextStateIgnoreRootState(character);
        if ( nextState == null ) {
            nextState = new State(this.depth + 1);
            this.success.put(character, nextState);
        }
        return nextState;
    }


    Collection<State> getStates () {
        return this.success.values();
    }


    Collection<Character> getTransitions () {
        return this.success.keySet();
    }


    Map<Character, State> getSuccess () {
        return this.success;
    }


    int getIndex () {
        return this.index;
    }


    void setIndex ( int index ) {
        this.index = index;
    }
}
