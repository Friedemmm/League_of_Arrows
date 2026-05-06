package com.backend.LeagueOfArrows.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final JdbcTemplate jdbc;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Map<String, Object>> logs = jdbc.queryForList("""
            SELECT
                al.id_audit        AS "idAudit",
                al.id_archer       AS "idArcher",
                ar.name            AS "archerName",
                al.id_tournament   AS "idTournament",
                t.name             AS "tournamentName",
                al.old_score       AS "oldScore",
                al.new_score       AS "newScore",
                al.modified_by     AS "modifiedBy",
                al.modified_at     AS "modifiedAt"
            FROM audit_log al
            LEFT JOIN archers     ar ON ar.id_archer     = al.id_archer
            LEFT JOIN tournaments t  ON t.id_tournament  = al.id_tournament
            ORDER BY al.id_audit DESC
            """);
        return ResponseEntity.ok(logs);
    }
}
