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
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.GenericParamExtractor;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.modular.interfaces.ModuleManagerDelegator;
import com.nu.art.reflection.injector.Injector;
import com.nu.art.reflection.tools.ART_Tools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author TacB0sS
 */
@SuppressWarnings("rawtypes")
public class ModuleManager
	extends Logger
	implements ModuleManagerDelegator {

	protected void onBuildPrepared() {
		logVerbose(" Application Starting...");
		logVerbose(" ");
		logVerbose(" _______  _______  _______  _       _________ _______  _______ __________________ _______  _          _______ _________ _______  _______ _________ _______  ______  ");
		logVerbose("(  ___  )(  ____ )(  ____ )( \\      \\__   __/(  ____ \\(  ___  )\\__   __/\\__   __/(  ___  )( (    /|  (  ____ \\\\__   __/(  ___  )(  ____ )\\__   __/(  ____ \\(  __  \\ ");
		logVerbose("| (   ) || (    )|| (    )|| (         ) (   | (    \\/| (   ) |   ) (      ) (   | (   ) ||  \\  ( |  | (    \\/   ) (   | (   ) || (    )|   ) (   | (    \\/| (  \\  )");
		logVerbose("| (___) || (____)|| (____)|| |         | |   | |      | (___) |   | |      | |   | |   | ||   \\ | |  | (_____    | |   | (___) || (____)|   | |   | (__    | |   ) |");
		logVerbose("|  ___  ||  _____)|  _____)| |         | |   | |      |  ___  |   | |      | |   | |   | || (\\ \\) |  (_____  )   | |   |  ___  ||     __)   | |   |  __)   | |   | |");
		logVerbose("| (   ) || (      | (      | |         | |   | |      | (   ) |   | |      | |   | |   | || | \\   |        ) |   | |   | (   ) || (\\ (      | |   | (      | |   ) |");
		logVerbose("| )   ( || )      | )      | (____/\\___) (___| (____/\\| )   ( |   | |   ___) (___| (___) || )  \\  |  /\\____) |   | |   | )   ( || ) \\ \\__   | |   | (____/\\| (__/  )");
		logVerbose("|/     \\||/       |/       (_______/\\_______/(_______/|/     \\|   )_(   \\_______/(_______)|/    )_)  \\_______)   )_(   |/     \\||/   \\__/   )_(   (_______/(______/ ");
		logVerbose(" ");
	}

	public interface OnModuleInitializedListener {

		void onModuleInitialized(Module module);
	}

	public final class ModuleInjector
		extends Injector<Module, Object> {

		private ModuleInjector() {}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValueForField(Object instance, Field field) {
			if (field.getType() == Module.class)
				return null;

			Module module = getModule((Class<? extends Module>) field.getType(), false);
			if (module == null)
				throw new ImplementationMissingException("Cannot set module to field: " + field + "\n  MUST add the module of type: '" + field.getType() + "' to one of your ModulePacks");

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

	private EventDispatcher eventDispatcher;

	private Module[] orderedModules = {};

	public static ModuleManager ModuleManager;

	protected ModuleManager() {
		this(GenericParamExtractor._GenericParamExtractor);
	}

	protected ModuleManager(GenericParamExtractor paramExtractor) {
		if (ModuleManager != null)
			throw new BadImplementationException("THERE CAN ONLY BE ONE MODULE MANAGER IN A JVM!!");

		eventDispatcher = new EventDispatcher("ModulesEventDispatcher", paramExtractor);
		ModuleManager = this;
	}

	public final ModuleInjector getInjector() {
		return moduleInjector;
	}

	@SuppressWarnings("unchecked")
	public final <Type> Type[] getModulesAssignableFrom(Class<Type> classType) {
		ArrayList<Type> modules = new ArrayList<>();
		for (Module orderedModule : orderedModules) {
			if (!classType.isAssignableFrom(orderedModule.getClass()))
				continue;

			modules.add((Type) orderedModule);
		}
		return ArrayTools.asArray(modules, classType);
	}

	@Override
	public final <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return getModule(moduleType, true);
	}

	final void setOrderedModules(Module[] orderedModules) {
		this.orderedModules = orderedModules;
		for (Module orderedModule : orderedModules) {
			eventDispatcher.addListener(orderedModule);
		}
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
			module.assignToDefaultInterface();
		}

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
		if (module != null)
			return null;

		module = ReflectiveTools.newInstance(moduleType);
		module.setMainManager(this);

		for (Class<? extends Module> key : module.keys) {
			Module olderModule = registeredModules.put(key, module);
			if (olderModule != null)
				logWarning("Shared Module key " + key + " between modules: " + olderModule.getClass() + " and " + module.getClass());
		}

		return module;
	}

	protected void onBuildCompleted() {}

	final void prepareModuleItem(ModuleItem moduleItem) {
		getInjector().injectToInstance(moduleItem);
		eventDispatcher.addListener(moduleItem);
	}

	final void disposeModuleItem(ModuleItem moduleItem) {
		eventDispatcher.removeListener(moduleItem);
	}

	@SuppressWarnings("unchecked")
	public <ListenerType> void dispatchModuleEvent(ILogger originator, String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		if (originator != null)
			originator.logInfo("Dispatching Module Event: " + message);
		eventDispatcher.dispatchEvent(null, listenerType, processor);
	}
}
