package edu.sjsu.team10.util;

public final class Html {

    private Html() { }

    // escape text before printing it in a JSP so user-entered values can't inject HTML/scripts
    public static String escape(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();
        StringBuilder out = new StringBuilder(text.length());

        for (char c : text.toCharArray()) {
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }

        return out.toString();
    }
}
