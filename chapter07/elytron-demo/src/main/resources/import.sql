/*
ajoute pour éviter la levée d'une exception lors des tests si la table a déjà été créé lors du lancement d'un test précédent
 */
DROP TABLE if exists quarkus_user;
/*
création d'une table stockant les infos d'authentification et d'autorisation utilisées par Elytron
permet d'obtenir les principals représentants les utilisateurs authentifiés.
 */
CREATE TABLE quarkus_user (
    id INT,
    username VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255)
);
INSERT INTO quarkus_user (id, username, password, role) VALUES (1, 'joe', '123', 'admin');
INSERT INTO quarkus_user (id, username, password, role) VALUES (2, 'frank','123', 'user');
/*
insertions de données métiers
 */
INSERT INTO customer (id, name, surname) VALUES ( nextval('customerId_seq'), 'John','Doe');
INSERT INTO customer (id, name, surname) VALUES ( nextval('customerId_seq'), 'Fred','Smith');