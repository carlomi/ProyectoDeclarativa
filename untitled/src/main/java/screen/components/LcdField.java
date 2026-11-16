package screen.components;

import screen.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Línea editable de la pantalla LCD: "f(x)= ...", "a= ...", "b= ...".
 * La etiqueta se invierte (fondo oscuro) cuando la línea está activa.
 */
public class LcdField extends JPanel {

    private final JLabel etiqueta;
    private final JTextField campo = new JTextField();

    public LcdField(String nombre, int anchoEtiqueta) {
        super(new BorderLayout(8, 0));
        setOpaque(false);

        etiqueta = new JLabel(nombre, SwingConstants.CENTER);
        etiqueta.setFont(Theme.mono(Font.BOLD, 17));
        etiqueta.setForeground(Theme.INK);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(Theme.LCD);
        etiqueta.setPreferredSize(new Dimension(anchoEtiqueta, 30));
        etiqueta.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        etiqueta.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                activar();
            }
        });

        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        campo.setFont(Theme.mono(Font.BOLD, 19));
        campo.setForeground(Theme.INK);
        campo.setCaretColor(Theme.INK);
        campo.setSelectionColor(Theme.INK);
        campo.setSelectedTextColor(Theme.LCD);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                marcar(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                marcar(false);
            }
        });

        add(etiqueta, BorderLayout.WEST);
        add(campo, BorderLayout.CENTER);
    }

    private void marcar(boolean activo) {
        etiqueta.setBackground(activo ? Theme.INK : Theme.LCD);
        etiqueta.setForeground(activo ? Theme.LCD : Theme.INK);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // subrayado punteado tipo LCD
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(campo.hasFocus() ? Theme.INK : Theme.INK_MUTED);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{2f, 3f}, 0f));
        int x = campo.getX();
        g2.drawLine(x, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
    }

    public void activar() {
        campo.requestFocusInWindow();
    }

    public JTextField campo() {
        return campo;
    }

    public String getText() {
        return campo.getText();
    }

    public void setText(String t) {
        campo.setText(t);
    }
}
