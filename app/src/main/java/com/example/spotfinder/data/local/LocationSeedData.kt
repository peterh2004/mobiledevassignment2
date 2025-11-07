package com.example.spotfinder.data.local

/**
 * Provides helper data used to preload the Room database with Greater Toronto Area locations.
 */
object LocationSeedData {
    private data class CitySeed(val name: String, val baseLat: Double, val baseLng: Double)

    fun buildSeedLocations(): List<LocationEntity> {
        val anchors = listOf(
            LocationEntity(address = "Toronto City Hall, Toronto, ON", latitude = 43.653481, longitude = -79.383934),
            LocationEntity(address = "CN Tower, Toronto, ON", latitude = 43.642566, longitude = -79.387057),
            LocationEntity(address = "Square One, Mississauga, ON", latitude = 43.593055, longitude = -79.641678),
            LocationEntity(address = "Brampton City Hall, Brampton, ON", latitude = 43.684137, longitude = -79.759971),
            LocationEntity(address = "Ajax Community Centre, Ajax, ON", latitude = 43.857519, longitude = -79.009132),
            LocationEntity(address = "Pickering City Hall, Pickering, ON", latitude = 43.836101, longitude = -79.086943),
            LocationEntity(address = "Markham Civic Centre, Markham, ON", latitude = 43.856098, longitude = -79.337021),
            LocationEntity(address = "Downtown Oshawa, Oshawa, ON", latitude = 43.897545, longitude = -78.865791)
        )

        val seeds = listOf(
            CitySeed("Toronto", 43.65107, -79.347015),
            CitySeed("Scarborough", 43.773077, -79.257774),
            CitySeed("Mississauga", 43.589045, -79.644119),
            CitySeed("Brampton", 43.731548, -79.762418),
            CitySeed("Markham", 43.856098, -79.337021),
            CitySeed("Pickering", 43.835486, -79.089209),
            CitySeed("Ajax", 43.850857, -79.020373),
            CitySeed("Oshawa", 43.897092, -78.865791)
        )

        val generated = mutableListOf<LocationEntity>()
        generated.addAll(anchors)

        seeds.forEach { seed ->
            for (i in 1..13) {
                val latOffset = (((i % 5) - 2) * 0.005) + ((i / 7) * 0.002)
                val lngOffset = (((i % 4) - 1.5) * 0.006) + ((i / 5) * 0.003)
                val address = "${seed.name} Spot $i, ${seed.name}, ON"
                generated.add(
                    LocationEntity(
                        address = address,
                        latitude = seed.baseLat + latOffset,
                        longitude = seed.baseLng + lngOffset
                    )
                )
            }
        }

        return generated
    }
}
