Crawler will visit all pages within the domain, but not follow the links to external sites such as Google or Twitter.

There is a use of Jsoup library so as to hit the URL and parse the HTML docs with the provided depth.

The output of the crawler service is wrapped under the standard response of the service. And the traceId is added for the response so that any request can be well tracked via loggers, or if required can be made available for Zipkin Integration.

URL to access : http://localhost:2050/apis/v1.0/crawl/link?url=https://www.facebook.com/ Exposed on Http Verb : GET

Basic Exception handling for the input fields is provided in the code.
In case user provides invalid inputs then response is handled gracefully with ErrorCode as EC0001 and respective Error Message. This is handled via @ControllerAdvice.

Netflix Hystrix API is used to handle the service response gracefully whenever circuit breaker is detected. For now for testing purpose if API takes more than 10 secs api fallsback. 
	Reference Document Used while developing: https://github.com/Netflix/Hystrix/wiki/Configuration

Enhancements to be made to the microservice.
------------------------------------------------
#RAML integration - Documentation for the api URL. 
#Zuul integration - Proxy the api url for the consumers. Version control can be moved at proxy layer. 
#Oauth2.0 - if any authorization is required. 
#Hystrix - For fallback of any URL, if required. 
#EnableCaching Strategy - For any URL's which are held in common we can use cache to enhance the service response.