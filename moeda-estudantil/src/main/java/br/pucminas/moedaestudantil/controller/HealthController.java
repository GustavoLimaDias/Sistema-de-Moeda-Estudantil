package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.security.Publico;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Health check do Render e "despertador" do plano free (não acessa o banco). */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Publico
    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
