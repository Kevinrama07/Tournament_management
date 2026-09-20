package com.example.tournament.controller.web;

import com.example.tournament.dto.request.MatchResultForm;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/matches")
public class MatchWebController {

    private final MatchService matchService;
    private final TournamentService tournamentService;

    public MatchWebController(MatchService matchService, TournamentService tournamentService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
    }

    @ModelAttribute("allStatuses")
    public MatchStatus[] allStatuses() {
        return MatchStatus.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long tournamentId,
                       @RequestParam(required = false) MatchStatus status,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) LocalDate from,
                       @RequestParam(required = false) LocalDate to,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        model.addAttribute("page",
                matchService.search(tournamentId, null, status, null, search, from, to,
                        PageRequest.of(page, size,
                                Sort.by(Sort.Direction.ASC, "scheduledAt"))));
        model.addAttribute("tournaments", tournamentService.search(null, null, null,
                PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "name"))).getContent());
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("pageSize", size);
        return "match/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        MatchResponse match = matchService.getById(id);
        model.addAttribute("match", match);
        model.addAttribute("form", new MatchResultForm());
        return "match/detail";
    }

    @PostMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String recordResult(@PathVariable Long id,
                               @ModelAttribute("form") @Valid MatchResultForm form,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("match", matchService.getById(id));
            return "match/detail";
        }
        MatchResponse updated = matchService.recordResult(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage",
                "Résultat enregistré pour le match #" + id + ".");
        return "redirect:/matches/" + updated.id();
    }
}