#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(VoisekAppExtension, NSObject)

RCT_EXTERN_METHOD(initCallService:(NSString*)groupAppName
                  withDirectoryExtensionId: (NSString*)directoryExtensionId
                  withRequestCallService: (BOOL)requestCallService
                  withCallbackSuccess:(RCTResponseSenderBlock)callbackSuccess
                  withCallbackFail:(RCTResponseSenderBlock)callbackFail)

RCT_EXTERN_METHOD(setNotificationData:(NSString*)listeningChannelTitle
                  withListeningChannelDesc: (NSString*)listeningChannelDesc
                  withListeningBackgroundNotTitle: (NSString*)listeningBackgroundNotTitle
                  withListeningBackgroundNotDesc: (NSString*)listeningBackgroundNotDesc
                  withListeningStartNotTitle: (NSString*)listeningStartNotTitle
                  withListeningStartNotDesc: (NSString*)listeningStartNotDesc
                  withListeningEndNotTitle: (NSString*)listeningEndNotTitle
                  withListeningEndNotDesc: (NSString*)listeningEndNotDesc)

RCT_EXTERN_METHOD(stopCallService)

RCT_EXTERN_METHOD(cancelNotifications:(NSNumber*)timerForNotToCancel)

RCT_EXTERN_METHOD(doActiveBlockCallOnList:(BOOL)active)

RCT_EXTERN_METHOD(addBlockingPhoneNumbers:(NSArray*)blockingPhoneNumbers
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(addSpamPhoneNumbers:(NSArray*)spamPhoneNumbers
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(showAFullScreenNotification:(NSString*)title
                  withDesc: (NSString*)desc
                  withTimerForNotToShow: (NSNumber*)timerForNotToShow)

@end
