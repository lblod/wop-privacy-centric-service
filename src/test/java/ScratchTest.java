
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.SneakyThrows;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.privacy.centric.jsonapi.Gender;
import mu.semte.ch.privacy.centric.jsonapi.Nationality;
import mu.semte.ch.privacy.centric.jsonapi.Person;
import mu.semte.ch.privacy.centric.jsonapi.PersonInformationRequest;
import mu.semte.ch.privacy.centric.jsonapi.PersonInformationUpdate;
import mu.semte.ch.privacy.centric.jsonapi.RequestReason;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;


public class ScratchTest {
  String readRequest = """
          {
             "data":{
                "type":"person-information-requests",
                "relationships":{
                   "person":{
                      "data":{
                         "type":"people",
                         "id":"fd88cb5a70db029111aa23a086b86f37"
                      }
                   },
                   "reason":{
                      "data":{
                         "type":"request-reasons",
                         "id":"3aeec145-acf3-4b6e-9c00-5b8e285736e0"
                      }
                   }
                }
             }
          }
          """;
  String updateRequest = """
 {
             "data":{
                "type":"person-information-updates",
                "attributes":{
                   "date-of-birth":"2021-02-03",
                   "registration":"84.04.17-319.90"
                },
                "relationships":{
                   "person":{
                      "data":{
                         "type":"people",
                         "id":"fd88cb5a70db029111aa23a086b86f37"
                      }
                   },
                   "nationalities":
                      {
                         "data":[{
                            "type":"nationalities",
                            "id":"b461dca9e69a540cd821559f0873fe46"
                         }]
                      }
                   ,
                   "gender":{
                      "data":{
                         "type":"genders",
                         "id":"5ab0e9b8a3b2ca7c5e000028"
                      }
                   },
                   "reason":{
                      "data":{
                         "type":"request-reasons",
                         "id":"3aeec145-acf3-4b6e-9c00-5b8e285736e0"
                      }
                   }
                }
             }
          }
                   
          """;

  String readResponse = """
          {
             "data":{
                "type":"person-information-requests",
                "attributes":{
                   "date-of-birth":"2021-02-03",
                   "registration":"84.04.17-319.90"
                },
                "relationships":{
                   "person":{
                      "data":{
                         "type":"people",
                         "id":"bc4e74e1-c80f-4411-9cf7-9654b2c888c8"
                      }
                   },
                   "nationalities":{
                      "data":[
                         {
                            "type":"nationalities",
                            "id":"31ea674b-3845-4873-a15d-28bdbc6860ca"
                         }
                      ]
                   },
                   "gender":{
                      "data":{
                         "type":"genders",
                         "id":"d771f3ff-e552-40a5-bbb5-89bc56445bfc"
                      }
                   },
                   "reason":{
                      "data":{
                         "type":"request-reasons",
                         "id":"3aeec145-acf3-4b6e-9c00-5b8e285736e0"
                      }
                   }
                }
             }
          }
          """;

  @Test
  public void test(){
  ResourceConverter converter = new ResourceConverter("https://data.lblod.info",
                                                      Gender.class, Nationality.class, Person.class, PersonInformationUpdate.class,
                                                      PersonInformationRequest.class,
                                                      RequestReason.class);
  JSONAPIDocument<PersonInformationUpdate> update = converter.readDocument(checkIdOrCreate(updateRequest).getBytes(StandardCharsets.UTF_8), PersonInformationUpdate.class);
  PersonInformationUpdate piu = update.get();
   System.out.println(piu);

   JSONAPIDocument<PersonInformationRequest> read = converter.readDocument(checkIdOrCreate(readRequest).getBytes(StandardCharsets.UTF_8), PersonInformationRequest.class);
   PersonInformationRequest pir = read.get();
   System.out.println(pir);
   JSONAPIDocument<PersonInformationRequest> readResp = converter.readDocument(checkIdOrCreate(readResponse).getBytes(StandardCharsets.UTF_8), PersonInformationRequest.class);
   PersonInformationRequest pir2 = readResp.get();
   System.out.println(pir2);
  }

  @SneakyThrows
  private String checkIdOrCreate(String jsonApiData){
    var mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(jsonApiData);
    JsonNode data = jsonNode.get("data");
    JsonNode id = data.get("id");
    if(id == null || StringUtils.isEmpty(id.asText())) {
      ((ObjectNode)data).put("id", ModelUtils.uuid());
    }
    return jsonNode.toString();
  }
}
