package fr.ribesg.pokemon.editor;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
                this.setLevel(Level.ALL);
            }
        });
    }

    public static boolean isDebugEnabled() {
        return Log.LOGGER.isLoggable(Level.FINE);
    }

    public static void setDebugEnabled(final boolean value) {
        Log.LOGGER.setLevel(value ? Level.ALL : Level.INFO);
    }

    public static void logInto(final JTextArea textArea) {
        Log.LOGGER.addHandler(new ConsoleHandler() {

            {
                this.setLevel(Level.ALL);
            }

            @Override
            public void publish(@NotNull final LogRecord record) {
                if (!this.isLoggable(record)) {
                    return;
                }
                String msg;
                try {
                    msg = this.getFormatter().format(record);
                } catch (final Exception e) {
                    // We don't want to throw an exception here, but we
                    // report the exception to any registered ErrorManager.
                    this.reportError(null, e, ErrorManager.FORMAT_FAILURE);
                    return;
                }
                if (textArea.isValid()) {
                    textArea.append(msg);
                }
            }
        });
    }

    public static void debug(final Object message) {
        Log.LOGGER.log(Level.FINE, message.toString());
    }

    public static void debug(final Object message, final Throwable t) {
        Log.LOGGER.log(Level.FINE, message.toString(), t);
    }

    public static void info(final Object message) {
        Log.LOGGER.log(Level.INFO, message.toString());
    }

    public static void error(final Object message) {
        Log.LOGGER.log(Level.SEVERE, message.toString());
        Log.flush();
    }

    public static void error(final Object message, final Throwable t) {
        Log.LOGGER.log(Level.SEVERE, message.toString(), t);
        Log.flush();
    }

    public static void flush() {
        for (final Handler h : Log.LOGGER.getHandlers()) {
            h.flush();
        }
    }
}
