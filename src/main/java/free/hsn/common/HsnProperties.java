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
package free.hsn.common;

public class HsnProperties {
	
	public static final String HSN = "HSN";
	
	public static final int DEFAULT_CHANNEL_SELECTOR_COUNT = Runtime.getRuntime().availableProcessors();

	public static final int DEFAULT_CHANNEL_THREAD_COUNT = DEFAULT_CHANNEL_SELECTOR_COUNT * 2;
	
	public static final int DEFAULT_BUFFER_POOL_SIZE = 1024;
	
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	
	public static final int DEFAULT_BUFFER_POOL_KEEPALIVE = 60;

	public static final int BACKLOG = 512;
	
	public static final int PERFORMANCE_CONNECTIONTIME = 2;
	
	public static final int PERFORMANCE_LATENCY = 1;
	
	public static final int PERFORMANCE_BANDWIDTH = 3;
}
