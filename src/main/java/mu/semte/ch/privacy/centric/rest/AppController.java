package mu.semte.ch.privacy.centric.rest;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.privacy.centric.service.RequestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;


@RestController
@Slf4j
public class AppController {

  private final RequestService requestService;

  public AppController(RequestService requestService) {
    this.requestService = requestService;
  }

  @PostMapping(value= "/person-information-updates", consumes = "application/vnd.api+json")
  public ResponseEntity<Void> personInformationUpdates(@RequestBody String payload, HttpServletRequest request){
        requestService.processUpdate(payload, getSessionIdHeader(request));
        return ResponseEntity.noContent().build();
  }

  @PostMapping(value="/person-information-requests", consumes = "application/vnd.api+json", produces = "application/vnd.api+json")
  public ResponseEntity<String> personInformationRequests(@RequestBody String payload, HttpServletRequest request){
    return ResponseEntity.ok(requestService.processRead(payload, getSessionIdHeader(request)));
  }
  private String getSessionIdHeader(HttpServletRequest request){
    String sessionIdHeader = request.getHeader("MU-SESSION-ID");
    if(StringUtils.isEmpty(sessionIdHeader)){
      throw new RuntimeException("Session header is missing");
    }
    return sessionIdHeader;
  }
}
