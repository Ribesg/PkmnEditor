package fr.ribesg.pokemon.editor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author Ribesg
 */
public final class Timer {

    private static DecimalFormat FORMAT;

    public static DecimalFormat getFormatter() {
        if (Timer.FORMAT == null) {
            Timer.FORMAT = new DecimalFormat("#0.00");
            Timer.FORMAT.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
        }
        return Timer.FORMAT;
    }

    private long startTime, endTime;

    public Timer start() {
        this.startTime = System.nanoTime();
        return this;
    }

    public Timer stop() {
        this.endTime = System.nanoTime();
        return this;
    }

    public long nanoDiff() {
        if (this.startTime != -1 && this.endTime != -1) {
            return this.endTime - this.startTime;
        } else {
            return -1;
        }
    }

    public String diffString() {
        return Timer.parseDiff(this.nanoDiff());
    }

    public long hotNanoDiff() {
        return System.nanoTime() - this.startTime;
    }

    public String hotDiffString() {
        return Timer.parseDiff(this.hotNanoDiff());
    }

    public static String parseDiff(final long nano) {
        if (nano < 1_000L) {
            return nano + "ns";
        } else if (nano < 1_000_000L) {
            return Timer.getFormatter().format(nano / 1_000D) + "µs";
        } else if (nano < 1_000_000_000L) {
            return Timer.getFormatter().format(nano / 1_000_000D) + "ms";
        } else if (nano < 60_000_000_000L) {
            return Timer.getFormatter().format(nano / 1_000_000_000D) + "s";
        } else {
            return nano / 60_000_000_000L + "min " + Timer.getFormatter().format(nano % 1_000_000_000D) + "s";
        }
    }
}
