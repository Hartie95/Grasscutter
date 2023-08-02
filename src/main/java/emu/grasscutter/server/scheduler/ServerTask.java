package emu.grasscutter.server.scheduler;

import emu.grasscutter.Grasscutter;
import lombok.*;

/**
 * This class works the same as a runnable, except with more information.
 */
public final class ServerTask implements Runnable {
    /* The runnable to run. */
    private final Runnable runnable;
    /* This ID is assigned by the scheduler. */
    @Getter private final int taskId;
    /* The period at which the task should be run. */
    /* The delay between the first execute. */
    private final int period, delay;

    public ServerTask(Runnable runnable, int taskId, int period, int delay) {
        this.runnable = runnable;
        this.taskId = taskId;
        this.period = period;
        this.delay = delay;
    }

    /* The amount of times the task has been run. */
    @Getter private int ticks = 0;

    /**
     * Cancels the task from running the next time.
     */
    public void cancel() {
        Grasscutter.getGameServer().getScheduler().cancelTask(this.taskId);
    }

    /**
     * Checks if the task should run at the current tick.
     * @return True if the task should run, false otherwise.
     * Example delayed task:
     * if scheduled at time 1690873237, delay 3s, then task should be executed in 1690873240
     */
    public boolean shouldRun() {
        // Increase tick count.
        var ticks = this.ticks++;
        if(this.delay != -1) {
            return ticks == this.delay;
        } else if(this.period != -1)
            return ticks % this.period == 0;
        else return true;
    }

    /**
     * Checks if the task should be canceled.
     * @return True if the task should be canceled, false otherwise.
     */
    public boolean shouldCancel() {
        return this.period == -1 && ticks > delay;
    }

    /**
     * Runs the task.
     */
    @Override public void run() {
        // Run the runnable.
        try {
            this.runnable.run();
        } catch (Exception ex){
            Grasscutter.getLogger().error("Exception during task: ", ex);
        }
    }
}
