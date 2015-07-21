package fr.ribesg.pokemon.editor;

import java.util.logging.*;

/**
 * @author Ribesg
 */
public final class Log {

    private static Logger LOGGER;

    static {
        Log.LOGGER = Logger.getLogger("PkmnEditor");
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "%5$s %6$s%n"
        );
        for (final Handler h : Log.LOGGER.getHandlers()) {
            Log.LOGGER.removeHandler(h);
        }
        Log.LOGGER.addHandler(new ConsoleHandler() {
            {
                this.setOutputStream(System.out);
            }
        });
    }

    public static boolean isDebugEnabled() {
        return Log.LOGGER.isLoggable(Level.FINE);
    }

    public static void debug(final Object message) {
        Log.LOGGER.log(Level.FINE, message.toString());
    }

    public static void info(final Object message) {
        Log.LOGGER.log(Level.INFO, message.toString());
    }

    public static void error(final Object message, final Throwable t) {
        Log.LOGGER.log(Level.SEVERE, message.toString(), t);
    }
}
