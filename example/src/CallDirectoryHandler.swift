//
//  CallDirectoryHandler.swift
//  CallDirectoryHandler
//
//  Created by Omar Ramos Espinosa on 16/11/21.
//

import Foundation
import CallKit

class CallDirectoryHandler: CXCallDirectoryProvider {
  private var OPTION_BLOCK_CALL_ON_BLACK_LIST = "OPTION_BLOCK_CALL_ON_BLACK_LIST"; //NEVER CHANGE: KEY FOR GET IF MOST BLOCK OR NOT A CALL
  private var OPTION_LABEL_CALL_ON_SPAM_LIST = "OPTION_LABEL_CALL_ON_SPAM_LIST"; //NEVER CHANGE: KEY FOR GET IF MOST LABEL AS SPAM OR NOT A CALL
  private var BLOCKING_PHONE_NUMBERS = "BLOCKING_PHONE_NUMBERS"; //NEVER CHANGE: KEY FOR GET BLOCKING NUMBER
  private var SPAM_PHONE_NUMBERS = "SPAM_PHONE_NUMBERS"; //NEVER CHANGE: KEY FOR GET SPAM PHONE NUMBERS
  private var SPAM_LABELS = "SPAM_LABELS"; //NEVER CHANGE: KEY FOR GET SPAM LABELS
  
  private var groupAppSuiteName = "group.voisekdata" //CHANGE FOR YOUR CREATED GROUP NAME
  
  override func beginRequest(with context: CXCallDirectoryExtensionContext) {
    context.delegate = self
    // Check whether this is an "incremental" data request. If so, only provide the set of phone number blocking
    // and identification entries which have been added or removed since the last time this extension's data was loaded.
    // But the extension must still be prepared to provide the full set of data at any time, so add all blocking
    // and identification phone numbers if the request is not incremental.
    /*
     if context.isIncremental {
     addOrRemoveIncrementalBlockingPhoneNumbers(to: context)
     addOrRemoveIncrementalIdentificationPhoneNumbers(to: context)
     } else {
     addAllBlockingPhoneNumbers(to: context)
     addAllIdentificationPhoneNumbers(to: context)
     }
     */
    addAllBlockingPhoneNumbers(to: context)
    addAllIdentificationPhoneNumbers(to: context)
    context.completeRequest()
  }
  
  private func addAllBlockingPhoneNumbers(to context: CXCallDirectoryExtensionContext) {
    // Retrieve all phone numbers to block from data store. For optimal performance and memory usage when there are many phone numbers,
    // consider only loading a subset of numbers at a given time and using autorelease pool(s) to release objects allocated during each batch of numbers which are loaded.
    //
    // Numbers must be provided in numerically ascending order.
    if let userVoisekDefaults = UserDefaults(suiteName: groupAppSuiteName) {
      let canBlock = userVoisekDefaults.bool(forKey: OPTION_BLOCK_CALL_ON_BLACK_LIST);
      if canBlock == true {
        if let allPhoneNumbers = userVoisekDefaults.object(forKey: BLOCKING_PHONE_NUMBERS) as? [String]{
          for phoneNumber in allPhoneNumbers {
            if let int64PhoneNumber = Int64(phoneNumber){
              context.addBlockingEntry(withNextSequentialPhoneNumber: int64PhoneNumber)
            }
          }
        }
      }
      else{
        context.removeAllBlockingEntries()
      }
    }
    else{
      context.removeAllBlockingEntries()
    }
  }
  
  private func addOrRemoveIncrementalBlockingPhoneNumbers(to context: CXCallDirectoryExtensionContext) {
    // Retrieve any changes to the set of phone numbers to block from data store. For optimal performance and memory usage when there are many phone numbers,
    // consider only loading a subset of numbers at a given time and using autorelease pool(s) to release objects allocated during each batch of numbers which are loaded.
    /*
     let phoneNumbersToAdd: [CXCallDirectoryPhoneNumber] = [ 1_408_555_1234 ]
     for phoneNumber in phoneNumbersToAdd {
     context.addBlockingEntry(withNextSequentialPhoneNumber: phoneNumber)
     }
     
     let phoneNumbersToRemove: [CXCallDirectoryPhoneNumber] = [ 1_800_555_5555 ]
     for phoneNumber in phoneNumbersToRemove {
     context.removeBlockingEntry(withPhoneNumber: phoneNumber)
     }
     */
    // Record the most-recently loaded set of blocking entries in data store for the next incremental load...
  }
  
  private func addAllIdentificationPhoneNumbers(to context: CXCallDirectoryExtensionContext) {
    // Retrieve phone numbers to identify and their identification labels from data store. For optimal performance and memory usage when there are many phone numbers,
    // consider only loading a subset of numbers at a given time and using autorelease pool(s) to release objects allocated during each batch of numbers which are loaded.
    //
    // Numbers must be provided in numerically ascending order.
    if let userVoisekDefaults = UserDefaults(suiteName: groupAppSuiteName) {
      let canBlock = userVoisekDefaults.bool(forKey: OPTION_LABEL_CALL_ON_SPAM_LIST);
      if canBlock == true {
        if let labels = userVoisekDefaults.object(forKey: SPAM_LABELS) as? [String], let allPhoneNumbers = userVoisekDefaults.object(forKey: SPAM_PHONE_NUMBERS) as? [String]{
          for (phoneNumber, label) in zip(allPhoneNumbers, labels) {
            if let int64PhoneNumber = Int64(phoneNumber){
              context.addIdentificationEntry(withNextSequentialPhoneNumber: int64PhoneNumber, label: label)
            }
          }
        }
      }
      else{
        context.removeAllIdentificationEntries()
      }
    }
    else{
      context.removeAllIdentificationEntries()
    }
  }
  
  private func addOrRemoveIncrementalIdentificationPhoneNumbers(to context: CXCallDirectoryExtensionContext) {
    // Retrieve any changes to the set of phone numbers to identify (and their identification labels) from data store. For optimal performance and memory usage when there are many phone numbers,
    // consider only loading a subset of numbers at a given time and using autorelease pool(s) to release objects allocated during each batch of numbers which are loaded.
    /*
     let phoneNumbersToAdd: [CXCallDirectoryPhoneNumber] = [ 1_408_555_5678 ]
     let labelsToAdd = [ "New local business" ]
     
     for (phoneNumber, label) in zip(phoneNumbersToAdd, labelsToAdd) {
     context.addIdentificationEntry(withNextSequentialPhoneNumber: phoneNumber, label: label)
     }
     
     let phoneNumbersToRemove: [CXCallDirectoryPhoneNumber] = [ 1_888_555_5555 ]
     
     for phoneNumber in phoneNumbersToRemove {
     context.removeIdentificationEntry(withPhoneNumber: phoneNumber)
     }
     */
  }
  
}

extension CallDirectoryHandler: CXCallDirectoryExtensionContextDelegate {
  
  func requestFailed(for extensionContext: CXCallDirectoryExtensionContext, withError error: Error) {
    // An error occurred while adding blocking or identification entries, check the NSError for details.
    // For Call Directory error codes, see the CXErrorCodeCallDirectoryManagerError enum in <CallKit/CXError.h>.
    //
    // This may be used to store the error details in a location accessible by the extension's containing app, so that the
    // app may be notified about errors which occurred while loading data even if the request to load data was initiated by
    // the user in Settings instead of via the app itself.
  }
  
}
