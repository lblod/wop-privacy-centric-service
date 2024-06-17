import { querySudo as query, updateSudo as update } from "@lblod/mu-auth-sudo";
import { uuid } from "mu";

import {
  getAccount,
  getReasonById,
  requestReadReason,
  getPersonInfo,
  getPersonNationalities,
  getBestuureenheidByAccountQuery,
} from "./queries";

const SESSION_GRAPH_URI =
  process.env.SESSION_GRAPH || "http://mu.semte.ch/graphs/sessions";

function checkNotEmpty(argument, message = "This cannont be empty!") {
  if (!argument?.length) {
    throw Error(message);
  }
}
export class RequestService {
  async processCheckPersonInfo(personId, sessionId) {
    checkNotEmpty(personId, "Person id must be set!");
    let accountUri = await this.getAccountBySession(sessionId);
    let { privacyGraph, orgGraph } =
      await this.getOrgGraphAndPrivacyGraphByAccount(accountUri, personId);
    checkNotEmpty(privacyGraph, "Privacy graph not found!");
    checkNotEmpty(orgGraph, "Org graph not found!");
    let responseBuilder = await this.buildPersonInfo(
      personId,
      privacyGraph,
      orgGraph,
    );
    if (responseBuilder.dateOfBirth?.length) {
      responseBuilder.dateOfBirth = "**********";
    }
    if (responseBuilder.registrationNumber?.length) {
      responseBuilder.registrationNumber = "***********";
    }
    if (responseBuilder.genderId?.length) {
      responseBuilder.genderId = "***********";
    }
    if (responseBuilder.nationalities?.length) {
      responseBuilder.nationalities = responseBuilder.nationalities.map(
        (_n) => "**********",
      );
    }
    responseBuilder.type = "person-information-asks";

    return this.getResponse(responseBuilder);
  }

  async processRead(request, sessionId) {
    let { data } = request;
    let { person, reason } = data.relationships;
    let reasonId = reason.data?.id;
    let personId = person.data?.id;
    checkNotEmpty(personId, "Person id must be set!");
    checkNotEmpty(reasonId, "Reason must be set!");

    let accountUri = await this.getAccountBySession(sessionId);

    let { privacyGraph, orgGraph } =
      await this.getOrgGraphAndPrivacyGraphByAccount(accountUri, personId);
    checkNotEmpty(privacyGraph, "Privacy graph not found!");
    checkNotEmpty(orgGraph, "Org graph not found!");
    let responseBuilder = await this.buildPersonInfo(
      personId,
      privacyGraph,
      orgGraph,
    );

    responseBuilder.reasonId = reasonId;
    responseBuilder.type = "person-information-requests";

    let reasonUri = await this.getReasonUri(reasonId);
    let requestReadReasonQuery = requestReadReason(
      privacyGraph,
      accountUri,
      personId,
      reasonUri,
    );

    await update(requestReadReasonQuery);
    return this.getResponse(responseBuilder);
  }


  async buildPersonInfo(personId, privacyGraph, orgGraph) {
    let responseBuilder = {};
    let queryParameters = {
      graph: privacyGraph,
      appGraph: orgGraph,
      personId,
    };
    let getPersonInfoQuery = getPersonInfo(
      queryParameters.graph,
      queryParameters.appGraph,
      queryParameters.personId,
    );

    let queryResult = await query(getPersonInfoQuery);

    if (queryResult.results.bindings.length) {
      const result = queryResult.results.bindings[0];
      responseBuilder.dateOfBirth = result.dateOfBirth?.value;
      responseBuilder.registrationNumber = result.registrationNumber?.value;
      responseBuilder.genderId = result.genderId?.value;
    }
    let getPersonNationalitiesQuery = getPersonNationalities(
      queryParameters.graph,
      queryParameters.appGraph,
      queryParameters.personId,
    );
    let queryResultNationalities = await query(getPersonNationalitiesQuery);
    responseBuilder.nationalities = queryResultNationalities.results.bindings
      .map((n) => n.nationaliteitId?.value)
      .filter((n) => n?.length > 0);
    return responseBuilder;
  }

  async getReasonUri(reasonId) {
    checkNotEmpty(reasonId, "reasonId cannot be null!");

    let getReasonCodeUriQuery = getReasonById(reasonId);

    let queryResult = await query(getReasonCodeUriQuery);
    if (queryResult.results.bindings.length) {
      const result = queryResult.results.bindings[0];
      let reasonUri = result.reasonUri?.value;
      checkNotEmpty(reasonUri, "code list not found");
      return reasonUri;
    } else {
      throw Error("code list not found");
    }
  }

  async getOrgGraphAndPrivacyGraphByAccount(accountUri, personId) {
    checkNotEmpty(accountUri, "No account uri!");
    let getPrivacyGraphByAccountQ = getBestuureenheidByAccountQuery(
      accountUri,
      personId,
    );
    const queryResult = await query(getPrivacyGraphByAccountQ);
    if (queryResult.results.bindings.length) {
      const result = queryResult.results.bindings[0];
      checkNotEmpty(
        result.graphBestuur?.value,
        "could not determine uuidBestuurseenheid",
      );
      let orgGraph = result.graphBestuur.value;
      return {
        privacyGraph: orgGraph + "/privacy",
        orgGraph,
      };
    } else {
      return { privacyGraph: null, orgGraph: null };
    }
  }
  async getAccountBySession(sessionId) {
    checkNotEmpty(sessionId, "No session id!");
    let getAccountQuery = getAccount(SESSION_GRAPH_URI, sessionId);
    const queryResult = await query(getAccountQuery);
    if (queryResult.results.bindings.length) {
      const result = queryResult.results.bindings[0];
      return result.account?.value;
    } else {
      return null;
    }
  }

  getResponse(responseBuilder) {
    let id = uuid();
    let registrationNumberAttribute = null;
    if (responseBuilder.registrationNumber) {
      registrationNumberAttribute = `"registration": "${responseBuilder.registrationNumber}" `;
    }
    let dateOfBirthAttribute = null;
    if (responseBuilder.dateOfBirth) {
      dateOfBirthAttribute = `"date-of-birth": "${responseBuilder.dateOfBirth}" `;
    }
    let attributes = [dateOfBirthAttribute, registrationNumberAttribute]
      .filter((f) => f?.length > 0)
      .join(",");

    let genderRelationship = null;
    if (responseBuilder.genderId) {
      genderRelationship = `
      "gender": {
        "data": {
          "type": "genders",
          "id": "${responseBuilder.genderId}"
        }
      }
      `;
    }
    let reasonRelationship = null;
    if (responseBuilder.reasonId) {
      reasonRelationship = `
      "reason": {
        "data": {
          "type": "request-reasons",
          "id": "${responseBuilder.reasonId}"
        }
      }
      `;
    }
    let nationalitiesRelationship = null;
    if (responseBuilder.nationalities?.length) {
      let nationalityJsonApi = responseBuilder.nationalities
        .map((nationality) => {
          return `
          {
            "type": "nationalities",
            "id": "${nationality}"
          }
    `;
        })
        .join(",");
      nationalitiesRelationship = `"nationalities":{
            "data" : [
              ${nationalityJsonApi}
            ]

        }`;
    }
    let relationships = [
      nationalitiesRelationship,
      genderRelationship,
      reasonRelationship,
    ]
      .filter((f) => f?.length > 0)
      .join(",");

    return `
    {
      "data": {
        "type": "${responseBuilder.type}",
        "id": "${id}",
        "attributes": {
         ${attributes}
        },
        "relationships": {
          ${relationships}
        }
      }
    }
    `;
  }
}
