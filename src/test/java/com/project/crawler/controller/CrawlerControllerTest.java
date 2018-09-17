package com.project.crawler.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.dto.CrawledUrlsDTO;
import com.project.crawler.services.CrawlerService;

import brave.Tracer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CrawlerControllerTest {

	@Mock
	private Tracer tracer;

	/** The port. */
	@LocalServerPort
	private int port;

	/** The web test client. */
	@Autowired
	private WebTestClient webTestClient;
	
	private CrawledUrlDetailsDTO crawledUrlDetailsDTO;


	private ObjectMapper mapper;
	@MockBean
	private CrawlerService crawlerService;


	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
				.responseTimeout(Duration.ofSeconds(15000)).build();

		crawledUrlDetailsDTO = new CrawledUrlDetailsDTO("http://www.google.co.in");
		
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMandatoryParam() throws Exception {
		this.webTestClient.get().uri("/apis/crawl/links").accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testGetCrawledUrlDetailsDTO() throws Exception {

		//Mockito.when(crawlerService.crawlForUrls(Mockito.anyString())).thenReturn(Optional.of(crawledDetails));
		Mockito.when(crawlerService.getAlllinksUnderSameDomain(Mockito.anyString(), Mockito.anyInt(), Mockito.isNull()))
				.thenReturn(crawledUrlDetailsDTO);
		BodySpec<String, ?> bodySpec = this.webTestClient.get().uri("/apis/crawl/links?url=http://www.test.in&depth=5")
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectBody(String.class);
		assertNotNull(bodySpec.returnResult().getResponseBody());
		assertThat(mapper.writeValueAsString(bodySpec.returnResult().getResponseBody())).isNotEmpty()
				.contains("http://www.google.co.in");
	}

	

}
