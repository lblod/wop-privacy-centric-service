package mu.semte.ch.privacy.centric.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.privacy.centric.jsonapi.Gender;
import mu.semte.ch.privacy.centric.jsonapi.Nationality;
import mu.semte.ch.privacy.centric.jsonapi.PersonInformationRequest;
import mu.semte.ch.privacy.centric.jsonapi.PersonInformationUpdate;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;
import static mu.semte.ch.lib.utils.ModelUtils.formattedDate;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Service
@Slf4j
public class RequestService {
  @Value("${sparql.sessionGraphUri}")
  private String sessionGraphUri;
  @Value("${sparql.defaultGraphUri}")
  private String graph;

  private final SparqlQueryStore queryStore;
  private final SparqlClient sparqlClient;
  private final ResourceConverter resourceConverter;

  public RequestService(SparqlQueryStore queryStore,
                        SparqlClient sparqlClient, ResourceConverter resourceConverter) {
    this.queryStore = queryStore;
    this.sparqlClient = sparqlClient;
    this.resourceConverter = resourceConverter;
  }

  public void processUpdate(String payload, String sessionId) {
    var document = resourceConverter.readDocument(checkIdOrCreate(payload).getBytes(StandardCharsets.UTF_8), PersonInformationUpdate.class);
    var updateRequest = ofNullable(document.get()).orElseThrow(() -> new RuntimeException("could not parse request"));
    checkNotNull(updateRequest.getPerson(), "Person must be set!");
    checkNotNull(updateRequest.getReason(), "Reason must be set!");
    String reasonId = updateRequest.getReason().getId();
    String personId = updateRequest.getPerson().getId();
    String accountUri = getAccountBySession(sessionId);
    checkNotNull(personId, "Person id must be set!");
    checkNotNull(reasonId, "Reason must be set!");
    checkNotNull(accountUri, "Account must be set!");
    Map<String, Object> parameters = new HashMap<>();
    if (updateRequest.getNationalities() != null && !updateRequest.getNationalities().isEmpty()) {
      var nationaliteits = updateRequest.getNationalities().stream().map(nationality -> {
                                          var q = queryStore.getQueryWithParameters("getNationalityById", Map.of("nationalityId", nationality.getId()));
                                          List<Map<String, String>> results = sparqlClient.executeSelectQueryAsListMap(q);
                                          if (results.isEmpty()) {
                                            return null;
                                          }
                                          return results.get(0).get("nationality");
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
      parameters.put("nationaliteits", nationaliteits);
    }
    if (updateRequest.getGender() != null && isNotEmpty(updateRequest.getGender().getId())) {
      var q = queryStore.getQueryWithParameters("getGenderById", Map.of("genderId", updateRequest.getGender().getId()));
      List<Map<String, String>> results = sparqlClient.executeSelectQueryAsListMap(q);
      if (!results.isEmpty()) {
        String genderUri = results.get(0).get("genderUri");
        if (isNotEmpty(genderUri)) {
          parameters.put("gender", genderUri);
        }
      }
    }

    if (isNotEmpty(updateRequest.getRegistrationNumber())) {
      parameters.put("registration", updateRequest.getRegistrationNumber());
    }
    if (isNotEmpty(updateRequest.getDateOfBirth())) {
      parameters.put("dateOfBirth", updateRequest.getDateOfBirth());
    }

    if (!parameters.isEmpty()) {
      String reasonUri = getReasonUri(reasonId);
      parameters.put("personId", personId);
      parameters.put("graph", graph);
      parameters.put("time", formattedDate(LocalDateTime.now()));
      parameters.put("accountUri", accountUri);
      parameters.put("code", reasonUri);
      var q = queryStore.getQueryWithParameters("updatePerson", parameters);
      sparqlClient.executeUpdateQuery(q);
    }
  }


  @SneakyThrows
  public String processRead(String payload, String sessionId) {
    var responseBuilder = PersonInformationRequest.builder();
    var document = resourceConverter.readDocument(checkIdOrCreate(payload).getBytes(StandardCharsets.UTF_8), PersonInformationRequest.class);
    var readRequest = ofNullable(document.get()).orElseThrow(() -> new RuntimeException("could not parse request"));
    checkNotNull(readRequest.getPerson(), "Person must be set!");
    checkNotNull(readRequest.getReason(), "Reason must be set!");
    String reasonId = readRequest.getReason().getId();
    String personId = readRequest.getPerson().getId();
    String accountUri = getAccountBySession(sessionId);
    checkNotNull(personId, "Person id must be set!");
    checkNotNull(reasonId, "Reason must be set!");
    checkNotNull(accountUri, "Account must be set!");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("graph", graph);
    parameters.put("personId", personId);
    String getPersonInfo = queryStore.getQueryWithParameters("getPersonInfo", parameters);

    List<Map<String, String>> res = sparqlClient.executeSelectQueryAsListMap(getPersonInfo);
    if (!res.isEmpty()) {
      Map<String, String> mapRes = res.get(0);
      responseBuilder.dateOfBirth(mapRes.get("dateOfBirth"));
      responseBuilder.registrationNumber(mapRes.get("registrationNumber"));
      String genderId = mapRes.get("genderId");
      if (isNotEmpty(genderId)) {
        responseBuilder.gender(Gender.builder().id(genderId).build());
      }
    }

    String getPersonNationalities = queryStore.getQueryWithParameters("getPersonNationalities", parameters);
    List<Map<String, String>> nationaliteits = sparqlClient.executeSelectQueryAsListMap(getPersonNationalities);
    if (!nationaliteits.isEmpty()) {
      responseBuilder.nationalities(nationaliteits.stream()
                                                  .map(n -> n.get("nationaliteitId"))
                                                  .filter(StringUtils::isNotEmpty)
                                                  .map(n -> Nationality.builder().id(n).build())
                                                  .collect(Collectors.toList()));
    }
    responseBuilder.reason(readRequest.getReason());

    String reasonUri = getReasonUri(reasonId);
    parameters.put("code", reasonUri);
    parameters.put("time", formattedDate(LocalDateTime.now()));
    parameters.put("accountUri", accountUri);
    String requestReadReason = queryStore.getQueryWithParameters("requestReadReason", parameters);
    sparqlClient.executeUpdateQuery(requestReadReason);

    byte[] responseBytes = resourceConverter.writeDocument(new JSONAPIDocument<>(responseBuilder.build()));
    return new String(responseBytes);
  }

  private String getReasonUri(String reasonId) {
    var getReasonCodeUri = queryStore.getQueryWithParameters("getReasonById", Map.of("reasonId", reasonId));
    List<Map<String, String>> res = sparqlClient.executeSelectQueryAsListMap(getReasonCodeUri);
    checkArgument(!res.isEmpty(), "Code list not found");
    String reasonUri = res.get(0).get("reasonUri");
    checkArgument(isNotEmpty(reasonUri), "Code list not found");
    return reasonUri;
  }

  @SneakyThrows
  private String checkIdOrCreate(String jsonApiData) {
    var mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(jsonApiData);
    JsonNode data = jsonNode.get("data");
    JsonNode id = data.get("id");
    if (id == null || StringUtils.isEmpty(id.asText())) {
      ((ObjectNode) data).put("id", ModelUtils.uuid());
    }
    return jsonNode.toString();
  }

  private String getAccountBySession(String sessionId) {
    var query = queryStore.getQueryWithParameters("getAccount", Map.of(
            "sessionId", sessionId,
            "sessionGraphUri", sessionGraphUri
    ));
    return sparqlClient.executeSelectQuery(query, resultSet -> {
      if (resultSet.hasNext()) {
        QuerySolution qs = resultSet.next();
        RDFNode account = qs.get("account");
        if (account != null && account.isResource()) {
          return account.asResource().getURI();
        }
      }
      return null;
    });
  }

}
