package screen;

import java.awt.*;

/** Paleta de la calculadora: carcasa, teclas y pantalla LCD. */
public final class Theme {

    private Theme() {
    }

    // fondo y carcasa
    public static final Color DESK = new Color(0x111216);
    public static final Color BODY_TOP = new Color(0x34373E);
    public static final Color BODY_BOTTOM = new Color(0x22242A);
    public static final Color BODY_EDGE = new Color(0x4A4E57);
    public static final Color BODY_TEXT = new Color(0xC9CCD4);
    public static final Color BODY_MUTED = new Color(0x8A8F9C);

    // teclas
    public static final Color KEY_FUNC = new Color(0x2B2D33);
    public static final Color KEY_NUM = new Color(0xE3E4E8);
    public static final Color KEY_OP = new Color(0x4F6FE8);
    public static final Color KEY_CTRL = new Color(0x4A4E58);
    public static final Color KEY_VAR = new Color(0x7C5CE0);
    public static final Color KEY_ENTER = new Color(0x2FAF7E);
    public static final Color KEY_CLEAR = new Color(0xD0504A);
    public static final Color KEY_2ND = new Color(0xF2B53A);
    public static final Color KEY_TEXT_LIGHT = new Color(0xF4F5F8);
    public static final Color KEY_TEXT_DARK = new Color(0x1B1C20);

    // pantalla LCD
    public static final Color LCD = new Color(0xCFD8C4);
    public static final Color LCD_DARK = new Color(0xB9C3AE);
    public static final Color INK = new Color(0x1E2820);
    public static final Color INK_MUTED = new Color(0x56614F);
    public static final Color INK_ERROR = new Color(0xA12A22);
    public static final Color CURVE = new Color(0x1D3FA8);
    public static final Color AREA_POS = new Color(0x1D, 0x3F, 0xA8, 70);
    public static final Color AREA_NEG = new Color(0xB0, 0x2A, 0x2A, 70);
    public static final Color MARK = new Color(0xA8571B);
    public static final Color GRID = new Color(0x1E, 0x28, 0x20, 28);
    public static final Color AXIS = new Color(0x1E, 0x28, 0x20, 150);

    public static Font sans(int estilo, int tamano) {
        return new Font(Font.SANS_SERIF, estilo, tamano);
    }

    public static Font mono(int estilo, int tamano) {
        return new Font(Font.MONOSPACED, estilo, tamano);
    }
}
