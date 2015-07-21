package fr.ribesg.pokemon.editor;

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
        final Timer timer = new Timer().start();

        try {
            Log.info("Creating a backup of " + romName + " as " + romName + ".original...");
            Files.copy(Paths.get(romName), Paths.get(romName + ".original"));
        } catch (final FileAlreadyExistsException ignored) {
            Log.info("\tFile already exists!");
        }

        Log.info("Extracting rom file using ndstool...");
        Tool.extract(romName);

        Log.info("Decompressing arm9.bin...");
        Tool.decompressArm9(romName);

        Log.info("Fixing arm9.bin so that it could work in a rom without being recompressed...");
        Tool.fixArm9(romName);
/*
        final ByteBuffer oneIntBuffer = ByteBuffer.allocate(4);
        try (final SeekableByteChannel arm9 = Files.newByteChannel(Paths.get(arm9File), StandardOpenOption.WRITE)) {

            arm9.position(Main.STARTER_1_OFFSET);
            oneIntBuffer.putInt(0, 1);
            arm9.write(oneIntBuffer);

            arm9.position(Main.STARTER_2_OFFSET);
            oneIntBuffer.putInt(0, 4);
            arm9.write(oneIntBuffer);

            arm9.position(Main.STARTER_3_OFFSET);
            oneIntBuffer.putInt(0, 7);
            arm9.write(oneIntBuffer);
        }
*/
        Log.info("Rebuilding rom as " + Tool.newRomName(romName) + "...");
        Tool.build(romName);

        Log.info("Done in " + timer.stop().diffString());
    }
}
