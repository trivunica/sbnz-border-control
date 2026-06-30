INSERT INTO interpol_warrant (document_number, full_name, date_of_birth, nationality, reason, issued_at, expires_at, active)
VALUES
    ('SRB123456',  'Petar Petrović',    '1978-03-15', 'SRB', 'Međunarodni nalog za hapšenje - krijumčarenje', '2024-01-10', '2026-12-31', TRUE),
    ('BIH987654',  'Marko Marković',    '1985-07-22', 'BIH', 'Poternica Interpola', '2023-06-01', '2027-06-01', TRUE),
    ('DEU445566',  'Hans Mueller',      '1970-11-05', 'DEU', 'Organizovani kriminal', '2025-02-20', NULL,TRUE),
    ('OLD000001',  'Već Rešen Slučaj',  '1960-01-01', 'SRB', 'Zatvoren predmet', '2020-01-01', '2023-01-01', FALSE);


INSERT INTO domestic_warrant (document_number, full_name, date_of_birth, nationality, reason, issuing_authority, issued_at, expires_at, active)
VALUES
    ('SRB111222',  'Nikola Nikolić',    '1990-05-10', 'SRB', 'Nalog za hapšenje - osnovi sumnje za krivično delo', 'Osnovno tužilaštvo Beograd', '2025-03-01', '2027-03-01', TRUE),
    ('MKD334455',  'Dragan Dragović',   '1982-09-18', 'MKD', 'Potraga MUP RS', 'MUP RS - Odeljenje kriminaliteta', '2024-11-15', NULL,          TRUE),
    ('SRB777888',  'Jovana Jovanović',  '1995-12-01', 'SRB', 'Nalog za hapšenje - prevara', 'Viši sud Novi Sad',            '2026-01-05', '2028-01-05', TRUE),
    ('SRB000999',  'Stari Predmet',     '1955-04-04', 'SRB', 'Predmet zatvoren', 'MUP RS',                         '2019-01-01', '2021-01-01', FALSE);


INSERT INTO stolen_lost_document (document_number, document_type, full_name, reported_by, reported_at, reason, active)
VALUES
    ('PA001234',   'PASSPORT', 'Stefan Stefanović',  'MUP RS - PS Zemun',         '2025-08-10', 'STOLEN', TRUE),
    ('LK556677',   'ID_CARD',  'Milena Milovanović', 'Građanin - lična prijava',   '2026-01-22', 'LOST',   TRUE),
    ('PA998877',   'PASSPORT', 'Aleksandar Aleksić', 'Ambasada RS u Berlinu',      '2024-05-05', 'STOLEN', TRUE),
    ('LK000111',   'ID_CARD',  'Pronađen Dokument',  'MUP RS',                     '2023-01-01', 'LOST',   FALSE);
