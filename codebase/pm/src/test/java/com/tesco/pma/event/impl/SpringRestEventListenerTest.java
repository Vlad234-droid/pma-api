package com.tesco.pma.event.impl;

import java.util.Collections;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.event.controller.EventController;
import com.tesco.pma.event.monitor.BaseEventMonitor;

@ActiveProfiles("test")
@SpringBootTest(classes = {SpringRestEventListenerTest.class, RestEventExecutorConfig.class, SpringRestEventListener.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SpringRestEventListenerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @MockBean
    private EventController eventController;

    @MockBean
    private BaseEventMonitor eventMonitor;

    @Qualifier("restEventExecutor")
    @Autowired
    private Executor executor;

    @Value("${tesco.application.rest-event-executor.core-pool-size}")
    private int expectedCorePoolSize;

    @Value("${tesco.application.rest-event-executor.max-core-pool-size}")
    private int expectedMaxCorePoolSize;

    @Value("${tesco.application.rest-event-executor.queue-capacity}")
    private int expectedQueueCapacity;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private final HttpHeaders headers = buildHttpHeaders();

    @BeforeEach
    public void before() throws InterruptedException {
        Thread.sleep(10_000);
    }

    @Test
    void testConfiguration() {
        Assertions.assertNotNull(executor);
        Assertions.assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) executor;
        Assertions.assertEquals(1, expectedCorePoolSize);
        Assertions.assertEquals(1, expectedMaxCorePoolSize);
        Assertions.assertEquals(1, expectedQueueCapacity);
        Assertions.assertEquals(expectedCorePoolSize, threadPoolTaskExecutor.getCorePoolSize());
        Assertions.assertEquals(expectedMaxCorePoolSize, threadPoolTaskExecutor.getMaxPoolSize());
        Assertions.assertEquals(expectedQueueCapacity, threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity());
    }
/* todo
    @Test
    public void testThatExecutionIsAsync() throws Exception {
        long stepDuration = 10;
        long checkTimeout = stepDuration * 100;
        long sleepTimeout = checkTimeout * 3;
        int stepCounter = 0;

        AtomicInteger flag = new AtomicInteger(0);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(checkTimeout / 10);
                flag.incrementAndGet();
                Thread.sleep(sleepTimeout);
                return null;
            }
        }).when(eventController).processEvent(Mockito.any(Event.class));

        Event event = new EventSupport("Random");
        String post = OBJECT_MAPPER.writeValueAsString(event);
        HttpEntity<String> entity = new HttpEntity<>(post, headers);
        long begin = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort(), HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        boolean testPassed = false;
        while (stepCounter * stepDuration < checkTimeout) {
            if (flag.get() > 0) {
                testPassed = true;
                break;
            }
            Thread.sleep(stepDuration);
            stepCounter++;
        }

        long duration = System.currentTimeMillis() - begin;
        Mockito.verify(eventController, Mockito.atLeast(1)).processEvent(Mockito.any(Event.class));
        Assertions.assertTrue(testPassed);              // if Event was not processed (starting processing), this test failed
        Assertions.assertTrue(duration < sleepTimeout); // if Event processing was not async, this test failed
    }

    @Test
    public void testCountOfEventsExceedsCapacity() throws Exception {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return null;
            }
        }).when(eventController).processEvent(Mockito.any(Event.class));

        Event event = new EventSupport("Random");
        String post = OBJECT_MAPPER.writeValueAsString(event);
        HttpEntity<String> entity = new HttpEntity<>(post, headers);

        ResponseEntity<String> response1 = restTemplate.exchange(createURLWithPort(), HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response1.getStatusCode()); // immediately processing

        ResponseEntity<String> response2 = restTemplate.exchange(createURLWithPort(), HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response2.getStatusCode()); // fall in queue

        ResponseEntity<String> response3 = restTemplate.exchange(createURLWithPort(), HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response3.getStatusCode()); // exceeds capacity
    }

    private String createURLWithPort() {
        return "http://localhost:" + port + "/event/handle";
    }
*/
    private HttpHeaders buildHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
        return httpHeaders;
    }
}