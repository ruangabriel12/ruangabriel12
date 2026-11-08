package br.com.foodlink.dto;

/** DTO do RF06 — indicadores consolidados de reaproveitamento e CO2 evitado. */
public class IndicadorResumoDTO {

    private final long doacoesConcluidas;
    private final double pesoTotalKg;
    private final double quantidadeTotal;
    private final double co2EvitadoKg;

    public IndicadorResumoDTO(long doacoesConcluidas, double pesoTotalKg, double quantidadeTotal, double co2EvitadoKg) {
        this.doacoesConcluidas = doacoesConcluidas;
        this.pesoTotalKg = pesoTotalKg;
        this.quantidadeTotal = quantidadeTotal;
        this.co2EvitadoKg = co2EvitadoKg;
    }

    public long getDoacoesConcluidas() { return doacoesConcluidas; }
    public double getPesoTotalKg() { return pesoTotalKg; }
    public double getQuantidadeTotal() { return quantidadeTotal; }
    public double getCo2EvitadoKg() { return co2EvitadoKg; }

    @Override
    public String toString() {
        return String.format("Doações concluídas: %d | Alimentos reaproveitados: %.2f kg (%.2f un) | CO2 evitado: %.2f kg",
                doacoesConcluidas, pesoTotalKg, quantidadeTotal, co2EvitadoKg);
    }
}
