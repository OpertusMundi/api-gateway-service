CREATE EXTENSION IF NOT EXISTS pgcrypto;

DELETE FROM contract.provider_contract_history;
DELETE FROM contract.master_contract_history;

DELETE FROM "admin"."account";

INSERT INTO "admin"."account" (
  "id", "key", "active", "blocked", "email", "email_verified", "email_verified_on", 
  "firstname", "lastname", "locale", "password", "created_on", "modified_on"
) VALUES (
    1, uuid_generate_v4(), true, false, 'helpdesk@opertusmundi.eu', true, now(),
    'John', 'Doe', 'en', crypt('test', gen_salt('bf', 8)), now(), now()
);

INSERT INTO "admin"."account_role" (
	"id", "role", "account", "granted_at", "granted_by"
) VALUES (
	1, 'ADMIN', 1, now(), NULL
);
INSERT INTO "admin"."account_role" (
	"id", "role", "account", "granted_at", "granted_by"
) VALUES (
	2, 'USER', 1, now(), NULL
);
