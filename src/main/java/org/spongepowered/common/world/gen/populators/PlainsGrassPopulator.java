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
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.Random;

public class PlainsGrassPopulator implements Populator {

    private NoiseGeneratorPerlin noise = new NoiseGeneratorPerlin(new Random(2345L), 1);
    private final boolean sunflowers;

    private Flower flowers;
    private Shrub grass;
    private DoublePlant plant;

    public PlainsGrassPopulator(boolean sunflowers) {
        this.sunflowers = sunflowers;
        this.flowers = Flower.builder()
                .perChunk(15)
                .type(PlantTypes.DANDELION, 2)
                .type(PlantTypes.POPPY, 1)
                .build();
        this.grass = Shrub.builder()
                .perChunk(5)
                .type(ShrubTypes.TALL_GRASS, 1)
                .build();
        this.plant = DoublePlant.builder()
                .type(DoublePlantTypes.GRASS, 1)
                .build();
    }

    @Override
    public PopulatorType getType() {
        return InternalPopulatorTypes.PLAINS_GRASS;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random random, ImmutableBiomeArea virtualBiomes) {
        Vector3i min = extent.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        double d0 = this.noise.getValue((chunkPos.getX() + 8) / 200.0D, (chunkPos.getZ() + 8) / 200.0D);

        if (d0 < -0.8D) {
            this.flowers.setFlowersPerChunk(15 * 64);
            this.grass.setShrubsPerChunk(5 * 64);
        } else {
            this.flowers.setFlowersPerChunk(4 * 64);
            this.grass.setShrubsPerChunk(10 * 64);

            this.plant.getPossibleTypes().clear();
            this.plant.getPossibleTypes().add(new WeightedObject<DoublePlantType>(DoublePlantTypes.GRASS, 1));
            this.plant.setPlantsPerChunk(7 * 5);
            this.plant.populate(world, extent, random, virtualBiomes);
        }
        this.flowers.populate(world, extent, random, virtualBiomes);
        this.grass.populate(world, extent, random, virtualBiomes);

        if (this.sunflowers) {
            this.plant.getPossibleTypes().clear();
            this.plant.getPossibleTypes().add(new WeightedObject<DoublePlantType>(DoublePlantTypes.SUNFLOWER, 1));
            this.plant.setPlantsPerChunk(10 * 5);
            this.plant.populate(world, extent, random, virtualBiomes);
        }
    }

}
