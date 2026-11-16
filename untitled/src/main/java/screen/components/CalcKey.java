package screen.components;

import screen.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tecla de calculadora: cuerpo con relieve y, opcionalmente, la función
 * secundaria (2ND) escrita encima en amarillo.
 */
public class CalcKey extends JButton {

    public enum Estilo {
        FUNCION(Theme.KEY_FUNC, Theme.KEY_TEXT_LIGHT),
        NUMERO(Theme.KEY_NUM, Theme.KEY_TEXT_DARK),
        OPERADOR(Theme.KEY_OP, Theme.KEY_TEXT_LIGHT),
        CONTROL(Theme.KEY_CTRL, Theme.KEY_TEXT_LIGHT),
        VARIABLE(Theme.KEY_VAR, Theme.KEY_TEXT_LIGHT),
        ENTER(Theme.KEY_ENTER, Theme.KEY_TEXT_LIGHT),
        CLEAR(Theme.KEY_CLEAR, Theme.KEY_TEXT_LIGHT),
        SEGUNDA(Theme.KEY_2ND, Theme.KEY_TEXT_DARK);

        final Color fondo, texto;

        Estilo(Color fondo, Color texto) {
            this.fondo = fondo;
            this.texto = texto;
        }
    }

    public static final int ALTO_ETIQUETA = 15;

    private final String segunda;
    private final Estilo estilo;
    private boolean hover;
    private boolean encendida;

    public CalcKey(String texto, String segunda, Estilo estilo) {
        super(texto);
        this.segunda = segunda;
        this.estilo = estilo;
        setFocusable(false);           // el foco se queda en el campo que se está editando
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(estilo.texto);
        setFont(Theme.sans(Font.BOLD, texto.length() > 4 ? 13 : 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(64, 44 + ALTO_ETIQUETA));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public String getSegunda() {
        return segunda;
    }

    /** Para la tecla 2ND: se ve "encendida" mientras está activa. */
    public void setEncendida(boolean encendida) {
        this.encendida = encendida;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int top = ALTO_ETIQUETA;
        boolean pulsada = getModel().isPressed() || encendida;
        int dy = pulsada ? 2 : 0;
        int arc = 12;

        // función secundaria (encima de la tecla)
        if (segunda != null) {
            g2.setFont(Theme.sans(Font.BOLD, 10));
            g2.setColor(Theme.KEY_2ND);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(segunda, (w - fm.stringWidth(segunda)) / 2, top - 4);
        }

        Color base = estilo.fondo;
        if (hover && !pulsada) base = aclarar(base, 0.10f);
        if (encendida) base = aclarar(base, 0.18f);

        // sombra / canto inferior
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(1, top + 3, w - 2, h - top - 3, arc, arc);
        g2.setColor(oscurecer(base, 0.35f));
        g2.fillRoundRect(1, top + 1 + dy, w - 2, h - top - 3, arc, arc);
        // cara de la tecla
        g2.setPaint(new GradientPaint(0, top + dy, aclarar(base, 0.08f), 0, h - 4 + dy, base));
        g2.fillRoundRect(1, top + dy, w - 2, h - top - 6, arc, arc);
        g2.setColor(new Color(255, 255, 255, estilo == Estilo.NUMERO ? 140 : 35));
        g2.drawRoundRect(1, top + dy, w - 3, h - top - 7, arc, arc);

        // texto principal
        g2.setFont(getFont());
        g2.setColor(isEnabled() ? estilo.texto : estilo.texto.darker());
        FontMetrics fm = g2.getFontMetrics();
        String t = getText();
        int ty = top + dy + (h - top - 6 + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(t, (w - fm.stringWidth(t)) / 2, ty);
        g2.dispose();
    }

    private static Color aclarar(Color c, float f) {
        return new Color(Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * f)),
                Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * f)),
                Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * f)));
    }

    private static Color oscurecer(Color c, float f) {
        return new Color((int) (c.getRed() * (1 - f)), (int) (c.getGreen() * (1 - f)), (int) (c.getBlue() * (1 - f)));
    }
}
