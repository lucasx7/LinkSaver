CREATE TABLE usuario(
    id_user SERIAL PRIMARY KEY,
    login TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    descricao TEXT,
    data_criacao DATE NOT NULL,
    data_adicionado_banco TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);