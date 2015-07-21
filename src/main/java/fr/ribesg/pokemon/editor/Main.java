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
        try {
            new Main();
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    private Main() throws Throwable {
        final String romName = "Pokemon_HG_Fr.nds";
        try {
            Files.copy(Paths.get(romName), Paths.get(romName + ".original"));
        } catch (final FileAlreadyExistsException ignored) {
        }

        Tool.extract(romName);
        Tool.decompressArm9(romName);
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
        Tool.build(romName);
    }
}
