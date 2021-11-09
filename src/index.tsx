import { NativeModules } from 'react-native';

interface VoisekAppExtensionType {
  initCallService(
    requestCallService: boolean,
    callbackSuccess: Function,
    callbackFail: Function
  ): void;
  setNotificationData(
    listeningChannelTitle?: string,
    listeningChannelDesc?: string,
    listeningBackgroundNotTitle?: string,
    listeningBackgroundNotDesc?: string,
    listeningStartNotTitle?: string,
    listeningStartNotDesc?: string,
    listeningEndNotTitle?: string,
    listeningEndNotDesc?: string
  ): void;
  stopCallService(): void;
  cancelNotifications(timerForNotToCancel?: number): void;
  doActiveBlockCallOnList(active: boolean): void;
  addBlockingPhoneNumbers(blockingPhoneNumbers: any[]): Promise<any>;
  showAFullScreenNotification(
    title: string,
    desc: string,
    timerForNotToShow: number
  ): void;
}

const { VoisekAppExtension } = NativeModules;

const VoisekAppExtensionModule = VoisekAppExtension as VoisekAppExtensionType;

export { VoisekAppExtensionModule as VoisekAppExtension };
