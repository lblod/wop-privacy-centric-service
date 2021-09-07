package mu.semte.ch.privacy.centric.rest;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.privacy.centric.jsonapi.ApiRequest;
import mu.semte.ch.privacy.centric.jsonapi.ApiResponse;
import mu.semte.ch.privacy.centric.service.RequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<ApiResponse> app(@RequestBody ApiRequest request) {
    var response = requestService.processRequest(request);
    return ResponseEntity.ok(response);
  }
  @GetMapping(value = "/reasons",
              produces = "application/ld+json")
  public ResponseEntity<String> reasons(Pageable pageable) {
    return ResponseEntity.ok(requestService.getReasons(pageable));
  }

}
