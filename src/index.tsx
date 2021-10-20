import { NativeModules } from 'react-native';

type VoisekAppExtension = {
  initCallService(
    requestCallService: boolean,
    callbackSuccess: Function,
    callbackFail: Function
  ): void;
  stopCallService(): void;
  doActiveBlockCallOnList(active: boolean): void;
  addBlockingPhoneNumbers(blockingPhoneNumbers: any[]): Promise<any>;
};

const { VoisekAppExtension } = NativeModules;

export { VoisekAppExtension as VoisekAppExtension };
