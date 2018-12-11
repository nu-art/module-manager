/*
 * The module-manager project, is THE infrastructure that all my frameworks
 *  are based on, it allows encapsulation of logic where needed, and allow
 *  modules to converse without other design patterns limitations.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
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
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.ArrayList;

public class ModuleManagerBuilder
	extends Logger {

	private ArrayList<ModulesPack> modulePacks = new ArrayList<>();

	public ModuleManagerBuilder() {
	}

	@SuppressWarnings("unchecked")
	public final ModuleManagerBuilder addModulePacks(Class<? extends ModulesPack>... modulePacks) {
		for (Class<? extends ModulesPack> packType : modulePacks) {
			ModulesPack pack = ReflectiveTools.newInstance(packType);
			this.modulePacks.add(pack);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public final ModuleManagerBuilder addModules(Class<? extends Module>... modules) {
		this.modulePacks.add(new ModulesPack(modules));
		return this;
	}

	public final ModuleManager build() {
		return build(new ModuleManager());
	}

	public final ModuleManager build(ModuleManager moduleManager) {
		ArrayList<Class<? extends Module>> modulesTypes = new ArrayList<>();

		ArrayList<Module> orderedModules = new ArrayList<>();

		for (ModulesPack pack : modulePacks) {
			pack.setManager(moduleManager);
			for (Class<? extends Module> moduleType : pack.getModuleTypes()) {
				if (modulesTypes.contains(moduleType))
					continue;

				modulesTypes.add(moduleType);
				Module module = moduleManager.registerModule(moduleType);
				if (module == null)
					continue;

				orderedModules.add(module);
				setupModule(module);
			}
		}

		moduleManager.setOrderedModules(ArrayTools.asArray(orderedModules, Module.class));

		for (ModulesPack pack : modulePacks) {
			pack.init();
		}

		Module[] registeredModules = moduleManager.getOrderedModules();
		validateModules(registeredModules);

		for (Module registeredModule : registeredModules) {
			moduleManager.getInjector().injectToInstance(registeredModule);
		}

		moduleManager.onBuildPrepared();
		moduleManager.init();
		for (Module module : registeredModules) {
			logInfo("----------- " + module.getClass().getSimpleName() + " ------------");
			module.printDetails();
			logInfo("-------- End of " + module.getClass().getSimpleName() + " --------");
		}

		moduleManager.onBuildCompleted();
		return moduleManager;
	}

	protected void setupModule(Module module) {
	}

	@SuppressWarnings("unused")
	private void validateModules(Module[] allRegisteredModuleInstances) {
		ValidationResult result = new ValidationResult();

		for (Module module : allRegisteredModuleInstances)
			module.validateModule(result);

		if (!result.isEmpty())
			throw new com.nu.art.modular.exceptions.ModuleNotSupportedException("\n" + result.getErrorData());
	}

	@SuppressWarnings("unused")
	protected void postInit(Module[] allRegisteredModuleInstances) {
	}
}
