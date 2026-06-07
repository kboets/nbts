DELETE FROM leagues.country;
INSERT INTO leagues.country (id, country_code, name_nl, name_en, flag_url, region)
VALUES (1, 'BE', 'Belgie','Belgium', 'http:www.flagurl.be', 'Europe');

INSERT INTO leagues.country (id, country_code, name_nl, name_en, flag_url, region)
VALUES (2, 'NL', 'Nederland','Netherlands',  'http:www.flagurl.nl', 'Europe');

INSERT INTO leagues.country (id, country_code, name_nl, name_en, flag_url, region)
VALUES (3, 'BRA', 'Brazilie','Brazil',  'http:www.flagurl.bra', 'South America');

DELETE FROM leagues.league;

INSERT INTO leagues.league(id, name, league_id, country_code, start_season, end_season, season, logo,current)
VALUES (1, 'Premier League', 1, 'UK', '2025-08-01', '2026-05-23', 2025, 'http:www.premierleague.uk', true);

INSERT INTO leagues.league(id, name, league_id, country_code, start_season, end_season, season, logo,current)
VALUES (2, 'La Liga', 2, 'ES', '2025-08-10', '2026-05-28', 2025, 'http:www.premierleague.es', true);

INSERT INTO leagues.league(id, name, league_id, country_code, start_season, end_season, season, logo,current)
VALUES (3, 'La Liga', 3, 'ES', '2024-08-14', '2025-05-26', 2024, 'http:www.premierleague.es', false);

--commit;
