/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.fixed.exceptionhandler;


import javax.annotation.Generated;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;


@SuppressWarnings ( {
    "javadoc"
} )
@Generated ( "primefaces" )
public class PrimeExceptionHandlerFactory extends ExceptionHandlerFactory {

    private final ExceptionHandlerFactory wrapped;


    public PrimeExceptionHandlerFactory ( final ExceptionHandlerFactory wrapped ) {
        this.wrapped = wrapped;
    }


    @Override
    public ExceptionHandler getExceptionHandler () {
        return new PrimeExceptionHandler(this.wrapped.getExceptionHandler());
    }


    @Override
    public ExceptionHandlerFactory getWrapped () {
        return this.wrapped;
    }
}