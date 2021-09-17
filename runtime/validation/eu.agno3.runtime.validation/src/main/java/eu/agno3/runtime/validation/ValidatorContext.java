/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation;


import java.util.Stack;

import javax.validation.Validator;


/**
 * @author mbechler
 *
 */
public class ValidatorContext {

    private ValidatorContext () {}

    private static final ThreadLocal<ValidatorContext> THREAD_LOCAL = ThreadLocal.withInitial( () -> {
        return new ValidatorContext();
    });


    /**
     * 
     * @return context
     */
    public static ValidatorContext getInstance () {
        return THREAD_LOCAL.get();
    }

    private Stack<ValidatorContextEntry> entries = new Stack<>();


    /**
     */
    public void pop () {
        this.entries.pop();
        if ( this.entries.isEmpty() ) {
            THREAD_LOCAL.remove();
        }
    }


    /**
     * @param v
     * @param groups
     */
    public void push ( Validator v, Class<?>[] groups ) {
        this.entries.push(new ValidatorContextEntry(v, groups));
    }


    /**
     * 
     * @return context entry
     */
    public ValidatorContextEntry top () {
        return this.entries.peek();
    }

    /**
     * @author mbechler
     *
     */
    public static class ValidatorContextEntry {

        private Class<?>[] groups;
        private Validator validator;


        /**
         * @param v
         * @param groups
         * 
         */
        public ValidatorContextEntry ( Validator v, Class<?>[] groups ) {
            this.validator = v;
            this.groups = groups;
        }


        /**
         * @return the groups
         */
        public Class<?>[] getGroups () {
            return this.groups;
        }


        /**
         * @return the validator
         * 
         */
        public Validator getValidator () {
            return this.validator;
        }
    }

}
