import { sparqlEscapeUri, uuid, sparqlEscapeString } from "mu";
const PREFIXES = `
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX mu: <http://mu.semte.ch/vocabularies/core/>
PREFIX persoon: <https://data.vlaanderen.be/ns/persoon#>
PREFIX ext: <http://mu.semte.ch/vocabularies/ext/>
PREFIX person: <http://www.w3.org/ns/person#>
PREFIX session: <http://mu.semte.ch/vocabularies/session/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX besluit: <http://data.vlaanderen.be/ns/besluit#>
`;

export function getPrivacyGraphByAccountQuery(accountUri) {
  return `
  ${PREFIXES}
  SELECT distinct ?orgGraph ?privacyGraph
      WHERE {
        graph ?g {
           ?persoon 
                     foaf:member ?bestuurseenheid ;
                     foaf:account ${sparqlEscapeUri(accountUri)}.
            ?bestuurseenheid mu:uuid ?uuidBestuurseenheid.
         }
         graph ?orgGraph {
           ?pg a ext:PrivacyGraph;
                            mu:uuid ?uuidBestuurseenheid;
                                    ext:hasGraph ?privacyGraph.

         }
      }
`;
}
export function getAccount(sessionGraphUri, sessionId) {
  return `
    ${PREFIXES}
    SELECT ?account
    WHERE {
      GRAPH ${sparqlEscapeUri(sessionGraphUri)} {
          ${sparqlEscapeUri(sessionId)} session:account ?account.
      }
    }

  `;
}

export function getPersonInfo(_privacy_graph, appGraph, personId) {
  return `
    ${PREFIXES}
    select distinct ?dateOfBirth ?registrationNumber ?genderId where {
      graph ${sparqlEscapeUri(appGraph)} {
        ?personen a person:Person; mu:uuid ${sparqlEscapeString(personId)}.

        optional {
          ?personen persoon:geslacht ?geslacht.
          graph ?public {
             ?geslacht mu:uuid ?genderId.
          }
        }
        optional {
          ?personen persoon:heeftGeboorte ?heeftGeboorte.
          ?heeftGeboorte persoon:datum ?dateOfBirth.
        }
        optional {
          ?personen persoon:registratie ?registratie.
          ?registratie <https://data.vlaanderen.be/ns/generiek#gestructureerdeIdentificator> ?identificator.
          ?identificator <https://data.vlaanderen.be/ns/generiek#lokaleIdentificator> ?registrationNumber.
        }
      }
      
      }
    
  `;
}
export function getPersonNationalities(_privacy_graph, appGraph, personId) {
  return `
    ${PREFIXES}
    select distinct ?nationaliteitId where {
      graph ${sparqlEscapeUri(appGraph)}{
          ?personen a person:Person; mu:uuid ${sparqlEscapeString(personId)}.
          ?personen persoon:heeftNationaliteit ?nationaliteit.
        }
        graph ?public  {
             ?nationaliteit mu:uuid ?nationaliteitId.
        }
      }
    
  `;
}
export function getReasonById(reasonId) {
  return `
    ${PREFIXES}
    select ?reasonUri where {
      ?reasonUri mu:uuid ${sparqlEscapeString(reasonId)}.
    }
    
  `;
}
export function requestReadReason(graph, accountUri, personId, reasonCodeUri) {
  let now = new Date().toISOString();
  let reasonId = uuid();
  return `
    ${PREFIXES}
    INSERT DATA {
      graph ${sparqlEscapeUri(graph)} {
            <http://data.lblod.info/id/person-information-reads/${reasonId}>  a ext:PersonInformationRead;
                                                            mu:uuid "${reasonId}";
                                                            ext:date "${now}"^^xsd:dateTime;
                                                            ext:requester  ${sparqlEscapeUri(
    accountUri,
  )};
                                                            ext:person ${sparqlEscapeUri(
    `http://data.lblod.info/id/personen/${personId}`,
  )} ;
                                                            ext:code  ${sparqlEscapeUri(
    reasonCodeUri,
  )}.
    }
  }
    
  `;
}
