package fr.ribesg.pokemon.editor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Ribesg
 */
public final class Main {

    private static final int UNTRIMMED_ROM_SIZE = 1 << 27;
    private static final int STARTER_1_OFFSET   = 0x108538;
    private static final int STARTER_2_OFFSET   = 0x10853C;
    private static final int STARTER_3_OFFSET   = 0x108540;

    public static void main(final String[] args) {
        try {
            Main.changeStarters();
        } catch (final Throwable t) {
            Log.error("Oops", t);
        }
    }

    private static void changeStarters() throws Throwable {
        final String fileName = "Pokemon_HG_Fr.nds";
        final String newFileName = "Pokemon_HG_Fr_Custom.nds";

        final Rom rom = new Rom(Paths.get(fileName));
        rom.load();
        rom.logHeaderData();

        final ByteBuffer romContent = rom.content();
        final int romSize = romContent.capacity();

        final ByteBuffer compressedArm9 = rom.getArm9Data();
        final ByteBuffer arm9 = Compression.decompressBlz(compressedArm9);
        arm9.putInt(Main.STARTER_1_OFFSET, 1);
        arm9.putInt(Main.STARTER_2_OFFSET, 4);
        arm9.putInt(Main.STARTER_3_OFFSET, 7);

        final ByteBuffer customRom = ByteBuffer.allocate(Main.UNTRIMMED_ROM_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        // Copy header
        romContent.position(0);
        romContent.limit(rom.getInt(Rom.ROM_HEADER_SIZE));

        customRom.put(romContent);

        romContent.position(0);
        romContent.limit(romSize);

        // Copy decompressed Arm9
        customRom.position(rom.getInt(Rom.ARM9_OFFSET));
        customRom.put(arm9);

        // Align with some 0x??????00 address
        while ((customRom.position() & 0xFF) != 0x00) {
            customRom.put((byte) 0xFF);
        }
        final int pointerDiff = customRom.position() - rom.getInt(Rom.ARM9_OVERLAY_OFFSET);
        Log.info("PointerDiff=" + pointerDiff);

        // Copy everything else
        romContent.position(rom.getInt(Rom.ARM9_OVERLAY_OFFSET));
        romContent.limit(rom.getInt(Rom.TOTAL_USED_ROM_SIZE));

        customRom.put(romContent);

        romContent.position(0);
        romContent.limit(romSize);

        // Add some FF
        while (customRom.hasRemaining()) {
            customRom.put((byte) 0xFF);
        }

        // Change some pointers
        customRom.putInt(Rom.ARM9_SIZE, arm9.capacity() - 12);
        customRom.putInt(Rom.ARM7_OFFSET, rom.getInt(Rom.ARM7_OFFSET) + pointerDiff);
        customRom.putInt(Rom.FNT_OFFSET, rom.getInt(Rom.FNT_OFFSET) + pointerDiff);
        customRom.putInt(Rom.FAT_OFFSET, rom.getInt(Rom.FAT_OFFSET) + pointerDiff);
        customRom.putInt(Rom.ARM9_OVERLAY_OFFSET, rom.getInt(Rom.ARM9_OVERLAY_OFFSET) + pointerDiff);
        customRom.putInt(Rom.ICON_TITLE_OFFSET, rom.getInt(Rom.ICON_TITLE_OFFSET) + pointerDiff);
        customRom.putInt(Rom.TOTAL_USED_ROM_SIZE, rom.getInt(Rom.TOTAL_USED_ROM_SIZE) + pointerDiff);

        // Update CRC16 of Secure Area
        customRom.position(customRom.getInt(Rom.ARM9_OFFSET));
        customRom.limit(0x7FFF);

        final short secureAreaCRC16 = CRC16.get(customRom);

        customRom.position(0);
        customRom.limit(Main.UNTRIMMED_ROM_SIZE);

        customRom.putShort(Rom.SECURE_AREA_CHECKSUM, secureAreaCRC16);

        // Update CRC16 of Rom header
        customRom.position(0);
        customRom.limit(Rom.HEADER_CHECKSUM);

        final short headerCRC16 = CRC16.get(customRom);

        customRom.position(0);
        customRom.limit(Main.UNTRIMMED_ROM_SIZE);

        customRom.putShort(Rom.HEADER_CHECKSUM, headerCRC16);

        // Do some Magic
        //Magic.fix(customRom, rom.getInt(Rom.ARM9_RAM), rom.getInt(Rom.ARM9_ENTRY));

        // Save
        Files.write(Paths.get(newFileName), customRom.array());

        Log.info("Done.");
        Log.info("");

        // Check new ROM
        final Rom newRom = new Rom(Paths.get(newFileName));
        newRom.load();
        newRom.logHeaderData();
    }
}
