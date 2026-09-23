CREATE TABLE links_list(
    id_link SERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    descricao TEXT,
    data_criacao DEFAULT,
    data_adicionado_banco DEFAULT,
    id_user INTEGER NOT NULL,

    FOREIGN KEY (id_user) REFERENCES usuarios(id)
);