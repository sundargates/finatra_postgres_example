package com.opendoor.listings.service

import java.net.URI

import com.opendoor.listings.model.{FilterListingsRequest, Listing}
import com.twitter.finagle.postgres
import com.twitter.finagle.postgres.{Client, Row, RowReader}
import com.twitter.logging.Logger
import com.twitter.util.Future

object PostgresListingsService {
  val ListingsTable = "opendoor_listings"
  val BulkUpdatesSize = 10
  private[this] val logger = Logger.get

  def apply(dbUri: URI): Option[PostgresListingsService] = {
    val regex = """postgres:\/+(\w+)(:(.+)@(.+):(\d+)\/(.+))?""".r
    logger.info(s"dbUri=${dbUri}")
    dbUri.toString match {
      case regex(username, null, null, null, null, null) =>
        logger.info("Only username found as part of the URI")
        logger.info(s"username=${username}")
        logger.info(s"database=${username}")
        val client = Client("localhost:5432", username, None, username, hostConnectionLimit = 4)
        Some(new PostgresListingsService(client, ListingsTable))
      case regex(username, _, password, host, port, database) =>
        logger.info(s"username=${username}")
        logger.info(s"host=${host}")
        logger.info(s"port=${port}")
        logger.info(s"database=${database}")
        val client = Client(s"${host}:${port}", username, Some(password), database, hostConnectionLimit = 4)
        Some(new PostgresListingsService(client, ListingsTable))
      case _ =>
        logger.warning(s"Couldn't match dbUrl=${dbUri.toString} with regex=${regex.toString()}")
        None
    }
  }
}

class PostgresListingsService(
  client: postgres.Client,
  listingsTable: String
) extends ListingsService {

  private[this] val logger = Logger.get

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
    val SquareFeet = "sq_ft"
    val Latitude = "lat"
    val Longitude = "lng"
  }

  object ListingsRowReader extends RowReader[Listing] {
    def apply(row: Row): Listing = {
      Listing(
        id = row.get[Int](FieldNames.Id),
        street = row.get[String](FieldNames.Street),
        status = row.get[String](FieldNames.Status),
        price = row.get[Integer](FieldNames.Price).longValue(),
        bedrooms = row.get[Integer](FieldNames.Bedrooms).intValue(),
        bathrooms = row.get[Integer](FieldNames.Bathrooms).intValue(),
        squareFeet = row.get[Integer](FieldNames.SquareFeet).intValue(),
        latitude = row.get[java.lang.Double](FieldNames.Latitude).doubleValue(),
        longitude = row.get[java.lang.Double](FieldNames.Longitude).doubleValue()
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
      s"${baseQuery} WHERE ${optionalParams mkString " AND "}"
    }
  }

  override def apply(request: FilterListingsRequest): Future[Seq[Listing]] = {
    val query = selectQuery(request)
    logger.debug(s"query=${query}")
    client.select(query) { ListingsRowReader(_) }
  }
}
