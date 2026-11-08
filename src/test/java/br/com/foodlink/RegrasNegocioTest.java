package br.com.foodlink;

import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.IndicadorImpacto;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.model.enums.StatusReserva;
import br.com.foodlink.model.enums.StatusRetirada;
import br.com.foodlink.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade das regras de negócio e dos pilares de POO
 * (não dependem de banco de dados).
 */
class RegrasNegocioTest {

    private static Date emHoras(int horas) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, horas);
        return c.getTime();
    }

    private OfertaDoacao ofertaValida() {
        Doador doador = new Doador(UUID.randomUUID(), "Mercado", "m@x.com", "1", HashUtil.hash("1234"));
        Endereco end = new Endereco("Rua A, 1", "Cianorte", "87200-000", -23.66f, -52.60f);
        return doador.cadastrarOferta(end, "Arroz", 10f, 5f, emHoras(48), "obs");
    }

    @Test
    @DisplayName("RN01 - oferta precisa de no minimo 2 horas de validade")
    void rn01ValidadeMinima() {
        assertTrue(ofertaValida().verificarValidadeMinima());

        Doador d = new Doador(UUID.randomUUID(), "D", "d@x.com", "2", "h");
        Endereco e = new Endereco("Rua B", "Cianorte", "87200-000", 0f, 0f);
        OfertaDoacao curta = d.cadastrarOferta(e, "Leite", 1f, 1f, emHoras(1), null);
        assertFalse(curta.verificarValidadeMinima());
    }

    @Test
    @DisplayName("Pilares de POO - classe abstrata, heranca e polimorfismo")
    void pilaresPoo() {
        Usuario doador = new Doador(UUID.randomUUID(), "Doador", "d@x.com", "1", "h");
        Usuario receptor = new Receptor(UUID.randomUUID(), "Receptor", "r@x.com", "2", "h");
        Usuario voluntario = new Voluntario(UUID.randomUUID(), "Voluntario", "v@x.com", "3", "h");

        assertTrue(doador instanceof Usuario);
        assertTrue(receptor instanceof Usuario);
        assertTrue(voluntario instanceof Usuario);

        // polimorfismo: a mesma chamada executa comportamentos diferentes (@Override)
        Usuario[] usuarios = {doador, receptor, voluntario};
        for (Usuario u : usuarios) {
            assertDoesNotThrow(u::notificar);
        }
    }

    @Test
    @DisplayName("Autenticacao compara o hash da senha")
    void autenticacaoComHash() {
        Doador d = new Doador(UUID.randomUUID(), "D", "d@x.com", "1", HashUtil.hash("1234"));
        assertTrue(d.autenticar(HashUtil.hash("1234")));
        assertFalse(d.autenticar(HashUtil.hash("errada")));
    }

    @Test
    @DisplayName("Reserva e Retirada - transicoes de estado do fluxo")
    void transicoesDeEstado() {
        OfertaDoacao oferta = ofertaValida();
        Receptor receptor = new Receptor(UUID.randomUUID(), "R", "r@x.com", "2", "h");
        Voluntario voluntario = new Voluntario(UUID.randomUUID(), "V", "v@x.com", "3", "h");

        Reserva reserva = receptor.reservarOferta(oferta);
        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
        assertEquals(StatusOferta.RESERVADA, oferta.getStatus());

        Retirada retirada = voluntario.assumirRetirada(reserva);
        assertEquals(StatusRetirada.PENDENTE, retirada.getStatus());
        assertThrows(IllegalStateException.class, retirada::registrarEntrega,
                "nao deve entregar sem registrar coleta");

        retirada.registrarColeta();
        assertEquals(StatusRetirada.EM_ANDAMENTO, retirada.getStatus());

        retirada.registrarEntrega();
        assertEquals(StatusRetirada.ENTREGUE, retirada.getStatus());
        assertEquals(StatusReserva.CONCLUIDA, reserva.getStatus());
        assertEquals(StatusOferta.CONCLUIDA, oferta.getStatus());
    }

    @Test
    @DisplayName("RN05 - indicador de impacto calculado pelo peso (2,5 kg CO2/kg)")
    void indicadorDeImpacto() {
        OfertaDoacao oferta = ofertaValida(); // 5 kg
        IndicadorImpacto indicador = new IndicadorImpacto(UUID.randomUUID(), oferta);
        assertEquals(5f, indicador.getPesoTotalKg(), 0.001f);
        assertEquals(12.5f, indicador.getCo2EvitadoKg(), 0.001f);
        assertEquals(10f, indicador.calcularImpacto(4f), 0.001f); // metodo publico
    }
}
