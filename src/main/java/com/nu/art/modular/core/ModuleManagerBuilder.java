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

import com.nu.art.core.tools.ArrayTools;
import com.nu.art.modular.interfaces.ModuleManagerDelegator;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.ArrayList;

public class ModuleManagerBuilder
	implements ModuleManagerDelegator {

	protected final ModuleManager moduleManager;

	private Class<? extends ModulesPack>[] modulesPacksTypes;

	public ModuleManagerBuilder(Class<? extends ModuleManager> managerType) {
		moduleManager = ReflectiveTools.newInstance(managerType);
	}

	protected final void setModulesPacksTypes(Class<? extends ModulesPack>[] modulesPacksTypes) {
		this.modulesPacksTypes = modulesPacksTypes;
	}

	public final ModuleManager buildMainManager() {
		ArrayList<Class<? extends Module>> modulesTypes = new ArrayList<>();
		ArrayList<ModulesPack> modulesPacks = new ArrayList<>();

		ArrayList<Module> orderedModules = new ArrayList<>();
		for (Class<? extends ModulesPack> packType : modulesPacksTypes) {
			ModulesPack pack = ReflectiveTools.newInstance(packType);
			modulesPacks.add(pack);

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

		for (ModulesPack pack : modulesPacks) {
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
			module.printDetails();
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

	@Override
	public <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}
}
