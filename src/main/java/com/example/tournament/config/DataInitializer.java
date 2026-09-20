package com.example.tournament.config;

import com.example.tournament.entity.AppUser;
import com.example.tournament.entity.Player;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.repository.AppUserRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.security.AppRole;
import com.example.tournament.service.MatchGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

/**
 * Initialise des données de démonstration lorsqu'aucun utilisateur n'existe.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(AppUserRepository userRepository,
                               TeamRepository teamRepository,
                               TournamentRepository tournamentRepository,
                               MatchGenerationService matchGenerationService,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.demo-data.enabled:true}") boolean enabled,
                               @Value("${app.demo-data.password:password}") String password) {
        return args -> {
            if (!enabled || userRepository.count() > 0) {
                return;
            }
            log.info("Initialisation des données de démonstration...");

            createUsers(userRepository, passwordEncoder, password);
            List<Team> teams = createTeams(teamRepository);

            Tournament championship = createTournament(tournamentRepository,
                    "Trophée Printemps 2026", "Football", "Championnat aller-retour de printemps",
                    LocalDate.now().plusWeeks(2), LocalDate.now().plusMonths(3), "Stade Municipal",
                    12, TournamentType.CHAMPIONNAT_ALLER_RETOUR, TournamentStatus.INSCRIPTION);
            register(tournamentRepository, championship, teams.subList(0, 8));

            Tournament knockout = createTournament(tournamentRepository,
                    "Coupe des Régions", "Football", "Élimination directe, huitièmes jusqu'à la finale",
                    LocalDate.now().plusWeeks(4), LocalDate.now().plusMonths(4), "Complexe Omnisports",
                    16, TournamentType.ELIMINATION_DIRECTE, TournamentStatus.PROGRAMME);
            register(tournamentRepository, knockout, teams);
            matchGenerationService.generate(knockout.getId());

            log.info("Données de démonstration créées. Comptes (mot de passe : {}): "
                    + "admin, manager, operator, user.", password);
        };
    }

    private void createUsers(AppUserRepository repository, PasswordEncoder encoder, String password) {
        createUser(repository, encoder, "admin", "Administrateur", AppRole.ADMIN, password);
        createUser(repository, encoder, "manager", "Gestionnaire", AppRole.MANAGER, password);
        createUser(repository, encoder, "operator", "Opérateur", AppRole.OPERATOR, password);
        createUser(repository, encoder, "user", "Utilisateur", AppRole.USER, password);
    }

    private void createUser(AppUserRepository repository, PasswordEncoder encoder, String username,
                            String displayName, AppRole role, String password) {
        if (repository.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setEnabled(true);
        repository.save(user);
    }

    private List<Team> createTeams(TeamRepository repository) {
        record Seed(String name, String city, String coach) {
        }
        List<Seed> seeds = List.of(
                new Seed("Lions de Dakar", "Dakar", "M. Diallo"),
                new Seed("Aigles de Lomé", "Lomé", "M. Kodjo"),
                new Seed("Falcons d'Oran", "Oran", "M. Benali"),
                new Seed("Tigres de Cotonou", "Cotonou", "M. Adjovi"),
                new Seed("Requins de Libreville", "Libreville", "M. Nguema"),
                new Seed("Étoiles de Kigali", "Kigali", "M. Uwimana"),
                new Seed("Tornades de Bamako", "Bamako", "M. Traoré"),
                new Seed("Gazelles de Tunis", "Tunis", "M. Hamdi"),
                new Seed("Cobras de Ouagadougou", "Ouagadougou", "M. Kaboré"),
                new Seed("Bisons de Yaoundé", "Yaoundé", "M. Mbarga")
        );
        return seeds.stream().map(seed -> {
            Team team = new Team();
            team.setName(seed.name());
            team.setCity(seed.city());
            team.setCoach(seed.coach());
            team.setLogo("/img/shield.svg");
            addPlayers(team);
            return repository.save(team);
        }).toList();
    }

    private void addPlayers(Team team) {
        String[][] players = {
                {"Sékou", "Camara", "Gardien", "1"},
                {"Ibrahima", "Sow", "Défenseur", "4"},
                {"Yannick", "Mensah", "Défenseur", "5"},
                {"Karim", "Ben Salah", "Milieu", "8"},
                {"Moussa", "Diop", "Attaquant", "9"}
        };
        for (String[] p : players) {
            Player player = new Player();
            player.setFirstName(p[0]);
            player.setLastName(p[1]);
            player.setPosition(p[2]);
            player.setShirtNumber(Integer.parseInt(p[3]));
            team.addPlayer(player);
        }
    }

    private Tournament createTournament(TournamentRepository repository, String name, String sport,
                                        String description, LocalDate start, LocalDate end,
                                        String location, int maxTeams, TournamentType type,
                                        TournamentStatus status) {
        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setSport(sport);
        tournament.setDescription(description);
        tournament.setStartDate(start);
        tournament.setEndDate(end);
        tournament.setLocation(location);
        tournament.setMaxTeams(maxTeams);
        tournament.setType(type);
        tournament.setStatus(status);
        return repository.save(tournament);
    }

    private void register(TournamentRepository repository, Tournament tournament, List<Team> teams) {
        Tournament loaded = repository.findById(tournament.getId()).orElseThrow();
        int seed = 1;
        for (Team team : teams) {
            com.example.tournament.entity.TournamentTeam registration =
                    new com.example.tournament.entity.TournamentTeam();
            registration.setRegistrationDate(java.time.LocalDateTime.now());
            registration.setSeed(seed++);
            registration.setTeam(team);
            loaded.addRegistration(registration);
        }
        repository.save(loaded);
    }
}