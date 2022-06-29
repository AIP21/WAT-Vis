package src.main.mapping.minemap.map;

import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.listener.Events;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.function.Function;

public class MapManager {

    public static final int DEFAULT_REGION_SIZE = 512;// 8192;
    public static final double DEFAULT_PIXELS_PER_FRAGMENT = 256.0;
    public int blocksPerFragment;
    private final MapPanel panel;
    public double pixelsPerFragment;
    public double centerX;
    public double centerY;

    public Point mousePointer;

    public MapManager(MapPanel panel) {
        this(panel, DEFAULT_REGION_SIZE);
    }

    public MapManager(MapPanel panel, int blocksPerFragment) {
        this.panel = panel;
        this.blocksPerFragment = blocksPerFragment;
        this.pixelsPerFragment = (int) (DEFAULT_PIXELS_PER_FRAGMENT * (this.blocksPerFragment / DEFAULT_REGION_SIZE));

        this.panel.addMouseMotionListener(Events.Mouse.onDragged(e -> {
            if (this.mousePointer == null)
                return;
            if (SwingUtilities.isLeftMouseButton(e)) {
                int dx = e.getX() - this.mousePointer.x;
                int dy = e.getY() - this.mousePointer.y;
                this.mousePointer = e.getPoint();
                this.centerX += dx;
                this.centerY += dy;
                this.panel.repaint();
            }
        }));

        this.panel.addMouseMotionListener(Events.Mouse.onMoved(e -> {
            this.mousePointer = e.getPoint();
            BPos pos = this.getMouseBPos();
            int x = pos.getX();
            int z = pos.getZ();
            this.panel.scheduler.forEachFragment(fragment -> fragment.onHovered(x, z));

            SwingUtilities.invokeLater(() -> {
                this.panel.repaint();
            });

        }));

        this.panel.addMouseListener(Events.Mouse.onPressed(e -> {
            if (SwingUtilities.isLeftMouseButton(e)) {
                this.mousePointer = e.getPoint();
                BPos pos = this.getMouseBPos();
                this.panel.scheduler.forEachFragment(fragment -> fragment.onClicked(pos.getX(), pos.getZ()));
            }
        }));

        this.panel.addMouseWheelListener(e -> {
            boolean isModifier = Configs.USER_PROFILE.getUserSettings().modifierDown.getModifier().apply(e);
            boolean zoomIn = e.getUnitsToScroll() > 0;
            zoom(zoomIn, isModifier).run();
        });
    }

    @SuppressWarnings("unused")
    public Point getMousePointer() {
        return mousePointer;
    }

    public BPos getMouseBPos() {
        return this.getPos(mousePointer.x, mousePointer.y);
    }

    public static Runnable zoom(boolean zoomOut, boolean isModifier) {
        return () -> {
            if (MineMap.INSTANCE == null)
                return;
            if (MineMap.INSTANCE.mapPanel == null)
                return;
            if (MineMap.INSTANCE.mapPanel.manager == null)
                return;
            MapManager manager = MineMap.INSTANCE.mapPanel.manager;
            if (!isModifier) {
                double newPixelsPerFragment = manager.pixelsPerFragment;

                if (zoomOut) {
                    newPixelsPerFragment /= 2.0D;
                } else {
                    newPixelsPerFragment *= 2.0D;
                }

                if (newPixelsPerFragment > 4096.0D * (double) manager.blocksPerFragment / DEFAULT_REGION_SIZE) {
                    // restrict min zoom to 4096 chunks per fragment
                    newPixelsPerFragment = 4096.0D * (manager.blocksPerFragment / 512.0D);
                } else if (Configs.USER_PROFILE.getUserSettings().restrictMaximumZoom
                        && newPixelsPerFragment < 32.0D * (double) manager.blocksPerFragment / DEFAULT_REGION_SIZE) {
                    // restrict max zoom to 32 chunks per fragment
                    newPixelsPerFragment = 32.0D * (manager.blocksPerFragment / 512.0D);
                }

                double scaleFactor = newPixelsPerFragment / manager.pixelsPerFragment;
                manager.centerX *= scaleFactor;
                manager.centerY *= scaleFactor;
                manager.pixelsPerFragment = newPixelsPerFragment;
                manager.panel.repaint();
            } else {
                int layerId = manager.panel.getContext().getLayerId();
                layerId += zoomOut ? -1 : 1;
                layerId = Mth.clamp(layerId, 0, manager.panel.getContext().getBiomeSource().getLayerCount() - 1);

                if (manager.panel.getContext().getLayerId() != layerId) {
                    manager.panel.getContext().setLayerId(layerId);
                    manager.panel.restart();
                }
            }
        };
    }

    public Vec3i getScreenSize() {
        return new Vec3i(this.panel.getWidth(), 0, this.panel.getHeight());
    }

    public BPos getCenterPos() {
        Vec3i screenSize = this.getScreenSize();
        return getPos(screenSize.getX() / 2.0D, screenSize.getZ() / 2.0D);
    }

    public void setCenterPos(int blockX, int blockZ) {
        double scaleFactor = this.pixelsPerFragment / this.blocksPerFragment;
        this.centerX = -blockX * scaleFactor;
        this.centerY = -blockZ * scaleFactor;
        this.panel.repaint();
    }

    public BPos getPos(double mouseX, double mouseY) {
        Vec3i screenSize = this.getScreenSize();
        double x = (mouseX - screenSize.getX() / 2.0D - centerX) / screenSize.getX();
        double y = (mouseY - screenSize.getZ() / 2.0D - centerY) / screenSize.getZ();
        double blocksPerWidth = (screenSize.getX() / this.pixelsPerFragment) * (double) this.blocksPerFragment;
        double blocksPerHeight = (screenSize.getZ() / this.pixelsPerFragment) * (double) this.blocksPerFragment;
        x *= blocksPerWidth;
        y *= blocksPerHeight;
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        return new BPos(xi, 0, yi);
    }

    public MapPanel getPanel() {
        return panel;
    }

    public enum ModifierDown {
        CTRL_DOWN(InputEvent::isControlDown),
        ALT_DOWN(InputEvent::isAltDown),
        META_DOWN(InputEvent::isMetaDown),
        SHIFT_DOWN(InputEvent::isShiftDown),
        ALT_GR_DOWN(InputEvent::isAltGraphDown),

        ;

        private final Function<InputEvent, Boolean> modifier;

        ModifierDown(Function<InputEvent, Boolean> modifier) {
            this.modifier = modifier;
        }

        public Function<InputEvent, Boolean> getModifier() {
            return modifier;
        }
    }

}