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
import org.apache.commons.text.CaseUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class RequestService {
  String graphReason = "http://whatever.com/graphReason"; // todo move it as a property
  String graph = "http://mu.semte.ch/graphs/contacthub/141d9d6b-54af-4d17-b313-8d1c30bc3f5b/ChAdmin"; // todo move it as a property

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
    var queryKey = CaseUtils.toCamelCase(data.getProperty(), false, '-');
    var queryName = "update".concat(StringUtils.capitalize(queryKey));
    var field = configWrapper.getField(queryKey, data.getType())
                             .orElseThrow(() -> new RuntimeException("Field not configured yet"));
    saveReason(data, field, "update");


    var query = queryStore.getQueryWithParameters(queryName, Map.of("dataJson",data.getData(), "graph", graph));
    log.info(query);
    sparqlClient.executeUpdateQuery(query);

    return null;
  }

  private void saveReason(ApiRequest data, FieldConfig config, String operation) {
    Map<String, Object> queryParameters = Map.of(
            "graphReason", graphReason,
            "dataJson", data.getData(),
            "nsType", config.getNsType(),
            "operation", operation,
            "property", config.getProperty()
    );
    var query = queryStore.getQueryWithParameters("saveReason", queryParameters);
    log.info(query);
    sparqlClient.executeUpdateQuery(query);
  }

  private ApiResponse readRequest(ApiRequest apiRequest) {
    var queryKey = CaseUtils.toCamelCase(apiRequest.getProperty(), false, '-');
    var queryName = "read".concat(StringUtils.capitalize(queryKey));
    var field = configWrapper.getField(queryKey, apiRequest.getType())
                             .orElseThrow(() -> new RuntimeException("Field not configured yet"));
    saveReason(apiRequest, field, "read");

    Map<String, Object> queryParameters = new HashMap<>(Map.of("dataJson",apiRequest.getData()));
    queryParameters.put("graph", graph);
    var query = queryStore.getQueryWithParameters(queryName, queryParameters);
    log.info(query);
    return ApiResponse.builder().data(sparqlClient.executeSelectQueryAsListMap(query)).build();
  }


}
