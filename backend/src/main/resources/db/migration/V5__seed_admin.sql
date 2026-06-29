-- Admin user: admin@marketplace.local / Admin1234
-- BCrypt hash for Admin1234 (strength 12)
INSERT INTO users (id, email, password_hash, full_name, status, email_verified, created_at, updated_at)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'admin@marketplace.local',
    '$2a$12$DIYHar7fyxmduzD2DFprbOXhOBbO5umPf..UXaBiZyj4Z6s.Gv9.G',
    'Platform Admin',
    'active',
    TRUE,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ADMIN')
ON CONFLICT DO NOTHING;
