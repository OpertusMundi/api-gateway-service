DELETE FROM contract.provider_contract_history;
DELETE FROM contract.master_contract_history;

DELETE FROM "web"."account";

INSERT INTO "web"."account" (
    "id",
    "key",
    "active",
    "blocked",
    "email",
    "email_verified",
    "email_verified_at",
    "firstname",
    "lastname",
    "locale",
    "registered_at",
    "activation_status",
    "activation_at",
    "idp_name",
    "terms_accepted",
    "terms_accepted_at",
    "type"
) VALUES (
    2,
    'db50feb4-85d6-46cb-bfe7-b1ca30f43e10',
    true,
    false,
    'user@opertusmundi.eu',
    true,
    '2021-01-08 11:20:26.941',
    'Demo',
    'User',
    'en',
    '2021-01-08 11:18:02.502',
    'COMPLETED',
    '2021-01-08 11:20:26.941',
    NULL,
    true,
    '2021-01-08 11:18:02.502',
    'OPERTUSMUNDI'
);

INSERT INTO "web"."account_profile" (
    "id", 
    "account",
    "mobile",
    "created_at",
    "modified_at"
) VALUES (
    2, 
    2,
    '+306900000000',
    '2021-01-08 11:18:02.502',
    '2021-01-08 11:18:02.502'
);

INSERT INTO "web"."account_role" (
	"id", "role", "account", "granted_at", "granted_by"
) VALUES (
	1, 'ROLE_USER', 2, '2021-01-08 11:18:02.671427', NULL
);

