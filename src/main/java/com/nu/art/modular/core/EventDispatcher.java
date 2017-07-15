/*
 * The module-manager project, is THE infrastructure that all my frameworks
 *  are based on, it allows encapsulation of logic where needed, and allow
 *  modules to converse without other design patterns limitations.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.modular.core;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;

/**
 * Created by TacB0sS on 16-Jul 2017.
 */

public class EventDispatcher
		extends Logger {

	private Object[] listeners = {};

	public EventDispatcher(String name) {
		setTag(name);
	}

	public final void addListener(Object listener) {
		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void setListeners(Object[] listeners) {
		this.listeners = listeners;
	}

	public final void removeListener(Object listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	@SuppressWarnings("unchecked")
	public <ParentType> void dispatchEvent(Class<ParentType> eventType, Processor<ParentType> processor) {
		for (Object module : listeners) {
			if (!eventType.isAssignableFrom(module.getClass()))
				continue;

			try {
				processor.process((ParentType) module);
			} catch (Throwable t) {
				logError("Error while processing event:\n + eventType:" + eventType.getSimpleName() + "\n listenerType:" + module.getClass(), t);
			}
		}
	}
}
