/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua.caps;


import java.util.HashMap;
import java.util.Map;

import net.sf.uadetector.ReadableUserAgent;

import eu.agno3.runtime.http.ua.CapabilityEvaluator;
import eu.agno3.runtime.http.ua.UACapability;


/**
 * @author mbechler
 *
 */
public final class UACapabilities {

    /**
     * 
     */
    private UACapabilities () {}

    private static Map<UACapability, CapabilityEvaluator> CAP_EVALUATORS = new HashMap<>();

    static {
        CAP_EVALUATORS.put(UACapability.CSP10, new CSPCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.CSP10_STANDARD_HEADER, new CSPStandardHeaderCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.CSP10_EXPERIMENTAL_HEADER, new CSPExperimentalHeaderCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.CSP10_WEBKIT_HEADER, new CSPWebkitHeaderCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.CSP10_SANDBOXING, new CSPSandboxCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.FRAME_SANDBOXING, new FrameSandboxCapabilityEvaluator());
        CAP_EVALUATORS.put(UACapability.NO_MULTI_AUTH, new NoMultiAuthCapabilityEvaluator());
    }


    /**
     * @param cap
     * @param ua
     * @param raw
     * @return whether the ua has the capability
     */
    public static boolean hasCapability ( UACapability cap, ReadableUserAgent ua, String raw ) {
        CapabilityEvaluator evaluator = CAP_EVALUATORS.get(cap);
        if ( evaluator == null ) {
            return false;
        }
        return evaluator.hasCapability(ua, raw);
    }
}
