/**
 * Copyright (c) 2015, adar.w (adar.w@outlook.com) 
 * 
 * http://www.smoe.me
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
package me.smoe.hsn.adaptor.impl;

import me.smoe.hsn.component.ChannelSession.ChannelContext;

/**
 * Http压测Adaptor
 */
public class PMChannelAdaptor extends StandardChannelAdaptor {
	
	private static final byte[] PM_HTTP_RESPONSE;
	static {
		StringBuilder buf = new StringBuilder();
		buf.append("HTTP/1.1 200 OK\r\n");
		buf.append("Connection: keep-alive\r\n");
		buf.append("Content-Length: 2\r\n");
		buf.append("\r\n");
		buf.append("ok");
		
		PM_HTTP_RESPONSE = buf.toString().getBytes();
	}
	
	@Override
	public void onMessage(ChannelContext channelContext) {
		channelContext.write(PM_HTTP_RESPONSE);
		channelContext.close();
	}
}
