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
package pers.adar.hsn.logger;

import pers.adar.hsn.common.HsnProperties;

public class Logger {

	private static final org.slf4j.Logger HSN_LOGGER = org.slf4j.LoggerFactory.getLogger(HsnProperties.HSN);
	
	public static void info(String message) {
		HSN_LOGGER.info(message);
	}

	public static void info(String message, Throwable throwable) {
		HSN_LOGGER.info(message, throwable);
	}
	
	public static void debug(String message) {
		HSN_LOGGER.debug(message);
	}

	public static void debug(String message, Throwable throwable) {
		HSN_LOGGER.debug(message, throwable);
	}
	
	public static void warn(String message) {
		HSN_LOGGER.warn(message);
	}

	public static void warn(String message, Throwable throwable) {
		HSN_LOGGER.warn(message, throwable);
	}
	
	public static void error(String message) {
		HSN_LOGGER.error(message);
	}

	public static void error(String message, Throwable throwable) {
		HSN_LOGGER.error(message, throwable);
	}
}
