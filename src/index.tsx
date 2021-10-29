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
    listeningNotTitle?: string,
    listeningNotDesc?: string
  ): void;
  stopCallService(): void;
  cancelNotifications(): void;
  doActiveBlockCallOnList(active: boolean): void;
  addBlockingPhoneNumbers(blockingPhoneNumbers: any[]): Promise<any>;
  showAFullScreenNotification(title: string, desc: string): void;
};

const { VoisekAppExtension } = NativeModules;

export { VoisekAppExtension as VoisekAppExtension };
