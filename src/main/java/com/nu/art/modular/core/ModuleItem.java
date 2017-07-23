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
import com.nu.art.modular.interfaces.ModuleManagerDelegator;

/**
 * Created by TacB0sS on 08-Oct 2016.
 */

public abstract class ModuleItem
		extends Logger
		implements ModuleManagerDelegator {

	private ModuleManager moduleManager;

	protected abstract void init();

	public final <ListenerType> void dispatchModuleEvent(String message, final Class<ListenerType> listenerType, final Processor<ListenerType> processor) {
		logDebug("Dispatching Module Event: " + message);
		moduleManager.dispatchModuleEvent(message, listenerType, processor);
	}

	@Override
	public <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}

	final void setModuleManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}
}

