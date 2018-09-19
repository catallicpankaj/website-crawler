package com.project.crawler.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.io.Resources;
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.services.CrawlerServiceImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class CrawlerServiceImplTest {

	private CrawlerServiceImpl crawlerService = new CrawlerServiceImpl();

	private String htmlContent;
	private String url="https://www.google.co.in";

	@Before
	public void setup() throws Exception {
		htmlContent = fetchDataAsString("google_html_content.html");
		Document doc=Jsoup.parse(htmlContent);
		PowerMockito.mockStatic(Jsoup.class);
		Connection connection = Mockito.mock(Connection.class);
		PowerMockito.when(Jsoup.connect(url)).thenReturn(connection);
		PowerMockito.when(connection.get()).thenReturn(doc);
	}
	
	@Test
	public void testCrawlForUrls() {
		assertEquals(crawlerService.crawlForUrls(url).isPresent(),true);
	}
	
	@Test
	public void testGetAlllinksUnderSameDomain() throws IOException {
		CrawledUrlDetailsDTO crawlerUrlDetailsDTO=crawlerService.getAlllinksUnderSameDomain(url, 0, null);
		assertNotNull(crawlerUrlDetailsDTO.getNodes());
		assertEquals(crawlerUrlDetailsDTO.getNodes().size(), 0);
	}
	
	private static String fetchDataAsString(final String uri) throws IOException {
		return Resources.toString(Resources.getResource(uri), Charset.defaultCharset());
	}
}
