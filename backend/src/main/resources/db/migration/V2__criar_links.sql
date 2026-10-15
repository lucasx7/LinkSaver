CREATE TABLE links_list(
    id_link SERIAL PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    descricao TEXT,
    data_criacao DATE NOT NULL,
    data_adicionado_banco TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_user INTEGER NOT NULL,

    FOREIGN KEY (id_user) REFERENCES usuario(id_user)
);