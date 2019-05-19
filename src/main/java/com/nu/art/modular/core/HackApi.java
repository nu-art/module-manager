package com.nu.art.modular.core;

/**
 * While I Completely disagree with this hack I have been ask number of times about this capability.
 *
 * Using this is a result of not sticking to the module pattern, and trying mix and match a bunch of frameworks..
 * Do me a favor.. don't mix and match it's stupid.. use the module management as intended or use whatever other crappy framework you'd like!!!
 */
public class HackApi {

	final <_Module extends Module> void registerModuleType(Class<_Module> moduleType) {
		ModuleManager.ModuleManager.registerModuleType(moduleType);
	}

	final <_Module extends Module> void registerModuleInstance(_Module module) {
		ModuleManager.ModuleManager.registerModuleInstance(module);
	}
}
