package com.project.crawler.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import brave.Span;
import brave.Tracer;

/**
 * The Class BaseServiceDTO.
 *
 * @author Pankaj
 * @param <T> the generic type
 */

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class CrawlerServiceDTO<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4557857727024338020L;

	/** The Constant serialVersionUID. */

	/** The Constant formatter. */
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-uuuu HH:mm:ss:SSS")
			.withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

	/** The service status. */
	protected ServiceStatus serviceStatus = ServiceStatus.SUCCESS;

	/** The error messages. */
	private List<ErrorMessage> errorMessages = new ArrayList<>();

	/** The data. */
	private T data;

	/** The trace id. */
	private String traceId;

	/** The span id. */
	private String spanId;
	
	/** The request received time. */
	private Instant requestReceivedTime;
	
	/** The response sent time. */
	private Instant responseSentTime;

	/** The req readable time. */
	private String reqReadableTime;

	/** The resp readable time. */
	private String respReadableTime;

	/** The page size. */
	private int pageSize;

	/** The page from. */
	private int pageFrom;

	/** The page to. */
	private int pageTo;

	/**
	 * Gets the service status.
	 *
	 * @return the service status
	 */
	public ServiceStatus getServiceStatus() {
		return serviceStatus;
	}

	/**
	 * Sets the service status.
	 *
	 * @param serviceStatus the new service status
	 */
	public void setServiceStatus(ServiceStatus serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * Gets the request received time.
	 *
	 * @return the request received time
	 */
	public Instant getRequestReceivedTime() {
		return requestReceivedTime;
	}

	/**
	 * Sets the request received time.
	 *
	 * @param requestReceivedTime the new request received time
	 */
	public void setRequestReceivedTime(Instant requestReceivedTime) {
		this.requestReceivedTime = requestReceivedTime;
		setReqReadableTime();
		
	}

	/**
	 * Gets the response sent time.
	 *
	 * @return the response sent time
	 */
	public Instant getResponseSentTime() {
		return responseSentTime;
	}

	/**
	 * Sets the response sent time.
	 *
	 * @param responseSentTime the new response sent time
	 */
	public void setResponseSentTime(Instant responseSentTime) {
		this.responseSentTime = responseSentTime;
		setRespReadableTime();
	}

	/**
	 * Gets the page size.
	 *
	 * @return the page size
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the page size.
	 *
	 * @param pageSize the new page size
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Gets the page from.
	 *
	 * @return the page from
	 */
	public int getPageFrom() {
		return pageFrom;
	}

	/**
	 * Sets the page from.
	 *
	 * @param pageFrom the new page from
	 */
	public void setPageFrom(int pageFrom) {
		this.pageFrom = pageFrom;
	}

	/**
	 * Gets the page to.
	 *
	 * @return the page to
	 */
	public int getPageTo() {
		return pageTo;
	}

	/**
	 * Sets the page to.
	 *
	 * @param pageTo the new page to
	 */
	public void setPageTo(int pageTo) {
		this.pageTo = pageTo;
	}

	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public List< ErrorMessage> getErrorMessages() {
		return Collections.unmodifiableList(errorMessages);
	}

	/**
	 *  
	 * For appending error Message.
	 *
	 * @param errorMessage the error message
	 */
	public void appendErrorMessage(ErrorMessage errorMessage) {
		if (null != errorMessage) {
			this.errorMessages.add(errorMessage);
		}
	}
	
	/**
	 * Adds the error message.
	 *
	 * @param errorMessagesToBeAdded the error messages
	 */
	public void addErrorMessages(List<? extends ErrorMessage> errorMessagesToBeAdded) {
		if (CollectionUtils.isNotEmpty(errorMessagesToBeAdded)) {
			this.errorMessages.addAll(errorMessagesToBeAdded);
		}
	}
	
	/**
	 * Gets the trace id.
	 *
	 * @return the trace id
	 */
	public String getTraceId() {
		return traceId;
	}

	/**
	 * Sets the trace id.
	 *
	 * @param traceId the new trace id
	 */
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	/**
	 * Gets the span id.
	 *
	 * @return the span id
	 */
	public String getSpanId() {
		return spanId;
	}

	/**
	 * Sets the span id.
	 *
	 * @param spanId the new span id
	 */
	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}



	/**
	 * Adds the trace info.
	 *
	 * @param spanAccessor the span accessor
	 */
	public void addTraceInfo(Tracer tracer) {
		if (null != tracer) {
			Span span = tracer.currentSpan();
			addTraceInfo(span);
		}
	}
	
	/**
	 * Adds the trace info.
	 *
	 * @param span the span
	 */
	public void addTraceInfo(Span span) {
		if (null != span) {
			String hexTraceId = span.context().traceIdString();
			long spanId = span.context().spanId();
			this.setTraceId(hexTraceId);
			this.setSpanId(Long.toString(spanId));
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BaseServiceDTO [entityStatus=" + serviceStatus + ", errorMessages=" + errorMessages + ", data=" + data
				+ ", requestRecievedTime=" + requestReceivedTime + ", responseSentTime=" + responseSentTime
				+ ", pageSize=" + pageSize + ", pageFrom=" + pageFrom + ", pageTo=" + pageTo + "]";
	}
	

	/**
	 * Gets the req readable time.
	 *
	 * @return the req readable time
	 */
	public String getReqReadableTime() {
		return reqReadableTime;
	}

	/**
	 * Sets the req readable time.
	 */
	public void setReqReadableTime() {
		this.reqReadableTime = formatter.format(getRequestReceivedTime());
	}

	/**
	 * Gets the resp readable time.
	 *
	 * @return the resp readable time
	 */
	public String getRespReadableTime() {
		return respReadableTime;
	}

	/**
	 * Sets the resp readable time.
	 */
	public void setRespReadableTime() {
		this.respReadableTime = formatter.format(getResponseSentTime());
	}
}
