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
package me.smoe.hsn.task.impl;

import me.smoe.hsn.component.ChannelSession;
import me.smoe.hsn.task.ChannelTask;

public abstract class AbstractChannelTask implements ChannelTask {

	protected ChannelSession channelSession;

	protected AbstractChannelTask(ChannelSession channelSession) {
		super();
		this.channelSession = channelSession;
	}

	@Override
	public ChannelSession channelSession() {
		return channelSession;
	}
	
	@Override
	public int taskQueueIndex() {
		return channelSession.taskQueueIndex();
	}

}
