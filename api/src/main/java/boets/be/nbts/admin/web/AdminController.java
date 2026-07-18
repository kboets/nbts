package boets.be.nbts.admin.web;

import boets.be.nbts.admin.domain.Version;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AdminController {

    private final Environment environment;

    @GetMapping("currentVersion")
    public ResponseEntity<Version> getCurrentVersion() {
        Version version = new Version(environment.getProperty("spring.application.version"));
        return ResponseEntity.ok(version);
    }
}
