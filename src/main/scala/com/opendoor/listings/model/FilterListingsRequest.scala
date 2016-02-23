package com.opendoor.listings.model

import com.twitter.finatra.request._

case class FilterListingsRequest(
  @QueryParam minPrice: Option[Long] = None,
  @QueryParam maxPrice: Option[Long] = None,
  @QueryParam minBed: Option[Int] = None,
  @QueryParam maxBed: Option[Int] = None,
  @QueryParam minBath: Option[Int] = None,
  @QueryParam maxBath: Option[Int] = None
)
