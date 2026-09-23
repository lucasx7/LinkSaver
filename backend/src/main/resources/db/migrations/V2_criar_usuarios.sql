CREATE TABLE usuario(
    id_user SERIAL NUMBER PRIMARY KEY,
    login TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    descricao TEXT,
    data_criacao DEFAULT,
     data_adicionado_banco DEFAULT,
);