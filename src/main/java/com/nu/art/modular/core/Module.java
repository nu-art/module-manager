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
import com.nu.art.reflection.tools.ReflectiveTools;

@SuppressWarnings("rawtypes")
public abstract class Module
		extends Logger
		implements ModuleManagerDelegator {

	private ModuleManager moduleManager;

	final void setMainManager(ModuleManager mainManager) {
		this.moduleManager = mainManager;
	}

	protected final <Type> Type[] getModulesAssignableFrom(Class<Type> classType) {
		return moduleManager.getModulesAssignableFrom(classType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}

	protected final <Type extends ModuleItem> Type createModuleItem(Class<Type> moduleItemType) {
		Type type = instantiateModuleItem(moduleItemType);
		type.init();
		return type;
	}

	protected <Type extends ModuleItem> Type instantiateModuleItem(Class<Type> moduleItemType) {
		Type moduleItem = ReflectiveTools.newInstance(moduleItemType);
		moduleItem.setModuleManager(moduleManager);
		return moduleItem;
	}

	protected final <ListenerType> void dispatchModuleEvent(String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		logInfo("Dispatch module event: " + message);
		moduleManager.dispatchModuleEvent(message, listenerType, processor);
	}

	protected abstract void init();

	protected void printDetails() {}

	protected void validateModule(ValidationResult result) {}
}
