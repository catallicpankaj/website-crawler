package com.project.crawler.controller;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.project.crawler.dto.CrawledUrlDetailsDTO;
import com.project.crawler.dto.CrawlerServiceDTO;
import com.project.crawler.dto.ErrorMessage;
import com.project.crawler.dto.ServiceStatus;
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
	@Qualifier("CrawlerServiceImpl")
	private CrawlerService crawlerService;
	
	@Value("${crawler.default.depth}")
	private int defaultDepth;

	@GetMapping(value = "/links", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CrawlerServiceDTO<CrawledUrlDetailsDTO>> getAllLinks(HttpServletRequest request,
			@RequestParam(name = "url", required = true) String url,
			@RequestParam(value = "depth", required = false) String depth) throws InvalidInputException {

		LOGGER.info("Start method: getAllLinks");
		Instant requestReceivedTime = Instant.now();
		 request.setAttribute(REQUEST_RECEIVED_TIME,requestReceivedTime);
		 //To avoid Sonar exception, assigning the value to the new variable.
		 int passedDepth=0;
		 if(depth==null) {
			 passedDepth=defaultDepth;
		 }else {
			 validateInputData(depth,url);
			 passedDepth=Integer.parseInt(depth);
		 }
		
		CrawledUrlDetailsDTO crawlerResponse = crawlerService.getAlllinksUnderSameDomain(url, passedDepth, null);
		CrawlerServiceDTO<CrawledUrlDetailsDTO> crawlerServiceDTO = new CrawlerServiceDTO<>();
		crawlerServiceDTO.setData(crawlerResponse);
		crawlerServiceDTO.setResponseSentTime(Instant.now());
		crawlerServiceDTO.setRequestReceivedTime(requestReceivedTime);
		crawlerServiceDTO.setServiceStatus(ServiceStatus.SUCCESS);
		crawlerServiceDTO.setPageSize(1);
		crawlerServiceDTO.setPageFrom(1);
		crawlerServiceDTO.setPageTo(1);
		crawlerServiceDTO.addTraceInfo(this.tracer);
		LOGGER.info("End method: getAllLinks");
		return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.OK);
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
		throw new InvalidInputException(errorMessages);
	}

}
