package fr.ribesg.pokemon.editor;

import java.io.RandomAccessFile;
import java.nio.file.*;

/**
 * @author Ribesg
 */
public final class Main {

    private static final int STARTER_1_OFFSET = 0x108538;
    private static final int STARTER_2_OFFSET = 0x10853C;
    private static final int STARTER_3_OFFSET = 0x108540;

    public static void main(final String[] args) {
        if (args.length != 1) {
            Log.info("Usage: java -jar PkmnEditor.jar <romName>");
        }
        try {
            new Main(args[0]);
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    private Main(final String romName) throws Throwable {
        Log.info("Original ROM data:");
        final Rom rom = new Rom(Paths.get(romName));
        rom.load();
        rom.printHeader();

        Log.info("");

        final Timer timer = new Timer().start();

        Log.info("Creating a backup of " + romName + " as " + romName + ".original...");
        Files.copy(Paths.get(romName), Paths.get(romName + ".original"), StandardCopyOption.REPLACE_EXISTING);

        Log.info("Extracting rom file using ndstool...");
        Tool.extract(romName);

        Log.info("Decompressing arm9.bin...");
        Tool.decompressArm9(romName);

        Log.info("Fixing arm9.bin so that it could work in a rom without being recompressed...");
        Tool.fixArm9(romName);

        try (
            final RandomAccessFile arm9 = new RandomAccessFile(
                Tool.arm9FileName(romName), "rw"
            )
        ) {
            arm9.seek(Main.STARTER_1_OFFSET);
            arm9.writeInt(Tool.switchEndianness(446));
            arm9.seek(Main.STARTER_2_OFFSET);
            arm9.writeInt(Tool.switchEndianness(447));
            arm9.seek(Main.STARTER_3_OFFSET);
            arm9.writeInt(Tool.switchEndianness(399));
        }

        Log.info("Rebuilding rom as " + Tool.newRomName(romName) + "...");
        Tool.build(romName, false);

        Log.info("Putting backup " + romName + ".original as back to " + romName + "...");
        Files.move(Paths.get(romName + ".original"), Paths.get(romName), StandardCopyOption.REPLACE_EXISTING);

        Log.info("Done in " + timer.stop().diffString());
        Log.info("");

        Log.info("Final ROM data:");
        final Rom newRom = new Rom(Paths.get(Tool.newRomName(romName)));
        newRom.load();
        newRom.printHeader();
    }
}
