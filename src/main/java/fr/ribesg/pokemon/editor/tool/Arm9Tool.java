package fr.ribesg.pokemon.editor.tool;

import fr.ribesg.pokemon.editor.Log;
import fr.ribesg.pokemon.editor.Rom;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kingcom (Original C# code)
 * @author Ribesg (This Java implementation)
 */
public final class Arm9Tool {

    /**
     * This thing is actually Arm9 code we're looking for.
     */
    private static final int[] ndsCompSignature = new int[] {
        0x00, 0x00, 0x50, 0xE3, 0x27, 0x00, 0x00, 0x0A, 0xF0, 0x00, 0x2D, 0xE9,
        0x06, 0x00, 0x10, 0xE9, 0x02, 0x20, 0x80, 0xE0, 0x21, 0x3C, 0x40, 0xE0,
        0xFF, 0x14, 0xC1, 0xE3, 0x01, 0x10, 0x40, 0xE0, 0x02, 0x40, 0xA0, 0xE1,
        0x01, 0x00, 0x53, 0xE1, 0x15, 0x00, 0x00, 0xDA, 0x01, 0x50, 0x73, 0xE5,
        0x08, 0x60, 0xA0, 0xE3, 0x01, 0x60, 0x56, 0xE2, 0xF9, 0xFF, 0xFF, 0xBA,
        0x80, 0x00, 0x15, 0xE3, 0x02, 0x00, 0x00, 0x1A, 0x01, 0x00, 0x73, 0xE5,
        0x01, 0x00, 0x62, 0xE5, 0x09, 0x00, 0x00, 0xEA, 0x01, 0xC0, 0x73, 0xE5,
        0x01, 0x70, 0x73, 0xE5, 0x0C, 0x74, 0x87, 0xE1, 0x0F, 0x7A, 0xC7, 0xE3,
        0x02, 0x70, 0x87, 0xE2, 0x20, 0xC0, 0x8C, 0xE2, 0x07, 0x00, 0xD2, 0xE7,
        0x01, 0x00, 0x62, 0xE5, 0x10, 0xC0, 0x5C, 0xE2, 0xFB, 0xFF, 0xFF, 0xAA,
        0x01, 0x00, 0x53, 0xE1, 0x85, 0x50, 0xA0, 0xE1, 0xEB, 0xFF, 0xFF, 0xCA,
        0x00, 0x00, 0xA0, 0xE3, 0x1F, 0x30, 0xC1, 0xE3, 0x9A, 0x0F, 0x07, 0xEE,
        0x35, 0x3F, 0x07, 0xEE, 0x3E, 0x3F, 0x07, 0xEE, 0x20, 0x30, 0x83, 0xE2,
        0x04, 0x00, 0x53, 0xE1, 0xF9, 0xFF, 0xFF, 0xBA, 0xF0, 0x00, 0xBD, 0xE8,
        0x1E, 0xFF, 0x2F, 0xE1
    };

    /**
     * Sets multiple ints in the arm9.bin file, switching the endianness in
     * the process.
     *
     * @param arm9Path         path to the arm9.bin file
     * @param offsetsAndValues offset-value couples
     *
     * @throws IOException if anything wrong occurs
     */
    public static void setInts(final Path arm9Path, final int... offsetsAndValues) throws IOException {
        assert offsetsAndValues.length % 2 == 0;
        try (final RandomAccessFile arm9 = new RandomAccessFile(arm9Path.toFile(), "rw")) {
            for (int i = 0; i < offsetsAndValues.length; i += 2) {
                arm9.seek(offsetsAndValues[i]);
                arm9.writeInt(MiscTools.switchEndianness(offsetsAndValues[i + 1]));
            }
        }
    }

    /**
     * Decompresses an arm9.bin file in place.
     *
     * @param arm9Path the arm9.bin file path
     *
     * @throws IOException if some I/O goes wrong
     */
    public static void decompressArm9(final Path arm9Path) throws IOException {
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer res = Arm9Tool.decompressBlz(arm9);
        Files.write(arm9Path, res.array());
    }

    /**
     * Fixes an extracted arm9.bin file.
     *
     * @param romPath  the rom path the arm9.bin file was extracted from
     * @param arm9Path the arm9.bin file path
     *
     * @throws IOException if some I/O goes wrong
     */
    public static void fixArm9(final Path romPath, final Path arm9Path) throws IOException {
        final Rom rom = new Rom(romPath);
        rom.load();
        final int arm9Ram = rom.getInt(Rom.ARM9_RAM);
        final int arm9EntryPoint = rom.getInt(Rom.ARM9_ENTRY);
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        Arm9Tool.fixUncompressedArm9(
            arm9,
            arm9Ram,
            arm9EntryPoint
        );
        Files.write(arm9Path, arm9.array());
    }

    /**
     * Fixes an extracted arm9.bin file.
     *
     * @param offset   the offset of the pointer which should be set to null
     * @param arm9Path the arm9.bin file path
     *
     * @throws IOException if some I/O goes wrong
     */
    public static void fixArm9(final int offset, final Path arm9Path) throws IOException {
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        if (Log.isDebugEnabled()) {
            Log.debug("Magic fix applied at 0x" + String.format("%08X", offset));
        }
        arm9.putInt(offset, 0);
        Files.write(arm9Path, arm9.array());
    }

    /**
     * Finds the offset of the pointer which should be set to null in a
     * decompressed arm9.bin file.
     *
     * @param romPath  the rom path the arm9.bin file was extracted from
     * @param arm9Path the arm9.bin file path
     *
     * @return the offset of the pointer which should be set to null
     */
    public static int findMagicOffset(final Path romPath, final Path arm9Path) throws IOException {
        final Rom rom = new Rom(romPath);
        rom.load();
        final int arm9Ram = rom.getInt(Rom.ARM9_RAM);
        final int arm9EntryPoint = rom.getInt(Rom.ARM9_ENTRY);
        final ByteBuffer arm9 = ByteBuffer.wrap(Files.readAllBytes(arm9Path)).order(ByteOrder.LITTLE_ENDIAN);
        return Arm9Tool.findMagicOffset(arm9, arm9Ram, arm9EntryPoint);
    }

    /**
     * Fixes that arm9.bin content data.
     *
     * @param arm9Content     the uncompressed arm9.bin file content
     * @param arm9RamLocation location where the arm9.bin file is loaded by DS
     * @param arm9EntryPoint  first arm9 instruction executed by the DS
     */
    private static void fixUncompressedArm9(final ByteBuffer arm9Content, final int arm9RamLocation, final int arm9EntryPoint) {
        final int pos = Arm9Tool.findMagicOffset(arm9Content, arm9RamLocation, arm9EntryPoint);
        if (pos < 0) {
            throw new RuntimeException("Not found :-/");
        }
        if (Log.isDebugEnabled()) {
            Log.debug("Magic fix applied at 0x" + String.format("%08X", pos));
        }
        arm9Content.putInt(pos, 0);
    }

    /**
     * Finds the offset of the pointer which should be set to null in a
     * decompressed arm9.bin file.
     *
     * @param arm9Content     the uncompressed arm9.bin file content
     * @param arm9RamLocation location where the arm9.bin file is loaded by DS
     * @param arm9EntryPoint  first arm9 instruction executed by the DS
     *
     * @return the offset of the pointer which should be set to null
     */
    private static int findMagicOffset(final ByteBuffer arm9Content, final int arm9RamLocation, final int arm9EntryPoint) {
        int i, j, destPos, structPos, structOffset, pos = -1, res = -1;

        final int startSearchIndex = arm9EntryPoint - arm9RamLocation;
        final int endSearchIndex = arm9Content.capacity() - Arm9Tool.ndsCompSignature.length;

        searchLoop:
        for (i = startSearchIndex; i < endSearchIndex; i += 4) {
            for (j = 0; j < Arm9Tool.ndsCompSignature.length; j++) {
                if ((0xFF & arm9Content.get(i + j)) != Arm9Tool.ndsCompSignature[j]) {
                    continue searchLoop;
                }
            }
            pos = i;
            break;
        }
        if (pos == -1) {
            res = -2;
        }

        if (res == -1) {
            for (i = startSearchIndex; i < arm9Content.capacity(); i += 4) {
                if ((arm9Content.getInt(i) & 0xFF000000) == 0xEB000000) {
                    destPos = ((arm9Content.getInt(i) & 0xFFFFFF) << 8) >> 8;
                    destPos = (destPos << 2) + 8 + i;

                    if (destPos == pos) {
                        if ((arm9Content.getInt(i - 8) & 0xFFFF0000) != 0xE59F0000) {
                            continue;
                        }
                        structPos = arm9Content.getInt((arm9Content.getInt(i - 8) & 0xFFF) + i) - arm9RamLocation;

                        if ((arm9Content.getInt(i - 4) & 0xFFF00000) != 0xE5900000) {
                            continue;
                        }
                        structOffset = arm9Content.getInt(i - 4) & 0xFFF;

                        res = structPos + structOffset;
                        break;
                    }
                }
            }
        }
        return res;
    }

    /**
     * Decompresses the provided ByteBuffer using the BLZ algorithm, designed
     * for arm9.bin files only.
     *
     * @param input the data to decompress
     *
     * @return the decompressed data
     */
    private static ByteBuffer decompressBlz(final ByteBuffer input) {
        final int inputSize = input.capacity();

        final int footerSize;
        if (input.getInt(inputSize - 12) == 0xDEC00621) {
            footerSize = 12;
        } else {
            footerSize = 0;
        }

        final int dataSize = inputSize - footerSize;

        final int w1 = input.getInt(dataSize - 4);
        final int w2 = input.getInt(dataSize - 8);
        final int outputSize = inputSize + w1;
        final int outputPos = dataSize + w1;
        final int inputPos = dataSize - (w2 >> 24);
        final int inputEnd = dataSize - (w2 & 0xFFFFFF);

        final ByteBuffer output = ByteBuffer.allocate(outputSize);

        // Copy inputEnd bytes from input/0 to output/0
        input.limit(inputEnd);
        output.put(input);
        input.limit(inputSize);

        // Copy footer from input/dataSize to output/outputPos
        output.position(outputPos);
        input.position(dataSize);
        output.put(input);
        output.position(0);
        input.position(0);

        int flagByte = 0, bit = 0, inPos = inputPos, outPos = outputPos, pos, size, i;
        while (inPos > inputEnd) {
            if (bit == 0) {
                bit = 0x80;
                flagByte = 0xFF & input.get(--inPos);
            }
            if ((flagByte & bit) != 0) {
                pos = ((0xFF & input.get(inPos - 2)) | ((0xFF & input.get(inPos - 1)) << 8) & 0xFFF) + 2;
                size = ((0xFF & input.get(inPos - 1)) >> 4) + 3;
                inPos -= 2;
                for (i = 0; i < size; i++) {
                    output.put(outPos - 1, output.get(outPos + pos));
                    outPos--;
                }
            } else {
                output.put(--outPos, input.get(--inPos));
            }
            bit >>= 1;
        }

        output.position(0);
        output.limit(outputSize);
        return output;
    }
}
