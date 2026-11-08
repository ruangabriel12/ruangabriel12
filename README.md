# 🍎 FoodLink

**Plataforma Inteligente de Redução de Desperdício Alimentar e Logística Solidária**

Projeto AEP — Engenharia de Software / ADS · Unicesumar · 2026 · `ESOFT-4S-N-B`
Equipe: **Diego Brito** (25006858-2) · **Ruan Gabriel** (25017807-2) · **Arthur Morales** (25225017-2)

---

## 📌 Sobre o projeto

O **FoodLink** reduz o desperdício de alimentos próprios para consumo e facilita o encaminhamento de
excedentes para organizações sociais, conectando **quem possui excedentes** com **quem precisa recebê-los**.

```
Doador → Oferta → Receptor → Reserva → Voluntário → Retirada/Entrega → Indicador de impacto
```

**ODS atendidos:** ODS 2 · ODS 12 · ODS 13 · ODS 17.

---

## 📋 Requisitos Funcionais

| Código | Requisito |
| --- | --- |
| **RF01** | Cadastro e autenticação de usuários dos tipos Doador, Receptor e Voluntário. |
| **RF02** | O Doador cadastra uma oferta com alimentos, quantidade/peso, validade, endereço e observações. |
| **RF03** | O Receptor consulta ofertas disponíveis por localização e raio de distância. |
| **RF04** | O Receptor reserva uma oferta e acompanha o status da reserva até a retirada. |
| **RF05** | O Voluntário consulta e assume solicitações de retirada, registrando coleta e entrega. |
| **RF06** | O sistema registra o histórico das doações e calcula os indicadores de impacto. |

## 📐 Regras de Negócio

| Código | Regra |
| --- | --- |
| **RN01** | A oferta precisa de pelo menos **2 horas de validade** no momento da publicação. |
| **RN02** | Um Receptor pode manter no máximo **3 reservas ativas** simultaneamente. |
| **RN03** | Uma oferta já reservada **não pode** ser reservada por outro Receptor. |
| **RN04** | Doações concluídas **permanecem registradas** para histórico e auditoria. |
| **RN05** | O impacto ambiental é calculado pelo **peso** dos alimentos (2,5 kg de CO₂ por kg). |

---

## 🏗️ Arquitetura e estrutura

**Monolito modular em camadas:**

```
View (console)  →  Controller  →  Service  →  Repository (JDBC)  →  PostgreSQL
```

```
FOODLINK_AEP/
├── pom.xml                  # Maven com <sourceDirectory>src</sourceDirectory>
├── README.md
├── database/
│   └── foodlink.sql         # script ÚNICO: 6 tabelas + 1 view + dados de exemplo
├── docs/
│   └── Diagrama_Classe_FOODLINK.png
└── src/                     # estrutura da entrega do 1º bimestre
    ├── model/               # Usuario (abstrata), Doador, Receptor, Voluntario, Endereco,
    │   └── enums/           # OfertaDoacao, Reserva, Retirada, IndicadorImpacto
    │                        # StatusOferta, StatusReserva, StatusRetirada
    ├── controller/          # UsuarioController · OfertaController · ReservaController
    ├── view/                # MenuConsole
    ├── service/             # UsuarioService · OfertaService · ReservaService (RN01–RN05)
    ├── repository/          # UsuarioRepository · OfertaRepository · ReservaRepository (JDBC)
    ├── config/              # ConexaoDB
    ├── app/                 # FoodLinkApp (main)
    ├── db.properties        # configuração da conexão
    └── test/                # RegrasNegocioTest (5) + FluxoIntegracaoTest (7)
```

São **27 arquivos Java** (era 40): os 4 DTOs, a interface `ConnectionProvider`, o `HashUtil`,
o `EnderecoRepository` e os controllers/services/repositories de retirada e de relatório
foram absorvidos pelas classes que já existiam — nada de funcionalidade foi perdido.

---

## 💻 Tecnologias

| Tecnologia | Papel |
| --- | --- |
| **Java 11+** | Backend (POO, herança, enums, coleções) |
| **PostgreSQL** | Banco de dados oficial |
| **JDBC** | Persistência sem ORM (SQL explícito) |
| **JUnit 5** | Testes das regras e do fluxo completo |
| **Maven** | Build, testes e empacotamento |

---

## 🗄️ Banco de dados

Um único arquivo: **`database/foodlink.sql`** — cria as 6 tabelas (`endereco`, `usuario`,
`oferta_doacao`, `reserva`, `retirada`, `indicador_impacto`), a view `vw_historico_doacoes`
e já insere os dados de exemplo (senha de todos: **1234**). Pode ser reexecutado à vontade.

Herança mapeada como tabela única `usuario` com discriminador `tipo`
(`DOADOR` | `RECEPTOR` | `VOLUNTARIO`).

---

## ▶️ Como executar

**Pré-requisitos:** JDK 11+ · Maven 3.8+ · PostgreSQL 12+

```bash
# 1) criar o banco
psql -U postgres -c "CREATE DATABASE foodlink;"

# 2) criar tabelas + dados de exemplo (script único)
psql -U postgres -d foodlink -f database/foodlink.sql

# 3) conexão: edite src/db.properties ou use variáveis de ambiente
export DB_URL="jdbc:postgresql://localhost:5432/foodlink"
export DB_USER="postgres"
export DB_PASSWORD="sua_senha"

# 4) compilar, testar e executar
mvn clean test
mvn package
java -jar target/foodlink.jar
```

**Windows/PowerShell:**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/foodlink"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua_senha"
java -jar target/foodlink.jar
```

### Roteiro rápido de demonstração

1. `2) Entrar` com `ong@foodlink.com` / `1234` → `1) Buscar ofertas por raio` (lat −23.66, long −52.60, 5 km)
2. `2) Reservar oferta` → informe o id exibido
3. `0) Sair` → `2) Entrar` com `ana@foodlink.com` / `1234` → `1) Solicitações` → `2) Assumir retirada`
4. `3) Registrar coleta` e `4) Registrar entrega` (use o id da retirada exibido)
5. Menu principal → `4) Relatório de impacto` → histórico + CO₂ evitado

---

## 🧩 Pilares de POO aplicados

| Pilar | Onde |
| --- | --- |
| **Classe abstrata** | `Usuario` (`abstract class` + `abstract void notificar()` + `abstract String getTipo()`) |
| **Herança** | `Doador`, `Receptor`, `Voluntario` estendem `Usuario` |
| **Polimorfismo (`@Override`)** | `notificar()` e `getTipo()` sobrescritos nas três subclasses |
| **Encapsulamento** | atributos `private` + getters/setters |
| **Composição** | `Doador 1 — 0..* OfertaDoacao` · `Voluntario 1 — 0..* Retirada` · `Reserva 1 — 0..1 Retirada` |
| **Enums tipados** | `StatusOferta`, `StatusReserva`, `StatusRetirada` |

## 🔁 CRUD completo (entidade principal: `OfertaDoacao`)

| Operação | Onde |
| --- | --- |
| **Create** | `OfertaService.cadastrar()` · menu do Doador → "Publicar oferta" |
| **Read** | `listarDisponiveis()`, `buscarPorRaio()`, `buscarPorId()`, `listarPorDoador()` |
| **Update** | `OfertaService.atualizar()` · menu do Doador → "Editar oferta" |
| **Delete** | `OfertaService.excluir()` · menu do Doador → "Excluir oferta" |

O CRUD de usuários (`UsuarioService`) também está implementado.

---

## 🧪 Testes

```bash
mvn clean test
```

- **`RegrasNegocioTest`** (5): RN01, polimorfismo, hash de senha, transições de estado, RN05.
- **`FluxoIntegracaoTest`** (7): executa o **mesmo `database/foodlink.sql`** e valida RF01–RF06,
  RN01–RN03 e o CRUD completo com persistência real
  (H2 em modo PostgreSQL nos testes; PostgreSQL em produção).

Resultado esperado: `Tests run: 12, Failures: 0, Errors: 0`.

---

## 📅 Cronograma de execução

| Data | Atividade | Responsável |
| --- | --- | --- |
| 06/09 – 11/09 | Levantamento do problema, requisitos, escopo e regras de negócio | Diego |
| 09/09 – 11/09 | Modelagem UML e DER | Ruan |
| 12/09 – 20/09 | Modelagem do banco e protótipo das telas | Arthur |
| 21/09 – 05/10 | Implementação das regras de negócio e integrações | Diego |
| 06/10 – 11/10 | Testes e ajustes finais | Ruan e Diego |

---

## 👥 Equipe

| Nome | RA |
| --- | --- |
| Diego Brito | 25006858-2 |
| Ruan Gabriel | 25017807-2 |
| Arthur Morales | 25225017-2 |
