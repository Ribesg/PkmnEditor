package fr.ribesg.pokemon.editor;

import java.nio.ByteBuffer;

/**
 * @author Ribesg
 */
public final class Compression {

    /**
     * Decompresses a file using BLZ.
     *
     * @param input the compressed file
     *
     * @return the decompressed file
     */
    public static ByteBuffer decompressBlz(final ByteBuffer input) {
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
                flagByte = input.get(--inPos);
            }
            if ((flagByte & bit) != 0) {
                pos = (input.get(inPos - 2) | (input.get(inPos - 1) << 8) & 0xFFF) + 2;
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
