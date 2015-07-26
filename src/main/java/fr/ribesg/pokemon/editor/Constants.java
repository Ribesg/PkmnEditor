package fr.ribesg.pokemon.editor;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Ribesg
 */
public final class Constants {

    public static final Path   TMP_PATH;
    public static final String JAR_PATH;
    public static final Font   FONT;

    static {
        try {
            final Class<?> clazz = Constants.class;
            TMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"));
            JAR_PATH = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            final Font font = Font.createFont(Font.TRUETYPE_FONT, clazz.getResourceAsStream("/NotoSans-Regular.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            FONT = font.deriveFont(Font.PLAIN, 12);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static final int STARTER_1 = 152;
    public static final int STARTER_2 = 155;
    public static final int STARTER_3 = 158;
}
