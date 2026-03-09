-- Reconciliacao incremental oficial de ministerios/secretarias a partir da planilha BASE DE DADOS - TEDS (2).xlsx
-- Aba: BASE_SECRETARIAS_MINISTERIOS
-- Objetivo: manter somente o seed oficial (populacao automatica) com vinculo correto ministerio -> secretaria.

CREATE TEMP TABLE tmp_ministry_secretary_source (
    ministry_name VARCHAR(255) NOT NULL,
    ministry_sigla VARCHAR(20) NOT NULL,
    secretary_name VARCHAR(255) NOT NULL,
    secretary_sigla VARCHAR(20) NOT NULL
) ON COMMIT DROP;

INSERT INTO tmp_ministry_secretary_source (
    ministry_name,
    ministry_sigla,
    secretary_name,
    secretary_sigla
)
VALUES
        ('Advocacia-Geral da União', 'AGU', 'Secretaria de Atos Normativos', 'SENOR/AGU'),
        ('Advocacia-Geral da União', 'AGU', 'Secretaria de Controle Interno', 'SCI/AGU'),
        ('Advocacia-Geral da União', 'AGU', 'Secretaria-Geral de Consultoria', 'SGCS/AGU'),
        ('Advocacia-Geral da União', 'AGU', 'Secretaria-Geral de Contencioso', 'SGCT/AGU'),
        ('Casa Civil', 'CC', 'Secretaria Especial de Análise Governamental', 'SAG/CC'),
        ('Casa Civil', 'CC', 'Secretaria Especial do Programa de Aceleração ao Crescimento', 'SEPAC/CC'),
        ('Casa Civil', 'CC', 'Secretaria Especial do Programa de Parcerias de Investimentos', 'SEPPI/CC'),
        ('Casa Civil', 'CC', 'Secretaria Especial para Assuntos Jurídicos', 'SAJ/CC'),
        ('Casa Civil', 'CC', 'Secretaria de Articulação e Monitoramento', 'SAM/CC'),
        ('Casa Civil', 'CC', 'Secretaria-Executiva - Casa Civil', 'SE/CC'),
        ('Controladoria-Geral da União', 'CGU', 'Secretaria Federal de Controle Interno', 'SFC/CGU'),
        ('Controladoria-Geral da União', 'CGU', 'Secretaria Nacional de Transparência e Acesso à Informação', 'SNAI/CGU'),
        ('Controladoria-Geral da União', 'CGU', 'Secretaria de Integridade Privada', 'SIPRI/CGU'),
        ('Controladoria-Geral da União', 'CGU', 'Secretaria de Integridade Pública', 'SIP/CGU'),
        ('Controladoria-Geral da União', 'CGU', 'Secretaria-Executiva - Controladoria-Geral da União', 'SE/CGU'),
        ('Gabinete de Segurança Institucional', 'GSI', 'Secretaria de Acompanhamento e Gestão de Assuntos Estratégicos', 'SAGAE/GSI'),
        ('Gabinete de Segurança Institucional', 'GSI', 'Secretaria de Coordenação e Assuntos Aeroespaciais', 'SCAE/GSI'),
        ('Gabinete de Segurança Institucional', 'GSI', 'Secretaria de Segurança Espacial', 'SPR/GSI'),
        ('Gabinete de Segurança Institucional', 'GSI', 'Secretaria de Segurança da Informação e Cibernética', 'SSIC/GSI'),
        ('Gabinete de Segurança Institucional', 'GSI', 'Secretaria-Executiva - Gabinete de Segurança Institucional', 'SE/GSI'),
        ('Ministério da Agricultura e Pecuária', 'MAPA', 'Secretaria de Comércio e Relações internacionais', 'SCRI/MAPA'),
        ('Ministério da Agricultura e Pecuária', 'MAPA', 'Secretaria de Defesa Agropecuária', 'DAS/MAPA'),
        ('Ministério da Agricultura e Pecuária', 'MAPA', 'Secretaria de Desenvolvimento Rural', 'SDR/MAPA'),
        ('Ministério da Agricultura e Pecuária', 'MAPA', 'Secretaria de Política Agrícola', 'SPA/MAPA'),
        ('Ministério da Agricultura e Pecuária', 'MAPA', 'Secretaria-Executiva - Ministério da Agricultura e Pecuária', 'SE/MAPA'),
        ('Ministério das Cidades', 'MCID', 'Secretaria Nacional de Desenvolvimento Urbano e Metropolitano', 'SNDUM/MCID'),
        ('Ministério das Cidades', 'MCID', 'Secretaria Nacional de Habitação', 'SNH/MCID'),
        ('Ministério das Cidades', 'MCID', 'Secretaria Nacional de Mobilidade Urbana', 'SEMOB/MCID'),
        ('Ministério das Cidades', 'MCID', 'Secretaria Nacional de Periferias', 'SNP/MCID'),
        ('Ministério das Cidades', 'MCID', 'Secretaria Nacional de Saneamento Ambiental', 'SNSA/MCID'),
        ('Ministério das Cidades', 'MCID', 'Secretaria-Executiva - Ministério das Cidades', 'SE/MCID'),
        ('Ministério das Comunicações', 'MCOM', 'Secretaria de Radiodifusão', 'SERAD/MCOM'),
        ('Ministério das Comunicações', 'MCOM', 'Secretaria de Telecomunicações', 'SETEL/MCOM'),
        ('Ministério das Comunicações', 'MCOM', 'Secretaria-Executiva - Ministério das Comunicações', 'SE/MCOM'),
        ('Ministério da Ciência, Tecnologia e Inovação', 'MCTI', 'Secretaria de Ciência e Tecnologia para Transformação Digital', 'SETAD/MCTI'),
        ('Ministério da Ciência, Tecnologia e Inovação', 'MCTI', 'Secretaria de Ciência e Tecnologia para o Desenvolvimento Social', 'SEDES/MCTI'),
        ('Ministério da Ciência, Tecnologia e Inovação', 'MCTI', 'Secretaria de Desenvolvimento Tecnológico e Inovação', 'SETEC/MCTI'),
        ('Ministério da Ciência, Tecnologia e Inovação', 'MCTI', 'Secretaria de Políticas e Programas Estratégicos', 'SEPPE/MCTI'),
        ('Ministério da Ciência, Tecnologia e Inovação', 'MCTI', 'Secretaria-Executiva - Ministério da Ciência, Tecnologia e Inovação', 'SE/MCTI'),
        ('Ministério da Defesa', 'MD', 'Secretaria de Controle Interno', 'CISET/MD'),
        ('Ministério da Defesa', 'MD', 'Secretaria-Geral', 'SG/MD'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Companhia Nacional de Abastecimento', 'CONAB/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Instituto Nacional de Colonização e Reforma Agrária', 'INCRA/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Secretaria de Abastecimento, Cooperativismo e Soberania Alimentar', 'SEAB/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Secretaria de Agricultura Familiar e Agroecologia', 'SAF/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Secretaria de Governança Fundiária, Desenvolvimento Territorial e Socioambiental', 'SFDT/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Secretaria de Territórios e Sistemas produtivos Quilombolas e Tradicionais', 'SETEQ/MDA'),
        ('Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'MDA', 'Secretaria-Executiva - Ministério do Desenvolvimento Agrário e Agricultura Familiar', 'SE/MDA'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria Nacional de Promoção e Defesa dos Direitos Humanos', 'SNDH/MDHC'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria Nacional dos Direitos da Criança e do Adolescente', 'SNDCA/MDHC'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria Nacional dos Direitos da Pessoa Idosa', 'SNDPI/MDHC'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria Nacional dos Direitos da Pessoa com Deficiência', 'SNDPD/MDHC'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria Nacional dos Direitos das Pessoas LGBTQIA+', 'SLGBTQIA+/MDHC'),
        ('Ministério dos Direitos Humanos e da Cidadania', 'MDHC', 'Secretaria-Executiva - Ministério dos Direitos Humanos e da Cidadania', 'SE/MDHC'),
        ('Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'MDIC', 'Secretaria de Competitividade e Política Regulatória', 'SCPR/MDIC'),
        ('Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'MDIC', 'Secretaria de Comércio Exterior', 'SECEX/MDIC'),
        ('Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'MDIC', 'Secretaria de Desenvolvimento Industrial, Inovação, Comércio e Serviços', 'SDIC/MDIC'),
        ('Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'MDIC', 'Secretaria de Economia Verde, Descarbonização e Bioindústria', 'SEV/MDIC'),
        ('Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'MDIC', 'Secretaria-Executiva - Ministério do Desenvolvimento, Indústria, Comércio e Serviços', 'SE/MDIC'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Extraordinária de Combate à Pobreza e à Fome', 'SECF/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional da Politica de Cuidados e Família', 'SNCF/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional de Assistência Social', 'SNAS/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional de Beneficios Assistenciais', 'SNBA/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional de Integração e Articulação de Plataformas Sociais Eletrônicas', 'SINAPSE/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional de Renda de Cidadania', 'SENARC/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria Nacional de Segurança Alimentar e Nutricional', 'SESAN/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria de Avaliação, Gestão da Informação e Cadastro Único', 'SAGICAD/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria de Inclusão Socioeconômica', 'SISEC/MDS'),
        ('Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'MDS', 'Secretaria-Executiva - Ministério do Desenvolvimento e Assistência Social, Família e Combate à Fome', 'SE/MDS'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Articulação Intersetorial e com os Sistemas de Ensino', 'SASE/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Educação Básica', 'SEB/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Educação Continuada, Alfabetização de Jovens e Adultos, Diversidade e Inclusão', 'SECADI/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Educação Profissional e Tecnológica', 'SETEC/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Educação Superior', 'SESU/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Gestão da Informação, Inovação e Avaliação de Políticas Educacionais', 'SEGAPE/MEC'),
        ('Ministério da Educação', 'MEC', 'Secretaria de Regulação e Supervisão da Educação Superior', 'SERES/MEC'),
        ('Ministério do Empreendedorismo da Microempresa e da Empresa de Pequeno Porte', 'MEMP', 'Secretaria Nacional de Ambiente de Negócios', 'SANE/MEMP'),
        ('Ministério do Empreendedorismo da Microempresa e da Empresa de Pequeno Porte', 'MEMP', 'Secretaria Nacional de Inclusão Socioprodutiva, Artesanato e Microempreendedor Individual', 'SISAM/MEMP'),
        ('Ministério do Empreendedorismo da Microempresa e da Empresa de Pequeno Porte', 'MEMP', 'Secretaria-Executiva - Ministério do Empreendedorismo da Microempresa e da Empresa de Pequeno Porte', 'SE/MEMP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Extraordinária da Copa do Mundo de Futebol Feminino', 'SECMFF/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Nacional de Apostas Esportivas e de Desenvolvimento Econômico do Esporte', 'SNAEDE/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Nacional de Esporte Amador, Educação, Lazer e Inclusão Social', 'SNEAELIS/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Nacional de Excelência Esportiva', 'SNE/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Nacional de Futebol e Defesa dos Direitos do Torcedor', 'SNFDT/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria Nacional de Paradesporto', 'SNPAR/MESP'),
        ('Ministério do Esporte', 'MESP', 'Secretaria-Executiva - Ministério do Esporte', 'SE/MESP'),
        ('Ministério da Fazenda', 'MF', 'Secretaria Especial da Receita Federal do Brasil', 'RFB/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria Extraordinária do Mercado de Carbono', 'SEMC/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria de Assuntos internacionais', 'SAIN/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria de Política Econômica', 'SPE/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria de Prêmios e Apostas', 'SPA/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria de Reformas Econômicas', 'SER/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria do Tesouro Nacional', 'STN/MF'),
        ('Ministério da Fazenda', 'MF', 'Secretaria-Executiva - Ministério da Fazenda', 'SE/MF'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria Extraordinária para a Transformação do Estado', 'SETE/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Coordenação e Governança das Empresas Estatais', 'SEST/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Gestão de Pessoas', 'SGP/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Gestão e Inovação', 'SEGES/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Governo Digital', 'SGD/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Relações de Trabalho', 'SRT/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria de Serviços Compartilhados', 'SSC/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria do Patrimônio da União', 'SPU/MGI'),
        ('Ministério da Gestão e da Inovação em Serviços Públicos', 'MGI', 'Secretaria-Executiva - Ministério da Gestão e da Inovação em Serviços Públicos', 'SE/MGI'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Secretaria Nacional de Fundos e Instrumentos Financeiros', 'SNFI/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Secretaria Nacional de Políticas de Desenvolvimento Regional e Territorial', 'SDR/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Secretaria Nacional de Proteção e Defesa Civil', 'SEDEC/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Secretaria Nacional de Segurança Hídrica', 'SNSH/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Secretaria-Executiva - Ministério da Integração e do Desenvolvimento Regional', 'SE/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Superintendência do Desenvolvimento do Centro-Oeste', 'SUDECO/MIDR'),
        ('Ministério da Integração e do Desenvolvimento Regional', 'MIDR', 'Superintendência do Desenvolvimento do Nordeste', 'SUDENE/MIDR'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Articulação Federativa e Comitês de Cultura', 'SAFCC/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Cidadania e Diversidade Cultural', 'SCDC/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Direitos Autorais e Intelectuais', 'OSDAI/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Economia Criativa', 'SEC/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Fomento e Incentivo à Cultura', 'SEFIC/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria de Formação Artística e Cultural, Livro e Leitura', 'SEFLI/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria do Audiovisual', 'SAV/MINC'),
        ('Ministério da Cultura', 'MINC', 'Secretaria-Executiva - Ministério da Cultura', 'SE/MINC'),
        ('Ministério da Igualdade Racial', 'MIR', 'Secretaria de Gestão do Sistema Nacional de Promoção da Igualdade Racial', 'SENAPIR/MIR'),
        ('Ministério da Igualdade Racial', 'MIR', 'Secretaria de Políticas de Ações Afirmativas, Combate e Superação do Racismo', 'SEPAR/MIR'),
        ('Ministério da Igualdade Racial', 'MIR', 'Secretaria de Políticas para Quilombolas, Povos e Comunidades Tradicionais de Matriz Africana, Povos de Terreiros e Ciganos', 'SQPT/MIR'),
        ('Ministério da Igualdade Racial', 'MIR', 'Secretaria-Executiva - Ministério da Igualdade Racial', 'SE/MIR'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Acesso à Justiça', 'SAJU/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Assuntos Legislativos', 'SAL/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Direitos Digitais', 'SEDIGI/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Justiça', 'SENAJUS/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Politicas Penais', 'SENAPPEN/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Politicas sobre Drogas e Gestão de Ativos', 'SENAD/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional de Segurança Pública', 'SENASP/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria Nacional do Consumidor', 'SENACON/MJSP'),
        ('Ministério da Justiça e Segurança Pública', 'MJSP', 'Secretaria-Executiva - Ministério da Justiça e Segurança Pública', 'SE/MJSP'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Extraordinária de Controle do Desmatamento e Ordenamento Ambiental Territorial', 'SECD/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Nacional de Biodiversidade. Florestas e Direitos Animais', 'SBIO/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Nacional de Bioeconomia', 'SBC/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Nacional de Meio Ambiente Urbano, Recursos Hidricos e Qualidade Ambiental', 'SQA/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Nacional de Mudança do Clima', 'SMC/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria Nacional de Povos e Comunidades Tradicionais e Desenvolvimento Rural Sustentável', 'SNPCT/MMA'),
        ('Ministério do Meio Ambiente e Mudança do Clima', 'MMA', 'Secretaria-Executiva - Ministério do Meio Ambiente e Mudança do Clima', 'SE/MMA'),
        ('Ministério de Minas e Energia', 'MME', 'Companhia de Pesquisa de Recursos Minerais', 'CPRM/MME'),
        ('Ministério de Minas e Energia', 'MME', 'Secretaria Nacional de Energia Elétrica', 'SNEE/MME'),
        ('Ministério de Minas e Energia', 'MME', 'Secretaria Nacional de Geologia, Mineração e Transformação Mineral', 'SNGM/MME'),
        ('Ministério de Minas e Energia', 'MME', 'Secretaria Nacional de Petróleo, Gás Natural e Biocombustíveis', 'SNPGB/MME'),
        ('Ministério de Minas e Energia', 'MME', 'Secretaria Nacional de Transição Energética e Planejamento', 'SNTEP/MME'),
        ('Ministério de Minas e Energia', 'MME', 'Secretaria-Executiva - Ministério de Minas e Energia', 'SE/MME'),
        ('Ministério das Mulheres', 'MMULHERES', 'Secretaria Nacional de Articulação Institucional, Ações Temáticas e Participação Política', 'SENATP/MMULHERES'),
        ('Ministério das Mulheres', 'MMULHERES', 'Secretaria Nacional de Autonomia Econômica', 'SENAEC/MMULHERES'),
        ('Ministério das Mulheres', 'MMULHERES', 'Secretaria Nacional de Enfrentamento à Violência contra Mulheres', 'SENEV/MMULHERES'),
        ('Ministério das Mulheres', 'MMULHERES', 'Secretaria-Executiva - Ministério das Mulheres', 'SE/MMULHERES'),
        ('Ministério da Pesca e Aquicultura', 'MPA', 'Secretaria Nacional de Aquicultura', 'SNA/MPA'),
        ('Ministério da Pesca e Aquicultura', 'MPA', 'Secretaria Nacional de Pesca Artesanal', 'SNPA/MPA'),
        ('Ministério da Pesca e Aquicultura', 'MPA', 'Secretaria Nacional de Pesca Industrial, Amadora e Esportiva', 'SNPI/MPA'),
        ('Ministério da Pesca e Aquicultura', 'MPA', 'Secretaria Nacional de Registro, Monitoramento e Pesquisa da Pesca e Aquicultura', 'SERMOP/MPA'),
        ('Ministério da Pesca e Aquicultura', 'MPA', 'Secretaria-Executiva - Ministério da Pesca e Aquicultura', 'SE/MPA'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Departamento de Mediação e Conciliação de Conflitos Fundiários Indígenas', 'DEMED/MPI'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Fundação Nacional dos Povos Indígenas', 'FUNAI/MPI'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Secretaria Nacional de Articulação e Promoção de Direitos Indigenas', 'SEART/MPI'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Secretaria Nacional de Direitos Territoriais Indigenas', 'SEDAT/MPI'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Secretaria Nacional de Gestão Ambiental e Territorial Indigena', 'SEGAT/MPI'),
        ('Ministério dos Povos Indígenas', 'MPI', 'Secretaria-Executiva - Ministério dos Povos Indígenas', 'SE/MPI'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria Nacional de Planejamento', 'SEPLAN/MPO'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria de Articulação Institucional', 'SEAI/MPO'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria de Assuntos Internacionais e Desenvolvimento', 'SEAID/MPO'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria de Monitoramento e Avaliação de Políticas Públicas e Assuntos Econômicos', 'SMA/MPO'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria de Orçamento Federal', 'SOF/MPO'),
        ('Ministério do Planejamento e Orçamento', 'MPO', 'Secretaria-Executiva - Ministério do Planejamento e Orçamento', 'SE/MPO'),
        ('Ministério de Portos e Aeroportos', 'MPOR', 'Secretaria Nacional de Aviação Civil', 'SAC/MPOR'),
        ('Ministério de Portos e Aeroportos', 'MPOR', 'Secretaria Nacional de Hidrovias e Navegação', 'SENHN/MPOR'),
        ('Ministério de Portos e Aeroportos', 'MPOR', 'Secretaria Nacional de Portos', 'SNP/MPOR'),
        ('Ministério de Portos e Aeroportos', 'MPOR', 'Secretaria-Executiva - Ministério de Portos e Aeroportos', 'SE/MPOR'),
        ('Ministério da Previdência Social', 'MPS', 'Secretaria de Regime Geral de Previdência Social', 'SRGPS/MPS'),
        ('Ministério da Previdência Social', 'MPS', 'Secretaria de Regime Próprio e Complementar', 'SRPC/MPS'),
        ('Ministério da Previdência Social', 'MPS', 'Secretaria-Executiva - Ministério da Previdência Social', 'SE/MPS'),
        ('Ministério das Relações Exteriores', 'MRE', 'Secretaria de Controle Interno', 'CISET/MRE'),
        ('Ministério das Relações Exteriores', 'MRE', 'Secretaria-Geral - SG', 'SG/MRE'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Atenção Especializada à Saúde', 'SAES/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Atenção Primária à Saúde', 'SAPS/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Ciência, Tecnologia e Inovação em Saúde', 'SCTIE/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Gestão do Trabalho e da Educação na Saúde', 'SGTES/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Informação e Saúde Digital', 'SEIDIGI/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Saúde Indígena', 'SESAI/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria de Vigilância em Saúde e Ambiente', 'SVSA/MS'),
        ('Ministério da Saúde', 'MS', 'Secretaria-Executiva - Ministério da Saúde', 'SE/MS'),
        ('Ministério dos Transportes', 'MT', 'Secretaria Nacional de Transporte Ferroviário', 'SNTF/MT'),
        ('Ministério dos Transportes', 'MT', 'Secretaria Nacional de Transporte Rodoviário', 'SNTR/MT'),
        ('Ministério dos Transportes', 'MT', 'Secretaria Nacional de Trânsito', 'SENATRAN/MT'),
        ('Ministério dos Transportes', 'MT', 'Secretaria-Executiva - Ministério dos Transportes', 'SE/MT'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria Nacional de Economia Popular e Solidária', 'SENAES/MTE'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria Nacional de Qualificação, Emprego e Juventude', 'SEQ/MTE'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria de Inspeção do Trabalho', 'SIT/MTE'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria de Proteção ao Trabalhador', 'SPT/MTE'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria de Relações do Trabalho', 'SRT/MTE'),
        ('Ministério do Trabalho e Emprego', 'MTE', 'Secretaria-Executiva - Ministério do Trabalho e Emprego', 'SE/MTE'),
        ('Ministério do Turismo', 'MTUR', 'Secretaria Nacional de Infraestrutura, Crédito e Investimento no Turismo', 'SNINFRA/MTUR'),
        ('Ministério do Turismo', 'MTUR', 'Secretaria Nacional de Políticas de Turismo', 'SNPTUR/MTUR'),
        ('Ministério do Turismo', 'MTUR', 'Secretaria-Executiva - Ministério do Turismo', 'SE/MTUR'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Comunicação Institucional', 'SECOI/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Estratégias e Redes', 'SERES/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Imprensa', 'SIMP/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Políticas Digitais', 'SPDIGI/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Produção e Divulgação de Conteúdo Audiovisual', 'SEAUD/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria de Publicidade e Patrocínios', 'SPP/SECOM'),
        ('Secretaria de Comunicação Social', 'SECOM', 'Secretaria-Executiva - Secretaria de Comunicação Social', 'SE/SECOM'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria Nacional de Diálogos Sociais e Articulação de Politicas Públicas', 'SNDS/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria Nacional de Juventude', 'DAS/MAPA'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria Nacional de Participação Social', 'SNPS/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria Nacional de Relações Politico-Sociais', 'SNRPS/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva - Secretaria-Geral da Presidência', 'SE/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva da Comissão Nacional de Agroecologia e Produção Orgànica', 'SECNAPO/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva da Comissão Nacional de População em Desenvolvimento', 'SECNPD/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva do Comitê Intermisterial para Inclusão Socioeconômica de Catadoras e Catadores de Materia', 'SECIISC/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva do Conselho Nacional de Fomento e Colaboração', 'SECONFOCO/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva do Conselho Nacional de Juventude', 'SECNJ/SGPR'),
        ('Secretaria-Geral da Presidência', 'SGPR', 'Secretaria-Executiva do Conselho Nacional de Segurança Alimentar e Nutricional', 'SECONSEA/SGPR'),
        ('Secretaria de Relações Institucionais', 'SRI', 'Secretaria Especial de Acompanhamento Governamental', 'SEAG/SRI'),
        ('Secretaria de Relações Institucionais', 'SRI', 'Secretaria Especial de Assuntos Federativos', 'SEAF/SRI'),
        ('Secretaria de Relações Institucionais', 'SRI', 'Secretaria Especial de Assuntos Parlamentares', 'SEPAR/SRI'),
        ('Secretaria de Relações Institucionais', 'SRI', 'Secretaria-Executiva - Secretaria de Relações Institucionais', 'SE/SRI'),
        ('Secretaria de Relações Institucionais', 'SRI', 'Secretaria-Executiva do Conselho da Federação', 'SE/CONSELHO/SRI');

CREATE TEMP TABLE tmp_ministry_secretary_source_normalized AS
SELECT DISTINCT
    regexp_replace(trim(ministry_name), '\s+', ' ', 'g') AS ministry_name,
    upper(regexp_replace(trim(ministry_sigla), '\s+', '', 'g')) AS ministry_sigla,
    regexp_replace(trim(secretary_name), '\s+', ' ', 'g') AS secretary_name,
    upper(regexp_replace(trim(secretary_sigla), '\s+', '', 'g')) AS secretary_sigla
FROM tmp_ministry_secretary_source;

CREATE INDEX idx_tmp_ms_ministry_sigla ON tmp_ministry_secretary_source_normalized (ministry_sigla);
CREATE INDEX idx_tmp_ms_secretary_pair ON tmp_ministry_secretary_source_normalized (ministry_sigla, secretary_name);

-- 1) Upsert ministerios oficiais (por sigla)
UPDATE public_agencies pa
SET
    name = src.ministry_name,
    sigla = src.ministry_sigla,
    public_agency_type = 'MINISTERIO',
    is_client = TRUE,
    is_active = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(pa.updated_by, 1)
FROM (
    SELECT DISTINCT ministry_name, ministry_sigla
    FROM tmp_ministry_secretary_source_normalized
) src
WHERE pa.public_agency_type = 'MINISTERIO'
  AND upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g')) = src.ministry_sigla;

INSERT INTO public_agencies (
    code,
    sigla,
    name,
    cnpj,
    is_client,
    public_agency_type,
    email,
    phone,
    address,
    contact_person,
    city,
    state,
    is_active,
    created_by,
    created_at
)
SELECT
    NULL,
    src.ministry_sigla,
    src.ministry_name,
    NULL,
    TRUE,
    'MINISTERIO',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    TRUE,
    1,
    NOW()
FROM (
    SELECT DISTINCT ministry_name, ministry_sigla
    FROM tmp_ministry_secretary_source_normalized
) src
WHERE NOT EXISTS (
    SELECT 1
    FROM public_agencies pa
    WHERE pa.public_agency_type = 'MINISTERIO'
      AND upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g')) = src.ministry_sigla
);

-- 2) Inativa ministerios antigos criados pelo seed automatico que nao estao na base oficial
UPDATE public_agencies pa
SET
    is_active = FALSE,
    is_client = FALSE,
    updated_at = NOW(),
    updated_by = COALESCE(pa.updated_by, 1)
WHERE pa.public_agency_type = 'MINISTERIO'
  AND pa.created_by = 1
  AND NOT EXISTS (
      SELECT 1
      FROM tmp_ministry_secretary_source_normalized src
      WHERE src.ministry_sigla = upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g'))
  );

CREATE TEMP TABLE tmp_ministry_lookup AS
SELECT
    pa.id AS public_agency_id,
    upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g')) AS ministry_sigla
FROM public_agencies pa
JOIN (
    SELECT DISTINCT ministry_sigla
    FROM tmp_ministry_secretary_source_normalized
) src
  ON src.ministry_sigla = upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g'))
WHERE pa.public_agency_type = 'MINISTERIO';

CREATE INDEX idx_tmp_ministry_lookup_sigla ON tmp_ministry_lookup (ministry_sigla);

-- 3) Upsert secretarias oficiais por (ministerio, nome)
UPDATE secretariats s
SET
    name = src.secretary_name,
    sigla = src.secretary_sigla,
    public_agency_id = ml.public_agency_id,
    is_client = TRUE,
    is_active = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(s.updated_by, 1)
FROM tmp_ministry_secretary_source_normalized src
JOIN tmp_ministry_lookup ml
  ON ml.ministry_sigla = src.ministry_sigla
WHERE s.public_agency_id = ml.public_agency_id
  AND lower(trim(s.name)) = lower(src.secretary_name);

INSERT INTO secretariats (
    code,
    sigla,
    public_agency_id,
    name,
    cnpj,
    is_client,
    email,
    phone,
    address,
    contact_person,
    is_active,
    created_by,
    created_at
)
SELECT
    NULL,
    src.secretary_sigla,
    ml.public_agency_id,
    src.secretary_name,
    NULL,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    TRUE,
    1,
    NOW()
FROM tmp_ministry_secretary_source_normalized src
JOIN tmp_ministry_lookup ml
  ON ml.ministry_sigla = src.ministry_sigla
WHERE NOT EXISTS (
    SELECT 1
    FROM secretariats s
    WHERE s.public_agency_id = ml.public_agency_id
      AND lower(trim(s.name)) = lower(src.secretary_name)
);

-- 4) Inativa secretarias antigas do seed automatico fora da base oficial
UPDATE secretariats s
SET
    is_active = FALSE,
    is_client = FALSE,
    updated_at = NOW(),
    updated_by = COALESCE(s.updated_by, 1)
WHERE s.created_by = 1
  AND s.public_agency_id IN (SELECT public_agency_id FROM tmp_ministry_lookup)
  AND NOT EXISTS (
      SELECT 1
      FROM tmp_ministry_secretary_source_normalized src
      JOIN tmp_ministry_lookup ml
        ON ml.ministry_sigla = src.ministry_sigla
      WHERE ml.public_agency_id = s.public_agency_id
        AND lower(trim(s.name)) = lower(src.secretary_name)
  );

-- 5) Corrige vinculo de secretarias do seed automatico quando sigla aponta para outro ministerio oficial
UPDATE secretariats s
SET
    public_agency_id = ml.public_agency_id,
    is_active = TRUE,
    is_client = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(s.updated_by, 1)
FROM tmp_ministry_lookup ml
WHERE s.created_by = 1
  AND s.sigla IS NOT NULL
  AND ml.ministry_sigla = split_part(
      upper(regexp_replace(trim(s.sigla), '\s+', '', 'g')),
      '/',
      array_length(string_to_array(upper(regexp_replace(trim(s.sigla), '\s+', '', 'g')), '/'), 1)
  )
  AND s.public_agency_id IS DISTINCT FROM ml.public_agency_id;

CREATE INDEX IF NOT EXISTS idx_secretariats_public_agency_active_client
    ON secretariats (public_agency_id, is_active, is_client);
