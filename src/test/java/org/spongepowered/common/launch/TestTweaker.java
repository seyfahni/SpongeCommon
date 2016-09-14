package org.spongepowered.common.launch;

import static org.spongepowered.asm.mixin.MixinEnvironment.Side.SERVER;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.util.List;

public class TestTweaker implements ITweaker {

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        SpongeLaunch.setupMixinEnvironment();
        MixinEnvironment.getDefaultEnvironment().setSide(SERVER);
    }

    @Override
    public String getLaunchTarget() {
        return "org.spongepowered.common.launch.TestMain";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

}
