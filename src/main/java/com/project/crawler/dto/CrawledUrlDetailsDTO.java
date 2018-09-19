package com.project.crawler.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CrawledUrlDetailsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1821725853524866377L;

	private String url;
	private List<CrawledUrlDetailsDTO> nodes;
	@JsonIgnore
	private ServiceStatus apiStatus;

	public ServiceStatus getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(ServiceStatus apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<CrawledUrlDetailsDTO> getNodes() {
		return nodes;
	}

	public void setNodes(List<CrawledUrlDetailsDTO> nodes) {
		this.nodes = nodes;
	}

	public CrawledUrlDetailsDTO(String url) {
		this.url = url;
	}

	public CrawledUrlDetailsDTO url(String url) {
		this.url = url;
		return this;
	}

	public CrawledUrlDetailsDTO nodes(List<CrawledUrlDetailsDTO> nodes) {
		this.nodes = nodes;
		return this;
	}

	public CrawledUrlDetailsDTO addNodesItem(CrawledUrlDetailsDTO nodesItems) {
		if (nodes == null) {
			nodes = new ArrayList<>();
		}
		if (nodesItems != null) {
			nodes.add(nodesItems);
		}
		return this;
	}

	@Override
	public boolean equals(final java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CrawledUrlDetailsDTO crawledUrlDetailsDTO = (CrawledUrlDetailsDTO) o;
		return Objects.equals(url, crawledUrlDetailsDTO.url) && Objects.equals(nodes, crawledUrlDetailsDTO.nodes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, nodes);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("class CrawledUrlDetailsDTO {\n");
		sb.append("    url: ").append(toIndentedString(url)).append("\n");
		sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	private String toIndentedString(final java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
