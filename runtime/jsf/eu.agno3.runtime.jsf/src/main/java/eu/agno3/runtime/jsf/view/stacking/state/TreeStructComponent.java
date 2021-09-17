/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.agno3.runtime.jsf.view.stacking.state;


import java.io.Serializable;

import javax.annotation.Generated;


/**
 * Stores facelet view state
 * 
 * Extracted from myfaces 2.2 DefaultFacleletsStateMamagementStrategy
 * 
 * @author mbechler
 * 
 */
@Generated ( "thirdparty" )
public class TreeStructComponent implements Serializable {

    private static final long serialVersionUID = 5069109074684737231L;
    private String componentClass;
    private String componentId;
    private TreeStructComponent[] children = null; // Array of children
    private Object[] facets = null; // Array of Array-tuples with Facetname and TreeStructComponent


    /**
     * @param componentClass
     * @param componentId
     */
    public TreeStructComponent ( String componentClass, String componentId ) {
        this.componentClass = componentClass;
        this.componentId = componentId;
    }


    /**
     * @return the component class
     */
    public String getComponentClass () {
        return this.componentClass;
    }


    /**
     * @return the component id
     */
    public String getComponentId () {
        return this.componentId;
    }


    /**
     * 
     * @param children
     */
    public void setChildren ( TreeStructComponent[] children ) {
        this.children = children;
    }


    /**
     * 
     * @return the child components
     */
    public TreeStructComponent[] getChildren () {
        return this.children;
    }


    /**
     * 
     * @return the component's facets
     */
    public Object[] getFacets () {
        return this.facets;
    }


    /**
     * 
     * @param facets
     */
    public void setFacets ( Object[] facets ) {
        this.facets = facets;
    }
}