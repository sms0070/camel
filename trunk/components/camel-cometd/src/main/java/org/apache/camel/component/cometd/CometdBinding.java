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
package org.apache.camel.component.cometd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.BayeuxServerImpl;

/**
 * A Strategy used to convert between a Camel {@link Exchange} and
 * to and from a Cometd messages
 */
public class CometdBinding {
    public static final String HEADERS_FIELD = "CamelHeaders";
    public static final String COMETD_CLIENT_ID_HEADER_NAME = "CometdClientId";
    public static final String SUBSCRIPTION_HEADER_NAME = "subscription";

    private final BayeuxServerImpl bayeux;

    public CometdBinding(BayeuxServerImpl bayeux) {
        this.bayeux = bayeux;
    }

    public ServerMessage.Mutable createCometdMessage(ServerChannel channel, ServerSession serverSession, Message camelMessage) {
        ServerMessage.Mutable mutable = bayeux.newMessage();
        mutable.setChannel(channel.getId());

        if (serverSession != null) {
            mutable.setClientId(serverSession.getId());
        }
        addHeadersToMessage(mutable, camelMessage);
        mutable.setData(camelMessage.getBody());
        return mutable;
    }

    public Message createCamelMessage(ServerSession remote, ServerMessage cometdMessage, Object data) {
        if (cometdMessage != null) {
            data = cometdMessage.getData();
        }

        Message message = new DefaultMessage();
        message.setBody(data);
        message.setHeaders(getHeadersFromMessage(cometdMessage));
        message.setHeader(COMETD_CLIENT_ID_HEADER_NAME, remote.getId());

        if (cometdMessage.get(SUBSCRIPTION_HEADER_NAME) != null) {
            message.setHeader(SUBSCRIPTION_HEADER_NAME, cometdMessage.get(SUBSCRIPTION_HEADER_NAME));
        }

        return message;
    }


    public void addHeadersToMessage(ServerMessage.Mutable cometdMessage, Message camelMessage) {
        if (camelMessage.hasHeaders()) {
            Map<String, Object> ext = cometdMessage.getExt(true);

            ext.put(HEADERS_FIELD, filterHeaders(camelMessage.getHeaders()));
        }
    }

    //TODO: do something in the style of JMS where they have header Strategies?
    private Object filterHeaders(Map<String, Object> headers) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> entry : headers.entrySet()) {
            if (entry != null && entry.getKey() != null) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getHeadersFromMessage(ServerMessage message) {
        Map<String, Object> ext = message.getExt();
        if (ext != null && ext.containsKey(HEADERS_FIELD)) {
            return (Map<String, Object>) ext.get(HEADERS_FIELD);
        } else {
            return null;
        }
    }

}
