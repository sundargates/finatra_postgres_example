package com.opendoor.listings.model

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
