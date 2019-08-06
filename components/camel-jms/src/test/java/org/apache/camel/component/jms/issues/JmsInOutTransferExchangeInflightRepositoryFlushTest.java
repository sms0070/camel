/*
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
package org.apache.camel.component.jms.issues;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.CamelJmsTestHelper;
import org.apache.camel.component.jms.SerializableRequestDto;
import org.apache.camel.component.jms.SerializableResponseDto;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

public class JmsInOutTransferExchangeInflightRepositoryFlushTest extends CamelTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent("activemq", jmsComponentAutoAcknowledge(connectionFactory));
        return camelContext;
    }

    @Test
    public void testTransferExchangeInOut() throws Exception {
        assertEquals(0, context().getInflightRepository().size());

        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(1);

        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new SerializableRequestDto("Restless Camel"));
            }
        });

        assertMockEndpointsSatisfied();

        assertEquals(0, context().getInflightRepository().size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .log("A ${exchangeId}")
                        .inOut("activemq:responseGenerator?transferExchange=true&requestTimeout=20000")
                        .log("A ${exchangeId}")
                        .to("log:result", "mock:result");

                from("activemq:responseGenerator?transferExchange=true")
                        .log("B ${exchangeId}")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                // there are 2 inflight (one for both routes)
                                assertEquals(2, exchange.getContext().getInflightRepository().size());
                                exchange.getMessage().setBody(new SerializableResponseDto(true));
                            }
                        }).to("log:reply")
                        .log("B ${exchangeId}");
            }
        };
    }
}
