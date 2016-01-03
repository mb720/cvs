# CVS
Scala and Play adventures

## Getting started

Before starting this application locally, make sure there is a [Postgres](http://www.postgresql.org/) database:
createdb -U postgres cvsDB

Then you can start it using a command like this and make it listen on port 9000 for HTTP requests and for HTTPS requests on port 9005.

activator "run -Dhttps.port=9005 -Ddb.default.url=jdbc:postgresql://localhost:5432/cvsDB -Ddb.default.username=postgres -Ddb.default.password=verySecret"

Finally, point your browser to https://localhost:9005 or http://localhost:9000 to see the home page.
