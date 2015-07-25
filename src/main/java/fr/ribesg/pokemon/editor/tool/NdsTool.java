package fr.ribesg.pokemon.editor.tool;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ribesg
 */
public final class NdsTool {

    private static String NDSTOOL_LOCATION = null;

    public static void setNdstoolLocation(final String location) {
        NdsTool.NDSTOOL_LOCATION = location;
    }

    /**
     * Runs NDSTool with the provided arguments.
     *
     * @param args arguments for NDSTool
     *
     * @return true if everything went well, false otherwise
     *
     * @throws IOException if an I/O error occurs
     */
    public static boolean ndstool(final String[] args) throws IOException {
        assert NdsTool.NDSTOOL_LOCATION != null;
        try {
            return MiscTools.run(MiscTools.prepend(NdsTool.NDSTOOL_LOCATION, args));
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Extracts a rom file to the provided folder using NDSTool, removing the
     * folder first if it already existed.
     *
     * @param romPath    the rom file path
     * @param folderPath the folder path
     *
     * @throws IOException if anything wrong happens
     */
    public static void extract(final Path romPath, final Path folderPath) throws IOException {
        while (Files.isDirectory(folderPath)) {
            FileUtils.deleteDirectory(folderPath.toFile());
        }
        Files.createDirectory(folderPath);
        final String romPathString = romPath.toAbsolutePath().toString().replace("\\", "/");
        final String folderPathString = folderPath.toAbsolutePath().toString();
        final String[] args = new String[] {
            "-x", '"' + romPathString + '"',
            "-9", '"' + folderPathString + "/arm9.bin" + '"',
            "-7", '"' + folderPathString + "/arm7.bin" + '"',
            "-y9", '"' + folderPathString + "/y9.bin" + '"',
            "-y7", '"' + folderPathString + "/y7.bin" + '"',
            "-d", '"' + folderPathString + "/data" + '"',
            "-y", '"' + folderPathString + "/overlay" + '"',
            "-t", '"' + folderPathString + "/banner.bin" + '"',
            "-h", '"' + folderPathString + "/header.bin" + '"'
        };
        if (!NdsTool.ndstool(args)) {
            throw new IOException("Failed to extract " + romPathString + " to " + folderPathString + ": NDSTool failed");
        }
    }

    /**
     * Builds a rom file from the provided folder using NDSTool, eventually
     * removing the folder afterward.
     *
     * @param folderPath the folder name
     * @param newRomPath the new rom name
     *
     * @throws IOException if anything wrong happens
     */
    public static void build(final Path folderPath, final Path newRomPath) throws IOException {
        if (!Files.isDirectory(folderPath)) {
            throw new IOException("No folder for this rom name");
        }
        final String newRomPathString = newRomPath.toAbsolutePath().toString().replace("\\", "/");
        final String folderPathString = folderPath.toAbsolutePath().toString();
        final String[] args = new String[] {
            "-c", '"' + newRomPathString + '"',
            "-9", '"' + folderPathString + "/arm9.bin" + '"',
            "-7", '"' + folderPathString + "/arm7.bin" + '"',
            "-y9", '"' + folderPathString + "/y9.bin" + '"',
            "-y7", '"' + folderPathString + "/y7.bin" + '"',
            "-d", '"' + folderPathString + "/data" + '"',
            "-y", '"' + folderPathString + "/overlay" + '"',
            "-t", '"' + folderPathString + "/banner.bin" + '"',
            "-h", '"' + folderPathString + "/header.bin" + '"'
        };
        try {
            if (!NdsTool.ndstool(args)) {
                throw new IOException("Failed to build " + newRomPathString + " using " + folderPathString + ": NDSTool failed");
            }
        } finally {
            FileUtils.deleteDirectory(folderPath.toFile());
        }
    }
}
