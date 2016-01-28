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
package pers.hsn;

import pers.hsn.adaptor.impl.PMChannelAdaptor;
import pers.hsn.core.HsnServer;

public class HelloHsn {
	
	public static void main(String[] args) throws Exception {
		// 监听端口10080, 设置Backlog值为200
		HsnServer server = new HsnServer(10080, 200);
		
		// 设置用于处理连接相关操作的Selector数量(默认同CPU核心数量)
		server.setChannelSelectorCount(2);
		
		// 设置用于处理连接相关操作的线程数量(默认为ChannelSelectorCount的两倍)
		server.setChannelThreadCount(3);

		// 设置缓冲区对象池大小(默认1024)
		server.setBufferPoolSize(2048);
		// 设置缓冲区默认容量(默认1024)
		server.setBufferSize(1024);

		// 设置Channel适配器
		server.setChannelAdaptor(PMChannelAdaptor.class);
		
		// 启动Hsn
		server.start();
	}
}
