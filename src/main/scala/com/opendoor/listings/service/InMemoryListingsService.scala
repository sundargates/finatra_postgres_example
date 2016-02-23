package com.opendoor.listings.service

import java.net.URL
import com.opendoor.listings.model.{Listing, FilterListingsRequest}
import com.twitter.logging.Logger
import com.twitter.util.Future


class InMemoryListingsService(uRL: URL) extends CsvBasedListingsService {

  val logger = Logger.get

  val listings = listingsFromUrl(uRL)

  logger.info(listings.length.toString)

  override def apply(request: FilterListingsRequest): Future[Seq[Listing]] =
    Future {
      listings.filter(Listing.satisfies(_, request))
    }
}
