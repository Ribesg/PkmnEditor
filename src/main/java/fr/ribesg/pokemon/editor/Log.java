package fr.ribesg.pokemon.editor;

import javafx.scene.control.TextArea;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
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
            "%n%5$s %6$s"
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> Log.info("")));
    }

    public static boolean isDebugEnabled() {
        return Log.LOGGER.isLoggable(Level.FINE);
    }

    public static void setDebugEnabled(final boolean value) {
        Log.LOGGER.setLevel(value ? Level.ALL : Level.INFO);
    }

    public static void logInto(final TextArea textArea) {
        final WeakReference<TextArea> weakTextArea = new WeakReference<>(textArea);
        Log.LOGGER.addHandler(new ConsoleHandler() {

            {
                this.setLevel(Level.ALL);
            }

            @SuppressWarnings("ConstantConditions") // WeakReference#get can return null yeah. I know.
            @Override
            public void publish(@NotNull final LogRecord record) {
                if (!this.isLoggable(record)) {
                    return;
                }
                final String msg;
                try {
                    msg = this.getFormatter().format(record);
                } catch (final Exception e) {
                    // We don't want to throw an exception here, but we
                    // report the exception to any registered ErrorManager.
                    this.reportError(null, e, ErrorManager.FORMAT_FAILURE);
                    return;
                }

                try {
                    final String finalMsg = msg.replaceAll("[\n\r]", "");
                    if (weakTextArea.get().getText().isEmpty()) {
                        weakTextArea.get().appendText(finalMsg);
                    } else {
                        weakTextArea.get().appendText('\n' + finalMsg);
                    }
                } catch (final NullPointerException e) {
                    Log.LOGGER.removeHandler(this);
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

    public static void error(final Throwable t) {
        Log.LOGGER.log(Level.SEVERE, "Error caught", t);
        Log.flush();
    }

    public static void flush() {
        for (final Handler h : Log.LOGGER.getHandlers()) {
            h.flush();
        }
    }
}
