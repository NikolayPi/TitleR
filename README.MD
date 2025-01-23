# TitleR (Title Retriever)
This service retrieves \<title\> contents of HTML documents.
It uses jsoup for parsing and http4s as HTTP server.

## How to run
```shell
sbt compile
sbt test    // optional
sbt run 
```

## Example

As an example, let's crawl two pages. One of the URLs is wrong, so the crawler will return an error response for this URL (CrawlerError), 
the other URL will be processed correctly yielding TitleResponse data type.
```shell
curl localhost:8000/titles --data '{"urls": ["https://stackoverflow.com/questions/7172784/how-do-i-post-json-data-with-cl", "https://http4s.org/v1/docs/client.html"]}' --request GET

[{"{"CrawlerError":{"url":"https://stackoverflow.com/some-missing-page","error":"unexpected HTTP status: 404 Not Found for request GET https://stackoverflow.com/some-missing-page"}},{"TitleResponse":{"url":"https://http4s.org/","result":{"title":"http4s"}}}":{"url":"https://stackoverflow.com/some-missing-page","error":"unexpected HTTP status: 404 Not Found for request GET https://stackoverflow.com/some-missing-page"}},{"TitleResponse":{"url":"https://http4s.org/","title":{"title":"http4s"}}}]
```

If the request is ill-formed, then an error will be returned, for example
```shell
curl localhost:8000/titles --data '{"surls": ["https://google.com"]}' --request GET

{"error":"Invalid message body: Could not decode JSON: {\n  \"surls\" : [\n    \"https://google.com\"\n  ]\n}"}%      ```
```

If title tag is missing in HTML, then `title` field in TitleResponse will be an empty string.
## Requirements
Recent JVM and SBT installed.
