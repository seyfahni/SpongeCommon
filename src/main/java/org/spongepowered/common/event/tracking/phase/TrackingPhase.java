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
package org.spongepowered.common.event.tracking.phase;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public abstract class TrackingPhase {

    @Nullable private final TrackingPhase parent;

    private final List<TrackingPhase> children = new ArrayList<>();

    TrackingPhase(@Nullable TrackingPhase parent) {
        this.parent = parent;
    }

    @Nullable
    public TrackingPhase getParent() {
        return this.parent;
    }

    public List<TrackingPhase> getChildren() {
        return this.children;
    }

    public TrackingPhase addChild(TrackingPhase child) {
        this.children.add(child);
        return this;
    }

    /**
     * The exit point of any phase. Every phase should have an unwinding
     * process where if anything is captured, events should be thrown and
     * processed accordingly. The outcome of each phase is dependent on
     * the {@link IPhaseState} provded, as different states require different
     * handling.
     *
     * <p>Examples of this include: {@link PacketPhase}, {@link TickPhase}, etc.
     * </p>
     *
     * <p>Note that the {@link CauseTracker} is only provided for easy access
     * to the {@link WorldServer}, {@link IMixinWorldServer}, and
     * {@link World} instances.</p>
     *
     * @param causeTracker The cause tracker instance
     * @param state The state
     * @param phaseContext The context of the current state being unwound
     */
    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);


    /**
     * This is the post dispatch method that is automatically handled for
     * states that deem it necessary to have some post processing for
     * advanced game mechanics. This is always performed when capturing
     * has been turned on during a phases's
     * {@link #unwind(CauseTracker, IPhaseState, PhaseContext)} is
     * dispatched. The rules of post dispatch are as follows:
     * - Entering extra phases is not allowed: This is to avoid
     *  potential recursion in various corner cases.
     * - The unwinding phase context is provided solely as a root
     *  cause tracking for any nested notifications that require
     *  association of causes
     * - The unwinding phase is used with the unwinding state to
     *  further exemplify during what state that was unwinding
     *  caused notifications. This narrows down to the exact cause
     *  of the notifications.
     * - post dispatch may loop several times until no more notifications
     *  are required to be dispatched. This may include block physics for
     *  neighbor notification events.
     *
     * @param causeTracker The cause tracker responsible for the dispatch
     * @param unwindingState The state that was unwinding
     * @param unwindingContext The context of the state that was unwinding,
     *     contains the root cause for the state
     * @param postContext The post dispatch context captures containing any
     *     extra captures that took place during the state's unwinding
     */
    public void postDispatch(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext unwindingContext, PhaseContext postContext) {
    }

    protected void processPostItemSpawns(CauseTracker causeTracker, IPhaseState unwindingState, ArrayList<Entity> items) {
        final SpawnEntityEvent
                event =
                SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE, items, causeTracker.getWorld());
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        }
    }

    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext phaseContext,
            ArrayList<Entity> entities) {
        final SpawnEntityEvent
                event =
                SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE, entities, causeTracker.getWorld());
        final User creator = phaseContext.getNotifier().orElseGet(() -> phaseContext.getOwner().orElse(null));
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                if (creator != null) {
                    EntityUtil.toMixin(entity).setCreator(creator.getUniqueId());
                }
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        }
    }

    // Default methods that are basic qualifiers, leaving up to the phase and state to decide
    // whether they perform capturing.

    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    // TODO
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return false;
    }

    public boolean allowEntitySpawns(IPhaseState currentState) {
        return true;
    }

    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return false;
    }

    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return false;
    }

    public boolean alreadyCapturingBlockTicks(IPhaseState phaseState, PhaseContext context) {
        return false;
    }

    public boolean alreadyCapturingEntitySpawns(IPhaseState state) {
        return false;
    }

    public boolean alreadyCapturingEntityTicks(IPhaseState state) {
        return false;
    }

    public boolean alreadyCapturingTileTicks(IPhaseState state) {
        return false;
    }

    public boolean requiresPost(IPhaseState state) {
        return true;
    }

    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return false;
    }

    public boolean ignoresItemPreMerging(IPhaseState currentState) {
        return false;
    }

    public boolean isWorldGeneration(IPhaseState state) {
        return false;
    }

    public boolean doesCaptureEntityDrops(IPhaseState currentState) {
        return false;
    }

    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {

    }


    public boolean isRestoring(IPhaseState state, PhaseContext context, int updateFlag) {
        return false;
    }

    public void capturePlayerUsingStackToBreakBlock(@Nullable ItemStack itemStack, EntityPlayerMP playerMP, IPhaseState state, PhaseContext context,
            CauseTracker causeTracker) {

    }


    /**
     * Associates any notifiers and owners for tracking as to what caused
     * the next {@link TickPhase.Tick} to enter for a block to be updated.
     * The interesting thing is that since the current state and context
     * are already known, we can associate the notifiers/owners appropriately.
     * This may have the side effect of a long winded "bubble down" from
     * a single lever pull to blocks getting updated hundreds of blocks
     * away.
     *
     * @param causeTracker
     * @param pos
     * @param currentState
     * @param context
     * @param newContext
     */
    public void appendNotifierPreBlockTick(CauseTracker causeTracker, BlockPos pos, IPhaseState currentState, PhaseContext context, PhaseContext newContext) {
        final Chunk chunk = causeTracker.getMinecraftWorld().getChunkFromBlockCoords(pos);
        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        if (chunk != null && !chunk.isEmpty()) {
            mixinChunk.getBlockOwner(pos).ifPresent(newContext::owner);
            mixinChunk.getBlockNotifier(pos).ifPresent(newContext::notifier);
        }
    }

    // Actual capture methods

    /**
     * This is Step 3 of entity spawning. It is used for the sole purpose of capturing an entity spawn
     * and doesn't actually spawn an entity into the world until the current phase is unwound.
     * The method itself should technically capture entity spawns, however, in the event it
     * is required that the entity cannot be captured, returning {@code false} will mark it
     * to spawn into the world, bypassing any of the bulk spawn events or capturing.
     *
     * <p>NOTE: This method should only be called and handled if and only if {@link #allowEntitySpawns(IPhaseState)}
     * returns {@code true}. Violation of this will have unforseen consequences.</p>
     *
     * @param phaseState The current phase state
     * @param context The current context
     * @param entity The entity being captured
     * @param chunkX The chunk x position
     * @param chunkZ The chunk z position
     * @return True if the entity was successfully captured
     */
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer minecraftWorld = (WorldServer) minecraftEntity.worldObj;
        final User user = context.getNotifier().orElseGet(() -> context.getOwner().orElse(null));
        if (user != null) {
            entity.setCreator(user.getUniqueId());
        }
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE,
                entities, (World) minecraftWorld);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            ((IMixinWorldServer) minecraftWorld).forceSpawnEntity(entity);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .toString();
    }

    public Optional<DamageSource> createDestructionDamageSource(IPhaseState state, PhaseContext context, net.minecraft.entity.Entity entity) {
        return Optional.empty();
    }

    public void addNotifierToBlockEvent(IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {

    }

    public boolean populateCauseForNotifyNeighborEvent(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker,
            IMixinChunk mixinChunk, BlockPos pos) {
        final Optional<User> notifier = context.firstNamed(NamedCause.NOTIFIER, User.class);
        if (notifier.isPresent()) {
            builder.named(NamedCause.notifier(notifier.get()));
            return true;
        } else {
            mixinChunk.getBlockNotifier(pos).ifPresent(user -> builder.named(NamedCause.notifier(user)));
            mixinChunk.getBlockOwner(pos).ifPresent(owner -> builder.named(NamedCause.owner(owner)));
        }
        return true;
    }

    public Cause generateTeleportCause(IPhaseState state, PhaseContext context) {
        return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.UNKNOWN).build()));
    }


    public void appendPreBlockProtectedCheck(Cause.Builder builder, IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker) {
        context.getSource(Player.class).ifPresent(player -> builder.named(NamedCause.notifier(player)));
    }

    public boolean isTicking(IPhaseState state) {
        return false;
    }

    public boolean handlesOwnPhaseCompletion(IPhaseState state) {
        return false;
    }

    public boolean requiresDimensionTransferBetweenWorlds(IPhaseState state) {
        return false;
    }

    public void appendContextPreExplosion(PhaseContext phaseContext, PhaseData currentPhaseData) {

    }

    public void appendExplosionCause(PhaseData phaseData) {

    }
}
