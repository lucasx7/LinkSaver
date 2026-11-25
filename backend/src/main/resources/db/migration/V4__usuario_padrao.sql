INSERT INTO usuario (login, email, data_criacao)
VALUES ('padrao', 'padrao@linksaver.dev', CURRENT_DATE)
ON CONFLICT DO NOTHING;
