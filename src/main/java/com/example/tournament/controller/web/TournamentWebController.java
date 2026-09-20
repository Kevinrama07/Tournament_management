package com.example.tournament.controller.web;

import com.example.tournament.dto.request.TournamentForm;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.service.MatchGenerationService;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingAndBracketService;
import com.example.tournament.service.TournamentService;
import com.example.tournament.service.TournamentTeamService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tournaments")
public class TournamentWebController {

    private final TournamentService tournamentService;
    private final TournamentTeamService tournamentTeamService;
    private final MatchGenerationService matchGenerationService;
    private final MatchService matchService;
    private final RankingAndBracketService rankingAndBracketService;

    public TournamentWebController(TournamentService tournamentService,
                                   TournamentTeamService tournamentTeamService,
                                   MatchGenerationService matchGenerationService,
                                   MatchService matchService,
                                   RankingAndBracketService rankingAndBracketService) {
        this.tournamentService = tournamentService;
        this.tournamentTeamService = tournamentTeamService;
        this.matchGenerationService = matchGenerationService;
        this.matchService = matchService;
        this.rankingAndBracketService = rankingAndBracketService;
    }

    @ModelAttribute("allStatuses")
    public TournamentStatus[] allStatuses() {
        return TournamentStatus.values();
    }

    @ModelAttribute("allTypes")
    public TournamentType[] allTypes() {
        return TournamentType.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) TournamentStatus status,
                       @RequestParam(required = false) TournamentType type,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("page",
                tournamentService.search(search, status, type,
                        org.springframework.data.domain.PageRequest.of(page, size,
                                org.springframework.data.domain.Sort.by(
                                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("pageSize", size);
        return "tournament/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("form", new TournamentForm());
        model.addAttribute("mode", "create");
        return "tournament/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(@ModelAttribute("form") @Valid TournamentForm form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "tournament/form";
        }
        TournamentResponse created = tournamentService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage",
                "Tournoi « " + created.name() + " » créé.");
        return "redirect:/tournaments/" + created.id();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String tab,
                         Model model) {
        TournamentResponse tournament = tournamentService.getById(id);
        model.addAttribute("tournament", tournament);
        model.addAttribute("registrations", tournamentTeamService.listRegistrations(id));
        model.addAttribute("availableTeams", tournamentTeamService.availableTeams(id));
        java.util.List<com.example.tournament.dto.response.MatchResponse> matchList =
                matchService.listByTournament(id);
        model.addAttribute("matches", matchList);

        java.util.LinkedHashMap<Integer, java.util.List<com.example.tournament.dto.response.MatchResponse>>
                byRound = new java.util.LinkedHashMap<>();
        for (com.example.tournament.dto.response.MatchResponse m : matchList) {
            byRound.computeIfAbsent(m.roundNumber(), k -> new java.util.ArrayList<>()).add(m);
        }
        model.addAttribute("matchesByRound", byRound);
        model.addAttribute("stats", rankingAndBracketService.stats(id));
        model.addAttribute("activeTab", tab == null ? "overview" : tab);
        if (tournament.type() == TournamentType.ELIMINATION_DIRECTE) {
            model.addAttribute("bracket", rankingAndBracketService.bracket(id));
        } else {
            model.addAttribute("ranking", rankingAndBracketService.ranking(id, null));
        }
        return "tournament/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        TournamentResponse tournament = tournamentService.getById(id);
        TournamentForm form = new TournamentForm();
        form.setName(tournament.name());
        form.setSport(tournament.sport());
        form.setDescription(tournament.description());
        form.setStartDate(tournament.startDate());
        form.setEndDate(tournament.endDate());
        form.setLocation(tournament.location());
        form.setMaxTeams(tournament.maxTeams());
        form.setType(tournament.type());
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("tournamentId", id);
        return "tournament/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") @Valid TournamentForm form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("tournamentId", id);
            return "tournament/form";
        }
        TournamentResponse updated = tournamentService.update(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage",
                "Tournoi « " + updated.name() + " » mis à jour.");
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tournamentService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Tournoi supprimé.");
        return "redirect:/tournaments";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String changeStatus(@PathVariable Long id, @RequestParam TournamentStatus status,
                               RedirectAttributes redirectAttributes) {
        TournamentResponse updated = tournamentService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "Statut mis à jour : " + updated.status());
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String start(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tournamentService.start(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Le tournoi est maintenant EN_COURS.");
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String generate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        matchGenerationService.generate(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Calendrier généré avec succès.");
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/teams")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String registerTeam(@PathVariable Long id, @RequestParam Long teamId,
                               RedirectAttributes redirectAttributes) {
        tournamentTeamService.register(id, teamId);
        redirectAttributes.addFlashAttribute("successMessage", "Équipe inscrite.");
        return "redirect:/tournaments/" + id + "?tab=teams";
    }

    @PostMapping("/{id}/teams/{teamId}/remove")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String unregisterTeam(@PathVariable Long id, @PathVariable Long teamId,
                                 RedirectAttributes redirectAttributes) {
        tournamentTeamService.unregister(id, teamId);
        redirectAttributes.addFlashAttribute("successMessage", "Équipe retirée.");
        return "redirect:/tournaments/" + id + "?tab=teams";
    }

    @PostMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String recordResult(@PathVariable Long id,
                               @RequestParam Long matchId,
                               @RequestParam Integer homeScore,
                               @RequestParam Integer awayScore,
                               RedirectAttributes redirectAttributes) {
        matchService.recordResult(matchId,
                new com.example.tournament.dto.request.MatchResultRequest(homeScore, awayScore));
        redirectAttributes.addFlashAttribute("successMessage", "Résultat enregistré.");
        return "redirect:/tournaments/" + id + "?tab=matches";
    }
}