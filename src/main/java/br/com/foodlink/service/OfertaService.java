package br.com.foodlink.service;

import br.com.foodlink.dto.HistoricoDTO;
import br.com.foodlink.dto.OfertaDTO;
import br.com.foodlink.dto.OfertaDistancia;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.repository.EnderecoRepository;
import br.com.foodlink.repository.OfertaRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * RF02 (cadastro de oferta), RF03 (consulta por localização/raio) e CRUD da entidade principal.
 * RN01 é verificada no ato da publicação.
 */
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final EnderecoRepository enderecoRepository;

    public OfertaService(OfertaRepository ofertaRepository, EnderecoRepository enderecoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.enderecoRepository = enderecoRepository;
    }

    /** CREATE — RF02 + RN01 */
    public OfertaDoacao cadastrar(Doador doador, Endereco endereco, String alimento, float quantidade,
                                 float peso, Date dataValidade, String observacoes) {
        if (doador == null) {
            throw new IllegalArgumentException("O doador deve estar autenticado.");
        }
        OfertaDoacao oferta = doador.cadastrarOferta(endereco, alimento, quantidade, peso, dataValidade, observacoes);
        validar(oferta);
        enderecoRepository.salvar(endereco);
        ofertaRepository.salvar(oferta);
        doador.notificar();
        return oferta;
    }

    /** UPDATE */
    public void atualizar(UUID ofertaId, String alimento, float quantidade, float peso,
                          Date dataValidade, String observacoes) {
        OfertaDoacao oferta = ofertaRepository.buscarPorId(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));
        if (oferta.getStatus() != StatusOferta.DISPONIVEL) {
            throw new IllegalStateException("Somente ofertas DISPONÍVEIS podem ser editadas (status atual: "
                    + oferta.getStatus() + ").");
        }
        if (alimento != null && !alimento.trim().isEmpty()) {
            oferta.setAlimento(alimento.trim());
        }
        if (quantidade > 0) {
            oferta.setQuantidade(quantidade);
        }
        if (peso > 0) {
            oferta.setPeso(peso);
        }
        if (dataValidade != null) {
            oferta.setDataValidade(dataValidade);
        }
        if (observacoes != null) {
            oferta.setObservacoes(observacoes);
        }
        if (!oferta.verificarValidadeMinima()) {
            throw new IllegalStateException("RN01: a oferta deve ter pelo menos 2 horas de validade.");
        }
        ofertaRepository.atualizar(oferta);
    }

    /** DELETE — bloqueado quando a oferta já está reservada/concluída. */
    public void excluir(UUID ofertaId) {
        OfertaDoacao oferta = ofertaRepository.buscarPorId(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));
        if (oferta.getStatus() != StatusOferta.DISPONIVEL) {
            throw new IllegalStateException("Não é possível excluir oferta com status " + oferta.getStatus() + ".");
        }
        ofertaRepository.excluir(ofertaId);
    }

    public OfertaDoacao buscarPorId(UUID id) {
        return ofertaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));
    }

    /** READ — ofertas disponíveis (RF03 sem filtro de distância) */
    public List<OfertaDTO> listarDisponiveis() {
        return converter(ofertaRepository.listarDisponiveis());
    }

    /** READ — ofertas disponíveis dentro de um raio (RF03) */
    public List<OfertaDTO> buscarPorRaio(double latitude, double longitude, double raioKm) {
        if (raioKm <= 0) {
            throw new IllegalArgumentException("Informe um raio maior que zero.");
        }
        return converter(ofertaRepository.buscarPorRaio(latitude, longitude, raioKm));
    }

    /** READ — ofertas de um doador */
    public List<OfertaDTO> listarPorDoador(UUID doadorId) {
        return converter(ofertaRepository.listarPorDoador(doadorId));
    }

    /** RF06 / RN04 — histórico de doações concluídas */
    public List<HistoricoDTO> listarHistorico() {
        return ofertaRepository.listarHistorico();
    }

    private List<OfertaDTO> converter(List<OfertaDistancia> origens) {
        List<OfertaDTO> dtos = new ArrayList<>();
        for (OfertaDistancia o : origens) {
            dtos.add(new OfertaDTO(o.getOferta(), o.getDistanciaKm()));
        }
        return dtos;
    }

    private void validar(OfertaDoacao oferta) {
        if (oferta.getAlimento() == null || oferta.getAlimento().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o alimento.");
        }
        if (oferta.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (oferta.getPeso() <= 0) {
            throw new IllegalArgumentException("O peso deve ser maior que zero.");
        }
        if (oferta.getDataValidade() == null) {
            throw new IllegalArgumentException("Informe a data de validade.");
        }
        if (!oferta.verificarValidadeMinima()) {
            throw new IllegalStateException("RN01: a oferta deve possuir pelo menos 2 horas de validade na publicação.");
        }
        if (oferta.getEndereco() == null) {
            throw new IllegalArgumentException("Informe o endereço de retirada.");
        }
    }
}
