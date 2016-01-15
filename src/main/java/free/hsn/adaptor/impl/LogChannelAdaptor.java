/**
 * Copyright (c) 2015, adar.w (adar.w@outlook.com) 
 * 
 * http://www.adar-w.me
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
package free.hsn.adaptor.impl;

import free.hsn.component.ChannelSession.ChannelContext;
import free.hsn.logger.Logger;

public class LogChannelAdaptor extends StandardChannelAdaptor {

	@Override
	public void onConnected(ChannelContext channelContext) {
		Logger.info("On Connected");
	}

	@Override
	public void onMessage(ChannelContext channelContext) {
		Logger.info("On Message");
		channelContext.close();
	}

	@Override
	public void onExeception(ChannelContext channelContext, Throwable throwable) {
		Logger.info("On Exeception");
	}

	@Override
	public void onClosed(ChannelContext channelContext) {
		Logger.info("On Closed");
	}
}
