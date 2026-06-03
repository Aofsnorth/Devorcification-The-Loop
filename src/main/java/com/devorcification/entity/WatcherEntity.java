package com.devorcification.entity;

import com.devorcification.Devorcification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class WatcherEntity extends LivingEntity implements GeoEntity {
    public enum State {
        NULL(0),
        PERIPHERAL(1),
        MIRROR(2),
        DOPPELGANGER(3),
        HUNT(4);

        public final int id;
        State(int id) { this.id = id; }

        public static State fromId(int id) {
            for (State s : values()) {
                if (s.id == id) return s;
            }
            return NULL;
        }
    }

    public static final TrackedData<Integer> STATE_ID =
        DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<String> TARGET_UUID =
        DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> DESPAWN_TIMER =
        DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> VISIBLE_LOOK_TICKS =
        DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private int lifetimeTicks = 0;
    private int maxLifetimeTicks = 200;

    public WatcherEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.noGravity = false;
        this.setNoGravity(false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE_ID, State.NULL.id);
        this.dataTracker.startTracking(TARGET_UUID, "");
        this.dataTracker.startTracking(DESPAWN_TIMER, 0);
        this.dataTracker.startTracking(VISIBLE_LOOK_TICKS, 0);
    }

    public State getState() {
        return State.fromId(this.dataTracker.get(STATE_ID));
    }

    public void setState(State state) {
        this.dataTracker.set(STATE_ID, state.id);
        applyStatePhysics(state);
    }

    public void setTargetPlayer(UUID id) {
        this.dataTracker.set(TARGET_UUID, id == null ? "" : id.toString());
    }

    public UUID getTargetPlayer() {
        String s = this.dataTracker.get(TARGET_UUID);
        if (s == null || s.isEmpty()) return null;
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    public void setMaxLifetime(int ticks) {
        this.maxLifetimeTicks = Math.max(20, ticks);
    }

    public int getLifetime() { return lifetimeTicks; }
    public int getMaxLifetime() { return maxLifetimeTicks; }

    private void applyStatePhysics(State state) {
        switch (state) {
            case NULL -> {
                this.setInvulnerable(true);
                this.setInvisible(true);
                this.setNoGravity(true);
            }
            case PERIPHERAL -> {
                this.setInvulnerable(true);
                this.setInvisible(false);
                this.setNoGravity(false);
            }
            case MIRROR -> {
                this.setInvulnerable(true);
                this.setInvisible(false);
                this.setNoGravity(true);
            }
            case DOPPELGANGER -> {
                this.setInvulnerable(true);
                this.setInvisible(false);
                this.setNoGravity(false);
            }
            case HUNT -> {
                this.setInvulnerable(true);
                this.setInvisible(false);
                this.setNoGravity(false);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) return;
        lifetimeTicks++;
        if (lifetimeTicks > maxLifetimeTicks) {
            this.discard();
            return;
        }
        State state = getState();
        ServerPlayerEntity target = resolveTarget();
        if (target == null && state != State.NULL) {
            this.discard();
            return;
        }
        switch (state) {
            case NULL -> tickNull(target);
            case PERIPHERAL -> tickPeripheral(target);
            case MIRROR -> tickMirror(target);
            case DOPPELGANGER -> tickDoppelganger(target);
            case HUNT -> tickHunt(target);
        }
    }

    private void tickNull(ServerPlayerEntity target) {
        if (target != null) {
            double dist = target.distanceTo(this);
            if (dist < 16.0) {
                setState(State.PERIPHERAL);
            }
        }
    }

    private void tickPeripheral(ServerPlayerEntity target) {
        if (target == null) return;
        double yawDiff = Math.abs(angleDelta(target.getYaw(), directionToYaw(target)));
        if (yawDiff < 45.0) {
            int visibleTicks = this.dataTracker.get(VISIBLE_LOOK_TICKS) + 1;
            this.dataTracker.set(VISIBLE_LOOK_TICKS, visibleTicks);
            if (visibleTicks >= 1) {
                Devorcification.LOGGER.info("[Devorcification] Watcher despawned (peripheral seen at yaw diff {})", String.format("%.1f", yawDiff));
                this.discard();
            }
        } else {
            this.dataTracker.set(VISIBLE_LOOK_TICKS, 0);
        }
    }

    private void tickMirror(ServerPlayerEntity target) {
        if (target == null) return;
        if (target.distanceTo(this) > 24.0) this.discard();
    }

    private void tickDoppelganger(ServerPlayerEntity target) {
        if (target == null) return;
        if (target.distanceTo(this) > 16.0) this.discard();
    }

    private void tickHunt(ServerPlayerEntity target) {
        if (target == null) return;
        Vec3d targetPos = target.getPos().add(0, 0, 0);
        Vec3d selfPos = this.getPos();
        Vec3d dir = targetPos.subtract(selfPos).normalize().multiply(0.35);
        this.setVelocity(dir.x, this.getVelocity().y, dir.z);
        this.velocityModified = true;
        double yawDiff = Math.abs(angleDelta(target.getYaw(), directionToYaw(target)));
        if (yawDiff < 25.0) {
            int visibleTicks = this.dataTracker.get(VISIBLE_LOOK_TICKS) + 1;
            this.dataTracker.set(VISIBLE_LOOK_TICKS, visibleTicks);
            if (visibleTicks >= 60) {
                this.discard();
            }
        } else {
            this.dataTracker.set(VISIBLE_LOOK_TICKS, 0);
        }
        if (target.distanceTo(this) > 64.0) this.discard();
    }

    private ServerPlayerEntity resolveTarget() {
        UUID id = getTargetPlayer();
        if (id == null) return null;
        if (this.getWorld() instanceof ServerWorld sw) {
            return sw.getServer().getPlayerManager().getPlayer(id);
        }
        return null;
    }

    private double directionToYaw(Entity other) {
        double dx = other.getX() - this.getX();
        double dz = other.getZ() - this.getZ();
        return (Math.toDegrees(Math.atan2(-dx, dz)) + 360.0) % 360.0;
    }

    private double angleDelta(double yawA, double yawB) {
        double diff = ((yawA - yawB + 540.0) % 360.0) - 180.0;
        return diff;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("state")) setState(State.fromId(nbt.getInt("state")));
        if (nbt.contains("target")) {
            try { setTargetPlayer(UUID.fromString(nbt.getString("target"))); } catch (Exception ignored) {}
        }
        if (nbt.contains("max_life")) this.maxLifetimeTicks = nbt.getInt("max_life");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("state", getState().id);
        UUID id = getTargetPlayer();
        if (id != null) nbt.putString("target", id.toString());
        nbt.putInt("max_life", this.maxLifetimeTicks);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    public BlockPos getOrigin() {
        return this.getBlockPos();
    }
}
