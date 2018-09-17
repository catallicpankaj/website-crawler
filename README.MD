Crawler will visit all pages within the domain, but not follow the links to external sites such as Google or Twitter.

The output should be a simple structured site map, showing links to other pages under the same domain, 
links to external URLs and links to static content such as images for each respective page.

GET: http://localhost:2050/apis/v1.0/crawl/link?url=https://www.facebook.com/


Enhancements to be made to the microservice.

#RAML integration - Documentation for the api URL.
#Zuul integration - Proxy the api url for the consumers. Version control can be moved at proxy layer.
#Oauth2.0 - if any authorization is required.
#Hystrix - For fallback of any URL, if required. 
