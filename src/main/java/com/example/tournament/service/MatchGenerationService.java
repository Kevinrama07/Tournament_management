package com.example.tournament.service;

import com.example.tournament.entity.Tournament;

public interface MatchGenerationService {

    /**
     * Génère le calendrier complet (championnat : toutes les journées ;
     * élimination directe : premier tour uniquement, les tours suivants sont
     * générés automatiquement à la fin de chaque tour).
     */
    void generate(Long tournamentId);

    /**
     * Appelé après la saisie d'un résultat : génère le tour suivant s'il est
     * élimination directe et que le tour courant est terminé, ou termine le
     * tournoi s'il s'agit du dernier tour.
     */
    void advanceAfterRound(Tournament tournament, int completedRound);
}