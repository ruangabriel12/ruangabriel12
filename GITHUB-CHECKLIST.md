# O que mudar no GitHub para fechar 100% dos critérios da 2ª entrega

Este documento lista, com comandos prontos, exatamente o que precisa ser alterado no repositório
`https://github.com/diegobr185/AEP-FOODLINK-` para cumprir os quatro critérios da 2ª entrega:

| Critério | Peso | Situação antes | Depois deste plano |
|---|---|---|---|
| Aderência ao planejamento (RF01–RF06) | 0,2 | ⚠️ só o modelo | ✅ RF01–RF06 implementados e rastreáveis |
| Pilares de POO (abstrata, herança, `@Override`, CamelCase) | 0,3 | ⚠️ parcial no repo (sem código) | ✅ `Usuario` abstrata + 3 heranças + `@Override` |
| CRUD funcional (entidade principal) | 0,3 | ❌ | ✅ CRUD completo de `OfertaDoacao` (+ usuários) |
| Persistência em BD (conexão + `/database`) | 0,2 | ❌ | ✅ PostgreSQL via JDBC + `script_banco.sql` |

---

## 1. Diagnóstico do repositório hoje

| Item | Estado verificado | Ação |
|---|---|---|
| Link público | ✅ `github.com/diegobr185/AEP-FOODLINK-` | — |
| Pastas `/src`, `/docs`, `/database` | ✅ existem | manter |
| `/database` com scripts SQL | ⚠️ pasta existe, sem os scripts validados | substituir pelos novos |
| `/src` com código | ⚠️ só pastas `model/controller/view` vazias | substituir pelo projeto Maven |
| README com RF01–RF06 | ✅ | manter |
| README com cronograma | ⚠️ datas divergentes do PDF | corrigir |
| README "como executar" | ❌ ausente (exigência da disciplina de SO) | incluir |
| README diz `Pessoa ├── Doador…` | ❌ diagrama usa `Usuario` | corrigir |
| Branches `feature/*` | ❌ só `main` | criar 5 branches + PRs |
| Contribuição de todos | ⚠️ últimos commits de 2 pessoas | distribuir commits (plano abaixo) |
| `.gitignore` | ⚠️ verificar | incluir `target/`, `.idea/` |

---

## 2. Estrutura final do repositório

```
FOODLINK_AEP/
├── pom.xml                       ← NOVO (Maven: compila, testa, empacota)
├── README.md                     ← ATUALIZAR (requisitos + cronograma + como executar)
├── .gitignore                    ← NOVO
├── database/
│   ├── script_banco.sql          ← NOVO (DDL das 6 tabelas + view, reexecutável)
│   ├── dados_exemplo.sql         ← NOVO (massa de dados opcional)
│   └── modelagem_der.sql         ← NOVO (DER documentado)
├── docs/
│   ├── Diagrama_Classe_FOODLINK.png
│   ├── DER_FOODLINK.png
│   └── Entrega_Bimestre_1.pdf
└── src/
    ├── main/java/br/com/foodlink/
    │   ├── FoodLinkApp.java
    │   ├── view/  controller/  service/  repository/  model/  dto/  config/  util/
    │   └── model/enums/
    ├── main/resources/db.properties
    └── test/java/br/com/foodlink/
        ├── RegrasNegocioTest.java      (5 testes)
        └── FluxoIntegracaoTest.java    (7 testes)
```

> ⚠️ **O `README` antigo prometia `src/{model,controller,view}`.** O projeto real usa o layout
> padrão Maven (`src/main/java/...`), que é o esperado por qualquer IDE e pelo `mvn`.
> Atualize a seção "Estrutura do projeto" do README para o layout acima — assim o documento
> continua espelhando o que foi entregue (evita a penalidade de "inconsistência de projeto").

---

## 3. Passo a passo (comandos)

### 3.1 Clonar e limpar o esqueleto antigo

```bash
git clone https://github.com/diegobr185/AEP-FOODLINK-.git
cd AEP-FOODLINK-

# remove as pastas vazias/placeholder de src (o código real vem do projeto Maven)
git rm -r --cached src 2>/dev/null
rm -rf src
```

### 3.2 Copiar o projeto novo

```bash
# a partir da pasta onde você baixou/descompactou o projeto "foodlink"
cp -r foodlink/pom.xml            .
cp -r foodlink/README.md          .
cp -r foodlink/.gitignore         .
cp -r foodlink/src                .
cp -r foodlink/database/*         database/
cp -r foodlink/docs/*             docs/
```

`.gitignore` mínimo:

```gitignore
target/
.idea/
*.iml
out/
.DS_Store
```

---

## 4. Branches e commits (todos os integrantes contribuem)

O `README` do projeto promete o padrão `feature/*` + Conventional Commits. Sequência sugerida —
cada bloco é um PR pequeno, revisável e com autor definido:

```bash
git checkout main

# ---------- 1) Diego: estrutura Maven + modelo do diagrama ----------
git checkout -b feature/estrutura-maven
git add pom.xml .gitignore src/main/java/br/com/foodlink/model src/main/java/br/com/foodlink/config
git commit -m "feat: estrutura Maven e entidades do diagrama de classes"
git push -u origin feature/estrutura-maven

# ---------- 2) Ruan: banco de dados ----------
git checkout main && git checkout -b feature/banco-de-dados
git add database/script_banco.sql database/dados_exemplo.sql database/modelagem_der.sql
git commit -m "feat: script de criacao do banco PostgreSQL e DER"
git push -u origin feature/banco-de-dados

# ---------- 3) Diego: RF01 (cadastro/autenticação) ----------
git checkout main && git checkout -b feature/cadastro-usuario
git add src/main/java/br/com/foodlink/repository/UsuarioRepository.java \
        src/main/java/br/com/foodlink/service/UsuarioService.java \
        src/main/java/br/com/foodlink/controller/UsuarioController.java \
        src/main/java/br/com/foodlink/util/HashUtil.java
git commit -m "feat: cadastro e autenticacao de doador, receptor e voluntario (RF01)"
git push -u origin feature/cadastro-usuario

# ---------- 4) Morales: RF02 + CRUD de ofertas ----------
git checkout main && git checkout -b feature/cadastro-oferta
git add src/main/java/br/com/foodlink/repository/OfertaRepository.java \
        src/main/java/br/com/foodlink/repository/EnderecoRepository.java \
        src/main/java/br/com/foodlink/service/OfertaService.java \
        src/main/java/br/com/foodlink/controller/OfertaController.java
git commit -m "feat: CRUD de ofertas de doacao com validade minima (RF02 e RN01)"
git push -u origin feature/cadastro-oferta

# ---------- 5) Morales: RF03 (busca por raio) ----------
git checkout main && git checkout -b feature/busca-por-localizacao
git add src/main/java/br/com/foodlink/dto/OfertaDTO.java src/main/java/br/com/foodlink/dto/OfertaDistancia.java
git commit -m "feat: consulta de ofertas por localizacao e raio de distancia (RF03)"
git push -u origin feature/busca-por-localizacao

# ---------- 6) Ruan: RF04 (reservas) ----------
git checkout main && git checkout -b feature/reserva-doacao
git add src/main/java/br/com/foodlink/repository/ReservaRepository.java \
        src/main/java/br/com/foodlink/service/ReservaService.java \
        src/main/java/br/com/foodlink/controller/ReservaController.java
git commit -m "feat: reserva de ofertas com limite de 3 ativas e exclusividade (RF04, RN02 e RN03)"
git push -u origin feature/reserva-doacao

# ---------- 7) Ruan: RF05 (logística) ----------
git checkout main && git checkout -b feature/logistica-retirada
git add src/main/java/br/com/foodlink/repository/RetiradaRepository.java \
        src/main/java/br/com/foodlink/service/RetiradaService.java \
        src/main/java/br/com/foodlink/controller/RetiradaController.java
git commit -m "feat: solicitacoes de retirada, coleta e entrega (RF05)"
git push -u origin feature/logistica-retirada

# ---------- 8) Diego: RF06 (histórico e indicadores) ----------
git checkout main && git checkout -b feature/indicadores-impacto
git add src/main/java/br/com/foodlink/repository/IndicadorRepository.java \
        src/main/java/br/com/foodlink/service/IndicadorService.java \
        src/main/java/br/com/foodlink/controller/RelatorioController.java \
        src/main/java/br/com/foodlink/dto
git commit -m "feat: historico de doacoes e indicadores de CO2 evitado (RF06 e RN05)"
git push -u origin feature/indicadores-impacto

# ---------- 9) Morales: interface de console + app ----------
git checkout main && git checkout -b feature/menu-console
git add src/main/java/br/com/foodlink/view src/main/java/br/com/foodlink/FoodLinkApp.java \
        src/main/resources/db.properties
git commit -m "feat: interface de console e integracao das camadas"
git push -u origin feature/menu-console

# ---------- 10) Equipe: testes ----------
git checkout main && git checkout -b feature/testes-automatizados
git add src/test
git commit -m "test: testes de regras de negocio e de integracao com o banco"
git push -u origin feature/testes-automatizados

# ---------- 11) Equipe: documentação ----------
git checkout main && git checkout -b feature/documentacao
git add README.md docs
git commit -m "docs: requisitos, cronograma, arquitetura e instrucoes de execucao"
git push -u origin feature/documentacao
```

Depois, no GitHub: abra 1 PR por branch (`Compare & pull request`), descreva o critério atendido e
faça **Squash and merge** na `main`. Isso deixa histórico legível e comprova a contribuição de todos.

```bash
git checkout main && git pull
git tag -a v2.0.0 -m "Entrega 2 - FoodLink funcional com CRUD e persistencia"
git push origin v2.0.0
```

---

## 5. Ajustes de conteúdo no README do repositório

1. **Trocar `Pessoa` por `Usuario`** na seção "🧩 Orientação a Objetos" — o diagrama e o código usam `Usuario`.
2. **Cronograma:** alinhar as três linhas divergentes com o PDF da 1ª entrega:
   - `10/09/2026 – 11/09/2026` (hoje está `11/10/2026`) · Responsável **Ruan**
   - Backend/banco: **Morales** (hoje está "Arthur")
   - Frontend/integração: **Morales e Diego**
3. **Nova seção "Como executar"** (exigência da disciplina de SO) — copiar de `README.md` do projeto:
   `createdb`, `psql -f database/script_banco.sql`, variáveis `DB_URL/DB_USER/DB_PASSWORD`,
   `mvn clean test`, `mvn package`, `java -jar target/foodlink.jar`.
4. **Nova seção "Banco de dados"** apontando `database/script_banco.sql` e `database/modelagem_der.sql`.
5. **Nova seção "Testes"** com `mvn test` e o resumo (12 testes: 5 unidade + 7 integração).
6. **Imagens:** embutir `docs/Diagrama_Classe_FOODLINK.png` e `docs/DER_FOODLINK.png` no README
   (`![Diagrama](docs/Diagrama_Classe_FOODLINK.png)`).
7. **Tabela RF → onde está implementado** (facilita a correção):

| RF | Implementação | Commit |
|---|---|---|
| RF01 | `UsuarioService.cadastrarDoador/Receptor/Voluntario` + `autenticar` | `feature/cadastro-usuario` |
| RF02 | `OfertaService.cadastrar` + `Doador.cadastrarOferta` | `feature/cadastro-oferta` |
| RF03 | `OfertaRepository.buscarPorRaio` (Haversine em SQL) | `feature/busca-por-localizacao` |
| RF04 | `ReservaService.reservar/cancelar` | `feature/reserva-doacao` |
| RF05 | `RetiradaService.listarSolicitacoes/assumir/registrarColeta/registrarEntrega` | `feature/logistica-retirada` |
| RF06 | `IndicadorService` + `OfertaRepository.listarHistorico` | `feature/indicadores-impacto` |

---

## 6. Checklist de aceitação da 2ª entrega

- [ ] `mvn clean test` roda **12 testes** sem falha (prova que o software executa)
- [ ] `database/script_banco.sql` cria as 6 tabelas + view em PostgreSQL limpo
- [ ] `mvn package` gera `target/foodlink.jar` e `java -jar` conecta no banco
- [ ] CRUD completo de `OfertaDoacao` demonstrado (Create/Read/Update/Delete)
- [ ] `Usuario` abstrata + herança (3 subclasses) + `@Override` em `notificar()`
- [ ] Pasta `/database` com os 3 scripts
- [ ] `README.md` com RF01–RF06, RN01–RN05, cronograma e "Como executar"
- [ ] 5 branches `feature/*` mergeadas + tag `v2.0.0`
- [ ] Commits dos **3 integrantes** (`git shortlog -sne` deve listar Diego, Ruan e Morales)
- [ ] Link do repositório público e no PDF/README

---

## 7. Cuidados que evitam perda de nota

| Risco (documento de diretrizes) | Como o plano resolve |
|---|---|
| "Simulação de CRUD: listas em memória zeram a nota de persistência" | Toda operação passa por `Repository` → JDBC → PostgreSQL |
| "Ausência de um pilar de POO zera 0,3" | `abstract class Usuario` + `abstract notificar()` + 3 heranças + 4 `@Override` |
| "Entrega divergente do planejado" | README/PDF atualizados para refletir exatamente as camadas e o layout entregues |
| "Contribuição de TODOS" | divisão de commits/PRs por integrante (§4) |
| "Instruções de execução (SO)" | seção "Como executar" com JDK, Maven, PostgreSQL e variáveis de ambiente |
