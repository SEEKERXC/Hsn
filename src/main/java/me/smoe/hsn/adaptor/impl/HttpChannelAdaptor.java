/**
 * Copyright (c) 2016, adar.w (adar.w@outlook.com) 
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

import java.nio.charset.Charset;

import me.smoe.hsn.component.ChannelSession.ChannelContext;

public class HttpChannelAdaptor extends StandardChannelAdaptor {

	@Override
	public void onMessage(ChannelContext channelContext) {
		String httpContent = Charset.forName("UTF-8").decode(channelContext.read()).toString();
		
		Object httpContentBefore = channelContext.getAttribute("http");
		if (httpContentBefore != null) {
			httpContent = httpContentBefore + httpContent;
		}
		
		if (checkOver(httpContent)) {
			channelContext.write(httpContent.getBytes());
			channelContext.setAttribute("http", null);
		} else {
			channelContext.setAttribute("http", httpContent);
		}
	}

	/*
	 * Need to complete
	 */
	private boolean checkOver(String httpContent) {
		return httpContent.endsWith("\r\n\r\n");
	}
}
