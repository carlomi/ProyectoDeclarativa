package screen;

import screen.components.CalcKey;
import screen.components.CalcKey.Estilo;
import screen.components.LcdField;
import screen.components.PlotPanel;
import uca.declarativa.Connection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/** Interfaz con aspecto de calculadora científica / graficadora. */
public class Window extends JFrame {

    private final Connection motor;

    // pantalla
    private final LcdField campoF = new LcdField("f(x)=", 64);
    private final LcdField campoA = new LcdField("a=", 64);
    private final LcdField campoB = new LcdField("b=", 64);
    private final List<LcdField> campos = List.of(campoF, campoA, campoB);
    private JTextField activo = campoF.campo();

    private final JTextArea txtIntegral = new JTextArea();
    private final JLabel lblFb = lcdLabel(" ", 15, false);
    private final JLabel lblFa = lcdLabel(" ", 15, false);
    private final JLabel lblArea = lcdLabel("ÁREA = ", 26, true);
    private final JLabel lblMensaje = lcdLabel(" ", 13, true);
    private final JLabel lblIndicador2nd = lcdLabel("    ", 12, true);
    private final JLabel lblEstado = lcdLabel("LISTO", 12, true);
    private final PlotPanel grafica = new PlotPanel();

    // teclado
    private CalcKey tecla2nd;
    private boolean modo2nd;
    private String textoCopiable = "";

    private static final String[][] REGLAS = {
            {"a", "∫ k dx", "k·x"},
            {"b", "∫ x dx", "x²/2"},
            {"c", "∫ k·x dx", "k·x²/2"},
            {"d", "∫ xⁿ dx", "xⁿ⁺¹/(n+1),  n = −1 → ln|x|"},
            {"e", "∫ u dx", "se descompone con las demás reglas"},
            {"f", "∫ k·u dx", "k·∫ u dx"},
            {"g", "∫ (u + v) dx", "∫ u dx + ∫ v dx"},
            {"h", "∫ (u − v) dx", "∫ u dx − ∫ v dx"},
            {"i", "∫ aˣ dx", "aˣ / ln a"},
            {"j", "∫ aᵘ dx  (u = kx + c)", "aᵘ / (k·ln a)"},
            {"k", "∫ sin x dx", "−cos x"},
            {"l", "∫ cos x dx", "sin x"},
            {"m", "∫ tan x dx", "−ln|cos x|"},
            {"n", "∫ arcsin x dx", "x·arcsin x + √(1−x²)"},
            {"o", "∫ arccos x dx", "x·arccos x − √(1−x²)"},
            {"p", "∫ arctan x dx", "x·arctan x − ln(1+x²)/2"},
            {"q", "∫ sh x dx", "cosh x"},
            {"r", "∫ cosh x dx", "sinh x"},
            {"s", "∫ tanh x dx", "ln(cosh x)"},
            {"t", "∫ arcsinh x dx", "x·arcsinh x − √(x²+1)"},
            {"u", "∫ arccosh x dx", "x·arccosh x − √(x²−1)"},
            {"v", "∫ arctanh x dx", "x·arctanh x + ln(1−x²)/2"},
    };

    public Window(Connection motor) {
        super("Integrales en Prolog");
        this.motor = motor;

        Carcasa cuerpo = new Carcasa();
        cuerpo.setLayout(new BorderLayout(0, 18));
        cuerpo.setBorder(new EmptyBorder(18, 26, 26, 26));
        cuerpo.add(crearMarca(), BorderLayout.NORTH);
        cuerpo.add(crearPantalla(), BorderLayout.CENTER);
        cuerpo.add(crearTeclado(), BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Theme.DESK);
        raiz.setBorder(new EmptyBorder(18, 18, 18, 18));
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);

        configurarCampos();
        setMinimumSize(new Dimension(1000, 860));
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        limpiarResultado();
        SwingUtilities.invokeLater(campoF::activar);
    }

    // ================================================================== marca superior
    private JPanel crearMarca() {
        JLabel marca = new JLabel("∫ INTEGRA·PL");
        marca.setFont(Theme.sans(Font.BOLD | Font.ITALIC, 20));
        marca.setForeground(Theme.BODY_TEXT);
        JLabel modelo = new JLabel("CALCULADORA DE INTEGRALES  ·  SISTEMA EXPERTO SWI-PROLOG");
        modelo.setFont(Theme.sans(Font.BOLD, 11));
        modelo.setForeground(Theme.BODY_MUTED);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(marca, BorderLayout.WEST);
        p.add(modelo, BorderLayout.EAST);
        return p;
    }

    // ================================================================== pantalla LCD
    private JComponent crearPantalla() {
        Lcd lcd = new Lcd();
        lcd.setLayout(new BorderLayout(0, 8));
        lcd.setBorder(new EmptyBorder(10, 16, 14, 16));

        // barra de estado de la pantalla
        JPanel estado = new JPanel(new BorderLayout());
        estado.setOpaque(false);
        JLabel modo = lcdLabel("RAD   FUNC   ∫dx", 12, true);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(modo);
        izq.add(Box.createHorizontalStrut(18));
        izq.add(lblIndicador2nd);
        estado.add(izq, BorderLayout.WEST);
        estado.add(lblEstado, BorderLayout.EAST);
        lcd.add(estado, BorderLayout.NORTH);

        // columna de texto
        JPanel texto = new JPanel(new GridBagLayout());
        texto.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 6, 0);
        for (LcdField c : campos) {
            texto.add(c, g);
            g.gridy++;
        }
        g.insets = new Insets(6, 0, 2, 0);
        texto.add(lcdLabel("∫ f(x) dx =", 13, true), g);
        g.gridy++;
        txtIntegral.setEditable(false);
        txtIntegral.setFocusable(false);
        txtIntegral.setOpaque(false);
        txtIntegral.setLineWrap(true);
        txtIntegral.setFont(Theme.mono(Font.BOLD, 17));
        txtIntegral.setForeground(Theme.INK);
        txtIntegral.setRows(2);
        g.insets = new Insets(0, 0, 6, 0);
        texto.add(txtIntegral, g);
        g.gridy++;
        g.insets = new Insets(0, 0, 0, 0);
        texto.add(lblFb, g);
        g.gridy++;
        texto.add(lblFa, g);
        g.gridy++;
        g.insets = new Insets(4, 0, 4, 0);
        texto.add(lblArea, g);
        g.gridy++;
        lblMensaje.setVerticalAlignment(SwingConstants.TOP);
        lblMensaje.setPreferredSize(new Dimension(10, 40));   // 2 líneas fijas: nada se mueve con errores largos
        texto.add(lblMensaje, g);
        g.gridy++;
        g.weighty = 1;
        texto.add(Box.createGlue(), g);

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 18, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(texto);
        cuerpo.add(grafica);
        lcd.add(cuerpo, BorderLayout.CENTER);
        lcd.setPreferredSize(new Dimension(900, 360));

        // bisel oscuro alrededor de la pantalla
        JPanel bisel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x141519));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(0x2C2E35));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        bisel.setOpaque(false);
        bisel.setBorder(new EmptyBorder(14, 14, 14, 14));
        bisel.add(lcd);
        return bisel;
    }

    // ================================================================== teclado
    private JComponent crearTeclado() {
        // bloque izquierdo: funciones y control
        JPanel funciones = new JPanel(new GridLayout(5, 5, 10, 4));
        funciones.setOpaque(false);

        tecla2nd = tecla(funciones, "2ND", null, Estilo.SEGUNDA, this::alternar2nd);
        tecla(funciones, "sin", "sin⁻¹", Estilo.FUNCION, () -> insertarFuncion("sin(", "asin("));
        tecla(funciones, "cos", "cos⁻¹", Estilo.FUNCION, () -> insertarFuncion("cos(", "acos("));
        tecla(funciones, "tan", "tan⁻¹", Estilo.FUNCION, () -> insertarFuncion("tan(", "atan("));
        tecla(funciones, "ln", "eˣ", Estilo.FUNCION, () -> insertarFuncion("ln(", "e^("));

        tecla(funciones, "x⁻¹", null, Estilo.FUNCION, () -> insertar("^(-1)"));
        tecla(funciones, "sinh", "sinh⁻¹", Estilo.FUNCION, () -> insertarFuncion("sinh(", "asinh("));
        tecla(funciones, "cosh", "cosh⁻¹", Estilo.FUNCION, () -> insertarFuncion("cosh(", "acosh("));
        tecla(funciones, "tanh", "tanh⁻¹", Estilo.FUNCION, () -> insertarFuncion("tanh(", "atanh("));
        tecla(funciones, "π", null, Estilo.FUNCION, () -> insertar("pi"));

        tecla(funciones, "x²", null, Estilo.FUNCION, () -> insertar("^2"));
        tecla(funciones, "^", null, Estilo.FUNCION, () -> insertar("^"));
        tecla(funciones, "(", null, Estilo.FUNCION, () -> insertar("("));
        tecla(funciones, ")", null, Estilo.FUNCION, () -> insertar(")"));
        tecla(funciones, "e", null, Estilo.FUNCION, () -> insertar("e"));

        tecla(funciones, "X", null, Estilo.VARIABLE, () -> insertar("x"));
        tecla(funciones, "f(x)", null, Estilo.CONTROL, campoF::activar);
        tecla(funciones, "a", null, Estilo.CONTROL, campoA::activar);
        tecla(funciones, "b", null, Estilo.CONTROL, campoB::activar);
        tecla(funciones, "DEL", null, Estilo.CONTROL, this::borrar);

        tecla(funciones, "◄", null, Estilo.CONTROL, () -> moverCursor(-1));
        tecla(funciones, "►", null, Estilo.CONTROL, () -> moverCursor(1));
        tecla(funciones, "REGLAS", null, Estilo.CONTROL, this::mostrarReglas);
        tecla(funciones, "COPIAR", null, Estilo.CONTROL, this::copiarResultado);
        tecla(funciones, "CLEAR", null, Estilo.CLEAR, this::limpiar);

        // bloque derecho: números, operadores y ENTER
        JPanel numeros = new JPanel(new GridBagLayout());
        numeros.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        g.weighty = 1;
        g.insets = new Insets(2, 5, 2, 5);
        String[][] filas = {
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "−"},
                {"0", ".", "(−)", "+"},
        };
        String[][] inserta = {
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "-", "+"},
        };
        for (int f = 0; f < filas.length; f++) {
            for (int c = 0; c < 4; c++) {
                g.gridx = c;
                g.gridy = f;
                boolean op = c == 3;
                String ins = inserta[f][c];
                CalcKey k = new CalcKey(filas[f][c], null, op ? Estilo.OPERADOR : Estilo.NUMERO);
                k.setFont(Theme.sans(Font.BOLD, 18));
                k.addActionListener(e -> insertar(ins));
                numeros.add(k, g);
            }
        }
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 4;
        CalcKey enter = new CalcKey("ENTER  =", null, Estilo.ENTER);
        enter.setFont(Theme.sans(Font.BOLD, 16));
        enter.addActionListener(e -> ejecutar());
        numeros.add(enter, g);

        JPanel teclado = new JPanel(new GridBagLayout());
        teclado.setOpaque(false);
        GridBagConstraints t = new GridBagConstraints();
        t.fill = GridBagConstraints.BOTH;
        t.weighty = 1;
        t.gridy = 0;
        t.gridx = 0;
        t.weightx = 5;
        teclado.add(funciones, t);
        t.gridx = 1;
        t.weightx = 0;
        teclado.add(Box.createHorizontalStrut(28), t);
        t.gridx = 2;
        t.weightx = 4;
        teclado.add(numeros, t);
        return teclado;
    }

    private CalcKey tecla(JPanel panel, String texto, String segunda, Estilo estilo, Runnable accion) {
        CalcKey k = new CalcKey(texto, segunda, estilo);
        k.addActionListener(e -> accion.run());
        panel.add(k);
        return k;
    }

    // ================================================================== edición
    private void configurarCampos() {
        for (int i = 0; i < campos.size(); i++) {
            JTextField c = campos.get(i).campo();
            int idx = i;
            c.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    activo = c;
                }
            });
            c.addActionListener(e -> ejecutar());   // Enter calcula
            InputMap im = c.getInputMap();
            ActionMap am = c.getActionMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "siguiente");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "anterior");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiar");
            am.put("siguiente", accion(() -> campos.get((idx + 1) % campos.size()).activar()));
            am.put("anterior", accion(() -> campos.get((idx + campos.size() - 1) % campos.size()).activar()));
            am.put("limpiar", accion(this::limpiar));
        }
    }

    private static Action accion(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
    }

    private void insertar(String s) {
        activo.replaceSelection(s);
        activo.requestFocusInWindow();
    }

    private void insertarFuncion(String normal, String segunda) {
        insertar(modo2nd ? segunda : normal);
        if (modo2nd) alternar2nd();
    }

    private void alternar2nd() {
        modo2nd = !modo2nd;
        tecla2nd.setEncendida(modo2nd);
        lblIndicador2nd.setText(modo2nd ? " 2ND " : "    ");
        lblIndicador2nd.setOpaque(modo2nd);
        lblIndicador2nd.setBackground(Theme.INK);
        lblIndicador2nd.setForeground(modo2nd ? Theme.LCD : Theme.INK);
        activo.requestFocusInWindow();
    }

    private void borrar() {
        if (activo.getSelectionStart() != activo.getSelectionEnd()) {
            activo.replaceSelection("");
        } else {
            int p = activo.getCaretPosition();
            if (p > 0) {
                String t = activo.getText();
                activo.setText(t.substring(0, p - 1) + t.substring(p));
                activo.setCaretPosition(p - 1);
            }
        }
        activo.requestFocusInWindow();
    }

    private void moverCursor(int d) {
        int p = Math.max(0, Math.min(activo.getText().length(), activo.getCaretPosition() + d));
        activo.setCaretPosition(p);
        activo.requestFocusInWindow();
    }

    /** CLEAR: borra la línea activa; si ya estaba vacía, borra todo. */
    private void limpiar() {
        if (!activo.getText().isEmpty()) {
            activo.setText("");
        } else {
            campos.forEach(c -> c.setText(""));
            limpiarResultado();
            campoF.activar();
            return;
        }
        activo.requestFocusInWindow();
    }

    // ================================================================== cálculo
    private void ejecutar() {
        String f = balancear(campoF.getText().trim());
        campoF.setText(f);
        String a = campoA.getText().trim();
        String b = campoB.getText().trim();
        if (f.isEmpty()) {
            error("ESCRIBE f(x)");
            campoF.activar();
            return;
        }
        if (a.isEmpty()) {
            error("FALTA EL LÍMITE a");
            campoA.activar();
            return;
        }
        if (b.isEmpty()) {
            error("FALTA EL LÍMITE b");
            campoB.activar();
            return;
        }
        lblEstado.setText("CALCULANDO…");
        lblMensaje.setText(" ");

        new SwingWorker<Connection.Resultado, Void>() {
            @Override
            protected Connection.Resultado doInBackground() throws Exception {
                return motor.consultar(f, a, b);
            }

            @Override
            protected void done() {
                try {
                    mostrar(f, get());
                } catch (Exception ex) {
                    Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                    limpiarResultado();
                    error(c.getMessage());
                }
            }
        }.execute();
    }

    private void mostrar(String f, Connection.Resultado r) {
        String integral = Formula.bonita(r.integral()) + " + C";
        String fb = Formula.numero(r.fb()), fa = Formula.numero(r.fa()), area = Formula.numero(r.area());
        txtIntegral.setText(integral);
        lblFb.setText("F(b) = F(" + Formula.numero(r.b()) + ") = " + fb);
        lblFa.setText("F(a) = F(" + Formula.numero(r.a()) + ") = " + fa);
        lblArea.setText("ÁREA = " + area);
        lblMensaje.setForeground(Theme.INK_MUTED);
        lblMensaje.setText("ÁREA = F(b) − F(a)");
        lblEstado.setText("LISTO ✓");
        grafica.setDatos(r.puntos(), r.a(), r.b());
        textoCopiable = "∫ (" + Formula.bonita(f) + ") dx = " + integral
                + "\nF(b) = " + fb + "\nF(a) = " + fa
                + "\nÁrea [" + campoA.getText().trim() + ", " + campoB.getText().trim() + "] = " + area;
    }

    private void limpiarResultado() {
        txtIntegral.setText("—");
        lblFb.setText("F(b) =");
        lblFa.setText("F(a) =");
        lblArea.setText("ÁREA =");
        lblMensaje.setText(" ");
        lblEstado.setText("LISTO");
        grafica.limpiar();
        textoCopiable = "";
    }

    private void error(String msg) {
        lblMensaje.setForeground(Theme.INK_ERROR);
        lblMensaje.setText("<html>ERR: " + msg.toUpperCase() + "</html>");
        lblEstado.setText("ERROR");
    }

    /** Cierra los paréntesis que el usuario dejó abiertos (como las calculadoras). */
    private static String balancear(String s) {
        int abiertos = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') abiertos++;
            else if (c == ')') abiertos--;
        }
        return abiertos > 0 ? s + ")".repeat(abiertos) : s;
    }

    private void copiarResultado() {
        if (textoCopiable.isEmpty()) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textoCopiable), null);
        lblEstado.setText("COPIADO");
        activo.requestFocusInWindow();
    }

    private void mostrarReglas() {
        JDialog d = new JDialog(this, "Reglas de la base de conocimiento", true);
        JPanel lista = new JPanel(new GridBagLayout());
        lista.setBackground(Theme.LCD);
        lista.setBorder(new EmptyBorder(16, 18, 16, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 18);
        for (int i = 0; i < REGLAS.length; i++) {
            g.gridy = i;
            for (int j = 0; j < 3; j++) {
                JLabel l = new JLabel(j == 0 ? REGLAS[i][0] + ")" : j == 2 ? "=  " + REGLAS[i][2] : REGLAS[i][1]);
                l.setFont(j == 0 ? Theme.mono(Font.BOLD, 13) : Theme.mono(Font.BOLD, 14));
                l.setForeground(j == 0 ? Theme.MARK : j == 1 ? Theme.INK : Theme.INK_MUTED);
                g.gridx = j;
                lista.add(l, g);
            }
        }
        JScrollPane sc = new JScrollPane(lista);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        d.setContentPane(sc);
        d.setSize(620, 640);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        activo.requestFocusInWindow();
    }

    private static JLabel lcdLabel(String texto, int tam, boolean negrita) {
        JLabel l = new JLabel(texto);
        l.setFont(Theme.mono(negrita ? Font.BOLD : Font.PLAIN, tam));
        l.setForeground(Theme.INK);
        return l;
    }

    // ================================================================== pintura
    /** Carcasa de la calculadora: plástico oscuro con degradado y canto. */
    private static class Carcasa extends JPanel {
        Carcasa() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(4, 8, w - 8, h - 8, 40, 40);
            g2.setPaint(new GradientPaint(0, 0, Theme.BODY_TOP, 0, h, Theme.BODY_BOTTOM));
            g2.fillRoundRect(0, 0, w - 4, h - 6, 40, 40);
            g2.setColor(Theme.BODY_EDGE);
            g2.drawRoundRect(0, 0, w - 5, h - 7, 40, 40);
            g2.dispose();
        }
    }

    /** Pantalla LCD con un leve degradado y sombra interior. */
    private static class Lcd extends JPanel {
        Lcd() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, Theme.LCD, 0, h, Theme.LCD_DARK));
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 45), 0, 10, new Color(0, 0, 0, 0)));
            g2.fillRoundRect(0, 0, w, 12, 10, 10);
            g2.dispose();
        }
    }
}
