Crawler will visit all pages within the domain, but not follow the links to external sites such as Google or Twitter.

The output should be a simple structured site map, showing links to other pages under the same domain, 
links to external URLs and links to static content such as images for each respective page.

#RAML integration https://www.baeldung.com/raml-restful-api-modeling-language-tutorial
#Zuul integration
#Eureka Registry & Discovery
#Java 8 
#v2.0 Rx
#Oauth2.0
#PowerMockito TDD
#React + Node
#Hystrix
#Streaming on thirdparty.
#TaskExecutors for Streaming.
#Demostrate- Exception Handling


#POST 
facebook.com - 

End User:
POST:- http://localhost:1010/apis/v1.0/stateless/crawl/url
Payload
https://www.facebook.com/

GET: http://localhost:2020/apis/crawl/link?url=https://www.facebook.com/