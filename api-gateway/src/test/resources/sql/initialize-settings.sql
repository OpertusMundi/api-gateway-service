DELETE FROM web.settings;

INSERT INTO web.settings (service, "key", "type", "value", updated_on, read_only) VALUES
('API_GATEWAY',   'announcement.text',    'HTML',    '',    now(), true),
('API_GATEWAY',   'announcement.enabled', 'BOOLEAN', false, now(), true),
('BPM_WORKER',    'topio-account-id',     'NUMERIC', '1',   now(), true),
('BPM_WORKER',    'topio-fee-percent',    'NUMERIC', '5',   now(), true),
('ADMIN_GATEWAY', 'user-service.pricing-model.price', 'JSON',    
 '{"type":"PER_CALL","price":0.05,"prePaidTiers":[],"discountRates":[{"count":10000,"discount":5.0}]}', now(), false
);
