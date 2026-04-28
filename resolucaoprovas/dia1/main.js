/* Script para a Questão 03 */
function mostrarTabela() {
    const container = document.getElementById('container-tabela');
    
    // Verifica se a imagem já foi inserida para não repetir
    if (document.getElementById('img-tabela')) return;
    // Cria o elemento de imagem
    const img = document.createElement('img');
    
    // Define o caminho da imagem (conforme descritivo de apoio)
    // Na estrutura do projeto Vite, colocamos em public/img/
    img.src = 'img/tabela_copa_2026.png'; 
    img.alt = 'Tabela Oficial de Jogos - Copa do Mundo 2026';
    img.id = 'img-tabela';
    
    // Estilização básica para a imagem não quebrar o layout
    img.style.maxWidth = '100%';
    img.style.marginTop = '20px';
    img.style.borderRadius = '10px';
    img.style.boxShadow = '0 10px 20px rgba(0,0,0,0.2)';
    // Insere no container ao final da página
    container.appendChild(img);
    
    // Opcional: Desabilitar o botão após o uso
    document.getElementById('btnTabela').innerText = 'Tabela Carregada';
    document.getElementById('btnTabela').disabled = true;
}
