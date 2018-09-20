package com.project.crawler.controller;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.crawler.constants.CrawlerConstants;
import com.project.crawler.data.CrawlUrlRepository;
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.dto.CrawlerServiceDTO;
import com.project.crawler.dto.ErrorMessage;
import com.project.crawler.dto.ServiceStatus;
import com.project.crawler.exception.FallbackException;
import com.project.crawler.exception.InvalidInputException;
import com.project.crawler.services.CrawlerService;

import brave.Span;
import brave.Tracer;

@RestController
public class CrawlerController {

	@Autowired
	protected Tracer tracer;

	protected Span getCurrentSpan() {
		return ((null != tracer) ? tracer.currentSpan() : null);
	}

	public static final String REQUEST_RECEIVED_TIME = "requestReceivedTime";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerController.class);

	@Autowired
	@Qualifier("CrawlUrlRepositoryImpl")
	CrawlUrlRepository crawlUrlRepo;
	
	@Autowired
	@Qualifier("CrawlerServiceImpl")
	private CrawlerService crawlerService;
	
	@Value("${crawler.default.depth}")
	private int defaultDepth;

	/**
	 * Crawl for all the links with the provided depth. Default depth is 1.
	 * 
	 * @param request -ServletRequest this helps to maintain the requestReceivedTime
	 *                in the response, in case any exception occurs during the
	 *                request processing.
	 * @param url     - manadatory param- URL to be crawled.
	 * @param depth   - optional param- depth to which crawling need to be done.
	 * @return CrawlerServiceDTO- This is the base model for the api response.
	 * @throws InvalidInputException - Handle invalid inputs.
	 * @throws FallbackException - Handle Hystrix Fallback.
	 */
	@GetMapping(value = "/v1.0/crawl/links", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CrawlerServiceDTO<CrawledUrlDetailsDTO>> getAllLinks(HttpServletRequest request,
			@RequestParam(name = "url", required = true) String url,
			@RequestParam(value = "depth", required = false) String depth)
			throws InvalidInputException, FallbackException {
		LOGGER.info("Start method: getAllLinks");
		Instant requestReceivedTime = Instant.now();
		request.setAttribute(REQUEST_RECEIVED_TIME, requestReceivedTime);
		// To avoid Sonar exception, assigning the value to the new variable.
		int passedDepth = 0;
		if (depth == null) {
			passedDepth = defaultDepth;
		} else {
			validateInputData(depth, url);
			passedDepth = Integer.parseInt(depth);
		}
		CrawledUrlDetailsDTO crawlerResponse = crawlerService.getAlllinksUnderSameDomain(url, passedDepth, null);
		CrawlerServiceDTO<CrawledUrlDetailsDTO> crawlerServiceDTO = setServiceResponse(requestReceivedTime,
				crawlerResponse);
		LOGGER.info("End method: getAllLinks");
		return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.OK);
	}

	
	/**
	 * Crawl for all the links with the provided depth. Default depth is 1.
	 * There is standalone redis configuration linked to this api. 
	 * Redis server with default configuration can be made up and It will link the data via CrawlUrlRepository bean.
	 * 
	 * @param request -ServletRequest this helps to maintain the requestReceivedTime
	 *                in the response, in case any exception occurs during the
	 *                request processing.
	 * @param url     - manadatory param- URL to be crawled.
	 * @param depth   - optional param- depth to which crawling need to be done.
	 * @return CrawlerServiceDTO- This is the base model for the api response.
	 * @throws InvalidInputException - Handle invalid inputs.
	 * @throws FallbackException - Handle Hystrix Fallback.
	 */
	@GetMapping(value = "/v2.0/crawl/links/", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CrawlerServiceDTO<CrawledUrlDetailsDTO>> getAllLinksWithRedisIntegration(HttpServletRequest request,
			@RequestParam(name = "url", required = true) String url,
			@RequestParam(value = "depth", required = false) String depth)
			throws InvalidInputException, FallbackException {
		LOGGER.info("Start method: getAllLinks");
		Instant requestReceivedTime = Instant.now();
		request.setAttribute(REQUEST_RECEIVED_TIME, requestReceivedTime);
		// To avoid Sonar exception, assigning the value to the new variable.
		int passedDepth = 0;
		if (depth == null) {
			passedDepth = defaultDepth;
		} else {
			validateInputData(depth, url);
			passedDepth = Integer.parseInt(depth);
		}
		String hashKeyString = url + Integer.toString(passedDepth);
		CrawledUrlDetailsDTO crawlerResponse = new CrawledUrlDetailsDTO(url);
		int redisHashKey=hashKeyString.hashCode();
		LOGGER.info("-------------------Trying to get the response from Redis-------------------");
		crawlerResponse = crawlUrlRepo.findDataByUrlAndDepth(redisHashKey);
		if (crawlerResponse == null) {
			LOGGER.info("-------------------Data not found in Redis-------------------");
			crawlerResponse = crawlerService.getAlllinksUnderSameDomain(url, passedDepth, null);
			LOGGER.info("-------------------Saving the response to Redis-------------------");
			crawlUrlRepo.saveDataByUrlAndDepth(redisHashKey, crawlerResponse);
		}
		CrawlerServiceDTO<CrawledUrlDetailsDTO> crawlerServiceDTO = setServiceResponse(requestReceivedTime,
				crawlerResponse);
		LOGGER.info("End method: getAllLinks");
		return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.OK);
	}
	private CrawlerServiceDTO<CrawledUrlDetailsDTO> setServiceResponse(Instant requestReceivedTime,
			CrawledUrlDetailsDTO crawlerResponse) throws FallbackException {
		if (crawlerResponse.getApiStatus() != null && crawlerResponse.getApiStatus().equals(ServiceStatus.ERROR)) {
			List<ErrorMessage> errorMessages = new ArrayList<>();
			ErrorMessage errorMessage = new ErrorMessage(CrawlerConstants.EC_HYSTRIX_FALLBACK,
					"Service Currently unavailable, please retry in sometime.");
			errorMessages.add(errorMessage);
			throw new FallbackException(errorMessage);
		} else {
			CrawlerServiceDTO<CrawledUrlDetailsDTO> crawlerServiceDTO = new CrawlerServiceDTO<>();
			crawlerServiceDTO.setData(crawlerResponse);
			crawlerServiceDTO.setResponseSentTime(Instant.now());
			crawlerServiceDTO.setRequestReceivedTime(requestReceivedTime);
			crawlerServiceDTO.setServiceStatus(ServiceStatus.SUCCESS);
			crawlerServiceDTO.setPageSize(1);
			crawlerServiceDTO.setPageFrom(1);
			crawlerServiceDTO.setPageTo(1);
			crawlerServiceDTO.addTraceInfo(this.tracer);
			return crawlerServiceDTO;
		}

	}
	
	
	
	private void validateInputData(String depth,String url) throws InvalidInputException {
		List<ErrorMessage> errorMessages = new ArrayList<>();
		UrlValidator urlValidator = new UrlValidator();
		if (!StringUtils.isNumeric(depth)) {
			String message = MessageFormat.format(CrawlerConstants.ERR_INVALID_INPUT_DEPTH, depth);
			LOGGER.info(CrawlerConstants.EC_INVALID_INPUT, message);
			ErrorMessage errorMessage = new ErrorMessage(CrawlerConstants.EC_INVALID_INPUT, message);
			errorMessages.add(errorMessage);
		} 
		if (!urlValidator.isValid(url)) {
			String message = MessageFormat.format(CrawlerConstants.ERR_INVALID_INPUT_URL, url);
			LOGGER.info(CrawlerConstants.EC_INVALID_INPUT, message);
			ErrorMessage errorMessage = new ErrorMessage(CrawlerConstants.EC_INVALID_INPUT, message);
			errorMessages.add(errorMessage);
		}
		if(CollectionUtils.isNotEmpty(errorMessages)) {
			throw new InvalidInputException(errorMessages);
		}
	}
	
}
