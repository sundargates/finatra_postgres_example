package com.opendoor.listings.service

import com.opendoor.listings.model.{Listing, FilterListingsRequest}
import com.twitter.finagle.Service

trait ListingsService extends Service[FilterListingsRequest, Seq[Listing]]
