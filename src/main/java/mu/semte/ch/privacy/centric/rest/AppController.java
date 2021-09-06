package mu.semte.ch.privacy.centric.rest;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.privacy.centric.jsonapi.ApiRequest;
import mu.semte.ch.privacy.centric.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class AppController {
  private final RequestService requestService;

  public AppController(RequestService requestService) {
    this.requestService = requestService;
  }

  @PostMapping
  public ResponseEntity<Void> app(@RequestBody ApiRequest request) {
    requestService.processRequest(request);
    return ResponseEntity.ok().build();
  }

}
