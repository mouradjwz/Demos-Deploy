package elections.demos.backend.utils;

import java.util.HashMap;
import java.util.Map;

public class PartyColorUtils {

    // Predefined colors for major Dutch political parties
    private static final Map<String, String> PARTY_COLORS = createPartyColors();

    private static Map<String, String> createPartyColors() {
        Map<String, String> colors = new HashMap<>();

        // Major parties with their official colors
        colors.put("VVD", "#1A4788");      // Blue
        colors.put("D66", "#FFE600");      // Yellow
        colors.put("PVV", "#464646");      // Dark Gray
        colors.put("CDA", "#095797");      // Dark Blue
        colors.put("SP", "#E30613");       // Red
        colors.put("PVD", "#FF0000");      // Bright Red
        colors.put("GL", "#6CAF52");       // Green
        colors.put("FVD", "#7839A3");      // Purple
        colors.put("PVD", "#8BC34A");      // Light Green
        colors.put("CU", "#8BC34A");       // Green
        colors.put("VOLT", "#502379");     // Purple
        colors.put("JA21", "#1A4788");     // Blue
        colors.put("SGP", "#7839A3");      // Purple
        colors.put("DENK", "#FFC107");     // Orange
        colors.put("BBB", "#8BC34A");      // Green
        colors.put("50PLUS", "#9C27B0");   // Purple
        colors.put("BIJ1", "#000000");     // Black
        colors.put("NSC", "#808080");      // Gray
        colors.put("LP", "#FF9800");       // Orange
        colors.put("OPRECHT", "#607D8B");  // Blue Gray
        colors.put("SPLINTER", "#795548"); // Brown

        // Default colors based on party name patterns
        colors.put("_CHRISTEN", "#7839A3");    // Purple for Christian parties
        colors.put("_SOCIALIST", "#E30613");   // Red for socialist parties
        colors.put("_LIBERAL", "#1A4788");     // Blue for liberal parties
        colors.put("_GREEN", "#6CAF52");       // Green for green parties
        colors.put("_ANIMAL", "#8BC34A");      // Light green for animal parties

        return colors;
    }

    // Color palette for unknown parties (will cycle through these)
    private static final String[] COLOR_PALETTE = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
            "#F8C471", "#82E0AA", "#F9E79F", "#D7BDE2", "#A9CCE3"
    };

    private static final Map<String, String> generatedColors = new HashMap<>();
    private static int colorIndex = 0;

    /**
     * Get consistent color for a political party
     */
    public static String getPartyColor(String partyName) {
        if (partyName == null || partyName.trim().isEmpty()) {
            return getNextColorFromPalette();
        }

        String normalizedName = normalizePartyName(partyName);

        // Check exact match first
        if (PARTY_COLORS.containsKey(normalizedName)) {
            return PARTY_COLORS.get(normalizedName);
        }

        // Check for partial matches based on party name patterns
        for (Map.Entry<String, String> entry : PARTY_COLORS.entrySet()) {
            if (entry.getKey().startsWith("_") && normalizedName.contains(entry.getKey().substring(1).toLowerCase())) {
                return entry.getValue();
            }
        }

        // Use generated color for consistency
        return generatedColors.computeIfAbsent(normalizedName, k -> getNextColorFromPalette());
    }

    /**
     * Normalize party name for consistent matching
     */
    private static String normalizePartyName(String partyName) {
        if (partyName == null) return "";

        return partyName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .replaceAll("PARTIJ", "")
                .replaceAll("PARTIJVOOR", "")
                .trim();
    }

    /**
     * Get next color from palette for unknown parties
     */
    private static synchronized String getNextColorFromPalette() {
        String color = COLOR_PALETTE[colorIndex % COLOR_PALETTE.length];
        colorIndex++;
        return color;
    }

    /**
     * Get all party colors (for debugging)
     */
    public static Map<String, String> getAllPartyColors() {
        return new HashMap<>(PARTY_COLORS);
    }
}