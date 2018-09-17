package com.project.crawler.services;


import java.util.List;

import com.project.crawler.dto.CrawledUrlDetailsDTO;


public interface CrawlerService {

	public CrawledUrlDetailsDTO getAlllinksUnderSameDomain(String url,int depth,List<String> urls);


}
