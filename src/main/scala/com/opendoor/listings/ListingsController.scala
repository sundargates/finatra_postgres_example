package com.opendoor.listings

import java.net.URL

import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.QueryParam
import com.twitter.logging.Logger
import com.twitter.util.Future
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic.codecs._
import play.api.libs.json._
import play.extras.geojson._

//id,street,status,price,bedrooms,bathrooms,sq_ft,lat,lng
case class Listing(
  id: Int,
  street: String,
  status: String,
  price: Long,
  bedrooms: Int,
  bathrooms: Int,
  squareFeet: Int,
  latitude: Double,
  longitude: Double
)

object GeoJsonUtils {
  def toFeature(listing: Listing): Feature[LatLng] = {
    Feature(
      geometry = Point(LatLng(listing.latitude, listing.longitude)),
      properties = Some(
        Json.obj(
          "id" -> listing.id.toString,
          "price" -> listing.price,
          "street" -> listing.street.toString,
          "bedrooms" -> listing.bedrooms,
          "bathrooms" -> listing.bathrooms,
          "sq_ft" -> listing.squareFeet
        )
      )
    )
  }

  def toFeatureCollection[C](features: Seq[Feature[C]]): FeatureCollection[C] =
    FeatureCollection(features.toList)
}

object Listing {
  private[this] def sFunc[T](optionalX: Option[T], y: T)(comp: (T, T) => Boolean) =
    optionalX.map(x => comp(x, y)).getOrElse(true)

  def satisfies(listing: Listing, request: FilterListingsRequest): Boolean = {
    import listing._
    val satisfiers =
      sFunc(request.minPrice, price) { _ <= _ } ::
        sFunc(request.maxPrice, price) { _ >= _ } ::
        sFunc(request.minBed, bedrooms) {_ <= _ } ::
        sFunc(request.maxBed, bedrooms) { _ >= _ } ::
        sFunc(request.minBath, bathrooms) {_ <= _ } ::
        sFunc(request.maxBath, bathrooms) { _ >= _ } ::
        Nil

    satisfiers reduceRight (_ && _)
  }
}

case class FilterListingsRequest(
  @QueryParam minPrice: Option[Long] = None,
  @QueryParam maxPrice: Option[Long] = None,
  @QueryParam minBed: Option[Int] = None,
  @QueryParam maxBed: Option[Int] = None,
  @QueryParam minBath: Option[Int] = None,
  @QueryParam maxBath: Option[Int] = None
)

trait ListingsService {
  def filter(request: FilterListingsRequest): Future[Seq[Listing]]
}

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

class InMemoryListingsService(uRL: URL) extends CsvBasedListingsService {

  val logger = Logger.get

  val listings = listingsFromUrl(uRL)

  logger.info(listings.length.toString)

  override def filter(request: FilterListingsRequest): Future[Seq[Listing]] =
    Future {
      listings.filter(Listing.satisfies(_, request))
    }
}

class ListingsController(listingsService: ListingsService) extends Controller {
  get("/listings") { request: FilterListingsRequest =>
    listingsService
      .filter(request)
      .map(_.map(GeoJsonUtils.toFeature))
      .onSuccess(features => logger.info(s"Returning ${features.length} results"))
      .map(features => Json.toJson(GeoJsonUtils.toFeatureCollection(features)))
      .map(_.toString())
      .map(json => response.ok(json).contentTypeJson())
  }
}
