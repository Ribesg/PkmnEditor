package fr.ribesg.pokemon.editor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ribesg
 */
public final class Rom {

    public static int ARM9_OFFSET          = 0x020;
    public static int ARM9_ENTRY           = 0x024;
    public static int ARM9_RAM             = 0x028;
    public static int ARM9_SIZE            = 0x02C;
    public static int ARM7_OFFSET          = 0x030;
    public static int ARM7_ENTRY           = 0x034;
    public static int ARM7_RAM             = 0x038;
    public static int ARM7_SIZE            = 0x03C;
    public static int FNT_OFFSET           = 0x040;
    public static int FNT_SIZE             = 0x044;
    public static int FAT_OFFSET           = 0x048;
    public static int FAT_SIZE             = 0x04C;
    public static int ARM9_OVERLAY_OFFSET  = 0x050;
    public static int ARM9_OVERLAY_SIZE    = 0x054;
    public static int ARM7_OVERLAY_OFFSET  = 0x058;
    public static int ARM7_OVERLAY_SIZE    = 0x05C;
    public static int ICON_TITLE_OFFSET    = 0x068;
    public static int SECURE_AREA_CHECKSUM = 0x06C;
    public static int TOTAL_USED_ROM_SIZE  = 0x080;
    public static int ROM_HEADER_SIZE      = 0x084;
    public static int HEADER_CHECKSUM      = 0x15E;

    private final Path filePath;
    private ByteBuffer content = null;

    public Rom(@NotNull final Path filePath) {
        assert Files.exists(filePath) : "File not found: " + filePath;
        this.filePath = filePath;
    }

    public void load() throws IOException {
        assert this.content == null : "Rom is already loaded!";
        Log.debug("Reading ROM file...");
        final Timer timer = new Timer().start();
        final byte[] bytes = Files.readAllBytes(this.filePath);
        this.content = ByteBuffer.wrap(bytes).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        timer.stop();
        Log.debug("Loaded in " + timer.diffString());
    }

    public void printHeader() {
        Log.debug("\tGAME_TITLE = " + this.getGameTitle());
        Log.debug("\tGAME_CODE = " + this.getGameCode());
        Log.debug("\tMAKER = " + this.getMaker());
        Log.debug("\tARM9_OFFSET = " + this.getIntAsHex(Rom.ARM9_OFFSET));
        Log.debug("\tARM9_ENTRY = " + this.getIntAsHex(Rom.ARM9_ENTRY));
        Log.debug("\tARM9_RAM = " + this.getIntAsHex(Rom.ARM9_RAM));
        Log.debug("\tARM9_SIZE = " + this.getIntAsHex(Rom.ARM9_SIZE) +
                  "\n\t\t-> ARM9_END_OFFSET = " + this.asHex(this.getInt(Rom.ARM9_OFFSET) + this.getInt(Rom.ARM9_SIZE))
        );
        Log.debug("\tARM7_OFFSET = " + this.getIntAsHex(Rom.ARM7_OFFSET));
        Log.debug("\tARM7_ENTRY = " + this.getIntAsHex(Rom.ARM7_ENTRY));
        Log.debug("\tARM7_RAM = " + this.getIntAsHex(Rom.ARM7_RAM));
        Log.debug("\tARM7_SIZE = " + this.getIntAsHex(Rom.ARM7_SIZE) +
                  "\n\t\t-> ARM7_END_OFFSET = " + this.asHex(this.getInt(Rom.ARM7_OFFSET) + this.getInt(Rom.ARM7_SIZE))
        );
        Log.debug("\tFNT_OFFSET = " + this.getIntAsHex(Rom.FNT_OFFSET));
        Log.debug("\tFNT_SIZE = " + this.getIntAsHex(Rom.FNT_SIZE) +
                  "\n\t\t-> FNT_END_OFFSET = " + this.asHex(this.getInt(Rom.FNT_OFFSET) + this.getInt(Rom.FNT_SIZE))
        );
        Log.debug("\tFAT_OFFSET = " + this.getIntAsHex(Rom.FAT_OFFSET));
        Log.debug("\tFAT_SIZE = " + this.getIntAsHex(Rom.FAT_SIZE) +
                  "\n\t\t-> FAT_END_OFFSET = " + this.asHex(this.getInt(Rom.FAT_OFFSET) + this.getInt(Rom.FAT_SIZE))
        );
        Log.debug("\tARM9_OVERLAY_OFFSET = " + this.getIntAsHex(Rom.ARM9_OVERLAY_OFFSET));
        Log.debug("\tARM9_OVERLAY_SIZE = " + this.getIntAsHex(Rom.ARM9_OVERLAY_SIZE) +
                  "\n\t\t-> ARM9_OVERLAY_END_OFFSET = " + this.asHex(this.getInt(Rom.ARM9_OVERLAY_OFFSET) + this.getInt(Rom.ARM9_OVERLAY_SIZE))
        );
        Log.debug("\tARM7_OVERLAY_OFFSET = " + this.getIntAsHex(Rom.ARM7_OVERLAY_OFFSET));
        Log.debug("\tARM7_OVERLAY_SIZE = " + this.getIntAsHex(Rom.ARM7_OVERLAY_SIZE) +
                  "\n\t\t-> ARM7_OVERLAY_END_OFFSET = " + this.asHex(this.getInt(Rom.ARM7_OVERLAY_OFFSET) + this.getInt(Rom.ARM7_OVERLAY_SIZE))
        );
        Log.debug("\tICON_TITLE_OFFSET = " + this.getIntAsHex(Rom.ICON_TITLE_OFFSET));
        Log.debug("\tSECURE_AREA_CHECKSUM = " + this.getShortAsHex(Rom.SECURE_AREA_CHECKSUM));
        Log.debug("\tTOTAL_USED_ROM_SIZE = " + this.getIntAsHex(Rom.TOTAL_USED_ROM_SIZE));
        Log.debug("\tROM_HEADER_SIZE = " + this.getIntAsHex(Rom.ROM_HEADER_SIZE));
        Log.debug("\tHEADER_CHECKSUM = " + this.getShortAsHex(Rom.HEADER_CHECKSUM));
    }

    public ByteBuffer content() {
        assert this.content != null : "Rom isn't loaded!";
        return this.content;
    }

    public String getGameTitle() {
        return this.readString("", 0x0000, 10);
    }

    public String getGameCode() {
        return this.readString("NTR-", 0x000C, 4);
    }

    public String getMaker() {
        switch (this.readString("", 0x0010, 2)) {
            case "00":
                return "Homebrew";
            case "01":
                return "Nintendo";
            default:
                return "Unknown";
        }
    }

    public String getIntAsHex(final int offset) {
        return this.asHex(this.getInt(offset));
    }

    private String asHex(final int integer) {
        return "0x" + Integer.toHexString(integer).toUpperCase();
    }

    public int getInt(final int offset) {
        return this.content().getInt(offset);
    }

    public String getShortAsHex(final int offset) {
        return "0x" + Integer.toHexString(this.getShort(offset) & 0xFFFF).toUpperCase();
    }

    public int getShort(final int offset) {
        return this.content().getShort(offset);
    }

    public String readString(@NotNull final String prefix, final int offset, final int size) {
        final ByteBuffer content = this.content();
        final StringBuilder res = new StringBuilder(prefix);
        for (int i = offset; i < offset + size; i++) {
            res.append((char) content.get(i));
        }
        return res.toString();
    }
}
