package com.tesco.pma.service.colleague.client;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Contact;
import com.tesco.pma.colleague.api.FindColleaguesRequest;
import com.tesco.pma.colleague.api.Profile;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.tesco.pma.service.colleague.client.RestTemplateBasedColleagueApiClient.EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class RestTemplateBasedColleagueApiClientTest {
    private static final String COLLEAGUE_PATH = "/colleague/v2/colleagues/";
    private final BasicJsonTester jsonTester = new BasicJsonTester(RestTemplateBasedColleagueApiClientTest.class);

    private final String colleague1Json = jsonTester.from("colleague1.json").getJson();
    private final String colleague2Json = jsonTester.from("colleague2.json").getJson();
    private final String colleagueMultiJson = "{\"colleagues\": [" + String.join(",", colleague1Json, colleague2Json) + "]}";
    private final UUID colleague1Uuid = UUID.fromString("f09c44ee-f879-4828-b35e-1c1585bf20f8");

    @Test
    void findColleagueByColleagueUuidSucceeded() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(dispatcher(
                    request -> request.getPath().equals(COLLEAGUE_PATH + colleague1Uuid)
                            && request.getMethod().equals(HttpMethod.GET.toString()),
                    () -> response(200, jsonTester.from("colleague1.json").getJson())
            ));

            final var url = server.url(COLLEAGUE_PATH).toString();
            final var client = new RestTemplateBasedColleagueApiClient(url, new RestTemplate());

            final var colleague = client.findColleagueByColleagueUuid(colleague1Uuid);

            assertThat(colleague).isEqualTo(colleague1Expected());
        }
    }

    @Test
    void findColleagueByColleagueUuidNotFound() throws Exception {
        final var colleagueUuid = UUID.fromString("99f70ac2-f879-4828-b35e-1c1585bf20f8");
        final var notFoundJson = "{\n" +
                "    \"code\": \"404\",\n" +
                "    \"message\": \"Colleague with identifier 99f70ac2-39d3-493c-9595-f665798b7578 effective on 2021-07-12 wasn't found\"\n" +
                "}";
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(dispatcher(
                    request -> request.getPath().equals(COLLEAGUE_PATH + colleagueUuid)
                            && request.getMethod().equals(HttpMethod.GET.toString()),
                    () -> response(404, notFoundJson)
            ));
            final var url = server.url(COLLEAGUE_PATH).toString();
            final var client = new RestTemplateBasedColleagueApiClient(url, new RestTemplate());

            assertThatCode(() -> client.findColleagueByColleagueUuid(colleagueUuid))
                    .asInstanceOf(type(HttpClientErrorException.NotFound.class))
                    .returns(notFoundJson, RestClientResponseException::getResponseBodyAsString);
        }
    }

    @Test
    void findColleaguesEmptyQueryParams() throws Exception {
        final var errorJson = "{\n" +
                "    \"code\": \"400\",\n" +
                "    \"message\": \"At least one of the following query parameters required: workRelationships.locationUUID, colleagueUUID, externalSystems.iam.id, externalSystems.hcm.id, employeeId, countryCode, externalSystems.sourceSystem, workRelationships.managerUUID, updatedFrom , updatedTo\"\n" +
                "}";
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(dispatcher(
                    request -> COLLEAGUE_PATH.equals(request.getPath())
                            && HttpMethod.GET.toString().equals(request.getMethod()),
                    () -> response(400, errorJson)
            ));
            final var url = server.url(COLLEAGUE_PATH).toString();
            final var client = new RestTemplateBasedColleagueApiClient(url, new RestTemplate());

            assertThatCode(() -> client.findColleagues(new FindColleaguesRequest()))
                    .asInstanceOf(type(HttpClientErrorException.BadRequest.class))
                    .returns(errorJson, RestClientResponseException::getResponseBodyAsString);
        }
    }

    @Test
    void findColleaguesWithIamId() throws Exception {
        final var iamId = RandomStringUtils.randomAlphanumeric(10);
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(dispatcher(
                    request -> request.getPath().startsWith(COLLEAGUE_PATH)
                            && request.getMethod().equals(HttpMethod.GET.toString())
                            && iamId.equals(request.getRequestUrl().queryParameter(EXTERNAL_SYSTEMS_IAM_ID_PARAM_NAME)),
                    () -> response(200, colleagueMultiJson)
            ));
            final var url = server.url(COLLEAGUE_PATH).toString();
            final var client = new RestTemplateBasedColleagueApiClient(url, new RestTemplate());

            final var colleagues = client.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build());

            assertThat(colleagues).hasSize(2)
                    .element(0).isEqualTo(colleague1Expected());
        }
    }

    @Test
    void findColleaguesEmptyResult() throws Exception {
        final var iamId = RandomStringUtils.random(10);
        try (MockWebServer server = new MockWebServer()) {
            server.setDispatcher(dispatcher(request -> true, () -> response(200, "{\"colleagues\":[]}")));

            final var url = server.url(COLLEAGUE_PATH).toString();
            final var client = new RestTemplateBasedColleagueApiClient(url, new RestTemplate());

            final var colleagues = client.findColleagues(FindColleaguesRequest.builder().iamId(iamId).build());

            assertThat(colleagues).isEmpty();
        }
    }

    private Dispatcher dispatcher(Predicate<RecordedRequest> requestPredicate, Supplier<MockResponse> responseSupplier) {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (requestPredicate.test(request)) {
                    return responseSupplier.get();
                }
                return response(HttpStatus.NOT_FOUND.value(), "{\n" +
                        "    \"message\": \"No matching endpoint found\",\n" +
                        "    \"code\": \"404\"\n" +
                        "}");
            }
        };
    }

    private MockResponse response(int status, String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(status)
                .setBody(body);
    }

    private Colleague colleague1Expected() {
        final var colleague1Expected = new Colleague();
        colleague1Expected.setColleagueUUID(UUID.fromString("f09c44ee-f879-4828-b35e-1c1585bf20f8"));
        final var profile = new Profile();
        profile.setTitle("Mr.");
        profile.setFirstName("first-name");
        profile.setLastName("last-name");
        profile.setMiddleName("middle-name");
        profile.setGender("M");
        colleague1Expected.setProfile(profile);
        final var contact = new Contact();
        contact.setEmail("some@dummy.com");
        colleague1Expected.setContact(contact);
        return colleague1Expected;
    }
}