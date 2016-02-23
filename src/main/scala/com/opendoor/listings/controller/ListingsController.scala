package com.opendoor.listings.controller

import com.opendoor.listings.model.FilterListingsRequest
import com.opendoor.listings.service.ListingsService
import com.opendoor.listings.utils.GeoJsonUtils
import com.twitter.finatra.http.Controller
import play.api.libs.json._

class ListingsController(listingsService: ListingsService) extends Controller {
  get("/listings") { request: FilterListingsRequest =>
    listingsService(request)
      .map(_.map(GeoJsonUtils.toFeature))
      .onSuccess(features => logger.info(s"Returning ${features.length} results"))
      .map(features => Json.toJson(GeoJsonUtils.toFeatureCollection(features)))
      .map(_.toString())
      .map(json => response.ok(json).contentTypeJson())
  }
}
