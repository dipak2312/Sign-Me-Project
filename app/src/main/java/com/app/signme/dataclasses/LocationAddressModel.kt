package com.app.signme.dataclasses

/**
 * Class used to parse the address which got from google places api
 * @property fullAddress String
 * @property address String
 * @property city String
 * @property state String
 * @property zipCode String
 * @property country String
 */
class LocationAddressModel {
    var fullAddress: String = ""
    var address: String = ""
    var city: String = ""
    var state: String = ""
    var zipCode: String = ""
    var country: String = ""
}