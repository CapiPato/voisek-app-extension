import { NativeModules } from 'react-native';

import VoisekCallStateManager from './VoisekCallStateManager';

type VoisekAppExtension = {
  initCallService(
    requestCallService: boolean,
    callbackSuccess: Function,
    callbackFail: Function
  ): void;
  doActiveBlockCallOnList(active: boolean): void;
  addBlockingPhoneNumbers(blockingPhoneNumbers: any[]): Promise<any>;
};

const { VoisekAppExtension } = NativeModules;

export { VoisekAppExtension as VoisekAppExtension, VoisekCallStateManager };
