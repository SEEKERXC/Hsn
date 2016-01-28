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
package pers.adar.hsn.task.impl;

import java.io.IOException;

import pers.adar.hsn.component.ChannelSession;

public class ChannelReadTask extends AbstractChannelTask {
	
	public ChannelReadTask(ChannelSession channelSession) {
		super(channelSession);
	}

	@Override
	public void run() {
		if (channelSession.checkClose()) {
			return;
		}

		try {
			if (channelSession.readChannel() == -1) {
				channelSession.close();
				return;
			}
		} catch (IOException e) {
			channelSession.onExeception(e);
		}
		
		channelSession.filpReadBuffer();
		
		channelSession.onMessage();
		
		channelSession.flush();

		channelSession.clearReadBuffer();

		channelSession.readable();
		
		channelSession.checkClose();
	}
}
