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
    "terms_accepted_at"
) VALUES (
    1, 
    'db50feb4-85d6-46cb-bfe7-b1ca30f43e10', 
    true, 
    false, 
    'test.seller@opertusmundi.eu', 
    true, 
    '2021-01-08 11:20:26.941', 
    'Test', 
    'Seller', 
    'en', 
    '2021-01-08 11:18:02.502', 
    'COMPLETED', 
    '2021-01-08 11:20:26.941', 
    NULL, 
    true, 
    '2021-01-08 11:18:02.502'
);

INSERT INTO "web"."customer" (
    "id", 
    "draft_key", 
    "account", 
    "type", 
    "payment_provider_user", 
    "payment_provider_wallet", 
    "kyc_level", 
    "email", 
    "email_verified", 
    "email_verified_at", 
    "terms_accepted", 
    "terms_accepted_at", 
    "created_at", 
    "modified_at"
) VALUES (
    1, 
    '68869aaa-1f96-414b-b7f8-ef2028ddf04a', 
    1, 
    2, 
    '1', 
    '1', 
    'LIGHT', 
    'test.seller@opertusmundi.eu', 
    true, 
    '2021-01-08 11:20:55.055', 
    true, 
    '2021-01-08 11:20:55.055', 
    '2021-01-08 11:20:55.055', 
    '2021-01-08 11:20:55.055'
);

INSERT INTO "web"."customer_professional" (
    "id", 
    "headquarters_address_line1", 
    "headquarters_address_city", 
    "headquarters_address_region", 
    "headquarters_address_postal_code", 
    "headquarters_address_country", 
    "legal_person_type", 
    "name", 
    "legal_representative_address_line1", 
    "legal_representative_address_city", 
    "legal_representative_address_region", 
    "legal_representative_address_postal_code", 
    "legal_representative_address_country", 
    "legal_representative_birthdate", 
    "legal_representative_country_of_residence", 
    "legal_representative_nationality", 
    "legal_representative_email", 
    "legal_representative_first_name", 
    "legal_representative_last_name", 
    "company_number", 
    "bank_account_owner_name", 
    "bank_account_owner_address_line1", 
    "bank_account_owner_address_city", 
    "bank_account_owner_address_region", 
    "bank_account_owner_address_postal_code", 
    "bank_account_owner_address_country", 
    "bank_account_iban", 
    "bank_account_bic", 
    "additional_info", 
    "company_type", 
    "payment_provider_bank_account", 
    "rating_count", 
    "rating_total"
) VALUES (
    1, 
    'Address 1',
    'City', 
    'Region', 
    '10000', 
    'GR', 
    'BUSINESS', 
    'Company', 
    'Address 1', 
    'City', 
    'Region', 
    '10000', 
    'GR', 
    '1980-01-01 02:00:00', 
    'GR', 
    'GR', 
    'test.seller@opertusmundi.eu', 
    'Test', 
    'Seler', 
    'EL000000000', 
    'Seller Account Owner', 
    'Address 1',
    'City', 
    'Region', 
    '10000', 
    'GR', 
    'EL0000000000000000000000000',
    'ABCDEFGH', 
    'Additional information', 
    'Software', 
    '1',
    0,
    0
);

INSERT INTO "web"."account_profile" (
    "id", "account", "provider", 
    "mobile", 
    "created_at", 
    "modified_at"
) VALUES (
    1, 1, 1,
    '+306900000000', 
    '2021-01-08 11:18:02.502', 
    '2021-01-08 11:18:02.502'
);

INSERT INTO "web"."account_role" (
	"id", "role", "account", "granted_at", "granted_by"
) VALUES (
	1, 'ROLE_USER', 1, '2021-01-08 11:18:02.671427', NULL
);
INSERT INTO "web"."account_role" (
	"id", "role", "account", "granted_at", "granted_by"
) VALUES (
	2, 'ROLE_PROVIDER', 1, '2021-01-08 11:20:51.894662', NULL
);
