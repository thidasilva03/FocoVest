package com.example.focovest.data;

import android.graphics.Color;
import java.util.*;

public class DataRepository {

    public enum SubjectId {
        mat("Matemática", "#FF5A1F", "#FFE4D8"),
        fis("Física", "#2F5BFF", "#DDE5FF"),
        qui("Química", "#0E9F6E", "#D5F3E6"),
        bio("Biologia", "#8B5CF6", "#EBE2FF"),
        his("História", "#C2410C", "#F8E3D2"),
        geo("Geografia", "#0F766E", "#D3F1EC"),
        por("Português", "#DB2777", "#FBDCEB");

        public final String name;
        public final String colorHex;
        public final String softHex;

        SubjectId(String name, String colorHex, String softHex) {
            this.name = name;
            this.colorHex = colorHex;
            this.softHex = softHex;
        }

        public int getColorInt() { return Color.parseColor(colorHex); }
        public int getSoftColorInt() { return Color.parseColor(softHex); }
    }

    public enum ExamId {
        PSC("PSC", "UFAM", "Processo Seletivo Contínuo · 3 etapas", 12, 7),
        SIS("SIS", "UEA", "Sistema de Ingresso Seriado · 3 etapas", 11, 30),
        Macro("Macro", "UEA", "Vestibular Macro · prova única", 12, 14),
        ENEM("ENEM", "SiSU", "UFAM, IFAM e outras pelo SiSU", 11, 9);

        public final String id;
        public final String institution;
        public final String desc;
        public final int month;
        public final int day;

        ExamId(String id, String institution, String desc, int month, int day) {
            this.id = id;
            this.institution = institution;
            this.desc = desc;
            this.month = month;
            this.day = day;
        }
    }

    public static class Question {
        public String id;
        public SubjectId subject;
        public String prompt;
        public List<String> options;
        public int answer;
        public String explanation;

        public Question(String id, SubjectId subject, String prompt, List<String> options, int answer, String explanation) {
            this.id = id;
            this.subject = subject;
            this.prompt = prompt;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
        }
    }

    public static class Topic {
        public String id;
        public SubjectId subject;
        public String title;
        public String summary;
        public int minutes;
        public List<Question> questions;
        public Map<ExamId, Integer> incidence;

        public Topic(String id, SubjectId subject, String title, String summary, int minutes,
                     Map<ExamId, Integer> incidence, List<Question> questions) {
            this.id = id;
            this.subject = subject;
            this.title = title;
            this.summary = summary;
            this.minutes = minutes;
            this.incidence = incidence;
            this.questions = questions;
        }
    }

    public static class Simulado {
        public String id;
        public String name;
        public String exam;
        public String description;
        public int minutes;
        public List<String> questionIds;
        public List<String> topicIds;
        public boolean custom;

        public Simulado(String id, String name, String exam, String description, int minutes,
                        List<String> questionIds, List<String> topicIds, boolean custom) {
            this.id = id;
            this.name = name;
            this.exam = exam;
            this.description = description;
            this.minutes = minutes;
            this.questionIds = questionIds;
            this.topicIds = topicIds;
            this.custom = custom;
        }
    }

    public static class RankEntry {
        public String name;
        public String city;
        public int points;
        public int streak;
        public int delta;
        public boolean isUser;

        public RankEntry(String name, String city, int points, int streak, int delta, boolean isUser) {
            this.name = name;
            this.city = city;
            this.points = points;
            this.streak = streak;
            this.delta = delta;
            this.isUser = isUser;
        }
    }

    public static class Organizer {
        public String name, role, initials;
        public Organizer(String name, String role, String initials) {
            this.name = name; this.role = role; this.initials = initials;
        }
    }

    public static class TurmaStudent {
        public String name;
        public int progress;
        public TurmaStudent(String name, int progress) {
            this.name = name; this.progress = progress;
        }
    }

    public static final String TURMA_NAME = "Intensivo PSC & SIS 2025";
    public static final String TURMA_CODE = "AM-2025";
    public static final List<Organizer> TURMA_ORGANIZERS = Arrays.asList(
            new Organizer("Profa. Ana Ribeiro", "Coordenação · Biologia", "AR"),
            new Organizer("Prof. Carlos Mendes", "Exatas", "CM")
    );
    public static final List<TurmaStudent> TURMA_STUDENTS = Arrays.asList(
            new TurmaStudent("Beatriz Lima", 92),
            new TurmaStudent("Rafael Souza", 81),
            new TurmaStudent("Júlia Castro", 74),
            new TurmaStudent("Pedro Alves", 58),
            new TurmaStudent("Larissa Nogueira", 43)
    );

    public static final List<String> AM_CITIES = Arrays.asList(
            "Manaus", "Parintins", "Itacoatiara", "Manacapuru", "Coari", "Tefé",
            "Tabatinga", "Humaitá", "Maués", "Benjamin Constant", "Lábrea",
            "São Gabriel da Cachoeira", "Presidente Figueiredo", "Boca do Acre",
            "Eirunepé", "Iranduba", "Borba", "Careiro"
    );

    private static Map<ExamId, Integer> inc(int psc, int sis, int macro, int enem) {
        Map<ExamId, Integer> m = new EnumMap<>(ExamId.class);
        if (psc > 0) m.put(ExamId.PSC, psc);
        if (sis > 0) m.put(ExamId.SIS, sis);
        if (macro > 0) m.put(ExamId.Macro, macro);
        if (enem > 0) m.put(ExamId.ENEM, enem);
        return m;
    }

    public static final List<Topic> TOPICS = new ArrayList<>();
    public static final List<Question> ALL_QUESTIONS = new ArrayList<>();
    public static final List<Simulado> SIMULADOS = new ArrayList<>();

    static {
        // Core Topics com questões
        TOPICS.add(new Topic("mat-funcoes", SubjectId.mat, "Função do 2º grau",
                "Vértice, raízes, concavidade e problemas de máximo e mínimo.", 60, inc(3, 3, 3, 2),
                Arrays.asList(
                        new Question("mf1", SubjectId.mat, "Qual é a coordenada x do vértice da parábola f(x) = x² − 6x + 5?",
                                Arrays.asList("−3", "3", "5", "6", "1"), 1, "xᵥ = −b / 2a = −(−6) / 2·1 = 3."),
                        new Question("mf2", SubjectId.mat, "As raízes de x² − 5x + 6 = 0 são:",
                                Arrays.asList("1 e 6", "−2 e −3", "2 e 3", "−1 e 6", "0 e 5"), 2, "Soma = 5 e produto = 6, logo as raízes são 2 e 3."),
                        new Question("mf3", SubjectId.mat, "Se a < 0 em f(x) = ax² + bx + c, a parábola possui:",
                                Arrays.asList("Concavidade para cima e ponto de mínimo", "Concavidade para baixo e ponto de máximo", "Nenhuma raiz real", "Duas raízes reais iguais", "Eixo de simetria horizontal"),
                                1, "Com a negativo a parábola “abre para baixo” e o vértice é ponto de máximo.")
                )));

        TOPICS.add(new Topic("mat-prob", SubjectId.mat, "Probabilidade",
                "Espaço amostral, eventos independentes e probabilidade condicional.", 55, inc(2, 2, 2, 3),
                Arrays.asList(
                        new Question("mp1", SubjectId.mat, "Ao lançar um dado honesto, qual a probabilidade de sair um número primo?",
                                Arrays.asList("1/6", "1/3", "1/2", "2/3", "5/6"), 2, "Primos em {1..6}: 2, 3 e 5 → 3/6 = 1/2."),
                        new Question("mp2", SubjectId.mat, "Lançando duas moedas, qual a probabilidade de obter duas caras?",
                                Arrays.asList("1/2", "1/3", "1/4", "3/4", "1/8"), 2, "Eventos independentes: 1/2 · 1/2 = 1/4."),
                        new Question("mp3", SubjectId.mat, "Uma urna tem 3 bolas azuis e 7 vermelhas. Retirando uma, P(azul) é:",
                                Arrays.asList("0,3", "0,7", "0,37", "3/7", "0,5"), 0, "3 casos favoráveis em 10 possíveis = 0,3.")
                )));

        TOPICS.add(new Topic("fis-cinematica", SubjectId.fis, "Cinemática: MRUV",
                "Aceleração constante, equação de Torricelli e gráficos v × t.", 60, inc(3, 3, 2, 2),
                Arrays.asList(
                        new Question("fc1", SubjectId.fis, "Um carro parte do repouso com a = 2 m/s². Sua velocidade após 5 s é:",
                                Arrays.asList("2 m/s", "5 m/s", "7 m/s", "10 m/s", "25 m/s"), 3, "v = v₀ + at = 0 + 2·5 = 10 m/s."),
                        new Question("fc2", SubjectId.fis, "No gráfico v × t, a área sob a curva representa:",
                                Arrays.asList("Aceleração", "Deslocamento", "Força", "Tempo", "Energia"), 1, "A área sob o gráfico de velocidade fornece o deslocamento."),
                        new Question("fc3", SubjectId.fis, "A equação de Torricelli relaciona:",
                                Arrays.asList("v, v₀, a e Δs (sem o tempo)", "Força, massa e aceleração", "Pressão e volume", "Calor e temperatura", "s, v e t apenas"),
                                0, "v² = v₀² + 2aΔs — não depende explicitamente do tempo.")
                )));

        TOPICS.add(new Topic("qui-estequio", SubjectId.qui, "Estequiometria",
                "Mol, massa molar, reagente limitante e rendimento.", 60, inc(3, 2, 2, 2),
                Arrays.asList(
                        new Question("qe1", SubjectId.qui, "Quantos mols há em 36 g de água (H₂O, M = 18 g/mol)?",
                                Arrays.asList("0,5", "1", "2", "18", "36"), 2, "n = m / M = 36 / 18 = 2 mol."),
                        new Question("qe2", SubjectId.qui, "O reagente limitante é aquele que:",
                                Arrays.asList("Está em excesso", "É consumido totalmente primeiro", "Tem maior massa molar", "Não participa da reação", "É sempre o gás"),
                                1, "Ele limita a quantidade de produto, pois acaba antes dos demais."),
                        new Question("qe3", SubjectId.qui, "A constante de Avogadro vale aproximadamente:",
                                Arrays.asList("6,02 × 10²³", "3,0 × 10⁸", "9,8", "1,6 × 10⁻¹⁹", "8,31"), 0, "1 mol contém cerca de 6,02 × 10²³ entidades.")
                )));

        TOPICS.add(new Topic("bio-genetica", SubjectId.bio, "Genética: 1ª Lei de Mendel",
                "Dominância, heredogramas e proporções fenotípicas.", 55, inc(3, 3, 3, 2),
                Arrays.asList(
                        new Question("bg1", SubjectId.bio, "O cruzamento Aa × Aa gera qual proporção fenotípica (dominância completa)?",
                                Arrays.asList("1:1", "1:2:1", "3:1", "9:3:3:1", "100% dominante"), 2, "Genótipos 1 AA : 2 Aa : 1 aa → 3 dominantes : 1 recessivo."),
                        new Question("bg2", SubjectId.bio, "Um indivíduo com dois alelos iguais para um gene é chamado de:",
                                Arrays.asList("Heterozigoto", "Homozigoto", "Hemizigoto", "Poliploide", "Mutante"), 1, "Homozigoto possui alelos idênticos (AA ou aa)."),
                        new Question("bg3", SubjectId.bio, "O cruzamento-teste é feito com um indivíduo:",
                                Arrays.asList("Homozigoto dominante", "Heterozigoto", "Homozigoto recessivo", "De outra espécie", "Qualquer um"),
                                2, "Cruza-se com aa para revelar o genótipo do indivíduo dominante.")
                )));

        TOPICS.add(new Topic("his-era-vargas", SubjectId.his, "Era Vargas",
                "Revolução de 1930, Estado Novo e legislação trabalhista.", 50, inc(2, 2, 2, 2),
                Arrays.asList(
                        new Question("hv1", SubjectId.his, "O Estado Novo (1937–1945) caracterizou-se por:",
                                Arrays.asList("Ampla liberdade partidária", "Centralização autoritária do poder", "Parlamentarismo", "Domínio das oligarquias regionais", "Eleições diretas regulares"),
                                1, "Vargas fechou o Congresso e governou de forma centralizada e autoritária."),
                        new Question("hv2", SubjectId.his, "A CLT, Consolidação das Leis do Trabalho, foi criada em:",
                                Arrays.asList("1930", "1934", "1943", "1954", "1964"), 2, "A CLT foi promulgada em 1943, durante o Estado Novo."),
                        new Question("hv3", SubjectId.his, "A Revolução de 1930 encerrou o período conhecido como:",
                                Arrays.asList("Império", "República Velha", "Regência", "Ditadura Militar", "Nova República"),
                                1, "Ela pôs fim à República Velha e à política do café com leite.")
                )));

        TOPICS.add(new Topic("por-interpretacao", SubjectId.por, "Interpretação de texto",
                "Inferência, coesão, tese e argumentos em textos dissertativos.", 50, inc(3, 3, 3, 3),
                Arrays.asList(
                        new Question("pi1", SubjectId.por, "Em um texto dissertativo-argumentativo, a tese corresponde:",
                                Arrays.asList("Ao título do texto", "Ao ponto de vista defendido pelo autor", "À conclusão obrigatória", "Aos dados estatísticos", "À citação de autoridade"),
                                1, "A tese é a opinião central que o autor defende ao longo do texto."),
                        new Question("pi2", SubjectId.por, "O conectivo “entretanto” expressa ideia de:",
                                Arrays.asList("Adição", "Conclusão", "Oposição", "Causa", "Tempo"), 2, "“Entretanto” é conjunção adversativa: indica oposição/contraste."),
                        new Question("pi3", SubjectId.por, "Inferir uma informação significa:",
                                Arrays.asList("Copiá-la literalmente", "Deduzi-la a partir de pistas do texto", "Ignorá-la", "Contestá-la", "Resumi-la em uma palavra"),
                                1, "Inferência é a conclusão lógica a partir do que está implícito.")
                )));

        TOPICS.add(new Topic("fis-ondas", SubjectId.fis, "Ondulatória",
                "Frequência, período, comprimento de onda e fenômenos ondulatórios.", 55, inc(2, 2, 2, 2),
                Arrays.asList(
                        new Question("fo1", SubjectId.fis, "Uma onda com f = 50 Hz e λ = 2 m tem velocidade de:",
                                Arrays.asList("25 m/s", "52 m/s", "100 m/s", "48 m/s", "200 m/s"), 2, "v = λ·f = 2·50 = 100 m/s."),
                        new Question("fo2", SubjectId.fis, "O som é uma onda:",
                                Arrays.asList("Eletromagnética e transversal", "Mecânica e longitudinal", "Mecânica e transversal", "Eletromagnética e longitudinal", "Que se propaga no vácuo"),
                                1, "O som precisa de meio material e oscila na direção de propagação."),
                        new Question("fo3", SubjectId.fis, "O fenômeno em que a onda contorna obstáculos é a:",
                                Arrays.asList("Reflexão", "Refração", "Difração", "Polarização", "Absorção"), 2, "Difração é a capacidade de a onda contornar obstáculos e fendas.")
                )));

        // Edital Topics adicionais
        TOPICS.add(new Topic("mat-porcent", SubjectId.mat, "Porcentagem e juros", "Aumentos, descontos, juros simples e compostos.", 50, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("mat-geoplana", SubjectId.mat, "Geometria plana", "Áreas, perímetros, semelhança e Teorema de Pitágoras.", 60, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("mat-estat", SubjectId.mat, "Estatística básica", "Média, moda, mediana e leitura de gráficos.", 50, inc(1, 1, 1, 3), Collections.emptyList()));
        TOPICS.add(new Topic("mat-pa-pg", SubjectId.mat, "Progressões (PA e PG)", "Termo geral, soma dos termos e aplicações.", 55, inc(2, 2, 2, 1), Collections.emptyList()));
        TOPICS.add(new Topic("mat-trigo", SubjectId.mat, "Trigonometria no triângulo", "Seno, cosseno, tangente e leis dos senos e cossenos.", 60, inc(2, 3, 2, 1), Collections.emptyList()));
        TOPICS.add(new Topic("fis-dinamica", SubjectId.fis, "Leis de Newton", "Força resultante, atrito, plano inclinado.", 60, inc(3, 3, 3, 2), Collections.emptyList()));
        TOPICS.add(new Topic("fis-energia", SubjectId.fis, "Trabalho e energia", "Energia cinética, potencial e conservação.", 55, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("fis-eletro", SubjectId.fis, "Eletrodinâmica", "Corrente, resistores, potência e circuitos simples.", 60, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("qui-organica", SubjectId.qui, "Funções orgânicas", "Reconhecimento de grupos funcionais e nomenclatura.", 60, inc(2, 3, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("qui-solucoes", SubjectId.qui, "Soluções", "Concentração comum, molaridade e diluição.", 55, inc(2, 2, 2, 2), Collections.emptyList()));
        TOPICS.add(new Topic("qui-ambiental", SubjectId.qui, "Química ambiental", "Chuva ácida, efeito estufa e tratamento de água.", 50, inc(1, 1, 1, 3), Collections.emptyList()));
        TOPICS.add(new Topic("bio-ecologia", SubjectId.bio, "Ecologia e biomas amazônicos", "Cadeias alimentares, ciclos e a floresta amazônica.", 60, inc(3, 3, 3, 3), Collections.emptyList()));
        TOPICS.add(new Topic("bio-citologia", SubjectId.bio, "Citologia", "Organelas, membrana plasmática e divisão celular.", 55, inc(2, 2, 2, 2), Collections.emptyList()));
        TOPICS.add(new Topic("bio-fisiologia", SubjectId.bio, "Fisiologia humana", "Sistemas digestório, circulatório e respiratório.", 60, inc(2, 2, 2, 2), Collections.emptyList()));
        TOPICS.add(new Topic("his-amazonas", SubjectId.his, "Ciclo da borracha e Amazonas", "Belle Époque manauara, Zona Franca e formação do estado.", 55, inc(3, 3, 3, 1), Collections.emptyList()));
        TOPICS.add(new Topic("his-colonial", SubjectId.his, "Brasil Colônia", "Economia açucareira, mineração e sociedade colonial.", 50, inc(2, 2, 2, 2), Collections.emptyList()));
        TOPICS.add(new Topic("his-ditadura", SubjectId.his, "Ditadura militar", "Golpe de 1964, AI-5, milagre econômico e abertura.", 50, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("geo-amazonia", SubjectId.geo, "Geografia da Amazônia", "Bacia hidrográfica, população e economia regional.", 55, inc(3, 3, 3, 1), Collections.emptyList()));
        TOPICS.add(new Topic("geo-urbanizacao", SubjectId.geo, "Urbanização brasileira", "Êxodo rural, metropolização e problemas urbanos.", 50, inc(2, 1, 1, 3), Collections.emptyList()));
        TOPICS.add(new Topic("geo-clima", SubjectId.geo, "Clima e vegetação", "Massas de ar, climas do Brasil e domínios morfoclimáticos.", 55, inc(2, 2, 2, 2), Collections.emptyList()));
        TOPICS.add(new Topic("geo-ambiental", SubjectId.geo, "Questões ambientais", "Desmatamento, queimadas, acordos climáticos.", 50, inc(2, 2, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("por-redacao", SubjectId.por, "Redação dissertativo-argumentativa", "Estrutura, repertório e proposta de intervenção.", 60, inc(0, 0, 2, 3), Collections.emptyList()));
        TOPICS.add(new Topic("por-obras", SubjectId.por, "Obras literárias regionais", "Leituras obrigatórias e autores amazonenses.", 60, inc(3, 3, 2, 0), Collections.emptyList()));
        TOPICS.add(new Topic("por-gramatica", SubjectId.por, "Sintaxe e concordância", "Termos da oração, concordância e regência.", 55, inc(2, 2, 2, 1), Collections.emptyList()));
        TOPICS.add(new Topic("por-modernismo", SubjectId.por, "Modernismo brasileiro", "Semana de 22, gerações modernistas e principais obras.", 50, inc(2, 2, 2, 2), Collections.emptyList()));

        for (Topic t : TOPICS) {
            ALL_QUESTIONS.addAll(t.questions);
        }

        SIMULADOS.add(new Simulado("psc-geral", "Conhecimentos Gerais", "PSC",
                "Prova objetiva mista no estilo do PSC da UFAM.", 40,
                Arrays.asList("mf1", "mp1", "fc3", "qe2", "bg1", "hv1", "pi1", "fo3", "mf2", "hv2", "pi2", "mp2"), null, false));
        SIMULADOS.add(new Simulado("sis-geral", "Etapa Seriada", "SIS",
                "Questões de todas as áreas no formato do SIS da UEA.", 30,
                Arrays.asList("mf3", "mp3", "fc1", "qe1", "bg2", "hv3", "pi3", "fo1", "qe3", "bg3"), null, false));
        SIMULADOS.add(new Simulado("macro-humanas", "Linguagens e Humanas", "Macro",
                "Interpretação de texto e História no estilo do Macro da UEA.", 15,
                Arrays.asList("pi1", "pi2", "pi3", "hv1", "hv2", "hv3"), null, false));
        SIMULADOS.add(new Simulado("enem-natureza", "Ciências da Natureza", "ENEM",
                "Física, Química e Biologia no estilo ENEM.", 25,
                Arrays.asList("fc1", "qe1", "bg1", "fo1", "qe2", "bg3", "fc2", "fo2", "qe3", "bg2"), null, false));
    }

    public static Topic findTopicById(String id) {
        for (Topic t : TOPICS) if (t.id.equals(id)) return t;
        return TOPICS.get(0);
    }

    public static Question findQuestionById(String id) {
        for (Question q : ALL_QUESTIONS) if (q.id.equals(id)) return q;
        return null;
    }

    // Gerador determinístico do Ranking do Amazonas (idêntico ao data.ts)
    private static final String[] FIRST = {"Ana", "Lucas", "Mariana", "Gabriel", "Isabela", "Matheus", "Laura", "Enzo", "Sofia", "Davi", "Helena", "Arthur", "Valentina", "Heitor", "Manuela", "Bernardo", "Lívia", "Theo", "Clara", "Miguel"};
    private static final String[] LAST = {"Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Araújo", "Melo", "Barbosa", "Rocha"};

    public static List<RankEntry> rankingAmazonas() {
        final long[] s = {2025};
        java.util.function.DoubleSupplier rnd = () -> {
            s[0] = (s[0] * 9301 + 49297) % 233280;
            return s[0] / 233280.0;
        };
        List<RankEntry> list = new ArrayList<>();
        int pts = 2400 + (int) Math.floor(rnd.getAsDouble() * 300);
        for (int i = 0; i < 30; i++) {
            pts -= (int) Math.floor(20 + rnd.getAsDouble() * 90);
            String name = FIRST[(int) Math.floor(rnd.getAsDouble() * FIRST.length)] + " " +
                    LAST[(int) Math.floor(rnd.getAsDouble() * LAST.length)];
            double r1 = rnd.getAsDouble(), r2 = rnd.getAsDouble();
            String city = r1 < 0.55 ? "Manaus" : AM_CITIES.get(1 + (int) Math.floor(r2 * (AM_CITIES.size() - 1)));
            int streak = 1 + (int) Math.floor(rnd.getAsDouble() * 21);
            int delta = (int) Math.floor(rnd.getAsDouble() * 9) - 4;
            list.add(new RankEntry(name, city, pts, streak, delta, false));
        }
        return list;
    }
}
