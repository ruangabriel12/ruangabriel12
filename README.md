# 🍎 FoodLink

**Plataforma Inteligente de Redução de Desperdício Alimentar e Logística Solidária**

Projeto AEP — Engenharia de Software / ADS · Unicesumar · 2026 · `ESOFT-4S-N-B`
Equipe: **Diego Brito** (25006858-2) · **Ruan Gabriel** (25017807-2) · **Arthur Morales** (25225017-2)

---

## 📌 Sobre o projeto

O **FoodLink** reduz o desperdício de alimentos próprios para consumo e facilita o encaminhamento de
excedentes para organizações sociais, conectando de forma organizada **quem possui excedentes** com
**quem precisa recebê-los**.

Fluxo do sistema:

```
Doador → Oferta → Receptor → Reserva → Voluntário → Retirada/Entrega → Registro de impacto
```

**ODS atendidos:** ODS 2 (Fome Zero) · ODS 12 (Consumo e Produção Responsáveis) ·
ODS 13 (Ação Contra a Mudança Global do Clima) · ODS 17 (Parcerias).

---

## 📋 Requisitos Funcionais

| Código | Requisito |
| --- | --- |
| **RF01** | O sistema deve permitir o cadastro e a autenticação de usuários dos tipos Doador, Receptor e Voluntário, com nome, e-mail, documento de identificação e dados de acesso. |
| **RF02** | O sistema deve permitir que o Doador cadastre uma oferta de doação informando os alimentos, quantidade ou peso, data de validade, endereço de retirada e observações. |
| **RF03** | O sistema deve permitir que o Receptor consulte ofertas disponíveis por localização e raio de distância, visualizando os dados da doação e sua validade. |
| **RF04** | O sistema deve permitir que o Receptor reserve uma oferta disponível e acompanhe o status da reserva até a retirada. |
| **RF05** | O sistema deve permitir que um Voluntário consulte e assuma solicitações de retirada, registrando a coleta e a entrega da doação. |
| **RF06** | O sistema deve registrar o histórico das doações e calcular indicadores de quantidade de alimentos reaproveitados e estimativa de CO₂ evitado. |

## 📐 Regras de Negócio

| Código | Regra |
| --- | --- |
| **RN01** | Uma oferta deve possuir pelo menos **2 horas de validade restantes** no momento da publicação. |
| **RN02** | Um Receptor pode manter no máximo **3 reservas ativas simultaneamente**. |
| **RN03** | Uma oferta já reservada **não pode** ser reservada por outro Receptor. |
| **RN04** | Doações concluídas **permanecem registradas** para histórico e auditoria. |
| **RN05** | A estimativa de impacto ambiental é calculada a partir do **peso** dos alimentos registrados. |

---

## 🏗️ Arquitetura

**Monolito modular em camadas** (escolha justificada na entrega do 1º bimestre):

```
View (console)  →  Controller  →  Service  →  Repository (JDBC)  →  PostgreSQL
```

```
src/main/java/br/com/foodlink/
├── FoodLinkApp.java          # ponto de entrada (wiring das camadas)
├── view/                     # MenuConsole — interface de console
├── controller/               # UsuarioController, OfertaController, ReservaController, RetiradaController, RelatorioController
├── service/                  # regras de negócio (RN01–RN05)
├── repository/               # JDBC + SQL (CRUD, busca por raio, histórico, indicadores)
├── model/                    # entidades do diagrama de classes + enums
│   └── enums/                # StatusOferta, StatusReserva, StatusRetirada
├── dto/                      # OfertaDTO, OfertaDistancia, HistoricoDTO, IndicadorResumoDTO
├── config/                   # ConexaoDB / ConnectionProvider
└── util/                     # HashUtil (SHA-256), GeoUtil (Haversine)
```

**Diagramas:** `docs/Diagrama_Classe_FOODLINK.png` (UML) e
`database/modelagem_der.sql` (DER documentado em DDL comentada).

---

## 💻 Tecnologias

| Tecnologia | Papel | Justificativa |
| --- | --- | --- |
| **Java 11+** | Backend | Tipagem forte, ecossistema maduro para autenticação, regras de reserva, controle de estados e serviços. |
| **PostgreSQL** | Banco de dados | Consistência transacional para reservas e histórico; consultas por proximidade implementadas em SQL (Haversine), com PostGIS opcional. |
| **JDBC** | Persistência | Acesso direto ao banco, sem ORM, evidenciando o SQL e o mapeamento objeto-relacional. |
| **JUnit 5** | Testes | Validação automatizada das regras de negócio e do fluxo completo. |
| **Maven** | Build | Compilação, testes e empacotamento padronizados. |

---

## 🗄️ Banco de dados

```
database/
├── script_banco.sql      # DDL completo (6 tabelas + view), reexecutável
├── dados_exemplo.sql     # massa de dados opcional (senha dos usuários: 1234)
└── modelagem_der.sql     # DER documentado + correspondência com o diagrama de classes
```

Mapeamento da herança: tabela única `usuario` com discriminador `tipo`
(`DOADOR` | `RECEPTOR` | `VOLUNTARIO`).

---

## ▶️ Como executar

**Pré-requisitos:** JDK 11+ · Maven 3.8+ · PostgreSQL 12+

```bash
# 1) criar o banco
createdb foodlink          # ou: psql -U postgres -c "CREATE DATABASE foodlink;"

# 2) criar as tabelas (o script pode ser reexecutado)
psql -U postgres -d foodlink -f database/script_banco.sql

# 3) (opcional) dados de exemplo
psql -U postgres -d foodlink -f database/dados_exemplo.sql

# 4) configurar a conexão
#    edite src/main/resources/db.properties  ou use variáveis de ambiente:
export DB_URL="jdbc:postgresql://localhost:5432/foodlink"
export DB_USER="postgres"
export DB_PASSWORD="sua_senha"

# 5) compilar, testar e executar
mvn clean test
mvn package
java -jar target/foodlink.jar      # ou: mvn exec:java
```

No **Windows/PowerShell**:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/foodlink"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua_senha"
java -jar target/foodlink.jar
```

### Roteiro rápido de demonstração

1. `2) Entrar` com `ong@foodlink.com` / `1234` → `1) Buscar ofertas por raio` (−23.66 / −52.60 / 5 km)
2. `2) Reservar oferta` → informe o id de uma oferta
3. `0) Sair` → `2) Entrar` com `ana@foodlink.com` / `1234` → `1) Ver solicitações` → `2) Assumir retirada`
4. `3) Registrar coleta` e `4) Registrar entrega` (informe o id da retirada exibido)
5. Menu principal → `4) Relatórios` → histórico e indicadores de CO₂

---

## 🧩 Pilares de POO aplicados

| Pilar | Onde |
| --- | --- |
| **Classe abstrata** | `Usuario` (`abstract class` + `abstract void notificar()`) |
| **Herança** | `Doador`, `Receptor`, `Voluntario` estendem `Usuario` |
| **Polimorfismo (`@Override`)** | `notificar()` sobrescrito nas três subclasses; chamado via `Usuario` |
| **Encapsulamento** | atributos `private` + getters/setters |
| **Composição (1:N)** | `Doador 1 — 0..* OfertaDoacao`; `Voluntario 1 — 0..* Retirada` |
| **Classes tipadas / CamelCase / enums** | `StatusOferta`, `StatusReserva`, `StatusRetirada` |

## 🔁 CRUD completo (entidade principal: `OfertaDoacao`)

| Operação | Onde |
| --- | --- |
| **Create** | `OfertaService.cadastrar()` / menu do Doador → "Publicar oferta" |
| **Read** | `listarDisponiveis()`, `buscarPorRaio()`, `buscarPorId()`, `listarPorDoador()` |
| **Update** | `OfertaService.atualizar()` / menu do Doador → "Editar oferta" |
| **Delete** | `OfertaService.excluir()` / menu do Doador → "Excluir oferta" |

CRUD de usuários (`UsuarioService`) também está disponível.

---

## 🧪 Testes

```bash
mvn test
```

- `RegrasNegocioTest` (5 testes): RN01, polimorfismo, hash de senha, transições de estado, RN05.
- `FluxoIntegracaoTest` (7 testes): executa o **mesmo `database/script_banco.sql`** e valida
  RF01–RF06, RN01–RN05 e o CRUD completo com persistência real
  (H2 em modo PostgreSQL nos testes; PostgreSQL em produção).

---

## 📅 Cronograma de execução

| Data | Atividade | Responsável |
| --- | --- | --- |
| 06/09/2026 – 07/09/2026 | Levantamento do problema, stakeholders e requisitos | Diego |
| 07/09/2026 – 08/09/2026 | Definição do escopo, regras de negócio e ODS | Diego |
| 09/09/2026 – 11/09/2026 | Modelagem UML e DER | Ruan |
| 10/09/2026 – 11/09/2026 | Configuração do repositório e estrutura inicial | Ruan |
| A Definir | Implementação do backend e banco de dados | Morales |
| A Definir | Implementação do frontend e integração | Morales e Diego |
| A Definir | Testes, correções e documentação | Equipe |

---

## 🔗 Repositório

https://github.com/diegobr185/AEP-FOODLINK-

Branches: `main` e `feature/*` · Commits no padrão **Conventional Commits**
(`feat:`, `fix:`, `docs:`, `test:`, `refactor:`).
