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

package com.nu.art.software.modular.core;

import com.nu.art.software.modular.interfaces.ModuleManagerDelegator;

/**
 * Created by TacB0sS on 08-Oct 2016.
 */

public abstract class ModuleItem
		implements ModuleManagerDelegator {

	private ModuleManager moduleManager;

	protected abstract void init();

	@Override
	public <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}

	final void setModuleManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}
}
