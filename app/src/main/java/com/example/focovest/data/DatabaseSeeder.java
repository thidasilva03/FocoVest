package com.example.focovest.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Popula o SQLite automaticamente com todo o conteúdo programático consolidado de:
 * - UFAM PSC (1ª, 2ª e 3ª Etapas — 7 disciplinas)
 * - UEA SIS (1ª, 2ª e 3ª Séries — 11 disciplinas)
 * - UEA Macro (1ª a 3ª Séries + obras literárias — 11 disciplinas)
 * - ENEM (Matriz de Referência e Objetos de Conhecimento — 4 áreas / 11 disciplinas)
 *
 * Cada tópico traz resumo aderente ao edital, videoaula de referência e 3 questões comentadas.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void seed(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            seedTopicsAndQuestionsInternal(db);
            seedInitialLocalUser(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Atualiza/insere tópicos e questões sem remover progresso salvo pelo usuário (onUpgrade v5).
     */
    public static void seedTopicsAndQuestionsOnly(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            seedTopicsAndQuestionsInternal(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void seedTopicsAndQuestionsInternal(SQLiteDatabase db) {
        seedLinguagens(db);
        seedMatematica(db);
        DatabaseSeederPart2.seedNatureza(db);
        DatabaseSeederPart2.seedHumanas(db);
    }

    // =========================================================================
    // 1. LINGUAGENS: PORTUGUÊS/LITERATURA (9), ARTE (3), INGLÊS (3)
    // =========================================================================
    private static void seedLinguagens(SQLiteDatabase db) {
        // --- PORTUGUÊS E LITERATURA ---
        upsertTopic(db, "por-interpretacao", "por", "Texto, leitura, coesão e variação linguística",
                "Sentido textual, figuras de linguagem, coesão, intertextualidade (paráfrase/paródia), norma-padrão e variações linguísticas.",
                50, "https://www.youtube.com/watch?v=M9xR4uF8k2Q",
                3, 3, 2, 2, 3, 2, 2, 3);
        upsertQuestion(db, "pi1", "por-interpretacao", "por",
                "Em um texto dissertativo-argumentativo, a tese corresponde:",
                "Ao título do texto", "Ao ponto de vista defendido pelo autor",
                "À conclusão obrigatória", "Aos dados estatísticos", "À citação de autoridade", 1,
                "A tese é a ideia central defendida pelo autor ao longo da argumentação.");
        upsertQuestion(db, "pi2", "por-interpretacao", "por",
                "O conectivo 'entretanto' estabelece relação de:",
                "Adição", "Conclusão", "Oposição", "Causa", "Tempo", 2,
                "'Entretanto' é conjunção adversativa, indicando contraste ou oposição.");
        upsertQuestion(db, "pi3", "por-interpretacao", "por",
                "A reescrita de um texto conhecido com intenção crítica ou bem-humorada caracteriza:",
                "Paráfrase", "Paródia", "Pleonasmo", "Eufemismo", "Catacrese", 1,
                "A paródia subverte ou critica o texto-fonte, enquanto a paráfrase mantém seu sentido original.");

        upsertTopic(db, "por-lit-colonial-rom", "por", "Literatura colonial, Barroco, Arcadismo e Romantismo",
                "Literatura dos colonizadores, Barroco, Arcadismo, poesia e contos românticos, literaturas portuguesa, indígena e africana.",
                55, "https://www.youtube.com/watch?v=E7jP8sL2x4R",
                2, 3, 0, 0, 3, 0, 0, 2);
        upsertQuestion(db, "plcr1", "por-lit-colonial-rom", "por",
                "Qual característica marca a estética barroca na literatura brasileira?",
                "Objetividade científica e determinismo",
                "Conflito entre o espiritual e o terreno (antíteses e paradoxos)",
                "Valorização exclusiva do verso livre modernista",
                "Impassibilidade formal parnasiana",
                "Linguagem exclusivamente coloquial", 1,
                "O Barroco expressa o dualismo humano por meio de antíteses, paradoxos, cultismo e conceptismo.");
        upsertQuestion(db, "plcr2", "por-lit-colonial-rom", "por",
                "O lema latino 'fugere urbem' (fugir da cidade) e o bucolismo caracterizam o:",
                "Barroco", "Arcadismo", "Realismo", "Parnasianismo", "Simbolismo", 1,
                "O Arcadismo idealiza a vida simples no campo e a busca pela serenidade natural.");
        upsertQuestion(db, "plcr3", "por-lit-colonial-rom", "por",
                "Na primeira geração romântica brasileira, destaca-se a temática:",
                "Indianista e nacionalista", "Ultrarromântica e escapista apenas",
                "Cientificista e determinista", "Concretista", "Parnasianista", 0,
                "A 1ª geração romântica (ex.: Gonçalves Dias, José de Alencar) valorizou o indígena e a pátria.");

        upsertTopic(db, "por-gramatica", "por", "Sintaxe, concordância, regência, crase e pontuação",
                "Período simples e composto (coordenação e subordinação), concordância, regência, vozes verbais, colocação pronominal, crase e vírgula.",
                55, "https://www.youtube.com/watch?v=Y1wT5rA9e3N",
                2, 3, 3, 2, 3, 3, 2, 3);
        upsertQuestion(db, "pgr1", "por-gramatica", "por",
                "Assinale a frase com concordância verbal correta na norma-padrão:",
                "Fazem dois anos que estudo para o vestibular.",
                "Houveram muitos inscritos no processo seletivo.",
                "Faz dois anos que estudo para o vestibular.",
                "Deve existir poucas vagas disponíveis.",
                "Falta ainda três semanas para a prova.", 2,
                "O verbo 'fazer' indicando tempo decorrido é impessoal e permanece no singular.");
        upsertQuestion(db, "pgr2", "por-gramatica", "por",
                "Assinale a frase em que o uso da crase está correto:",
                "Vou à Manaus amanhã.",
                "Entreguei o resumo à professora.",
                "Começou à estudar cedo.",
                "Ficou frente à frente com o problema.",
                "Escreveu à lápis o rascunho.", 1,
                "'Entregar' exige preposição 'a' e 'professora' admite artigo 'a', resultando em 'à'.");
        upsertQuestion(db, "pgr3", "por-gramatica", "por",
                "Na frase 'A aluna que estudou passou', a oração 'que estudou' classifica-se como subordinada:",
                "Substantiva objetiva direta", "Adjetiva restritiva",
                "Adverbial causal", "Coordenada sindética", "Adverbial concessiva", 1,
                "Introduzida pelo pronome relativo 'que' sem vírgulas, restringe o sentido de 'aluna'.");

        upsertTopic(db, "por-realismo-parnaso", "por", "Realismo, Naturalismo, Parnasianismo e gêneros midiáticos",
                "Realismo e Naturalismo, poesia parnasiana, crônica, fato vs. opinião, bolhas informacionais, pós-verdade, infográficos e literatura marginal.",
                55, "https://www.youtube.com/watch?v=H4kL9mP2v7S",
                2, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "prp1", "por-realismo-parnaso", "por",
                "Diferentemente do Romantismo, o Realismo brasileiro (como em Machado de Assis) prioriza:",
                "Idealização amorosa e fuga da realidade",
                "Análise psicológica profunda e crítica social irônica",
                "Determinismo exclusivamente biológico em cortiços",
                "Exaltação medieval europeia",
                "Escrita automática surrealista", 1,
                "O Realismo machadiano investiga as contradições morais e psicológicas da sociedade.");
        upsertQuestion(db, "prp2", "por-realismo-parnaso", "por",
                "A busca pela 'arte pela arte', rigor métrico e objetividade descritiva define o:",
                "Romantismo", "Parnasianismo", "Simbolismo", "Barroco", "Modernismo", 1,
                "Os parnasianos valorizavam o apuro formal, sonetos regulares e descrições precisas.");
        upsertQuestion(db, "prp3", "por-realismo-parnaso", "por",
                "No campo jornalístico-midiático, distinguir fato de opinião é essencial para evitar:",
                "A coesão referencial", "A desinformação e o viés em bolhas informacionais",
                "A concordância nominal", "O uso de norma culta", "A leitura inferencial", 1,
                "Separar dados verificáveis de juízos de valor combate fake news e pós-verdade.");

        upsertTopic(db, "por-modernismo", "por", "Simbolismo, Pré-Modernismo, Vanguardas e Modernismo",
                "Simbolismo, Pré-Modernismo, Vanguardas Europeias, Modernismo brasileiro e português, Clube da Madrugada no Amazonas e teatro contemporâneo.",
                55, "https://www.youtube.com/watch?v=L5xK9vT3m8P",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "pmd1", "por-modernismo", "por",
                "Qual evento inaugurou oficialmente o Modernismo brasileiro em fevereiro de 1922?",
                "Publicação de Os Sertões", "Semana de Arte Moderna em São Paulo",
                "Fundação da Academia Brasileira de Letras", "Criação do Clube da Madrugada",
                "Estreia do Teatro Amazonas", 1,
                "A Semana de Arte Moderna de 1922 propôs ruptura estética e valorização da cultura brasileira.");
        upsertQuestion(db, "pmd2", "por-modernismo", "por",
                "No Amazonas, o movimento literário e artístico que difundiu ideais modernistas na década de 1950 foi:",
                "Academia dos Esquecidos", "Clube da Madrugada",
                "Escola do Recife", "Grupo de Anta", "Concretismo Paulista", 1,
                "Fundado em Manaus em 1954, o Clube da Madrugada renovou a literatura e as artes amazonenses.");
        upsertQuestion(db, "pmd3", "por-modernismo", "por",
                "O Pré-Modernismo (Euclides da Cunha, Lima Barreto, Monteiro Lobato) caracterizou-se por:",
                "Recusar qualquer tema social", "Retratar o Brasil marginalizado e tensões republicanas",
                "Retomar rigorosamente o soneto árcade", "Ignorar a realidade regional",
                "Seguir estritamente o simbolismo francês", 1,
                "Autores pré-modernistas expuseram conflitos sociais, o sertão, o subúrbio e o nacionalismo crítico.");

        upsertTopic(db, "por-redacao", "por", "Redação dissertativo-argumentativa e gêneros textuais",
                "Estrutura dissertativa e expositivo-argumentativa, projeto de texto, repertório, coesão, proposta de intervenção, artigo de opinião e resenha.",
                60, "https://www.youtube.com/watch?v=D8sN6vF3k1X",
                3, 2, 3, 3, 2, 3, 3, 3);
        upsertQuestion(db, "pre1", "por-redacao", "por",
                "Na Competência 5 da Redação do ENEM, a proposta de intervenção completa deve conter:",
                "Apenas um resumo do tema",
                "Agente, ação, meio/modo, finalidade e detalhamento",
                "Somente citação de um filósofo",
                "Título obrigatório e assinatura",
                "Perguntas retóricas sem solução", 1,
                "Os cinco elementos válidos garantem pontuação máxima na Competência 5, respeitando os direitos humanos.");
        upsertQuestion(db, "pre2", "por-redacao", "por",
                "Qual função exercem conectivos como 'portanto', 'dessa forma' e 'logo' na conclusão?",
                "Indicar oposição", "Sinalizar retomada conclusiva e fechamento argumentativo",
                "Introduzir uma narrativa ficcional", "Substituir o tema central",
                "Anular os argumentos anteriores", 1,
                "Operadores argumentativos conclusivos garantem coesão interparágrafos e progressão textual.");
        upsertQuestion(db, "pre3", "por-redacao", "por",
                "Um texto expositivo-argumentativo em prosa na modalidade culta (como no SIS e Macro) exige:",
                "Uso de gírias e primeira pessoa informal",
                "Clareza, organização lógica de ideias e justificativa consistente do ponto de vista",
                "Estrutura em versos rimados",
                "Listas em tópicos sem conectivos",
                "Apenas narração de uma história pessoal", 1,
                "Tanto o SIS quanto o Macro avaliam domínio da norma culta, coerência e defesa fundamentada.");

        upsertTopic(db, "por-obras-1", "por", "Obras literárias — 1ª Etapa (Esaú e Jacó; O Karaíba; Torto Arado)",
                "Análise de Esaú e Jacó (Machado de Assis — PSC 1), O Karaíba (Daniel Munduruku — SIS 1) e Torto Arado (Itamar Vieira Junior — SIS 1).",
                60, "https://www.youtube.com/watch?v=K2wL8rT4p9M",
                0, 3, 0, 0, 3, 0, 0, 0);
        upsertQuestion(db, "po1_1", "por-obras-1", "por",
                "Em 'Esaú e Jacó' (Machado de Assis), a rivalidade dos gêmeos Pedro e Paulo simboliza também o embate político entre:",
                "Feudalismo e mercantilismo", "Monarquia e República no Brasil do fim do século XIX",
                "Barroco e Arcadismo", "Portugal e Espanha", "Norte e Sul durante a Guerra Fria", 1,
                "Pedro defende a Monarquia e Paulo a República, refletindo as ambiguidades da transição política brasileira.");
        upsertQuestion(db, "po1_2", "por-obras-1", "por",
                "Em 'O Karaíba', de Daniel Munduruku, a narrativa valoriza principalmente:",
                "A visão eurocêntrica da colonização",
                "A ancestralidade, espiritualidade e memória dos povos indígenas antes da chegada europeia",
                "A vida urbana industrial do século XXI",
                "A poesia parnasiana francesa",
                "A migração europeia para o Sul", 1,
                "Daniel Munduruku destaca saberes tradicionais, profecia e harmonia comunitária dos povos originários.");
        upsertQuestion(db, "po1_3", "por-obras-1", "por",
                "Em 'Torto Arado' (Itamar Vieira Junior), as irmãs Bibiana e Belonísia vivenciam:",
                "A corte imperial no Rio de Janeiro",
                "Relações de trabalho análogas à escravidão, religiosidade e luta pela terra no sertão baiano",
                "A imigração japonesa em São Paulo",
                "A Guerra do Paraguai",
                "O ciclo do ouro em Minas Gerais", 1,
                "A obra retrata descendentes de escravizados na fazenda Água Negra e a luta por dignidade e terra.");

        upsertTopic(db, "por-obras-2", "por", "Obras literárias — 2ª Etapa (A cidade ilhada; Brás Cubas; Úrsula)",
                "Análise de A cidade ilhada (Milton Hatoum — PSC 2), Memórias Póstumas de Brás Cubas (Machado — SIS 2) e Úrsula (Maria Firmina dos Reis — SIS 2).",
                60, "https://www.youtube.com/watch?v=G9rK4wF2v8Q",
                0, 0, 3, 0, 0, 3, 0, 0);
        upsertQuestion(db, "po2_1", "por-obras-2", "por",
                "Em 'A cidade ilhada', Milton Hatoum reúne contos marcados por:",
                "Epopéias medievais europeias",
                "Memória, deslocamento, afetos e contrastes sociais tendo Manaus e a Amazônia como eixo",
                "Ficção científica espacial",
                "Manuais técnicos de botânica",
                "Poemas concretos sem narrativa", 1,
                "Os contos de Hatoum exploram exílio, lembranças familiares e transformações urbanas de Manaus.");
        upsertQuestion(db, "po2_2", "por-obras-2", "por",
                "Qual inovação narrativa marca 'Memórias Póstumas de Brás Cubas', de Machado de Assis?",
                "Narrador em terceira pessoa imparcial",
                "Um defunto-autor que narra com ironia, digressões e diálogo direto com o leitor",
                "Estrutura em forma de diário de viagem colonial",
                "Ausência total de crítica à elite",
                "Linguagem exclusivamente regionalista nordestina", 1,
                "Brás Cubas narra além-túmulo, rompendo a linearidade e expondo a hipocrisia da elite carioca.");
        upsertQuestion(db, "po2_3", "por-obras-2", "por",
                "'Úrsula' (1859), de Maria Firmina dos Reis, é marco histórico da literatura brasileira por ser:",
                "O primeiro romance modernista de 1922",
                "O primeiro romance abolicionista brasileiro escrito por uma mulher negra, dando voz a personagens escravizados",
                "Uma peça teatral parnasiana",
                "Um relato de viagens europeias",
                "Uma coletânea de crônicas esportivas", 1,
                "Maria Firmina dos Reis humaniza personagens como Túlio e Mãe Susana, denunciando a escravidão.");

        upsertTopic(db, "por-obras", "por", "Obras literárias — 3ª Etapa e Macro (Clarice, Guimarães, Mário, Lima Barreto)",
                "Laços de Família (Clarice Lispector), Sagarana (Guimarães Rosa), Macunaíma (Mário de Andrade) e Triste Fim de Policarpo Quaresma (Lima Barreto).",
                60, "https://www.youtube.com/watch?v=G9rK4wF2v8Q",
                0, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "pob1", "por-obras", "por",
                "Nos contos de 'Laços de Família' (Clarice Lispector), um elemento recorrente é:",
                "A batalha épica militar",
                "A epifania cotidiana que revela crises existenciais nas relações familiares",
                "A exaltação ufanista do governo",
                "A descrição científica de rochas",
                "O soneto clássico decassílabo", 1,
                "Clarice explora momentos súbitos de revelação interior (epifania) em personagens comuns.");
        upsertQuestion(db, "pob2", "por-obras", "por",
                "Em 'Macunaíma' (Mário de Andrade), o protagonista é definido como 'o herói sem nenhum caráter' porque:",
                "Representa um modelo europeu rígido",
                "Sintetiza a pluralidade cultural, contradições e ausência de uma identidade única fixa do povo brasileiro",
                "É um cientista positivista",
                "Defende a volta da monarquia portuguesa",
                "Não interage com mitos indígenas", 1,
                "A rapsódia modernista funde mitos indígenas (como Makunaima), folclore e crítica bem-humorada ao Brasil.");
        upsertQuestion(db, "pob3", "por-obras", "por",
                "Em 'Triste Fim de Policarpo Quaresma' (Lima Barreto), o projeto nacionalista do protagonista:",
                "É plenamente acolhido pela República sem conflitos",
                "Choca-se com a burocracia, o autoritarismo e a hipocrisia da Primeira República",
                "Ocorre durante o Brasil Colônia",
                "Foca apenas na economia industrial europeia",
                "Ignora a língua tupi e a agricultura brasileira", 1,
                "O ufanismo ingênuo de Policarpo transforma-se em desilusão diante da realidade política republicana.");

        // --- ARTE (SIS 1/2/3, Macro, ENEM — 0 no PSC) ---
        upsertTopic(db, "art-identidade-am", "art", "Arte, corpo, patrimônio e identidade amazônica",
                "Corpo e identidade, patrimônio artístico-cultural local, música, dança, teatro e expressões multiculturais amazônicas.",
                45, "https://www.youtube.com/watch?v=Q6pM2vN8k3L",
                2, 0, 0, 0, 3, 0, 0, 2);
        upsertQuestion(db, "aia1", "art-identidade-am", "art",
                "O patrimônio artístico e cultural imaterial da Amazônia inclui:",
                "Apenas prédios tombados de concreto",
                "Saberes, danças, músicas, festivais populares (como o Boi-Bumbá) e grafismos indígenas",
                "Somente pinturas renascentistas europeias",
                "Exclusivamente esculturas em mármore",
                "Apenas obras digitais estrangeiras", 1,
                "Patrimônio imaterial abrange práticas vivas, festas, musicalidades e saberes transmitidos entre gerações.");
        upsertQuestion(db, "aia2", "art-identidade-am", "art",
                "Nas práticas corporais e cênicas (dança e teatro), o corpo é compreendido como:",
                "Apenas um instrumento mecânico sem cultura",
                "Meio de expressão estética, memória social e construção de identidade",
                "Elemento irrelevante para a linguagem artística",
                "Restrito ao esporte competitivo",
                "Separado do contexto histórico", 1,
                "A linguagem corporal expressa identidades culturais, pertencimento e visão crítica de mundo.");
        upsertQuestion(db, "aia3", "art-identidade-am", "art",
                "O processo de ressignificação artística na cultura local ocorre quando:",
                "Tradições são apagadas para copiar um único padrão",
                "Elementos tradicionais dialogam com novas linguagens e mídias gerando novos sentidos",
                "A arte é proibida de misturar referências",
                "Apenas artistas estrangeiros produzem obras",
                "A obra perde vínculo com seu público", 1,
                "A ressignificação atualiza raízes culturais em diálogo com o presente sem apagar sua origem.");

        upsertTopic(db, "art-circo-cenicas", "art", "Artes cênicas, circo e linguagens visuais e sonoras",
                "O circo como patrimônio cultural amazônico e global, palhaçaria, comédia física, dramaturgia cênica e linguagens visuais e sonoras.",
                45, "https://www.youtube.com/watch?v=B8tR4yM7n1K",
                2, 0, 0, 0, 0, 3, 0, 2);
        upsertQuestion(db, "acc1", "art-circo-cenicas", "art",
                "A arte circense contemporânea caracteriza-se por:",
                "Separar rigidamente música, corpo e cena",
                "Integrar acrobacia, palhaçaria, teatro, música e dramaturgia visual em diálogo social",
                "Usar apenas textos escritos sem movimento",
                "Existir apenas na Antiguidade Romana",
                "Descartar processos criativos coletivos", 1,
                "O circo é uma linguagem híbrida e colaborativa que une expressão corporal, sonora e visual.");
        upsertQuestion(db, "acc2", "art-circo-cenicas", "art",
                "Na tradição do palhaço e da comédia física, o humor nasce frequentemente de:",
                "Exibição de perfeição inatingível",
                "Exposição poética da vulnerabilidade humana, do erro e da crítica social leve",
                "Leitura silenciosa de decretos",
                "Ausência de contato com a plateia",
                "Efeitos exclusivamente digitais", 1,
                "O palhaço transforma tropeços e fragilidades cotidianas em empatia, riso e reflexão crítica.");
        upsertQuestion(db, "acc3", "art-circo-cenicas", "art",
                "A relação entre sonoplastia e iluminação em um espetáculo cênico tem como função:",
                "Apenas preencher silêncios aleatoriamente",
                "Construir atmosferas, ritmo dramático e reforçar sentidos da encenação",
                "Substituir completamente os artistas",
                "Tornar a cena incompreensível",
                "Restringir-se a transmissões de rádio", 1,
                "Som e luz são elementos estruturantes que ampliam a expressividade e a narrativa cênica.");

        upsertTopic(db, "art-contemporanea-digital", "art", "Arte contemporânea, urbana e novas mídias",
                "Corpo performático, arte contemporânea na Amazônia e no Brasil, arte urbana, videoarte, arte digital e ética no espaço público digital.",
                45, "https://www.youtube.com/watch?v=R5mN3cV9j2W",
                3, 0, 0, 0, 0, 0, 3, 3);
        upsertQuestion(db, "acd1", "art-contemporanea-digital", "art",
                "Uma marca central da arte contemporânea e da performance é:",
                "A obrigação de retratar apenas mitologia grega em óleo sobre tela",
                "A participação ativa do público, a hibridização de linguagens e o questionamento do cotidiano",
                "A proibição de usar tecnologia",
                "A exclusão de temas socioambientais",
                "O uso exclusivo de museus fechados", 1,
                "A arte contemporânea desloca limites entre obra, artista, espaço público e espectador.");
        upsertQuestion(db, "acd2", "art-contemporanea-digital", "art",
                "A arte urbana (grafite, murais, intervenções) distingue-se por:",
                "Ser exibida apenas em galerias privadas",
                "Ocupar o espaço público promovendo diálogo visual, memória e debate social na cidade",
                "Ignorar a comunidade local",
                "Usar somente esculturas clássicas",
                "Não possuir intenção estética", 1,
                "Intervenções urbanas democratizam o acesso à arte e ressignificam a paisagem das cidades.");
        upsertQuestion(db, "acd3", "art-contemporanea-digital", "art",
                "A videoarte e a arte digital ampliam a criação artística ao permitir:",
                "A eliminação total da autoria humana",
                "Interatividade, circulação em rede e propostas socioambientais multimídia",
                "Apenas a impressão em papel jornal",
                "O fim de todas as artes cênicas",
                "Uso sem qualquer responsabilidade ética", 1,
                "Novas mídias possibilitam experiências interativas, exigindo também ética e respeito aos direitos autorais.");

        // --- LÍNGUA INGLESA (SIS 1/2/3, Macro, ENEM — 0 no PSC) ---
        upsertTopic(db, "ing-tempos-leitura", "ing", "Tempos simples, contínuos e estratégias de leitura",
                "Simple Present, Simple Past, Simple Future (will/going to), Present Continuous, skimming, scanning, cognatos, falsos cognatos e conectores básicos.",
                45, "https://www.youtube.com/watch?v=W8hJ2kL5p4M",
                3, 0, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "itl1", "ing-tempos-leitura", "ing",
                "Na leitura de textos em inglês, a estratégia de 'scanning' consiste em:",
                "Traduzir palavra por palavra no dicionário",
                "Buscar rapidamente informações específicas (datas, nomes, números) no texto",
                "Ignorar o título e as imagens",
                "Ler o texto de trás para frente",
                "Decorar todos os verbos irregulares", 1,
                "Scanning localiza dados pontuais; skimming capta a ideia geral rapidamente.");
        upsertQuestion(db, "itl2", "ing-tempos-leitura", "ing",
                "Qual das palavras abaixo é um falso cognato (false friend) em inglês?",
                "Information", "Hospital", "Actually", "Important", "Problem", 2,
                "'Actually' significa 'na verdade' (e não 'atualmente', que é 'currently').");
        upsertQuestion(db, "itl3", "ing-tempos-leitura", "ing",
                "Complete corretamente: 'She ___ studying for the exam right now.'",
                "are", "is", "am", "were", "be", 1,
                "No Present Continuous com 'she', usa-se o auxiliar 'is' + verbo com '-ing'.");

        upsertTopic(db, "ing-perfect-modais", "ing", "Perfect Tenses, Modais, Phrasal Verbs e Voz Passiva",
                "Present/Past/Future Perfect, modal verbs (can, must, should, might), quantifiers, phrasal verbs, voz passiva e condicionais Type 0 e 1.",
                50, "https://www.youtube.com/watch?v=P4vN9mK2r7T",
                2, 0, 0, 0, 0, 3, 0, 2);
        upsertQuestion(db, "ipm1", "ing-perfect-modais", "ing",
                "Na frase 'However, the forest still faces serious threats', o conector 'however' indica:",
                "Adição", "Contraste / oposição", "Conclusão", "Causa", "Sequência temporal", 1,
                "'However' equivale a 'contudo / entretanto', introduzindo ideia de contraste.");
        upsertQuestion(db, "ipm2", "ing-perfect-modais", "ing",
                "O phrasal verb 'give up' em 'Never give up on your goals' significa:",
                "Continuar", "Desistir", "Organizar", "Procurar", "Aumentar", 1,
                "'Give up' significa desistir ou abandonar algo.");
        upsertQuestion(db, "ipm3", "ing-perfect-modais", "ing",
                "Na condicional Type 1 'If it rains, we ___ stay home', o auxiliar adequado é:",
                "would have", "will", "did", "was", "had", 1,
                "A First Conditional (possibilidade real no futuro) usa If + Simple Present, will + infinitivo.");

        upsertTopic(db, "ing-avancado-enem", "ing", "Condicionais avançadas, Reported Speech e interpretação crítica",
                "Conditionals (Type 2, 3 e Mixed), Reported Speech, modais de dedução, expressões idiomáticas, estratégias ENEM e vocabulário socioambiental/Amazônia.",
                50, "https://www.youtube.com/watch?v=N7cL5xR8q1V",
                3, 0, 0, 0, 0, 0, 3, 3);
        upsertQuestion(db, "iae1", "ing-avancado-enem", "ing",
                "A expressão idiomática 'piece of cake' em 'That physics question was a piece of cake' significa:",
                "Algo muito difícil", "Algo muito fácil", "Um erro grave", "Um segredo revelado",
                "Estudar até tarde", 1,
                "'Piece of cake' é uma expressão idiomática que indica algo muito fácil de fazer.");
        upsertQuestion(db, "iae2", "ing-avancado-enem", "ing",
                "No discurso indireto (Reported Speech), 'She said: I study every day' transforma-se em:",
                "She said she studied every day.",
                "She said she will study every day.",
                "She said she is studying tomorrow.",
                "She said I study yesterday.",
                "She said she has been study.", 0,
                "No Reported Speech com verbo introdutório no passado ('said'), o Simple Present recua para Simple Past.");
        upsertQuestion(db, "iae3", "ing-avancado-enem", "ing",
                "Em textos sobre sustentabilidade na Amazônia, o termo 'biodiversity loss' refere-se a:",
                "Aumento da biodiversidade", "Perda de biodiversidade", "Lucro agrícola",
                "Crescimento urbano planejado", "Energia renovável", 1,
                "'Loss' significa perda; 'biodiversity loss' designa redução na variedade de espécies e ecossistemas.");
    }

    // =========================================================================
    // 2. MATEMÁTICA (10 tópicos — aderentes a PSC 1/2/3, SIS 1/2/3, Macro e ENEM)
    // =========================================================================
    private static void seedMatematica(SQLiteDatabase db) {
        upsertTopic(db, "mat-porcent", "mat", "Potenciação, razão, proporção, porcentagem e financeira",
                "Potenciação, notação científica, números racionais, grandezas proporcionais, porcentagem, descontos e matemática financeira.",
                55, "https://www.youtube.com/watch?v=nUgL7f5xY6M",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "mpo1", "mat-porcent", "mat",
                "Um produto de R$ 200,00 recebe desconto de 15%. Qual é o valor final?",
                "R$ 165,00", "R$ 170,00", "R$ 175,00", "R$ 180,00", "R$ 185,00", 1,
                "15% de 200 = 30. Logo, 200 − 30 = R$ 170,00.");
        upsertQuestion(db, "mpo2", "mat-porcent", "mat",
                "Um capital de R$ 1.000,00 aplicado a juros simples de 2% ao mês durante 5 meses rende:",
                "R$ 50,00", "R$ 80,00", "R$ 100,00", "R$ 120,00", "R$ 200,00", 2,
                "J = C · i · t = 1000 · 0,02 · 5 = R$ 100,00.");
        upsertQuestion(db, "mpo3", "mat-porcent", "mat",
                "Um valor sofre aumento de 10% e depois desconto de 10%. Em relação ao valor inicial, ele fica:",
                "Igual ao inicial", "1% maior", "1% menor", "2% menor", "10% menor", 2,
                "1,10 × 0,90 = 0,99, ou seja, 1% menor que o valor original.");

        upsertTopic(db, "mat-funcao-afim", "mat", "Noção de função, plano cartesiano e função afim",
                "Variação de grandezas, taxa de variação, plano cartesiano, função do 1º grau (afim) e funções definidas por partes (tarifas progressivas).",
                55, "https://www.youtube.com/watch?v=V7xN4pR2k8L",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "mfa1", "mat-funcao-afim", "mat",
                "Na função afim f(x) = 3x − 12, qual é a raiz (zero da função)?",
                "−4", "3", "4", "12", "−12", 2,
                "Igualando a zero: 3x − 12 = 0 → 3x = 12 → x = 4.");
        upsertQuestion(db, "mfa2", "mat-funcao-afim", "mat",
                "Uma corrida de aplicativo cobra taxa fixa de R$ 5,00 mais R$ 2,50 por km. Para 8 km, o valor é:",
                "R$ 15,00", "R$ 20,00", "R$ 25,00", "R$ 27,50", "R$ 30,00", 2,
                "f(8) = 5 + 2,50 · 8 = 5 + 20 = R$ 25,00.");
        upsertQuestion(db, "mfa3", "mat-funcao-afim", "mat",
                "Em f(x) = ax + b, o coeficiente 'a' representa:",
                "O ponto onde a reta corta o eixo y", "A taxa de variação (inclinação) da reta",
                "O vértice da parábola", "A área sob o eixo x", "O valor máximo absoluto", 1,
                "O coeficiente angular 'a' indica o quanto y varia para cada unidade de aumento em x.");

        upsertTopic(db, "mat-funcoes", "mat", "Função do 2º grau (quadrática)",
                "Raízes, concavidade, vértice, pontos de máximo e mínimo, gráfico e modelagem de problemas quadráticos.",
                60, "https://www.youtube.com/watch?v=Z5aWL_4-k5M",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "mf1", "mat-funcoes", "mat",
                "Qual é a coordenada x do vértice da parábola f(x) = x² − 6x + 5?",
                "−3", "3", "5", "6", "1", 1,
                "xᵥ = −b / (2a) = −(−6) / (2·1) = 3.");
        upsertQuestion(db, "mf2", "mat-funcoes", "mat",
                "As raízes da equação x² − 5x + 6 = 0 são:",
                "1 e 6", "−2 e −3", "2 e 3", "−1 e 6", "0 e 5", 2,
                "Soma = 5 e produto = 6, logo as raízes são 2 e 3.");
        upsertQuestion(db, "mf3", "mat-funcoes", "mat",
                "Se a < 0 em f(x) = ax² + bx + c, a parábola possui:",
                "Concavidade para cima e ponto de mínimo",
                "Concavidade para baixo e ponto de máximo",
                "Nenhuma raiz real", "Duas raízes reais iguais", "Eixo de simetria horizontal", 1,
                "Com a negativo a parábola abre para baixo e o vértice é ponto de máximo.");

        upsertTopic(db, "mat-exp-log", "mat", "Função exponencial e função logarítmica",
                "Equações e funções exponenciais e logarítmicas, propriedades de logaritmos e aplicações em juros compostos e crescimento populacional.",
                60, "https://www.youtube.com/watch?v=M3kP9wR5n1Q",
                2, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "mel1", "mat-exp-log", "mat",
                "Qual é o valor de log₂(32)?",
                "4", "5", "6", "8", "16", 1,
                "Como 2⁵ = 32, temos log₂(32) = 5.");
        upsertQuestion(db, "mel2", "mat-exp-log", "mat",
                "Uma população de bactérias dobra a cada hora segundo P(t) = 100 · 2ᵗ. Após 3 horas haverá:",
                "300", "400", "600", "800", "1600", 3,
                "P(3) = 100 · 2³ = 100 · 8 = 800 bactérias.");
        upsertQuestion(db, "mel3", "mat-exp-log", "mat",
                "Pela propriedade operatória dos logaritmos, log(a · b) (com a, b > 0) é igual a:",
                "log(a) · log(b)", "log(a) + log(b)", "log(a) − log(b)", "log(a) / log(b)", "b · log(a)", 1,
                "O logaritmo do produto é igual à soma dos logaritmos dos fatores.");

        upsertTopic(db, "mat-pa-pg", "mat", "Progressões (PA e PG)",
                "Termo geral, razão, soma dos termos de PA e PG e aplicações em matemática financeira.",
                55, "https://www.youtube.com/watch?v=9xJ7a6P2L1Q",
                2, 0, 3, 0, 0, 3, 0, 2);
        upsertQuestion(db, "mpp1", "mat-pa-pg", "mat",
                "Na PA (3, 7, 11, ...), qual é o 10º termo?",
                "35", "39", "40", "43", "47", 1,
                "a₁₀ = a₁ + 9r = 3 + 9·4 = 3 + 36 = 39.");
        upsertQuestion(db, "mpp2", "mat-pa-pg", "mat",
                "Na PG (2, 6, 18, ...), qual é o 5º termo?",
                "54", "108", "162", "216", "486", 2,
                "Razão q = 3. Logo a₅ = a₁ · q⁴ = 2 · 3⁴ = 2 · 81 = 162.");
        upsertQuestion(db, "mpp3", "mat-pa-pg", "mat",
                "A soma dos 5 primeiros termos da PA (2, 4, 6, 8, 10) é:",
                "20", "25", "30", "35", "40", 2,
                "S₅ = (a₁ + a₅) · 5 / 2 = (2 + 10) · 5 / 2 = 30.");

        upsertTopic(db, "mat-trigo", "mat", "Trigonometria e relações métricas no triângulo",
                "Relações métricas no triângulo retângulo, seno, cosseno, tangente e funções trigonométricas (seno e cosseno).",
                60, "https://www.youtube.com/watch?v=5hR4mK8n2Q0",
                2, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "mtr1", "mat-trigo", "mat",
                "Em um triângulo retângulo, o seno de um ângulo agudo é a razão entre:",
                "Cateto adjacente e hipotenusa", "Cateto oposto e hipotenusa",
                "Cateto oposto e cateto adjacente", "Hipotenusa e cateto oposto",
                "Hipotenusa e cateto adjacente", 1,
                "Por definição: sen(θ) = cateto oposto / hipotenusa.");
        upsertQuestion(db, "mtr2", "mat-trigo", "mat",
                "Qual é o valor exato de sen(30°) + cos(60°)?",
                "0", "1/2", "1", "√2", "√3", 2,
                "sen(30°) = 1/2 e cos(60°) = 1/2. Logo, 1/2 + 1/2 = 1.");
        upsertQuestion(db, "mtr3", "mat-trigo", "mat",
                "Pela relação fundamental da trigonometria, para todo ângulo θ vale:",
                "sen(θ) + cos(θ) = 1", "sen²(θ) + cos²(θ) = 1",
                "sen²(θ) − cos²(θ) = 1", "tg(θ) = cos(θ)/sen(θ)", "sen(θ) · cos(θ) = 1", 1,
                "Pelo Teorema de Pitágoras no ciclo trigonométrico: sen²(θ) + cos²(θ) = 1.");

        upsertTopic(db, "mat-matrizes-sistemas", "mat", "Matrizes, determinantes, sistemas lineares e lógica",
                "Operações com matrizes, determinantes, sistemas de equações lineares, algoritmos, fluxogramas e lógica matemática.",
                55, "https://www.youtube.com/watch?v=K6vR2nL9p4T",
                1, 0, 3, 0, 0, 3, 0, 2);
        upsertQuestion(db, "mms1", "mat-matrizes-sistemas", "mat",
                "O determinante da matriz 2×2 [[4, 2], [1, 3]] é:",
                "6", "10", "12", "14", "−10", 1,
                "det = (4 · 3) − (2 · 1) = 12 − 2 = 10.");
        upsertQuestion(db, "mms2", "mat-matrizes-sistemas", "mat",
                "No sistema linear x + y = 10 e x − y = 4, os valores de x e y são:",
                "x = 6, y = 4", "x = 7, y = 3", "x = 8, y = 2", "x = 5, y = 5", "x = 9, y = 1", 1,
                "Somando as equações: 2x = 14 → x = 7; substituindo, y = 10 − 7 = 3.");
        upsertQuestion(db, "mms3", "mat-matrizes-sistemas", "mat",
                "Em um fluxograma de algoritmo, o símbolo de losango representa:",
                "Início ou fim do programa", "Uma decisão / condição lógica (sim ou não)",
                "Apenas exibição de texto", "Um erro de sintaxe", "Uma matriz inversa", 1,
                "Em fluxogramas, o losango indica teste condicional que desvia o fluxo conforme verdadeiro ou falso.");

        upsertTopic(db, "mat-geoplana", "mat", "Geometria plana e espacial",
                "Áreas, perímetros, semelhança, tesselações, prismas, pirâmides, cilindros, cones, princípio de Cavalieri e volumes em situações reais.",
                60, "https://www.youtube.com/watch?v=th5k6bzSDTA",
                3, 0, 2, 3, 0, 2, 3, 3);
        upsertQuestion(db, "mgp1", "mat-geoplana", "mat",
                "Um triângulo retângulo tem catetos de 6 cm e 8 cm. Qual é a medida da hipotenusa?",
                "9 cm", "10 cm", "12 cm", "14 cm", "15 cm", 1,
                "Pelo Teorema de Pitágoras: h² = 6² + 8² = 36 + 64 = 100 → h = 10 cm.");
        upsertQuestion(db, "mgp2", "mat-geoplana", "mat",
                "Qual é o volume de um prisma reto de área da base 20 cm² e altura 7 cm?",
                "27 cm³", "70 cm³", "120 cm³", "140 cm³", "280 cm³", 3,
                "V = Área da base × altura = 20 × 7 = 140 cm³.");
        upsertQuestion(db, "mgp3", "mat-geoplana", "mat",
                "Um cone e um cilindro possuem mesma base e mesma altura. O volume do cone equivale a:",
                "Metade do volume do cilindro", "Um terço do volume do cilindro",
                "O dobro do volume do cilindro", "O mesmo volume do cilindro", "Um quarto do volume", 1,
                "V_cone = (1/3) · πr²h = (1/3) · V_cilindro.");

        upsertTopic(db, "mat-estat", "mat", "Estatística: gráficos, medidas centrais e dispersão",
                "Leitura crítica de tabelas e gráficos, pesquisas amostrais, média, moda, mediana, desvio-padrão e variância.",
                55, "https://www.youtube.com/watch?v=T4m8R9mY0pA",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "mes1", "mat-estat", "mat",
                "Qual é a média aritmética dos valores 4, 7, 9 e 12?",
                "7", "8", "8,5", "9", "10", 1,
                "(4 + 7 + 9 + 12) / 4 = 32 / 4 = 8.");
        upsertQuestion(db, "mes2", "mat-estat", "mat",
                "No conjunto ordenado {2, 5, 7, 11, 20}, a mediana é:",
                "5", "7", "9", "11", "20", 1,
                "Com 5 elementos ordenados, o termo central (3º) é 7.");
        upsertQuestion(db, "mes3", "mat-estat", "mat",
                "Um conjunto de notas com menor desvio-padrão em relação a outro significa que seus valores são:",
                "Sempre maiores", "Mais homogêneos (próximos da média)",
                "Mais dispersos", "Negativos", "Iguais a zero", 1,
                "Desvio-padrão mede a dispersão; quanto menor, mais regulares e próximos da média são os dados.");

        upsertTopic(db, "mat-prob", "mat", "Análise combinatória e probabilidade",
                "Princípio fundamental da contagem, arranjos, combinações, espaços amostrais e probabilidade com e sem reposição.",
                55, "https://www.youtube.com/watch?v=iNCFx3rK2-w",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "mp1", "mat-prob", "mat",
                "Ao lançar um dado honesto de 6 faces, qual a probabilidade de sair um número primo?",
                "1/6", "1/3", "1/2", "2/3", "5/6", 2,
                "Os números primos em {1, 2, 3, 4, 5, 6} são 2, 3 e 5: 3/6 = 1/2.");
        upsertQuestion(db, "mp2", "mat-prob", "mat",
                "Lançando duas moedas honestas simultaneamente, qual a probabilidade de obter duas caras?",
                "1/2", "1/3", "1/4", "3/4", "1/8", 2,
                "Eventos independentes: P = (1/2) · (1/2) = 1/4.");
        upsertQuestion(db, "mp3", "mat-prob", "mat",
                "Quantas senhas de 3 dígitos distintos podem ser formadas com os algarismos {1, 2, 3, 4}?",
                "12", "16", "24", "64", "4", 2,
                "Pelo princípio fundamental da contagem: 4 × 3 × 2 = 24 senhas.");
    }

    private static void seedInitialLocalUser(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("id", FocoVestRepository.LOCAL_USER_ID);
        cv.put("name", "");
        cv.put("city", "Manaus");
        cv.put("target_course", "");
        cv.put("in_high_school", 1);
        cv.put("high_school_year", 3);
        cv.put("target_exams", "ENEM,PSC,SIS");
        cv.put("daily_hours", 3);
        cv.put("study_days", "1,2,3,4,5");
        cv.put("profile_completed", 0);
        cv.put("last_topic_id", "mat-funcoes");
        db.insertWithOnConflict(FocoVestDbHelper.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    static void upsertTopic(SQLiteDatabase db, String id, String subject, String title,
                            String summary, int minutes, String defaultVideoUrl,
                            int wEnem, int wPsc1, int wPsc2, int wPsc3,
                            int wSis1, int wSis2, int wSis3, int wMacro) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("subject", subject);
        cv.put("title", title);
        cv.put("summary", summary);
        cv.put("minutes", minutes);
        cv.put("default_video_url", defaultVideoUrl);
        cv.put("weight_enem", wEnem);
        cv.put("weight_psc1", wPsc1);
        cv.put("weight_psc2", wPsc2);
        cv.put("weight_psc3", wPsc3);
        cv.put("weight_sis1", wSis1);
        cv.put("weight_sis2", wSis2);
        cv.put("weight_sis3", wSis3);
        cv.put("weight_macro", wMacro);

        long inserted = db.insertWithOnConflict(
                FocoVestDbHelper.TABLE_TOPICS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if (inserted == -1L) {
            cv.remove("id");
            db.update(FocoVestDbHelper.TABLE_TOPICS, cv, "id = ?", new String[]{id});
        }
    }

    static void upsertQuestion(SQLiteDatabase db, String id, String topicId, String subject,
                               String prompt, String a, String b, String c, String d, String e,
                               int correctIndex, String explanation) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("topic_id", topicId);
        cv.put("subject", subject);
        cv.put("prompt", prompt);
        cv.put("option_a", a);
        cv.put("option_b", b);
        cv.put("option_c", c);
        cv.put("option_d", d);
        cv.put("option_e", e);
        cv.put("correct_index", correctIndex);
        cv.put("explanation", explanation);

        long inserted = db.insertWithOnConflict(
                FocoVestDbHelper.TABLE_QUESTIONS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if (inserted == -1L) {
            cv.remove("id");
            db.update(FocoVestDbHelper.TABLE_QUESTIONS, cv, "id = ?", new String[]{id});
        }
    }
}
