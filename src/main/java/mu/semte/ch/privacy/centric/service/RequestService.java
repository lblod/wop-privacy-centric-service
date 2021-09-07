package mu.semte.ch.privacy.centric.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.privacy.centric.config.FieldConfig;
import mu.semte.ch.privacy.centric.config.FieldConfigWrapper;
import mu.semte.ch.privacy.centric.jsonapi.ApiRequest;
import mu.semte.ch.privacy.centric.jsonapi.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class RequestService {
  @Value("${sparql.reasonGraphUri}")
  private String graphReason;
  @Value("${sparql.defaultGraphUri}")
  private String graph;

  private final FieldConfigWrapper configWrapper;
  private final SparqlQueryStore queryStore;
  private final SparqlClient sparqlClient;

  public RequestService(FieldConfigWrapper configWrapper,
                        SparqlQueryStore queryStore,
                        SparqlClient sparqlClient) {
    this.configWrapper = configWrapper;
    this.queryStore = queryStore;
    this.sparqlClient = sparqlClient;
  }

  public ApiResponse processRequest(ApiRequest request) {
    return switch (request.getOperation().toLowerCase()) {
      case "read" -> readRequest(request);
      case "update" -> updateRequest(request);
      default -> throw new RuntimeException("unknown operation");
    };
  }

  private ApiResponse updateRequest(ApiRequest data) {
    var queryName = "update".concat(StringUtils.capitalize(data.getProperty()));
    var field = configWrapper.getField(data.getProperty(), data.getType())
                             .orElseThrow(() -> new RuntimeException("Field not configured yet"));
    saveReason(data, field, "update");


    var query = queryStore.getQueryWithParameters(queryName, Map.of("dataJson",data.getData(), "graph", graph));
    sparqlClient.executeUpdateQuery(query);

    return null;
  }

  private void saveReason(ApiRequest data, FieldConfig config, String operation) {
    Map<String, Object> queryParameters = Map.of(
            "graphReason", graphReason,
            "dataJson", data.getData(),
            "nsType", config.getNsType(),
            "operation", operation,
            "time", ModelUtils.formattedDate(LocalDateTime.now()),
            "property", config.getProperty()
    );
    var query = queryStore.getQueryWithParameters("saveReason", queryParameters);
    sparqlClient.executeUpdateQuery(query);
  }

  private ApiResponse readRequest(ApiRequest apiRequest) {
    var queryName = "read".concat(StringUtils.capitalize(apiRequest.getProperty()));
    var field = configWrapper.getField(apiRequest.getProperty(), apiRequest.getType())
                             .orElseThrow(() -> new RuntimeException("Field not configured yet"));
    saveReason(apiRequest, field, "read");

    Map<String, Object> queryParameters = new HashMap<>(Map.of("dataJson",apiRequest.getData()));
    queryParameters.put("graph", graph);
    var query = queryStore.getQueryWithParameters(queryName, queryParameters);
    return ApiResponse.builder().data(sparqlClient.executeSelectQueryAsListMap(query)).build();
  }

  public String getReasons(Pageable page) {
    //int pageSize = page.getPageSize();
    //long offset = page.getOffset();
    var query = queryStore.getQueryWithParameters("getReasons", Map.of(
            "graphReason", graphReason
            //"limitSize", pageSize,
            //"offsetNumber", offset

    ));
    log.info(query);
    var model = sparqlClient.executeSelectQuery(query);
    return ModelUtils.toString(model, Lang.JSONLD);
  }


}
