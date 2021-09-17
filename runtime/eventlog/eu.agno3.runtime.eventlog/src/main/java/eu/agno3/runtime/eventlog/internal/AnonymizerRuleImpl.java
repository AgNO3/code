/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.Map;

import eu.agno3.runtime.eventlog.AnonymizerRule;


/**
 * @author mbechler
 *
 */
public class AnonymizerRuleImpl implements AnonymizerRule {

    private String field;
    private Map<String, String> opts;
    private String type;


    /**
     * @param field
     * @param type
     * @param opts
     */
    public AnonymizerRuleImpl ( String field, String type, Map<String, String> opts ) {
        this.field = field;
        this.type = type;
        this.opts = opts;
    }


    /**
     * @return the type
     */
    @Override
    public String getType () {
        return this.type;
    }


    /**
     * @return the field
     */
    @Override
    public String getField () {
        return this.field;
    }


    /**
     * @return the opts
     */
    @Override
    public Map<String, String> getOpts () {
        return this.opts;
    }
}
