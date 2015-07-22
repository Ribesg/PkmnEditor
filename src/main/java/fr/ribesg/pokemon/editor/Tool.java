package fr.ribesg.pokemon.editor;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;

/**
 * @author Ribesg
 */
public final class Tool {

    // ############ //
    // ## Global ## //
    // ############ //

    public static boolean run(final String[] args) throws IOException, InterruptedException {
        final ProcessBuilder pb = new ProcessBuilder(args);
        if (Log.isDebugEnabled()) {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        Log.debug("Running command: " + pb.command().stream().reduce((a, b) -> a + ' ' + b).get());
        final Process p = pb.start();
        final int res = p.waitFor();
        return res == 0;
    }

    // ############# //
    // ## NDSTool ## //
    // ############# //

    public static void ndstool(final String[] args) throws IOException, InterruptedException {
        Tool.run(Tool.prepend("tools/ndstool.exe", args));
    }

    public static String extract(final String romName) throws Throwable {
        final String folderName = Tool.folderName(romName);
        final Path folderPath = Paths.get(folderName);
        while (Files.isDirectory(folderPath)) {
            FileUtils.deleteDirectory(folderPath.toFile());
        }
        Files.createDirectory(folderPath);
        final String[] args = new String[] {
            "-x", '"' + romName + '"',
            "-9", '"' + Tool.arm9FileName(romName) + '"',
            "-7", '"' + folderName + "/arm7.bin" + '"',
            "-y9", '"' + folderName + "/y9.bin" + '"',
            "-y7", '"' + folderName + "/y7.bin" + '"',
            "-d", '"' + folderName + "/data" + '"',
            "-y", '"' + folderName + "/overlay" + '"',
            "-t", '"' + folderName + "/banner.bin" + '"',
            "-h", '"' + folderName + "/header.bin" + '"'
        };
        Tool.ndstool(args);
        return folderName;
    }

    public static void build(final String romName, final boolean removeFiles) throws Throwable {
        final String folderName = Tool.folderName(romName);
        final Path folderPath = Paths.get(folderName);
        if (!Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("No folder for this rom name");
        }
        final String[] args = new String[] {
            "-c", '"' + Tool.newRomName(romName) + '"',
            "-9", '"' + Tool.arm9FileName(romName) + '"',
            "-7", '"' + folderName + "/arm7.bin" + '"',
            "-y9", '"' + folderName + "/y9.bin" + '"',
            "-y7", '"' + folderName + "/y7.bin" + '"',
            "-d", '"' + folderName + "/data" + '"',
            "-y", '"' + folderName + "/overlay" + '"',
            "-t", '"' + folderName + "/banner.bin" + '"',
            "-h", '"' + folderName + "/header.bin" + '"'
        };
        Tool.ndstool(args);
        if (removeFiles) {
            FileUtils.deleteDirectory(folderPath.toFile());
        }
    }

    // ######### //
    // ## BLZ ## //
    // ######### //

    public static void decompressArm9(final String romName) throws IOException, InterruptedException {
        final Path arm9FilePath = Paths.get(Tool.arm9FileName(romName));
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9FilePath)).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer res = Arm9Tools.decompressBlz(arm9);
        Files.write(arm9FilePath, res.array());
    }

    // ############ //
    // ## Others ## //
    // ############ //

    public static void fixArm9(final String romName) throws IOException {
        final Path romPath = Paths.get(romName);
        final Path arm9Path = Paths.get(Tool.arm9FileName(romName));
        final Rom rom = new Rom(romPath);
        rom.load();
        final int arm9Ram = rom.getInt(Rom.ARM9_RAM);
        final int arm9EntryPoint = rom.getInt(Rom.ARM9_ENTRY);
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        Arm9Tools.fix(
            arm9,
            arm9Ram,
            arm9EntryPoint
        );
        Files.write(arm9Path, arm9.array());
    }

    public static int switchEndianness(final int input) {
        return
            0xFF000000 & (input << 24) |
            0x00FF0000 & (input << 8) |
            0x0000FF00 & (input >>> 8) |
            0x000000FF & (input >>> 24);
    }

    public static String newRomName(final String originalRomName) {
        return originalRomName.replaceFirst("\\.nds", "") + ".new.nds";
    }

    public static String folderName(final String romName) {
        return "extracted_" + romName;
    }

    public static String arm9FileName(final String romName) {
        return Tool.folderName(romName) + "/arm9.bin";
    }

    private static String[] prepend(final String arg0, final String[] args) {
        final String[] newArgs = new String[args.length + 1];
        newArgs[0] = arg0;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }
}
