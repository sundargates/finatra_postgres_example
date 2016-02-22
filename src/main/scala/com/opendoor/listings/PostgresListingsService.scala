package com.opendoor.listings

import com.twitter.finagle.postgres
import com.twitter.finagle.postgres.{RowReader, Row}
import com.twitter.util.Future

object PostgresListingsService {
  val ListingsTable = "listings"
}

class PostgresListingsService(
  client: postgres.Client,
  listingsTable: String
) extends ListingsService {

  private[this] val Operators = new {
    val LEQ = "<="
    val GEQ = ">="
  }

  private[this] val FieldNames = new {
    val Id = "id"
    val Street = "street"
    val Status = "status"
    val Price = "price"
    val Bedrooms = "bedrooms"
    val Bathrooms = "bathrooms"
    val SquareFeet = "sq_feet"
    val Latitude = "latitude"
    val Longitude = "longitude"
  }

  object ListingsRowReader extends RowReader[Listing] {
    def apply(row: Row): Listing = {
      Listing(
        id = row.get[Int](FieldNames.Id),
        street = row.get[String](FieldNames.Street),
        status = row.get[String](FieldNames.Status),
        price = row.get[Long](FieldNames.Price),
        bedrooms = row.get[Int](FieldNames.Bedrooms),
        bathrooms = row.get[Int](FieldNames.Bathrooms),
        squareFeet = row.get[Int](FieldNames.SquareFeet),
        latitude = row.get[Double](FieldNames.Latitude),
        longitude = row.get[Double](FieldNames.Longitude)
      )
    }
  }

  private[this] def paramOption[T](
    field: String,
    operator: String,
    optionalParam: Option[T]
  ): Option[String] = {
    optionalParam
      .map { param => s"${field} ${operator} ${param}"}
  }

  private[this] def selectQuery(request: FilterListingsRequest): String = {
    val baseQuery = s"SELECT * FROM ${listingsTable}"
    val optionalParams = Seq(
      paramOption(FieldNames.Price, Operators.GEQ, request.minPrice),
      paramOption(FieldNames.Price, Operators.LEQ, request.maxPrice),
      paramOption(FieldNames.Bedrooms, Operators.GEQ, request.minBed),
      paramOption(FieldNames.Bedrooms, Operators.LEQ, request.maxBed),
      paramOption(FieldNames.Bathrooms, Operators.GEQ, request.minBath),
      paramOption(FieldNames.Bathrooms, Operators.LEQ, request.maxBath)
    ).flatten
    if (optionalParams.isEmpty) {
      baseQuery
    } else {
      s"${baseQuery} WHERE ${optionalParams mkString " "}"
    }
  }

  override def filter(request: FilterListingsRequest): Future[Seq[Listing]] = {
    client.select(selectQuery(request)) { ListingsRowReader(_) }
  }
}
