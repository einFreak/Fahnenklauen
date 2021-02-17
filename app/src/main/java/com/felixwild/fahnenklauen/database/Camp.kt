package com.felixwild.fahnenklauen.database

import com.google.firebase.firestore.GeoPoint

class Camp {

    var iId = 0
    var campName: String = "Westernhohe"
    var kidsActive = true
    var tagOnly = true
    var numberParticipants = 0
    var additionalRules: String? = null
    var currentRating = 0
    var location: GeoPoint = GeoPoint(0.0, 0.0)

}