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
        Log.info("GAME_TITLE = " + this.getGameTitle());
        Log.info("GAME_CODE = " + this.getGameCode());
        Log.info("MAKER = " + this.getMaker());
        Log.info("ARM9_OFFSET = " + this.getIntAsHex(Rom.ARM9_OFFSET));
        Log.info("ARM9_ENTRY = " + this.getIntAsHex(Rom.ARM9_ENTRY));
        Log.info("ARM9_RAM = " + this.getIntAsHex(Rom.ARM9_RAM));
        Log.info("ARM9_SIZE = " + this.getIntAsHex(Rom.ARM9_SIZE));
        Log.info("ARM7_OFFSET = " + this.getIntAsHex(Rom.ARM7_OFFSET));
        Log.info("ARM7_ENTRY = " + this.getIntAsHex(Rom.ARM7_ENTRY));
        Log.info("ARM7_RAM = " + this.getIntAsHex(Rom.ARM7_RAM));
        Log.info("ARM7_SIZE = " + this.getIntAsHex(Rom.ARM7_SIZE));
        Log.info("FNT_OFFSET = " + this.getIntAsHex(Rom.FNT_OFFSET));
        Log.info("FNT_SIZE = " + this.getIntAsHex(Rom.FNT_SIZE));
        Log.info("FAT_OFFSET = " + this.getIntAsHex(Rom.FAT_OFFSET));
        Log.info("FAT_SIZE = " + this.getIntAsHex(Rom.FAT_SIZE));
        Log.info("ARM9_OVERLAY_OFFSET = " + this.getIntAsHex(Rom.ARM9_OVERLAY_OFFSET));
        Log.info("ARM9_OVERLAY_SIZE = " + this.getIntAsHex(Rom.ARM9_OVERLAY_SIZE));
        Log.info("ARM7_OVERLAY_OFFSET = " + this.getIntAsHex(Rom.ARM7_OVERLAY_OFFSET));
        Log.info("ARM7_OVERLAY_SIZE = " + this.getIntAsHex(Rom.ARM7_OVERLAY_SIZE));
        Log.info("ICON_TITLE_OFFSET = " + this.getIntAsHex(Rom.ICON_TITLE_OFFSET));
        Log.info("SECURE_AREA_CHECKSUM = " + this.getShortAsHex(Rom.SECURE_AREA_CHECKSUM));
        Log.info("TOTAL_USED_ROM_SIZE = " + this.getIntAsHex(Rom.TOTAL_USED_ROM_SIZE));
        Log.info("ROM_HEADER_SIZE = " + this.getIntAsHex(Rom.ROM_HEADER_SIZE));
        Log.info("HEADER_CHECKSUM = " + this.getShortAsHex(Rom.HEADER_CHECKSUM));
    }

    public ByteBuffer content() {
        assert this.content != null : "Rom isn't loaded!";
        return this.content;
    }

    public ByteBuffer getArm9Data() {
        final ByteBuffer content = this.content();
        try {
            final int arm9Offset = this.getInt(Rom.ARM9_OFFSET);
            final int arm9Size = this.getInt(Rom.ARM9_SIZE);
            content.position(arm9Offset);
            if (this.content().get(arm9Offset + arm9Size) == 0xFF) {
                content.limit(arm9Offset + arm9Size);
            } else {
                content.limit(arm9Offset + arm9Size + 12 /* Footer length */);
            }
            return content.slice().asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        } finally {
            content.flip();
        }
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
        return "0x" + Integer.toHexString(this.getInt(offset)).toUpperCase();
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
