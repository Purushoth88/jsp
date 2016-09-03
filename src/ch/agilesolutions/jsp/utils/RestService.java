package ch.agilesolutions.jsp.utils;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestService {

	private static final String REST_SERVICE_URL = "http://www.agile-solutions.ch/JDO/rest/ids/spaces";

	public List<IDSSpace> getSpace() {
		
		List<IDSSpace> spaces = Collections.EMPTY_LIST;

		Client client = ClientBuilder.newBuilder().register(GsonMessageBodyHandler.class).build();

		Response response = client.target(REST_SERVICE_URL).request(MediaType.APPLICATION_JSON).get();

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			spaces = response.readEntity(new GenericType<List<IDSSpace>>() {
			});
		}
		return spaces;

	}
}
