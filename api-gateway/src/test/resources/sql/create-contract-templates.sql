DELETE FROM contract.provider_contract_history;
DELETE FROM contract.master_contract_history;

INSERT INTO contract.master_contract_history (
	id, key, contract_root, contract_parent, owner, title, subtitle, version, created_at, modified_at, status)
	VALUES (1, uuid_generate_v4(), null, 1, 1, 'MTC', 'MTC', 1, now(), now(), 'ACTIVE');
  
INSERT INTO contract.provider_contract_history (
	id, key, template, contract_root, contract_parent, owner, title, subtitle, version, created_at, modified_at, status)
	VALUES (1, uuid_generate_v4(), 1, 1, 1, 1, 'Template', 'Template', 1, now(), now(), 'ACTIVE');
  
