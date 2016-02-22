package com.opendoor

import com.opendoor.listings.{InMemoryListingsService, ListingsController}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.logging.filter.{LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.logging.modules.Slf4jBridgeModule

object Server extends HttpServer {

  override val disableAdminHttpServer = true

  override def modules = Seq(Slf4jBridgeModule)

  override def configureHttp(router: HttpRouter): Unit = {
    val inMemoryListingsService = new InMemoryListingsService(getClass.getResource("/listing-details.csv"))

    val filteringService = inMemoryListingsService

    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add(new ListingsController(filteringService))
  }
}
