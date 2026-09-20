package com.example.tournament.controller.web;

import com.example.tournament.entity.AppUser;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.repository.AppUserRepository;
import com.example.tournament.security.AppRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminWebController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<AppUser> users = userRepository.findAll();
        users.sort((a, b) -> a.getRole().name().compareTo(b.getRole().name()));
        model.addAttribute("users", users);
        model.addAttribute("roles", AppRole.values());
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                             @RequestParam String displayName,
                             @RequestParam AppRole role,
                             @RequestParam String password,
                             RedirectAttributes redirectAttributes) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Le nom d'utilisateur « " + username + " » existe déjà.");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Utilisateur « " + username + " » créé.");
        return "redirect:/admin/users";
    }
}