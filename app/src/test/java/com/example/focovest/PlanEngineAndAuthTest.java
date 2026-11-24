package com.example.focovest;

import com.example.focovest.domain.PlanEngine;
import com.example.focovest.domain.PlanEngine.PlanResult;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.SubjectId;
import com.example.focovest.model.Topic;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PlanEngineAndAuthTest {

    private List<Topic> sampleTopics() {
        return Arrays.asList(
                new Topic("por-redacao", SubjectId.por, "Redação", "", 60, "https://youtu.be/abc12345", 3, 0, 0, 2),
                new Topic("por-obras", SubjectId.por, "Obras AM", "", 60, "https://youtu.be/def12345", 0, 3, 3, 2),
                new Topic("mat-funcoes", SubjectId.mat, "Funções", "", 60, "https://youtu.be/ghi12345", 2, 3, 3, 3),
                new Topic("his-amazonas", SubjectId.his, "Amazonas", "", 55, "https://youtu.be/jkl12345", 1, 3, 3, 3)
        );
    }

    @Test
    public void dedicatedPlanUsesOnlySelectedExamWeight() {
        List<Topic> all = sampleTopics();

        List<Topic> enem = PlanEngine.editalTopicsForExam(all, ExamId.ENEM);
        assertEquals(3, enem.size());
        assertEquals("por-redacao", enem.get(0).id); // tier 3 no ENEM
        for (Topic t : enem) {
            assertFalse("por-obras não cai no ENEM", "por-obras".equals(t.id));
        }

        List<Topic> psc = PlanEngine.editalTopicsForExam(all, ExamId.PSC);
        assertEquals(3, psc.size());
        for (Topic t : psc) {
            assertFalse("por-redacao não cai no PSC", "por-redacao".equals(t.id));
        }
    }

    @Test
    public void completedTopicsReflectAcrossPlansAndInvalidDaysDoNotLoop() {
        List<Topic> all = sampleTopics();
        Set<String> completed = new HashSet<>(Collections.singletonList("mat-funcoes"));

        PlanResult pscPlan = PlanEngine.buildPlanForExam(
                ExamId.PSC, all, 2, Arrays.asList(99, -1), completed, new HashMap<>());
        assertEquals(3, pscPlan.edital.size());
        assertEquals(1, pscPlan.completedTopics.size());
        assertEquals("mat-funcoes", pscPlan.completedTopics.get(0).id);
        assertFalse(pscPlan.days.isEmpty());
    }
}
