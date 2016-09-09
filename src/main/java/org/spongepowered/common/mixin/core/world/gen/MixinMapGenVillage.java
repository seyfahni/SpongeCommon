/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.world.gen;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.world.gen.IFlaggedPopulator;
import org.spongepowered.common.world.gen.WorldGenConstants;

import java.util.List;
import java.util.Random;

/**
 * The placement of villages within a chunk has impact can have impact on other
 * populators. Possibly this should be added as a concept in the API in order to
 * allow better access and control over the function of these populators while
 * preserving vanilla functionality.
 */
@Mixin(MapGenVillage.class)
public abstract class MixinMapGenVillage extends MapGenStructure implements IFlaggedPopulator {

    @Override
    public void populate(Extent extent, org.spongepowered.api.world.World worldIn, Random random, ImmutableBiomeArea virtualBiomes, List<String> flags) {
        Vector3i min = extent.getBlockMin();
        World world = (World) worldIn;
        boolean flag = generateStructure(world, random, new ChunkPos((min.getX() - 8) / 16, (min.getZ() - 8) / 16));
        if (flag) {
            flags.add(WorldGenConstants.VILLAGE_FLAG);
        }
    }

}
