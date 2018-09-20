<H1>Use Case</H1>

Crawler will visit all pages within the domain, but not follow the links to external sites such as Google or Twitter.

There is a use of Jsoup library so as to hit the URL and parse the HTML docs with the provided depth.

The output of the crawler service is wrapped under the standard response of the service. And the traceId is added for the response so that any request can be well tracked via loggers, or if required can be made available for Zipkin Integration.

<b>URL to access</b> : http://localhost:2050/apis/v1.0/crawl/links?url=https://www.facebook.com/&depth=100 
		
		depth is optional param
Exposed on Http Verb : GET 

<h2>Exception Handling </h2>

Basic Exception handling for the input fields is provided in the code.
In case user provides invalid inputs then response is handled gracefully with ErrorCode as EC0001 and respective Error Message. This is handled via @ControllerAdvice.

<h2>Fault Tolerance </h2>
 <b>Netflix Hystrix</b> API is used to handle the service response gracefully whenever circuit breaker is detected. For now for testing purpose if API takes more than 10 secs api fallsback. This can be tested where depth is increased so that timeout can be introduced.
	Reference Document Used while developing: https://github.com/Netflix/Hystrix/wiki/Configuration

<h2>Caching</h2>
EnableCaching Strategy - For any URL's which are held in common for the requests we can use cache to enhance the service response. This can be tested by hitting the same URL again, the data appears from cache.

<h2> Versioning provided for Redis Cache Integration </h2>
Added Support for Redis standalone server (pre-requisite: redis server should be up and running on host 127.0.0.1 and port 6379. These details are configurable in application.properties. Upgraded the version to v2.0 for API using redis cache.
<b>URL to access</b> : http://localhost:2050/apis/v2.0/crawl/links?url=https://www.facebook.com/&depth=100 
		
		depth is optional param
Exposed on Http Verb : GET 


<h1>Enhancements to be made to the microservice.</h1>
------------------------------------------------
<b>RAML integration </b>- Documentation for the api URL. <br/> 
<b>Zuul integration  </b>- Proxy the api url for the consumers. Version control can be moved at proxy layer. <br/>
<b>Oauth2.0 </b>- if any authorization is required. <br/>

