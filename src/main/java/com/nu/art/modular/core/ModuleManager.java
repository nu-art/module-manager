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
import com.nu.art.modular.interfaces.ModuleManagerDelegator;
import com.nu.art.reflection.injector.Injector;
import com.nu.art.reflection.tools.ART_Tools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author TacB0sS
 */
@SuppressWarnings("rawtypes")
public class ModuleManager
		implements ModuleManagerDelegator {

	public interface OnModuleInitializedListener {

		void onModuleInitialized(Module module);
	}

	public final class ModuleInjector
			extends Injector<Module, Object> {

		private ModuleInjector() {}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValueForField(Field field) {
			if (field.getType() == Module.class)
				return null;

			Module module = getModule((Class<? extends Module>) field.getType());
			if (module == null)
				throw new ImplementationMissingException("You need to add the module of type: '" + field.getType() + "' to one of your ModulePacks");

			return module;
		}

		@Override
		protected Field[] extractFieldsFromInstance(Class<?> injecteeType) {
			return ART_Tools.getFieldsWithAnnotationAndTypeFromClassHierarchy(injecteeType, Object.class, null, null, Module.class);
		}
	}

	private final ModuleInjector moduleInjector = new ModuleInjector();

	private OnModuleInitializedListener moduleInitializedListener;

	/**
	 * Holds a references to all the module types which have registered to this main module,
	 */
	private HashMap<Class<? extends Module>, Module> registeredModules = new HashMap<>();

	private Module[] orderedModules = {};

	protected ModuleManager() {
	}

	public final ModuleInjector getInjector() {
		return moduleInjector;
	}

	@Override
	public final <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return getModule(moduleType, true);
	}

	final void setOrderedModules(Module[] orderedModules) {
		this.orderedModules = orderedModules;
	}

	protected final Module[] getOrderedModules() {
		return orderedModules;
	}

	public final void setModuleListener(OnModuleInitializedListener moduleInitializedListener) {
		this.moduleInitializedListener = moduleInitializedListener;
	}

	@SuppressWarnings("unchecked")
	private <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType, boolean throwException) {
		ModuleType module = (ModuleType) registeredModules.get(moduleType);
		if (module == null && throwException) {
			throw new ImplementationMissingException("MUST add module of type: '" + moduleType.getName() + "' to one of your module packs");
		}
		return module;
	}

	final void init() {
		for (Module module : orderedModules) {
			module.init();

			if (moduleInitializedListener == null)
				continue;

			moduleInitializedListener.onModuleInitialized(module);
		}
	}

	/**
	 * @param moduleType The module type to register with the Module Manager.
	 */
	@SuppressWarnings("unchecked")
	final <_Module extends Module> _Module registerModule(Class<_Module> moduleType) {
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
				String errorMessage = "Error while processing module event:\n   parentType: " + parentType.getSimpleName() + "\n   moduleType: " + module.getClass()
						.getSimpleName();
				System.err.print(errorMessage);
				t.printStackTrace();
			}
		}
	}
}
