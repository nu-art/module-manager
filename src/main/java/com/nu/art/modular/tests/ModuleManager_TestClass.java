package com.nu.art.modular.tests;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.DefaultLogClient;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleManager;
import com.nu.art.modular.core.ModuleManagerBuilder;
import com.nu.art.modular.core.ModulesPack;

public class ModuleManager_TestClass
	extends ComponentBaseTest {

	public static ModuleManager moduleManager;

	@SuppressWarnings("unchecked")
	public static void initWithModules(Class<? extends Module>... moduleTypes) {
		if (moduleManager != null)
			return;

		BeLogged.getInstance().addClient(new DefaultLogClient());
		moduleManager = new ModuleManagerBuilder().addModules(moduleTypes).build();
	}

	@SuppressWarnings("unchecked")
	public static void initWithPacks(Class<? extends ModulesPack>... packs) {
		if (moduleManager != null)
			return;

		BeLogged.getInstance().addClient(new DefaultLogClient());
		moduleManager = new ModuleManagerBuilder().addModulePacks(packs).build();
	}

	public static <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}
}
