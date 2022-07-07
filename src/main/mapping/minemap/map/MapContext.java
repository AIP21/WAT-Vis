package src.main.mapping.minemap.map;

import com.seedfinding.mcbiome.layer.BiomeLayer;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcbiome.source.LayeredBiomeSource;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mccore.version.UnsupportedVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import src.main.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;

public class MapContext {
    public final MCVersion version;
    public final Dimension dimension;
    public final long worldSeed;

    private final MapSettings settings;

    private final ThreadLocal<Map<Dimension, LayeredBiomeSource<? extends BiomeLayer>>> biomeSource;
    private final ThreadLocal<Map<Dimension, TerrainGenerator>> chunkGenerators;
    private CPos[] starts = null;

    private int layerId;

    public MapContext(long worldSeed, MapSettings settings) {
        this.version = settings.getVersion();
        this.dimension = settings.getDimension();
        this.worldSeed = worldSeed;
        this.settings = settings;

        this.biomeSource = ThreadLocal.withInitial(() -> {
            Map<Dimension, LayeredBiomeSource<?>> map = new HashMap<>();
            for (Dimension dim : Dimension.values()) {
                try {

                    LayeredBiomeSource<? extends BiomeLayer> biomeSource;

                    if (dim == Dimension.OVERWORLD) {
                        biomeSource = new OverworldBiomeSource(this.version, this.worldSeed, settings.getBiomeSize(), settings.getRiverSize());
                    } else {
                        biomeSource = (LayeredBiomeSource<? extends BiomeLayer>) BiomeSource.of(dim, this.version, worldSeed);
                    }

                    map.put(dim, biomeSource);
                } catch (UnsupportedVersion e) {
                    Logger.info(String.format("Biome source for the %s for version %s could not be initialized%n", dim.getName(), this.version.toString()));
                    throw e;
                }
            }
            return map;
        });

        this.chunkGenerators = ThreadLocal.withInitial(() -> {
            Map<Dimension, TerrainGenerator> map = new HashMap<>();
            for (Dimension dim : Dimension.values()) {
                try {
                    TerrainGenerator chunkGenerator = TerrainGenerator.of(dim, this.biomeSource.get().get(dim));
                    map.put(dim, chunkGenerator);
                } catch (UnsupportedVersion e) {
                    System.err.printf("Chunk generator for the %s for version %s could not be initialized%n", dim.getName(), this.version.toString());
                    map.put(dim, null);
                }
            }
            return map;
        });

        this.layerId = this.getBiomeSource().getLayerCount() - 2;
    }

    public MapContext(MCVersion version, Dimension dimension, long worldSeed) {
        this(worldSeed, new MapSettings(version, dimension));
    }

    public CPos[] getStarts() {
        return this.starts;
    }

    public MapSettings getSettings() {
        return this.settings;
    }

    public int getLayerId() {
        return this.layerId;
    }

    public MapContext setLayerId(int layerId) {
        this.layerId = layerId;
        return this;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public MCVersion getVersion() {
        return version;
    }

    public ThreadLocal<Map<Dimension, TerrainGenerator>> getTerrainGenerators() {
        return chunkGenerators;
    }

    public TerrainGenerator getTerrainGenerator() {
        return this.getTerrainGenerator(this.dimension);
    }

    public TerrainGenerator getTerrainGenerator(Dimension dimension) {
        return this.chunkGenerators.get().get(dimension);
    }

    public LayeredBiomeSource<? extends BiomeLayer> getBiomeSource() {
        return this.getBiomeSource(this.dimension);
    }

    public LayeredBiomeSource<? extends BiomeLayer> getBiomeSource(Dimension dimension) {
        return this.biomeSource.get().get(dimension);
    }

    @SuppressWarnings("unchecked")
    public <T extends BiomeLayer> T getBiomeLayer() {
        return (T) this.getBiomeSource().getLayer(this.layerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapContext)) return false;
        MapContext that = (MapContext) o;
        return worldSeed == that.worldSeed && layerId == that.layerId && version == that.version && dimension == that.dimension && settings.equals(that.settings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, dimension, worldSeed, settings, layerId);
    }
}
