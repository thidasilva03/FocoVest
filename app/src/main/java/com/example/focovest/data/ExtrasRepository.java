package com.example.focovest.data;

import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.model.BookItem;
import com.example.focovest.model.ExamEvent;
import com.example.focovest.model.FlashcardItem;
import com.example.focovest.model.ForumQuestion;
import com.example.focovest.model.ForumReply;
import com.example.focovest.model.PastExam;
import com.example.focovest.model.ReadingText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExtrasRepository {

    public static final List<FlashcardItem> FLASHCARDS = new ArrayList<>();
    public static final List<ReadingText> READING_TEXTS = new ArrayList<>();
    public static final List<BookItem> BOOKS = new ArrayList<>();
    public static final List<PastExam> PAST_EXAMS = new ArrayList<>();

    static {
        // Flashcards alinhados aos tópicos do DataRepository
        FLASHCARDS.add(new FlashcardItem(
                "fc-mat-1", "mat-funcoes", SubjectId.mat,
                "Vértice da Parábola",
                "Quais são as fórmulas das coordenadas (xᵥ, yᵥ) do vértice de f(x) = ax² + bx + c?",
                "xᵥ = -b / (2a)  e  yᵥ = -Δ / (4a), onde Δ = b² - 4ac.",
                "Dica: você também pode calcular yᵥ substituindo xᵥ diretamente em f(x)."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-mat-2", "mat-prob", SubjectId.mat,
                "Probabilidade Condicional",
                "Como se calcula P(A | B), a probabilidade de A ocorrer dado que B já ocorreu?",
                "P(A | B) = P(A ∩ B) / P(B), com P(B) > 0.",
                "Reduza mentalmente o espaço amostral apenas aos casos em que B acontece."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-fis-1", "fis-cinematica", SubjectId.fis,
                "Equação de Torricelli",
                "Qual equação do MRUV permite calcular velocidade ou deslocamento sem conhecer o tempo?",
                "v² = v₀² + 2 · a · Δs",
                "Lembre-se de padronizar unidades no SI (m, s, m/s e m/s²)."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-fis-2", "fis-ondas", SubjectId.fis,
                "Equação Fundamental da Ondulatória",
                "Qual relação conecta velocidade (v), comprimento de onda (λ) e frequência (f)?",
                "v = λ · f   (e f = 1 / T, onde T é o período).",
                "A frequência depende apenas da fonte; ao mudar de meio, mudam v e λ."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-qui-1", "qui-estequio", SubjectId.qui,
                "Quantidade de Matéria (Mol)",
                "Como relacionar massa (m), massa molar (M) e número de mols (n)?",
                "n = m / M. Em 1 mol existem 6,02 × 10²³ entidades (Constante de Avogadro).",
                "Em reações com reagente limitante, calcule sempre o produto a partir do limitante."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-bio-1", "bio-genetica", SubjectId.bio,
                "1ª Lei de Mendel",
                "Qual a proporção genotípica e fenotípica do cruzamento Aa × Aa com dominância completa?",
                "Genotípica: 1 AA : 2 Aa : 1 aa.\nFenotípica: 3 dominantes : 1 recessivo.",
                "Se houver dominância incompleta ou codominância, a proporção fenotípica passa a ser 1:2:1."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-his-1", "his-amazonas", SubjectId.his,
                "Ciclo da Borracha no Amazonas",
                "Quais foram os principais marcos urbanos da Belle Époque manauara (1879–1912)?",
                "Inauguração do Teatro Amazonas (1896), Porto de Manaus (flutuante), bondes elétricos e saneamento do centro histórico.",
                "A crise veio com a concorrência da borracha asiática (Malásia), cultivada a partir de sementes levadas da Amazônia."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-geo-1", "geo-amazonia", SubjectId.geo,
                "Rios de Água Branca, Preta e Clara",
                "Qual a diferença básica entre o Rio Negro e o Rio Solimões no Encontro das Águas?",
                "Solimões: água barrenta/branca, rica em sedimentos andinos, pH próximo do neutro e maior velocidade.\nNegro: água preta, ácida, pobre em sedimentos e mais quente.",
                "A diferença de densidade, temperatura e velocidade explica por que correm lado a lado sem se misturar por quilômetros."
        ));
        FLASHCARDS.add(new FlashcardItem(
                "fc-por-1", "por-redacao", SubjectId.por,
                "Proposta de Intervenção (ENEM)",
                "Quais são os 5 elementos obrigatórios da proposta de intervenção na Competência 5?",
                "1. Agente · 2. Ação · 3. Meio/Modo · 4. Finalidade/Efeito · 5. Detalhamento de um dos itens.",
                "Sempre respeite os direitos humanos e conecte a proposta aos argumentos desenvolvidos no D1 e D2."
        ));

        // Textos e resumos aprofundados
        READING_TEXTS.add(new ReadingText(
                "txt-mat-1", "mat-funcoes", SubjectId.mat,
                "Guia rápido: Máximos, Mínimos e Raízes na Função Quadrática",
                "Prof. Carlos Mendes", 6,
                "A função quadrática f(x) = ax² + bx + c (com a ≠ 0) é um dos assuntos de maior incidência no PSC (UFAM) e no SIS (UEA).\n\n"
                        + "1. Concavidade:\n"
                        + "• Se a > 0, a parábola tem concavidade para cima e o vértice é ponto de MÍNIMO.\n"
                        + "• Se a < 0, a parábola tem concavidade para baixo e o vértice é ponto de MÁXIMO.\n\n"
                        + "2. Raízes e discriminante (Δ = b² - 4ac):\n"
                        + "• Δ > 0: duas raízes reais distintas.\n"
                        + "• Δ = 0: uma raiz real dupla (a parábola tangencia o eixo x).\n"
                        + "• Δ < 0: nenhuma raiz real.\n\n"
                        + "3. Dica de prova:\n"
                        + "Quando o enunciado pede 'qual valor de x maximiza o lucro' → calcule xᵥ = -b/(2a). Quando pede 'qual o lucro máximo' → calcule yᵥ = f(xᵥ)."
        ));
        READING_TEXTS.add(new ReadingText(
                "txt-his-1", "his-amazonas", SubjectId.his,
                "Amazônia no Século XX: da Borracha à Zona Franca de Manaus",
                "Profa. Ana Ribeiro", 7,
                "Temas de História Regional têm peso decisivo nas provas da UFAM e da UEA.\n\n"
                        + "1. Primeiro Ciclo da Borracha (1879–1912):\n"
                        + "Impulsionado pela demanda industrial automobilística, atraiu grande migração nordestina (sistema de aviamento nos seringais) e transformou Manaus e Belém em centros cosmopolitas.\n\n"
                        + "2. Segundo Ciclo (1942–1945):\n"
                        + "Durante a 2ª Guerra Mundial, os Acordos de Washington mobilizaram os 'Soldados da Borracha' para suprir os Aliados.\n\n"
                        + "3. Zona Franca de Manaus (1967):\n"
                        + "Reestruturada pelo Decreto-Lei nº 288/1967, criou um polo industrial, comercial e agropecuário com incentivos fiscais visando integração nacional e desenvolvimento regional."
        ));
        READING_TEXTS.add(new ReadingText(
                "txt-bio-1", "bio-ecologia", SubjectId.bio,
                "Bioma Amazônico: Ciclagem de Nutrientes e Floresta em Pé",
                "Profa. Ana Ribeiro", 5,
                "Embora abrigue a maior biodiversidade do planeta, grande parte do solo amazônico é relativamente pobre em minerais.\n\n"
                        + "Como a floresta se mantém tão exuberante?\n"
                        + "A resposta está na rápida ciclagem de nutrientes na serrapilheira (folhas, galhos e matéria orgânica), acelerada pelo clima quente e úmido e pela ação intensa de fungos e bactérias decompositoras.\n\n"
                        + "Além disso, a evapotranspiração da floresta alimenta os chamados 'rios voadores', massas de ar úmidas essenciais para o regime de chuvas do Centro-Oeste e Sudeste do Brasil."
        ));
        READING_TEXTS.add(new ReadingText(
                "txt-fis-1", "fis-cinematica", SubjectId.fis,
                "Interpretando Gráficos no MRU e MRUV sem decorar",
                "Prof. Carlos Mendes", 5,
                "Nos vestibulares regionais e no ENEM, questões gráficas são mais comuns do que contas puras.\n\n"
                        + "• Gráfico s × t: a inclinação representa a velocidade. Reta inclinada = MRU; parábola = MRUV.\n"
                        + "• Gráfico v × t: a inclinação representa a aceleração e a ÁREA sob a curva equivale ao deslocamento Δs.\n"
                        + "• Gráfico a × t: a área sob a curva fornece a variação da velocidade Δv.\n\n"
                        + "Sempre verifique as unidades nos eixos antes de calcular qualquer área!"
        ));

        // Livros e obras literárias recomendadas
        BOOKS.add(new BookItem(
                "book-1", "Dois Irmãos", "Milton Hatoum", ExamId.PSC, SubjectId.por,
                "Romance ambientado em Manaus que narra a rivalidade entre os gêmeos Yaqub e Omar em uma família de origem libanesa. Referência clássica em literatura amazonense.",
                "https://www.google.com/search?q=Dois+Irmaos+Milton+Hatoum+resumo+vestibular"
        ));
        BOOKS.add(new BookItem(
                "book-2", "A Selva", "Ferreira de Castro", ExamId.SIS, SubjectId.por,
                "Retrata as duras condições de trabalho nos seringais amazônicos no início do século XX através da trajetória do imigrante português Alberto.",
                "https://www.google.com/search?q=A+Selva+Ferreira+de+Castro+resumo+vestibular"
        ));
        BOOKS.add(new BookItem(
                "book-3", "Galvez, Imperador do Acre", "Márcio Souza", ExamId.Macro, SubjectId.por,
                "Folhetim satírico sobre o ciclo da borracha e a questão do Acre, frequentemente cobrado em análises interdisciplinares de Literatura e História da Amazônia.",
                "https://www.google.com/search?q=Galvez+Imperador+do+Acre+Marcio+Souza+resumo"
        ));
        BOOKS.add(new BookItem(
                "book-4", "Tópicos de Física (Vol. 1–3)", "Gualter, Helou e Newton", ExamId.ENEM, SubjectId.fis,
                "Coleção de referência para aprofundamento em Mecânica, Termologia, Ondulatória e Eletricidade com exercícios graduados por nível.",
                "https://www.google.com/search?q=Topicos+de+Fisica+mecanica+vestibular"
        ));
        BOOKS.add(new BookItem(
                "book-5", "Fundamentos de Matemática Elementar", "Gelson Iezzi", ExamId.PSC, SubjectId.mat,
                "Base teórica sólida para Funções, Trigonometria, Geometria Plana e Probabilidade com foco em questões discursivas e objetivas.",
                "https://www.google.com/search?q=Fundamentos+de+Matematica+Elementar+Iezzi"
        ));

        // Provas anteriores oficiais
        PAST_EXAMS.add(new PastExam(
                "pe-psc-2024", ExamId.PSC, 2024, "Etapas 1, 2 e 3",
                "Cadernos oficiais e gabaritos do Processo Seletivo Contínuo da UFAM (COMPEC).",
                "https://compec.ufam.edu.br/",
                "https://compec.ufam.edu.br/"
        ));
        PAST_EXAMS.add(new PastExam(
                "pe-sis-2024", ExamId.SIS, 2024, "1ª, 2ª e 3ª Etapas",
                "Provas anteriores do Sistema de Ingresso Seriado da Universidade do Estado do Amazonas (UEA).",
                "https://selecao.uea.edu.br/",
                "https://selecao.uea.edu.br/"
        ));
        PAST_EXAMS.add(new PastExam(
                "pe-macro-2024", ExamId.Macro, 2024, "Conhecimentos Gerais + Específicos",
                "Cadernos de prova geral e específica + redação do Vestibular Macro da UEA.",
                "https://selecao.uea.edu.br/",
                "https://selecao.uea.edu.br/"
        ));
        PAST_EXAMS.add(new PastExam(
                "pe-enem-2024", ExamId.ENEM, 2024, "1º e 2º Dia · Caderno Azul",
                "Provas oficiais e gabaritos definitivos do Exame Nacional do Ensino Médio (INEP).",
                "https://www.gov.br/inep/pt-br/areas-de-atuacao/avaliacao-e-exames-educacionais/enem/provas-e-gabaritos",
                "https://www.gov.br/inep/pt-br/areas-de-atuacao/avaliacao-e-exames-educacionais/enem/provas-e-gabaritos"
        ));
    }

    /**
     * Gera os eventos oficiais do calendário anual a partir das datas dos vestibulares em DataRepository.ExamId.
     */
    public static List<ExamEvent> buildOfficialEvents(int year) {
        List<ExamEvent> list = new ArrayList<>();
        for (ExamId ex : ExamId.values()) {
            String examDate = String.format(Locale.US, "%04d-%02d-%02d", year, ex.month, ex.day);
            list.add(new ExamEvent(
                    "ev-prova-" + ex.id + "-" + year,
                    "Prova " + ex.id + " (" + ex.institution + ")",
                    ex.desc,
                    ex,
                    examDate,
                    "08:00 · Abertura dos portões",
                    "prova",
                    false
            ));

            // Estimativa de janela de inscrição (~2 meses antes da prova)
            Calendar cal = Calendar.getInstance();
            cal.set(year, ex.month - 1, Math.min(ex.day, 28));
            cal.add(Calendar.MONTH, -2);
            String regDate = String.format(Locale.US, "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            list.add(new ExamEvent(
                    "ev-insc-" + ex.id + "-" + year,
                    "Período de Inscrição · " + ex.id,
                    "Verifique o edital oficial da " + ex.institution + " e confirme isenção ou taxa.",
                    ex,
                    regDate,
                    "Dia todo",
                    "inscricao",
                    false
            ));
        }
        return list;
    }

    public static List<ForumQuestion> createSeedForumQuestions(String todayKey) {
        List<ForumQuestion> list = new ArrayList<>();
        list.add(new ForumQuestion(
                "fq-1",
                "Como diferenciar quando usar Torricelli ou a função horária da posição?",
                "Sempre fico em dúvida nas questões de Cinemática do PSC sobre qual fórmula escolher primeiro.",
                "Beatriz Lima", "Manaus", ExamId.PSC, SubjectId.fis, todayKey, false,
                Arrays.asList(
                        new ForumReply("fr-1", "Prof. Carlos Mendes", true,
                                "Liste os dados do enunciado. Se o tempo (t) não foi dado e também não foi pedido, vá direto para Torricelli: v² = v₀² + 2aΔs!",
                                todayKey)
                )
        ));
        list.add(new ForumQuestion(
                "fq-2",
                "História do Amazonas cai mais no PSC ou no SIS?",
                "Estou montando minha revisão final e queria saber em qual prova priorizar Ciclo da Borracha e Zona Franca.",
                "Rafael Souza", "Parintins", ExamId.SIS, SubjectId.his, todayKey, false,
                Arrays.asList(
                        new ForumReply("fr-2", "Profa. Ana Ribeiro", true,
                                "Cai bastante nos dois! Tanto o PSC (UFAM) quanto o SIS e o Macro (UEA) cobram Amazônia Colonial, Borracha e Zona Franca com frequência alta.",
                                todayKey)
                )
        ));
        list.add(new ForumQuestion(
                "fq-3",
                "Dica para não errar cruzamentos de Genética (1ª Lei de Mendel)?",
                "Às vezes confundo proporção genotípica com fenotípica em heredogramas.",
                "Júlia Castro", "Itacoatiara", ExamId.ENEM, SubjectId.bio, todayKey, false,
                new ArrayList<>()
        ));
        return list;
    }
}
