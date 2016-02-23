# Listings service

*Yet another Finatra service!*

## Installation

- Install Sbt
- Install Scala
- `git clone git@github.com:sundargates/`
- `sbt compile`
- *(If you want to use a datastore)* Install postgres

## Usage

- *(If you use postgres)* DATABASE_URL should point to a valid postgres JDBC URI
- Compile code using `sbt compile`
- Run server using `sbt 'run-main com.opendoor.Server'`
- To build jars for deployment, `sbt compile stage`
- To *deploy* to heroku, just push the code to your heroku remote
