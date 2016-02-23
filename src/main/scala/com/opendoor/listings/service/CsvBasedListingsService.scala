package com.opendoor.listings.service

import java.net.URL

import com.opendoor.listings.model.Listing
import kantan.csv._
import kantan.csv.ops._

trait CsvBasedListingsService extends ListingsService {
  implicit val codec = scala.io.Codec.ISO8859

  implicit val rowDecoder: RowDecoder[Listing] = RowDecoder { ss =>
    for {
      id <- CellDecoder[Int].decode(ss, 0)
      street <- CellDecoder[String].decode(ss, 1)
      status <- CellDecoder[String].decode(ss, 2)
      price <- CellDecoder[Long].decode(ss, 3)
      bedrooms <- CellDecoder[Int].decode(ss, 4)
      bathrooms <- CellDecoder[Int].decode(ss, 5)
      squareFeet <- CellDecoder[Int].decode(ss, 6)
      latitude <- CellDecoder[Double].decode(ss, 7)
      longitude <- CellDecoder[Double].decode(ss, 8)
    } yield new Listing(id, street, status, price, bedrooms, bathrooms, squareFeet, latitude, longitude)
  }

  def listingsFromUrl(uRL: URL): Seq[Listing] =
    uRL
      .asCsvReader[Listing](',', true)
      .toSeq
      .flatMap(_.toOption)
}
