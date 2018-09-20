package com.project.crawler.data;

import com.project.crawler.dto.CrawledUrlDetailsDTO;


public interface CrawlUrlRepository{
	
	CrawledUrlDetailsDTO findDataByUrlAndDepth(int hashKey);

	void saveDataByUrlAndDepth(int hashKey, CrawledUrlDetailsDTO crawledUrlDetailsDTO);   

}
