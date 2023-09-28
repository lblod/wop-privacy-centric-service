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
           ?privacyGraph a ext:PrivacyGraph;
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

export function getPersonInfo(graph, appGraph, personId) {
  return `
    ${PREFIXES}
    select ?dateOfBirth ?registrationNumber ?genderId where {
      graph ${sparqlEscapeUri(graph)} {
        optional {
           ${sparqlEscapeUri(
    `http://data.lblod.info/id/personen/${personId}`,
  )} persoon:geslacht ?geslacht.
          graph ${sparqlEscapeUri(appGraph)} {
             ?geslacht mu:uuid ?genderId.
          }
        }
        optional {
           ${sparqlEscapeUri(
    `http://data.lblod.info/id/personen/${personId}`,
  )} persoon:heeftGeboorte ?heeftGeboorte.
          ?heeftGeboorte persoon:datum ?dateOfBirth.
        }
        optional {
           ${sparqlEscapeUri(
    `http://data.lblod.info/id/personen/${personId}`,
  )}  persoon:registratie ?registratie.
          ?registratie <https://data.vlaanderen.be/ns/generiek#gestructureerdeIdentificator> ?identificator.
          ?identificator<https://data.vlaanderen.be/ns/generiek#lokaleIdentificator> ?registrationNumber.
        }
      }
      
      }
    
  `;
}
export function getPersonNationalities(graph, appGraph, personId) {
  return `
    ${PREFIXES}
    select ?nationaliteitId where {
      graph ${sparqlEscapeUri(graph)}{
          ${sparqlEscapeUri(
    `http://data.lblod.info/id/personen/${personId}`,
  )} persoon:heeftNationaliteit ?nationaliteit.
        }
        graph ${sparqlEscapeUri(appGraph)}  {
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
