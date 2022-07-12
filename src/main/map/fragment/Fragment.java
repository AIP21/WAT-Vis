package mapping.minemap.map.fragment;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.layer.BiomeLayer;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcterrain.TerrainGenerator;
import main.MainPanel;
import main.PlayerTrackerDecoder;
import config.mapping.BiomeColorsConfig;
import config.mapping.Configs;
import mapping.minemap.map.MapContext;
import util.objects.DrawInfo;
import util.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Consumer;

public class Fragment {
    private final int blockX;
    private final int blockZ;
    private final int regionSize;
    private final MapContext context;

    private int layerIdCache;
    private int[][] biomeCache;
    private int[][] heightCache;
    private BufferedImage imageCache;
    private int lastCheatingBiome = 1;
    private int lastCheatingHeight = 1;
    private boolean hasBiomeModified = false;
    private boolean hasHeightModified = false;

    private BPos hoveredPos;
    private BPos clickedPos;

    public Fragment(int blockX, int blockZ, int regionSize, MapContext context) {
        this.blockX = blockX;
        this.blockZ = blockZ;
        this.regionSize = regionSize;
        this.context = context;

        if (this.context != null) {
            this.refreshBiomeCache();
        }
    }

    public Fragment(BPos pos, int regionSize, MapContext context) {
        this(pos.getX(), pos.getZ(), regionSize, context);
    }

    public Fragment(RPos pos, MapContext context) {
        this(pos.toBlockPos(), pos.getRegionSize(), context);
    }

    public int getX() {
        return this.blockX;
    }

    public int getZ() {
        return this.blockZ;
    }

    public int getSize() {
        return this.regionSize;
    }

    public MapContext getContext() {
        return this.context;
    }

    public void drawBiomes(Graphics graphics, DrawInfo info) {
        this.refreshBiomeCache();
        if (this.context.getSettings().isDirty()) {
            this.refreshBiomeImageCache();
        }

        if (this.imageCache != null) {
            graphics.drawImage(this.imageCache, info.x, info.y, info.width, info.height, null);
        }
    }

    public void drawHeight(Graphics graphics, DrawInfo info) {
        this.refreshHeightCache();
        if (this.context.getSettings().isDirty()) {
            this.refreshHeightImageCache();
        }

        if (this.imageCache != null && this.context.getSettings().showBiomes) {
            graphics.drawImage(this.imageCache, info.x, info.y, info.width, info.height, null);
        }
    }

    public void drawGrid(Graphics graphics, DrawInfo info) {
        if (this.context.getSettings().showGrid) {
            Color old = graphics.getColor();
            graphics.setColor(Color.BLACK);
            graphics.drawRect(info.x, info.y, info.width, info.height);
            graphics.setColor(old);
        }
    }

    public void drawNonLoading(Consumer<Fragment> action) {
        if (this.context != null) {
            action.accept(this);
        }
    }

    public void onHovered(int blockX, int blockZ) {
        this.hoveredPos = new BPos(blockX, 0, blockZ);
    }

    public void onClicked(int blockX, int blockZ) {
        this.clickedPos = new BPos(blockX, 0, blockZ);
    }

    private void refreshHeightCache() {
        MainPanel panel = PlayerTrackerDecoder.INSTANCE.mainPanel;
        int cheating;
        if (panel != null && panel != null) {
            cheating = Math.max(2, (int) (panel.blocksPerFragment / 2 / panel.pixelsPerFragment));
            cheating = Math.max(cheating, 1);
            if (this.heightCache != null && lastCheatingHeight <= cheating) return;
        } else {
            cheating = 2;
        }
        lastCheatingHeight = cheating;
        BiomeLayer layer = this.context.getBiomeLayer();
        int effectiveRegion = Math.max(Math.max(this.regionSize / layer.getScale(), 1) / cheating, 1);
        TerrainGenerator terrainGenerator = this.context.getTerrainGenerator();

        if (this.heightCache == null || this.heightCache.length != effectiveRegion) {
            this.heightCache = new int[effectiveRegion][effectiveRegion];
        }
        for (int x = 0; x < effectiveRegion; x++) {
            for (int z = 0; z < effectiveRegion; z++) {
                this.heightCache[x][z] = terrainGenerator == null ? -1 : terrainGenerator.getHeightOnGround(this.blockX + x * layer.getScale() * cheating, this.blockZ + z * layer.getScale() * cheating);
            }
        }
        hasHeightModified = true;
        this.refreshHeightImageCache();
    }

    private void refreshHeightImageCache() {
        if (this.imageCache != null && !hasHeightModified) return;
        hasHeightModified = false;
        int scaledSize = this.heightCache.length;
        this.imageCache = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_RGB);
        TerrainGenerator generator = this.context.getTerrainGenerator();
        int minGen = generator == null ? 0 : generator.getMinWorldHeight();
        int maxGen = generator == null ? 0 : generator.getMaxWorldHeight();
        Color minColor = Color.WHITE;
        Color maxColor = Color.BLACK;
        Color defaultColor = Color.orange;
        for (int x = 0; x < scaledSize; x++) {
            for (int z = 0; z < scaledSize; z++) {
                Color color = get2DGradientColor(this.heightCache[x][z], minGen, maxGen, minColor, maxColor, defaultColor);
                this.imageCache.setRGB(x, z, color.getRGB());
            }
        }
    }

    private void refreshBiomeCache() {
        MainPanel panel = PlayerTrackerDecoder.INSTANCE.mainPanel;
        int cheating;
        if (panel != null && panel != null) {
            cheating = Math.max(1, (int) (panel.blocksPerFragment / 16 / panel.pixelsPerFragment));
            if (this.biomeCache != null && this.layerIdCache == this.context.getLayerId() && lastCheatingBiome <= cheating)
                return;
        } else {
            cheating = 1;
        }
        lastCheatingBiome = cheating;
        this.layerIdCache = this.context.getLayerId();
        BiomeLayer layer = this.context.getBiomeLayer();
        int effectiveRegion = Math.max(Math.max(this.regionSize / layer.getScale(), 1) / cheating, 1);
        RPos region = new BPos(this.blockX, 0, this.blockZ).toRegionPos(layer.getScale());

        if (this.biomeCache == null || this.biomeCache.length != effectiveRegion) {
            this.biomeCache = new int[effectiveRegion][effectiveRegion];
        }

        // if (layer instanceof IntBiomeLayer){
        // int[] biomes=((IntBiomeLayer) layer).sample(region.getX() ,0,
        // region.getZ(),effectiveRegion,1,effectiveRegion);
        // for (int x = 0; x < effectiveRegion; x++) {
        // this.biomeCache[x]=Arrays.copyOfRange(biomes,x*effectiveRegion,(x+1)*effectiveRegion);
        // }
        // }else{
        // for (int x = 0; x < effectiveRegion; x++) {
        // for (int z = 0; z < effectiveRegion; z++) {
        // this.biomeCache[x][z]=layer.getBiome(region.getX() + x, 0, region.getZ() +
        // z);
        // }
        // }
        // }
        for (int x = 0; x < effectiveRegion; x++) {
            for (int z = 0; z < effectiveRegion; z++) {
                this.biomeCache[x][z] = layer.getBiome(region.getX() + x * cheating, 0, region.getZ() + z * cheating);
            }
        }
        hasBiomeModified = true;
        this.refreshBiomeImageCache();
    }

    private void refreshBiomeImageCache() {
        if (this.imageCache != null && !hasBiomeModified)
            return;
        hasBiomeModified = false;
        int scaledSize = this.biomeCache.length;
        this.imageCache = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < scaledSize; x++) {
            for (int z = 0; z < scaledSize; z++) {
                Biome biome = Biomes.REGISTRY.get(this.biomeCache[x][z]);
                if (biome == null) continue;
                Color color = Configs.BIOME_COLORS.get(BiomeColorsConfig.DEFAULT_STYLE_NAME, biome).brighter();

                this.imageCache.setRGB(x, z, color.getRGB());
            }
        }
    }

    public static Color get2DGradientColor(int value, int min, int max, Color from, Color to, Color outside) {
        // we can not work with such bad decisions
        if (min > max) {
            throw new IllegalArgumentException("Min should be less than max");
        }
        if (value < min || value > max) {
            return outside;
        }
        // if max==min==value then ratio=0/1=0
        double ratio = (double) (value - min) / (double) Math.min(max - min, 1) / 100.0D;
        int red = (int) Utils.smartClamp(to.getRed() * ratio + from.getRed() * (1 - ratio), from.getRed(), to.getRed());
        int green = (int) Utils.smartClamp(to.getGreen() * ratio + from.getGreen() * (1 - ratio), from.getGreen(), to.getGreen());
        int blue = (int) Utils.smartClamp(to.getBlue() * ratio + from.getBlue() * (1 - ratio), from.getBlue(), to.getBlue());
        int alpha = (int) Utils.smartClamp(to.getAlpha() * ratio + from.getAlpha() * (1 - ratio), from.getAlpha(), to.getAlpha());
        return new Color(red, green, blue, alpha);
    }

    public boolean isPosInFragment(BPos pos) {
        return this.isPosInFragment(pos.getX(), pos.getZ());
    }

    public boolean isPosInFragment(int blockX, int blockZ) {
        if (blockX < this.getX() || blockX >= this.getX() + this.getSize()) return false;
        return blockZ >= this.getZ() && blockZ < this.getZ() + this.getSize();
    }

    public Rectangle getRectangle() {
        return new Rectangle(blockX, blockZ, regionSize, regionSize);
    }

    @Override
    public String toString() {
        return "Fragment{" + "blockX=" + blockX + ", blockZ=" + blockZ + ", regionSize=" + regionSize + ", context=" + context + ", layerIdCache=" + layerIdCache + ", biomeCache=" + Arrays.toString(biomeCache) + ", imageCache=" + imageCache + ", hoveredPos=" + hoveredPos + '}';
    }
}
