package boets.be.nbts.leagues.eventhandlers;

import boets.be.nbts.leagues.domain.models.LeagueSavedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeagueEventLeagueHandler {

    /**
     * Handle a LeagueSavedEvent.  
     * @param event
     */
    void handle(LeagueSavedEvent event) {
        log.info("[League]: Received event for new league {}", event.leagueId());

    }
}
