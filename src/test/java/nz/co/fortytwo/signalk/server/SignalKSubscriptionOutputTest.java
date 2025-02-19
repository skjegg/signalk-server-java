/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package nz.co.fortytwo.signalk.server;

import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_FULL;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_FIXED;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_speedApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_courseOverGroundTrue;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_position_latitude;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels_dot_self_dot;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.JsonSerializer;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.TestHelper;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SignalKSubscriptionOutputTest extends SignalKCamelTestSupport {
 
    static final String DIRECT_INPUT = "seda:input";
	static Logger logger = Logger.getLogger(SignalKSubscriptionOutputTest.class);

	MockEndpoint output = null;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    protected ProducerTemplate template;
	
    String jsonString = null;
	@Before
	public void before() throws Exception {

		
	}
	
	public void init() throws Exception{
		
		template= new DefaultProducerTemplate(routeManager.getContext());
		template.setDefaultEndpointUri(DIRECT_INPUT);
		template.start();
		SignalKModel model = SignalKModelFactory.getCleanInstance();
		SignalKModelFactory.loadConfig(signalkModel);
		logger.debug("SignalKModel at init:"+signalkModel);
		
		model.putAll(TestHelper.getBasicModel().getFullData());
		
		JsonSerializer ser = new JsonSerializer();
		jsonString=ser.write(model);
	}

	
	@Test
    public void shouldOutputSubscribedMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(Math.toRadians(11.96d),(double)signalkModel.getValue(vessels_dot_self_dot + nav_courseOverGroundTrue),0.00001);
		 logger.debug("Lat :"+(double)signalkModel.get(vessels_dot_self_dot + nav_position_latitude));
		 
		 //add a sub
		 Json sub = getSubscribe("vessels." + SignalKConstants.self,"navigation", 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 
		 output.assertIsSatisfied();
      
    }
	
	@Test
    public void shouldOutputPositionMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(Math.toRadians(11.96d),(double)signalkModel.getValue(vessels_dot_self_dot + nav_courseOverGroundTrue),0.001);
		 logger.debug("Lat :"+(double)signalkModel.get(vessels_dot_self_dot + nav_position_latitude));
		 
		 //add a sub
		 Json sub = getSubscribe("vessels." + SignalKConstants.self,nav_position, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 output.assertIsSatisfied();
		 SignalKModel out = (SignalKModel)output.getReceivedExchanges().get(0).getIn().getBody(SignalKModel.class);
		 logger.debug("Received msg: "+out);
		 
		 assertEquals(-41.29369354d,(double)out.get(vessels_dot_self_dot + nav_position_latitude), 0.001d);
		 assertTrue(out.getSubMap(vessels_dot_self_dot + env).size()==0);
    }
	@Test
    public void shouldOutputPositionWindMessage() throws Exception {
		init();
		String wsSession = UUID.randomUUID().toString();
        assertNotNull(template);
         output.reset();
         output.expectedMessageCount(1);
         template.sendBody(DIRECT_INPUT,jsonString);
         latch.await(2,TimeUnit.SECONDS);
		 logger.debug("SignalKModel:"+signalkModel);
		 assertEquals(Math.toRadians(11.96d),(double)signalkModel.getValue(vessels_dot_self_dot + nav_courseOverGroundTrue),0.00001);
		 logger.debug("Lat :"+(double)signalkModel.get(vessels_dot_self_dot + nav_position_latitude));
		 
		 //add a sub
		 Json sub = getSubscribe("vessels." + SignalKConstants.self,nav_position, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 sub = getSubscribe("vessels." + SignalKConstants.self,env_wind, 500, 0,FORMAT_FULL, POLICY_FIXED);
		 template.sendBodyAndHeader(DIRECT_INPUT, sub.toString(),WebsocketConstants.CONNECTION_KEY, wsSession);
		 
		 output.assertIsSatisfied();
		 SignalKModel out = output.getReceivedExchanges().get(0).getIn().getBody(SignalKModel.class);
		 logger.debug("Received msg: "+out);
		 logger.debug("Getting: "+vessels_dot_self_dot + nav_position_latitude);
		 assertEquals(-41.29369354d,(double)out.get(vessels_dot_self_dot + nav_position_latitude), 0.001d);
		 assertEquals(0.0,(double)out.getValue(vessels_dot_self_dot + env_wind_speedApparent), 0.001d);
    }
	
	
	@Override
	public void configureRouteBuilder(RouteBuilder routeBuilder) {
		output = (MockEndpoint) routeBuilder.getContext().getEndpoint("mock:output");
		try{
			SignalkRouteFactory.configureInputRoute(routeBuilder, DIRECT_INPUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		routeBuilder.from(RouteManager.SEDA_COMMON_OUT).to(output);
		
	}

}
