CREATE SCHEMA IF NOT EXISTS cashreg;


---------------   CASHBOX    ---------------

CREATE SEQUENCE cashreg.seq_cashbox_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.cashbox
(
  id       BIGINT       NOT NULL DEFAULT nextval('cashreg.seq_cashbox_id' :: REGCLASS),
  name     VARCHAR(255) NOT NULL,
  url      VARCHAR(255) NOT NULL,
  settings TEXT                  DEFAULT '{}',
  comment  VARCHAR(255),
  CONSTRAINT cashbox_pkey PRIMARY KEY (id)
);

COMMENT ON COLUMN cashreg.cashbox.url is 'URL adapter';

---------------   ACCOUNT   ---------------

CREATE SEQUENCE cashreg.seq_account_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.account
(
  id          BIGINT       NOT NULL DEFAULT nextval('seq_account_id' :: REGCLASS),
  merchant_id VARCHAR(255) NOT NULL,
  shop_id     VARCHAR(255),
  cashbox_id  BIGINT       NULL REFERENCES cashreg.cashbox (id),
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  comment     VARCHAR(255),
  CONSTRAINT account_pkey PRIMARY KEY (id)
);


---------------   PAYER INFO   ---------------

CREATE SEQUENCE cashreg.seq_payer_info_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.payer_info
(
  id           BIGINT       NOT NULL DEFAULT nextval('cashreg.seq_payer_info_id' :: REGCLASS),
  contact      VARCHAR(255) NOT NULL,
  contact_type VARCHAR(255) NOT NULL,
  CONSTRAINT payer_info_pkey PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS payer_info_contact
  ON cashreg.payer_info (contact);

---------------   REFUND    ---------------

CREATE SEQUENCE cashreg.seq_refund_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.refund
(
  id            BIGINT       NOT NULL DEFAULT nextval('cashreg.seq_refund_id' :: REGCLASS),
  refund_id     VARCHAR(255) NOT NULL,
  amount        NUMERIC      NOT NULL,
  cart          TEXT                  DEFAULT '{}',
  previous_cart TEXT                  DEFAULT '{}',
  status        VARCHAR(12)           DEFAULT 'none',
  CONSTRAINT refund_pkey PRIMARY KEY (id)
);

---------------   PAYMENT   ---------------

CREATE SEQUENCE cashreg.seq_payment_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.payment
(
  id             BIGINT       NOT NULL DEFAULT nextval('cashreg.seq_payment_id' :: REGCLASS),
  amount         NUMERIC      NOT NULL,
  partial_amount NUMERIC      NULL,
  currency       VARCHAR(3)   NOT NULL,
  payment_id     VARCHAR(255) NOT NULL,
  payer_info_id  BIGINT       NOT NULL REFERENCES cashreg.payer_info (id),
  refund_id      BIGINT       NULL REFERENCES cashreg.refund (id),
  payment_type   VARCHAR(255) NOT NULL,
  capture_cart   TEXT                  DEFAULT '{}',
  status         VARCHAR(12)           DEFAULT 'none',
  CONSTRAINT payment_pkey PRIMARY KEY (id)
);

COMMENT ON COLUMN cashreg.payment.payment_id is 'Payment ID. Example (1)';
COMMENT ON COLUMN cashreg.payment.refund_id is 'Refund ID. Example (1AkoSR8NZbM)';


---------------   INVOICE_PAYER    ---------------

CREATE SEQUENCE cashreg.seq_invoice_payer_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.invoice_payer
(
  id            BIGINT       NOT NULL DEFAULT nextval('cashreg.seq_invoice_payer_id' :: REGCLASS),
  invoice_id    VARCHAR(255) NOT NULL,
  payment_id    BIGINT       NULL REFERENCES cashreg.payment (id),
  account_id    BIGINT       NOT NULL REFERENCES cashreg.account (id),
  currency      VARCHAR(3)   NOT NULL,
  amount        NUMERIC      NOT NULL,
  metadata      TEXT                  DEFAULT '{}',
  cart          TEXT                  DEFAULT '{}',
  exchange_cart TEXT                  DEFAULT '{}',
  CONSTRAINT invoice_payer_pkey PRIMARY KEY (id)
);


---------------   CASHREG DELIVERY    ---------------

CREATE SEQUENCE cashreg.seq_cashreg_delivery_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.cashreg_delivery
(
  id               BIGINT      NOT NULL DEFAULT nextval('cashreg.seq_cashreg_delivery_id' :: REGCLASS),
  invoice_id       BIGINT      NOT NULL REFERENCES cashreg.invoice_payer (id),
  payment_id       BIGINT      NOT NULL REFERENCES cashreg.payment (id),
  refund_id        BIGINT      NULL REFERENCES cashreg.refund (id),
  type_operation   VARCHAR(21) NOT NULL,
  request_id       VARCHAR(255),
  cart_state       VARCHAR(255)         DEFAULT 'full',
  cashreg_status   VARCHAR(255)         DEFAULT 'none',
  cashreg_uuid     VARCHAR(255),
  cashreg_response TEXT,
  timestamp        timestamp            DEFAULT now(),
  CONSTRAINT cashreg_delivery_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS cashreg_delivery_cashreg_status
  ON cashreg.cashreg_delivery (cashreg_status);
CREATE INDEX IF NOT EXISTS cashreg_delivery_status_type_operation
  ON cashreg.cashreg_delivery (cashreg_status, type_operation);


---------------   CASHREG STATUS   ---------------

CREATE SEQUENCE cashreg.seq_cashreg_status_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.cashreg_status
(
  id      BIGINT      NOT NULL DEFAULT nextval('cashreg.seq_cashreg_status_id' :: REGCLASS),
  name    VARCHAR(21) NOT NULL,
  comment VARCHAR(255),
  CONSTRAINT cashreg_status_pkey PRIMARY KEY (id)
);


INSERT INTO cashreg.cashreg_status (name, comment)
VALUES ('none', 'Нет статуса'),
       ('error', 'Ошибка, надо разбираться, что пошло не так'),
       ('ready', 'Данные готовы к отправке чека'),
       ('sent', 'Чек отправлен, но нужно запросить финальный статус'),
       ('fail', 'Чек не отправлен, нужно уточнять причины'),
       ('done', 'Чек успешно доставлен');

---------------   CART_STATE   ---------------

CREATE SEQUENCE cashreg.seq_cart_state_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.cart_state
(
  id      BIGINT      NOT NULL DEFAULT nextval('cashreg.seq_cart_state_id' :: REGCLASS),
  name    VARCHAR(21) NOT NULL,
  comment VARCHAR(255),
  CONSTRAINT cart_state_pkey PRIMARY KEY (id)
);


INSERT INTO cashreg.cart_state (name, comment)
VALUES ('full', 'Полная корзина'),
       ('partial', 'Частично заполненная');


---------------   TYPE OPERATION   ---------------

CREATE SEQUENCE cashreg.seq_type_operation_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.type_operation
(
  id      BIGINT      NOT NULL DEFAULT nextval('cashreg.seq_type_operation_id' :: REGCLASS),
  name    VARCHAR(21) NOT NULL,
  comment VARCHAR(255),
  CONSTRAINT type_operation_pkey PRIMARY KEY (id)
);

INSERT INTO cashreg.type_operation (name, comment)
VALUES ('debit', 'Приход (доход)'),
       ('credit', 'Расход'),
       ('refund_debit', 'Возврат прихода (дохода)'),
       ('refund_credit', 'Возврат расхода');


---------------   CASHREG DELIVERY HISTORY   ---------------
---------------  Save any changes in table   ---------------

CREATE SEQUENCE cashreg.seq_cashreg_delivery_history_id
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;

CREATE TABLE IF NOT EXISTS cashreg.cashreg_delivery_history
(
  id               BIGINT NOT NULL DEFAULT nextval('cashreg.seq_cashreg_delivery_history_id' :: REGCLASS),
  original_id      BIGINT NOT NULL,
  invoice_id       BIGINT NOT NULL REFERENCES cashreg.invoice_payer (id),
  payment_id       BIGINT NOT NULL REFERENCES cashreg.payment (id),
  refund_id        BIGINT NULL REFERENCES cashreg.refund (id),
  type_operation   VARCHAR(21),
  request_id       VARCHAR(255),
  cashreg_status   VARCHAR(255),
  cashreg_uuid     VARCHAR(255),
  cashreg_response TEXT,
  command          VARCHAR(1),
  timestamp        timestamp       DEFAULT now(),
  CONSTRAINT cashreg_delivery_history_pkey PRIMARY KEY (id)
);


-- FUNCTION function_copy_cashreg_delivery - BEGIN
CREATE OR REPLACE FUNCTION cashreg.function_copy_cashreg_delivery()
  RETURNS TRIGGER AS
$BODY$
BEGIN

  IF (TG_OP = 'DELETE')
  THEN
    INSERT INTO cashreg.cashreg_delivery_history (original_id, invoice_id, payment_id, refund_id,
                                                  type_operation, request_id, cashreg_status, cashreg_uuid,
                                                  cashreg_response, command)
    VALUES (old.id, old.invoice_id, old.payment_id, old.refund_id,
            old.type_operation, old.request_id, old.cashreg_status, old.cashreg_uuid, old.cashreg_response, 'D');
    RETURN old;
  ELSIF (TG_OP = 'UPDATE')
  THEN
    INSERT INTO cashreg.cashreg_delivery_history (original_id, invoice_id, payment_id, refund_id,
                                                  type_operation, request_id, cashreg_status, cashreg_uuid,
                                                  cashreg_response, command)
    VALUES (new.id, new.invoice_id, new.payment_id, new.refund_id,
            new.type_operation, new.request_id, new.cashreg_status, new.cashreg_uuid, new.cashreg_response, 'U');
    RETURN new;
  ELSIF (TG_OP = 'INSERT')
  THEN
    INSERT INTO cashreg.cashreg_delivery_history (original_id, invoice_id, payment_id, refund_id,
                                                  type_operation, request_id, cashreg_status, cashreg_uuid,
                                                  cashreg_response, command)
    VALUES (new.id, new.invoice_id, new.payment_id, new.refund_id,
            new.type_operation, new.request_id, new.cashreg_status, new.cashreg_uuid, new.cashreg_response, 'I');
    RETURN new;
  END IF;
  RETURN NULL;
END;
$BODY$
  language plpgsql;

CREATE TRIGGER trigger_cashreg_delivery
  AFTER INSERT OR UPDATE OR DELETE
  ON cashreg.cashreg_delivery
  FOR EACH ROW
EXECUTE PROCEDURE cashreg.function_copy_cashreg_delivery();
