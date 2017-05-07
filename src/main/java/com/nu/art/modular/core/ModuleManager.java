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

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.HashMap;

/**
 * @author TacB0sS
 */
@SuppressWarnings("rawtypes")
public class ModuleManager
		implements com.nu.art.modular.interfaces.ModuleManagerDelegator {

	/**
	 * Holds a references to all the module types which have registered to this main module,
	 */
	private HashMap<Class<? extends Module>, Module> registeredModules = new HashMap<>();

	private Module[] orderedModules = {};

	protected ModuleManager() {
	}

	@Override
	public final <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return getModule(moduleType, false);
	}

	final void setOrderedModules(Module[] orderedModules) {
		this.orderedModules = orderedModules;
	}

	protected final Module[] getOrderedModules() {
		return orderedModules;
	}

	@SuppressWarnings("unchecked")
	private <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType, boolean throwException) {
		ModuleType module = (ModuleType) registeredModules.get(moduleType);
		if (module == null && throwException) {
			throw new ImplementationMissingException("MUST add module of type: '" + moduleType.getName() + "' In your application module" + " " + "builder!!!");
		}
		return module;
	}

	final void init() {
		for (Module module : orderedModules) {
			module.init();
		}
	}

	/**
	 * @param moduleType The module type to register with the Module Manager.
	 */
	@SuppressWarnings("unchecked")
	protected final <_Module extends Module> _Module registerModule(Class<_Module> moduleType) {
		_Module module = (_Module) registeredModules.get(moduleType);
		if (module != null) {
			return null;
		}
		module = ReflectiveTools.newInstance(moduleType);
		module.setMainManager(this);

		registeredModules.put(moduleType, module);
		return module;
	}

	protected void onBuildCompleted() {}

	@SuppressWarnings("unchecked")
	protected <ParentType> void dispatchModuleEvent(String message, Class<ParentType> parentType, Processor<ParentType> processor) {
		for (Module module : orderedModules) {
			if (!parentType.isAssignableFrom(module.getClass()))
				continue;

			try {
				processor.process((ParentType) module);
			} catch (Throwable t) {
				String errorMessage = "Error while processing module event:\n   parentType: " + parentType.getSimpleName() + "\n   moduleType: " + module
						.getClass().getSimpleName();
				System.err.print(errorMessage);
				t.printStackTrace();
			}
		}
	}
}
