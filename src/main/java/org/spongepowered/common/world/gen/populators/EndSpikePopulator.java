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
package org.spongepowered.common.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.Random;

public class EndSpikePopulator implements Populator {

    private final WorldGenSpikes spikeGen = new WorldGenSpikes();

    @Override
    public PopulatorType getType() {
        return InternalPopulatorTypes.END_SPIKE;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random rand, ImmutableBiomeArea virtualBiomes) {
        Vector3i min = extent.getBlockMin().sub(8,0,8);
        World worldIn = (World) world;
        WorldGenSpikes.EndSpike[] aworldgenspikes$endspike = BiomeEndDecorator.getSpikesForWorld(worldIn);
        BlockPos pos = VecHelper.toBlockPos(min);
        for (WorldGenSpikes.EndSpike worldgenspikes$endspike : aworldgenspikes$endspike) {
            if (worldgenspikes$endspike.doesStartInChunk(pos)) {
                this.spikeGen.setSpike(worldgenspikes$endspike);
                this.spikeGen.generate(worldIn, rand,
                        new BlockPos(worldgenspikes$endspike.getCenterX(), 45, worldgenspikes$endspike.getCenterZ()));
            }
        }
    }

}
