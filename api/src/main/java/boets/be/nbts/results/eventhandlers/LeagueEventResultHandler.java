package boets.be.nbts.results.eventhandlers;

import boets.be.nbts.leagues.domain.models.LeagueDeletedEvent;
import boets.be.nbts.leagues.domain.models.LeagueSavedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeagueEventResultHandler {


    @ApplicationModuleListener
    void handleNewLeague(LeagueSavedEvent event) {
        log.info("[Result]: Received event for new league {}", event.leagueId());
    }

    @ApplicationModuleListener
    void handleRemovedLeague(LeagueDeletedEvent event) {
        log.info("[Result]: Received event for removed league {}", event.leagueId());
    }
}
