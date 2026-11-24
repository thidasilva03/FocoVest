package com.example.focovest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ExamId {
    ENEM("ENEM", "SiSU", "Exame Nacional do Ensino Médio · UFAM, IFAM e SiSU"),
    PSC("PSC", "UFAM", "Processo Seletivo Contínuo · 3 etapas"),
    SIS("SIS", "UEA", "Sistema de Ingresso Seriado · 3 etapas"),
    Macro("Macro", "UEA", "Vestibular Macro · prova única");

    public final String id;
    public final String institution;
    public final String desc;

    ExamId(String id, String institution, String desc) {
        this.id = id;
        this.institution = institution;
        this.desc = desc;
    }

    /**
     * Retorna os vestibulares elegíveis de acordo com a situação no Ensino Médio:
     * - 1ª e 2ª séries do EM: PSC (1ª/2ª etapa), SIS (SIS 1/SIS 2) e ENEM (a partir do 1º ano).
     * - 3ª série do EM: PSC (3ª etapa), SIS 3, Macro e ENEM.
     * - Já concluiu o EM (inHighSchool == false): apenas Macro e ENEM.
     */
    public static List<ExamId> availableFor(boolean inHighSchool, int highSchoolYear) {
        if (!inHighSchool) {
            return new ArrayList<>(Arrays.asList(Macro, ENEM));
        }
        if (highSchoolYear == 1 || highSchoolYear == 2) {
            return new ArrayList<>(Arrays.asList(PSC, SIS, ENEM));
        }
        if (highSchoolYear == 3) {
            return new ArrayList<>(Arrays.asList(PSC, SIS, Macro, ENEM));
        }
        return new ArrayList<>();
    }

    /**
     * Remove vestibulares inelegíveis mantendo apenas os permitidos para o perfil escolar.
     */
    public static List<ExamId> filterEligible(List<ExamId> exams, boolean inHighSchool, int highSchoolYear) {
        List<ExamId> allowed = availableFor(inHighSchool, highSchoolYear);
        List<ExamId> filtered = new ArrayList<>();
        if (exams != null) {
            for (ExamId ex : exams) {
                if (allowed.contains(ex) && !filtered.contains(ex)) {
                    filtered.add(ex);
                }
            }
        }
        return filtered;
    }

    /**
     * Rótulo contextual exibindo a etapa dedicada do PSC e SIS conforme a série do Ensino Médio.
     */
    public String getDisplayLabel(boolean inHighSchool, int highSchoolYear) {
        if (inHighSchool && highSchoolYear >= 1 && highSchoolYear <= 3) {
            if (this == PSC) {
                return "PSC · " + highSchoolYear + "ª etapa";
            }
            if (this == SIS) {
                return "SIS " + highSchoolYear;
            }
        }
        return id;
    }

    /**
     * Descrição contextual do vestibular considerando a série/conclusão do Ensino Médio.
     */
    public String getContextualDesc(boolean inHighSchool, int highSchoolYear) {
        if (inHighSchool && highSchoolYear >= 1 && highSchoolYear <= 3) {
            if (this == PSC) {
                return "Processo Seletivo Contínuo · " + highSchoolYear + "ª etapa dedicada à " + highSchoolYear + "ª série";
            }
            if (this == SIS) {
                return "Sistema de Ingresso Seriado · etapa SIS " + highSchoolYear + " dedicada à " + highSchoolYear + "ª série";
            }
            if (this == ENEM && highSchoolYear < 3) {
                return "Disponível a partir da 1ª série (participação como treineiro)";
            }
            if (this == Macro) {
                return "Vestibular Macro · prova única (disponível a partir da 3ª série)";
            }
        }
        return desc;
    }

    public static ExamId fromCode(String code) {
        if (code == null) return null;
        String trimmed = code.trim();
        for (ExamId ex : values()) {
            if (ex.id.equalsIgnoreCase(trimmed)) {
                return ex;
            }
        }
        return null;
    }

    public static List<ExamId> parseCsv(String csv) {
        List<ExamId> list = new ArrayList<>();
        if (csv != null && !csv.trim().isEmpty()) {
            for (String token : csv.split(",")) {
                ExamId ex = fromCode(token);
                if (ex != null && !list.contains(ex)) {
                    list.add(ex);
                }
            }
        }
        if (list.isEmpty()) {
            list.add(ENEM);
            list.add(PSC);
            list.add(SIS);
        }
        return list;
    }

    public static String toCsv(List<ExamId> exams) {
        if (exams == null || exams.isEmpty()) {
            return "ENEM,PSC,SIS";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exams.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(exams.get(i).id);
        }
        return sb.toString();
    }
}
