package com.example.focovest.model;

import android.graphics.Color;

public enum SubjectId {
    por("por", "Português", "#DB2777", "#FBDCEB"),
    art("art", "Arte", "#E11D48", "#FFE4E6"),
    ing("ing", "Inglês", "#0284C7", "#E0F2FE"),
    mat("mat", "Matemática", "#FF5A1F", "#FFE4D8"),
    fis("fis", "Física", "#2F5BFF", "#DDE5FF"),
    qui("qui", "Química", "#0E9F6E", "#D5F3E6"),
    bio("bio", "Biologia", "#8B5CF6", "#EBE2FF"),
    his("his", "História", "#C2410C", "#F8E3D2"),
    geo("geo", "Geografia", "#0F766E", "#D3F1EC"),
    fil("fil", "Filosofia", "#7C3AED", "#EDE9FE"),
    soc("soc", "Sociologia", "#B45309", "#FEF3C7");

    public final String code;
    public final String name;
    public final String colorHex;
    public final String softHex;

    SubjectId(String code, String name, String colorHex, String softHex) {
        this.code = code;
        this.name = name;
        this.colorHex = colorHex;
        this.softHex = softHex;
    }

    public int getColorInt() {
        return Color.parseColor(colorHex);
    }

    public int getSoftColorInt() {
        return Color.parseColor(softHex);
    }

    public static SubjectId fromCode(String code) {
        if (code == null) return mat;
        for (SubjectId s : values()) {
            if (s.code.equalsIgnoreCase(code.trim())) {
                return s;
            }
        }
        return mat;
    }
}
