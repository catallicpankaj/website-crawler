package com.project.crawler.exception.handler;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.project.crawler.constants.CrawlerConstants;
import com.project.crawler.controller.CrawlerController;
import com.project.crawler.dto.CrawlerServiceDTO;
import com.project.crawler.dto.ErrorMessage;
import com.project.crawler.dto.ServiceStatus;
import com.project.crawler.exception.FallbackException;
import com.project.crawler.exception.InvalidInputException;

import brave.Span;
import brave.Tracer;

@ControllerAdvice(assignableTypes = CrawlerController.class)
@RequestMapping(produces = "application/vnd.error+json")
public class RestResponseExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseExceptionHandler.class);

	public static final String REQUEST_RECEIVED_TIME = "requestReceivedTime";

	@Autowired
	private Tracer tracer;
	
	@SuppressWarnings("all")
	@ExceptionHandler(Throwable.class)
	protected @ResponseBody ResponseEntity<CrawlerServiceDTO<Void>> handleRestException(Throwable ex, WebRequest request) {
		Instant requestReceivedTime = (Instant) (request.getAttribute(REQUEST_RECEIVED_TIME, WebRequest.SCOPE_REQUEST));
		if (requestReceivedTime == null) {
			LOGGER.warn("Request Recieved Time is Empty, Adding a current Time to it");
			requestReceivedTime = Instant.now();
		}
		Span span = this.tracer.currentSpan();
		String hexTraceId = span.context().traceIdString();
		String spanId = Long.toString(span.context().spanId());

		CrawlerServiceDTO<Void> crawlerServiceDTO = new CrawlerServiceDTO<Void>();
		crawlerServiceDTO.setServiceStatus(ServiceStatus.ERROR);
		crawlerServiceDTO.setResponseSentTime(Instant.now());
		crawlerServiceDTO.setRequestReceivedTime(requestReceivedTime);
		crawlerServiceDTO.setPageFrom(1);
		crawlerServiceDTO.setPageSize(1);
		crawlerServiceDTO.setPageTo(1);
		crawlerServiceDTO.setTraceId(hexTraceId);
		crawlerServiceDTO.setSpanId(spanId);

		if (isAssignableTo(ex, InvalidInputException.class)) {
			InvalidInputException invalidInputException = (InvalidInputException) getExceptionType(ex,
					InvalidInputException.class);
			List<ErrorMessage> errorMessages = invalidInputException.getErrorMessages();
			String msg = MessageFormat.format("{0} - Invalid input received in request. Error messages are: {1}",
					CrawlerConstants.EC_INVALID_INPUT, errorMessages);
			LOGGER.error(msg, ex);
			addErrorInDTO(crawlerServiceDTO, errorMessages);
			return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.BAD_REQUEST);
		} else if(isAssignableTo(ex, FallbackException.class)) {
			FallbackException fallbackException = (FallbackException) getExceptionType(ex,
					FallbackException.class);
			List<ErrorMessage> errorMessages = fallbackException.getErrorMessages();
			String msg = MessageFormat.format("{0} - Service fallback enabled. Error messages are: {1}",
					CrawlerConstants.EC_HYSTRIX_FALLBACK, errorMessages);
			LOGGER.error(msg,ex);
			addErrorInDTO(crawlerServiceDTO, errorMessages);
			return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		else {
				String msg = MessageFormat.format("{0} - Data/Entity not found.",
						CrawlerConstants.EC_GENERIC_INTERNAL_SERVER_ERROR);
				LOGGER.error(msg, ex);
				addErrorInDTO(crawlerServiceDTO, CrawlerConstants.EC_GENERIC_INTERNAL_SERVER_ERROR,
						this.extractExceptionCause(ex));
				return new ResponseEntity<>(crawlerServiceDTO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private boolean isAssignableTo(Throwable ex, Class<? extends Throwable> exceptionType) {
		Throwable cause = ex.getCause();
		return (exceptionType.isInstance(ex) || (null != cause && cause.getClass().isAssignableFrom(exceptionType)));
	}
	
	private void addErrorInDTO(CrawlerServiceDTO<Void> crawlerServiceDTO,  List<ErrorMessage> errorMessagesToAppend) {
		crawlerServiceDTO.addErrorMessages(errorMessagesToAppend);
	
	}
	
	private void addErrorInDTO(CrawlerServiceDTO<Void> crawlerServiceDTO, String errorCode, String message) {
		crawlerServiceDTO.appendErrorMessage(new ErrorMessage(errorCode, message));
	}
	
	private String extractExceptionCause(Throwable t) {
		StringBuilder sb = new StringBuilder();
		Throwable cause = t;
		Throwable oldCause = null;
		int i = 1;
		while ((null != cause) && (oldCause != cause)) {
			if (i == 1) {
				sb.append(cause.getMessage());
			} else {
				sb.append(" Issue Occured due to: [ ").append(cause.getMessage()).append(" ]");
			}
			i++;
			oldCause = cause;
			cause = cause.getCause();
		}
		return sb.toString();
	}
	
	private Throwable getExceptionType(Throwable ex, Class<? extends Throwable> exceptionType) {
		Throwable exception = null;
		Throwable cause = ex.getCause();
		if (exceptionType.isInstance(ex)) {
			exception = ex;
		} else if ((null != cause && cause.getClass().isAssignableFrom(exceptionType))) {
			exception = cause;
		}
		return exception;

	}
	
}
