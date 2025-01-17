package fr.frinn.custommachinery.common.util;

import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class CycleTimer {
    /* the amount of time in ms to display one thing before cycling to the next one */
    private final int cycleTime;
    private long startTime;
    private long drawTime;
    private long pausedDuration = 0;

    public CycleTimer(int cycleTime) {
        this.cycleTime = cycleTime;
        long time = System.currentTimeMillis();
        this.startTime = time - cycleTime;
        this.drawTime = time;
    }

    @Nullable
    public <T> T get(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        long index = ((drawTime - startTime) / cycleTime) % list.size();
        return list.get(Math.toIntExact(index));
    }

    public <T> T getOrDefault(List<T> list, T defaultObject) {
        return Optional.ofNullable(get(list)).orElse(defaultObject);
    }

    public void onDraw() {
        if (!Screen.hasShiftDown()) {
            if (pausedDuration > 0) {
                startTime += pausedDuration;
                pausedDuration = 0;
            }
            drawTime = System.currentTimeMillis();
        } else {
            pausedDuration = System.currentTimeMillis() - drawTime;
        }
    }
}
