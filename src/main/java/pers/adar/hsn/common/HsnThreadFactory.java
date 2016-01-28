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
package pers.adar.hsn.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HsnThreadFactory {
	
	public static ThreadFactory buildAcceptSelectorFactory() {
		return new AcceptSelectorFactory();
	}

	public static ThreadFactory buildChannelSelectorFactory() {
		return new ChannelSelectorFactory();
	}

	public static ThreadFactory buildChannelHandlerFactory() {
		return new ChannelHandlerFactory();
	}
	
	private static class AcceptSelectorFactory implements ThreadFactory {
		
		private static final String mark = "AcceptSelector";

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.setName(mark);
			
			return thread;
		}
	}

	private static class ChannelSelectorFactory implements ThreadFactory {
		
		private static final String mark = "ChannelSelector-";
		
		private final AtomicInteger id = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.setName(mark + id.incrementAndGet());
			
			return thread;
		}
	}
	
	private static class ChannelHandlerFactory implements ThreadFactory {
		
		private static final String mark = "ChannelHandler-";
		
		private final AtomicInteger id = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.setName(mark + id.incrementAndGet());
			
			return thread;
		}
	}
}
