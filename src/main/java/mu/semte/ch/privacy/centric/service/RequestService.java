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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class RequestService {
  String graphReason = "http://whatever.com/graphReason"; // todo move it as a property
  String graph = "http://mu.semte.ch/graphs/contacthub/141d9d6b-54af-4d17-b313-8d1c30bc3f5b/ChAdmin"; // todo move it as a property

  private static final Function<Map<String,String>, Map<String,String>> CHECK_ID_OR_SET = attrs -> {
    String id = attrs.getOrDefault("id", ModelUtils.uuid());
    var stream = attrs.entrySet().stream().filter(e -> !e.getKey().equals("id"));
    return Stream.concat(Stream.of(Map.entry("id", id)), stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  };
  private static final SparqlQueryStore INTERNAL_STORE = () -> Map.of("saveReason", """
                  PREFIX mu: <http://mu.semte.ch/vocabularies/core/>
                  INSERT DATA {
                          GRAPH <${graphReason}> {
                            <http://whatever.com/reasons/${id}> a  <http://whatever.com/reason#Reason>;
                                                         mu:uuid "${id}";
                                                          <http://whatever.com/reason#username>  "${username}";
                                                          <http://whatever.com/reason#property>  "${property}";  
                                                          <http://whatever.com/reason#property>  "${property}";  
                                                          <http://whatever.com/reason#nsType>  "${nsType}";      
                                                          <http://whatever.com/reason#reasonPhrase>  "${reasonText}".    
                          }
                        }
          """);
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
    return switch (request.getOperation().toLowerCase()){
      case "read"-> readRequest(request);
      case "update" -> updateRequest(request);
      default -> throw new RuntimeException("unknown operation");
    };
  }

  private ApiResponse updateRequest(ApiRequest data) {
    var queryKey = CaseUtils.toCamelCase(data.getProperty(), false, '-');
    var queryName = "update".concat(StringUtils.capitalize(queryKey));
    var field = configWrapper.getField(queryKey, data.getType()).orElseThrow(()-> new RuntimeException("Field not configured yet"));
    saveReason(data, field, "update");

    Map<String, Object> queryParameters = new HashMap<>(CHECK_ID_OR_SET.apply(data.getAttributes()));
    queryParameters.put("graph", graph);

       queryParameters.put("relationships",data.getRelationships()
                                                                .stream()
                                                                .map(CHECK_ID_OR_SET)
                                                                .collect(Collectors.toList()));

    var query = queryStore.getQueryWithParameters(queryName, queryParameters);
    log.info(query);
    sparqlClient.executeUpdateQuery(query);

    return null;
  }

  private void saveReason(ApiRequest data, FieldConfig config, String operation) {
    var username = data.getRequester();
    var reason = data.getReason();
    var id = ModelUtils.uuid();
    Map<String, Object> queryParameters = Map.of(
        "graphReason", graphReason,
        "id", id,
        "username", username,
        "reasonText", reason,
        "nsType", config.getNsType(),
        "operation", operation,
        "property", config.getProperty()
    );
    var query = INTERNAL_STORE.getQueryWithParameters("saveReason", queryParameters);
    sparqlClient.executeUpdateQuery(query);
  }

  private ApiResponse readRequest(ApiRequest data) {
    var queryKey = CaseUtils.toCamelCase(data.getProperty(), false, '-');
    var queryName = "read".concat(StringUtils.capitalize(queryKey));
    var field = configWrapper.getField(queryKey, data.getType()).orElseThrow(()-> new RuntimeException("Field not configured yet"));
    saveReason(data, field, "read");

    Map<String, Object> queryParameters = new HashMap<>(data.getAttributes());
    var query = queryStore.getQueryWithParameters(queryName, queryParameters);
    sparqlClient.executeSelectQuery(query);
    return null;
  }


}
