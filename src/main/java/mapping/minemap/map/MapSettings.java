package mapping.minemap.map;

import com.google.gson.annotations.Expose;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;

import java.util.*;
import java.util.stream.Collectors;

public class MapSettings {
    public static final boolean DEFAULT_SHOW_BIOMES = true;
    public static final boolean DEFAULT_SHOW_GRID = false;
    public static final int DEFAULT_BIOME_SIZE = OverworldBiomeSource.DEFAULT_BIOME_SIZE;
    public static final int DEFAULT_RIVER_SIZE = OverworldBiomeSource.DEFAULT_RIVER_SIZE;

    private MCVersion version;
    private Dimension dimension;
    @Expose
    public Boolean showBiomes = true;
    @Expose
    public Boolean showGrid = false;
    @Expose
    public Integer biomeSize = 4;
    @Expose
    public Integer riverSize = 4;
    @Expose
    private Map<String, Boolean> biomes;
    private Map<Biome, Boolean> biomeStates = new HashMap<>();
    private boolean isDirty;

    public MapSettings(Dimension dimension) {
        this(MCVersion.values()[0], dimension);
    }

    public MapSettings(MCVersion version, Dimension dimension) {
        this.version = version;
        this.dimension = dimension;

        this.biomes = Biomes.REGISTRY.values().stream()
                .filter(b -> b.getDimension() == this.dimension)
                .map(Biome::getName)
                .collect(Collectors.toMap(e -> e, e -> true));
    }

    public MCVersion getVersion() {
        return this.version;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public MapSettings refresh() {
        this.biomeStates = Biomes.REGISTRY.values().stream()
                .filter(b -> b.getDimension() == this.dimension)
                .filter(b -> b.getVersion().isOlderOrEqualTo(this.version) || b.getDimension() == Dimension.END)
                .collect(Collectors.toMap(
                        e -> e,
                        e -> this.biomes.getOrDefault(e.getName(), true)));

        return this;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public MapSettings setState(Biome biome, boolean state) {
        this.biomeStates.replace(biome, state);
        this.biomes.put(biome.getName(), state);
        return this;
    }

    public final MapSettings hide(Biome... biomes) {
        for (Biome biome : biomes) {
            this.setState(biome, false);
        }

        return this;
    }

    public final MapSettings show(Biome... biomes) {
        for (Biome biome : biomes) {
            this.setState(biome, true);
        }

        return this;
    }

    public List<Biome> getAllBiomes() {
        return this.getAllBiomes(Comparator.comparingInt(Biome::getId));
    }

    public List<Biome> getAllBiomes(Comparator<Biome> comparator) {
        List<Biome> b = new ArrayList<>(this.biomeStates.keySet());
        b.sort(comparator);
        return b;
    }

    public Integer getBiomeSize() {
        return biomeSize;
    }

    public Integer getRiverSize() {
        return riverSize;
    }

    public void setBiomeSize(Integer biomeSize) {
        this.biomeSize = biomeSize;
    }

    public void setRiverSize(Integer riverSize) {
        this.riverSize = riverSize;
    }

    public void maintainConfig(Dimension dimension, MCVersion version) {
        this.dimension = dimension;
        this.version = version;
        List<Biome> biomes = Biomes.REGISTRY.values().stream()
                .filter(b -> b.getDimension() == this.dimension)
                .collect(Collectors.toList());
        for (Biome biome : biomes) {
            this.biomes.putIfAbsent(biome.getName(), true);
        }
        this.showBiomes = this.showBiomes != null ? this.showBiomes : DEFAULT_SHOW_BIOMES;
        this.showGrid = this.showGrid != null ? this.showGrid : DEFAULT_SHOW_GRID;
        this.biomeSize = this.biomeSize != null ? this.biomeSize : DEFAULT_BIOME_SIZE;
        this.riverSize = this.riverSize != null ? this.riverSize : DEFAULT_RIVER_SIZE;
    }

    public MapSettings set(MapSettings other) {
        this.showBiomes = other.showBiomes;
        this.showGrid = other.showGrid;
        this.biomeSize = other.biomeSize;
        this.riverSize = other.riverSize;
        this.getAllBiomes().forEach(this::hide);
        return this;
    }

    public MapSettings copy() {
        return this.copyFor(this.version, this.dimension);
    }

    public MapSettings copyFor(MCVersion version, Dimension dimension) {
        MapSettings copy = new MapSettings(version, dimension);
        copy.showBiomes = this.showBiomes;
        copy.showGrid = this.showGrid;
        copy.biomeSize = this.biomeSize;
        copy.riverSize = this.riverSize;
        copy.biomes = new HashMap<>(this.biomes);
        return copy.refresh();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MapSettings))
            return false;
        MapSettings that = (MapSettings) o;
        return isDirty == that.isDirty && version == that.version && dimension == that.dimension
                && Objects.equals(showBiomes, that.showBiomes)
                && Objects.equals(showGrid, that.showGrid)
                && Objects.equals(biomeSize, that.biomeSize)
                && Objects.equals(riverSize, that.riverSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, dimension, showBiomes, showGrid, biomeSize, riverSize, isDirty);
    }
}
