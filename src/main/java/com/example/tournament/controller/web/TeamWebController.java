package com.example.tournament.controller.web;

import com.example.tournament.dto.request.PlayerForm;
import com.example.tournament.dto.request.TeamForm;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.service.TeamService;
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

@Controller
@RequestMapping("/teams")
public class TeamWebController {

    private final TeamService teamService;

    public TeamWebController(TeamService teamService) {
        this.teamService = teamService;
    }

    @ModelAttribute("allStatuses")
    public TeamStatus[] allStatuses() {
        return TeamStatus.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) TeamStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("page",
                teamService.search(search, status,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))));
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("pageSize", size);
        return "team/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("form", new TeamForm());
        model.addAttribute("mode", "create");
        return "team/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(@ModelAttribute("form") @Valid TeamForm form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "team/form";
        }
        TeamResponse created = teamService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage",
                "Équipe « " + created.name() + " » créée.");
        return "redirect:/teams/" + created.id();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        TeamResponse team = teamService.getById(id);
        model.addAttribute("team", team);
        model.addAttribute("players", teamService.players(id));
        model.addAttribute("history", teamService.history(id));
        model.addAttribute("playerForm", new PlayerForm());
        return "team/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        TeamForm form = new TeamForm();
        form.from(teamService.getById(id));
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("teamId", id);
        return "team/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") @Valid TeamForm form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("teamId", id);
            return "team/form";
        }
        TeamResponse updated = teamService.update(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage",
                "Équipe « " + updated.name() + " » mise à jour.");
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        teamService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Équipe supprimée.");
        return "redirect:/teams";
    }

    @PostMapping("/{id}/players")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String addPlayer(@PathVariable Long id,
                            @ModelAttribute("playerForm") @Valid PlayerForm form,
                            BindingResult bindingResult, Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("team", teamService.getById(id));
            model.addAttribute("players", teamService.players(id));
            model.addAttribute("history", teamService.history(id));
            return "team/detail";
        }
        teamService.addPlayer(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Joueur ajouté.");
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/players/{playerId}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String deletePlayer(@PathVariable Long id, @PathVariable Long playerId,
                               RedirectAttributes redirectAttributes) {
        teamService.deletePlayer(playerId);
        redirectAttributes.addFlashAttribute("successMessage", "Joueur retiré.");
        return "redirect:/teams/" + id;
    }
}