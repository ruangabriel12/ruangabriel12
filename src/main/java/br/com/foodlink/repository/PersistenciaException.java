package br.com.foodlink.repository;

/** Exceção de infraestrutura de persistência (unchecked). */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem) { super(mensagem); }
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
