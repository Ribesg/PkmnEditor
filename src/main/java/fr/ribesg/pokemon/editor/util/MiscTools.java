package fr.ribesg.pokemon.editor.util;

import fr.ribesg.pokemon.editor.Log;

import java.io.IOException;

/**
 * @author Ribesg
 */
public final class MiscTools {

    /**
     * Runs a new process and waits for it to end.
     *
     * @param args the commandline to run
     *
     * @return true if the process ended without error
     *
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted while
     *                              waiting for the process to end
     */
    public static boolean run(final String[] args) throws InterruptedException, IOException {
        final ProcessBuilder pb = new ProcessBuilder(args);
        if (Log.isDebugEnabled()) {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        Log.debug("Running command: " + pb.command().get(0).replaceAll(".+/", ""));
        final Process p = pb.start();
        final int res = p.waitFor();
        return res == 0;
    }

    /**
     * Switches the endianness of an int.
     *
     * @param input a LE or BE int
     *
     * @return a BE or LE int
     */
    public static int switchEndianness(final int input) {
        return 0xFF000000 & (input << 0x18) |
               0x00FF0000 & (input << 0x08) |
               0x0000FF00 & (input >> 0x08) |
               0x000000FF & (input >> 0x18);
    }

    /**
     * Generates a new rom file name based on an original rom file name.
     *
     * @param originalRomName the original rom file name
     *
     * @return a new rom file name
     */
    public static String newRomName(final String originalRomName) {
        return originalRomName.replaceFirst("\\.nds", "") + ".new.nds";
    }

    /**
     * Generates a folder name matching a rom file name.
     *
     * @param romName a rom file name
     *
     * @return a folder name
     */
    public static String folderName(final String romName) {
        return "extracted_" + romName;
    }

    /**
     * Prepends a String to a String array.
     *
     * @param arg0 the String to prepend
     * @param args the original String array
     *
     * @return the final String array
     */
    public static String[] prepend(final String arg0, final String[] args) {
        final String[] newArgs = new String[args.length + 1];
        newArgs[0] = arg0;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }

    public static String asHex(final int val) {
        return "0x" + Integer.toHexString(val).toUpperCase();
    }

    public static String asHex(final short val) {
        return MiscTools.asHex(val & 0xFFFF);
    }
}
