import { NativeModules } from 'react-native';

type VoisekAppExtension = {
  initCallService(
    requestCallService: boolean,
    callbackSuccess: Function,
    callbackFail: Function
  ): void;
  setNotificationData(
    listeningChannelTitle?: string,
    listeningChannelDesc?: string,
    listeningStartNotTitle?: string,
    listeningStartNotDesc?: string,
    listeningEndNotTitle?: string,
    listeningEndNotDesc?: string
  ): void;
  stopCallService(): void;
  cancelNotifications(): void;
  doActiveBlockCallOnList(active: boolean): void;
  addBlockingPhoneNumbers(blockingPhoneNumbers: any[]): Promise<any>;
  showAFullScreenNotification(title: string, desc: string): void;
};

const { VoisekAppExtension } = NativeModules;

export { VoisekAppExtension as VoisekAppExtension };
