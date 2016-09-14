package org.spongepowered.common.launch;

import net.minecraft.launchwrapper.Launch;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class LaunchWrapperTestRunner extends BlockJUnit4ClassRunner {

    private static boolean initialized;

    public LaunchWrapperTestRunner(Class<?> klass) throws InitializationError {
        super(loadTestClass(klass));
    }

    public static Class<?> loadTestClass(Class<?> originalClass) throws InitializationError {
        if (!initialized) {
            initialized = true;
            Launch.main(new String[]{"--tweakClass", "org.spongepowered.common.launch.TestTweaker"});
        }

        // JUnit attempts to lookup the @Test annotation so we need to make sure the classes are loaded
        // using the same class loader (the main class loader)
        Launch.classLoader.addClassLoaderExclusion("org.junit.");
        Launch.classLoader.addClassLoaderExclusion("org.hamcrest.");

        try {
            return Class.forName(originalClass.getName(), true, Launch.classLoader);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
    }

}
