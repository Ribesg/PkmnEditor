package fr.ribesg.pokemon.editor.tool;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;

/**
 * @author Ribesg
 */
public final class NdsTool {

    /**
     * Runs NDSTool with the provided arguments.
     *
     * @param args arguments for NDSTool
     *
     * @return true if everything went well, false otherwise
     *
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted while
     *                              waiting for the process to end
     */
    public static boolean ndstool(final String[] args) throws IOException, InterruptedException {
        return MiscTools.run(MiscTools.prepend("tools/ndstool.exe", args));
    }

    /**
     * Extracts a rom file to the provided folder using NDSTool, removing the
     * folder first if it already existed.
     *
     * @param romName    the rom file name
     * @param folderName the folder name
     *
     * @throws Throwable if anything wrong happens
     */
    public static void extract(final String romName, final String folderName) throws Throwable {
        final Path folderPath = Paths.get(folderName);
        while (Files.isDirectory(folderPath)) {
            FileUtils.deleteDirectory(folderPath.toFile());
        }
        Files.createDirectory(folderPath);
        final String[] args = new String[] {
            "-x", '"' + romName + '"',
            "-9", '"' + folderName + "/arm9.bin" + '"',
            "-7", '"' + folderName + "/arm7.bin" + '"',
            "-y9", '"' + folderName + "/y9.bin" + '"',
            "-y7", '"' + folderName + "/y7.bin" + '"',
            "-d", '"' + folderName + "/data" + '"',
            "-y", '"' + folderName + "/overlay" + '"',
            "-t", '"' + folderName + "/banner.bin" + '"',
            "-h", '"' + folderName + "/header.bin" + '"'
        };
        if (!NdsTool.ndstool(args)) {
            throw new RuntimeException("Failed to extract " + romName + " to " + folderName + ": NDSTool failed");
        }
    }

    /**
     * Builds a rom file from the provided folder using NDSTool, eventually
     * removing the folder afterward.
     *
     * @param folderName  the folder name
     * @param newRomName  the new rom name
     * @param removeFiles if this should remove the folder
     *
     * @throws Throwable if anything wrong happens
     */
    public static void build(final String folderName, final String newRomName, final boolean removeFiles) throws Throwable {
        final Path folderPath = Paths.get(folderName);
        if (!Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("No folder for this rom name");
        }
        final String[] args = new String[] {
            "-c", '"' + newRomName + '"',
            "-9", '"' + folderName + "/arm9.bin" + '"',
            "-7", '"' + folderName + "/arm7.bin" + '"',
            "-y9", '"' + folderName + "/y9.bin" + '"',
            "-y7", '"' + folderName + "/y7.bin" + '"',
            "-d", '"' + folderName + "/data" + '"',
            "-y", '"' + folderName + "/overlay" + '"',
            "-t", '"' + folderName + "/banner.bin" + '"',
            "-h", '"' + folderName + "/header.bin" + '"'
        };
        try {
            if (!NdsTool.ndstool(args)) {
                throw new RuntimeException("Failed to build " + newRomName + " using " + folderName + ": NDSTool failed");
            }
        } finally {
            if (removeFiles) {
                FileUtils.deleteDirectory(folderPath.toFile());
            }
        }
    }
}
