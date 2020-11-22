INSERT INTO customer (id, name, surname) VALUES ( nextval('customerId_seq'), 'John','Doe');
INSERT INTO customer (id, name, surname) VALUES ( nextval('customerId_seq'), 'Fred','Smith');
INSERT INTO orders (id, item, price, customer_id) VALUES ( nextval('orderId_seq'), 'Bike',9999,1);
INSERT INTO orders (id, item, price, customer_id) VALUES ( nextval('orderId_seq'), 'Book',200,1);
INSERT INTO orders (id, item, price, customer_id) VALUES ( nextval('orderId_seq'), 'Switch',49900,1);
INSERT INTO orders (id, item, price, customer_id) VALUES ( nextval('orderId_seq'), 'Zfone',29000,1);