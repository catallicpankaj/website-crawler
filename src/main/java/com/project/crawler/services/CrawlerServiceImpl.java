package com.project.crawler.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.dto.CrawledUrlsDTO;
import com.project.crawler.dto.ServiceStatus;

@Service("CrawlerServiceImpl")
public class CrawlerServiceImpl implements CrawlerService {

	private static final String HREF_STRING = "href";
	private static final String SCRIPT = "script";
	private static final String LINK = "link";
	private static final String IMAGES = "img";
	private static final String ANCHOR = "a";
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceImpl.class);
	
	@Override
	@Cacheable("crawlurlcache")
	@HystrixCommand(
			commandKey="CrawlerApiCommandKey",
			fallbackMethod = "handleFailureResponse_fallback", 
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "30000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "50")
					}
			)
	public CrawledUrlDetailsDTO getAlllinksUnderSameDomain(String url, int depth, List<String> navigatedUrls) {
		LOGGER.debug("fetching navigations");
		if (depth < 0) {
			return null;
		} else {
			List<String> updatedNavigatedUrls = Optional.ofNullable(navigatedUrls).orElse(new ArrayList<>());
			if (updatedNavigatedUrls.contains(url)) {
				return null;
			} else {
				updatedNavigatedUrls.add(url);
				final CrawledUrlDetailsDTO crawledUrlDetails = new CrawledUrlDetailsDTO(url);
				crawlForUrls(url).ifPresent(crawledUrl -> {
					crawledUrl.stream().forEach(allUrls -> {
						allUrls.getNavigations().parallelStream().forEach(navigation -> {
							if (navigation.absUrl(HREF_STRING).startsWith(url) || navigation.absUrl("src").startsWith(url)) {
								if (navigation.tag().getName().equals(ANCHOR)) {
									crawledUrlDetails.addNodesItem(getAlllinksUnderSameDomain(navigation.attr("abs:href"), depth - 1, updatedNavigatedUrls));
								} else if (navigation.tag().getName().equals(IMAGES) || navigation.tag().getName().equals(SCRIPT)) {
									CrawledUrlDetailsDTO crawledUrlDetailsDTO = new CrawledUrlDetailsDTO(navigation.attr("abs:src"));
									crawledUrlDetails.addNodesItem(crawledUrlDetailsDTO);
								} else if (navigation.tag().getName().equals(LINK)) {
									CrawledUrlDetailsDTO crawledUrlDetailsDTO = new CrawledUrlDetailsDTO(navigation.attr("abs:href"));
									crawledUrlDetails.addNodesItem(crawledUrlDetailsDTO);
								}
							}
						});
					});
				});
				return crawledUrlDetails;
			}
		}
	}
	
	
	public Optional<List<CrawledUrlsDTO>> crawlForUrls(final String url) {
		try {
			Connection connection=Jsoup.connect(url);
			connection.followRedirects(false);
			Document htmlDocument = connection.get();
			Elements navigations = htmlDocument.select("a[href]");
			Elements imagesAndScripts = htmlDocument.select("[src]");
	        Elements cssElements = htmlDocument.select("link[href]");
	        List<CrawledUrlsDTO> allLinksList =new ArrayList<>();
	        allLinksList.add(new CrawledUrlsDTO(url, navigations));
	        allLinksList.add(new CrawledUrlsDTO(url, imagesAndScripts));
	        allLinksList.add(new CrawledUrlsDTO(url, cssElements));
			return Optional.of(allLinksList);
		} catch (final IOException | IllegalArgumentException e) {
			LOGGER.error(String.format("Error getting contents of url %s", url), e);
			return Optional.empty();
		}
	}
	
	private CrawledUrlDetailsDTO handleFailureResponse_fallback(String url,int depth, List<String> navigatedUrls) {
		final CrawledUrlDetailsDTO crawledUrlDetails = new CrawledUrlDetailsDTO(url);
		crawledUrlDetails.setApiStatus(ServiceStatus.ERROR);
		return crawledUrlDetails;
	}

}
