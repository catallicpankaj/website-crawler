package com.project.crawler.data;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.project.crawler.dto.CrawledUrlDetailsDTO;

@Component("CrawlUrlRepositoryImpl")
public class CrawlUrlRepositoryImpl implements CrawlUrlRepository {

	private static final String KEY = "CRAWLER";
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Object> redisTemplate;
	private HashOperations hashOperations;

	@PostConstruct
	private void init() {
		hashOperations = redisTemplate.opsForHash();
	}

	@Override
	public CrawledUrlDetailsDTO findDataByUrlAndDepth(int hashKey) {
		return (CrawledUrlDetailsDTO) hashOperations.get(KEY, hashKey);
	}

	@Override
	public void saveDataByUrlAndDepth(int hashKey, CrawledUrlDetailsDTO crawledUrlDetailsDTO) {
		hashOperations.put(KEY, hashKey, crawledUrlDetailsDTO);
	}
}
