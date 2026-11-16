package screen.components;

import screen.Formula;
import screen.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/** Gráfica de f(x) en la pantalla LCD, con el área entre a y b sombreada. */
public class PlotPanel extends JPanel {

    private List<double[]> puntos = List.of();
    private double a, b;

    public PlotPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(440, 300));
    }

    public void setDatos(List<double[]> puntos, double a, double b) {
        this.puntos = puntos;
        this.a = Math.min(a, b);
        this.b = Math.max(a, b);
        repaint();
    }

    public void limpiar() {
        puntos = List.of();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(Theme.INK.getRed(), Theme.INK.getGreen(), Theme.INK.getBlue(), 90));
        g2.drawRect(0, 0, w - 1, h - 1);

        if (puntos.size() < 2) {
            g2.setColor(Theme.INK_MUTED);
            g2.setFont(Theme.mono(Font.BOLD, 13));
            String msg = "GRÁFICA DE f(x)";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int padL = 50, padR = 12, padT = 12, padB = 40;
        int pw = w - padL - padR, ph = h - padT - padB;

        double xMin = puntos.get(0)[0], xMax = puntos.get(puntos.size() - 1)[0];
        if (xMax - xMin < 1e-12) xMax = xMin + 1;
        double[] rango = rangoY();
        double yMin = rango[0], yMax = rango[1];
        final double fxMin = xMin, fxMax = xMax;

        DoubleUnaryOperator sx = x -> padL + (x - fxMin) / (fxMax - fxMin) * pw;
        DoubleUnaryOperator sy = y -> padT + (yMax - y) / (yMax - yMin) * ph;

        g2.setFont(Theme.mono(Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        // cuadrícula y números del eje Y
        double pasoY = pasoBonito((yMax - yMin) / 5);
        for (double yv = Math.ceil(yMin / pasoY) * pasoY; yv <= yMax + 1e-12; yv += pasoY) {
            int yy = (int) sy.applyAsDouble(yv);
            g2.setColor(Theme.GRID);
            g2.drawLine(padL, yy, w - padR, yy);
            g2.setColor(Theme.INK_MUTED);
            String t = Formula.numero(Math.round(yv / pasoY) * pasoY);
            g2.drawString(t, padL - 6 - fm.stringWidth(t), yy + 4);
        }
        // cuadrícula y números del eje X
        double pasoX = pasoBonito((xMax - xMin) / 6);
        for (double xv = Math.ceil(xMin / pasoX) * pasoX; xv <= xMax + 1e-12; xv += pasoX) {
            int xx = (int) sx.applyAsDouble(xv);
            g2.setColor(Theme.GRID);
            g2.drawLine(xx, padT, xx, padT + ph);
            g2.setColor(Theme.INK_MUTED);
            String t = Formula.numero(Math.round(xv / pasoX) * pasoX);
            g2.drawString(t, xx - fm.stringWidth(t) / 2, padT + ph + 14);
        }
        // ejes
        g2.setColor(Theme.AXIS);
        int y0 = (int) sy.applyAsDouble(0);
        if (y0 >= padT && y0 <= padT + ph) g2.drawLine(padL, y0, w - padR, y0);
        int x0 = (int) sx.applyAsDouble(0);
        if (x0 >= padL && x0 <= padL + pw) g2.drawLine(x0, padT, x0, padT + ph);

        // área sombreada entre a y b
        Shape clip = g2.getClip();
        g2.clipRect(padL, padT, pw, ph);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int base = (int) sy.applyAsDouble(Math.max(yMin, Math.min(yMax, 0)));
        for (int i = 0; i < puntos.size() - 1; i++) {
            double[] p = puntos.get(i), q = puntos.get(i + 1);
            if (Double.isNaN(p[1]) || Double.isNaN(q[1])) continue;
            if (q[0] < a - 1e-12 || p[0] > b + 1e-12) continue;
            double x1 = Math.max(p[0], a), x2 = Math.min(q[0], b);
            if (x2 <= x1) continue;
            double y1 = interp(p, q, x1), y2 = interp(p, q, x2);
            Path2D.Double poly = new Path2D.Double();
            poly.moveTo(sx.applyAsDouble(x1), base);
            poly.lineTo(sx.applyAsDouble(x1), sy.applyAsDouble(y1));
            poly.lineTo(sx.applyAsDouble(x2), sy.applyAsDouble(y2));
            poly.lineTo(sx.applyAsDouble(x2), base);
            poly.closePath();
            g2.setColor((y1 + y2) >= 0 ? Theme.AREA_POS : Theme.AREA_NEG);
            g2.fill(poly);
        }

        // curva
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Theme.CURVE);
        Path2D.Double curva = new Path2D.Double();
        boolean dibujando = false;
        for (double[] p : puntos) {
            if (Double.isNaN(p[1]) || Math.abs(p[1]) > 1e6) {
                dibujando = false;
                continue;
            }
            double px = sx.applyAsDouble(p[0]), py = sy.applyAsDouble(p[1]);
            if (!dibujando) curva.moveTo(px, py);
            else curva.lineTo(px, py);
            dibujando = true;
        }
        g2.draw(curva);
        g2.setClip(clip);

        // marcas a y b
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 3f}, 0f));
        g2.setFont(Theme.mono(Font.BOLD, 12));
        int xa = (int) sx.applyAsDouble(a), xb = (int) sx.applyAsDouble(b);
        int yTexto = padT + ph + 31;
        if (Math.abs(b - a) < 1e-12) {
            marcar(g2, xa, padT, ph, "a = b = " + corto(a), 0, yTexto, w);
        } else {
            marcar(g2, xa, padT, ph, "a = " + corto(a), -1, yTexto, w);
            marcar(g2, xb, padT, ph, "b = " + corto(b), 1, yTexto, w);
        }
        g2.dispose();
    }

    private static String corto(double v) {
        return Formula.numero(Math.round(v * 1e4) / 1e4);
    }

    /** lado: -1 texto a la izquierda de la línea, 1 a la derecha, 0 centrado. */
    private void marcar(Graphics2D g2, int x, int top, int alto, String texto, int lado, int yTexto, int w) {
        g2.setColor(Theme.MARK);
        g2.drawLine(x, top, x, top + alto);
        int tw = g2.getFontMetrics().stringWidth(texto);
        int tx = lado < 0 ? x - tw - 4 : lado > 0 ? x + 4 : x - tw / 2;
        tx = Math.max(4, Math.min(w - tw - 4, tx));
        g2.drawString(texto, tx, yTexto);
    }

    private static double interp(double[] p, double[] q, double x) {
        if (q[0] == p[0]) return p[1];
        return p[1] + (q[1] - p[1]) * (x - p[0]) / (q[0] - p[0]);
    }

    private static double pasoBonito(double bruto) {
        if (!(bruto > 0) || !Double.isFinite(bruto)) return 1;
        double exp = Math.pow(10, Math.floor(Math.log10(bruto)));
        double f = bruto / exp;
        double n = f < 1.5 ? 1 : f < 3 ? 2 : f < 7 ? 5 : 10;
        return n * exp;
    }

    /** Rango vertical robusto: ignora picos (asíntotas) y siempre incluye y = 0. */
    private double[] rangoY() {
        List<Double> ys = new ArrayList<>();
        for (double[] p : puntos) if (!Double.isNaN(p[1]) && Double.isFinite(p[1])) ys.add(p[1]);
        if (ys.isEmpty()) return new double[]{-1, 1};
        double[] v = ys.stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(v);
        double lo = v[(int) (v.length * 0.02)], hi = v[(int) Math.min(v.length - 1, v.length * 0.98)];
        lo = Math.min(lo, 0);
        hi = Math.max(hi, 0);
        if (hi - lo < 1e-9) {
            hi += 1;
            lo -= 1;
        }
        double m = (hi - lo) * 0.1;
        return new double[]{lo - m, hi + m};
    }
}
