package com.example.focovest.data;

import android.database.sqlite.SQLiteDatabase;

import static com.example.focovest.data.DatabaseSeeder.upsertQuestion;
import static com.example.focovest.data.DatabaseSeeder.upsertTopic;

/**
 * Continuação do seeder oficial com Ciências da Natureza (Física, Química, Biologia)
 * e Ciências Humanas (História, Geografia, Filosofia, Sociologia), aderente ao
 * Conteúdo Programático de PSC (1ª/2ª/3ª), SIS (1ª/2ª/3ª), Macro e ENEM.
 */
final class DatabaseSeederPart2 {

    private DatabaseSeederPart2() {}

    // =========================================================================
    // 3. CIÊNCIAS DA NATUREZA: FÍSICA (8), QUÍMICA (8), BIOLOGIA (7)
    // =========================================================================
    static void seedNatureza(SQLiteDatabase db) {
        // --- FÍSICA ---
        upsertTopic(db, "fis-cinematica", "fis", "Medidas físicas, Cinemática e gráficos do movimento",
                "Notação científica, algarismos significativos, MRU, MRUV, equação de Torricelli e leitura de gráficos s × t e v × t.",
                60, "https://www.youtube.com/watch?v=x0w7g2jH1eE",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "fc1", "fis-cinematica", "fis",
                "Um carro parte do repouso com aceleração constante a = 2 m/s². Sua velocidade após 5 s é:",
                "2 m/s", "5 m/s", "7 m/s", "10 m/s", "25 m/s", 3,
                "v = v₀ + a·t = 0 + 2·5 = 10 m/s.");
        upsertQuestion(db, "fc2", "fis-cinematica", "fis",
                "No gráfico velocidade × tempo (v × t), a área sob a curva representa:",
                "Aceleração", "Deslocamento", "Força", "Tempo", "Energia", 1,
                "A área sob o gráfico v × t fornece numericamente o deslocamento Δs.");
        upsertQuestion(db, "fc3", "fis-cinematica", "fis",
                "A equação de Torricelli relaciona diretamente:",
                "v, v₀, a e Δs (sem o tempo)", "Força, massa e aceleração",
                "Pressão e volume", "Calor e temperatura", "s, v e t apenas", 0,
                "v² = v₀² + 2·a·Δs independe explicitamente do tempo.");

        upsertTopic(db, "fis-dinamica", "fis", "Dinâmica, Leis de Newton e Física dos fluidos",
                "Leis de Newton, força de atrito, plano inclinado, força centrípeta, hidrostática e física aplicada ao saneamento básico.",
                60, "https://www.youtube.com/watch?v=kJ8pL3mR7sA",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "fdi1", "fis-dinamica", "fis",
                "Um bloco de massa 4 kg sofre aceleração de 3 m/s². A força resultante sobre ele vale:",
                "1,3 N", "7 N", "12 N", "16 N", "36 N", 2,
                "Pela 2ª Lei de Newton: F = m · a = 4 · 3 = 12 N.");
        upsertQuestion(db, "fdi2", "fis-dinamica", "fis",
                "A tendência de um corpo manter seu estado de repouso ou MRU corresponde à:",
                "Inércia (1ª Lei de Newton)", "Ação e reação", "Gravitação universal",
                "Lei de Ohm", "Lei de Snell", 0,
                "A 1ª Lei de Newton (Inércia) explica a resistência à mudança no estado de movimento.");
        upsertQuestion(db, "fdi3", "fis-dinamica", "fis",
                "Pelo Princípio de Arquimedes, um corpo totalmente imerso em um fluido sofre empuxo igual:",
                "À sua massa", "Ao peso do volume de fluido deslocado",
                "À temperatura da água", "À sua velocidade", "Zero em água doce", 1,
                "O empuxo aponta verticalmente para cima e tem módulo igual ao peso do líquido deslocado.");

        upsertTopic(db, "fis-energia", "fis", "Energia mecânica, quantidade de movimento e Astronomia",
                "Energia cinética e potencial, conservação da energia e quantidade de movimento, gravitação, evolução estelar e teoria do Big Bang.",
                55, "https://www.youtube.com/watch?v=B9vY4kS2pW8",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "fen1", "fis-energia", "fis",
                "Qual é a energia cinética de um corpo de 2 kg movendo-se a 4 m/s?",
                "4 J", "8 J", "16 J", "32 J", "64 J", 2,
                "Ec = (m · v²) / 2 = (2 · 16) / 2 = 16 J.");
        upsertQuestion(db, "fen2", "fis-energia", "fis",
                "Um objeto de 5 kg a 2 m de altura (g = 10 m/s²) possui energia potencial gravitacional de:",
                "10 J", "20 J", "50 J", "100 J", "200 J", 3,
                "Ep = m · g · h = 5 · 10 · 2 = 100 J.");
        upsertQuestion(db, "fen3", "fis-energia", "fis",
                "Em um sistema mecanicamente isolado (sem atrito), durante a queda livre de um corpo:",
                "A energia mecânica total é conservada",
                "A energia potencial aumenta",
                "A energia cinética diminui",
                "A massa varia",
                "A aceleração é nula", 0,
                "A energia potencial gravitacional converte-se em cinética mantendo E_mec constante.");

        upsertTopic(db, "fis-termologia", "fis", "Calorimetria, Termodinâmica e propriedades térmicas",
                "Escalas termométricas, dilatação térmica, condução/convecção/radiação, calorimetria, leis da termodinâmica, máquinas térmicas e efeito estufa.",
                55, "https://www.youtube.com/watch?v=T2kL8mV4p1R",
                3, 2, 3, 1, 2, 3, 1, 3);
        upsertQuestion(db, "fte1", "fis-termologia", "fis",
                "A transferência de calor que ocorre no vácuo (como a energia do Sol chegando à Terra) é a:",
                "Condução", "Convecção", "Irradiação (radiação térmica)", "Evaporação", "Fusão", 2,
                "A radiação propaga-se por ondas eletromagnéticas e não depende de meio material.");
        upsertQuestion(db, "fte2", "fis-termologia", "fis",
                "A temperatura de 27 °C corresponde na escala Kelvin (K) a aproximadamente:",
                "246 K", "273 K", "300 K", "310 K", "373 K", 2,
                "T(K) = T(°C) + 273 = 27 + 273 = 300 K.");
        upsertQuestion(db, "fte3", "fis-termologia", "fis",
                "Na 2ª Lei da Termodinâmica, nenhuma máquina térmica operando em ciclos pode ter rendimento:",
                "Menor que 50%", "Igual a 100%", "Maior que 10%", "Entre 20% e 40%", "Dependente da fonte fria", 1,
                "Sempre há rejeição de parte do calor para a fonte fria, impedindo rendimento térmico de 100%.");

        upsertTopic(db, "fis-eletro", "fis", "Eletrostática, Eletrodinâmica, Eletromagnetismo e Energia",
                "Processos de eletrização, Lei de Ohm, circuitos, potência e consumo consciente, choque elétrico, usinas geradoras e eletromagnetismo.",
                60, "https://www.youtube.com/watch?v=R6tM2nX9cQ4",
                3, 2, 3, 3, 2, 3, 3, 3);
        upsertQuestion(db, "fel1", "fis-eletro", "fis",
                "Um resistor de 10 Ω é submetido a uma tensão de 20 V. A corrente elétrica que o atravessa é:",
                "0,5 A", "2 A", "10 A", "30 A", "200 A", 1,
                "Pela 1ª Lei de Ohm: U = R · i → i = 20 / 10 = 2 A.");
        upsertQuestion(db, "fel2", "fis-eletro", "fis",
                "Um chuveiro de 220 V percorrido por corrente de 20 A dissipa potência elétrica de:",
                "11 W", "240 W", "2.200 W", "4.400 W", "8.800 W", 3,
                "P = U · i = 220 · 20 = 4.400 W.");
        upsertQuestion(db, "fel3", "fis-eletro", "fis",
                "Dois resistores de 6 Ω ligados em série apresentam resistência equivalente igual a:",
                "3 Ω", "6 Ω", "12 Ω", "18 Ω", "36 Ω", 2,
                "Em série, somam-se as resistências: Req = 6 + 6 = 12 Ω.");

        upsertTopic(db, "fis-ondas", "fis", "Ondulatória e radiações eletromagnéticas",
                "Velocidade, frequência e comprimento de onda, fenômenos ondulatórios, propagação de sinais e espectro eletromagnético (ionizante e não ionizante).",
                55, "https://www.youtube.com/watch?v=L4pH7dN3sK2",
                3, 1, 3, 3, 1, 3, 3, 3);
        upsertQuestion(db, "fo1", "fis-ondas", "fis",
                "Uma onda com frequência f = 50 Hz e comprimento λ = 2 m possui velocidade de:",
                "25 m/s", "52 m/s", "100 m/s", "48 m/s", "200 m/s", 2,
                "v = λ · f = 2 · 50 = 100 m/s.");
        upsertQuestion(db, "fo2", "fis-ondas", "fis",
                "O som propagando-se no ar é classificado como uma onda:",
                "Eletromagnética e transversal", "Mecânica e longitudinal",
                "Mecânica e transversal", "Eletromagnética e longitudinal",
                "Que se propaga no vácuo", 1,
                "O som necessita de meio material e oscila na mesma direção em que se propaga.");
        upsertQuestion(db, "fo3", "fis-ondas", "fis",
                "O fenômeno em que a onda contorna obstáculos ou fendas é denominado:",
                "Reflexão", "Refração", "Difração", "Polarização", "Absorção", 2,
                "Difração é a capacidade das ondas contornarem obstáculos de dimensões comparáveis a λ.");

        upsertTopic(db, "fis-optica", "fis", "Óptica geométrica, lentes e visão humana",
                "Reflexão, refração, espelhos, lentes esféricas, anatomia óptica do olho humano, miopia, hipermetropia, astigmatismo e tecnologia assistiva.",
                55, "https://www.youtube.com/watch?v=J8pK4rW2m5L",
                2, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "fop1", "fis-optica", "fis",
                "A miopia ocorre quando a imagem se forma antes da retina e é corrigida com lentes:",
                "Convergentes", "Divergentes", "Planas", "Opacas", "Cilíndricas exclusivamente", 1,
                "Lentes divergentes abrem os raios luminosos para que o foco seja deslocado até a retina.");
        upsertQuestion(db, "fop2", "fis-optica", "fis",
                "A mudança na velocidade e direção da luz ao passar do ar para a água caracteriza a:",
                "Reflexão total", "Refração luminosa", "Difração sonora", "Polarização", "Absorção total", 1,
                "Na refração, a luz muda de meio de propagação alterando velocidade e comprimento de onda.");
        upsertQuestion(db, "fop3", "fis-optica", "fis",
                "Em um espelho plano, a imagem de um objeto real é sempre:",
                "Real e invertida", "Virtual, direita e do mesmo tamanho do objeto",
                "Real e ampliada", "Virtual e menor", "Invertida e reduzida", 1,
                "Espelhos planos produzem imagens virtuais, direitas, simétricas e de mesmo tamanho.");

        upsertTopic(db, "fis-moderna-nuclear", "fis", "Física Moderna, Radioatividade e Energia Nuclear",
                "Radiações ionizantes, fissão e fusão nuclear, decaimento radioativo, aplicações médicas/industriais, riscos radiológicos e EPIs/EPCs.",
                50, "https://www.youtube.com/watch?v=V4mR7kN2q9P",
                2, 0, 2, 3, 0, 2, 3, 2);
        upsertQuestion(db, "fmn1", "fis-moderna-nuclear", "fis",
                "Nas usinas nucleares atuais, a energia térmica para gerar eletricidade provém principalmente da:",
                "Combustão de carvão", "Fissão controlada de núcleos pesados (como urânio-235)",
                "Fusão fria de hidrogênio", "Queda d'água", "Fermentação biológica", 1,
                "Na fissão nuclear, o núcleo pesado se divide liberando grande quantidade de energia térmica.");
        upsertQuestion(db, "fmn2", "fis-moderna-nuclear", "fis",
                "Qual tipo de radiação possui maior poder de penetração na matéria?",
                "Partícula alfa (α)", "Partícula beta (β)", "Radiação gama (γ)",
                "Ondas de rádio", "Infravermelho", 2,
                "Raios gama são ondas eletromagnéticas de altíssima frequência e grande penetração.");
        upsertQuestion(db, "fmn3", "fis-moderna-nuclear", "fis",
                "Na medicina, o uso controlado de radiações ionizantes é aplicado em:",
                "Apenas fabricação de plástico",
                "Radiodiagnóstico (raios X/tomografia) e radioterapia no combate a tumores",
                "Somente agricultura orgânica",
                "Iluminação pública residencial",
                "Purificação sonora", 1,
                "O controle radiológico permite benefícios médicos e industriais minimizando danos biológicos.");

        // --- QUÍMICA ---
        upsertTopic(db, "qui-materia-atomos", "qui", "Matéria, misturas, modelos atômicos, Tabela e ligações",
                "Estados físicos, separação de misturas, Dalton/Thomson/Rutherford/Bohr, Tabela Periódica, ligações iônica/covalente/metálica e forças intermoleculares.",
                60, "https://www.youtube.com/watch?v=H5mN2kR8v4P",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "qma1", "qui-materia-atomos", "qui",
                "Qual método é mais adequado para separar água e sal dissolvido recuperando ambos os componentes?",
                "Filtração simples", "Decantação", "Destilação simples", "Peneiração", "Imantação", 2,
                "A destilação simples evapora e condensa a água, separando-a do sal (sólido dissolvido).");
        upsertQuestion(db, "qma2", "qui-materia-atomos", "qui",
                "No modelo atômico de Rutherford-Bohr, os elétrons movimentam-se:",
                "Fixos no núcleo positivo", "Em camadas (níveis) quantizados de energia ao redor do núcleo",
                "Sem carga elétrica", "Apenas em átomos gasosos", "Misturados aos prótons no pudim de passas", 1,
                "Bohr propôs órbitas estacionárias com energias bem definidas; saltos emitem ou absorvem fótons.");
        upsertQuestion(db, "qma3", "qui-materia-atomos", "qui",
                "A ligação química formada por compartilhamento de pares de elétrons entre ametais (como na H₂O) é:",
                "Iônica", "Covalente", "Metálica", "Nuclear", "Gravitacional", 1,
                "Ametais compartilham elétrons formando ligações covalentes moleculares.");

        upsertTopic(db, "qui-estequio", "qui", "Leis ponderais, gases, reações inorgânicas e Estequiometria",
                "Leis de Lavoisier, Proust e Dalton, estudo dos gases, reações inorgânicas, mol, massa molar, reagente limitante e cálculos estequiométricos.",
                60, "https://www.youtube.com/watch?v=C3mT8qP2k7L",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "qe1", "qui-estequio", "qui",
                "Quantos mols há em 36 g de água (H₂O, massa molar = 18 g/mol)?",
                "0,5", "1", "2", "18", "36", 2,
                "n = m / M = 36 / 18 = 2 mol.");
        upsertQuestion(db, "qe2", "qui-estequio", "qui",
                "A Lei de Lavoisier (Conservação das Massas) afirma que, em sistema fechado:",
                "A massa dos produtos é sempre menor que a dos reagentes",
                "A soma das massas dos reagentes é igual à soma das massas dos produtos",
                "Os gases não possuem massa",
                "Átomos desaparecem na combustão",
                "A temperatura não varia", 1,
                "Na natureza nada se cria, nada se perde; a massa total se conserva nas reações químicas.");
        upsertQuestion(db, "qe3", "qui-estequio", "qui",
                "A constante de Avogadro vale aproximadamente:",
                "6,02 × 10²³", "3,0 × 10⁸", "9,8", "1,6 × 10⁻¹⁹", "8,31", 0,
                "1 mol contém aproximadamente 6,02 × 10²³ entidades.");

        upsertTopic(db, "qui-termo-cinetica", "qui", "Termoquímica, Cinética Química e combustíveis",
                "Reações endo/exotérmicas, entalpia, Lei de Hess, combustíveis fósseis, biocombustíveis, velocidade de reação, colisões e catalisadores.",
                60, "https://www.youtube.com/watch?v=Q8rK3vM5n2L",
                3, 1, 3, 0, 1, 3, 0, 3);
        upsertQuestion(db, "qtc1", "qui-termo-cinetica", "qui",
                "Uma reação química com ΔH < 0 é classificada como:",
                "Endotérmica (absorve calor)", "Exotérmica (libera calor)",
                "Nuclear apenas", "Irreversível sempre", "Sem variação energética", 1,
                "Variação de entalpia negativa indica que os produtos têm menor entalpia e houve liberação de calor.");
        upsertQuestion(db, "qtc2", "qui-termo-cinetica", "qui",
                "O papel de um catalisador em uma reação química é:",
                "Aumentar a energia de ativação",
                "Oferecer um caminho alternativo de menor energia de ativação, acelerando a reação",
                "Alterar o ΔH final da reação",
                "Ser totalmente consumido como reagente",
                "Impedir o equilíbrio químico", 1,
                "Catalisadores diminuem a energia de ativação sem alterar ΔH nem serem consumidos no processo global.");
        upsertQuestion(db, "qtc3", "qui-termo-cinetica", "qui",
                "Entre as opções abaixo, são exemplos de biocombustíveis renováveis:",
                "Carvão mineral e diesel fóssil", "Etanol, biodiesel e biogás",
                "Gasolina e querosene de petróleo", "Gás natural e nafta", "Urânio e plutônio", 1,
                "Biocombustíveis são obtidos de biomassa renovável (cana-de-açúcar, oleaginosas, matéria orgânica).");

        upsertTopic(db, "qui-eletroquimica", "qui", "Eletroquímica: pilhas, baterias, eletrólise e oxirredução",
                "Número de oxidação, oxidação e redução, Pilha de Daniell, pilhas e baterias, eletrólise, corrosão e proteção de metais.",
                55, "https://www.youtube.com/watch?v=F9kT2wP6m3R",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "qel1", "qui-eletroquimica", "qui",
                "Em uma pilha eletroquímica (como a de Daniell), o eletrodo onde ocorre oxidação (perda de elétrons) é o:",
                "Cátodo (polo positivo)", "Ânodo (polo negativo)", "Ponte salina",
                "Eletrólito inerte", "Catalisador", 1,
                "Na pilha, o ânodo sofre oxidação (libera elétrons) e é o polo negativo.");
        upsertQuestion(db, "qel2", "qui-eletroquimica", "qui",
                "Diferentemente da pilha, a eletrólise é um processo:",
                "Espontâneo que gera eletricidade",
                "Não espontâneo que utiliza energia elétrica para provocar reações químicas",
                "Exclusivo de materiais orgânicos",
                "Sem transferência de elétrons",
                "Que dispensa fonte externa", 1,
                "Na eletrólise, uma fonte externa força uma reação redox não espontânea (ex.: obtenção de alumínio).");
        upsertQuestion(db, "qel3", "qui-eletroquimica", "qui",
                "O uso de 'metais de sacrifício' (como magnésio ou zinco) em cascos de navios protege o ferro contra:",
                "Ebulição", "Corrosão (oxidação), pois o metal de sacrifício oxida-se preferencialmente",
                "Solidificação", "Radioatividade", "Fusão nuclear", 1,
                "O metal de menor potencial de redução oxida primeiro, preservando a estrutura de ferro.");

        upsertTopic(db, "qui-solucoes", "qui", "Soluções, ácidos/bases e Equilíbrio Químico",
                "Concentração comum e molar, diluição, ácidos, bases, sais e óxidos, escala de pH, constante de equilíbrio e deslocamento (Le Chatelier).",
                55, "https://www.youtube.com/watch?v=E7nV3rT8m2L",
                3, 2, 3, 0, 2, 3, 0, 3);
        upsertQuestion(db, "qso1", "qui-solucoes", "qui",
                "Dissolvendo 20 g de sal em água até completar 0,5 L de solução, a concentração comum é:",
                "10 g/L", "20 g/L", "40 g/L", "50 g/L", "100 g/L", 2,
                "C = m / V = 20 g / 0,5 L = 40 g/L.");
        upsertQuestion(db, "qso2", "qui-solucoes", "qui",
                "Em uma solução aquosa a 25 °C, um valor de pH = 3 indica meio:",
                "Neutro", "Ácido", "Básico (alcalino)", "Isotérmico", "Saturado", 1,
                "A 25 °C, pH < 7 indica caráter ácido, pH = 7 neutro e pH > 7 básico.");
        upsertQuestion(db, "qso3", "qui-solucoes", "qui",
                "Um sistema químico atinge equilíbrio dinâmico quando:",
                "Os reagentes acabam completamente",
                "As velocidades das reações direta e inversa se igualam e as concentrações permanecem constantes",
                "A temperatura chega a 0 K",
                "A pressão cai a zero",
                "A reação para de ocorrer em nível molecular", 1,
                "No equilíbrio químico dinâmico, v_direta = v_inversa mantendo concentrações constantes.");

        upsertTopic(db, "qui-radioatividade", "qui", "Radioatividade, decaimento e acidentes nucleares",
                "Radioisótopos, emissões alfa, beta e gama, meia-vida, leis de decaimento e acidentes históricos (Chernobyl, Fukushima, Goiânia/Césio-137).",
                50, "https://www.youtube.com/watch?v=S6mK9vR3p1N",
                2, 1, 3, 0, 1, 3, 0, 2);
        upsertQuestion(db, "qra1", "qui-radioatividade", "qui",
                "Um radioisótopo com massa inicial de 80 g tem meia-vida de 10 dias. Após 20 dias restarão:",
                "40 g", "20 g", "10 g", "5 g", "0 g", 1,
                "Em 20 dias passam-se 2 meias-vidas: 80 g → 40 g → 20 g.");
        upsertQuestion(db, "qra2", "qui-radioatividade", "qui",
                "O grave acidente radiológico ocorrido em Goiânia (1987) envolveu o manuseio indevido de uma cápsula de:",
                "Carbono-14", "Césio-137", "Hélio-4", "Oxigênio-16", "Sódio-23", 1,
                "A violação de um aparelho de radioterapia abandonado expôs a população ao Césio-137.");
        upsertQuestion(db, "qra3", "qui-radioatividade", "qui",
                "Ao emitir uma partícula alfa (⁴₂α), o número atômico (Z) do átomo radioativo:",
                "Aumenta em 2 unidades", "Diminui em 2 unidades", "Permanece igual",
                "Aumenta em 4 unidades", "Diminui em 1 unidade", 1,
                "Na emissão alfa, o núcleo perde 2 prótons (Z − 2) e 2 nêutrons (A − 4).");

        upsertTopic(db, "qui-organica", "qui", "Química Orgânica, Petróleo, Bioquímica e Biotecnologia",
                "Hidrocarbonetos, petróleo e petroquímica, funções orgânicas (álcool, ácido carboxílico, éster, amina), polímeros, bioquímica e transgênicos.",
                60, "https://www.youtube.com/watch?v=W2xN6kP9r4V",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "qor1", "qui-organica", "qui",
                "A função orgânica caracterizada pela hidroxila (−OH) ligada a carbono saturado é:",
                "Álcool", "Cetona", "Éter", "Amina", "Aldeído", 0,
                "Álcoois apresentam grupo −OH ligado diretamente a carbono saturado (sp³).");
        upsertQuestion(db, "qor2", "qui-organica", "qui",
                "O grupo funcional carboxila (−COOH) identifica qual função orgânica?",
                "Éster", "Ácido carboxílico", "Amida", "Hidrocarboneto", "Fenol", 1,
                "A carboxila (−C(=O)OH) caracteriza os ácidos carboxílicos.");
        upsertQuestion(db, "qor3", "qui-organica", "qui",
                "O processo físico usado nas refinarias para separar frações do petróleo (gás, gasolina, diesel) é:",
                "Filtração a vácuo", "Destilação fracionada", "Decantação simples",
                "Sublimação", "Centrifugação", 1,
                "A destilação fracionada separa componentes líquidos com base em suas diferentes faixas de ebulição.");

        upsertTopic(db, "qui-ambiental", "qui", "Química Ambiental, Química Verde e sustentabilidade",
                "Tratamento de água e esgoto, reciclagem, lixo eletrônico/mineração urbana, ciclos biogeoquímicos, camada de ozônio, agrotóxicos e Química Verde.",
                50, "https://www.youtube.com/watch?v=P4sM8vL1k6D",
                3, 2, 2, 3, 2, 2, 3, 3);
        upsertQuestion(db, "qam1", "qui-ambiental", "qui",
                "Os principais gases responsáveis pela chuva ácida são óxidos de:",
                "Sódio e potássio", "Enxofre (SO₂/SO₃) e nitrogênio (NOₓ)",
                "Hélio e neônio", "Cálcio e magnésio", "Ouro e prata", 1,
                "SO₂ e NOₓ reagem com a água atmosférica formando ácidos sulfúrico e nítrico.");
        upsertQuestion(db, "qam2", "qui-ambiental", "qui",
                "Na estação de tratamento de água (ETA), a etapa que elimina microrganismos patogênicos é:",
                "Decantação", "Desinfecção (cloração)", "Floculação", "Peneiração", "Gradeamento", 1,
                "O cloro (ou ozônio/UV) elimina bactérias e vírus tornando a água potável.");
        upsertQuestion(db, "qam3", "qui-ambiental", "qui",
                "A 'mineração urbana' ligada ao lixo eletrônico consiste em:",
                "Queimar celulares a céu aberto",
                "Recuperar metais valiosos e evitar contaminação por metais pesados por meio da reciclagem",
                "Enterrar baterias em rios",
                "Produzir aparelhos descartáveis",
                "Aumentar o uso de mercúrio", 1,
                "A economia circular recupera cobre, ouro e lítio de equipamentos eletrônicos descartados corretamente.");

        // --- BIOLOGIA ---
        upsertTopic(db, "bio-origem-classif", "bio", "Origem da vida, seres vivos e Classificação Filogenética",
                "Método científico, biogênese vs. abiogênese, Oparin/Haldane, sistemática filogenética, vírus, bactérias, protozoários, algas, fungos, plantas e animais.",
                55, "https://www.youtube.com/watch?v=N2rK8pL5v3M",
                2, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "boc1", "bio-origem-classif", "bio",
                "A hipótese de Oparin e Haldane propôs que as primeiras moléculas orgânicas formaram-se:",
                "Apenas dentro de células eucarióticas modernas",
                "Nos mares primitivos a partir de gases atmosféricos sob ação de descargas elétricas e radiação UV",
                "Exclusivamente por geração espontânea de insetos",
                "Em atmosfera rica em gás oxigênio (O₂)",
                "Após o surgimento dos mamíferos", 1,
                "A evolução química nos oceanos primitivos originou compostos orgânicos e coacervados.");
        upsertQuestion(db, "boc2", "bio-origem-classif", "bio",
                "Na sistemática filogenética atual, os organismos são classificados prioritariamente segundo:",
                "Apenas a cor e o tamanho", "Grau de parentesco evolutivo e ancestrais comuns",
                "O continente onde foram descobertos", "Ordem alfabética", "Utilidade comercial", 1,
                "A cladística agrupa táxons com base em ancestralidade comum e novidades evolutivas.");
        upsertQuestion(db, "boc3", "bio-origem-classif", "bio",
                "Os vírus diferem dos demais seres vivos celulares principalmente por:",
                "Possuírem mitocôndrias próprias",
                "Serem acelulares e parasitas intracelulares obrigatórios",
                "Realizarem fotossíntese",
                "Possuírem parede celular de celulose",
                "Reproduzirem-se por mitose", 1,
                "Vírus não possuem metabolismo próprio e dependem da maquinaria celular do hospedeiro.");

        upsertTopic(db, "bio-citologia", "bio", "Citologia, Bioquímica Celular e Metabolismo Energético",
                "Células procariontes e eucariontes, organelas, água, sais, carboidratos, lipídios, proteínas, DNA/RNA, fotossíntese, respiração, mitose e meiose.",
                60, "https://www.youtube.com/watch?v=N6kR2wP4v9T",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "bci1", "bio-citologia", "bio",
                "Qual organela é responsável pela respiração celular e produção de grande parte do ATP?",
                "Ribossomo", "Mitocôndria", "Lisossomo", "Complexo de Golgi", "Centríolo", 1,
                "As mitocôndrias realizam o ciclo de Krebs e a fosforilação oxidativa gerando ATP.");
        upsertQuestion(db, "bci2", "bio-citologia", "bio",
                "A principal diferença entre células procarióticas e eucarióticas é que as eucarióticas possuem:",
                "DNA apenas fora da célula", "Núcleo delimitado por carioteca e organelas membranosas",
                "Ausência de membrana plasmática", "Ausência de ribossomos", "Apenas RNA", 1,
                "Eucariontes apresentam carioteca envolvendo o material genético e sistema de endomembranas.");
        upsertQuestion(db, "bci3", "bio-citologia", "bio",
                "A divisão celular que reduz pela metade o número de cromossomos, formando gametas, é a:",
                "Mitose", "Meiose", "Fagocitose", "Osmose", "Pinocitose", 1,
                "A meiose (reducional) gera células haploides promovendo variabilidade genética.");

        upsertTopic(db, "bio-ecologia", "bio", "Ecologia, Ciclos Biogeoquímicos e Sustentabilidade",
                "Ecossistemas, cadeias/teias alimentares, pirâmides ecológicas, ciclos (água, carbono, nitrogênio, fósforo), efeito estufa, Kyoto/Paris e bioma amazônico.",
                60, "https://www.youtube.com/watch?v=M8tK5qP2w7L",
                3, 0, 3, 3, 0, 3, 3, 3);
        upsertQuestion(db, "bec1", "bio-ecologia", "bio",
                "Em uma cadeia alimentar, os organismos que produzem matéria orgânica por fotossíntese são:",
                "Consumidores primários", "Produtores", "Decompositores", "Parasitas", "Predadores de topo", 1,
                "Produtores (plantas, algas e cianobactérias) convertem energia luminosa em energia química.");
        upsertQuestion(db, "bec2", "bio-ecologia", "bio",
                "O fluxo de energia ao longo dos níveis tróficos de um ecossistema é sempre:",
                "Cíclico e crescente", "Unidirecional e decrescente",
                "Constante em todos os níveis", "Maior nos carnívoros de topo", "Independente do Sol", 1,
                "Parte da energia é dissipada como calor a cada nível trófico; apenas a matéria é reciclada.");
        upsertQuestion(db, "bec3", "bio-ecologia", "bio",
                "No ciclo do nitrogênio, a conversão de N₂ atmosférico em amônia assimilável depende de:",
                "Apenas herbívoros", "Bactérias fixadoras de nitrogênio (como Rhizobium em leguminosas)",
                "Vírus aquáticos", "Queima de carvão", "Erosão marinha", 1,
                "Bactérias fixadoras transformam o N₂ gasoso em compostos nitrogenados aproveitáveis pelas plantas.");

        upsertTopic(db, "bio-fisiologia", "bio", "Fisiologia Humana (sistemas do corpo)",
                "Sistemas digestório, respiratório, circulatório, excretor, nervoso e endócrino, homeostase e impactos fisiológicos da poluição.",
                60, "https://www.youtube.com/watch?v=H4wV9mK3x2S",
                2, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "bfi1", "bio-fisiologia", "bio",
                "A hematose (troca de O₂ e CO₂ entre o ar inspirado e o sangue) ocorre nos:",
                "Brônquios", "Alvéolos pulmonares", "Esôfago", "Rins", "Ventrículos", 1,
                "Nos alvéolos pulmonares ocorre a difusão gasosa entre o ar e os capilares sanguíneos.");
        upsertQuestion(db, "bfi2", "bio-fisiologia", "bio",
                "Qual hormônio pancreático reduz a glicemia facilitando a entrada de glicose nas células?",
                "Adrenalina", "Insulina", "Glucagon", "Melatonina", "Aldosterona", 1,
                "A insulina promove a captação de glicose pelas células; sua falta ou resistência causa diabetes.");
        upsertQuestion(db, "bfi3", "bio-fisiologia", "bio",
                "A unidade funcional dos rins responsável pela filtração do sangue e formação da urina é o:",
                "Neurônio", "Néfron", "Alvéolo", "Sarcomero", "Osteócito", 1,
                "Os néfrons filtram o plasma, reabsorvem substâncias úteis e excretam ureia e excessos.");

        upsertTopic(db, "bio-saude-epidemiologia", "bio", "Ciência, Saúde Pública, Virologia e Epidemiologia",
                "Fundamentos de virologia e epidemiologia, epidemias e pandemias, vacinação/imunização, doenças negligenciadas, saúde das juventudes e EPIs/EPCs.",
                55, "https://www.youtube.com/watch?v=D7vL4kM9p2R",
                3, 0, 3, 2, 0, 3, 2, 3);
        upsertQuestion(db, "bse1", "bio-saude-epidemiologia", "bio",
                "Qual é a diferença fundamental entre vacina e soro terapêutico?",
                "Ambos contêm apenas antibióticos",
                "A vacina induz imunização ativa (memória imunológica) preventiva; o soro fornece anticorpos prontos (imunização passiva)",
                "O soro previne doenças por toda a vida",
                "A vacina só funciona contra picadas de cobra",
                "Não há diferença biológica", 1,
                "Vacinas estimulam o sistema imune a produzir anticorpos e células de memória; soros agem imediatamente.");
        upsertQuestion(db, "bse2", "bio-saude-epidemiologia", "bio",
                "Na região Amazônica, a malária é causada por protozoários do gênero Plasmodium e transmitida por:",
                "Água contaminada diretamente", "Fêmea do mosquito Anopheles",
                "Mosquito Aedes aegypti exclusivamente", "Contato com solo seco", "Vírus respiratório", 1,
                "O vetor da malária é a fêmea infectada do mosquito-prego (Anopheles).");
        upsertQuestion(db, "bse3", "bio-saude-epidemiologia", "bio",
                "Chamam-se 'doenças negligenciadas' (como doença de Chagas e leishmaniose) aquelas que:",
                "Não possuem agente causador conhecido",
                "Afetam principalmente populações vulneráveis e recebem menor investimento em pesquisa e tratamento",
                "Ocorrem apenas em regiões polares",
                "São causadas exclusivamente por genética",
                "Já foram totalmente erradicadas no planeta", 1,
                "Estão fortemente ligadas à desigualdade social, falta de saneamento e acesso precário à saúde.");

        upsertTopic(db, "bio-genetica", "bio", "Genética, Hereditariedade e Biotecnologia",
                "1ª e 2ª Leis de Mendel, heredogramas, grupos sanguíneos, Projeto Genoma, DNA recombinante, células-tronco, clonagem e genética aplicada.",
                60, "https://www.youtube.com/watch?v=vJ8pD7nQ4yM",
                3, 2, 0, 3, 2, 0, 3, 3);
        upsertQuestion(db, "bg1", "bio-genetica", "bio",
                "O cruzamento Aa × Aa gera qual proporção fenotípica em dominância completa?",
                "1:1", "1:2:1", "3:1", "9:3:3:1", "100% dominante", 2,
                "Genótipos 1 AA : 2 Aa : 1 aa resultam em 3 dominantes : 1 recessivo.");
        upsertQuestion(db, "bg2", "bio-genetica", "bio",
                "Um organismo que recebe um gene de outra espécie por engenharia genética é chamado de:",
                "Híbrido natural", "Transgênico (OGM)", "Fóssil vivo", "Decompositor", "Bacteriófago", 1,
                "Organismos transgênicos possuem DNA recombinante contendo sequência exógena funcional.");
        upsertQuestion(db, "bg3", "bio-genetica", "bio",
                "O exame de DNA utilizado na identificação humana e paternidade baseia-se em:",
                "Comparar apenas o tipo sanguíneo ABO",
                "Analisar regiões altamente polimórficas (STRs) herdadas metade da mãe e metade do pai",
                "Contar o número de mitocôndrias",
                "Medir o nível de glicose no sangue",
                "Observar a cor dos olhos", 1,
                "Os marcadores genéticos do filho devem corresponder às bandas da mãe e do pai biológico.");

        upsertTopic(db, "bio-evolucao", "bio", "Evolução Biológica, Bioética e Astrobiologia",
                "Evidências da evolução, Lamarck, Darwin (seleção natural), teoria sintética, especiação, bioética nas ciências da vida e astrobiologia.",
                55, "https://www.youtube.com/watch?v=C5rT8nK2p4M",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "bev1", "bio-evolucao", "bio",
                "Segundo Charles Darwin e a Teoria Sintética da Evolução, bactérias tornam-se resistentes a antibióticos porque:",
                "O antibiótico provoca mutações benéficas direcionadas",
                "Mutações aleatórias pré-existentes conferem resistência e o antibiótico seleciona os indivíduos resistentes",
                "Elas escolhem mudar seu DNA por necessidade",
                "A resistência não é hereditária",
                "Todos os indivíduos são geneticamente idênticos", 1,
                "A variabilidade surge ao acaso (mutação/recombinação) e o ambiente atua como agente seletivo.");
        upsertQuestion(db, "bev2", "bio-evolucao", "bio",
                "Órgãos homólogos (como o braço humano e a nadadeira da baleia) constituem evidência evolutiva de:",
                "Ausência de relação evolutiva", "Ancestralidade comum com irradiação adaptativa",
                "Geração espontânea", "Clonagem artificial", "Extinção imediata", 1,
                "Estruturas homólogas compartilham origem embrionária e ancestral comum, embora possam ter funções distintas.");
        upsertQuestion(db, "bev3", "bio-evolucao", "bio",
                "O campo da Bioética nas ciências da vida dedica-se a:",
                "Proibir todo avanço médico",
                "Refletir sobre limites morais, dignidade humana, consentimento e impactos sociais das biotecnologias",
                "Apenas classificar plantas",
                "Substituir o método experimental",
                "Estudar rochas sedimentares", 1,
                "A bioética orienta pesquisas com seres humanos, edição genética, clonagem e proteção ambiental.");
    }

    // =========================================================================
    // 4. CIÊNCIAS HUMANAS: HISTÓRIA (8), GEOGRAFIA (8), FILOSOFIA (3), SOCIOLOGIA (3)
    // =========================================================================
    static void seedHumanas(SQLiteDatabase db) {
        // --- HISTÓRIA ---
        upsertTopic(db, "his-antiguidade-media", "his", "Introdução à História, Pré-História, Antiguidade e Idade Média",
                "Fontes históricas, memória e patrimônio, povoamento da América e Amazônia, Antiguidade (Egito, Grécia, Roma, África Antiga) e Feudalismo.",
                55, "https://www.youtube.com/watch?v=K4nM8vP2r7T",
                2, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "ham1", "his-antiguidade-media", "his",
                "A democracia na Atenas Clássica diferenciava-se da democracia atual principalmente por ser:",
                "Representativa com voto universal",
                "Direta, porém excludente (mulheres, escravizados e estrangeiros não eram cidadãos)",
                "Baseada em eleições presidenciais",
                "Controlada pelo Senado romano",
                "Governada por faraós", 1,
                "Apenas homens livres, maiores e filhos de pais atenienses participavam diretamente da Assembleia.");
        upsertQuestion(db, "ham2", "his-antiguidade-media", "his",
                "Na Europa Ocidental medieval, o feudalismo caracterizou-se por:",
                "Economia industrial urbana centralizada",
                "Poder político descentralizado, economia agrária de subsistência e relações de suserania/servidão",
                "Abolição da influência da Igreja",
                "Domínio exclusivo das cidades-Estado gregas",
                "Livre comércio global", 1,
                "O feudo era a unidade produtiva básica e a sociedade dividia-se em estamentos.");
        upsertQuestion(db, "ham3", "his-antiguidade-media", "his",
                "Além do Egito, grandes civilizações da África Antiga que contribuíram para ciência e comércio incluem:",
                "Apenas reinos europeus", "Núbia/Cuxe, Axum, Gana e Mali",
                "Incas e Astecas", "Esparta e Atenas", "Mesopotâmia e Pérsia", 1,
                "Reinos africanos como Cuxe, Axum, Gana e Mali destacaram-se em metalurgia, comércio transaariano e escrita.");

        upsertTopic(db, "his-moderna-indigenas", "his", "Renascimento, Reformas, Expansão Marítima e Povos Indígenas",
                "Renascimento, Reformas Religiosas, Estados Nacionais, Expansão Marítima, astecas/maias/incas, Amazônia pré-colonial (Marajó/Santarém) e impactos europeus.",
                55, "https://www.youtube.com/watch?v=L9pR3wN5m2K",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "hmi1", "his-moderna-indigenas", "his",
                "O humanismo renascentista promoveu uma mudança de mentalidade caracterizada pela:",
                "Recusa total da ciência", "Valorização da razão, do ser humano (antropocentrismo) e da cultura clássica",
                "Fortalecimento exclusivo da servidão feudal",
                "Proibição das artes plásticas",
                "Extinção das universidades", 1,
                "O Renascimento combinou racionalismo, observação científica e inspiração greco-romana.");
        upsertQuestion(db, "hmi2", "his-moderna-indigenas", "his",
                "Os sítios arqueológicos de Marajó, Santarém e Alto Xingu demonstram que a Amazônia pré-colonial:",
                "Era um vazio demográfico sem organização social",
                "Abrigava sociedades indígenas populosas e complexas com cerâmica sofisticada e manejo florestal",
                "Foi ocupada apenas após o século XIX",
                "Não possuía agricultura",
                "Era habitada apenas por navegadores europeus", 1,
                "A arqueologia amazônica comprova sociedades densas, terra preta de índio e sofisticada produção cultural.");
        upsertQuestion(db, "hmi3", "his-moderna-indigenas", "his",
                "A Reforma Protestante iniciada por Martinho Lutero (1517) contestou principalmente:",
                "A existência da Bíblia",
                "A venda de indulgências e a autoridade exclusiva do papado na interpretação das escrituras",
                "O uso da imprensa",
                "A expansão do comércio",
                "A tradução da Bíblia para línguas vernáculas", 1,
                "Lutero defendeu a salvação pela fé e o livre exame das escrituras, gerando a reação da Contrarreforma.");

        upsertTopic(db, "his-colonial", "his", "Colonização da América, Escravidão e Amazônia Colonial",
                "Mercantilismo, tráfico atlântico e diáspora africana, conquista da Amazônia, missões, Período Pombalino, Diretório dos Índios e Tratado de Madri.",
                55, "https://www.youtube.com/watch?v=S7mK3vN9r4P",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "hco1", "his-colonial", "his",
                "O Tratado de Madri (1750), baseado no princípio do 'uti possidetis', foi decisivo para a Amazônia porque:",
                "Entregou toda a Amazônia à Inglaterra",
                "Reconheceu a posse portuguesa sobre terras efetivamente ocupadas a oeste de Tordesilhas",
                "Aboliu imediatamente a escravidão",
                "Criou a Zona Franca de Manaus",
                "Encerrou a República Velha", 1,
                "O tratado redefiniu as fronteiras da América Portuguesa legitimando a expansão na bacia amazônica.");
        upsertQuestion(db, "hco2", "his-colonial", "his",
                "As reformas pombalinas na Amazônia (século XVIII) incluíram:",
                "A entrega do governo aos jesuítas",
                "A expulsão dos jesuítas, o Diretório dos Índios (1757) e a criação da Capitania de São José do Rio Negro",
                "A independência do Amazonas",
                "A abolição do pacto colonial",
                "O fim das fronteiras com a América Espanhola", 1,
                "Pombal buscou centralizar a administração régia, secularizar aldeamentos e assegurar o território amazônico.");
        upsertQuestion(db, "hco3", "his-colonial", "his",
                "Entre as formas de resistência de africanos e afrodescendentes à escravidão colonial destacaram-se:",
                "Apenas aceitação passiva do sistema",
                "Formação de quilombos (como Palmares), fugas, irmandades, sincretismo cultural e rebeliões",
                "Criação das capitanias hereditárias",
                "Assinatura do Tratado de Tordesilhas",
                "Exclusiva atuação no parlamento português", 1,
                "Africanos e indígenas foram agentes históricos ativos em múltiplas estratégias de resistência.");

        upsertTopic(db, "his-revolucoes-imperio", "his", "Iluminismo, Revoluções Burguesas, Independências e Império",
                "Absolutismo, Iluminismo, Revoluções Inglesa/Francesa/Industrial, independências nas Américas, 1º Reinado, Regência, 2º Reinado e abolição no Brasil e Amazonas.",
                60, "https://www.youtube.com/watch?v=V3mL8rK6n2P",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "hri1", "his-revolucoes-imperio", "his",
                "A Declaração dos Direitos do Homem e do Cidadão (1789), na Revolução Francesa, inspirou-se no:",
                "Feudalismo medieval", "Iluminismo (liberdade, igualdade jurídica e soberania popular)",
                "Absolutismo de direito divino", "Mercantilismo ibérico", "Fascismo", 1,
                "O ideário iluminista fundamentou rupturas com o Antigo Regime na Europa e nas Américas.");
        upsertQuestion(db, "hri2", "his-revolucoes-imperio", "his",
                "No Período Regencial brasileiro, a Cabanagem (1835–1840) ocorreu no Grão-Pará (incluindo o atual Amazonas) destacando-se por:",
                "Ser liderada exclusivamente pela família real",
                "Ampla participação popular (indígenas, negros, mestiços e pobres) que chegou a tomar o poder provincial",
                "Defender a recolonização por Portugal",
                "Acontecer apenas no Rio Grande do Sul",
                "Ocorrer durante a Ditadura Militar", 1,
                "A Cabanagem foi uma das maiores revoltas populares da história do Brasil, duramente reprimida pelo Império.");
        upsertQuestion(db, "hri3", "his-revolucoes-imperio", "his",
                "A Província do Amazonas destacou-se no movimento abolicionista por declarar a abolição da escravidão em:",
                "1822", "1850", "10 de julho de 1884 (quatro anos antes da Lei Áurea)", "1889", "1930", 2,
                "O Amazonas foi a segunda província brasileira (após o Ceará) a abolir oficialmente a escravidão, em 1884.");

        upsertTopic(db, "his-amazonas", "his", "Belle Époque Amazônica, Imperialismo e Primeira República",
                "Ciclo da borracha, seringais e Belle Époque manauara, Imperialismo na África/Ásia e EUA, Primeira República, revoltas populares e movimento operário.",
                55, "https://www.youtube.com/watch?v=L2pV8rM5k9Q",
                2, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "hamz1", "his-amazonas", "his",
                "O auge econômico da 'Belle Époque' em Manaus (final do séc. XIX e início do XX) foi impulsionado por:",
                "Indústria automobilística local", "Exportação do látex (borracha natural) para a indústria mundial",
                "Extração de petróleo no pré-sal", "Plantação de café em larga escala", "Turismo ecológico", 1,
                "A demanda internacional por borracha gerou riqueza concentrada, modernização urbana e exploração nos seringais.");
        upsertQuestion(db, "hamz2", "his-amazonas", "his",
                "Na Conferência de Berlim (1884–1885), potências europeias estabeleceram regras para:",
                "A independência imediata da África", "A partilha imperialista do continente africano",
                "A criação da ONU", "O fim da Segunda Guerra Mundial", "O Tratado de Tordesilhas", 1,
                "A partilha ignorou fronteiras étnicas africanas, gerando exploração colonial e conflitos duradouros.");
        upsertQuestion(db, "hamz3", "his-amazonas", "his",
                "Na Primeira República brasileira (1889–1930), a 'política do café com leite' expressava o predomínio das oligarquias de:",
                "Amazonas e Pará", "São Paulo e Minas Gerais", "Bahia e Pernambuco",
                "Rio Grande do Sul e Paraná", "Goiás e Mato Grosso", 1,
                "O pacto oligárquico alternava indicações presidenciais sustentadas pelo coronelismo e voto de cabresto.");

        upsertTopic(db, "his-guerras-fria", "his", "Guerras Mundiais, Entreguerras, Guerra Fria e Descolonização",
                "1ª e 2ª Guerras Mundiais, totalitarismos, Guerra Fria, Revoluções Socialistas, descolonização da África/Ásia e conflitos no Oriente Médio.",
                55, "https://www.youtube.com/watch?v=P8rN4kL2v6M",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "hgf1", "his-guerras-fria", "his",
                "O período da Guerra Fria (1947–1991) foi marcado pela bipolaridade geopolítica entre:",
                "Inglaterra e França", "Estados Unidos (capitalismo) e União Soviética (socialismo)",
                "Alemanha e Itália", "Brasil e Argentina", "China e Japão", 1,
                "A disputa ideológica, tecnológica e militar dividiu o mundo em blocos sem confronto direto total entre EUA e URSS.");
        upsertQuestion(db, "hgf2", "his-guerras-fria", "his",
                "Durante a Segunda Guerra Mundial, os 'Soldados da Borracha' foram mobilizados na Amazônia para:",
                "Combater na Europa Oriental",
                "Extrair látex destinado ao esforço de guerra dos Aliados após os Acordos de Washington",
                "Construir Brasília",
                "Proclamar a República",
                "Explorar ouro em Minas Gerais", 1,
                "Milhares de migrantes nordestinos trabalharam nos seringais amazônicos em condições extremamente duras.");
        upsertQuestion(db, "hgf3", "his-guerras-fria", "his",
                "A Conferência de Bandung (1955) relacionou-se à descolonização afro-asiática ao defender:",
                "A continuidade do domínio europeu",
                "O não alinhamento aos blocos da Guerra Fria e a autodeterminação dos povos",
                "A criação do Pacto de Varsóvia",
                "A partilha da América Latina",
                "O retorno do mercantilismo", 1,
                "Países recém-independentes buscaram autonomia política diante de EUA e URSS.");

        upsertTopic(db, "his-era-vargas", "his", "Era Vargas e Pós-Guerra desenvolvimentista",
                "Revolução de 1930, Governo Constitucional, Estado Novo, CLT, trabalhismo e desenvolvimentismo no pós-guerra (JK).",
                50, "https://www.youtube.com/watch?v=kP8nR4xM2qW",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "hv1", "his-era-vargas", "his",
                "O Estado Novo (1937–1945) caracterizou-se por:",
                "Ampla liberdade partidária", "Centralização autoritária do poder, censura (DIP) e trabalhismo",
                "Parlamentarismo", "Domínio das oligarquias regionais", "Eleições diretas regulares", 1,
                "Vargas fechou o Congresso e governou de forma autoritária aliada a políticas de industrialização.");
        upsertQuestion(db, "hv2", "his-era-vargas", "his",
                "A Consolidação das Leis do Trabalho (CLT) foi promulgada em:",
                "1930", "1934", "1943", "1954", "1964", 2,
                "A CLT reuniu e sistematizou direitos trabalhistas em 1943, durante o Estado Novo.");
        upsertQuestion(db, "hv3", "his-era-vargas", "his",
                "O governo Juscelino Kubitschek (1956–1961) ficou marcado pelo Plano de Metas ('50 anos em 5') e por:",
                "Fechamento da economia industrial",
                "Construção de Brasília e estímulo à indústria automobilística e rodoviária",
                "Criação da CLT",
                "Abolição da escravidão",
                "Promulgação da Constituição de 1988", 1,
                "O desenvolvimentismo de JK acelerou a industrialização com capital nacional, estatal e estrangeiro.");

        upsertTopic(db, "his-ditadura", "his", "Ditadura Militar, Redemocratização, ZFM e Contemporaneidade",
                "Ditaduras na América Latina e Brasil (1964–1985), Zona Franca de Manaus (1967), Diretas Já, Constituição de 1988, neoliberalismo e identidades na Amazônia.",
                55, "https://www.youtube.com/watch?v=T6mK2wP9r1D",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "hdi1", "his-ditadura", "his",
                "O Ato Institucional nº 5 (AI-5), decretado em dezembro de 1968, marcou:",
                "A abertura política imediata",
                "O endurecimento do regime militar, fechamento do Congresso e suspensão do habeas corpus",
                "A promulgação da Constituição Cidadã",
                "A criação do Mercosul",
                "O fim da censura", 1,
                "O AI-5 inaugurou o período mais repressivo da ditadura militar brasileira.");
        upsertQuestion(db, "hdi2", "his-ditadura", "his",
                "A reformulação da Zona Franca de Manaus pelo Decreto-Lei nº 288/1967 visou:",
                "Despovoar a região Norte",
                "Promover integração nacional e desenvolvimento econômico na Amazônia Ocidental via incentivos fiscais",
                "Substituir o comércio fluvial por ferrovias europeias",
                "Proibir indústrias no Amazonas",
                "Transferir a capital federal para Manaus", 1,
                "A ZFM transformou profundamente a demografia, a economia e o espaço urbano de Manaus.");
        upsertQuestion(db, "hdi3", "his-ditadura", "his",
                "A Constituição Federal de 1988 ficou conhecida como 'Constituição Cidadã' porque:",
                "Restaurou o voto censitário",
                "Restabeleceu direitos civis, políticos, sociais e reconheceu direitos originários dos povos indígenas",
                "Eliminou o voto direto para presidente",
                "Proibiu sindicatos",
                "Retirou o voto dos analfabetos", 1,
                "A Carta de 1988 consolidou a redemocratização brasileira e ampliou garantias fundamentais.");

        // --- GEOGRAFIA ---
        upsertTopic(db, "geo-cartografia-espaco", "geo", "Espaço geográfico, Cartografia, fusos horários e geotecnologias",
                "Categorias geográficas (paisagem, lugar, território, região), projeções, escalas, mapas temáticos, fusos horários, GPS, SIG e sensoriamento remoto.",
                55, "https://www.youtube.com/watch?v=R3kM8pL4v2N",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "gce1", "geo-cartografia-espaco", "geo",
                "Em um mapa de escala 1:100.000, uma distância gráfica de 5 cm corresponde na realidade a:",
                "500 m", "5 km", "50 km", "500 km", "50 m", 1,
                "5 cm × 100.000 = 500.000 cm = 5.000 m = 5 km.");
        upsertQuestion(db, "gce2", "geo-cartografia-espaco", "geo",
                "Em relação ao horário de Brasília (UTC-3), a cidade de Manaus (UTC-4) encontra-se:",
                "1 hora adiantada", "1 hora atrasada", "2 horas adiantada", "No mesmo fuso", "3 horas atrasada", 1,
                "Manaus está a oeste de Brasília, no fuso UTC-4 (1 hora a menos que Brasília).");
        upsertQuestion(db, "gce3", "geo-cartografia-espaco", "geo",
                "O monitoramento do desmatamento na Amazônia por imagens de satélite utiliza principalmente:",
                "Bússola magnética apenas", "Sensoriamento remoto e Sistemas de Informação Geográfica (SIG)",
                "Projeção de Mercator manual", "Astrolábio", "Sismógrafos oceânicos", 1,
                "Satélites e SIG permitem mapear uso do solo, focos de queimadas e cobertura vegetal em tempo real.");

        upsertTopic(db, "geo-relevo-hidro", "geo", "Geologia, Relevo, Solos, Extrativismo e Hidrografia",
                "Estrutura da Terra, placas tectônicas, relevo brasileiro/amazônico, solos, Urucu/AM, bacias hidrográficas mundiais, brasileiras e amazônica, hidrovias.",
                60, "https://www.youtube.com/watch?v=C7pN2kM9v4R",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "grh1", "geo-relevo-hidro", "geo",
                "Na formação do relevo, vulcanismo e tectonismo são classificados como agentes:",
                "Exógenos (externos)", "Endógenos (internos)", "Climáticos apenas",
                "Antrópicos", "Eólicos", 1,
                "Agentes endógenos atuam a partir do interior da Terra; intemperismo e erosão são exógenos.");
        upsertQuestion(db, "grh2", "geo-relevo-hidro", "geo",
                "A província petrolífera de Urucu, localizada no município de Coari (AM), destaca-se na produção nacional de:",
                "Carvão mineral", "Petróleo e gás natural em terra (onshore)",
                "Energia nuclear", "Sal marinho", "Minério de ferro", 1,
                "Urucu é a maior reserva terrestre de gás natural e petróleo leve do Brasil, ligada a Manaus por gasoduto.");
        upsertQuestion(db, "grh3", "geo-relevo-hidro", "geo",
                "No Amazonas, a Bacia Amazônica é vital para a circulação regional principalmente por viabilizar:",
                "Exclusivamente transporte ferroviário",
                "O modal hidroviário/fluvial ligando municípios e comunidades ribeirinhas",
                "Apenas navegação marítima polar",
                "Substituição total do regime de chuvas",
                "Drenagem endorreica fechada", 1,
                "Rios, furos, paranás e igarapés constituem as principais vias de transporte de pessoas e cargas no Estado.");

        upsertTopic(db, "geo-clima", "geo", "Clima, Formações Vegetais e Mudanças Climáticas",
                "Atmosfera, elementos e fatores climáticos, climas mundiais e brasileiros, biomas, conferências do clima e papel da Amazônia na regulação climática.",
                55, "https://www.youtube.com/watch?v=F6kM9wR2n8P",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "gcl1", "geo-clima", "geo",
                "O clima predominante na maior parte da região amazônica brasileira é o:",
                "Subtropical úmido", "Equatorial (quente e úmido com baixa amplitude térmica anual)",
                "Semiárido", "Mediterrâneo", "Temperado oceânico", 1,
                "A baixa latitude e a intensa evapotranspiração da floresta mantêm altas temperaturas e chuvas abundantes.");
        upsertQuestion(db, "gcl2", "geo-clima", "geo",
                "Os chamados 'rios voadores' associados à Floresta Amazônica correspondem a:",
                "Canais artificiais de irrigação",
                "Massas de ar úmidas alimentadas pela evapotranspiração florestal que levam chuvas ao Centro-Oeste e Sudeste",
                "Correntes marítimas frias",
                "Tempestades de areia",
                "Águas subterrâneas congeladas", 1,
                "A floresta recicla bilhões de toneladas de vapor d'água essenciais ao regime pluviométrico brasileiro.");
        upsertQuestion(db, "gcl3", "geo-clima", "geo",
                "O principal fator climático responsável pela diminuição da temperatura à medida que subimos uma serra é a:",
                "Longitude", "Altitude", "Maritimidade", "Rotação lunar", "Salinidade", 1,
                "Quanto maior a altitude, menor a densidade do ar e menor a retenção de calor na troposfera.");

        upsertTopic(db, "geo-ambiental", "geo", "Políticas Ambientais, Energia, Transportes e Globalização Digital",
                "Acordos da ONU, unidades de conservação, modais de transporte, fontes de energia limpas, comunicação digital, exclusão digital e lixo eletrônico.",
                55, "https://www.youtube.com/watch?v=M3pN7kL9v1R",
                3, 3, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "gam1", "geo-ambiental", "geo",
                "Na matriz elétrica brasileira, a fonte historicamente predominante é a:",
                "Nuclear", "Hidrelétrica (com expansão recente de solar e eólica)",
                "Carvão mineral", "Geotérmica", "Maremoto", 1,
                "O Brasil possui matriz elétrica majoritariamente renovável, liderada por hidrelétricas e crescente participação solar/eólica.");
        upsertQuestion(db, "gam2", "geo-ambiental", "geo",
                "O conceito de desenvolvimento sustentável busca conciliar:",
                "Lucro máximo imediato e desmatamento",
                "Crescimento econômico, justiça social e conservação ambiental para gerações presentes e futuras",
                "Estagnação produtiva total",
                "Consumo ilimitado de combustíveis fósseis",
                "Substituição de florestas por áreas impermeáveis", 1,
                "Sustentabilidade integra dimensões econômica, social e ecológica.");
        upsertQuestion(db, "gam3", "geo-ambiental", "geo",
                "O fenômeno da exclusão digital no espaço geográfico revela:",
                "Acesso igualitário em todas as áreas rurais e urbanas",
                "Desigualdades socioespaciais no acesso à internet, infraestrutura e cidadania digital",
                "O fim das redes sociais",
                "Redução total do lixo eletrônico",
                "Ausência de impactos ambientais em data centers", 1,
                "A conectividade desigual aprofunda disparidades educacionais e de renda entre regiões e grupos sociais.");

        upsertTopic(db, "geo-amazonia", "geo", "Formação Territorial, Regionalização, Fronteiras e Demografia",
                "Formação do território brasileiro/amazônico, Calha Norte/SIVAM/SISFRON, Amazônia Azul, regionalizações, teorias demográficas, pirâmide etária e migrações.",
                60, "https://www.youtube.com/watch?v=C4pL7mR9v2K",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "gama1", "geo-amazonia", "geo",
                "O conceito de 'Amazônia Azul' designa:",
                "A floresta inundada de igapó",
                "As águas jurisdicionais brasileiras no Atlântico (Mar Territorial, ZEE e Plataforma Continental)",
                "A bacia do Rio Negro exclusivamente",
                "O Projeto Calha Norte terrestre",
                "O Aquífero Alter do Chão", 1,
                "A Amazônia Azul destaca a riqueza estratégica, biológica e mineral (como o Pré-sal) do espaço marítimo brasileiro.");
        upsertQuestion(db, "gama2", "geo-amazonia", "geo",
                "A transição demográfica observada no Brasil nas últimas décadas caracteriza-se por:",
                "Aumento explosivo da natalidade",
                "Queda da taxa de fecundidade, maior expectativa de vida e envelhecimento populacional",
                "Estreitamento do topo da pirâmide etária",
                "Redução da população urbana",
                "Aumento contínuo da mortalidade infantil", 1,
                "Com menos nascimentos e maior longevidade, a base da pirâmide etária estreita-se e o topo alarga-se.");
        upsertQuestion(db, "gama3", "geo-amazonia", "geo",
                "Na proposta dos três complexos geoeconômicos brasileiros (Pedro Geiger), o país divide-se em:",
                "Norte, Sul e Leste", "Amazônia, Nordeste e Centro-Sul",
                "Cinco macrorregiões estaduais do IBGE", "Litoral, Sertão e Pampa", "BRICS e Mercosul", 1,
                "Os complexos regionais consideram critérios históricos e econômicos sem seguir rigidamente limites estaduais.");

        upsertTopic(db, "geo-industria-agro", "geo", "Industrialização, Zona Franca de Manaus e Espaço Agrário",
                "Revoluções industriais à Indústria 4.0, industrialização brasileira, ZFM, Revolução Verde, agricultura familiar, agronegócio, estrutura fundiária e reforma agrária.",
                55, "https://www.youtube.com/watch?v=N5kR8vL2m4T",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "gia1", "geo-industria-agro", "geo",
                "A principal característica do espaço agrário brasileiro ligada a conflitos no campo é a:",
                "Distribuição totalmente igualitária de terras",
                "Alta concentração fundiária histórica (latifúndios) em contraste com a agricultura familiar",
                "Ausência de exportação de commodities",
                "Inexistência de mecanização agrícola",
                "Proibição da agricultura na Amazônia", 1,
                "Embora o agronegócio lidere exportações, a agricultura familiar responde por grande parte dos alimentos do mercado interno.");
        upsertQuestion(db, "gia2", "geo-industria-agro", "geo",
                "A Zona Franca de Manaus (Polo Industrial de Manaus) baseia-se em:",
                "Apenas mineração de ferro",
                "Incentivos fiscais para atrair indústrias (eletroeletrônicos, duas rodas, química) e dinamizar a economia regional",
                "Eliminação do setor de serviços",
                "Exclusiva produção de trigo",
                "Substituição das hidrovias por metrô", 1,
                "A ZFM gera empregos e arrecadação contribuindo para conter pressões diretas de desmatamento no interior.");
        upsertQuestion(db, "gia3", "geo-industria-agro", "geo",
                "A chamada Indústria 4.0 associa-se principalmente a:",
                "Máquinas a vapor do século XVIII",
                "Automação avançada, inteligência artificial, internet das coisas (IoT) e big data",
                "Manufatura puramente artesanal",
                "Abandono da informática",
                "Trabalho exclusivo no campo", 1,
                "A 4ª Revolução Industrial integra sistemas ciberfísicos, robótica e dados em tempo real.");

        upsertTopic(db, "geo-urbanizacao", "geo", "Urbanização, Hierarquia Urbana e Desigualdades Sociais",
                "Hierarquia e redes urbanas, metrópoles, Região Metropolitana de Manaus, segregação socioespacial, Plano Diretor, Estatuto da Cidade e IDH/IDHM.",
                55, "https://www.youtube.com/watch?v=K8rT5mN3p2L",
                3, 0, 3, 0, 0, 3, 0, 3);
        upsertQuestion(db, "gur1", "geo-urbanizacao", "geo",
                "O rápido processo de urbanização brasileira na segunda metade do século XX foi impulsionado pelo:",
                "Êxodo urbano", "Êxodo rural associado à industrialização e mecanização do campo",
                "Crescimento exclusivo do setor primário", "Fechamento das rodovias", "Decréscimo populacional", 1,
                "A migração campo-cidade concentrou a população em áreas urbanas, muitas vezes sem infraestrutura adequada.");
        upsertQuestion(db, "gur2", "geo-urbanizacao", "geo",
                "O instrumento básico da política de desenvolvimento e ordenamento urbano municipal previsto no Estatuto da Cidade é o:",
                "Tratado de Madri", "Plano Diretor", "Protocolo de Kyoto", "Acordo de Bretton Woods", "Pacto Colonial", 1,
                "O Plano Diretor orienta o uso do solo, mobilidade, moradia e função social da propriedade urbana.");
        upsertQuestion(db, "gur3", "geo-urbanizacao", "geo",
                "O Índice de Desenvolvimento Humano (IDH) combina indicadores de:",
                "Apenas PIB industrial", "Renda per capita, escolaridade (educação) e expectativa de vida (saúde)",
                "Somente área territorial", "Número de portos e aeroportos", "Clima e altitude", 1,
                "O IDH avalia desenvolvimento para além da renda, incorporando longevidade e educação.");

        upsertTopic(db, "geo-geopolitica", "geo", "Geopolítica Mundial, Capitalismo, Blocos e Mundo do Trabalho",
                "Capitalismo e reordenamento do espaço mundial, Nova Ordem, blocos econômicos, organismos internacionais, guerras civis e precarização do trabalho.",
                55, "https://www.youtube.com/watch?v=V9mK4pL7r2T",
                3, 0, 0, 3, 0, 0, 3, 3);
        upsertQuestion(db, "ggp1", "geo-geopolitica", "geo",
                "Com o fim da Guerra Fria (1991), a Nova Ordem Mundial passou a apresentar caráter:",
                "Estritamente bipolar militar",
                "Multipolar na economia (EUA, União Europeia, China, emergentes/BRICS) sob intensa globalização financeira",
                "Feudal e fragmentado sem comércio",
                "Sem atuação de blocos econômicos",
                "Controlado apenas pela Liga das Nações", 1,
                "O mundo pós-Guerra Fria ampliou polos econômicos, redes transnacionais e disputas comerciais.");
        upsertQuestion(db, "ggp2", "geo-geopolitica", "geo",
                "Entre os blocos econômicos, o estágio mais avançado de integração (com livre circulação e moeda única em grande parte dos membros) é observado na:",
                "APEC", "União Europeia", "OPEP", "OTAN", "OCDE", 1,
                "A União Europeia alcançou o estágio de união econômica e monetária (Zona do Euro).");
        upsertQuestion(db, "ggp3", "geo-geopolitica", "geo",
                "No mundo do trabalho contemporâneo, a 'uberização' refere-se a:",
                "Ampliação de empregos vitalícios com direitos plenos",
                "Trabalho mediado por plataformas digitais, marcado por flexibilização e menor proteção trabalhista",
                "Retorno às corporações de ofício medievais",
                "Eliminação completa do setor de serviços",
                "Substituição da internet por telégrafo", 1,
                "A plataformização transforma vínculos laborais, ampliando informalidade e debates sobre regulação.");

        // --- FILOSOFIA (SIS 1/2/3, Macro, ENEM — 0 no PSC) ---
        upsertTopic(db, "fil-etica-politica", "fil", "Origens da Filosofia, Lógica, Ética, Política e Estética",
                "Passagem do mito ao logos, pensamento racional e lógico, ética, política, estética, matéria e pensamento e reflexões filosóficas atuais.",
                50, "https://www.youtube.com/watch?v=D4mK8vR2p9L",
                3, 0, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "fep1", "fil-etica-politica", "fil",
                "Na Grécia Antiga, a passagem do pensamento mítico para o filosófico (logos) caracterizou-se pela:",
                "Aceitação dogmática de narrativas sobrenaturais",
                "Busca por explicações racionais, argumentadas e coerentes sobre o cosmos e a pólis",
                "Eliminação do debate político",
                "Proibição da lógica",
                "Subordinação à astrologia", 1,
                "A filosofia inaugurou a investigação crítica baseada na razão e no argumento.");
        upsertQuestion(db, "fep2", "fil-etica-politica", "fil",
                "Para Aristóteles, a ética tem como finalidade a 'eudaimonia' (felicidade/florescimento), alcançada por meio da:",
                "Obediência cega aos impulsos", "Prática das virtudes guiadas pela justa medida (equilíbrio racional)",
                "Acumulação ilimitada de riquezas", "Isolamento total da cidade", "Renúncia ao conhecimento", 1,
                "Na ética aristotélica, a virtude é um hábito racional situado no meio-termo entre o excesso e a falta.");
        upsertQuestion(db, "fep3", "fil-etica-politica", "fil",
                "Na reflexão filosófica, distingue-se moral de ética porque a ética:",
                "É apenas um conjunto de costumes locais imutáveis",
                "Consiste na reflexão crítica e filosófica sobre os valores e normas morais",
                "Não tem relação com ações humanas",
                "Aplica-se somente à arte",
                "Confunde-se com opinião aleatória", 1,
                "Enquanto a moral reúne costumes e regras vividas, a ética analisa criticamente seus fundamentos.");

        upsertTopic(db, "fil-medieval-moderna", "fil", "Filosofia Medieval, Moderna, Criticismo, Idealismo e Materialismo",
                "Fé e razão (Agostinho e Tomás de Aquino), racionalismo e empirismo, Kant (criticismo), Hegel (idealismo), Marx (materialismo) e métodos filosóficos.",
                50, "https://www.youtube.com/watch?v=M7rL3pK9v2N",
                3, 0, 0, 0, 0, 3, 0, 3);
        upsertQuestion(db, "fmm1", "fil-medieval-moderna", "fil",
                "Na Filosofia Moderna, a frase 'Penso, logo existo' (Cogito, ergo sum), de René Descartes, expressa:",
                "O empirismo radical baseado apenas nos sentidos",
                "O racionalismo e a dúvida metódica como busca por uma certeza indubitável",
                "O existencialismo do século XX",
                "O ceticismo absoluto",
                "A escolástica medieval", 1,
                "Descartes duvida dos sentidos e encontra no próprio ato de pensar o fundamento seguro do conhecimento.");
        upsertQuestion(db, "fmm2", "fil-medieval-moderna", "fil",
                "Para Immanuel Kant, o 'imperativo categórico' estabelece que devemos agir:",
                "Apenas buscando vantagens pessoais",
                "Segundo uma máxima que possamos desejar que se torne lei universal, tratando a humanidade como fim em si mesma",
                "Conforme a força do mais forte",
                "Baseados exclusivamente no medo de punição",
                "Sem considerar a razão", 1,
                "A ética do dever kantiana fundamenta-se na autonomia racional e na universalidade da lei moral.");
        upsertQuestion(db, "fmm3", "fil-medieval-moderna", "fil",
                "Diferentemente do idealismo de Hegel, o materialismo histórico de Karl Marx sustenta que:",
                "As ideias puras determinam sozinhas a história",
                "As condições materiais de produção e as relações sociais de trabalho estruturam a história e a consciência",
                "A economia não influencia a política",
                "As classes sociais nunca entram em conflito",
                "A história é guiada pelo destino mítico", 1,
                "Para Marx, o modo de produção material da vida condiciona os processos sociais, políticos e ideológicos.");

        upsertTopic(db, "fil-contemporanea", "fil", "Nietzsche, Fenomenologia, Existencialismo e Linguagem",
                "Crítica dos valores em Nietzsche, fenomenologia, estruturalismo, linguagem em Heidegger, existencialismo (Sartre) e pós-estruturalismo.",
                50, "https://www.youtube.com/watch?v=Q2nV9kL5r8P",
                2, 0, 0, 0, 0, 0, 3, 3);
        upsertQuestion(db, "fco1", "fil-contemporanea", "fil",
                "No existencialismo de Jean-Paul Sartre, a afirmação 'a existência precede a essência' significa que:",
                "O ser humano nasce com um destino fixo e imutável",
                "O ser humano primeiro existe e constrói quem é por meio de suas escolhas livres e responsabilidade",
                "Não há liberdade humana",
                "A natureza determina totalmente os valores",
                "A linguagem não tem papel na consciência", 1,
                "Para Sartre, estamos 'condenados a ser livres', sendo responsáveis pelo projeto que construímos.");
        upsertQuestion(db, "fco2", "fil-contemporanea", "fil",
                "Friedrich Nietzsche criticou a moral tradicional ocidental propondo:",
                "A submissão passiva a dogmas",
                "A genealogia e transvaloração dos valores em afirmação criativa da vida",
                "O retorno ao racionalismo cartesiano",
                "A defesa do positivismo científico",
                "A restauração da escolástica", 1,
                "Nietzsche questionou valores absolutos e propôs a criação ativa de sentidos afirmativos da existência.");
        upsertQuestion(db, "fco3", "fil-contemporanea", "fil",
                "Na reflexão de Martin Heidegger, a linguagem é compreendida como 'a casa do ser' porque:",
                "Serve apenas para etiquetar objetos físicos",
                "É por meio da linguagem que o mundo se desvela e ganha sentido para o ser humano",
                "Impede o pensamento filosófico",
                "É um recurso meramente biológico idêntico ao dos minerais",
                "Não se relaciona com a existência", 1,
                "Heidegger vê a linguagem não como simples ferramenta técnica, mas como abertura fundamental de sentido.");

        // --- SOCIOLOGIA (SIS 1/2/3, Macro, ENEM — 0 no PSC) ---
        upsertTopic(db, "soc-classicos-cultura", "soc", "Clássicos da Sociologia, Cultura, Racismo Estrutural e Cidadania",
                "Durkheim, Weber e Marx, sociólogos brasileiros, racismo estrutural, cultura material/imaterial na Amazônia, poder, cidadania e movimentos sociais.",
                50, "https://www.youtube.com/watch?v=R8kP3mN5v2L",
                3, 0, 0, 0, 3, 0, 0, 3);
        upsertQuestion(db, "scc1", "soc-classicos-cultura", "soc",
                "Para Émile Durkheim, o objeto de estudo da Sociologia é o 'fato social', caracterizado por:",
                "Ser puramente individual e biológico",
                "Coercitividade, exterioridade e generalidade sobre os indivíduos",
                "Depender apenas da vontade isolada de uma pessoa",
                "Existir apenas em sociedades digitais",
                "Ser impossível de observar", 1,
                "Fatos sociais (leis, língua, costumes) existem fora do indivíduo e exercem força coercitiva coletiva.");
        upsertQuestion(db, "scc2", "soc-classicos-cultura", "soc",
                "O conceito de racismo estrutural compreende o racismo como:",
                "Apenas ofensas isoladas entre indivíduos",
                "Um processo histórico enraizado nas instituições políticas, econômicas e sociais que reproduz desigualdades",
                "Um fenômeno biológico",
                "Algo inexistente após a abolição",
                "Restrito a países europeus", 1,
                "O racismo estrutural manifesta-se na distribuição desigual de renda, representação política e acesso a direitos.");
        upsertQuestion(db, "scc3", "soc-classicos-cultura", "soc",
                "Para Max Weber, os três tipos puros de dominação legítima são:",
                "Feudal, industrial e digital",
                "Tradicional, carismática e racional-legal",
                "Biológica, geográfica e climática",
                "Agrária, urbana e comercial",
                "Oral, escrita e visual", 1,
                "Weber analisa como a autoridade se legitima pelo costume, pelo carisma do líder ou por leis impessoais.");

        upsertTopic(db, "soc-redes-desigualdade", "soc", "Sociedade em Rede, Estratificação, Multiculturalismo e Direitos Humanos",
                "Sociedade em rede, globalização, estratificação e desigualdade social, multiculturalismo, gênero, ECA/Constituição e direitos indígenas/negros na Amazônia.",
                50, "https://www.youtube.com/watch?v=K3pV8nM4r6T",
                3, 0, 0, 0, 0, 3, 0, 3);
        upsertQuestion(db, "srd1", "soc-redes-desigualdade", "soc",
                "A perspectiva do interculturalismo e do multiculturalismo crítico propõe:",
                "A imposição de uma única cultura dominante (etnocentrismo)",
                "O reconhecimento, diálogo equitativo e valorização da diversidade cultural, étnica e religiosa",
                "O isolamento obrigatório de cada grupo",
                "A extinção de línguas indígenas",
                "A negação dos direitos humanos", 1,
                "Combate o etnocentrismo promovendo convivência democrática e respeito às identidades diversas.");
        upsertQuestion(db, "srd2", "soc-redes-desigualdade", "soc",
                "A 'sociedade em rede' (conceito associado a Manuel Castells) destaca:",
                "O fim das desigualdades no mundo",
                "A reorganização da economia, sociabilidade e mobilização política em torno de redes digitais de informação",
                "O desaparecimento completo do Estado",
                "A substituição da escola por rádios AM",
                "A ausência de conflitos sociais online", 1,
                "As redes digitais aceleram fluxos de comunicação e novos movimentos sociais, mas também geram exclusão digital.");
        upsertQuestion(db, "srd3", "soc-redes-desigualdade", "soc",
                "O Estatuto da Criança e do Adolescente (ECA) estabelece o princípio da:",
                "Responsabilidade exclusiva do mercado",
                "Proteção integral, reconhecendo crianças e adolescentes como sujeitos de direitos",
                "Restrição do acesso à educação básica",
                "Exclusão de políticas públicas",
                "Prioridade apenas penal", 1,
                "O ECA garante dever compartilhado de família, sociedade e Estado na proteção dos direitos infantojuvenis.");

        upsertTopic(db, "soc-trabalho-ambiente", "soc", "Indústria Cultural, Mundo do Trabalho, ZFM e Sociologia Ambiental",
                "Escola de Frankfurt e indústria cultural, sociedade de risco, política ambiental na Amazônia, reestruturação produtiva, flexibilização do trabalho e Polo Industrial de Manaus.",
                50, "https://www.youtube.com/watch?v=T9mL5kR2v7N",
                3, 0, 0, 0, 0, 0, 3, 3);
        upsertQuestion(db, "sta1", "soc-trabalho-ambiente", "soc",
                "Para Adorno e Horkheimer (Escola de Frankfurt), a 'Indústria Cultural' caracteriza-se por:",
                "Estimular sempre a reflexão crítica autônoma",
                "Transformar bens culturais em mercadorias padronizadas voltadas ao consumo de massa e conformismo",
                "Valorizar exclusivamente culturas tradicionais locais",
                "Eliminar o papel da mídia na sociedade",
                "Substituir o capitalismo pelo feudalismo", 1,
                "A indústria cultural aplica lógica fabril à cultura, padronizando gostos e reduzindo o senso crítico.");
        upsertQuestion(db, "sta2", "soc-trabalho-ambiente", "soc",
                "A reestruturação produtiva contemporânea (toyotismo e flexibilização) impactou o mundo do trabalho gerando:",
                "Aumento universal da estabilidade empregatícia",
                "Terceirização, polivalência exigida do trabalhador, automação e crescimento da informalidade",
                "Eliminação da tecnologia nas fábricas",
                "Retorno exclusivo ao trabalho artesanal",
                "Redução da competição global", 1,
                "A produção enxuta e digital aumentou produtividade, mas também trouxe precarização e novos desafios sindicais.");
        upsertQuestion(db, "sta3", "soc-trabalho-ambiente", "soc",
                "Na Sociologia Ambiental, a questão climática e os desastres socioambientais evidenciam o 'racismo ambiental' quando:",
                "Os impactos atingem de forma idêntica todos os bairros e classes",
                "Populações periféricas, indígenas, quilombolas e ribeirinhas sofrem desproporcionalmente com poluição e desastres",
                "A natureza não sofre interferência humana",
                "Políticas públicas beneficiam apenas áreas preservadas",
                "Não há relação entre desigualdade social e meio ambiente", 1,
                "A vulnerabilidade socioambiental distribui-se de forma desigual segundo renda, território e raça.");
    }
}
