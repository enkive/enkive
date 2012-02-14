/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.linuxbox.enkive.mailprocessor;

public enum ProcessorState {
	NOT_YET_STARTED,

	STARTING_UP_ARCHIVER,

	PREPARING_PROCESSOR,

	RETRIEVING_MESSAGE_DATA,

	ANALYZING_MESSAGE_DATA,

	PARSING_MESSAGE,

	POST_PROCESSING_MESSAGE,

	ARCHIVING,

	EMERGENCY_SAVING,

	IDLE,

	SHUTTING_DOWN,

	ERROR_HANDLING,

	WAITING_FOR_DATA,

	READING_ADDRESSES,

	READING_MESSAGE;
}
