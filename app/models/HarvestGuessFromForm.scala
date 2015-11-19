package models

/**
  * Created by Matthias Braun on 11/18/2015.
  */
//case class FarmerPersonalData(name: String, postAdress: String, telefonNumber: String, emailAdress: String)
//
//case class FarmerCompanyData(companyNr: String, vatRegistrationNr: String, inspectionBody: String, memberInBioUnion:Boolean)
//
//case class BankData(iban: String)
//
//case class HarvestGuessFromForm(contactData: FarmerPersonalData, companyData: FarmerCompanyData, bankData: BankData)
case class FarmerPersonalData(name: String, postalAddress: String, phoneNumber: String, emailAddress: String)

case class HarvestGuessFromForm(contactData: FarmerPersonalData)
