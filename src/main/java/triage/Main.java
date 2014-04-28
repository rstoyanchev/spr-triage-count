package triage;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {

	public static final String COMPONENTS_URL =
			"https://jira.spring.io/rest/api/2/project/SPR/components";

	public static final String SEARCH_URL = "https://jira.spring.io/rest/api/2/search";

	public static final String TRIAGE_JQL =
			"project = SPR AND fixVersion = \"Waiting for Triage\" AND component = {componentName} ORDER BY summary ASC";



	public static void main(String[] args) {

		RestTemplate restTemplate = new RestTemplate();

		ParameterizedTypeReference<List<Map>> typeRef = new ParameterizedTypeReference<List<Map>>() {};
		ResponseEntity<List<Map>> result = restTemplate.exchange(COMPONENTS_URL, HttpMethod.GET, null, typeRef);

		UriComponentsBuilder searchUriBuilder = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
				.queryParam("jql", TRIAGE_JQL)
				.queryParam("startAt", 0)
				.queryParam("maxResults", 0);

		int count = getTriageCount(restTemplate, searchUriBuilder, "EMPTY");
		System.out.println("No Component, " + count);

		for (Map<String, Object> row : result.getBody()) {
			String componentName = (String) row.get("name");
			String quotedComponentName = "\"" + componentName + "\"";
			count = getTriageCount(restTemplate, searchUriBuilder, quotedComponentName);
			System.out.println(componentName + ", " + count);
		}
	}

	protected static int getTriageCount(RestTemplate restTemplate, UriComponentsBuilder uriBuilder, String component) {
		URI searchUri = uriBuilder.buildAndExpand(component).encode().toUri();
		Map<String, Object> map = restTemplate.getForObject(searchUri, Map.class);
		return (int) map.get("total");
	}

}
