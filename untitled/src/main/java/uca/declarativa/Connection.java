package uca.declarativa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Puente entre la interfaz Java y la base de conocimiento en SWI-Prolog.
 * Ejecuta: swipl -q -g api -t halt proyecto_final.pl -- FUNCION A B
 * Los datos van como argumentos del proceso (no se arma código Prolog con texto del usuario).
 */
public class Connection {

    /** Resultado de una consulta a la base de conocimiento. */
    public record Resultado(String integral, double area, double fa, double fb, double a, double b, List<double[]> puntos) {
    }

    private final String swipl;
    private final Path baseConocimiento;

    public Connection() throws IOException {
        this.swipl = buscarSwipl();
        this.baseConocimiento = extraerBase();
    }

    public String getSwipl() {
        return swipl;
    }

    public Resultado consultar(String funcion, String a, String b) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(swipl, "-q", "-g", "api", "-t", "halt",
                baseConocimiento.toString(), "--", funcion, a, b);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] salida;
        try (InputStream in = p.getInputStream()) {
            salida = in.readAllBytes();
        }
        if (!p.waitFor(15, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("Prolog tardó demasiado en responder");
        }

        String integral = null;
        Double area = null;
        double a0 = 0, b0 = 0, fa = 0, fb = 0;
        List<double[]> puntos = new ArrayList<>();
        for (String linea : new String(salida, StandardCharsets.UTF_8).split("\\R")) {
            String[] partes = linea.split("\t", 2);
            switch (partes[0]) {
                case "ERR" -> throw new IllegalArgumentException(partes.length > 1 ? partes[1] : "Error");
                case "INT" -> integral = partes[1].trim();
                case "AREA" -> area = Double.parseDouble(partes[1].trim());
                case "FA" -> fa = Double.parseDouble(partes[1].trim());
                case "FB" -> fb = Double.parseDouble(partes[1].trim());
                case "LIM" -> {
                    String[] ab = partes[1].trim().split("\\s+");
                    a0 = Double.parseDouble(ab[0]);
                    b0 = Double.parseDouble(ab[1]);
                }
                case "PTS" -> {
                    for (String par : partes[1].trim().split("\\s+")) {
                        String[] xy = par.split(":");
                        if (xy.length != 2) continue;
                        double x = Double.parseDouble(xy[0]);
                        double y = xy[1].equals("nan") ? Double.NaN : Double.parseDouble(xy[1]);
                        puntos.add(new double[]{x, y});
                    }
                }
                default -> { }
            }
        }
        if (integral == null || area == null) {
            throw new IllegalStateException("Respuesta inesperada de Prolog:\n" + new String(salida, StandardCharsets.UTF_8));
        }
        return new Resultado(integral, area, fa, fb, a0, b0, puntos);
    }

    /** Copia el .pl de resources a un archivo temporal para que swipl lo pueda cargar. */
    private Path extraerBase() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/proyecto_final.pl")) {
            if (in == null) throw new IllegalStateException("No se encontró proyecto_final.pl");
            Path tmp = Files.createTempFile("proyecto_final", ".pl");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            return tmp;
        }
    }

    /** Busca el ejecutable de SWI-Prolog: variable SWIPL, PATH o rutas típicas. */
    private static String buscarSwipl() {
        String env = System.getenv("SWIPL");
        if (env != null && new File(env).canExecute()) return env;

        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        String exe = windows ? "swipl.exe" : "swipl";
        for (String dir : System.getenv().getOrDefault("PATH", "").split(File.pathSeparator)) {
            File f = new File(dir, exe);
            if (f.canExecute()) return f.getAbsolutePath();
        }
        String[] candidatos = windows
                ? new String[]{"C:\\Program Files\\swipl\\bin\\swipl.exe", "C:\\Program Files (x86)\\swipl\\bin\\swipl.exe"}
                : new String[]{"/usr/bin/swipl", "/usr/local/bin/swipl", "/opt/homebrew/bin/swipl",
                "/Applications/SWI-Prolog.app/Contents/MacOS/swipl"};
        for (String c : candidatos) {
            if (new File(c).canExecute()) return c;
        }
        throw new IllegalStateException("No se encontró SWI-Prolog (swipl). Instálalo o define la variable SWIPL.");
    }
}
