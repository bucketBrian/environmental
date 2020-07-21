package com.team_abnormals.environmental.common.slabfish.condition;

import com.team_abnormals.environmental.common.entity.SlabfishEntity;
import com.team_abnormals.environmental.common.slabfish.SlabfishManager;
import com.team_abnormals.environmental.common.slabfish.SlabfishType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>A context used for determining what kinds of slabfish can be spawned.</p>
 *
 * @author Ocelot
 */
public class SlabfishConditionContext
{
    private final boolean struckByLightning;
    private final LazyValue<Random> random;
    private final LazyValue<String> name;
    private final LazyValue<BlockPos> pos;
    private final LazyValue<Biome> biome;
    private final LazyValue<Boolean> raid;
    private final LazyValue<Integer> light;
    private final Map<LightType, LazyValue<Integer>> lightTypes;
    private final LazyValue<ResourceLocation> dimension;
    private final LazyValue<ResourceLocation> slabfishType;
    private final LazyValue<Boolean> breederInsomnia;
    private final Pair<SlabfishType, SlabfishType> parents;

    private SlabfishConditionContext(SlabfishEntity slabfish, boolean struckByLightning, @Nullable ServerPlayerEntity breeder, @Nullable SlabfishEntity parent1, @Nullable SlabfishEntity parent2)
    {
        ServerWorld world = (ServerWorld) slabfish.getEntityWorld();
        this.struckByLightning = struckByLightning;
        this.random = new LazyValue<>(world::getRandom);
        this.name = new LazyValue<>(() -> slabfish.getDisplayName().getString().trim());
        this.pos = new LazyValue<>(() -> new BlockPos(slabfish.getPositionVec()));
        this.biome = new LazyValue<>(() -> world.getBiome(this.pos.getValue()));
        this.raid = new LazyValue<>(() -> world.findRaid(this.pos.getValue()) != null);
        this.light = new LazyValue<>(() -> world.getLight(this.pos.getValue()));
        this.lightTypes = new HashMap<>();
        for (LightType lightType : LightType.values())
            this.lightTypes.put(lightType, new LazyValue<>(() -> world.getLightFor(lightType, this.pos.getValue())));
        this.dimension = new LazyValue<>(() -> world.func_234923_W_().func_240901_a_());
        this.slabfishType = new LazyValue<>(slabfish::getSlabfishType);
        this.breederInsomnia = new LazyValue<>(() -> breeder != null && breeder.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= 72000 && world.isNightTime());
        this.parents = parent1 != null && parent2 != null ? new ImmutablePair<>(SlabfishManager.DEFAULT_SLABFISH, SlabfishManager.DEFAULT_SLABFISH) : null;// TODO get type from parents
    }

    /**
     * @return Whether or not the slabfish was struck by lightning
     */
    public boolean isStruckByLightning()
    {
        return struckByLightning;
    }

    /**
     * @return The slabfish world random number generator
     */
    public Random getRandom()
    {
        return this.random.getValue();
    }

    /**
     * @return The name of the slabfish
     */
    public String getName()
    {
        return this.name.getValue();
    }

    /**
     * @return The position of the slabfish
     */
    public BlockPos getPos()
    {
        return this.pos.getValue();
    }

    /**
     * @return The biome the slabfish is in
     */
    public Biome getBiome()
    {
        return this.biome.getValue();
    }

    /**
     * @return Whether or not a raid is currently ongoing
     */
    public boolean isInRaid()
    {
        return this.raid.getValue();
    }

    /**
     * @return The light value at the slabfish position
     */
    public int getLight()
    {
        return this.light.getValue();
    }

    /**
     * Fetches light for the specified type of light
     *
     * @param lightType The type of light to get
     * @return The sky light value at the slabfish position
     */
    public int getLightFor(LightType lightType)
    {
        return this.lightTypes.get(lightType).getValue();
    }

    /**
     * @return The dimension the slabfish is in
     */
    public ResourceLocation getDimension()
    {
        return this.dimension.getValue();
    }

    /**
     * @return The type of slabfish this slabfish was before trying to undergo a change
     */
    public ResourceLocation getSlabfishType()
    {
        return this.slabfishType.getValue();
    }

    /**
     * @return Whether or not the player that bred the two slabfish together has insomnia
     */
    public boolean isBreederInsomnia()
    {
        return this.breederInsomnia.getValue();
    }

    /**
     * @return The types of slabfish the parents were or null if there are no parents
     */
    @Nullable
    public Pair<SlabfishType, SlabfishType> getParentTypes()
    {
        return this.parents;
    }

    /**
     * Fetches a new context for the specified entity.
     *
     * @param slabfish The entity to focus on
     * @return A new context with that slabfish as the focus
     */
    public static SlabfishConditionContext of(SlabfishEntity slabfish)
    {
        return new SlabfishConditionContext(slabfish, false, null, null, null);
    }

    /**
     * Fetches a new context for the specified entity when struck by lightning.
     *
     * @param slabfish The entity to focus on
     * @return A new context with that slabfish as the focus
     */
    public static SlabfishConditionContext lightning(SlabfishEntity slabfish)
    {
        return new SlabfishConditionContext(slabfish, true, null, null, null);
    }

    /**
     * Fetches a new context for the specified entity with the two parents.
     *
     * @param slabfish The entity to focus on
     * @param breeder  The player that bred the two parents together
     * @param parent1  The first parent of breeding with
     * @param parent2  The second parent of breeding
     * @return A new context with that slabfish as the focus
     */
    public static SlabfishConditionContext breeding(SlabfishEntity slabfish, @Nullable ServerPlayerEntity breeder, SlabfishEntity parent1, SlabfishEntity parent2)
    {
        return new SlabfishConditionContext(slabfish, false, breeder, parent1, parent2);
    }
}
