package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MatchSpecifications {

    private MatchSpecifications() {
    }

    public static Specification<Match> filter(Long tournamentId, Long teamId, MatchStatus status,
                                              MatchPhase phase, String search,
                                              LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tournamentId != null) {
                predicates.add(cb.equal(root.get("tournament").get("id"), tournamentId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (phase != null) {
                predicates.add(cb.equal(root.get("phase"), phase));
            }
            if (teamId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("homeTeam").get("id"), teamId),
                        cb.equal(root.get("awayTeam").get("id"), teamId)
                ));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                Join<Object, Object> home = root.join("homeTeam", JoinType.LEFT);
                Join<Object, Object> away = root.join("awayTeam", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(home.get("name")), like),
                        cb.like(cb.lower(away.get("name")), like),
                        cb.like(cb.lower(root.get("venue")), like)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt").as(LocalDate.class), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt").as(LocalDate.class), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}