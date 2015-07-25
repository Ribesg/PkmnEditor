package fr.ribesg.pokemon.editor;

import com.google.common.io.Files;
import fr.ribesg.pokemon.editor.gui.MainWindow;
import fr.ribesg.pokemon.editor.tool.Arm9Tool;
import fr.ribesg.pokemon.editor.tool.NdsTool;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Ribesg
 */
public final class Context {

    private MainWindow mainWindow;

    private Offsets offsets;
    private Lang    lang;

    private Rom  rom;
    private Path romPath;

    private boolean startersChanged;

    public Context(final MainWindow mainWindow) throws IOException {
        this.mainWindow = mainWindow;

        this.offsets = null;
        this.lang = new Lang();

        this.rom = null;
        this.romPath = null;

        this.startersChanged = false;
    }

    public void loadLang(final String lang) throws IOException {
        this.lang = new Lang(lang);
    }

    public boolean saveRomAs(final Path path) {
        final boolean nothingToDo = !this.startersChanged;
        if (nothingToDo) {
            Log.error("No change to save!");
            return false;
        }
        final Path tmpDir = Files.createTempDir().toPath();
        try {
            NdsTool.extract(this.romPath, tmpDir);
        } catch (final IOException e) {
            Log.error("Failed to extract ROM", e);
            return false;
        }
        if (this.startersChanged) {
            final Path arm9Path = tmpDir.resolve("arm9.bin");
            try {
                Arm9Tool.decompressArm9(arm9Path);
            } catch (final IOException e) {
                Log.error("Failed to decompress arm9.bin", e);
                return false;
            }
            try {
                Arm9Tool.fixArm9(this.offsets.getMagicFix(), arm9Path);
            } catch (final IOException e) {
                Log.error("Failed to fix arm9.bin", e);
                return false;
            }
            try {
                Arm9Tool.setInts(
                    arm9Path,
                    this.offsets.getStarters(), this.mainWindow.getStarter1(),
                    this.offsets.getStarters() + 4, this.mainWindow.getStarter2(),
                    this.offsets.getStarters() + 8, this.mainWindow.getStarter3()
                );
            } catch (final IOException e) {
                Log.error("Failed to set starters in arm9.bin", e);
                return false;
            }
        }
        try {
            NdsTool.build(tmpDir, path);
        } catch (final IOException e) {
            Log.error("Failed to build ROM", e);
            return false;
        }
        return true;
    }

    public boolean loadRom(final Path path) {
        this.romPath = path;
        this.rom = new Rom(this.romPath);
        try {
            this.rom.load();
            switch (this.rom.getGameTitle()) {
                case "POKEMON HG":
                case "POKEMON SS":
                    this.loadOffsets(this.rom);
                    break;
                default:
                    throw new IOException("Invalid game");
            }
            return true;
        } catch (final IOException e) {
            Log.error("Failed to load ROM", e);
            this.rom = null;
            this.romPath = null;
            return false;
        }
    }

    private void loadOffsets(final Rom rom) throws IOException {
        final String prefix = rom.getGameTitle().substring(8);
        final String suffix;
        final int headCrc = rom.getInt(Rom.HEADER_CHECKSUM);
        try {
            switch (rom.getGameCode().charAt(7)) {
                case 'D':
                    assert headCrc == 0x34DA || headCrc == 0x972D
                        : "Invalid ROM header checksum";
                    suffix = "GER";
                    break;
                case 'E':
                    // Damn EUR and USA roms having same code :-/
                    if (0x161F == headCrc || 0xA6CB == headCrc) {
                        suffix = "EUR";
                    } else if (0xD061 == headCrc || 0x60B5 == headCrc) {
                        suffix = "USA";
                    } else {
                        throw new IOException("Unknown rom!");
                    }
                    break;
                case 'F':
                    assert headCrc == 0x4291 || headCrc == 0x4D6A
                        : "Invalid ROM header checksum";
                    suffix = "FRA";
                    break;
                case 'I':
                    assert headCrc == 0xF7F6 || headCrc == 0x9D01
                        : "Invalid ROM header checksum";
                    suffix = "ITA";
                    break;
                case 'J':
                    assert headCrc == 0x09FB || headCrc == 0xC360
                        : "Invalid ROM header checksum";
                    suffix = "JAP";
                    break;
                case 'K':
                    assert headCrc == 0xC645 || headCrc == 0xD817
                        : "Invalid ROM header checksum";
                    suffix = "KOR";
                    break;
                case 'S':
                    assert headCrc == 0x68BA || headCrc == 0xAC63
                        : "Invalid ROM header checksum";
                    suffix = "SPA";
                    break;
                default:
                    throw new IOException("Unsupported rom lang: " + rom.getGameCode().charAt(7));
            }
        } catch (final AssertionError e) {
            throw new IOException(e.getMessage(), e);
        }
        this.offsets = new Offsets(prefix + '_' + suffix);
    }

    public Offsets getOffsets() {
        return this.offsets;
    }

    public Lang getLang() {
        return this.lang;
    }

    public Rom getRom() {
        return this.rom;
    }

    public Path getRomPath() {
        return this.romPath;
    }

    public boolean getStartersChanged() {
        return this.startersChanged;
    }

    public void setStartersChanged(final boolean value) {
        this.startersChanged = value;
    }
}
