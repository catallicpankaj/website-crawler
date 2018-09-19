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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.dto.CrawledUrlsDTO;
import com.project.crawler.dto.ServiceStatus;

@Service("CrawlerServiceImpl")
public class CrawlerServiceImpl implements CrawlerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceImpl.class);

	ObjectMapper mapper= new ObjectMapper();

	@Override
	@Cacheable("crawlurlcache")
	@HystrixCommand(
			commandKey="CrawlerApiCommandKey",
			fallbackMethod = "handleFailureResponse_fallback", 
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "10000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "4")
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
						crawledUrl.getNavigations().parallelStream().forEach(navigation -> {
						if (navigation.absUrl("href").startsWith(url)) {
							crawledUrlDetails.addNodesItem(getAlllinksUnderSameDomain(navigation.attr("abs:href"),
									depth - 1, updatedNavigatedUrls));
						}
					});
				});
				return crawledUrlDetails;
			}
		}
	}

	public Optional<CrawledUrlsDTO> crawlForUrls(final String url) {
		try {
			Connection connection=Jsoup.connect(url);
			connection.followRedirects(false);
			Document htmlDocument = connection.get();
			Elements navigations = htmlDocument.select("a[href]");
			return Optional.of(new CrawledUrlsDTO(url, navigations));
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
