# Readme

*Yet another Finatra service!*

## Technologies used
- Finatra (Sinatra like framework for writing Finagle HTTP services)
- kantan.csv (CSV parser. Allows parsing into *case classes*)
- play-geojson (Utils/Datastructures for geojson representation)
- Finagle-postgres (Finagle Client for postgres)
- ***Scala**, SBT and the other usual suspects!*

## Endpoints
- **GET**
  - `/listings?min_price=100000&max_price=200000&min_bed=2&max_bed=2&min_bath=2&max_bath=2`
