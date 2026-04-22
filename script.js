function mostrarTabela() {

    if (document.getElementById("tabelaJogos")) {
        alert("A tabela já foi exibida!");
        return;
    }

    let img = document.createElement("img");

    img.src = "Tabela_Jogos.png";

    img.id = "tabelaJogos";
    img.alt = "Tabela de Jogos da Copa do Mundo 2026";

    img.style.display = "block";
    img.style.margin = "30px auto";
    img.style.maxWidth = "90%";
    img.style.borderRadius = "10px";

    document.body.appendChild(img);
}