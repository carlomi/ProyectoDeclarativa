package screen;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Convierte la notación de Prolog a una notación matemática más legible. */
public final class Formula {

    private Formula() {
    }

    private static final String NORMAL = "0123456789-+x()";
    private static final String SUPER = "⁰¹²³⁴⁵⁶⁷⁸⁹⁻⁺ˣ⁽⁾";

    private static final Pattern EXP_PAR = Pattern.compile("\\^\\(([0-9x+\\-]+)\\)");
    private static final Pattern EXP_NUM = Pattern.compile("\\^\\s*(-?\\d+)(?![.\\d])");
    private static final Pattern EXP_X = Pattern.compile("\\^(x)\\b");

    public static String bonita(String s) {
        String r = s.replace("- (", "-(").replace("^ -", "^-").replace("+ -", "- ");
        r = r.replaceAll("\\ba(sin|cos|tan)(h?)\\(", "arc$1$2(");
        r = r.replaceAll("log\\(abs\\(((?:[^()]|\\([^()]*\\))*)\\)\\)", "ln|$1|");
        r = r.replace("log(", "ln(").replace("sqrt(", "√(");
        r = r.replace("exp(x)", "eˣ");
        r = superindices(EXP_PAR, r);
        r = superindices(EXP_NUM, r);
        r = superindices(EXP_X, r);
        r = r.replace("*", "·");
        r = r.replaceAll("(?<=[\\w)ˣ⁰¹²³⁴⁵⁶⁷⁸⁹])([+\\-])(?=[\\w(√])", " $1 ");
        return r;
    }

    private static String superindices(Pattern p, String s) {
        Matcher m = p.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(sup(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String sup(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            int i = NORMAL.indexOf(c);
            sb.append(i >= 0 ? SUPER.charAt(i) : c);
        }
        return sb.toString();
    }

    /** Formatea un número sin ceros de más (máx. 8 decimales). */
    public static String numero(double v) {
        if (Math.abs(v) < 1e-10) v = 0;
        String t = String.format(Locale.ROOT, "%.8f", v);
        t = t.replaceAll("0+$", "").replaceAll("\\.$", "");
        return t.equals("-0") ? "0" : t;
    }
}
