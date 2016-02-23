package com.opendoor

import java.net.URI

import com.opendoor.listings.controller.ListingsController
import com.opendoor.listings.service.{InMemoryListingsService, PostgresListingsService}
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
    lazy val inMemoryListingsService =
      new InMemoryListingsService(getClass.getResource("/listing-details.csv"))

    lazy val optionalPostgresListingsService =
      PostgresListingsService(new URI(System.getenv("DATABASE_URL")))

    val filteringService =
      optionalPostgresListingsService.getOrElse(inMemoryListingsService)

    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add(new ListingsController(filteringService))
  }
}
