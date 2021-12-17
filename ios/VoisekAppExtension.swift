import Foundation
@objc(VoisekAppExtension)
class VoisekAppExtension: NSObject {
    private var OPTION_BLOCK_CALL_ON_BLACK_LIST = "OPTION_BLOCK_CALL_ON_BLACK_LIST";
    private var OPTION_LABEL_CALL_ON_SPAM_LIST = "OPTION_LABEL_CALL_ON_SPAM_LIST";
    private var OPTION_CAN_CHECK_CALL_STATE = "OPTION_CAN_CHECK_CALL_STATE";
    private var BLOCKING_PHONE_NUMBERS = "BLOCKING_PHONE_NUMBERS";
    private var SPAM_PHONE_NUMBERS = "SPAM_PHONE_NUMBERS";
    private var SPAM_LABELS = "SPAM_LABELS";
    private var directoryExtensionName: String = "";
    private var voisekSuiteName: String = "";
    private var voisekDefaults: UserDefaults?
    private var callObserver: CXCallObserver!
    
    func ensureVoisekDefaults() -> UserDefaults {
        guard let def = voisekDefaults else {
            fatalError("UserDefaults must be initialized first")
        }
        return def
    }
    
    
    @objc(initCallService:
            withDirectoryExtensionId:
            withRequestCallService:
            withCallbackSuccess:
            withCallbackFail:)
    func initCallService(groupAppName: String, directoryExtensionId: String, requestCallService: Bool, callbackSuccess:RCTResponseSenderBlock,callbackFail:RCTResponseSenderBlock) -> Void {
        directoryExtensionName = directoryExtensionId
        voisekSuiteName = groupAppName
        voisekDefaults = UserDefaults.init(suiteName: voisekSuiteName)!
        ensureVoisekDefaults().set(requestCallService, forKey: OPTION_CAN_CHECK_CALL_STATE)
        ensureVoisekDefaults().set(true, forKey: OPTION_LABEL_CALL_ON_SPAM_LIST)
        callbackSuccess(["Resolve"])
    }
    
    @objc(setNotificationData:withListeningChannelDesc:
            withListeningBackgroundNotTitle:
            withListeningBackgroundNotDesc:
            withListeningStartNotTitle:
            withListeningStartNotDesc:
            withListeningEndNotTitle:
            withListeningEndNotDesc:)
    func setNotificationData(listeningChannelTitle: String, listeningChannelDesc: String, listeningBackgroundNotTitle: String, listeningBackgroundNotDesc: String, listeningStartNotTitle: String, listeningStartNotDesc: String, listeningEndNotTitle: String, listeningEndNotDesc: String) -> Void {
    }
    
    @objc(stopCallService)
    func stopCallService() -> Void {
        ensureVoisekDefaults().persistentDomain(forName: voisekSuiteName)?.forEach {
            ensureVoisekDefaults().removeObject(forKey: $0.key)
        }
        CXCallDirectoryManager.sharedInstance.reloadExtension(withIdentifier: directoryExtensionName) { (error) in
            print("reloaded extension: \(String(describing: error))")
        }
    }
    
    @objc(cancelNotifications:)
    func cancelNotifications(timerForNotToCancel: Int) -> Void {
    }
    
    @objc(reloadCallExtension)
    func reloadCallExtension() -> Void {
        CXCallDirectoryManager.sharedInstance.reloadExtension(withIdentifier: directoryExtensionName) { (error) in
            print("reloaded extension: \(String(describing: error))")
        }
    }
    
    @objc(doActiveBlockCallOnList:)
    func doActiveBlockCallOnList(active: Bool) -> Void {
        ensureVoisekDefaults().set(active, forKey: OPTION_BLOCK_CALL_ON_BLACK_LIST)
    }
    
    @objc(addBlockingPhoneNumbers:withResolver:withRejecter:)
    func addBlockingPhoneNumbers(blockingPhoneNumbers: Array<AnyObject>, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        var phoenNumbers: Array<String> = []
        for blockingPhoneNumberData in blockingPhoneNumbers {
            if var blockingPhoneNumber = blockingPhoneNumberData["phoneNumber"]{
                blockingPhoneNumber = (blockingPhoneNumber as! String).replacingOccurrences(of: "+", with: "")
                phoenNumbers.append(blockingPhoneNumber as! String)
            }
        }
        ensureVoisekDefaults().set(phoenNumbers, forKey: BLOCKING_PHONE_NUMBERS)
        resolve("Did Add Blocking")
    }
    
    @objc(addSpamPhoneNumbers:withResolver:withRejecter:)
    func addSpamPhoneNumbers(spamPhoneNumbers: Array<NSDictionary>, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        var phoenNumbers: Array<String> = []
        var labels: Array<String> = []
        for spamPhoneNumberData in spamPhoneNumbers {
            if var spamPhoneNumber = spamPhoneNumberData["phoneNumber"], let spamLabel = spamPhoneNumberData["label"]{
                spamPhoneNumber = (spamPhoneNumber as! String).replacingOccurrences(of: "+", with: "")
                phoenNumbers.append(spamPhoneNumber as! String)
                labels.append(spamLabel as! String)
            }
        }
        ensureVoisekDefaults().set(phoenNumbers, forKey: SPAM_PHONE_NUMBERS)
        ensureVoisekDefaults().set(labels, forKey: SPAM_LABELS)
        resolve("Did Add Spam")
    }
    
    @objc(showAFullScreenNotification:withDesc:
            withTimerForNotToShow:)
    func showAFullScreenNotification(title: String, desc: String, timerForNotToShow: Int) -> Void {
    }
    
    @objc(checkCallDetection:withRejecter:)
    func checkCallDetection(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        var isOnCall = false;
        for call in CXCallObserver().calls {
            if call.hasEnded == false {
                isOnCall = true
            }
        }
        resolve(isOnCall)
    }
}
