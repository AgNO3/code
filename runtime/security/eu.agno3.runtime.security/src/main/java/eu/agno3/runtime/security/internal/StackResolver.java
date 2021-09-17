/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 16, 2016 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.shiro.authc.AuthenticationException;

import eu.agno3.runtime.security.login.LoginRealm;


/**
 * @author mbechler
 *
 */
public final class StackResolver {

    private static final Logger log = Logger.getLogger(StackResolver.class);


    /**
     * 
     */
    private StackResolver () {}

    private static final String WILDCARD = "*"; //$NON-NLS-1$


    /**
     * @param primary
     *            primary realm
     * @param realms
     *            available realms
     * @return a resolved login stack
     */
    public static List<LoginRealm> buildStack ( LoginRealm primary, Collection<LoginRealm> realms ) {
        Set<String> candidateIds = new HashSet<>();
        Map<String, LoginRealm> idx = new HashMap<>();
        List<LoginRealm> candidates = getCandidates(primary, realms, candidateIds, idx);
        filterUnresolvable(candidateIds, candidates);
        dumpCandidates("Filtered candidates:", candidates, Level.TRACE); //$NON-NLS-1$
        candidates = sortCandidates(candidateIds, candidates, idx);
        dumpCandidates("Sorted candidates:", candidates, Level.DEBUG); //$NON-NLS-1$
        if ( !candidates.contains(primary) ) {
            throw new AuthenticationException("Primary not contained in stack"); //$NON-NLS-1$
        }
        return candidates;
    }


    /**
     * 
     * 
     * 
     * /**
     * 
     * @param primary
     * @param candidateIds
     * @return
     */
    private static List<LoginRealm> getCandidates ( LoginRealm primary, Collection<LoginRealm> realms, Set<String> candidateIds,
            Map<String, LoginRealm> idx ) {
        List<LoginRealm> candidates = new ArrayList<>();
        Set<String> realmIds = realms.stream().map(x -> x.getId()).collect(Collectors.toSet());
        candidates.add(primary);
        candidateIds.add(primary.getId());
        idx.put(primary.getId(), primary);
        for ( LoginRealm lr : realms ) {
            Collection<String> before = lr.getBefore() != null ? lr.getBefore() : Collections.EMPTY_SET;
            Collection<String> after = lr.getAfter() != null ? lr.getAfter() : Collections.EMPTY_SET;
            if ( before.isEmpty() && after.isEmpty() ) {
                // only suited as primary
                continue;
            }
            checkReferences(lr, "before", before, realmIds); //$NON-NLS-1$
            checkReferences(lr, "after", after, realmIds); //$NON-NLS-1$
            candidates.add(lr);
            candidateIds.add(lr.getId());
            idx.put(lr.getId(), lr);
        }
        return candidates;
    }


    /**
     * @param lr
     * @param type
     * @param items
     */
    private static void checkReferences ( LoginRealm lr, String type, Collection<String> items, Set<String> realmIds ) {
        for ( String itm : items ) {
            if ( WILDCARD.equals(itm) ) {
                continue;
            }
            if ( !realmIds.contains(itm) ) {
                log.warn(String.format("Realm %s references unknown realm %s in %s", lr.getId(), itm, type)); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param candidates
     * @param level
     */
    private static void dumpCandidates ( String msg, List<LoginRealm> candidates, Priority level ) {
        if ( log.isEnabledFor(level) ) {
            log.log(level, msg);
            for ( LoginRealm lr : candidates ) {
                log.log(level, String.format("%s before: %s after: %s", lr.getId(), lr.getBefore(), lr.getAfter())); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param candidateIds
     * @param candidates
     * @param idx
     * @return
     */
    private static List<LoginRealm> sortCandidates ( Set<String> candidateIds, List<LoginRealm> candidates, Map<String, LoginRealm> idx ) {
        Map<String, Set<String>> out = buildGraph(candidateIds, candidates, idx);

        if ( log.isTraceEnabled() ) {
            log.trace(out);
        }

        List<LoginRealm> sorted = topSortGraph(candidates, idx, out);
        if ( sorted.size() != candidates.size() ) {
            throw new AuthenticationException("Lost realm during sort"); //$NON-NLS-1$
        }
        return sorted;
    }


    /**
     * @param candidates
     * @param idx
     * @param out
     * @return
     */
    private static List<LoginRealm> topSortGraph ( List<LoginRealm> candidates, Map<String, LoginRealm> idx, Map<String, Set<String>> out ) {
        Queue<LoginRealm> toVisit = new LinkedList<>();
        Set<LoginRealm> temp = new HashSet<>();
        Set<LoginRealm> visited = new HashSet<>();
        toVisit.addAll(candidates);
        List<LoginRealm> sorted = new LinkedList<>();

        while ( !toVisit.isEmpty() ) {
            visitTopSort(out, idx, temp, sorted, visited, toVisit.poll());
        }
        return sorted;
    }


    /**
     * @param candidateIds
     * @param candidates
     * @param idx
     * @return
     */
    private static Map<String, Set<String>> buildGraph ( Set<String> candidateIds, List<LoginRealm> candidates, Map<String, LoginRealm> idx ) {
        Map<String, Set<String>> out = new HashMap<>();
        for ( LoginRealm lr : candidates ) {
            Collection<String> before = lr.getBefore() != null ? lr.getBefore() : Collections.EMPTY_SET;
            Collection<String> after = lr.getAfter() != null ? lr.getAfter() : Collections.EMPTY_SET;
            addForwardDependencies(candidateIds, idx, out, lr, before);
            addReverseDependencies(candidateIds, idx, out, lr, after);
            if ( !out.containsKey(lr.getId()) ) {
                out.put(lr.getId(), new HashSet<>());
            }
        }
        return out;
    }


    /**
     * @param candidateIds
     * @param idx
     * @param out
     * @param lr
     * @param after
     */
    static void addReverseDependencies ( Set<String> candidateIds, Map<String, LoginRealm> idx, Map<String, Set<String>> out, LoginRealm lr,
            Collection<String> after ) {
        for ( String aft : after ) {
            if ( WILDCARD.equals(aft) ) {
                for ( String cand : candidateIds ) {
                    LoginRealm candr = idx.get(cand);
                    if ( candr.getAfter() != null && candr.getAfter().contains(WILDCARD) ) {
                        continue;
                    }
                    addTo(cand, out, lr.getId());
                }
            }
            else if ( candidateIds.contains(aft) ) {
                addTo(aft, out, lr.getId());
            }
        }
    }


    /**
     * @param candidateIds
     * @param idx
     * @param out
     * @param lr
     * @param before
     */
    static void addForwardDependencies ( Set<String> candidateIds, Map<String, LoginRealm> idx, Map<String, Set<String>> out, LoginRealm lr,
            Collection<String> before ) {
        for ( String bef : before ) {
            if ( WILDCARD.equals(bef) ) {
                for ( String cand : candidateIds ) {
                    LoginRealm candr = idx.get(cand);
                    if ( candr.getBefore() != null && candr.getBefore().contains(WILDCARD) ) {
                        continue;
                    }
                    addTo(lr.getId(), out, cand);
                }
            }
            else if ( candidateIds.contains(bef) ) {
                addTo(lr.getId(), out, bef);
            }
        }
    }


    /**
     * @param out
     * @param temp
     * @param sorted
     * @param visited
     * @param pick
     */
    private static void visitTopSort ( Map<String, Set<String>> out, Map<String, LoginRealm> idx, Set<LoginRealm> temp, List<LoginRealm> sorted,
            Set<LoginRealm> visited, LoginRealm pick ) {
        if ( temp.contains(pick) ) {
            throw new AuthenticationException("Cannot resolve stack, contains cyclic dependencies"); //$NON-NLS-1$
        }
        if ( visited.contains(pick) ) {
            return;
        }
        temp.add(pick);
        for ( String o : out.get(pick.getId()) ) {
            visitTopSort(out, idx, temp, sorted, visited, idx.get(o));
        }
        visited.add(pick);
        temp.remove(pick);
        sorted.add(0, pick);
    }


    /**
     * @param id
     * @param out
     * @param ids
     */
    private static void addTo ( String id, Map<String, Set<String>> out, String oid ) {
        Set<String> items = out.get(id);
        if ( items == null ) {
            items = new HashSet<>();
            out.put(id, items);
        }
        items.add(oid);
    }


    /**
     * @param candidateIds
     * @param candidates
     */
    private static void filterUnresolvable ( Set<String> candidateIds, List<LoginRealm> candidates ) {
        boolean changed = true;
        while ( changed ) {
            changed = false;
            Iterator<LoginRealm> candIt = candidates.iterator();

            while ( candIt.hasNext() ) {
                LoginRealm lr = candIt.next();

                Collection<String> before = lr.getBefore() != null ? lr.getBefore() : Collections.EMPTY_SET;
                Collection<String> after = lr.getAfter() != null ? lr.getAfter() : Collections.EMPTY_SET;

                if ( isUnresolvable(candidateIds, lr, before) || isUnresolvable(candidateIds, lr, after) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Removing unresolvable %s before: %s after: %s", lr.getId(), lr.getBefore(), lr.getAfter())); //$NON-NLS-1$
                    }
                    changed = true;
                    candidateIds.remove(lr.getId());
                    candIt.remove();
                }
            }
        }
    }


    /**
     * @param candidateIds
     * @param lr
     * @param items
     */
    private static boolean isUnresolvable ( Set<String> candidateIds, LoginRealm lr, Collection<String> items ) {
        if ( !items.isEmpty() ) {
            boolean foundAny = false;
            for ( String bef : items ) {
                if ( WILDCARD.equals(bef) || candidateIds.contains(bef) ) {
                    foundAny = true;
                    break;
                }
            }

            if ( !foundAny ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Realm specified before but stack does not match, removing " + lr.getId()); //$NON-NLS-1$
                }
                return true;
            }
        }
        return false;
    }
}
