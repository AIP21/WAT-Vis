package mapping.minemap.map.fragment;

import com.seedfinding.mccore.util.data.ThreadPool;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import main.MainPanel;
import util.objects.DrawInfo;
import util.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FragmentScheduler {

    protected final Map<RPos, Fragment> fragments = new ConcurrentHashMap<>();
    private final AtomicBoolean scheduledModified = new AtomicBoolean(false);
    public static Fragment LOADING_FRAGMENT = new Fragment(0, 0, 0, null) {
        @Override
        public void drawBiomes(Graphics graphics, DrawInfo info) {
        }
    };
    public List<RPos> scheduledRegions = Collections.synchronizedList(new ArrayList<>());
    protected ThreadPool executor;
    protected MainPanel listener;

    public FragmentScheduler(MainPanel listener, int threadCount) {
        this.listener = listener;
        this.executor = new ThreadPool(threadCount + 1);

        this.executor.run(() -> {
            while (!this.executor.getExecutor().isShutdown()) {
                RPos nearest = this.getNearestScheduled();

                if (nearest == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Logger.err("Error sleeping fragment scheduler thread:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                    }
                    continue;
                } else if (!this.isInBounds(nearest)) {
                    this.fragments.remove(nearest);
                    this.scheduledRegions.remove(nearest);
                    continue;
                }

                this.scheduledRegions.remove(nearest);

                try {
                    this.executor.run(() -> {
                        Fragment fragment = new Fragment(nearest, this.listener.getContext());
                        this.fragments.put(nearest, fragment);
                        SwingUtilities.invokeLater(() -> this.listener.repaint());
                    });
                } catch (RejectedExecutionException ignored) {

                }

                this.executor.awaitFreeThread();
            }
        });
    }

    public void forEachFragment(Consumer<Fragment> consumer) {
        this.fragments.values().forEach(consumer);
    }

    public void terminate() {
        this.executor.shutdown();
    }

    public void purge() {
        this.scheduledRegions.removeIf(region -> !this.isInBounds(region));
        this.fragments.entrySet().removeIf(e -> !this.isInBounds(e.getKey()));
    }

    public RPos getNearestScheduled() {
        if (this.scheduledModified.getAndSet(false)) {
            SwingUtilities.invokeLater(() -> this.scheduledRegions.sort(Comparator.comparingDouble(this::distanceToCenter)));
        }

        if (!this.scheduledRegions.isEmpty()) {
            return this.scheduledRegions.get(0);
        }

        return null;
    }

    public double distanceToCenter(RPos regionPos) {
        return regionPos.distanceTo(this.listener.getCenterPos().toRegionPos(this.listener.blocksPerFragment), DistanceMetric.EUCLIDEAN_SQ);
    }

    public boolean isInBounds(RPos region) {
        BPos min = this.listener.getPos(0, 0);
        BPos max = this.listener.getPos(this.listener.getWidth(), this.listener.getHeight());
        RPos regionMin = min.toRegionPos(this.listener.blocksPerFragment);
        RPos regionMax = max.toRegionPos(this.listener.blocksPerFragment);
        if (region.getX() < regionMin.getX() - 40 || region.getX() > regionMax.getX() + 40) return false;
        return region.getZ() >= regionMin.getZ() - 40 && region.getZ() <= regionMax.getZ() + 40;
    }

    public Fragment getFragmentAt(int regionX, int regionZ) {
        return this.getFragmentAt(regionX, regionZ, 1);
    }

    public Fragment getFragmentAt(int regionX, int regionZ, int factor) {
        RPos regionPos = new RPos(regionX, regionZ, this.listener.blocksPerFragment * factor);

        if (!this.fragments.containsKey(regionPos) && !this.scheduledRegions.contains(regionPos)) {
            this.fragments.put(regionPos, LOADING_FRAGMENT);
            this.scheduledRegions.add(regionPos);
            this.scheduledModified.set(true);
        }

        return this.fragments.get(regionPos);
    }
}