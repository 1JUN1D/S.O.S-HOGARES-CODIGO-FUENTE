package com.uv.mapa_usuario.api


data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val start_location: Location,
    val end_location: Location,
    val steps: List<Step> // Añadir este campo
)

data class Step(
    val distance: Distance,
    val duration: Duration,
    val start_location: Location,
    val end_location: Location,
    val polyline: Polyline,
    val html_instructions: String // Añadir este campo
)

data class Polyline(
    val points: String
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Location(
    val lat: Double,
    val lng: Double
)


