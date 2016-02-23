package com.opendoor.listings.utils

import com.opendoor.listings.model.Listing
import play.api.libs.json.Json
import play.extras.geojson.{Feature, FeatureCollection, LatLng, Point}

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
