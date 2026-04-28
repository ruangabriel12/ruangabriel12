function add() {
    // 1. Selecionar o container onde os cards estão
    const container = document.getElementById('Cards');

    // 2. Definir o HTML do novo card com os dados do Descritivo de Apoio
    const novoCardHTML = `
        <div class="card" style="width: 22rem;">
            <img src="img/Lucas_Paqueta.png" class="card-img-top" alt="Lucas Paquetá">
            <div class="card-body">
                <h5 class="card-title">
                    <span>Lucas Paquetá</span>
                    <span class="badge text-bg-secondary">8,8</span>
                </h5>
                <p class="card-text">
                    <strong>Nascimento:</strong> 27/08/1997 (28 anos)<br>
                    <strong>Altura:</strong> 1,80 m<br>
                    <strong>Posição:</strong> Meio-campista<br>
                </p>
            </div>
        </div>
    `;

    // 3. Inserir o novo card sem remover o anterior
    // Utilizamos o += para concatenar o novo conteúdo ao existente
    container.innerHTML += novoCardHTML;
    
    // Opcional: Desabilitar o botão após adicionar para não repetir o mesmo jogador
    const btn = document.querySelector('button');
    btn.disabled = true;
    btn.innerText = "Jogador Adicionado";
}
