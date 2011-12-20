/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.language.simple.types;

import org.apache.camel.RuntimeCamelException;

/**
 * Holds information about error parsing the simple expression at a given location
 * in the input.
 */
public class SimpleParserException extends RuntimeCamelException {

    private final int index;

    public SimpleParserException(String message, int index) {
        super(message);
        this.index = index;
    }

    /**
     * Index where the parsing error occurred
     *
     * @return index of the parsing error in the input
     */
    public int getIndex() {
        return index;
    }
}
