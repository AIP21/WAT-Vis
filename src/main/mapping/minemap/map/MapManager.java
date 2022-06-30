//package src.main.mapping.minemap.map;
//
//import com.seedfinding.mccore.util.math.Vec3i;
//import com.seedfinding.mccore.util.pos.BPos;
//import src.main.MainPanel;
//import src.main.PlayerTrackerDecoder;
//import java.awt.*;
//
//public class MapManager {
//    public static final int DEFAULT_REGION_SIZE = 512;// 8192;
//    public static final double DEFAULT_PIXELS_PER_FRAGMENT = 256.0;
//    public int blocksPerFragment;
//    private final MainPanel panel;
//    public double pixelsPerFragment;
//
//    public Point mousePointer;
//
//    public MapManager(MainPanel panel) {
//        this(panel, DEFAULT_REGION_SIZE);
//    }
//
//    public MapManager(MainPanel panel, int blocksPerFragment) {
//        this.panel = panel;
//        this.blocksPerFragment = blocksPerFragment;
//        this.pixelsPerFragment = (int) (DEFAULT_PIXELS_PER_FRAGMENT * (this.blocksPerFragment / DEFAULT_REGION_SIZE));
//
////        this.panel.addMouseMotionListener(Events.Mouse.onDragged(e -> {
////            if (this.mousePointer == null)
////                return;
////            if (SwingUtilities.isLeftMouseButton(e)) {
////                int dx = e.getX() - this.mousePointer.x;
////                int dy = e.getY() - this.mousePointer.y;
////                this.mousePointer = e.getPoint();
////                this.panel.curX += dx;
////                this.panel.curY += dy;
////                this.panel.repaint();
////            }
////        }));
////
////        this.panel.addMouseMotionListener(Events.Mouse.onMoved(e -> {
////            this.mousePointer = e.getPoint();
////            BPos pos = this.getMouseBPos();
////            int x = pos.getX();
////            int z = pos.getZ();
////            this.panel.scheduler.forEachFragment(fragment -> fragment.onHovered(x, z));
////
////            SwingUtilities.invokeLater(this.panel::repaint);
////
////        }));
////
////        this.panel.addMouseListener(Events.Mouse.onPressed(e -> {
////            if (SwingUtilities.isLeftMouseButton(e)) {
////                this.mousePointer = e.getPoint();
////                BPos pos = this.getMouseBPos();
////                this.panel.scheduler.forEachFragment(fragment -> fragment.onClicked(pos.getX(), pos.getZ()));
////            }
////        }));
////
////        this.panel.addMouseWheelListener(e -> {
////            boolean zoomOut = e.getUnitsToScroll() < 0;
////            zoom(zoomOut).run();
////        });
//    }
//
//    @SuppressWarnings("unused")
//    public Point getMousePointer() {
//        return mousePointer;
//    }
//
//    public static Runnable zoom(boolean zoomOut) {
//        return () -> {
//            if (PlayerTrackerDecoder.INSTANCE == null)
//                return;
//            if (PlayerTrackerDecoder.INSTANCE.mainPanel == null)
//                return;
//            if (PlayerTrackerDecoder.INSTANCE.mainPanel.manager == null)
//                return;
//
//            MapManager manager = PlayerTrackerDecoder.INSTANCE.mainPanel.manager;
//
//            double newPixelsPerFragment = manager.pixelsPerFragment;
//
//            if (zoomOut) {
//                newPixelsPerFragment /= 2.0D;
//            } else {
//                newPixelsPerFragment *= 2.0D;
//            }
//
//            if (newPixelsPerFragment > 4096.0D * (double) manager.blocksPerFragment / DEFAULT_REGION_SIZE) {
//                // restrict min zoom to 4096 chunks per fragment
//                newPixelsPerFragment = 4096.0D * (manager.blocksPerFragment / 512.0D);
//            } else if (newPixelsPerFragment < 32.0D * (double) manager.blocksPerFragment / DEFAULT_REGION_SIZE) {
//                // restrict max zoom to 32 chunks per fragment
//                newPixelsPerFragment = 32.0D * (manager.blocksPerFragment / 512.0D);
//            }
//
//            double scaleFactor = newPixelsPerFragment / manager.pixelsPerFragment;
//            manager.panel.curX *= scaleFactor;
//            manager.panel.curY *= scaleFactor;
//            manager.pixelsPerFragment = newPixelsPerFragment;
//            manager.panel.repaint();
//        };
//    }
//
//    public Vec3i getScreenSize() {
//        return new Vec3i(this.panel.getWidth(), 0, this.panel.getHeight());
//    }
//
//    public BPos getCenterPos() {
//        Vec3i screenSize = this.getScreenSize();
//        return getPos(screenSize.getX() / 2.0D, screenSize.getZ() / 2.0D);
//    }
//
//    public BPos getPos(double mouseX, double mouseY) {
//        Vec3i screenSize = this.getScreenSize();
//        double x = (mouseX - screenSize.getX() / 2.0D - panel.curX) / screenSize.getX();
//        double y = (mouseY - screenSize.getZ() / 2.0D - panel.curY) / screenSize.getZ();
//        double blocksPerWidth = (screenSize.getX() / this.pixelsPerFragment) * (double) this.blocksPerFragment;
//        double blocksPerHeight = (screenSize.getZ() / this.pixelsPerFragment) * (double) this.blocksPerFragment;
//        x *= blocksPerWidth;
//        y *= blocksPerHeight;
//        int xi = (int) Math.round(x);
//        int yi = (int) Math.round(y);
//        return new BPos(xi, 0, yi);
//    }
//}