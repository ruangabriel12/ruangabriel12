function revelar() {
    // 1. Alterar a imagem do jogador
    const foto = document.getElementById("FotoJogador");
    foto.src = "img/_vinicius_junior.png";

    // 2. Selecionar os elementos span
    const nome = document.getElementById("Nome");
    const rank = document.getElementById("Rank");
    const dataNas = document.getElementById("Data_Nas");
    const altura = document.getElementById("Altura");
    // Nota: O ID no HTML original continha um espaço "Posição ", 
    // corrigi para "Posicao" no HTML acima para evitar erros.
    const posicao = document.getElementById("Posicao");

    // 3. Atualizar o conteúdo textual conforme Descritivo de Apoio
    nome.innerHTML = "Vinícius José Paixão de Oliveira Júnior";
    rank.innerHTML = "9,5";
    dataNas.innerHTML = "<strong>Data de Nascimento:</strong> 12/07/2000 (25 anos)";
    altura.innerHTML = "<strong>Altura:</strong> 1,76 m";
    posicao.innerHTML = "<strong>Posição:</strong> Ponta-esquerda / Atacante";

    // 4. Remover as classes de placeholder e aplicar card-text
    // Lista de elementos que possuem a classe placeholder
    const elementosParaLimpar = [nome, dataNas, altura, posicao];

    elementosParaLimpar.forEach(el => {
        // Remove a classe placeholder
        el.classList.remove("placeholder");
        el.classList.remove("col-6", "col-8", "col-4");
        
        // Garante que o pai não tenha mais o brilho (glow)
        if(el.parentElement.classList.contains("placeholder-glow")){
            el.parentElement.classList.remove("placeholder-glow");
        }

        // Aplica a classe card-text para finalizar estilização
        el.classList.add("card-text");
    });
    
    // Opcional: Desabilitar o botão após revelar
    document.querySelector("button").disabled = true;
}
